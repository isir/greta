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
public class CalculationCoarticulation {

    Phoneme curPho;
    Phoneme prePho;
    Phoneme sucPho;
    Phoneme prePhysiquePho;
    Phoneme sucPhysiquePho;
    int currentIndex;
    int currentSequenceLength;
    float curTime[][] = new float[LipModel.NUM_LABIALS][3];
    float preTime[][] = new float[LipModel.NUM_LABIALS][3];
    float sucTime[][] = new float[LipModel.NUM_LABIALS][3];
    float preTarget[][] = new float[LipModel.NUM_LABIALS][3];
    float sucTarget[][] = new float[LipModel.NUM_LABIALS][3];
    float curTarget[][] = new float[LipModel.NUM_LABIALS][3];
    float[][] preVisemeSucTarget = new float[LipModel.NUM_LABIALS][3];
    float[][] sucVisemePreTarget = new float[LipModel.NUM_LABIALS][3];
    double strong = LipModel.STRONG;
    double mild = LipModel.MILD;
    double weak = LipModel.WEAK;

    public void setParameter(Phoneme curPho, Phoneme prePho, Phoneme sucPho, Phoneme prePhysiquePho, Phoneme sucPhysiquePho, float[][] curTime, float[][] preTime, float[][] sucTime, float[][] preTarget, float[][] sucTarget, int index, int length, float[][] preVisemeSucTarget, float[][] sucVisemePreTarget) {
        this.curPho = curPho;
        this.prePho = prePho;
        this.sucPho = sucPho;
        this.prePhysiquePho = prePhysiquePho;
        this.sucPhysiquePho = sucPhysiquePho;
        this.curTime = curTime;
        this.preTime = preTime;
        this.sucTime = sucTime;
        this.preTarget = preTarget;
        this.sucTarget = sucTarget;
        this.currentIndex = index;
        this.currentSequenceLength = length;
        this.preVisemeSucTarget = preVisemeSucTarget;
        this.sucVisemePreTarget = sucVisemePreTarget;
    }

    public float[][] calculation() {
        if (curPho.isVowel()) {
            vowelCoarticulation();
        } else {
            if (!curPho.isPause()) {
                if (prePho != null && sucPho != null && (!(prePho.isPause())) && (!(sucPho.isPause()))) {
                    Consonant_Coarticulation();
                    if (curPho.equals(Phoneme.PhonemeType.tS)
                            || curPho.equals(Phoneme.PhonemeType.SS)
                            || curPho.equals(Phoneme.PhonemeType.q)
                            || curPho.equals(Phoneme.PhonemeType.w)
                            || curPho.isBilabial()
                            || curPho.isLabiodental()) {
                        //TODO
                    }
                } else if ((sucPho != null) && (!(sucPho.isPause()))) {
                    curTarget = sucTarget;
                } else if ((prePho != null) && (!(prePho.isPause()))) {
                    curTarget = preTarget;
                }
            }
        }
        return curTarget;
    }

    public void vowelCoarticulation() {

        double fracteur;
        int index_labiaux;
        // index_labiaux = 0 : upper_lip_opening
        // index_labiaux = 1 : lower_lip_opening
        // index_labiaux = 2 : jaw
        // index_labiaux = 3 : lip width
        // index_labiaux = 4 : upper lip protrusion
        // index_labiaux = 5 : lower lip protrusion
        // index_labiaux = 6 : corner lip
        int index_TargetPoint;
        // Because of vowel, index_TargetPoint = 0,1,2

        // case 1
        // index_labiaux = 0,1,2 : upper_lip_opening, lower_lip_opening, jaw
        if ((sucPho.isBilabial() || sucPho.isLabiodental() || sucPho.equals(Phoneme.PhonemeType.tS) || sucPho.equals(Phoneme.PhonemeType.SS) || sucPho.equals(Phoneme.PhonemeType.q) || sucPho.equals(Phoneme.PhonemeType.w)) && !(prePho.isBilabial() || prePho.isLabiodental() || prePho.equals(Phoneme.PhonemeType.tS) || prePho.equals(Phoneme.PhonemeType.SS) || prePho.equals(Phoneme.PhonemeType.q) || prePho.equals(Phoneme.PhonemeType.w))) {
            for (index_labiaux = 0; index_labiaux <= 2; index_labiaux++) {
                for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {

                    fracteur = (curTime[index_labiaux][index_TargetPoint] - preTime[index_labiaux][0]) / (sucTime[index_labiaux][0] - preTime[index_labiaux][0]);
                    curTarget[index_labiaux][index_TargetPoint] = (float) (A_coart(mild, fracteur) * sucTarget[index_labiaux][index_TargetPoint] + (1 - A_coart(mild, fracteur)) * preTarget[index_labiaux][index_TargetPoint]);
                }
            }
        } else if (prePho.isBilabial() || prePho.isLabiodental() || prePho.equals(Phoneme.PhonemeType.tS) || prePho.equals(Phoneme.PhonemeType.SS) || prePho.equals(Phoneme.PhonemeType.q) || prePho.equals(Phoneme.PhonemeType.w)) {
            for (index_labiaux = 0; index_labiaux <= 2; index_labiaux++) {
                for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {

                    fracteur = (curTime[index_labiaux][index_TargetPoint] - preTime[index_labiaux][0]) / (sucTime[index_labiaux][0] - preTime[index_labiaux][0]);
                    curTarget[index_labiaux][index_TargetPoint] = (float) (CO_coart(mild, fracteur) * preTarget[index_labiaux][index_TargetPoint] + (1 - CO_coart(mild, fracteur)) * sucTarget[index_labiaux][index_TargetPoint]);
                }
            }
        } else // all the other consnants
        {
            for (index_labiaux = 0; index_labiaux <= 2; index_labiaux++) {
                for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {

                    fracteur = (curTime[index_labiaux][index_TargetPoint] - preTime[index_labiaux][0]) / (sucTime[index_labiaux][0] - preTime[index_labiaux][0]);
                    curTarget[index_labiaux][index_TargetPoint] = (float) ((1 - fracteur) * preTarget[index_labiaux][index_TargetPoint] + fracteur * sucTarget[index_labiaux][index_TargetPoint]);
                }
            }
        }
        // case 2
        // index_labiaux = 3 : lip width
        if ((sucPho.equals(Phoneme.PhonemeType.tS) || sucPho.equals(Phoneme.PhonemeType.SS) || sucPho.equals(Phoneme.PhonemeType.q) || sucPho.equals(Phoneme.PhonemeType.w)) && (!(prePho.equals(Phoneme.PhonemeType.tS) || prePho.equals(Phoneme.PhonemeType.SS) || prePho.equals(Phoneme.PhonemeType.q) || prePho.equals(Phoneme.PhonemeType.w)))) {
            for (index_labiaux = 3; index_labiaux <= 3; index_labiaux++) {
                for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {
                    fracteur = (curTime[index_labiaux][index_TargetPoint] - preTime[index_labiaux][0]) / (sucTime[index_labiaux][0] - preTime[index_labiaux][0]);
                    curTarget[index_labiaux][index_TargetPoint] = (float) (A_coart(strong, fracteur) * sucTarget[index_labiaux][index_TargetPoint] + (1 - A_coart(strong, fracteur)) * preTarget[index_labiaux][index_TargetPoint]);
                }
            }
        } else if (prePho.equals(Phoneme.PhonemeType.tS) || prePho.equals(Phoneme.PhonemeType.SS) || prePho.equals(Phoneme.PhonemeType.q) || prePho.equals(Phoneme.PhonemeType.w)) {
            for (index_labiaux = 3; index_labiaux <= 3; index_labiaux++) {
                for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {

                    fracteur = (curTime[index_labiaux][index_TargetPoint] - preTime[index_labiaux][0]) / (sucTime[index_labiaux][0] - preTime[index_labiaux][0]);
                    curTarget[index_labiaux][index_TargetPoint] = (float) (CO_coart(strong, fracteur) * preTarget[index_labiaux][index_TargetPoint] + (1 - CO_coart(strong, fracteur)) * sucTarget[index_labiaux][index_TargetPoint]);
                }
            }
        } else // all the other consnants
        {
            for (index_labiaux = 3; index_labiaux <= 3; index_labiaux++) {
                for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {

                    fracteur = (curTime[index_labiaux][index_TargetPoint] - preTime[index_labiaux][0]) / (sucTime[index_labiaux][0] - preTime[index_labiaux][0]);
                    curTarget[index_labiaux][index_TargetPoint] = (float) ((1 - fracteur) * preTarget[index_labiaux][index_TargetPoint] + fracteur * preTarget[index_labiaux][index_TargetPoint]);
                }
            }
        }

        // case 3
        // index_labiaux = 4,5 : upper lip protrusion, lower lip protrusion
        if ((sucPho.equals(Phoneme.PhonemeType.tS) || sucPho.equals(Phoneme.PhonemeType.SS) || sucPho.equals(Phoneme.PhonemeType.q) || sucPho.equals(Phoneme.PhonemeType.w) || sucPho.isLabiodental()) && !(prePho.equals(Phoneme.PhonemeType.tS) || prePho.equals(Phoneme.PhonemeType.SS) || prePho.equals(Phoneme.PhonemeType.q) || prePho.equals(Phoneme.PhonemeType.w) || prePho.isLabiodental())) {
            for (index_labiaux = 4; index_labiaux <= 5; index_labiaux++) {
                for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {
                    fracteur = (curTime[index_labiaux][index_TargetPoint] - preTime[index_labiaux][0]) / (sucTime[index_labiaux][0] - preTime[index_labiaux][0]);
                    curTarget[index_labiaux][index_TargetPoint] = (float) (A_coart(strong, fracteur) * sucTarget[index_labiaux][index_TargetPoint] + (1 - A_coart(strong, fracteur)) * preTarget[index_labiaux][index_TargetPoint]);
                }
            }
        } else if (prePho.equals(Phoneme.PhonemeType.tS) || prePho.equals(Phoneme.PhonemeType.SS) || prePho.equals(Phoneme.PhonemeType.q) || prePho.equals(Phoneme.PhonemeType.w) || prePho.isLabiodental()) {
            for (index_labiaux = 4; index_labiaux <= 5; index_labiaux++) {
                for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {

                    fracteur = (curTime[index_labiaux][index_TargetPoint] - preTime[index_labiaux][0]) / (sucTime[index_labiaux][0] - preTime[index_labiaux][0]);
                    curTarget[index_labiaux][index_TargetPoint] = (float) (CO_coart(strong, fracteur) * preTarget[index_labiaux][index_TargetPoint] + (1 - CO_coart(strong, fracteur)) * sucTarget[index_labiaux][index_TargetPoint]);
                }
            }
        } else // all the other consnants
        {
            for (index_labiaux = 4; index_labiaux <= 5; index_labiaux++) {
                for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {
                    fracteur = (curTime[index_labiaux][index_TargetPoint] - preTime[index_labiaux][0]) / (sucTime[index_labiaux][0] - preTime[index_labiaux][0]);
                    curTarget[index_labiaux][index_TargetPoint] = (float) ((1 - fracteur) * preTarget[index_labiaux][index_TargetPoint] + fracteur * preTarget[index_labiaux][index_TargetPoint]);
                }
            }
        }
        // case 4
        // index_labiaux = 6 : corner lip
        if ((sucPho.equals(Phoneme.PhonemeType.tS) || sucPho.equals(Phoneme.PhonemeType.SS) || sucPho.equals(Phoneme.PhonemeType.q) || sucPho.equals(Phoneme.PhonemeType.w) || sucPho.isLabiodental()) && !(prePho.equals(Phoneme.PhonemeType.tS) || prePho.equals(Phoneme.PhonemeType.SS) || prePho.equals(Phoneme.PhonemeType.q) || prePho.equals(Phoneme.PhonemeType.w) || prePho.isLabiodental())) {
            for (index_labiaux = 6; index_labiaux <= 6; index_labiaux++) {
                for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {

                    fracteur = (curTime[index_labiaux][index_TargetPoint] - preTime[index_labiaux][0]) / (sucTime[index_labiaux][0] - preTime[index_labiaux][0]);
                    curTarget[index_labiaux][index_TargetPoint] = (float) (A_coart(strong, fracteur) * preTarget[index_labiaux][index_TargetPoint] + (1 - A_coart(strong, fracteur)) * sucTarget[index_labiaux][index_TargetPoint]);
                }
            }
        } else if (prePho.equals(Phoneme.PhonemeType.tS) || prePho.equals(Phoneme.PhonemeType.SS) || prePho.equals(Phoneme.PhonemeType.q) || prePho.equals(Phoneme.PhonemeType.w) || prePho.isLabiodental()) {
            for (index_labiaux = 6; index_labiaux <= 6; index_labiaux++) {
                for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {

                    fracteur = (curTime[index_labiaux][index_TargetPoint] - preTime[index_labiaux][0]) / (sucTime[index_labiaux][0] - preTime[index_labiaux][0]);
                    curTarget[index_labiaux][index_TargetPoint] = (float) (CO_coart(strong, fracteur) * preTarget[index_labiaux][index_TargetPoint] + (1 - CO_coart(strong, fracteur)) * sucTarget[index_labiaux][index_TargetPoint]);
                }
            }
        } else //all the other consonants
        {
            for (index_labiaux = 6; index_labiaux <= 6; index_labiaux++) {
                for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {

                    fracteur = (curTime[index_labiaux][index_TargetPoint] - preTime[index_labiaux][0]) / (sucTime[index_labiaux][0] - preTime[index_labiaux][0]);
                    curTarget[index_labiaux][index_TargetPoint] = (float) ((1 - fracteur) * preTarget[index_labiaux][index_TargetPoint] + fracteur * sucTarget[index_labiaux][index_TargetPoint]);

                }
            }
        }

        // if(precPho != "NULL")
        if (currentIndex != 0) {
            if ((prePhysiquePho.equals(Phoneme.PhonemeType.u) || prePhysiquePho.equals(Phoneme.PhonemeType.u1) || prePhysiquePho.equals(Phoneme.PhonemeType.o) || prePhysiquePho.equals(Phoneme.PhonemeType.o1) || prePhysiquePho.equals(Phoneme.PhonemeType.O1) || prePhysiquePho.equals(Phoneme.PhonemeType.y)) && (!(curPho.equals(Phoneme.PhonemeType.u)) && !(curPho.equals(Phoneme.PhonemeType.u1)) && !(curPho.equals(Phoneme.PhonemeType.o)) && !(curPho.equals(Phoneme.PhonemeType.o1)) && !(curPho.equals(Phoneme.PhonemeType.O1)) && !(curPho.equals(Phoneme.PhonemeType.y)))) {
                for (index_labiaux = 0; index_labiaux <= 6; index_labiaux++) {
                    for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {
                        curTarget[index_labiaux][index_TargetPoint] = (preVisemeSucTarget[index_labiaux][index_TargetPoint] + preTarget[index_labiaux][index_TargetPoint]) / 2;
                    }
                }
            }
        }

        // if(sucPho != "NULL")
        if (currentIndex != (currentSequenceLength - 1)) {
            if ((sucPhysiquePho.equals(Phoneme.PhonemeType.u) || sucPhysiquePho.equals(Phoneme.PhonemeType.u1) || sucPhysiquePho.equals(Phoneme.PhonemeType.o) || sucPhysiquePho.equals(Phoneme.PhonemeType.o1) || sucPhysiquePho.equals(Phoneme.PhonemeType.O1) || sucPhysiquePho.equals(Phoneme.PhonemeType.y)) && (!(curPho.equals(Phoneme.PhonemeType.u)) && !(curPho.equals(Phoneme.PhonemeType.u1)) && !(curPho.equals(Phoneme.PhonemeType.o)) && !(curPho.equals(Phoneme.PhonemeType.o1)) && !(curPho.equals(Phoneme.PhonemeType.O1)) && !(curPho.equals(Phoneme.PhonemeType.y)))) {
                for (index_labiaux = 0; index_labiaux <= 6; index_labiaux++) {
                    for (index_TargetPoint = 0; index_TargetPoint < 3; index_TargetPoint++) {
                        curTarget[index_labiaux][index_TargetPoint] = (sucVisemePreTarget[index_labiaux][index_TargetPoint] + sucTarget[index_labiaux][index_TargetPoint]) / 2;
                    }
                }
            }
        }


    }    // end of vowelCoarticulation

    public double A_coart(double b, double fracteur) {

        double a, c;
        double exp_b = java.lang.Math.exp(-b);
        double exp_bfacteur = java.lang.Math.exp(-b * fracteur);

        c = (1 + exp_b) / (exp_b - 1);
        a = -2 * c;

        return (a / (1 + exp_bfacteur) + c);
    }

    public double CO_coart(double b, double fracteur) {

        double a, c;
        fracteur -= 1;
        double expb = java.lang.Math.exp(b);
        double exp_bfacteur = java.lang.Math.exp(-b * fracteur);
        c = (expb + 1) / (expb - 1);
        a = -2 * c;

        return (a / (1 + exp_bfacteur) + c);
    }

    public void Consonant_Coarticulation() {

        double fracteur = 0;

        int index_labiaux;
        // index_labiaux = 0 : upper_lip_opening
        // index_labiaux = 1 : lower_lip_opening
        // index_labiaux = 2 : jaw
        // index_labiaux = 3 : lip width
        // index_labiaux = 4 : upper lip protrusion
        // index_labiaux = 5 : lower lip protrusion
        // index_labiaux = 6 : corner lip

        // case 1
        // index_labiaux = 0,1,2 : upper_lip_opening, lower_lip_opening, jaw
        if (prePho.equals(Phoneme.PhonemeType.a)) {
            for (index_labiaux = 0; index_labiaux <= 2; index_labiaux++) {
                //NOTE (AM) : if1
                fracteur = getConsonantNormalizedTime(index_labiaux);
                curTarget[index_labiaux][0] = (float) (CO_coart(strong, fracteur) * preTarget[index_labiaux][0] + (1 - CO_coart(strong, fracteur)) * sucTarget[index_labiaux][0]);
            }
        }
        if (sucPho.equals(Phoneme.PhonemeType.a)) {
            //NOTE (AM) : if2
            for (index_labiaux = 0; index_labiaux <= 2; index_labiaux++) {
                fracteur = getConsonantNormalizedTime(index_labiaux);
                curTarget[index_labiaux][0] = (float) (A_coart(strong, fracteur) * sucTarget[index_labiaux][0] + (1 - A_coart(strong, fracteur)) * preTarget[index_labiaux][0]);
            }
        }
        if ((prePho.equals(Phoneme.PhonemeType.e) || prePho.equals(Phoneme.PhonemeType.o)) && (sucPho.equals(Phoneme.PhonemeType.u) || sucPho.equals(Phoneme.PhonemeType.i) || sucPho.equals(Phoneme.PhonemeType.y))) {
            //NOTE (AM) : same in if1 using "mild" instead of "strong"
            for (index_labiaux = 0; index_labiaux <= 2; index_labiaux++) {
                fracteur = getConsonantNormalizedTime(index_labiaux);
                curTarget[index_labiaux][0] = (float) (CO_coart(mild, fracteur) * preTarget[index_labiaux][0] + (1 - CO_coart(mild, fracteur)) * sucTarget[index_labiaux][0]);
            }
        }
        if ((prePho.equals(Phoneme.PhonemeType.u) || prePho.equals(Phoneme.PhonemeType.i) || prePho.equals(Phoneme.PhonemeType.y)) && (sucPho.equals(Phoneme.PhonemeType.e) || sucPho.equals(Phoneme.PhonemeType.o))) {
            //NOTE (AM) : same in if2, exactly
            for (index_labiaux = 0; index_labiaux <= 2; index_labiaux++) {
                fracteur = getConsonantNormalizedTime(index_labiaux);
                curTarget[index_labiaux][0] = (float) (A_coart(strong, fracteur) * sucTarget[index_labiaux][0] + (1 - A_coart(strong, fracteur)) * preTarget[index_labiaux][0]);
            }
        }
        if ((prePho.equals(Phoneme.PhonemeType.e) || prePho.equals(Phoneme.PhonemeType.o)) && (sucPho.equals(Phoneme.PhonemeType.o) || sucPho.equals(Phoneme.PhonemeType.e))) {
            //NOTE (AM) : if5
            for (index_labiaux = 0; index_labiaux <= 2; index_labiaux++) {
                fracteur = getConsonantNormalizedTime(index_labiaux);
                curTarget[index_labiaux][0] = (float) ((1 - fracteur) * preTarget[index_labiaux][0] + fracteur * sucTarget[index_labiaux][0]);
            }
        }
        if ((prePho.equals(Phoneme.PhonemeType.u) || prePho.equals(Phoneme.PhonemeType.i) || prePho.equals(Phoneme.PhonemeType.y)) && (sucPho.equals(Phoneme.PhonemeType.u) || sucPho.equals(Phoneme.PhonemeType.i) || sucPho.equals(Phoneme.PhonemeType.y))) {
            //NOTE (AM) : same in if5, exactly
            for (index_labiaux = 0; index_labiaux <= 2; index_labiaux++) {
                fracteur = getConsonantNormalizedTime(index_labiaux);
                curTarget[index_labiaux][0] = (float) ((1 - fracteur) * preTarget[index_labiaux][0] + fracteur * sucTarget[index_labiaux][0]);
            }
        }
        if (!curPho.equals(Phoneme.PhonemeType.tS) && !curPho.equals(Phoneme.PhonemeType.tS) && !curPho.equals(Phoneme.PhonemeType.q) && !curPho.equals(Phoneme.PhonemeType.w)) {
            // case 2
            // index_labiaux = 3 : lip width
            if (sucPho.equals(Phoneme.PhonemeType.a) && (prePho.equals(Phoneme.PhonemeType.e) || prePho.equals(Phoneme.PhonemeType.i) || prePho.equals(Phoneme.PhonemeType.u) || prePho.equals(Phoneme.PhonemeType.o) || prePho.equals(Phoneme.PhonemeType.y))) {
                for (index_labiaux = 3; index_labiaux <= 3; index_labiaux++) {
                    fracteur = getConsonantNormalizedTime(index_labiaux);
                    curTarget[index_labiaux][0] = (float) (CO_coart(strong, fracteur) * preTarget[index_labiaux][0] + (1 - CO_coart(strong, fracteur)) * sucTarget[index_labiaux][0]);
                }
            }
            if (prePho.equals(Phoneme.PhonemeType.a) && (sucPho.equals(Phoneme.PhonemeType.e) || sucPho.equals(Phoneme.PhonemeType.i) || sucPho.equals(Phoneme.PhonemeType.u) || sucPho.equals(Phoneme.PhonemeType.o) || sucPho.equals(Phoneme.PhonemeType.y))) {
                for (index_labiaux = 3; index_labiaux <= 3; index_labiaux++) {
                    fracteur = getConsonantNormalizedTime(index_labiaux);
                    curTarget[index_labiaux][0] = (float) (A_coart(strong, fracteur) * sucTarget[index_labiaux][0] + (1 - A_coart(strong, fracteur)) * preTarget[index_labiaux][0]);
                }
            }
            if ((prePho.equals(Phoneme.PhonemeType.e) || prePho.equals(Phoneme.PhonemeType.i) || prePho.equals(Phoneme.PhonemeType.u) || prePho.equals(Phoneme.PhonemeType.o) || prePho.equals(Phoneme.PhonemeType.y)) && (sucPho.equals(Phoneme.PhonemeType.e) || sucPho.equals(Phoneme.PhonemeType.i) || sucPho.equals(Phoneme.PhonemeType.u) || sucPho.equals(Phoneme.PhonemeType.o) || sucPho.equals(Phoneme.PhonemeType.y))) {
                for (index_labiaux = 3; index_labiaux <= 3; index_labiaux++) {
                    fracteur = getConsonantNormalizedTime(index_labiaux);
                    curTarget[index_labiaux][0] = (float) ((1 - fracteur) * preTarget[index_labiaux][0] + fracteur * sucTarget[index_labiaux][0]);
                }
            }

            // case 3
            // index_labiaux = 4,5 : upper lip protrusion, lower lip protrusion
            if ((prePho.equals(Phoneme.PhonemeType.u) || prePho.equals(Phoneme.PhonemeType.o) || prePho.equals(Phoneme.PhonemeType.y)) && (sucPho.equals(Phoneme.PhonemeType.u) || sucPho.equals(Phoneme.PhonemeType.o) || sucPho.equals(Phoneme.PhonemeType.y) || sucPho.equals(Phoneme.PhonemeType.a) || sucPho.equals(Phoneme.PhonemeType.e) || sucPho.equals(Phoneme.PhonemeType.i))) {
                for (index_labiaux = 4; index_labiaux <= 5; index_labiaux++) {
                    fracteur = getConsonantNormalizedTime(index_labiaux);
                    curTarget[index_labiaux][0] = (float) (CO_coart(strong, fracteur) * preTarget[index_labiaux][0] + (1 - CO_coart(strong, fracteur)) * sucTarget[index_labiaux][0]);
                }
            }
            if ((sucPho.equals(Phoneme.PhonemeType.u) || sucPho.equals(Phoneme.PhonemeType.o) || sucPho.equals(Phoneme.PhonemeType.y)) && (prePho.equals(Phoneme.PhonemeType.u) || prePho.equals(Phoneme.PhonemeType.o) || prePho.equals(Phoneme.PhonemeType.y) || prePho.equals(Phoneme.PhonemeType.a) || prePho.equals(Phoneme.PhonemeType.e) || prePho.equals(Phoneme.PhonemeType.i))) {
                for (index_labiaux = 4; index_labiaux <= 5; index_labiaux++) {
                    fracteur = getConsonantNormalizedTime(index_labiaux);
                    curTarget[index_labiaux][0] = (float) (A_coart(strong, fracteur) * sucTarget[index_labiaux][0] + (1 - A_coart(strong, fracteur)) * preTarget[index_labiaux][0]);
                }
            }
            if ((prePho.equals(Phoneme.PhonemeType.a) || prePho.equals(Phoneme.PhonemeType.e) || prePho.equals(Phoneme.PhonemeType.i)) && (sucPho.equals(Phoneme.PhonemeType.a) || sucPho.equals(Phoneme.PhonemeType.e) || sucPho.equals(Phoneme.PhonemeType.i))) {
                for (index_labiaux = 4; index_labiaux <= 5; index_labiaux++) {
                    fracteur = getConsonantNormalizedTime(index_labiaux);
                    curTarget[index_labiaux][0] = (float) ((1 - fracteur) * preTarget[index_labiaux][0] + fracteur * sucTarget[index_labiaux][0]);
                }
            }
            // case 4
            // index_labiaux = 6 : corner lip
            if ((prePho.equals(Phoneme.PhonemeType.u) || prePho.equals(Phoneme.PhonemeType.o) || prePho.equals(Phoneme.PhonemeType.y)) && (sucPho.equals(Phoneme.PhonemeType.u) || sucPho.equals(Phoneme.PhonemeType.o) || sucPho.equals(Phoneme.PhonemeType.y) || sucPho.equals(Phoneme.PhonemeType.a) || sucPho.equals(Phoneme.PhonemeType.e) || sucPho.equals(Phoneme.PhonemeType.i))) {
                for (index_labiaux = 6; index_labiaux <= 6; index_labiaux++) {
                    fracteur = getConsonantNormalizedTime(index_labiaux);
                    curTarget[index_labiaux][0] = (float) (CO_coart(strong, fracteur) * preTarget[index_labiaux][0] + (1 - CO_coart(strong, fracteur)) * sucTarget[index_labiaux][0]);
                }
            }
            if ((sucPho.equals(Phoneme.PhonemeType.u) || sucPho.equals(Phoneme.PhonemeType.o) || sucPho.equals(Phoneme.PhonemeType.y)) && (prePho.equals(Phoneme.PhonemeType.u) || prePho.equals(Phoneme.PhonemeType.o) || prePho.equals(Phoneme.PhonemeType.y) || prePho.equals(Phoneme.PhonemeType.a) || prePho.equals(Phoneme.PhonemeType.e) || prePho.equals(Phoneme.PhonemeType.i))) {
                for (index_labiaux = 6; index_labiaux <= 6; index_labiaux++) {
                    fracteur = getConsonantNormalizedTime(index_labiaux);
                    curTarget[index_labiaux][0] = (float) (A_coart(strong, fracteur) * sucTarget[index_labiaux][0] + (1 - A_coart(strong, fracteur)) * preTarget[index_labiaux][0]);
                }
            }
            if ((prePho.equals(Phoneme.PhonemeType.a) || prePho.equals(Phoneme.PhonemeType.e) || prePho.equals(Phoneme.PhonemeType.i)) && (sucPho.equals(Phoneme.PhonemeType.a) || sucPho.equals(Phoneme.PhonemeType.e) || sucPho.equals(Phoneme.PhonemeType.i))) {
                for (index_labiaux = 6; index_labiaux <= 6; index_labiaux++) {
                    fracteur = getConsonantNormalizedTime(index_labiaux);
                    curTarget[index_labiaux][0] = (float) ((1 - fracteur) * preTarget[index_labiaux][0] + fracteur * sucTarget[index_labiaux][0]);
                }
            }
        }
    } // end of Consonant_Coarticulation

    private double getConsonantNormalizedTime(int index) {
        return (curTime[index][0] - preTime[index][0]) / (sucTime[index][0] - preTime[index][0]);
    }
} // end of class CalculationCoarticulation