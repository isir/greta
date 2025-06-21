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
