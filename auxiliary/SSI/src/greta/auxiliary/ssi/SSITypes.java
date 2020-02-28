/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.ssi;

/**
 *
 * @author Angelo Cafaro
 */
public class SSITypes {

    public static double INVALID_OR_EMPTY_DOUBLE_FEATURE_VALUE = -1.0d;
    public static int INVALID_OR_EMPTY_INTEGER_FEATURE_VALUE = -1;
    public static String INVALID_OR_EMPTY_STRING_FEATURE_VALUE = "N/D";

    /**
     * Notes
     * For integer values use the method getIntValue in SSIFrame
     * For double values use the method getDoubleValue in SSIFrame
     * For String values use the method getStringValue in SSIFrame
     * %: means value in percentage
     * cat: stored as an integer categorical value (i.e. value in enumeration)
     */

    public static enum SSIFeatureNames {

        null_ssi,                                                   // This is a dummy value used when no feature is found (see method getFeatureName below)
        prosody_voice_activity, // 1                                // Integer, 0 = false, 1 = true
        prosody_voice_systemtime,                                   // Integer, not used at the moment
        prosody_voice_duration,                                     // Integer, not used at the moment
        prosody_voice_speech_prob,                                  // Double, probability voice activity is speech [0-1]
        prosody_voice_laughter_prob, // 5                           // Double, probability voice activity is laughter (1 – speech)
        prosody_praat_pitch_median_hz,                              // Double
        prosody_praat_pitch_mean_hz,                                // Double
        prosody_praat_pitch_sd_hz,                                  // Double
        prosody_praat_pitch_min_hz,                                 // Double
        prosody_praat_pitch_max_hz, // 10                           // Double
        prosody_praat_pulses_number,                                // Integer
        prosody_praat_pulses_per_sec,                               // Double
        prosody_praat_periods_number,                               // Integer
        prosody_praat_period_mean_sec,                              // Double
        prosody_praat_period_sd_sec, // 15                          // Double
        prosody_praat_fraction_locally_unvoiced_frames_100,         // Double (%)
        prosody_praat_voice_breaks_number,                          // Integer
        prosody_praat_voice_breaks_degree_100,                      // Double (%)
        prosody_praat_jitter_local_100,                             // Double(%)
        prosody_praat_jitter_local_abs_sec, // 20                   // Double
        prosody_praat_jitter_rap_100,                               // Double (%)
        prosody_praat_jitter_ppq5_100,                              // Double (%)
        prosody_praat_jitter_ddp_100,                               // Double (%)
        prosody_praat_shimmer_local_100,                            // Double (%)
        prosody_praat_shimmer_local_db, // 25                       // Double
        prosody_praat_shimmer_apq3_100,                             // Double (%)
        prosody_praat_shimmer_apq5_100,                             // Double (%)
        prosody_praat_shimmer_apq11_100,                            // Double (%)
        prosody_praat_shimmer_dda_100,                              // Double (%)
        prosody_praat_harmonicity_mean_autocor, // 30               // Double
        prosody_praat_harmonicity_mean_noise_harmonics_ratio,       // Double
        prosody_praat_harmonicity_mean_harmonics_noise_ratio_db,    // Double
        prosody_praat_speechrate_duration_sec,                      // Double
        prosody_praat_speechrate_voiced_count,                      // Integer
        prosody_praat_speechrate_syllables_per_sec, // 35           // Double
        prosody_praat_intensity_minimum_db,                         // Double
        prosody_praat_intensity_maximum_db,                         // Double
        prosody_praat_intensity_median_db,                          // Double
        prosody_praat_intensity_average_db,                         // Double
        prosody_opensmile_pitch_cat, // 40                          // Integer, returns an item of the type SSIPitchValues (see below), use the method getPitchValueName to convert from int to enum value
        prosody_opensmile_pitch_direction_cat,                      // Integer, returns an item of the type SSIPitchDirectionValues (see below), use the method getPitchDirectionValueName to convert from int to enum value
        prosody_opensmile_energy_cat,                               // Integer, returns an item of the type SSIVoiceEnergyValues (see below), use the method getVoiceEnergyValueName to convert from int to enum value
        prosody_geneva_F0semitoneFrom55Hz_sma3nz_a_mean,            // Double
        prosody_geneva_F0semitoneFrom55Hz_sma3nz_stddevNorm,        // Double
        prosody_geneva_F0semitoneFrom55Hz_sma3nz_percentile20, //45 // Double
        prosody_geneva_F0semitoneFrom55Hz_sma3nz_percentile50,      // Double
        prosody_geneva_F0semitoneFrom55Hz_sma3nz_percentile80,      // Double
        prosody_geneva_F0semitoneFrom55Hz_sma3nz_pctlrange0_2,      // Double
        prosody_geneva_UnvoicedSegmentLength_stddev,                // Double
        prosody_msspeech_keyword, // 50                             // String, the keyword (i.e. uttr value) recognized as defined in the SSI grammar for the language set in the options of SSI
                                                                    // NOTE: use applyStringValue and getStringValue to store/retrive this feature
        prosody_msspeech_keyword_confidence,                        // Double, [0..1] representing the recognition confidence of the keyword (the uttr value) (if "semantics_prolog" option is used in SSI), otherwise -1 (if option "keyword" is used in SSI).
        prosody_msspeech_function,                                  // String, the function (i.e. function value) recognized as defined in the SSI grammar for the language set in the options of SSI
                                                                    // NOTE: use applyStringValue and getStringValue to store/retrive this feature
        prosody_msspeech_function_confidence,                       // Double, [0..1] representing the recognition confidence of the keyword (the function value) (if "semantics_prolog" option is used in SSI), otherwise -1 (if option "keyword" is used in SSI).
        head_position_x,                                            // Double, coordinates in relation to camera
        head_position_y, // 55                                      // Double, coordinates in relation to camera
        head_position_z,                                            // Double, coordinates in relation to camera *************************************************
        head_orientation_roll,                                      // Double, coordinates from Kinect SDK [-90,90] -91 = invalid value
        head_orientation_pitch,                                     // Double, coordinates from Kinect SDK [-90,90] -91 = invalid value
        head_orientation_yaw,                                       // Double, coordinates from Kinect SDK [-90,90] -91 = invalid value
        head_focus,                                                 // Double [0..1] where 1 = focused head position (centered), 0 = looking away, .. between values possible
        head_tilt, // 61                                            // Double [0..1] where 1 = tilted head, 0 = straight head, .. between values possible
        head_nod_cat,                                               // Integer, 1 = yes head nod, 0 = no head nod
        head_shake_cat,                                             // Integer, 1 = yes head shake, 0 = no head shake
        head_smile,                                                 // Double, values 0 - ~100
        body_posture_lean,                                          // Double, [0..1] where 1 = front, 0.5 = center, 0 = back
        body_arms_openness, // 66                                   // Double [0..1] where 1  = open, 0 = closed, .. between values possible
        body_overall_activity,                                      // Double, [0..50?] where the movement is in 30 second timespan
        body_hands_energy,                                          // Double, [0..1?] where it represents the energy of hand movement
        body_gesture_arms_open,                                     // Integer, where 1 = present, 0 = not present
        body_gesture_arms_crossed,                                  // Integer, where 1 = present, 0 = not present
        body_gesture_left_hand_head_touch, // 71                    // Integer, where 1 = present, 0 = not present
        body_gesture_right_hand_head_touch,                         // Integer, where 1 = present, 0 = not present
        body_gesture_lean_front,                                    // Integer, where 1 = present, 0 = not present
        body_gesture_lean_back, // 74                               // Integer, where 1 = present, 0 = not present

        agender_active,                                             // Integer, where 1 = present, 0 = not present
        agender_gender_male, // 76                                  // Double, [0..1] representing the probability of the user being male
        agender_gender_female,                                      // Double, [0..1] representing the probability of the user being female
        agender_gender_child,                                       // Double, [0..1] representing the probability of the user being child
        agender_age_child,                                          // Double, [0..1] representing the probability of the user being child
        agender_age_youth,                                          // Double, [0..1] representing the probability of the user being youth
        agender_age_adult, // 81                                    // Double, [0..1] representing the probability of the user being adult
        agender_age_senior,                                         // Double, [0..1] representing the probability of the user being senior

        emax_face1_id,                                              // Integer, id of the first face, -1 = not present, >= 0 face id
        emax_face1_neutral,                                         // Double, if positive emotion is present
        emax_face1_anger, // 86                                     // -----
        emax_face1_disgust,                                         // -----
        emax_face1_fear,                                            // -----
        emax_face1_happiness,                                       // -----
        emax_face1_sadness,                                         // -----
        emax_face1_surprised, // 91                                 // -----
        emax_face2_id,                                              // Integer, id of the second face, -1 = not present, >= 0 face id
        emax_face2_neutral,                                         // Double, if positive emotion is present
        emax_face2_anger,                                           // -----
        emax_face2_disgust,                                         // -----
        emax_face2_fear, // 96                                      // -----
        emax_face2_happiness,                                       // -----
        emax_face2_sadness,                                         // -----
        emax_face2_surprised; // 99                                 // -----


        // public static final int NUM_SSI_FEATURES = 73; // This is the number of features provided by SSI, note that the null_ssi item is excluded from the count
        public static final int NUM_SSI_FEATURES = values().length - 1; // This will update itself, -1 because null_ssi is ignored
        public static final int NUM_SSI_STRING_FEATURES = 2; // This is the number of features (included in the NUM_SSI_FEATURES) that are represented by string values

        public static SSIFeatureNames getFeatureName(int ordinal){
            return (ordinal < 0 || ordinal > NUM_SSI_FEATURES) ? null_ssi : values()[ordinal];
        }

        public static boolean isStringFeature(int ordinal) {
            if (ordinal < 0 || ordinal > NUM_SSI_FEATURES) {
                return false;
            }
            if ((ordinal == prosody_msspeech_keyword.ordinal()) || (ordinal == prosody_msspeech_function.ordinal())){
                return true;
            }
            return false;
        }
    };


    public static enum SSIPitchValues {

        none,
        low,
        normal,
        high;

        public static final int NUM_PITCH_VALUES = 4;

        public static SSIPitchValues getPitchValueName(int cat){
            return (cat < 0 || cat >= NUM_PITCH_VALUES) ? none : values()[cat];
        }
    };

    public static enum SSIPitchDirectionValues {

        none,
        rise,
        fall,
        rise_fall,
        fall_rise;

        public static final int NUM_PITCH_DIRECTION_VALUES = 5;

        public static SSIPitchDirectionValues getPitchDirectionValueName(int cat){
            return (cat < 0 || cat >= NUM_PITCH_DIRECTION_VALUES) ? none : values()[cat];
        }

    };

    public static enum SSIVoiceEnergyValues {

        none,
        low,
        medium,
        high;

        public static final int NUM_VOICE_ENERGY_VALUES = 4;

        public static SSIVoiceEnergyValues getVoiceEnergyValueName(int cat){
            return (cat < 0 || cat >= NUM_VOICE_ENERGY_VALUES) ? none : values()[cat];
        }
    };

}
