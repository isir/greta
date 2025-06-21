/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.core.animation.lipmodel;

import greta.core.util.speech.Phoneme;

/**
 *
 * @author Yu Ding
 */
public class FAPPhoneme {

    Phoneme pho;
    float time[][] = new float[3][64];
    float target[][] = new float[3][64];

    FAPPhoneme() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 64; j++) {
                time[i][j] = 0;
                target[i][j] = 0;
            }
        }
    }

    //=== test ====//
    public void printKeyFrame()
    {
        int keyTargetNum = (pho.isVowel() ? 3 : 1);
        int[] fapIndex = {4,5,6,7,8,9,10,11,12,13,51,52,53,54,55,56,57,58,59,60,16,17,3};

        for (int fm = 0; fm<keyTargetNum; fm++){
             for (int fp = 0; fp<23; fp++){
                 //System.out.print("  fp="+fp+"  "+ "fapIndex[fp]"+fapIndex[fp]);
                 System.out.print("  "+target[fm][fapIndex[fp]]);
             }
             System.out.println(" ");
        }
    }

    public void printKeyTime()
    {
        int keyTargetNum = (pho.isVowel() ? 3 : 1);
        int[] fapIndex = {4,5,6,7,8,9,10,11,12,13,51,52,53,54,55,56,57,58,59,60,16,17,3};

        //System.out.println(" time as follows:");
        for (int fm = 0; fm<keyTargetNum; fm++){
            for (int fp = 0; fp<23; fp++){
                 int type = getTypeNumber(fapIndex[fp]);
                 System.out.print("  "+time[fm][type]);
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


        return type;
    }
    //=============//

}
