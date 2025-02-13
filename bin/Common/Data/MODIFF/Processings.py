import numpy as np
import pandas as pd
import os

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
    df['AU12_r'] = df['AU12_r'] * 3
    df['AU06_r'] = df['AU12_r'] * 0.5
    df['AU25_r'] = df['AU12_r'] * 0.6
    df['AU02_r'] = df['AU12_r'] * 0.4


    # Copy values from 'AU09_r' to 'AU10_r'
    df['AU10_r'] = df['AU09_r'] * 3
    df['AU09_r'] = df['AU10_r'] * 0.5
    df['AU04_r'] = df['AU10_r'] * 0.3


    # Copy values from 'AU15_r' to 'AU14_r'
    df['AU15_r'] = df['AU15_r'] * 3
    df['AU10_r'] = df['AU15_r'] * 0.5
    df['AU24_r'] = df['AU15_r'] * 0.2
    df['AU01_r'] = df['AU15_r'] * 0.3



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
    columns_to_interpolate = ['AU06_r', 'AU25_r', 'AU12_r', 'AU10_r', 'AU09_r', 'AU14_r', 'AU15_r', 'AU01_r', 'AU04_r','AU24_r' , 'AU02_r']  # Add more columns if needed

    # Perform interpolation
    interpolated_df = interpolate_activations(df_adjusted, columns_to_interpolate, N, K)

    # Save the interpolated results
    interpolated_df.to_csv(extended_csv_path, index=False)