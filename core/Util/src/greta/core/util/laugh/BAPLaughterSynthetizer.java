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
package greta.core.util.laugh;

import greta.core.util.audio.Audio;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class BAPLaughterSynthetizer implements LaughSynthetizer {

    private Laugh laugh;
    private Audio audio;
    private String laughterFile = "../bin/laugh/dataset/Woman1/Sd10.bap";
    private int frameNumber;

    @Override
    public void setLaugh(Laugh laugh) {
        this.laugh = laugh;
        audio = null;
    }

    @Override
    public void compute(boolean doTemporize, boolean doAudio, boolean doPhonemes) {
        double duration = laugh.getEnd().getValue() - laugh.getStart().getValue();
        double intensity = laugh.getIntensity();
        if (duration != 0.350 && duration != 1.2000) {

            //Sélection du rire en fonction d'une valeur aléatoire
            Random r = new Random();
            int randomLaugh = 1 + r.nextInt(14 - 1);

            if (duration < 4.000) {
                if (randomLaugh > 12) {
                    laughterFile = "../bin/laugh/dataset/Woman1/Sd5.bap";
                } else if (randomLaugh > 10) {
                    laughterFile = "../bin/laugh/dataset/Woman1/Sd20.bap";
                } else if (randomLaugh > 8) {
                    laughterFile = "../bin/laugh/dataset/Woman1/Sd2.bap";
                } else if (randomLaugh > 6) {
                    laughterFile = "../bin/laugh/dataset/Woman1/Sd4.bap";
                } else if (randomLaugh > 4) {
                    laughterFile = "../bin/laugh/dataset/Woman1/Sd21.bap";
                } else if (randomLaugh > 2) {
                    laughterFile = "../bin/laugh/dataset/Woman1/Sd3.bap";
                } else {
                    laughterFile = "../bin/laugh/dataset/Woman1/St19.bap";
                }
            } else {
                if (randomLaugh > 12) {
                    laughterFile = "../bin/laugh/dataset/Woman1/Sd6.bap";
                } else if (randomLaugh > 10) {
                    laughterFile = "../bin/laugh/dataset/Woman1/St43.bap";
                } else if (randomLaugh > 8) {
                    laughterFile = "../bin/laugh/dataset/Woman1/Sd12.bap";
                } else if (randomLaugh > 6) {
                    laughterFile = "../bin/laugh/dataset/Woman1/St17.bap";
                } else if (randomLaugh > 4) {
                    laughterFile = "../bin/laugh/dataset/Woman1/Sd21.bap";
                } else if (randomLaugh > 2) {
                    laughterFile = "../bin/laugh/dataset/Woman1/St15.bap";
                } else {
                    laughterFile = "../bin/laugh/dataset/Woman1/St19.bap";
                }
            }if (duration > 0.750 && duration < 1.000) {
                laughterFile = "../bin/laugh/dataset/Woman1/Sd5.bap";
            } else if (duration >= 1.000 && duration <3.000){
                laughterFile = "../bin/laugh/dataset/Woman1/Sd4.bap";
            } else {
                laughterFile = "../bin/laugh/dataset/Woman1/Sd3.bap";
            }
        }

    }

    @Override
    public List<LaughPhoneme> getPhonemes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

}
