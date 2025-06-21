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
