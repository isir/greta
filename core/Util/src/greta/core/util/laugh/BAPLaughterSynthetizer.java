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
