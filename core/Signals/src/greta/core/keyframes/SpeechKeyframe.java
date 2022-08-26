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
package greta.core.keyframes;

/**
 *
 * @author Quoc Anh Le
 */
public class SpeechKeyframe   extends ParametersKeyframe{
    String wavFilePath;

    public SpeechKeyframe(double time, String wavFilePath){
        modality = "speech";
        this.onset = time;
        this.wavFilePath = wavFilePath;
    }

    public String getFileName(){
        return this.wavFilePath;
    }

    public double getOffset() {
        return this.offset;
    }

    public double getOnset() {
        return this.onset;
    }

    public String getModality() {
        return this.modality;
    }

    public String getId() {
        return this.id;
    }

    public String getPhaseType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getCategory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getSPC() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getTMP() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getPWR() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getFLD() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getSTF() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
