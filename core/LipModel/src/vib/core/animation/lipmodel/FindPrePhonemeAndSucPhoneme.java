/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.lipmodel;

import vib.core.util.speech.Phoneme;
import java.util.List;

/**
 *
 * @author Yu Ding
 */
public class FindPrePhonemeAndSucPhoneme {

    List<Phoneme> phonemes;
    int length;
    double timePositionBegin[];

    FindPrePhonemeAndSucPhoneme(List<Phoneme> phonemes) {
        this.phonemes = phonemes;
        this.length = phonemes.size();
        this.timePositionBegin = new double[length];

        this.timePositionBegin[0] = 0;
        for (int i = 1; i < length; i++) {
            timePositionBegin[i] = timePositionBegin[i - 1] + phonemes.get(i - 1).getDuration();
        }
    }

    public double[] gettimePositionBegin() {
        return timePositionBegin;
    }

    public Phoneme getPrePhysiquePhoneme(int curindex) {

        Phoneme prePhoneme = null;
        int jpre = curindex - 1;

        if (jpre >= 0) {
            prePhoneme = phonemes.get(jpre);
        }

        return prePhoneme;
    }

    public Phoneme getSucPhysiquePhoneme(int curindex) {
        Phoneme sucPhoneme = null;
        int jsuc = curindex + 1;
        if (jsuc < length) {
            sucPhoneme = phonemes.get(jsuc);
        }
        return sucPhoneme;
    }

    public Phoneme getPrePhoneme(int curindex) {

        Phoneme prePhoneme = null;
        int jpre = curindex - 1;
        Phoneme curPhoneme = phonemes.get(curindex);
        // prePhoneme
        if (curPhoneme.isVowel()) {
            while ((jpre >= 0) && phonemes.get(jpre).isVowel() && (!(phonemes.get(jpre).isPause()))) {
                jpre = jpre - 1;
            }
            if (jpre >= 0) {
                prePhoneme = phonemes.get(jpre);
            }
        } else {
            while (jpre >= 0 && (!(phonemes.get(jpre).isVowel())) && (!(phonemes.get(jpre).isPause()))) {
                jpre = jpre - 1;
            }
            if (jpre >= 0) {
                prePhoneme = phonemes.get(jpre);
            }
        }

        return prePhoneme;

    }

    public Phoneme getSucPhoneme(int curindex) {
        //(Phoneme curPhoneme, List<Phoneme> ,int curindex){
        Phoneme sucPhoneme = null;
        int jsuc = curindex + 1;
        Phoneme curPhoneme = phonemes.get(curindex);
        // prePhoneme
        if (curPhoneme.isVowel()) {
            while (jsuc <= (length - 1) && phonemes.get(jsuc).isVowel() && (!(phonemes.get(jsuc).isPause()))) {
                jsuc = jsuc + 1;
            }
            if (jsuc <= (length - 1)) {
                sucPhoneme = phonemes.get(jsuc);
            }
        } else {
            while (jsuc <= (length - 1) && (!(phonemes.get(jsuc).isVowel())) && (!(phonemes.get(jsuc).isPause()))) {
                jsuc = jsuc + 1;
            }
            if (jsuc <= (length - 1)) {
                sucPhoneme = phonemes.get(jsuc);
            }
        }

        return sucPhoneme;
    }
}
