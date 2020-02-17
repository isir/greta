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
package greta.core.util.speech;

/**
 * This class contains informations about phonemes.
 * @author Andre-Marie Pez
 */
public class Phoneme {
    private PhonemeType type;
    private double duration;

    /**
     * Contructor.
     * @param type the phoneme type
     * @param duration the duration of this phoneme
     */
    public Phoneme(PhonemeType type, double duration){
        this.type = type;
        this.duration = duration;
    }

    /**
     * Returns the type of this {@code Phoneme}.
     * @return the type of this {@code Phoneme}
     */
    public PhonemeType getPhonemeType(){
        return type;
    }

    /**
     * Returns the duration of this {@code Phoneme}.
     * @return the duration of this {@code Phoneme}
     */
    public double getDuration(){
        return duration;
    }

    /**
     * Returns {@code true} if the type of this {@code Phoneme} correspond to a vowel, {@code false} otherwise.
     * @return {@code true} if this {@code Phoneme} correspond to a vowel
     */
    public boolean isVowel(){
        return type == PhonemeType.a ||
               type == PhonemeType.e ||
               type == PhonemeType.i ||
               type == PhonemeType.o ||
               type == PhonemeType.u ||
               type == PhonemeType.y ||
               type == PhonemeType.a1 ||
               type == PhonemeType.e1 ||
               type == PhonemeType.i1 ||
               type == PhonemeType.o1 ||
               type == PhonemeType.u1 ||
               type == PhonemeType.E1 ||
               type == PhonemeType.O1;
    }

    /**
     * Returns {@code true} if the type of this {@code Phoneme} correspond to a bilabial consonant, {@code false} otherwise.<br/>
     * A bilabial consonant is a consonant articulated with both lips.
     * @return {@code true} if this {@code Phoneme} correspond to a bilabial consonant
     */
    public boolean isBilabial(){
        return type == PhonemeType.b ||  type == PhonemeType.m || type == PhonemeType.p;
    }

    /**
     * Returns {@code true} if the type of this {@code Phoneme} correspond to a labiodental consonant, {@code false} otherwise.<br/>
     * A labiodental consonant is a consonant articulated with the lower lip and the upper teeth.
     * @return {@code true} if this {@code Phoneme} labiodental to a bilabial consonant
     */
    public boolean isLabiodental(){
        return type == PhonemeType.f ||  type == PhonemeType.v;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Phoneme) {
            return ((Phoneme)o).type == this.type;
        }
        if(o instanceof PhonemeType) {
            return ((PhonemeType)o) == this.type;
        }
        return false;
    }

    public boolean isPause(){
        return type == PhonemeType.pause;
    }

    /**
     * Enumeration of all phoneme types used by Greta.
     */
    public static enum PhonemeType{ pause,
                                    a1,
                                    a,
                                    e1,
                                    e,
                                    E1,
                                    i1,
                                    i,
                                    o1,
                                    o,
                                    O1,
                                    u1,
                                    u,
                                    y,
                                    b,
                                    c,
                                    d, f, g, h, k, l, m, n, p, q, r, s,
                                t, v, w, z, SS, tS, th };
                                //? c h q
}
