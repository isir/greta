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

import java.util.List;

/**
 *
 * @author Yu Ding
 */
public class InterpolationLip {

    List<FAPPhoneme> fapPhonemes;
    double totalDuration;
    float[][] targetTimes;
    float[][] targetFaps;
    int j = 0;
    int targetLength = 0;
    int startTargetLengthEnd = 0;
    int outSequenceLength = 0;
    int[] indexMinTime = new int[LipModel.NUM_LABIALS];
    int[] indexMaxTime = new int[LipModel.NUM_LABIALS];

    int[] vcLable = null;

    InterpolationLip(List<FAPPhoneme> fapPhonemes, double totalDuration) {
        this.fapPhonemes = fapPhonemes;
        int length = fapPhonemes.size();
        //==========//
        vcLable = new int[length+2];
        //==== +2 means 1st and last pho are pause
        vcLable[0] = 1;
        vcLable[length+1] = 1;
        this.totalDuration = totalDuration;

        // computing targetLength
        for (int i = 0; i < length; i++) {
            int targetNb = ((fapPhonemes.get(i).pho.isVowel()) ? 3 : 1);
            targetLength = targetLength + targetNb;
        }
        startTargetLengthEnd = targetLength + 2;
        // fin de computing targetLength
        // targetLength + 1 start + 1 end
        targetTimes = new float[LipModel.NUM_LABIALS][startTargetLengthEnd];
        targetFaps = new float[64][startTargetLengthEnd];

        // time sequence
        for (int type = 0; type < LipModel.NUM_LABIALS; type++) {
            // type = 0; UPPER LIP OPENING - Faps 4, 8, 9, 51, 55, 56
            // type = 1; LOWER LIP OPENING - Faps 5, 10, 11, 52, 57, 58
            // type = 2; JAW - Faps 3, 41, 42
            // type = 3; LIP WIDTH - Faps 6, 7, 53, 54, 61, 62
            // type = 4; UPPER LIP PROTRUSION - Faps Fap 17, 63
            // type = 5; LOWER LIP PROTRUSION - Faps 16
            // type = 6; CORNER LIP - Faps 12, 13, 59, 60
            // type = 7; VISEME (blending factor with facial expression) - Fap 1
            int[] typeTarget = null;
            float[] targetFap = null;

            switch (type) {
                case 0:
                    typeTarget = new int[]{4, 8, 9, 51, 55, 56};
                    break;
                case 1:
                    typeTarget = new int[]{5, 10, 11, 52, 57, 58};
                    break;
                case 2:
                    typeTarget = new int[]{3, 41, 42};
                    break;
                case 3:
                    typeTarget = new int[]{6, 7, 53, 54, 61, 62};
                    break;
                case 4:
                    typeTarget = new int[]{17, 63};
                    break;
                case 5:
                    typeTarget = new int[]{16};
                    break;
                case 6:
                    typeTarget = new int[]{12, 13, 59, 60};
                    break;
                case 7:
                    typeTarget = new int[]{1};
                    break;
                default:
                    System.out.println("ERROR : Interpolation--Out of Lip : 0, 1, 2, 3, 4, 5, 6, 7");
            }
            // 1  targetTimes[type][0]
            targetTimes[type][0] = 0;
            // 2  targetTimes[type][0<...<end]
            j = 1;
            for (int i = 0; i < length; i++) {
                int targetNb = ((fapPhonemes.get(i).pho.isVowel()) ? 3 : 1);
                //==========//
                vcLable[i+1] = ((fapPhonemes.get(i).pho.isVowel()) ? 3 : 1);
                //==== +1 means that 1st pho is pause

                for (int t = 0; t < targetNb; t++) {
                    int pp = typeTarget[0];
                    // float time[][] = new float[3][64];
                    targetTimes[type][j] = fapPhonemes.get(i).time[t][pp];
                    j = j + 1;
                }
            }
            // 3  targetTimes[type][end]
            targetTimes[type][startTargetLengthEnd - 1] = (float) totalDuration;

        }

        // targetFaps
        // 1   targetFaps[:][0]
        for (int i = 0; i < 64; i++) {
            targetFaps[i][0] = 0;
        }
        // 2   targetFaps[:][end]
        for (int i = 0; i < 64; i++) {
            targetFaps[i][startTargetLengthEnd - 1] = 0;
        }
        // 3   targetFaps[:][0<...<end]
        j = 1; //j=0; targetFaps[][0] = 0
        for (int i = 0; i < length; i++) {
            int targetNb = ((fapPhonemes.get(i).pho.isVowel()) ? 3 : 1);
            for (int t = 0; t < targetNb; t++) {
                // UPPER LIP OPENING - Faps 4, 8, 9, 51, 55, 56
                targetFaps[4][j] = fapPhonemes.get(i).target[t][4];
                targetFaps[8][j] = fapPhonemes.get(i).target[t][8];
                targetFaps[9][j] = fapPhonemes.get(i).target[t][9];
                targetFaps[51][j] = fapPhonemes.get(i).target[t][51];
                targetFaps[55][j] = fapPhonemes.get(i).target[t][55];
                targetFaps[56][j] = fapPhonemes.get(i).target[t][56];

                // LOWER LIP OPENING - Faps 5, 10, 11, 52, 57, 58
                targetFaps[5][j] = fapPhonemes.get(i).target[t][5];
                targetFaps[10][j] = fapPhonemes.get(i).target[t][10];
                targetFaps[11][j] = fapPhonemes.get(i).target[t][11];
                targetFaps[52][j] = fapPhonemes.get(i).target[t][52];
                targetFaps[57][j] = fapPhonemes.get(i).target[t][57];
                targetFaps[58][j] = fapPhonemes.get(i).target[t][58];

                // JAW - Faps 3, 41, 42
                targetFaps[3][j] = fapPhonemes.get(i).target[t][3];
                targetFaps[41][j] = fapPhonemes.get(i).target[t][41];
                targetFaps[42][j] = fapPhonemes.get(i).target[t][42];

                // LIP WIDTH - Faps 6, 7, 53, 54, 61, 62
                targetFaps[6][j] = fapPhonemes.get(i).target[t][6];
                targetFaps[7][j] = fapPhonemes.get(i).target[t][7];
                targetFaps[53][j] = fapPhonemes.get(i).target[t][53];
                targetFaps[54][j] = fapPhonemes.get(i).target[t][54];
                targetFaps[61][j] = fapPhonemes.get(i).target[t][61];
                targetFaps[62][j] = fapPhonemes.get(i).target[t][62];

                // UPPER LIP PROTRUSION - Faps Fap 17, 63
                targetFaps[17][j] = fapPhonemes.get(i).target[t][17];
                targetFaps[63][j] = fapPhonemes.get(i).target[t][63];

                // LOWER LIP PROTRUSION - Faps 16
                targetFaps[16][j] = fapPhonemes.get(i).target[t][16];

                // CORNER LIP - Faps 12, 13, 59, 60
                targetFaps[12][j] = fapPhonemes.get(i).target[t][12];
                targetFaps[13][j] = fapPhonemes.get(i).target[t][13];
                targetFaps[59][j] = fapPhonemes.get(i).target[t][59];
                targetFaps[60][j] = fapPhonemes.get(i).target[t][60];

                // VISEME (blending factor with facial expression (*1000 due to int precision) - Fap 1
                targetFaps[1][j] = fapPhonemes.get(i).target[t][1];

                j = j + 1;
            }
        }
    }

    public void printKeyFrameAndKeyTime()
    {
        int[] fapIndex = {1,4,5,6,7,8,9,10,11,12,13,51,52,53,54,55,56,57,58,59,60,16,17,3};
        for (int fp = 0; fp<24; fp++){
             for (int fm = 0; fm<startTargetLengthEnd; fm++){
                 System.out.print("  "+targetFaps[fapIndex[fp]][fm]);
             }
             System.out.println(" ");
        }
        System.out.println(" time as follows:");
        for (int fp = 0; fp<24; fp++){
            for (int fm = 0; fm<startTargetLengthEnd; fm++){
                 int type = getTypeNumber(fapIndex[fp]);
                 System.out.print("  "+targetTimes[type][fm]);
            }
            System.out.println(" ");
        }
    }

    public int getTypeNumber(int fapIndex)
    {
        int type = -1;

        if ((fapIndex==4)||(fapIndex==8)||(fapIndex==9)||(fapIndex==51)||(fapIndex==55)||(fapIndex==56))
        {
            type = 0;
        }
        else if ((fapIndex==5)||(fapIndex==10)||(fapIndex==11)||(fapIndex==52)||(fapIndex==57)||(fapIndex==58))
        {
            type = 1;
        }
        else if ((fapIndex==3)||(fapIndex==18)||(fapIndex==41)||(fapIndex==42))
        {
            type = 2;
        }
        else if ((fapIndex==6)||(fapIndex==7)||(fapIndex==53)||(fapIndex==54)||(fapIndex==61)||(fapIndex==62))
        {
            type = 3;
        }
        else if ((fapIndex==17)||(fapIndex==63))
        {
            type = 4;
        }
        else if (fapIndex==16)
        {
            type = 5;
        }
        else if ((fapIndex==12)||(fapIndex==13)||(fapIndex==59)||(fapIndex==60))
        {
            type = 6;
        }
        else if (fapIndex==1)
        {
            type = 7;
        }

        return type;
    }


   public float[][] getTargetFapsSequence(double fre) {
        float periode = 1.0f / (float)fre;

        double timeTemp = 0.0;
        float timeStep = 1.0f / (float)fre;
        this.outSequenceLength = 0;
        while(timeTemp <= targetTimes[0][startTargetLengthEnd - 1])
        {
            outSequenceLength++;
            timeTemp = timeTemp + timeStep;
        }
        //outSequenceLength = nb;
        float[][] targetFapsSequence = new float[64][outSequenceLength];

        TCBSpline interpolator = null;

        int sum = 0;
        for (int i = 0; i < vcLable.length; i++)
        {
             sum = sum + vcLable[i];
        }

        for (int type = 0; type < LipModel.NUM_LABIALS; type++) {
            //==//
             // loop iteration on type
             int[] fapIndexSet = getFapsIndexByLipType(type);
             double[] timePhonemeStream = new double[targetTimes[type].length];
             double[] targetPhonemeStream = new double[targetFaps[type].length];
             for (int p = 0; p < targetTimes[type].length; p++){
                  // set time stream on phoneme
                  timePhonemeStream[p] = targetTimes[type][p];
             }
             for (int i = 0; i < fapIndexSet.length; i++){
                  int fapIndex = fapIndexSet[i];
                  for (int p = 0; p < targetFaps[type].length; p++){
                       targetPhonemeStream[p] = targetFaps[fapIndex][p];
                  }

                  //==========================================================//
                  // find the first vowel
                  int pho = 0;
                  int ouputIndex = 0;
                  int[] targetSeg = new int[2];
                  targetSeg[0] = 0;
                  targetSeg[1] = -1;// index of last target in a unit
                  //==== if targetSeg[1] = 0; it means numSeg =  targetSeg[1] - targetSeg[0] = 0
                  //==== 0 can an index, for example, see function "for" beblow
                  float time = 0.0f;
                  double[] oriPosSegStr = null;
                  double[] oriTimSegStr = null;
                  while ((pho < vcLable.length) && (vcLable[pho] == 1)) {
                      targetSeg[1] = targetSeg[1] + vcLable[pho];
                      pho++;
                  }
                  if (pho < vcLable.length) {
                    targetSeg[1] = targetSeg[1] + vcLable[pho];
                  }

                  pho++;
                  // take into account the number (3) tragerts of the first vowel
                  oriPosSegStr = new double[targetSeg[1]-targetSeg[0]+1];
                  // ori position value
                  oriTimSegStr = new double[targetSeg[1]-targetSeg[0]+1];
                  // ori position time
                  for (int tarIndex = targetSeg[0]; tarIndex <= targetSeg[1]; tarIndex++)
                  {
                          oriPosSegStr[tarIndex-targetSeg[0]] = targetPhonemeStream[tarIndex];
                          oriTimSegStr[tarIndex-targetSeg[0]] = timePhonemeStream[tarIndex];
                  }
                  interpolator = new TCBSpline(oriPosSegStr, oriTimSegStr, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);

                  double[] timTem = interpolator.getTime();

                  double[] output = interpolator.getOutputStream(oriTimSegStr[0], (float) greta.core.util.Constants.FRAME_DURATION_SECONDS);
                  for (int f = 0; f<output.length; f++){
                          targetFapsSequence[fapIndex][ouputIndex] = (float)output[f];
                          time = time + timeStep;
                          ouputIndex++;
                  }
                  while(pho < vcLable.length){
                        targetSeg[0] = targetSeg[1]-2;
                        while ((pho < vcLable.length)&&(vcLable[pho] == 1)){
                            targetSeg[1] = targetSeg[1] + vcLable[pho];
                            pho++;
                        }
                        if (pho < vcLable.length)
                        {
                            targetSeg[1] = targetSeg[1] + vcLable[pho];
                            pho++;
                            oriPosSegStr = new double[targetSeg[1]-targetSeg[0]+1];
                            oriTimSegStr = new double[targetSeg[1]-targetSeg[0]+1];
                        }
                        else // last pho has been taken
                        {
                            oriPosSegStr = new double[targetSeg[1]-targetSeg[0]+1];
                            oriTimSegStr = new double[targetSeg[1]-targetSeg[0]+1];
                            // +1 presents the last target point in input stream
                            // which presents the additional last frame
                        }
                            // take into account the number (3) tragerts of the previous vowel
                            //oriPosSegStr = new double[targetSeg[1]-targetSeg[0]+1];
                            //oriTimSegStr = new double[targetSeg[1]-targetSeg[0]+1];
                        for (int tarIndex = targetSeg[0]; tarIndex <= targetSeg[1]; tarIndex++)
                            {
                                    oriPosSegStr[tarIndex-targetSeg[0]] = targetPhonemeStream[tarIndex];
                                    oriTimSegStr[tarIndex-targetSeg[0]] = timePhonemeStream[tarIndex];
                             }
                            //interpolator = new PolynomialFunctionLagrangeForm(oriTimSegStr, oriPosSegStr);
                             interpolator = new TCBSpline(oriPosSegStr, oriTimSegStr, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);

                             output = interpolator.getOutputStream(time, (float) greta.core.util.Constants.FRAME_DURATION_SECONDS);

                             for (int f = 0; f<output.length; f++){
                                    targetFapsSequence[fapIndex][ouputIndex] = (float)output[f];
                                    time = time + periode;
                                    ouputIndex++;
                            }
                  }

             }

        }
        return targetFapsSequence;
    }

    public int[] getFapsIndexByLipType(int type)
    {
        int[] fapIndex = null;
        switch (type){
            case 0:
                fapIndex = new int[6];
                fapIndex[0] = 4;
                fapIndex[1] = 8;
                fapIndex[2] = 9;
                fapIndex[3] = 51;
                fapIndex[4] = 55;
                fapIndex[5] = 56;
                break;
            case 1:
                fapIndex = new int[6];
                //Faps 5, 10, 11, 52, 57, 58
                fapIndex[0] = 5;
                fapIndex[1] = 10;
                fapIndex[2] = 11;
                fapIndex[3] = 52;
                fapIndex[4] = 57;
                fapIndex[5] = 58;
                break;
            case 2:
                //type = 2; JAW - Faps 3, 41, 42
                fapIndex = new int[3];
                fapIndex[0] = 3;
                fapIndex[1] = 41;
                fapIndex[2] = 42;
                break;
            case 3:
                // type = 3; LIP WIDTH - Faps 6, 7, 53, 54, 61, 62
                fapIndex = new int[6];
                fapIndex[0] = 6;
                fapIndex[1] = 7;
                fapIndex[2] = 53;
                fapIndex[3] = 54;
                fapIndex[4] = 61;
                fapIndex[5] = 62;
                break;
            case 4:
                // type = 4; UPPER LIP PROTRUSION - Faps Fap 17, 63
                fapIndex = new int[2];
                fapIndex[0] = 17;
                fapIndex[1] = 63;
                break;
            case 5:
                //type = 5; LOWER LIP PROTRUSION - Faps 16
                fapIndex = new int[1];
                fapIndex[0] = 16;
                break;
            case 6:
                // type = 6; CORNER LIP - Faps 12, 13, 59, 60
                fapIndex = new int[4];
                fapIndex[0] = 12;
                fapIndex[1] = 13;
                fapIndex[2] = 59;
                fapIndex[3] = 60;
                break;
            case 7:
                fapIndex = new int[1];
                fapIndex[0] = 1;
                break;
            default:
                break;
        }
        return fapIndex;

    }

    public void setindexMinAndMaxTime(double time) {

        for (int type = 0; type < LipModel.NUM_LABIALS; type++) {
            for (int i = startTargetLengthEnd - 1; i >= 1; i--) {
                if ((time >= targetTimes[type][i - 1]) && (time <= targetTimes[type][i])) {
                    indexMinTime[type] = i - 1;
                    indexMaxTime[type] = i;
                    break;
                }
            }
        }

    }

    public int getOutSequenceLength() {
        return outSequenceLength;
    }
}
