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
package greta.core.intentions;

import greta.core.util.time.TimeMarker;

/**
 * This {@code BasicIntention} contains informations about an emotion.
 * @author Andre-Marie Pez
 */
public class EmotionIntention extends BasicIntention implements IntensifiableIntention {

    /**
     * Constant to define a fake emotion.
     */
    public static final int FAKE      = 0;
    /**
     * Constant to define a felt emotion.
     */
    public static final int FELT      = 1;
    /**
     * Constant to define an inhibited emotion.
     */
    public static final int INHIBITED = 2;

    private static String [] stringOfRegulation = {"fake","felt","inhibited"};

    private static int warpRegulation(int regulation){
        return regulation<FAKE || regulation>INHIBITED ? FELT : regulation;
    }

    /**
     * Translates a {@code String} to a regulation value.
     * @param regulation the String to translate
     * @return the regulation value
     * @see #FAKE
     * @see #FELT
     * @see #INHIBITED
     */
    public static int stringToRegulation(String regulation){
        if("fake".equalsIgnoreCase(regulation)) {
            return FAKE;
        }
        if("felt".equalsIgnoreCase(regulation)) {
            return FELT;
        }
        if("inhibited".equalsIgnoreCase(regulation)) {
            return INHIBITED;
        }
        return FELT;
    }

    /**
     * Translates a regulation value to a {@code String}.<br>
     * "fake" for FAKE<br>
     * "felt" for FELT<br>
     * "inhibited" for INHIBITED
     * @param regulation
     * @return the regulation as a String
     * @see #FAKE
     * @see #FELT
     * @see #INHIBITED
     */
    public static String regulationToString(int regulation){
        return stringOfRegulation[warpRegulation(regulation)];
    }

    private int regulation;
    private double intensity;

    public EmotionIntention(String id, String type, TimeMarker start, TimeMarker end, double importance, int regulation, double intensity){
        super("emotion", id, type, start, end, importance);
        this.regulation = warpRegulation(regulation);
        this.intensity = intensity;
    }

    public EmotionIntention(String id, String type, TimeMarker start, TimeMarker end, int regulation, double intensity){
        this(id, type, start, end, 0.5, regulation, intensity);
    }
    /**
     * Returns the regulation of this emotion.
     * @return the regulation of this emotion
     * @see #FAKE
     * @see #FELT
     * @see #INHIBITED
     */
    public int getRegulation(){
        return regulation;
    }

    /**
     * Returns the intensity of this emotion.
     * @return the intensity of this emotion
     */
    public double getIntensity(){
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }
}
