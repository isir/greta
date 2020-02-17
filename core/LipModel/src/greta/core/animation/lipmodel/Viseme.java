/*
 * This file is part of Greta.
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
package greta.core.animation.lipmodel;

import greta.core.util.speech.Phoneme;

/**
 *
 * @author Yu Ding
 */
public class Viseme {

    double timePositionBegin;
    double visemeDuration;
    double sequencenDuration;
    Phoneme curPho = null;
    Phoneme prePho = null;
    Phoneme sucPho = null;
    int currentIndex;
    int currentSequenceLength;
    Phoneme prePhysiquePho = null;
    Phoneme sucPhysiquePho = null;
    float coef = 1.0f;
    // coef is needed to diminish the tension on the lip near the beginning and the end of the speech
    float generalTensionSup = 0;
    float generalTensionInf = 0;
    float curTime[][] = new float[LipModel.NUM_LABIALS][3];
    float preTime[][] = new float[LipModel.NUM_LABIALS][3];
    float sucTime[][] = new float[LipModel.NUM_LABIALS][3];
    float preTarget[][] = new float[LipModel.NUM_LABIALS][3];
    float sucTarget[][] = new float[LipModel.NUM_LABIALS][3];
    float curTarget[][] = new float[LipModel.NUM_LABIALS][3];
    double weightTarget[] = new double[]{LipModel.WEIGHT_ULO, LipModel.WEIGHT_LLO, LipModel.WEIGHT_JAW, LipModel.WEIGHT_LW, LipModel.WEIGHT_ULP, LipModel.WEIGHT_LLP, LipModel.WEIGHT_CR, LipModel.WEIGHT_LLO};

    Viseme(Phoneme curPhoneme, Phoneme prePhoneme, Phoneme sucPhoneme, Phoneme prePhyPho, Phoneme sucPhyPho, int index, int length, double timePositionBegin, double visemeDuration, double sequencenDuration) {
        currentIndex = index;
        currentSequenceLength = length;
        curPho = curPhoneme;
        prePho = prePhoneme;
        sucPho = sucPhoneme;
        prePhysiquePho = prePhyPho;
        sucPhysiquePho = sucPhyPho;
        this.timePositionBegin = timePositionBegin;
        this.visemeDuration = visemeDuration;
        this.sequencenDuration = sequencenDuration;
    }

    public Phoneme getprePho() {
        return prePho;
    }

    public Phoneme getsucPho() {
        return sucPho;
    }

    public double gettimePositionBegin() {
        return timePositionBegin;
    }

    public void settimePositionBegin(double timePositionBegin) {
        this.timePositionBegin = timePositionBegin;
    }

    public double getTimePosition() {
        return timePositionBegin;
    }

    public float[][] getpreTarget() {
        return preTarget;
    }

    public float[][] getsucTarget() {
        return sucTarget;
    }

    public void showcurTimeAndpreTargetAndsucTarget() {
        // Time
        double duration = curPho.getDuration();
        for (int i = 0; i < LipModel.NUM_LABIALS; i++) {
            System.out.println("Time[" + i + "] : " + curTime[i][0] / duration + "    " + curTime[i][1] / duration + "    " + curTime[i][2] / duration);
        }
        // preTarget
        for (int i = 0; i < LipModel.NUM_LABIALS; i++) {
            System.out.println("preTarget[" + i + "] : " + preTarget[i][0] + "    " + preTarget[i][1] + "    " + preTarget[i][2]);
        }
        // sucTarget
        for (int i = 0; i < LipModel.NUM_LABIALS; i++) {
            System.out.println("sucTarget[" + i + "] : " + sucTarget[i][0] + "    " + sucTarget[i][1] + "    " + sucTarget[i][2]);
        }
    }

    public void testChargementpreTimeAndsucTime() {

        for (int i = 0; i < LipModel.NUM_LABIALS; i++) {
            System.out.println("preTime[" + i + "] : " + preTime[i][0] + "    " + preTime[i][1] + "    " + preTime[i][2]);
        }
        for (int i = 0; i < LipModel.NUM_LABIALS; i++) {
            System.out.println("sucTime[" + i + "] : " + sucTime[i][0] + "    " + sucTime[i][1] + "    " + sucTime[i][2]);
        }
    }

    public void showcurTime() {

        for (int i = 0; i < LipModel.NUM_LABIALS; i++) {
            System.out.println("curTime[" + i + "] : " + curTime[i][0] + "    " + curTime[i][1] + "    " + curTime[i][2]);
        }
    }

    public void setTimeAndTargetPreAndTargetSuc(Phoneme curPhoneme, int index, int length, CoartParameter coartpre, CoartParameter coartsuc) {
        //public void setTimeAndTargetPreAndTargetSuc(Phoneme curPhoneme, Phoneme neighborPhoneme, int index, int length){
        //   curPhoneme.getDuration();
        // ==== this function takes into account two cases: (vowel) and (consonant/null)
        // ==== for each case, target and time is separately set.
        double duration = curPhoneme.getDuration();
        if (curPhoneme.isVowel()) {
            //       //Vowel
            if (index == 0) {
                if (coartsuc != null) {
                    for (int i = 0; i < weightTarget.length; i++){
                        sucTarget[i][0] = (float) (coartsuc.middleULO * weightTarget[i]);
                        sucTarget[i][1] = (float) (coartsuc.apexULO * weightTarget[i]);
                        sucTarget[i][2] = (float) (coartsuc.middleULO * weightTarget[i]);
                    }
                }
            } else if (index == (length - 1)) {
                if (coartpre != null) {
                    preTarget[0][0] = (float) (coartpre.middleULO * weightTarget[0]);
                    preTarget[0][1] = (float) (coartpre.apexULO * weightTarget[0]);
                    preTarget[0][2] = (float) (coartpre.middleULO * weightTarget[0]);

                    preTarget[1][0] = (float) (coartpre.middleLLO * weightTarget[1]);
                    preTarget[1][1] = (float) (coartpre.apexLLO * weightTarget[1]);
                    preTarget[1][2] = (float) (coartpre.middleLLO * weightTarget[1]);

                    preTarget[2][0] = (float) (coartpre.middleJAW * weightTarget[2]);
                    preTarget[2][1] = (float) (coartpre.apexJAW * weightTarget[2]);
                    preTarget[2][2] = (float) (coartpre.middleJAW * weightTarget[2]);

                    preTarget[3][0] = (float) (coartpre.middleLW * weightTarget[3]);
                    preTarget[3][1] = (float) (coartpre.apexLW * weightTarget[3]);
                    preTarget[3][2] = (float) (coartpre.middleLW * weightTarget[3]);

                    preTarget[4][0] = (float) (coartpre.middleULP * weightTarget[4]);
                    preTarget[4][1] = (float) (coartpre.apexULP * weightTarget[4]);
                    preTarget[4][2] = (float) (coartpre.middleULP * weightTarget[4]);

                    preTarget[5][0] = (float) (coartpre.middleLLP * weightTarget[5]);
                    preTarget[5][1] = (float) (coartpre.apexLLP * weightTarget[5]);
                    preTarget[5][2] = (float) (coartpre.middleLLP * weightTarget[5]);

                    preTarget[6][0] = (float) (coartpre.middleCR * weightTarget[6]);
                    preTarget[6][1] = (float) (coartpre.apexCR * weightTarget[6]);
                    preTarget[6][2] = (float) (coartpre.middleCR * weightTarget[6]);

                    // Blending factor timing is synchonized with the LLO (liabial and bilabial, both use the lower lip)
                    preTarget[7][0] = (float) (coartpre.middleLLO * weightTarget[7]);
                    preTarget[7][1] = (float) (coartpre.apexLLO * weightTarget[7]);
                    preTarget[7][2] = (float) (coartpre.middleLLO * weightTarget[7]);
                }

            } else {
                // sucTarget
                if (coartsuc != null) {

                    sucTarget[0][0] = (float) (coartsuc.middleULO * weightTarget[0]);
                    sucTarget[0][1] = (float) (coartsuc.apexULO * weightTarget[0]);
                    sucTarget[0][2] = (float) (coartsuc.middleULO * weightTarget[0]);

                    sucTarget[1][0] = (float) (coartsuc.middleLLO * weightTarget[1]);
                    sucTarget[1][1] = (float) (coartsuc.apexLLO * weightTarget[1]);
                    sucTarget[1][2] = (float) (coartsuc.middleLLO * weightTarget[1]);

                    sucTarget[2][0] = (float) (coartsuc.middleJAW * weightTarget[2]);
                    sucTarget[2][1] = (float) (coartsuc.apexJAW * weightTarget[2]);
                    sucTarget[2][2] = (float) (coartsuc.middleJAW * weightTarget[2]);

                    sucTarget[3][0] = (float) (coartsuc.middleLW * weightTarget[3]);
                    sucTarget[3][1] = (float) (coartsuc.apexLW * weightTarget[3]);
                    sucTarget[3][2] = (float) (coartsuc.middleLW * weightTarget[3]);

                    sucTarget[4][0] = (float) (coartsuc.middleULP * weightTarget[4]);
                    sucTarget[4][1] = (float) (coartsuc.apexULP * weightTarget[4]);
                    sucTarget[4][2] = (float) (coartsuc.middleULP * weightTarget[4]);

                    sucTarget[5][0] = (float) (coartsuc.middleLLP * weightTarget[5]);
                    sucTarget[5][1] = (float) (coartsuc.apexLLP * weightTarget[5]);
                    sucTarget[5][2] = (float) (coartsuc.middleLLP * weightTarget[5]);

                    sucTarget[6][0] = (float) (coartsuc.middleCR * weightTarget[6]);
                    sucTarget[6][1] = (float) (coartsuc.apexCR * weightTarget[6]);
                    sucTarget[6][2] = (float) (coartsuc.middleCR * weightTarget[6]);

                    sucTarget[7][0] = (float) (coartsuc.middleLLO * weightTarget[7]);
                    sucTarget[7][1] = (float) (coartsuc.apexLLO * weightTarget[7]);
                    sucTarget[7][2] = (float) (coartsuc.middleLLO * weightTarget[7]);
                }

                // preTarget
                if (coartpre != null) {

                    preTarget[0][0] = (float) (coartpre.middleULO * weightTarget[0]);
                    preTarget[0][1] = (float) (coartpre.apexULO * weightTarget[0]);
                    preTarget[0][2] = (float) (coartpre.middleULO * weightTarget[0]);

                    preTarget[1][0] = (float) (coartpre.middleLLO * weightTarget[1]);
                    preTarget[1][1] = (float) (coartpre.apexLLO * weightTarget[1]);
                    preTarget[1][2] = (float) (coartpre.middleLLO * weightTarget[1]);

                    preTarget[2][0] = (float) (coartpre.middleJAW * weightTarget[2]);
                    preTarget[2][1] = (float) (coartpre.apexJAW * weightTarget[2]);
                    preTarget[2][2] = (float) (coartpre.middleJAW * weightTarget[2]);

                    preTarget[3][0] = (float) (coartpre.middleLW * weightTarget[3]);
                    preTarget[3][1] = (float) (coartpre.apexLW * weightTarget[3]);
                    preTarget[3][2] = (float) (coartpre.middleLW * weightTarget[3]);

                    preTarget[4][0] = (float) (coartpre.middleULP * weightTarget[4]);
                    preTarget[4][1] = (float) (coartpre.apexULP * weightTarget[4]);
                    preTarget[4][2] = (float) (coartpre.middleULP * weightTarget[4]);

                    preTarget[5][0] = (float) (coartpre.middleLLP * weightTarget[5]);
                    preTarget[5][1] = (float) (coartpre.apexLLP * weightTarget[5]);
                    preTarget[5][2] = (float) (coartpre.middleLLP * weightTarget[5]);

                    preTarget[6][0] = (float) (coartpre.middleCR * weightTarget[6]);
                    preTarget[6][1] = (float) (coartpre.apexCR * weightTarget[6]);
                    preTarget[6][2] = (float) (coartpre.middleCR * weightTarget[6]);

                    preTarget[7][0] = (float) (coartpre.middleLLO * weightTarget[7]);
                    preTarget[7][1] = (float) (coartpre.apexLLO * weightTarget[7]);
                    preTarget[7][2] = (float) (coartpre.middleLLO * weightTarget[7]);

                }

            }

            // vowelle
            // curTime
            if ((coartpre != null) && (coartsuc != null)) {
                curTime[0][1] = (float) (((coartpre.apextimeULO + coartsuc.apextimeULO) / 2) * duration);
                curTime[0][0] = curTime[0][1] / 2;
                curTime[0][2] = curTime[0][1] + ((float) duration - curTime[0][1]) / 2;

                curTime[1][1] = (float) (((coartpre.apextimeLLO + coartsuc.apextimeLLO) / 2) * duration);
                curTime[1][0] = curTime[1][1] / 2;
                curTime[1][2] = curTime[1][1] + ((float) duration - curTime[1][1]) / 2;

                curTime[2][1] = (float) (((coartpre.apextimeJAW + coartsuc.apextimeJAW) / 2) * duration);
                curTime[2][0] = curTime[2][1] / 2;
                curTime[2][2] = curTime[2][1] + ((float) duration - curTime[2][1]) / 2;

                curTime[3][1] = (float) (((coartpre.apextimeLW + coartsuc.apextimeLW) / 2) * duration);
                curTime[3][0] = curTime[3][1] / 2;
                curTime[3][2] = curTime[3][1] + ((float) duration - curTime[3][1]) / 2;

                curTime[4][1] = (float) (((coartpre.apextimeULP + coartsuc.apextimeULP) / 2) * duration);
                curTime[4][0] = curTime[4][1] / 2;
                curTime[4][2] = curTime[4][1] + ((float) duration - curTime[4][1]) / 2;

                curTime[5][1] = (float) (((coartpre.apextimeLLP + coartsuc.apextimeLLP) / 2) * duration);
                curTime[5][0] = curTime[5][1] / 2;
                curTime[5][2] = curTime[5][1] + ((float) duration - curTime[5][1]) / 2;

                curTime[6][1] = (float) (((coartpre.apextimeCR + coartsuc.apextimeCR) / 2) * duration);
                curTime[6][0] = curTime[6][1] / 2;
                curTime[6][2] = curTime[6][1] + ((float) duration - curTime[6][1]) / 2;

                curTime[7][1] = (float) (((coartpre.apextimeLLO + coartsuc.apextimeLLO) / 2) * duration);
                curTime[7][0] = curTime[7][1] / 2;
                curTime[7][2] = curTime[7][1] + ((float) duration - curTime[7][1]) / 2;
            } else if ((coartpre == null) && (coartsuc != null)) {
                curTime[0][1] = (float) (coartsuc.apextimeULO * duration);
                curTime[0][0] = curTime[0][1] / 2;
                curTime[0][2] = curTime[0][1] + ((float) duration - curTime[0][1]) / 2;

                curTime[1][1] = (float) (coartsuc.apextimeLLO * duration);
                curTime[1][0] = curTime[1][1] / 2;
                curTime[1][2] = curTime[1][1] + ((float) duration - curTime[1][1]) / 2;

                curTime[2][1] = (float) (coartsuc.apextimeJAW * duration);
                curTime[2][0] = curTime[2][1] / 2;
                curTime[2][2] = curTime[2][1] + ((float) duration - curTime[2][1]) / 2;

                curTime[3][1] = (float) (coartsuc.apextimeLW * duration);
                curTime[3][0] = curTime[3][1] / 2;
                curTime[3][2] = curTime[3][1] + ((float) duration - curTime[3][1]) / 2;

                curTime[4][1] = (float) (coartsuc.apextimeULP * duration);
                curTime[4][0] = curTime[4][1] / 2;
                curTime[4][2] = curTime[4][1] + ((float) duration - curTime[4][1]) / 2;

                curTime[5][1] = (float) (coartsuc.apextimeLLP * duration);
                curTime[5][0] = curTime[5][1] / 2;
                curTime[5][2] = curTime[5][1] + ((float) duration - curTime[5][1]) / 2;

                curTime[6][1] = (float) (coartsuc.apextimeCR * duration);
                curTime[6][0] = curTime[6][1] / 2;
                curTime[6][2] = curTime[6][1] + ((float) duration - curTime[6][1]) / 2;

                curTime[7][1] = (float) (coartsuc.apextimeLLO * duration);
                curTime[7][0] = curTime[7][1] / 2;
                curTime[7][2] = curTime[7][1] + ((float) duration - curTime[7][1]) / 2;
            } else if ((coartpre != null) && (coartsuc == null)) {
                curTime[0][1] = (float) (coartpre.apextimeULO * duration);
                curTime[0][0] = curTime[0][1] / 2;
                curTime[0][2] = curTime[0][1] + ((float) duration - curTime[0][1]) / 2;

                curTime[1][1] = (float) (coartpre.apextimeLLO * duration);
                curTime[1][0] = curTime[1][1] / 2;
                curTime[1][2] = curTime[1][1] + ((float) duration - curTime[1][1]) / 2;

                curTime[2][1] = (float) (coartpre.apextimeJAW * duration);
                curTime[2][0] = curTime[2][1] / 2;
                curTime[2][2] = curTime[2][1] + ((float) duration - curTime[2][1]) / 2;

                curTime[3][1] = (float) (coartpre.apextimeLW * duration);
                curTime[3][0] = curTime[3][1] / 2;
                curTime[3][2] = curTime[3][1] + ((float) duration - curTime[3][1]) / 2;

                curTime[4][1] = (float) (coartpre.apextimeULP * duration);
                curTime[4][0] = curTime[4][1] / 2;
                curTime[4][2] = curTime[4][1] + ((float) duration - curTime[4][1]) / 2;

                curTime[5][1] = (float) (coartpre.apextimeLLP * duration);
                curTime[5][0] = curTime[5][1] / 2;
                curTime[5][2] = curTime[5][1] + ((float) duration - curTime[5][1]) / 2;

                curTime[6][1] = (float) (coartpre.apextimeCR * duration);
                curTime[6][0] = curTime[6][1] / 2;
                curTime[6][2] = curTime[6][1] + ((float) duration - curTime[6][1]) / 2;

                curTime[7][1] = (float) (coartpre.apextimeLLO * duration);
                curTime[7][0] = curTime[7][1] / 2;
                curTime[7][2] = curTime[7][1] + ((float) duration - curTime[7][1]) / 2;
            }
            // + timePositionBegin;
            for (int i = 0; i < LipModel.NUM_LABIALS; i++) {
                for (int j = 0; j < 3; j++) {
                    curTime[i][j] = curTime[i][j] + (float) timePositionBegin;
                }
            }
        } else {
            //-----------------------Consonant-------------------------------------------------------------
            // sucTarget and preTarget
            if (index == 0) {
                if (coartsuc != null) {

                    sucTarget[0][0] = (float) (coartsuc.apexULO * weightTarget[0]);
                    sucTarget[1][0] = (float) (coartsuc.apexLLO * weightTarget[1]);
                    sucTarget[2][0] = (float) (coartsuc.apexJAW * weightTarget[2]);
                    sucTarget[3][0] = (float) (coartsuc.apexLW * weightTarget[3]);
                    sucTarget[4][0] = (float) (coartsuc.apexULP * weightTarget[4]);
                    sucTarget[5][0] = (float) (coartsuc.apexLLP * weightTarget[5]);
                    sucTarget[6][0] = (float) (coartsuc.apexCR * weightTarget[6]);
                    sucTarget[7][0] = (float) (coartsuc.apexLLO * weightTarget[7]);

                }

            } else if (index == (length - 1)) {
                if (coartpre != null) {
                    preTarget[0][0] = (float) (coartpre.apexULO * weightTarget[0]);
                    preTarget[1][0] = (float) (coartpre.apexLLO * weightTarget[1]);
                    preTarget[2][0] = (float) (coartpre.apexJAW * weightTarget[2]);
                    preTarget[3][0] = (float) (coartpre.apexLW * weightTarget[3]);
                    preTarget[4][0] = (float) (coartpre.apexULP * weightTarget[4]);
                    preTarget[5][0] = (float) (coartpre.apexLLP * weightTarget[5]);
                    preTarget[6][0] = (float) (coartpre.apexCR * weightTarget[6]);
                    preTarget[7][0] = (float) (coartpre.apexLLO * weightTarget[7]);
                }

            } else {
                // sucTarget
                if (coartsuc != null) {
                    sucTarget[0][0] = (float) (coartsuc.apexULO * weightTarget[0]);
                    sucTarget[1][0] = (float) (coartsuc.apexLLO * weightTarget[1]);
                    sucTarget[2][0] = (float) (coartsuc.apexJAW * weightTarget[2]);
                    sucTarget[3][0] = (float) (coartsuc.apexLW * weightTarget[3]);
                    sucTarget[4][0] = (float) (coartsuc.apexULP * weightTarget[4]);
                    sucTarget[5][0] = (float) (coartsuc.apexLLP * weightTarget[5]);
                    sucTarget[6][0] = (float) (coartsuc.apexCR * weightTarget[6]);
                    sucTarget[7][0] = (float) (coartsuc.apexLLO * weightTarget[7]);
                }

                // preTarget
                if (coartpre != null) {
                    preTarget[0][0] = (float) (coartpre.apexULO * weightTarget[0]);
                    preTarget[1][0] = (float) (coartpre.apexLLO * weightTarget[1]);
                    preTarget[2][0] = (float) (coartpre.apexJAW * weightTarget[2]);
                    preTarget[3][0] = (float) (coartpre.apexLW * weightTarget[3]);
                    preTarget[4][0] = (float) (coartpre.apexULP * weightTarget[4]);
                    preTarget[5][0] = (float) (coartpre.apexLLP * weightTarget[5]);
                    preTarget[6][0] = (float) (coartpre.apexCR * weightTarget[6]);
                    preTarget[7][0] = (float) (coartpre.apexLLO * weightTarget[7]);
                }

            }
            // fin sucTarget and preTarget----Consonant

            // consonnant
            // curTime
            if ((coartpre != null) && (coartsuc != null)) {
                curTime[0][0] = (float) (((coartpre.apextimeULO + coartsuc.apextimeULO) / 2) * duration);
                curTime[1][0] = (float) (((coartpre.apextimeLLO + coartsuc.apextimeLLO) / 2) * duration);
                curTime[2][0] = (float) (((coartpre.apextimeJAW + coartsuc.apextimeJAW) / 2) * duration);
                curTime[3][0] = (float) (((coartpre.apextimeLW + coartsuc.apextimeLW) / 2) * duration);
                curTime[4][0] = (float) (((coartpre.apextimeULP + coartsuc.apextimeULP) / 2) * duration);
                curTime[5][0] = (float) (((coartpre.apextimeLLP + coartsuc.apextimeLLP) / 2) * duration);
                curTime[6][0] = (float) (((coartpre.apextimeCR + coartsuc.apextimeCR) / 2) * duration);
                curTime[7][0] = (float) (((coartpre.apextimeLLO + coartsuc.apextimeLLO) / 2) * duration);
            } else if ((coartpre == null) && (coartsuc != null)) {
                curTime[0][0] = (float) ((coartsuc.apextimeULO) * duration);
                curTime[1][0] = (float) ((coartsuc.apextimeLLO) * duration);
                curTime[2][0] = (float) ((coartsuc.apextimeJAW) * duration);
                curTime[3][0] = (float) ((coartsuc.apextimeLW) * duration);
                curTime[4][0] = (float) ((coartsuc.apextimeULP) * duration);
                curTime[5][0] = (float) ((coartsuc.apextimeLLP) * duration);
                curTime[6][0] = (float) ((coartsuc.apextimeCR) * duration);
                curTime[7][0] = (float) ((coartsuc.apextimeLLO) * duration);
            } else if ((coartpre != null) && (coartsuc == null)) {
                curTime[0][0] = (float) ((coartpre.apextimeULO) * duration);
                curTime[1][0] = (float) ((coartpre.apextimeLLO) * duration);
                curTime[2][0] = (float) ((coartpre.apextimeJAW) * duration);
                curTime[3][0] = (float) ((coartpre.apextimeLW) * duration);
                curTime[4][0] = (float) ((coartpre.apextimeULP) * duration);
                curTime[5][0] = (float) ((coartpre.apextimeLLP) * duration);
                curTime[6][0] = (float) ((coartpre.apextimeCR) * duration);
                curTime[7][0] = (float) ((coartpre.apextimeLLO) * duration);

            }
            // + timePositionBegin;
            for (int i = 0; i < LipModel.NUM_LABIALS; i++) {
                curTime[i][0] = curTime[i][0] + (float) timePositionBegin;
            }

            // fin curTime--consonnant

        }

    }

    public void coarticulation(int index, int length, float[][] preVisemeSucTarget, float[][] sucVisemePreTarget) {
        CalculationCoarticulation calculationCoarticulation = new CalculationCoarticulation();
        calculationCoarticulation.setParameter(curPho, prePho, sucPho, prePhysiquePho, sucPhysiquePho, curTime, preTime, sucTime, preTarget, sucTarget, index, length, preVisemeSucTarget, sucVisemePreTarget);
        curTarget = calculationCoarticulation.calculation();

    }

    public void setPreTime(float[][] preTime) {
        this.preTime = preTime;
    }

    public void setSucTime(float[][] sucTime) {
        this.sucTime = sucTime;
    }

    public FAPPhoneme lipToFap() {

        int curIndex = currentIndex;
        int length = currentSequenceLength;

        int fap[] = new int[69];
        int i;
        for (i = 0; i < 69; i++) {
            fap[i] = 0;
        }

        float rule1[] = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
        // rule1: it is applied on the internal and the external lip bounder and it allows us to simulate lip pression or lip thinning
        float rule4 = -0.05f;
        // rule4: tip nouse movement because of the lip protrusion
        float rule5 = 0.2f;
        // rule 5: nostril movement because of lip width
        // float rule6[2] = {1, 1}; //not used
        // rule 6: asymmetric expression in disgust

        // fapPho : 64 fap at 1 time
        FAPPhoneme fapPho = new FAPPhoneme();
        fapPho.pho = curPho;

        //coef is needed to diminish the tension on the lip near the beginning and the end of the speech
        if (timePositionBegin <= 0.3) {
            coef = (float) (timePositionBegin / 0.3);
        }

        if (visemeDuration >= (sequencenDuration - (sequencenDuration/5))) {
            coef = (float) (1 - (     ( visemeDuration      -   (    sequencenDuration - sequencenDuration / 5    ))   / (sequencenDuration / 5)               )                     );
        }

        if (!curPho.isPause()) {
            // ENGINE_LIP_WEIGHT
            generalTensionSup = LipModel.BaseTensionSup * coef;
            generalTensionInf = LipModel.BaseTensionInf * coef;

        } else {
            // ENGINE_LIP_WEIGHT
            generalTensionSup = LipModel.BaseTensionSup * coef * 4f / 5f;
            generalTensionInf = LipModel.BaseTensionInf * coef * 6f / 7f;
        }

        if (curPho.isLabiodental()) {   // inSeq=="f" || inSeq=="v"
        }

        if (curPho.isBilabial()) {
            // Lip pression when a bilabial consonant is uttered    inSeq=="b" || inSeq=="m" || inSeq=="p"
            rule1[1] *= 3.0f;
            rule1[3] *= 1.0f;

        } else {
            // Texts en dessous proviennent le mail de Elisabetta 13/07/2012
            //Le if((*inPho).TargetLeft[0][3]>0) devrait être comme ça :
            //   if((is_vowel((*inPho).Pho) && (*inPho).TargetLeft[3][1]>0) ||
            //      (!is_vowel((*inPho).Pho) && (*inPho).TargetLeft[3][0]>0))
            //
            //   et le if((*inPho).TargetLeft[0][4]>0) devrait être :
            //         if((is_vowel((*inPho).Pho) && (*inPho).TargetLeft[4][1]>0) ||
            //           (!is_vowel((*inPho).Pho) && (*inPho).TargetLeft[4][0]>0))

            // C ++ : if ((*inPho).TargetLeft[0][3]>0)
            if ((curPho.isVowel() && preTarget[3][1] > 0) || ((!curPho.isVowel()) && preTarget[3][0] > 0)) {
                // C ++ : if ((*inPho).TargetLeft[0][3]>0)
                //lips get thinner because lip width increases
                rule1[0] *= 0.9f;
                rule1[1] *= 0.7f;
                rule1[2] *= 0.9f;
                rule1[3] *= 0.6f;
            } else if ((curPho.isVowel() && preTarget[3][1] < 0) || ((!curPho.isVowel()) && preTarget[3][0] < 0)) {
                // C ++ : if ((*inPho).TargetLeft[0][3]<0)
                rule1[1] *= 1.1f;
                rule1[3] *= 1.1f;
            }

            if (curIndex > 1) {
                if ((curIndex - 1) >= 0 && (curIndex + 1) < (length - 1)) {
                    if ((prePho.isBilabial()) || (sucPho.isBilabial())) {
                        //lip pression because the phoneme is adjacent to a bilabial one
                        rule1[1] *= 0.8f;
                        rule1[3] *= 0.8f;
                    }
                }
            }
        }

        if ((curPho.isVowel() && preTarget[4][1] > 0) || ((!curPho.isVowel()) && preTarget[4][0] > 0)) {
            // C ++ : if ((*inPho).TargetLeft[0][4]>0)
            // tip nouse movement because of the lip protrusion
            rule4 *= -0.05f;
        }

        if ((curPho.isVowel() && preTarget[3][1] > 0) || ((!curPho.isVowel()) && preTarget[3][0] > 0)) {
            // C ++ : if ((*inPho).TargetLeft[0][3]>0)
            // nostril movement because of lip width
            rule5 *= 0.25f;
        } else {
            rule5 *= 0.1f;
        }

        int j;
        if (curPho.isVowel()) {
            j = 3;
        } else {
            j = 1;
        }

        for (i = 0; i < j; i++) {
            fapPho.time[i][4] = curTime[0][i];
            fapPho.time[i][8] = curTime[0][i];
            fapPho.time[i][9] = curTime[0][i];
            fapPho.time[i][51] = curTime[0][i];
            fapPho.time[i][55] = curTime[0][i];
            fapPho.time[i][56] = curTime[0][i];

            fapPho.time[i][5] = curTime[1][i];
            fapPho.time[i][10] = curTime[1][i];
            fapPho.time[i][11] = curTime[1][i];
            fapPho.time[i][52] = curTime[1][i];
            fapPho.time[i][57] = curTime[1][i];
            fapPho.time[i][58] = curTime[1][i];

            fapPho.time[i][3] = curTime[2][i];
            fapPho.time[i][18] = curTime[2][i];    ////altro punto per il mento
            fapPho.time[i][41] = curTime[2][i];
            fapPho.time[i][42] = curTime[2][i];

            fapPho.time[i][6] = curTime[3][i];
            fapPho.time[i][7] = curTime[3][i];
            fapPho.time[i][53] = curTime[3][i];
            fapPho.time[i][54] = curTime[3][i];
            fapPho.time[i][61] = curTime[3][i];
            fapPho.time[i][62] = curTime[3][i];

            fapPho.time[i][17] = curTime[4][i];
            fapPho.time[i][63] = curTime[4][i];

            fapPho.time[i][16] = curTime[5][i];

            fapPho.time[i][12] = curTime[6][i];
            fapPho.time[i][13] = curTime[6][i];
            fapPho.time[i][59] = curTime[6][i];
            fapPho.time[i][60] = curTime[6][i];

            fapPho.time[i][1] = curTime[7][i];


            // UPPER LIP OPENING - Faps 4, 8, 9, 51, 55, 56
            fapPho.target[i][4] = (-curTarget[0][i] + fap[4]) * rule1[0];
            fapPho.target[i][8] = (-curTarget[0][i] + fap[8]) * rule1[0];
            fapPho.target[i][9] = (-curTarget[0][i] + fap[9]) * rule1[0];
            fapPho.target[i][51] = (-curTarget[0][i] + fap[51]) * rule1[1] + generalTensionSup;
            fapPho.target[i][55] = (-curTarget[0][i] + fap[55]) * rule1[1] + generalTensionSup;
            fapPho.target[i][56] = (-curTarget[0][i] + fap[56]) * rule1[1] + generalTensionSup;

            // LOWER LIP OPENING - Faps 5, 10, 11, 52, 57, 58
            fapPho.target[i][5] = (-curTarget[1][i] + fap[5]) * rule1[2] + generalTensionInf * 0.2f;
            fapPho.target[i][10] = (-curTarget[1][i] + fap[10]) * rule1[2] + generalTensionInf * 0.2f;
            fapPho.target[i][11] = (-curTarget[1][i] + fap[11]) * rule1[2] + generalTensionInf * 0.2f;
            fapPho.target[i][52] = (-curTarget[1][i] * 0.5f + fap[52]) * rule1[3] + generalTensionInf;
            fapPho.target[i][57] = (-curTarget[1][i] * 0.5f + fap[57]) * rule1[3] + generalTensionInf;
            fapPho.target[i][58] = (-curTarget[1][i] * 0.5f + fap[58]) * rule1[3] + generalTensionInf;

            // JAW - Faps 3, 41, 42
            fapPho.target[i][3] = (curTarget[2][i] + fap[3]) * rule1[4] - generalTensionInf * 0.01f;
            fapPho.target[i][18] = (curTarget[2][i] + fap[18]) * rule1[4];
            fapPho.target[i][41] = (curTarget[2][i] / 4) + fap[41];
            fapPho.target[i][42] = (curTarget[2][i] / 4) + fap[42];

            // LIP WIDTH - Faps 6, 7, 53, 54, 61, 62
            fapPho.target[i][6] = (curTarget[3][i] + fap[6]);
            fapPho.target[i][7] = (curTarget[3][i] + fap[7]);
            fapPho.target[i][53] = (curTarget[3][i] + fap[53]);
            fapPho.target[i][54] = (curTarget[3][i] + fap[54]);
            fapPho.target[i][61] = (curTarget[3][i] + fap[61]) * rule5;
            fapPho.target[i][62] = (curTarget[3][i] + fap[62]) * rule5;

            // UPPER LIP PROTRUSION - Faps Fap 17, 63
            fapPho.target[i][17] = curTarget[4][i] + fap[17];
            fapPho.target[i][63] = (curTarget[4][i] + fap[63]) * rule4;

            // LOWER LIP PROTRUSION - Faps 16
            fapPho.target[i][16] = curTarget[5][i] + fap[16];

            // CORNER LIP - Faps 12, 13, 59, 60
            fapPho.target[i][12] = curTarget[6][i] + fap[12];
            fapPho.target[i][13] = curTarget[6][i] + fap[13];
            fapPho.target[i][59] = curTarget[6][i] + fap[59];
            fapPho.target[i][60] = curTarget[6][i] + fap[60];

            // VISEME (blending factor with facial expression (*1000 due to int precision) - Fap 1
            if (curPho.isBilabial() || curPho.isLabiodental()) {
                fapPho.target[i][1] = 1000;
            }
            else if (curPho.isPause()) {
                fapPho.target[i][1] = 0;
            }
            else {
                fapPho.target[i][1] = 500;
            }
        }

        return fapPho;
    } // la fin de Lip2Fap
} //la fin de class Viseme

//-----------------------------------------------------------------------------