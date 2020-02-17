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

/**
 *
 * @author Andre-Marie Pez
 * @author Yu Ding
 */
public class LaughPhoneme {

    private LaughPhonemeType type;
    private double duration;
    private double[] energy;
    private double[] pitch;
    private double[] phoIntensityByFrame;

    public double[] getPhoIntensityByFrame() {
        return phoIntensityByFrame;
    }

    public void setPhoIntensityByFrame(double[] phoIntensityByFrame) {
        this.phoIntensityByFrame = phoIntensityByFrame;
    }
    private double intensity = 1;

    public LaughPhoneme(LaughPhonemeType type, double duration){
        this.type = type;
        this.duration = duration;
    }

    public LaughPhonemeType getPhonemeType(){
        return type;
    }

    public double getDuration(){
        return duration;
    }

    public void setEnergy(double... energy) {
        this.energy = energy;
    }

    public void setPitch(double... pitch) {
        this.pitch = pitch;
    }

    public double[] getEnergy() {
        return energy;
    }

    public double[] getPitch() {
        return pitch;
    }


    public void setInensity(double intensity) {
        this.intensity = intensity;
    }

    public double getInensity() {
        return intensity;
    }
    /**
     * Enumeration of all laugh phoneme types used by Greta.
     */

    public static enum LaughPhonemeType{
                sil,
                ne,
                click,
                nasal,
                plosive,
                fricative,
                ic,
                e,
                o,
                grunt,
                cackle,
                a,
                glotstop,
                vowel
    }
}
