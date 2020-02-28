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

import static greta.auxiliary.ssi.SSITypes.INVALID_OR_EMPTY_STRING_FEATURE_VALUE;
import greta.auxiliary.ssi.SSITypes.SSIFeatureNames;
import static greta.auxiliary.ssi.SSITypes.SSIFeatureNames.NUM_SSI_FEATURES;
import static greta.auxiliary.ssi.SSITypes.SSIFeatureNames.NUM_SSI_STRING_FEATURES;
import greta.core.util.animationparameters.AnimationParametersFrame;
import java.util.ArrayList;

/**
 *
 * @author Angelo Cafaro
 */
public class SSIFrame extends AnimationParametersFrame<SSI> {

    private ArrayList<String> stringFeaturesList;
    private int[] stringFeaturesListIndexes;
    private int nextAvailableStringIndexPos;
    private static final int EMPTY_STRING_FEATURE = -1;
    private static final int STRING_FEATURE_INDEX_NOTFOUND = -1;

    public SSIFrame() {
        super(NUM_SSI_FEATURES + 1);
        this.setupStringLists();
    }

    public SSIFrame(int frameNum) {
       super(NUM_SSI_FEATURES + 1, frameNum);
       this.setupStringLists();
    }

    public SSIFrame(SSIFrame ssiFrame) {
        super(ssiFrame);
        this.setupStringLists();
    }


    private void setupStringLists() {
        nextAvailableStringIndexPos = 0;
        stringFeaturesList = new ArrayList<String>(NUM_SSI_STRING_FEATURES);
        stringFeaturesListIndexes = new int[NUM_SSI_STRING_FEATURES];
        for (int i=0; i < NUM_SSI_STRING_FEATURES; i++) {
            stringFeaturesList.add(null);
            stringFeaturesListIndexes[i] = EMPTY_STRING_FEATURE;
        }
    }

    private int searchValueInStringIndexes(int targetValue) {

        int indexOf = STRING_FEATURE_INDEX_NOTFOUND;
        for (int i=0; i < NUM_SSI_STRING_FEATURES; i++) {
            if(stringFeaturesListIndexes[i] == targetValue) {
                    indexOf = i;
                    break;
            }
	}
	return indexOf;
    }


    @Override
    public SSIFrame clone(){
        return new SSIFrame(this);
    }

    public void setValue(SSIFeatureNames which, double value) {
        getAnimationParameter(which.ordinal()).setValue(value);
    }

    public void applyValue(SSIFeatureNames which, double value) {
        getAnimationParameter(which.ordinal()).applyValue(value);
    }

    public void applyValue(SSIFeatureNames which, int value) {
        getAnimationParameter(which.ordinal()).applyValue(value);
    }

    public void applyValue(SSIFeatureNames which, String value) {
        if (SSITypes.SSIFeatureNames.isStringFeature(which.ordinal()) )
        {
            int index = searchValueInStringIndexes(which.ordinal());

            if (index == STRING_FEATURE_INDEX_NOTFOUND) {
                stringFeaturesListIndexes[nextAvailableStringIndexPos] = which.ordinal();
                index = nextAvailableStringIndexPos;
                nextAvailableStringIndexPos++;
            }

            stringFeaturesList.set(index, value);
        }
    }

    public String getStringValue(SSIFeatureNames which) {

        String val = INVALID_OR_EMPTY_STRING_FEATURE_VALUE;

        if (SSITypes.SSIFeatureNames.isStringFeature(which.ordinal()) )
        {
            int index = searchValueInStringIndexes(which.ordinal());
            if (index != STRING_FEATURE_INDEX_NOTFOUND) {
                val = stringFeaturesList.get(index);
            }

            if (val == null) {
                val = INVALID_OR_EMPTY_STRING_FEATURE_VALUE;
            }
            else if (val.trim().equals("")) {
                val = INVALID_OR_EMPTY_STRING_FEATURE_VALUE;
            }
        }

        return val;
    }

    public double getDoubleValue(SSIFeatureNames which) {
        return getAnimationParameter(which.ordinal()).getNormalizedValue();
    }

    public int getIntValue(SSIFeatureNames which) {
        return getAnimationParameter(which.ordinal()).getValue();
    }

    public void setMask(SSIFeatureNames which, boolean mask) {
        setMask(which.ordinal(), mask);
    }

    public boolean getMask(SSIFeatureNames which) {
        return getMask(which.ordinal());
    }

    public void setMaskAndValue(SSIFeatureNames which, boolean mask, double value) {
        getAnimationParameter(which.ordinal()).set(mask, value);
    }

     @Override
    protected SSI newAnimationParameter() {
        return new SSI();
    }

    @Override
    protected SSI copyAnimationParameter(SSI ssi) {
        return new SSI(ssi);
    }

    @Override
    public SSI newAnimationParameter(boolean mask, int value) {
        return new SSI(mask, value);
    }

    @Override
    public String toString() {
        return "\n\n---------- SSIFrame [" + this.getFrameNumber() + "] ---------- \n" +
                "---- PROSODY ----\n" +
                "-------- VOICE --\n" +
                "Voice activity [" + this.getIntValue(SSIFeatureNames.prosody_voice_activity) + "]\n" +
                "Voice system time [" + this.getIntValue(SSIFeatureNames.prosody_voice_systemtime) + "]\n" +
                "Voice duration [" + this.getIntValue(SSIFeatureNames.prosody_voice_duration) + "]\n" +
                "Voice speech probability [" + this.getDoubleValue(SSIFeatureNames.prosody_voice_speech_prob) + "]\n" +
                "Voice laughter probability (1 – speech p.) [" + this.getDoubleValue(SSIFeatureNames.prosody_voice_laughter_prob) + "]\n" +
                "-------- PRAAT --\n" +
                "Praat pitch median (Hz) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_pitch_median_hz) + "]\n" +
                "Praat pitch mean (Hz) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_pitch_mean_hz) + "]\n" +
                "Praat pitch sd (Hz) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_pitch_sd_hz) + "]\n" +
                "Praat pitch min (Hz) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_pitch_min_hz) + "]\n" +
                "Praat pitch max (Hz) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_pitch_max_hz) + "]\n" +
                "Praat pulses number [" + this.getIntValue(SSIFeatureNames.prosody_praat_pulses_number) + "]\n" +
                "Praat pulses per sec (pulses/sec) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_pulses_per_sec)+ "]\n" +
                "Praat periods number [" + this.getIntValue(SSIFeatureNames.prosody_praat_periods_number) + "]\n" +
                "Praat period mean (sec) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_period_mean_sec) + "]\n" +
                "Praat period sd (sec) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_period_sd_sec) + "]\n" +
                "Praat fraction locally unvoiced frames (%) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_fraction_locally_unvoiced_frames_100) + "]\n" +
                "Praat voice breaks number [" + this.getIntValue(SSIFeatureNames.prosody_praat_voice_breaks_number) + "]\n" +
                "Praat voice breaks degree (%) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_voice_breaks_degree_100) + "]\n" +
                "Praat jitter local (%) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_jitter_local_100) + "]\n" +
                "Praat jitter local abs (sec) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_jitter_local_abs_sec) + "]\n" +
                "Praat jitter rap (%) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_jitter_rap_100) + "]\n" +
                "Praat jitter ppq5 (%) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_jitter_ppq5_100) + "]\n" +
                "Praat jitter ddp (%) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_jitter_ddp_100) + "]\n" +
                "Praat shimmer local (%) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_shimmer_local_100) + "]\n" +
                "Praat shimmer local (dB) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_shimmer_local_db) + "]\n" +
                "Praat shimmer apq3 (%) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_shimmer_apq3_100) + "]\n" +
                "Praat shimmer apq5 (%) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_shimmer_apq5_100) + "]\n" +
                "Praat shimmer apq11 (%) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_shimmer_apq11_100) + "]\n" +
                "Praat shimmer dda (%) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_shimmer_dda_100) + "]\n" +
                "Praat harmonicity mean autocor [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_harmonicity_mean_autocor) + "]\n" +
                "Praat harmonicity mean noise-to-harmonics ratio [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_harmonicity_mean_noise_harmonics_ratio)+ "]\n" +
                "Praat harmonicity mean harmonics-to-noise ratio (dB) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_harmonicity_mean_harmonics_noise_ratio_db) + "]\n" +
                "Praat speechrate duration (sec) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_speechrate_duration_sec) + "]\n" +
                "Praat speechrate voiced count [" + this.getIntValue(SSIFeatureNames.prosody_praat_speechrate_voiced_count) + "]\n" +
                "Praat speechrate syllabes per second [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_speechrate_syllables_per_sec) + "]\n" +
                "Praat speechrate intensity minimum (db) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_intensity_minimum_db) + "]\n" +
                "Praat speechrate intensity maximum (db) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_intensity_maximum_db) + "]\n" +
                "Praat speechrate intensity median (db) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_intensity_median_db) + "]\n" +
                "Praat speechrate intensity average (db) [" + this.getDoubleValue(SSIFeatureNames.prosody_praat_intensity_average_db) + "]\n" +
                "-------- OPENSMILE --\n" +
                "Opensmile pitch [" + SSITypes.SSIPitchValues.getPitchValueName(this.getIntValue(SSIFeatureNames.prosody_opensmile_pitch_cat)) + "]\n" +
                "Opensmile pitch direction [" + SSITypes.SSIPitchDirectionValues.getPitchDirectionValueName(this.getIntValue(SSIFeatureNames.prosody_opensmile_pitch_direction_cat)) + "]\n" +
                "Opensmile voice energy [" + SSITypes.SSIVoiceEnergyValues.getVoiceEnergyValueName(this.getIntValue(SSIFeatureNames.prosody_opensmile_energy_cat)) + "]\n" +
                "-------- GENEVA-FEATURES-SET --\n" +
                "Geneva set F0 semitone From 55Hz-sma3nz mean [" + this.getDoubleValue(SSIFeatureNames.prosody_geneva_F0semitoneFrom55Hz_sma3nz_a_mean) + "]\n" +
                "Geneva set F0 semitone From55Hz-sma3nz stddevNorm [" + this.getDoubleValue(SSIFeatureNames.prosody_geneva_F0semitoneFrom55Hz_sma3nz_stddevNorm) + "]\n" +
                "Geneva set F0 semitone From55Hz-sma3nz percentile20 [" + this.getDoubleValue(SSIFeatureNames.prosody_geneva_F0semitoneFrom55Hz_sma3nz_percentile20) + "]\n" +
                "Geneva set F0 semitone From55Hz-sma3nz percentile50 [" + this.getDoubleValue(SSIFeatureNames.prosody_geneva_F0semitoneFrom55Hz_sma3nz_percentile50) + "]\n" +
                "Geneva set F0 semitone From55Hz-sma3nz percentile80 [" + this.getDoubleValue(SSIFeatureNames.prosody_geneva_F0semitoneFrom55Hz_sma3nz_percentile80) + "]\n" +
                "Geneva set F0 semitone From55Hz-sma3nz pctl range (0-2) [" + this.getDoubleValue(SSIFeatureNames.prosody_geneva_F0semitoneFrom55Hz_sma3nz_pctlrange0_2) + "]\n" +
                "Geneva set StdDev Unvoiced Segment Length [" + this.getDoubleValue(SSIFeatureNames.prosody_geneva_UnvoicedSegmentLength_stddev) + "]\n" +
                "-------- MICROSOFT SPEECH API --\n" +
                "Keyword [" + this.getStringValue(SSIFeatureNames.prosody_msspeech_keyword) + "]\n" +
                "Keyword Confidence [" + this.getDoubleValue(SSIFeatureNames.prosody_msspeech_keyword_confidence) + "]\n" +
                "Function [" + this.getStringValue(SSIFeatureNames.prosody_msspeech_function) + "]\n" +
                "Function Confidence [" + this.getDoubleValue(SSIFeatureNames.prosody_msspeech_function_confidence) + "]\n" +
                "---- HEAD ----\n" +
                "Head position X [" + this.getDoubleValue(SSIFeatureNames.head_position_x) + "]\n" +
                "Head position Y [" + this.getDoubleValue(SSIFeatureNames.head_position_y) + "]\n" +
                "Head position Z [" + this.getDoubleValue(SSIFeatureNames.head_position_z) + "]\n" + // ************** z coordinate adde **********
                "Head orientation roll [" + this.getDoubleValue(SSIFeatureNames.head_orientation_roll) + "]\n" +
                "Head orientation pitch [" + this.getDoubleValue(SSIFeatureNames.head_orientation_pitch) + "]\n" +
                "Head orientation yaw [" + this.getDoubleValue(SSIFeatureNames.head_orientation_yaw) + "]\n" +
                "Head focus [" + this.getDoubleValue(SSIFeatureNames.head_focus) + "]\n" +
                "Head tilt [" + this.getDoubleValue(SSIFeatureNames.head_tilt) + "]\n" +
                "Head nod [" + (((this.getIntValue(SSIFeatureNames.head_nod_cat)) == 1)?"yes":"no") + "]\n" +
                "Head shake [" + (((this.getIntValue(SSIFeatureNames.head_shake_cat)) == 1)?"yes":"no") + "]\n" +
                "Head smile [" + this.getDoubleValue(SSIFeatureNames.head_smile) + "]\n" +
                "---- BODY ----\n" +
                "Body posture lean [" + this.getDoubleValue(SSIFeatureNames.body_posture_lean) + "]\n" +
                "Body arms openess [" + this.getDoubleValue(SSIFeatureNames.body_arms_openness) + "]\n" +
                "Body overall activity [" + this.getDoubleValue(SSIFeatureNames.body_overall_activity) + "]\n" +
                "Body hands energy [" + this.getDoubleValue(SSIFeatureNames.body_hands_energy) + "]\n" +
                "-------- BODY GESTURES --\n" +
                "Body gesture arms open [" + (((this.getIntValue(SSIFeatureNames.body_gesture_arms_open)) == 1)?"yes":"no") + "]\n" +
                "Body gesture arms crossed [" + (((this.getIntValue(SSIFeatureNames.body_gesture_arms_crossed)) == 1)?"yes":"no") + "]\n" +
                "Body gesture left hand head touch [" + (((this.getIntValue(SSIFeatureNames.body_gesture_left_hand_head_touch)) == 1)?"yes":"no") + "]\n" +
                "Body gesture right hand head touch [" + (((this.getIntValue(SSIFeatureNames.body_gesture_right_hand_head_touch)) == 1)?"yes":"no") + "]\n" +
                "Body gesture leaning front [" + (((this.getIntValue(SSIFeatureNames.body_gesture_lean_front)) == 1)?"yes":"no") + "]\n" +
                "Body gesture leaning back [" + (((this.getIntValue(SSIFeatureNames.body_gesture_lean_back)) == 1)?"yes":"no") + "]\n";
    }

}
