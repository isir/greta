import numpy as np
import pandas as pd
import os


def find_segments(values, threshold=0.05):
    """
    Returns a list of (start_frame, end_frame) where 'values' > threshold.
    """
    segments = []
    n = len(values)
    active = False
    seg_start = None

    for i in range(n):
        if not active and values[i] > threshold:
            active = True
            seg_start = i
        elif active and values[i] <= threshold:
            active = False
            segments.append((seg_start, i - 1))
            seg_start = None

    # If still active at the end
    if active and seg_start is not None:
        segments.append((seg_start, n - 1))

    return segments


def discrete_symmetric_reflection(df, columns, threshold=0.05):
    """
    For each column in 'columns', detect active segments (above threshold)
    and enforce frame-by-frame symmetry around the segment's midpoint in time.

    Concretely, if a segment is [start, end], for offset in [0..(length//2)],
    we set:
        new_val = (values[start+offset] + values[end-offset]) / 2
        values[start+offset] = new_val
        values[end-offset]   = new_val

    This produces a mirrored (symmetric) shape around the segment midpoint.

    Parameters:
      df: A pandas DataFrame containing the AU columns of interest.
      columns: List of columns to process (e.g., the AUs in one group, or all AUs).
      threshold: Activation threshold for detecting active segments.

    Returns:
      A new DataFrame with symmetrical shapes in each detected segment.
    """
    import numpy as np

    df_out = df.copy()
    n = len(df_out)

    for col in columns:
        values = df_out[col].values.copy()

        # 1) Identify active segments
        segments = find_segments(values, threshold=threshold)

        # 2) For each segment, enforce symmetry by reflection around the midpoint
        for (start, end) in segments:
            length = end - start + 1
            half_len = length // 2
            for offset in range(half_len):
                left = start + offset
                right = end - offset
                # Average the two
                new_val = (values[left] + values[right]) / 2.0
                values[left] = new_val
                values[right] = new_val

        df_out[col] = values

    return df_out


def discrete_symmetric_groups(df, groups, threshold=0.05):
    """
    Applies discrete_symmetric_reflection to each group's columns separately.

    groups: a dict like {
        'smile': {'cols': ['AU12_r', 'AU25_r', 'AU06_r', 'AU02_r']},
        'frown': {'cols': ['AU10_r', 'AU09_r', 'AU04_r']},
        'other': {'cols': ['AU15_r', 'AU14_r']}
    }
    """
    df_out = df.copy()
    for gname, info in groups.items():
        df_out = discrete_symmetric_reflection(
            df_out,
            columns=info['cols'],
            threshold=threshold
        )
    return df_out



def exclusive_group_activation(df, groups, threshold=0.05):
    """
    Enforces exclusive activation across facial expression groups.

    For each frame:
      - If no group is currently active, choose the group with the highest representative AU above threshold.
      - Once a group is chosen, continue using that group until its representative falls to or below the threshold.
      - When the current group fades to 0, switch to the new group (if any) that is active.
      - For each frame, set all AUs of non-selected groups to 0.

    Parameters:
      df: DataFrame containing the interpolated AU values.
      groups: Dictionary mapping group names to a dictionary with:
              'rep': representative AU column,
              'cols': list of AU columns for the group.
              Example:
              {
                  'smile': {'rep': 'AU12_r', 'cols': ['AU12_r', 'AU25_r', 'AU06_r', 'AU02_r']},
                  'frown': {'rep': 'AU10_r', 'cols': ['AU10_r', 'AU09_r', 'AU04_r']},
                  'other': {'rep': 'AU15_r', 'cols': ['AU15_r', 'AU14_r']}
              }
      threshold: Minimum activation to consider a group “on.”

    Returns:
      A new DataFrame where, at each frame, only one group's AUs are nonzero.
    """
    n_frames = len(df)
    df_out = df.copy()

    # The currently active group (None initially)
    current_group = None

    # Process each frame sequentially.
    for i in range(n_frames):
        # For each group, read the representative activation.
        rep_values = {g: df.loc[i, info['rep']] for g, info in groups.items()}

        # If no group is active, choose the group with the highest rep value above threshold.
        if current_group is None:
            candidates = {g: val for g, val in rep_values.items() if val > threshold}
            if candidates:
                # Choose the one with maximum representative value.
                current_group = max(candidates, key=candidates.get)
            else:
                current_group = None

        # If a group is already active, check if its activation has faded.
        if current_group is not None:
            if rep_values[current_group] <= threshold:
                # The current group has faded; allow switching.
                candidates = {g: val for g, val in rep_values.items() if val > threshold}
                current_group = max(candidates, key=candidates.get) if candidates else None

        # At this point, current_group is the one that will be active at frame i.
        # For each group that is not current_group, set all of its columns to 0.
        for g, info in groups.items():
            if g != current_group:
                for col in info['cols']:
                    df_out.loc[i, col] = 0
        # (Optionally, you could leave the current group as is.)

    return df_out

def identify_activation_indices(values):
    """
    Identify the indices of rising and falling edges in the sequence of values.
    This function returns two lists:
    1. rising_indices: Indices where the value changes from 0 to the activation value.
    2. falling_indices: Indices where the value changes from the activation value to 0.

    :param values: The array of values (AU activation values).
    :return: A list of rising_indices and falling_indices.
    """
    rising_indices = []
    falling_indices = []

    length = len(values)

    for i in range(1, length):
        # Rising edge: from 0 to activation value
        if values[i] > 0 and values[i - 1] == 0:
            rising_indices.append(i)

        # Falling edge: from activation value to 0
        if values[i] == 0 and values[i - 1] > 0:
            falling_indices.append(i)

    # Handle activation starting at the first index
    if values[0] > 0:
        rising_indices = [0] + rising_indices

    # Handle activation ending at the last index
    if values[-1] > 0:
        falling_indices.append(length)

    return rising_indices, falling_indices


def interpolate_activations(df, columns, N, K):
    """
    Interpolates N frames before and after an activation for the specified columns.
    If the number of frames between two successive interpolated segments is less than K,
    then merge the two segments into one big segment (preserving the interpolation before the
    start of the first segment and after the end of the second segment).

    :param df: DataFrame with activation data.
    :param columns: List of columns to perform interpolation on.
    :param N: Number of frames for interpolation.
    :param K: Minimum gap between activations to consider them separate.
    :return: DataFrame with interpolated values.
    """
    for col in columns:
        values = df[col].values  # Extract the column's values as a numpy array

        # Since activation values are always the same when active, find the activation value
        activation_value = max(values)  # Assumes activation_value > 0
        original_values = values.copy()  # Copy original values to preserve activation values

        # Step 1: Identify rising and falling edges
        rising_indices, falling_indices = identify_activation_indices(values)

        # Ensure rising_indices and falling_indices are of the same length
        if len(rising_indices) > len(falling_indices):
            # Activation continues beyond the end of the data
            falling_indices.append(len(values))
        elif len(falling_indices) > len(rising_indices):
            # Activation started before the data began
            rising_indices = [0] + rising_indices

        # Step 2: Create list of segments
        segments = list(zip(rising_indices, falling_indices))

        # Step 3: Merge segments with gaps less than K
        merged_segments = []
        if segments:
            current_segment = segments[0]
            for next_segment in segments[1:]:
                gap = next_segment[0] - current_segment[1]
                if gap < K:
                    # Merge segments
                    current_segment = (current_segment[0], next_segment[1])
                else:
                    merged_segments.append(current_segment)
                    current_segment = next_segment
            merged_segments.append(current_segment)
        else:
            merged_segments = segments  # Empty list if no segments found

        # Step 4: Reset the values to zero before interpolation
        values[:] = 0

        # Step 5: Perform interpolation on merged segments
        for segment in merged_segments:
            start_idx = max(0, segment[0] - N)
            end_idx = min(len(values), segment[1] + N)

            # Set activation values within the segment
            values[segment[0]:segment[1]] = activation_value

            # Interpolate N frames before activation (rising edge)
            if segment[0] > 0 and start_idx < segment[0]:
                interp_length = segment[0] - start_idx
                for idx, j in enumerate(range(start_idx, segment[0])):
                    # Linearly interpolate from 0 to activation_value
                    values[j] = round((activation_value / interp_length) * (idx + 1), 3)

            # Interpolate N frames after activation (falling edge)
            if segment[1] < len(values) and segment[1] < end_idx:
                interp_length = end_idx - segment[1]
                for idx, j in enumerate(range(segment[1], end_idx)):
                    # Linearly interpolate from activation_value down to 0
                    values[j] = round(activation_value - (activation_value / interp_length) * (idx + 1), 3)

        # Update the DataFrame column with the interpolated values
        df[col] = values

    return df


def detect_segments(df, rep_au, threshold=0.05):
    """
    Returns a list of (start_frame, end_frame) segments where rep_au is above threshold.
    """
    segments = []
    active = False
    start = None
    for i in range(len(df)):
        val = df.loc[i, rep_au]
        if not active and val > threshold:
            active = True
            start = i
        elif active and val <= threshold:
            active = False
            segments.append((start, i - 1))
            start = None
    if active and start is not None:
        segments.append((start, len(df) - 1))
    return segments


def accelerated_fade_out(df, group_cols, start_fade, end_fade):
    """
    Linearly fade out the intensities of the given group's columns from start_fade to end_fade.
    At start_fade, intensities remain as is; by end_fade, intensities are forced to zero.
    """
    fade_length = end_fade - start_fade + 1
    if fade_length <= 0:
        return
    for frame in range(start_fade, end_fade + 1):
        alpha = (frame - start_fade) / fade_length  # goes from 0 to ~1
        for col in group_cols:
            cur_val = df.loc[frame, col]
            df.loc[frame, col] = max(0, cur_val * (1 - alpha))


def adaptive_accelerated_fade(df, groups, threshold=0.05, min_duration=5, fade_frames_default=3):
    """
    This function resolves overlaps between conflicting groups by adaptively accelerating the fade.
    It uses the representative AU for each group to detect segments.

    For each detected conflict between an older segment (A) and a new segment (B):
      - If B’s duration (endB - startB + 1) >= min_duration, then B wins.
        Fade out group A so that its intensities reach 0 by frame (startB).
      - Otherwise (B is too short), fade out group B over a short fade period,
        effectively suppressing B while leaving A intact.

    Parameters:
      df: DataFrame with the AU intensities.
      groups: dict mapping group name to a dictionary with:
              'rep': representative AU,
              'cols': list of AU columns for the group.
      threshold: activation threshold for considering a group active.
      min_duration: minimum duration (in frames) for a new segment to be considered "long enough" to override the previous one.
      fade_frames_default: default number of frames over which to perform the fade.

    Returns:
      Modified DataFrame with adjusted intensities.
    """
    # 1) Build segment lists for each group
    group_segments = {}
    for gname, info in groups.items():
        rep_au = info['rep']
        segs = detect_segments(df, rep_au, threshold=threshold)
        # store as (start, end, group_name)
        group_segments[gname] = [(s[0], s[1], gname) for s in segs]

    # 2) Combine and sort all segments by start time
    all_segments = []
    for gname, segs in group_segments.items():
        all_segments.extend(segs)
    all_segments.sort(key=lambda x: x[0])

    # 3) Process consecutive segments to resolve overlaps
    for i in range(len(all_segments) - 1):
        startA, endA, gA = all_segments[i]
        startB, endB, gB = all_segments[i + 1]

        # If there is overlap: new segment B starts before old segment A ends.
        if startB <= endA:
            # Determine duration of the new segment B
            new_duration = endB - startB + 1
            if new_duration >= min_duration:
                # New group B is "long enough": let B win.
                # Accelerate fade-out for group A so that it reaches 0 by frame (startB).
                final_fade_frame = startB - 1
                # Determine fade start for A: we want a fade duration equal to (endA - final_fade_frame + 1)
                fade_length = endA - final_fade_frame + 1
                # Use fade_length if it's reasonable, otherwise fallback to fade_frames_default.
                fade_length = fade_length if fade_length > 0 else fade_frames_default
                fade_start = final_fade_frame - fade_length + 1
                if fade_start < startA:
                    fade_start = startA
                accelerated_fade_out(df, groups[gA]['cols'], fade_start, final_fade_frame)
                # Update segment A's end to final_fade_frame.
                all_segments[i] = (startA, final_fade_frame, gA)
            else:
                # New group B is too short: suppress B.
                # Fade out group B quickly over fade_frames_default frames starting from B's start.
                fade_end = min(startB + fade_frames_default - 1, endB)
                accelerated_fade_out(df, groups[gB]['cols'], startB, fade_end)
                # Update segment B to effectively end before it rises.
                all_segments[i + 1] = (startB, startB - 1, gB)  # mark as suppressed

    return df
def crossfade_activations_btwchunks(df_prev, df_curr, columns, overlap):
    """
    Applies overlapping cross-fade between two successive chunks.

    For each specified column, the function:
      - Uses the last `overlap` frames of the previous chunk and the first `overlap` frames of the current chunk.
      - Computes a linear cross-fade where the blending weight increases from 0 to 1 across the overlap region.
      - Replaces the overlapping frames in both chunks with the blended values.

    This approach ensures that if one chunk ends with a certain activation and the next starts differently,
    the transition is smoothed out by gradually shifting from the previous value to the current one.

    :param df_prev: DataFrame representing the previous chunk.
    :param df_curr: DataFrame representing the current chunk.
    :param columns: List of column names to process.
    :param overlap: Number of frames in the overlapping region (e.g., set overlap = N).
    :return: Modified df_prev and df_curr DataFrames with cross-faded overlapping regions.
    """
    for col in columns:
        # Copy arrays from the DataFrames.
        values_prev = df_prev[col].values.copy()
        values_curr = df_curr[col].values.copy()

        # Determine the actual overlap (cannot exceed the length of either chunk).
        actual_overlap = min(overlap, len(values_prev), len(values_curr))

        # Blend the overlapping region.
        for i in range(actual_overlap):
            # Compute a weight that increases linearly from 0 to 1 over the overlapping region.
            weight = (i + 1) / (actual_overlap + 1)
            # The overlapping frame in df_prev is at position: -actual_overlap + i,
            # and in df_curr it is at position i.
            blended_value = (1 - weight) * values_prev[-actual_overlap + i] + weight * values_curr[i]
            # Optionally, round the result to 3 decimals.
            blended_value = round(blended_value, 3)
            values_prev[-actual_overlap + i] = blended_value
            values_curr[i] = blended_value

        # Update the DataFrames.
        df_prev[col] = values_prev
        df_curr[col] = values_curr

    return df_prev, df_curr



def adaptive_interpolation_btwchunks(df_prev, df_curr, columns, min_overlap=5, max_overlap=15, diff_threshold=0.2,
                                     k=10):
    """
    Adaptively interpolates activations between two successive chunks based on signal dynamics.

    For each specified column:
      - Finds the last nonzero activation in the previous chunk and the first nonzero activation in the current chunk.
      - Computes the absolute difference between these activations.
      - Chooses an overlap length (adaptive_overlap) that scales between min_overlap and max_overlap depending on the difference.
      - Applies a sigmoid-based weighting function over the adaptive_overlap region for a smooth, non-linear transition.

    Parameters:
      df_prev: DataFrame for the previous chunk.
      df_curr: DataFrame for the current chunk.
      columns: List of column names to process.
      min_overlap: Minimum number of overlapping frames.
      max_overlap: Maximum number of overlapping frames.
      diff_threshold: Difference threshold below which minimal overlap is used.
      k: Steepness factor for the sigmoid weighting function.

    Returns:
      Modified df_prev and df_curr with adapted interpolation in their overlapping regions.
    """
    for col in columns:
        values_prev = df_prev[col].values.copy()
        values_curr = df_curr[col].values.copy()

        # Identify last nonzero in previous chunk.
        non_zero_prev = np.nonzero(values_prev)[0]
        if len(non_zero_prev) > 0:
            last_idx_prev = non_zero_prev[-1]
            act_prev = values_prev[last_idx_prev]
        else:
            last_idx_prev = None
            act_prev = 0

        # Identify first nonzero in current chunk.
        non_zero_curr = np.nonzero(values_curr)[0]
        if len(non_zero_curr) > 0:
            first_idx_curr = non_zero_curr[0]
            act_curr = values_curr[first_idx_curr]
        else:
            first_idx_curr = None
            act_curr = 0

        # Compute the absolute difference.
        diff = abs(act_curr - act_prev)

        # Determine adaptive overlap length.
        if diff < diff_threshold:
            adaptive_overlap = min_overlap
        else:
            # Scale proportionally: larger difference leads to a larger overlap.
            proportion = min((diff - diff_threshold) / (1 - diff_threshold), 1) if (1 - diff_threshold) > 0 else 1
            adaptive_overlap = int(min_overlap + (max_overlap - min_overlap) * proportion)

        # Ensure adaptive_overlap does not exceed available frames.
        adaptive_overlap = min(adaptive_overlap, len(values_prev), len(values_curr))
        if adaptive_overlap <= 0:
            continue

        # Create a sigmoid-based weight curve over the overlap.
        # This generates weights that start near 0 and approach 1 over the overlapping region.
        weights = [1 / (1 + np.exp(-k * (((i + 1) / (adaptive_overlap + 1)) - 0.5))) for i in range(adaptive_overlap)]
        # (If you prefer linear weights, simply use:
        # weights = [(i + 1) / (adaptive_overlap + 1) for i in range(adaptive_overlap)])

        # Apply the weighted blending on the overlapping frames.
        for i in range(adaptive_overlap):
            blended_value = (1 - weights[i]) * values_prev[-adaptive_overlap + i] + weights[i] * values_curr[i]
            blended_value = round(blended_value, 3)
            values_prev[-adaptive_overlap + i] = blended_value
            values_curr[i] = blended_value

        df_prev[col] = values_prev
        df_curr[col] = values_curr

    return df_prev, df_curr


def interpolate_activations_btwchunks(df_prev, df_curr, columns, N, K):
    """
    Interpolates activations between two successive chunks.

    :param df_prev: DataFrame of the previous chunk.
    :param df_curr: DataFrame of the current chunk.
    :param columns: List of columns to process.
    :param N: Number of frames for interpolation.
    :param K: Maximum gap to consider merging activations.
    :return: Modified df_prev and df_curr DataFrames.
    """
    for col in columns:
        # Get the values for the column in both chunks
        values_prev = df_prev[col].values.copy()
        values_curr = df_curr[col].values.copy()

        # Find the last non-zero index in the previous chunk
        non_zero_indices_prev = np.nonzero(values_prev)[0]
        if len(non_zero_indices_prev) > 0:
            last_non_zero_prev = non_zero_indices_prev[-1]
            activation_value_prev = values_prev[last_non_zero_prev]
        else:
            last_non_zero_prev = None
            activation_value_prev = 0

        # Find the first non-zero index in the current chunk
        non_zero_indices_curr = np.nonzero(values_curr)[0]
        if len(non_zero_indices_curr) > 0:
            first_non_zero_curr = non_zero_indices_curr[0]
            activation_value_curr = values_curr[first_non_zero_curr]
        else:
            first_non_zero_curr = None
            activation_value_curr = 0

        # Calculate the gap between the two activations
        if last_non_zero_prev is not None and first_non_zero_curr is not None:
            gap = first_non_zero_curr + (len(values_prev) - last_non_zero_prev - 1)
            if gap < K:
                # Merge the activations by filling the gap
                # Adjust previous chunk if needed
                # No need to adjust previous chunk in this case
                # Adjust current chunk
                values_curr[:first_non_zero_curr] = activation_value_curr
        elif last_non_zero_prev is not None and first_non_zero_curr is None:
            # Previous chunk ends with activation, current chunk is zeros
            # Interpolate decrease over N frames in current chunk
            end_idx = min(N, len(values_curr))
            for i in range(end_idx):
                values_curr[i] = activation_value_prev * (1 - (i + 1) / N)
        elif last_non_zero_prev is None and first_non_zero_curr is not None:
            # Previous chunk ends with zeros, current chunk starts with activation
            # Interpolate increase over N frames at the end of previous chunk
            start_idx = max(0, len(values_prev) - N)
            for i in range(start_idx, len(values_prev)):
                values_prev[i] = activation_value_curr * ((i - start_idx + 1) / N)

        # Update the DataFrames
        df_prev[col] = values_prev
        df_curr[col] = values_curr

    return df_prev, df_curr

def append_sequence_to_csv(sequence, output_csv_path, frame_rate=25):
    duration_per_frame = 1.0 / frame_rate

    if os.path.exists(output_csv_path):
        existing_df = pd.read_csv(output_csv_path)
        last_timestamp = existing_df['timestamp'].iloc[-1]
    else:
        last_timestamp = 0.0
        existing_df = pd.DataFrame(columns=['timestamp'] + [str(i) for i in range(4)])
        existing_df.to_csv(output_csv_path, index=False)

    new_data = []
    for category in sequence:
        new_row = [last_timestamp + duration_per_frame] + [1 if i == category else 0 for i in range(4)]
        new_data.append(new_row)
        last_timestamp += duration_per_frame

    new_df = pd.DataFrame(new_data, columns=['timestamp'] + [str(i) for i in range(4)])
    new_df.to_csv(output_csv_path, mode='a', header=False, index=False)

    return new_df

def restructure_to_baseline(transformed_csv_path, baseline_csv_path, output_csv_path):
    transformed_df = pd.read_csv(transformed_csv_path)
    baseline_df = pd.read_csv(baseline_csv_path)

    transformed_df.columns = transformed_df.columns.str.strip().astype(str)
    baseline_df.columns = baseline_df.columns.str.strip().astype(str)

    structured_df = pd.DataFrame(0, index=transformed_df.index, columns=baseline_df.columns)
    structured_df['timestamp'] = transformed_df['timestamp']

    for col in transformed_df.columns:
        if col in structured_df.columns:
            structured_df[col] = transformed_df[col]

    structured_df.to_csv(output_csv_path, index=False)

    return structured_df

def coactivate_aus(input_csv_path, output_csv_path):
    df = pd.read_csv(input_csv_path)

    # Copy values from 'AU12_r' to 'AU06_r' and 'AU25_r'
    df['AU12_r'] = df['AU12_r'] * 3.5
    df['AU06_r'] = df['AU12_r'] * 0.5
    df['AU25_r'] = df['AU12_r'] * 0.5
    df['AU02_r'] = df['AU12_r'] * 0.6


    # Copy values from 'AU09_r' to 'AU10_r'
    df['AU20_r'] = df['AU09_r'] * 0.7
    df['AU09_r'] = df['AU20_r'] * 2
    df['AU04_r'] = df['AU20_r'] * 3


    # Copy values from 'AU15_r' to 'AU14_r'
    df['AU15_r'] = df['AU15_r'] * 3
    #df['AU10_r'] = df['AU15_r'] * 0.6
    df['AU10_r'] = df['AU15_r'] * 0.3
    df['AU01_r'] = df['AU15_r'] * 0.5



    df.to_csv(output_csv_path, index=False)

def process_and_save_to_csv(reprojected_sequence, intermed_csv_path, transformed_csv_path, ground_csv_path, final_csv_path, adjusted_csv_path, extended_csv_path, N, K, frame_rate=25):
    # Step 1: Append the reprojected sequence to the intermediate CSV
    append_sequence_to_csv(reprojected_sequence, intermed_csv_path, frame_rate)

    # Step 2: Rename headers and drop unnecessary columns
    df = pd.read_csv(intermed_csv_path)
    rename_dict = {'1': 'AU12_r', '2': 'AU09_r', '3': 'AU15_r'}
    df.rename(columns=rename_dict, inplace=True)
    df.drop(columns='0', inplace=True)
    df.to_csv(transformed_csv_path, index=False)

    # Step 3: Restructure the CSV to match the baseline structure
    restructure_to_baseline(transformed_csv_path, ground_csv_path, final_csv_path)

    # Step 4: Coactivate AUs and save the final output
    coactivate_aus(final_csv_path, adjusted_csv_path)

    # Step 5: Perform interpolation on the newly added rows in adjusted_csv_path
    df_adjusted = pd.read_csv(adjusted_csv_path)

    # Specify the columns to interpolate (AU columns)
    columns_to_interpolate = ['AU06_r', 'AU25_r', 'AU12_r', 'AU10_r', 'AU09_r', 'AU14_r', 'AU15_r', 'AU01_r', 'AU04_r','AU20_r' , 'AU02_r']  # Add more columns if needed

    # Perform interpolation
    interpolated_df = interpolate_activations(df_adjusted, columns_to_interpolate, N, K)

    # Save the interpolated results
    interpolated_df.to_csv(extended_csv_path, index=False)