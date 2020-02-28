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
package greta.core.animation.lipmodel;

import greta.core.animation.mpeg4.fap.FAP;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPFrameEmitter;
import greta.core.animation.mpeg4.fap.FAPFramePerformer;
import greta.core.animation.mpeg4.fap.FAPType;
import greta.core.keyframes.Keyframe;
import greta.core.keyframes.KeyframePerformer;
import greta.core.keyframes.PhonemSequence;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterDependentAdapterThread;
import greta.core.util.CharacterManager;
import greta.core.util.Constants;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.ID;
import greta.core.util.speech.Phoneme;
import greta.core.util.speech.Phoneme.PhonemeType;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yu Ding
 * @author Brice Donval
 */
public class LipModel extends CharacterDependentAdapterThread implements KeyframePerformer, FAPFrameEmitter, CharacterDependent {

    public static final int NUM_LABIALS = 8; //12 is useless for now
    public static final double STRONG = 4.5;
    public static final double MILD = 3.5;
    public static final double WEAK = 2.5;
    public static double WEIGHT_ULO = 8;
    public static double WEIGHT_LLO = 38;
    public static double WEIGHT_JAW = 11;
    public static double WEIGHT_LW = 15;
    public static double WEIGHT_ULP = 70;
    public static double WEIGHT_LLP = 70;
    public static double WEIGHT_CR = 1;
    public static float BaseTensionSup = 50;
    public static float BaseTensionInf = 70;

    private Lipdata lipdata;

    private final ArrayList<FAPFramePerformer> fapFramePerformers = new ArrayList<FAPFramePerformer>();

    private final SortedMap<Integer, ID> requestIds = new TreeMap<Integer, ID>();
    private final SortedMap<Integer, FAPFrame> requestFrames = new TreeMap<Integer, FAPFrame>();

    public LipModel(CharacterManager cm) {
        setCharacterManager(cm);
        onCharacterChanged();

        this.start();
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId) {
        // TODO : Mode management in progress
        performKeyframes(keyframes, requestId, new Mode(CompositionType.replace));
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId, Mode mode) {
        for (Keyframe keyFrame : keyframes) {
            if (keyFrame instanceof PhonemSequence) {
                List<Phoneme> phonemes = ((PhonemSequence) keyFrame).getPhonems();

                if (!phonemes.get(0).isPause()) {
                    phonemes.add(0, new Phoneme(PhonemeType.pause, 0));
                }

                if (!phonemes.get(phonemes.size() - 1).isPause()) {
                    phonemes.add(new Phoneme(PhonemeType.pause, Constants.FRAME_DURATION_SECONDS));
                }
                if (!phonemes.get(phonemes.size() - 2).isPause()) {
                    phonemes.add(new Phoneme(PhonemeType.pause,  Constants.FRAME_DURATION_SECONDS));
                }

                int FirstFrameNumber = (int) (keyFrame.getOffset() * Constants.FRAME_PER_SECOND);

                //---------------------------------------------------------------
                //------construire List visemes----------------------------------

                List<Viseme> visemes = new ArrayList<Viseme>();
                int length = phonemes.size();

                FindPrePhonemeAndSucPhoneme findPrePhonemeAndSucPhoneme = new FindPrePhonemeAndSucPhoneme(phonemes);
                //====  set List<Phoneme> phonemes = phonemes;
                //====  set length = phonemes.size();
                //====  set double timePositionBegin[] = begining time of each phoneme;
                double timePositionBegin[] = findPrePhonemeAndSucPhoneme.gettimePositionBegin();
                //==== return timePositionBegin;
                double sequencenDuration = timePositionBegin[length - 1] + phonemes.get(length - 1).getDuration();
                //==== get the time of sequence

                //------charge curPhoneme, prePhoneme, sucPhoneme, timePositionBegin
                for (int i = 0; i < length; i++) {

                    Phoneme curPhoneme = phonemes.get(i);
                    double visemeDuration = phonemes.get(i).getDuration();
                    Phoneme prePhoneme = findPrePhonemeAndSucPhoneme.getPrePhoneme(i);
                    //==== prePhoneme is the preceding vowel or pause or null (i=0)
                    Phoneme sucPhoneme = findPrePhonemeAndSucPhoneme.getSucPhoneme(i);
                    //==== sucPhoneme is the next vowel or pause or null(i=last)
                    Phoneme prePhysiquePho = findPrePhonemeAndSucPhoneme.getPrePhysiquePhoneme(i);
                    //==== prePhysiquePho is the prededing phoneme(vowel, consonant pause) or null(i=last)
                    Phoneme sucPhysiquePho = findPrePhonemeAndSucPhoneme.getSucPhysiquePhoneme(i);
                    //==== prePhysiquePho is the next phoneme(vowel, consonant pause) or null(i=last)
                    Viseme viseme = new Viseme(curPhoneme, prePhoneme, sucPhoneme, prePhysiquePho, sucPhysiquePho, i, length, timePositionBegin[i], visemeDuration, sequencenDuration);
                    //==== set information for each viseme
                    visemes.add(i, viseme);
                    //==== build viseme list
                }

                //------charge preTarget, sucTarget, curTime
                for (int i = 0; i < length; i++) {
                    CoartParameter coartpre = findCoartParameter(visemes.get(i).curPho, visemes.get(i).prePho);
                    //==== prePho is the preceding vowel or pause or null (i=0)
                    //==== 3 cases for prePho. It can be null, vowel or consonant
                    CoartParameter coartsuc = findCoartParameter(visemes.get(i).curPho, visemes.get(i).sucPho);
                    //====
                    visemes.get(i).setTimeAndTargetPreAndTargetSuc(visemes.get(i).curPho, i, length, coartpre, coartsuc);
                }

                // set preTime and sucTime
                for (int i = 0; i < length; i++) {
                    if (i == 0) {
                        visemes.get(i).setSucTime(visemes.get(i + 1).curTime);
                    } else if (i == (length - 1)) {
                        visemes.get(i).setPreTime(visemes.get(i - 1).curTime);
                    } else {
                        visemes.get(i).setSucTime(visemes.get(i + 1).curTime);
                        visemes.get(i).setPreTime(visemes.get(i - 1).curTime);
                    }

                }

                // coarticulation
                List<FAPPhoneme> fapPhonemes = new ArrayList<FAPPhoneme>();
                float preVisemeSucTarget[][] = new float[NUM_LABIALS][3];
                float sucVisemePreTarget[][] = new float[NUM_LABIALS][3];

                for (int i = 0; i < (length - 1); i++) {
                    // exclusion end frame pause
                    if (i == 0) {
                        sucVisemePreTarget = visemes.get(i + 1).getpreTarget();
                    } else {
                        preVisemeSucTarget = visemes.get(i - 1).getsucTarget();
                        sucVisemePreTarget = visemes.get(i + 1).getpreTarget();
                    }

                    visemes.get(i).coarticulation(i, length, preVisemeSucTarget, sucVisemePreTarget);
                    FAPPhoneme fapPhoneme = visemes.get(i).lipToFap();
                    fapPhonemes.add(i, fapPhoneme);
                }

                // pay attention: totalDuration without considering the last pause
                // Interpolation
                // double totalDuration = visemes.get(length-1).gettimePositionBegin() +  phonemes.get(length-1).getDuration();
                double totalDuration = visemes.get(length - 1).gettimePositionBegin();

                InterpolationLip interpolationLip = new InterpolationLip(fapPhonemes, totalDuration);

                float[][] targetFapsSequence = interpolationLip.getTargetFapsSequence(Constants.FRAME_PER_SECOND);
                int outLength = interpolationLip.getOutSequenceLength();
                // fin de Interpolation

                // building Faps files
                List<FAPFrame> fapFrames = new ArrayList<FAPFrame>(outLength);
                for (int i = 0; i < outLength; i++) {
                    FAPFrame f = new FAPFrame();
                    f.setFrameNumber(FirstFrameNumber + i);
                    // type = 0; UPPER LIP OPENING - 6Faps 4, 8, 9, 51, 55, 56

                    f.applyValue(4, (int) targetFapsSequence[4][i]);
                    f.applyValue(8, (int) targetFapsSequence[8][i]);
                    f.applyValue(9, (int) targetFapsSequence[9][i]);
                    f.applyValue(51, (int) targetFapsSequence[51][i]);
                    f.applyValue(55, (int) targetFapsSequence[55][i]);
                    f.applyValue(56, (int) targetFapsSequence[56][i]);

                    // type = 1; LOWER LIP OPENING - 6Faps 5, 10, 11, 52, 57, 58

                    f.applyValue(5, (int) targetFapsSequence[5][i]);
                    f.applyValue(10, (int) targetFapsSequence[10][i]);
                    f.applyValue(11, (int) targetFapsSequence[11][i]);
                    f.applyValue(52, (int) targetFapsSequence[52][i]);
                    f.applyValue(57, (int) targetFapsSequence[57][i]);
                    f.applyValue(58, (int) targetFapsSequence[58][i]);

                    // type = 2; JAW - 3Faps 3, 41, 42

                    f.applyValue(3, (int) (targetFapsSequence[3][i]));
                    f.applyValue(41, (int) (targetFapsSequence[41][i]));
                    f.applyValue(42, (int) (targetFapsSequence[42][i]));

                    // type = 3; LIP WIDTH - 6Faps 6, 7, 53, 54, 61, 62

                    f.applyValue(6, (int) targetFapsSequence[6][i]);
                    f.applyValue(7, (int) targetFapsSequence[7][i]);
                    f.applyValue(53, (int) targetFapsSequence[53][i]);
                    f.applyValue(54, (int) targetFapsSequence[54][i]);
                    f.applyValue(61, (int) targetFapsSequence[61][i]);
                    f.applyValue(62, (int) targetFapsSequence[62][i]);

                    // type = 4; UPPER LIP PROTRUSION - 2Faps 17, 63

                    f.applyValue(17, (int) targetFapsSequence[17][i]);
                    f.applyValue(63, (int) targetFapsSequence[63][i]);

                    // type = 5; LOWER LIP PROTRUSION - 1Faps 16

                    f.applyValue(16, (int) targetFapsSequence[16][i]);

                    // type = 6; CORNER LIP - 4Faps 12, 13, 59, 60

                    f.applyValue(12, (int) targetFapsSequence[12][i]);
                    f.applyValue(13, (int) targetFapsSequence[13][i]);
                    f.applyValue(59, (int) targetFapsSequence[59][i]);
                    f.applyValue(60, (int) targetFapsSequence[60][i]);

                    // type = 7; VISEME - 1Fap 1

                    f.applyValue(1, (int) targetFapsSequence[1][i]);

                    fapFrames.add(f);
                }

                // adding three empty FAP frames to enforce the closure of the mouth
                FAPFrame emptyFAPFrame = new FAPFrame(fapFrames.get(fapFrames.size() - 1).getFrameNumber() + 1);
                emptyFAPFrame.applyValue(FAPType.viseme, 0);
                fapFrames.add(emptyFAPFrame);

                emptyFAPFrame = new FAPFrame(fapFrames.get(fapFrames.size() - 1).getFrameNumber() + 1);
                emptyFAPFrame.applyValue(FAPType.viseme, 0);
                fapFrames.add(emptyFAPFrame);

                emptyFAPFrame = new FAPFrame(fapFrames.get(fapFrames.size() - 1).getFrameNumber() + 1);
                emptyFAPFrame.applyValue(FAPType.viseme, 0);
                fapFrames.add(emptyFAPFrame);

                // storage of frames
                updateFrames(fapFrames, requestId, mode);
            }
        }
    }

    @Override
    public void addFAPFramePerformer(FAPFramePerformer fapp) {
        fapFramePerformers.add(fapp);
    }

    @Override
    public void removeFAPFramePerformer(FAPFramePerformer fapp) {
        fapFramePerformers.remove(fapp);
    }

    @Override
    public void run() {

        int currentFrameNumber;
        int lastFAPFrameSent = 0;

        while (true) {

            synchronized (this) {

                currentFrameNumber = Timer.getCurrentFrameNumber() + 3;
                SortedMap<Integer, ID> currentRequestIds = requestIds.headMap(currentFrameNumber);
                for (Map.Entry<Integer, ID> entry : currentRequestIds.entrySet()) {
                    ID currentRequestId = requestIds.get(entry.getKey());
                    FAPFrame currentFAPFrame = requestFrames.remove(entry.getKey());
                    if(currentFAPFrame.getFrameNumber() > lastFAPFrameSent){
                        sendFrame(currentFAPFrame, currentRequestId);
                        lastFAPFrameSent = currentFAPFrame.getFrameNumber();
                    }
                }

                currentRequestIds.clear();
            }
            try {
                Thread.sleep(10); // in ms
            } catch (InterruptedException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void sendFrame(FAPFrame fapFrame, ID requestId) {
        //System.out.println("faps will be sent ");
        //int[] fapIndex = {1,4,5,6,7,8,9,10,11,12,13,51,52,53,54,55,56,57,58,59,60,16,17,3};
        for (FAPFramePerformer performer : fapFramePerformers) {
            performer.performFAPFrame(fapFrame, requestId);
        }
    }

    private void updateFrame(FAPFrame fapFrame, ID requestId) {

        synchronized (this) {

            int fapFrameNumber = fapFrame.getFrameNumber();

            requestIds.put(fapFrameNumber, requestId);
            requestFrames.put(fapFrameNumber, fapFrame);
        }
    }

    public void updateFrames(List<FAPFrame> fapFrames, ID requestId, Mode mode) {

        switch (mode.getCompositionType()) {

            case append: {
                appendFrames(fapFrames, requestId, mode);
                break;
            }

            case blend: {
                blendFrames(fapFrames, requestId, mode);
                break;
            }

            case replace: {
                replaceFrames(fapFrames, requestId, mode);
                break;
            }

            default: {
                System.out.println("[LipModel] updateFrames() : mode is wrong");
                break;
            }
        }
    }

    public void appendFrames(List<FAPFrame> fapFrames, ID requestId, Mode mode) {
        for (FAPFrame fapFrame : fapFrames) {
            updateFrame(fapFrame, requestId);
        }
    }

    public void blendFrames(List<FAPFrame> fapFrames, ID requestId, Mode mode) {
        for (FAPFrame fapFrame : fapFrames) {
            updateFrame(fapFrame, requestId);
        }
    }

    public void replaceFrames(List<FAPFrame> fapFrames, ID requestId, Mode mode) {

        synchronized (this) {

            FAPFrame lastNewFAPFrame = fapFrames.get(fapFrames.size() - 1);
            int lastNewFAPFrameNumber = lastNewFAPFrame.getFrameNumber();
            int fadeDurationInFrame = Math.min(10, fapFrames.size());

            int i = 1;
            for (FAPFrame fapFrame : fapFrames) {
                if (i < fadeDurationInFrame) {
                    double weight = (double) i / (double) fadeDurationInFrame;
                    blend(fapFrame, weight);
                }
                ++i;
                updateFrame(fapFrame, requestId);
            }

            SortedMap<Integer, ID> requestIdsAfterThisOne = requestIds.tailMap(lastNewFAPFrameNumber + 1);
            SortedMap<Integer, FAPFrame> requestFramesAfterTheseOnes = requestFrames.tailMap(lastNewFAPFrameNumber + 1);

            requestIdsAfterThisOne.clear();
            requestFramesAfterTheseOnes.clear();
        }
    }

    public void blend(FAPFrame fromFAPFrame, FAPFrame toFAPFrame, double weight) {
        if (fromFAPFrame != null && toFAPFrame != null) {
            for (int i = 0; i < toFAPFrame.getAnimationParametersList().size(); ++i) {
                FAP fromFAP = fromFAPFrame.getAnimationParametersList().get(i);
                FAP toFAP = toFAPFrame.getAnimationParametersList().get(i);
                if (fromFAP.getMask() || toFAP.getMask()) {
                    toFAP.applyValue((int) (toFAP.getValue() * weight + fromFAP.getValue() * (1 - weight)));
                }
            }
        }
    }

    public void blend(FAPFrame toFAPFrame, double weight) {
        if (toFAPFrame != null) {
            FAPFrame fromFAPFrame = requestFrames.get(toFAPFrame.getFrameNumber());
            if (fromFAPFrame != null) {
                for (int i = 0; i < toFAPFrame.getAnimationParametersList().size(); ++i) {
                    FAP fromFAP = fromFAPFrame.getAnimationParametersList().get(i);
                    FAP toFAP = toFAPFrame.getAnimationParametersList().get(i);
                    if (fromFAP.getMask() || toFAP.getMask()) {
                        toFAP.applyValue((int) (toFAP.getValue() * weight + fromFAP.getValue() * (1 - weight)));
                    }
                }
            }
        }
    }

    private CoartParameter findCoartParameter(Phoneme refPhonem, Phoneme targetPhonem) {
        if (targetPhonem == null) {
            return null;
        }

        if (targetPhonem.isVowel()) {
            return lipdata.datas.get(refPhonem.getPhonemeType()).get(getBaseVowel(targetPhonem.getPhonemeType()));
            //==== getBaseVowel seen in lines below
        }
        return lipdata.datas.get(refPhonem.getPhonemeType()).get(targetPhonem.getPhonemeType());
    }

    private PhonemeType getBaseVowel(PhonemeType vowel) {
        if (vowel == PhonemeType.a1 || vowel == PhonemeType.a) {
            return PhonemeType.a;
        }
        if (vowel == PhonemeType.e1 || vowel == PhonemeType.e || vowel == PhonemeType.E1) {
            return PhonemeType.e;
        }
        if (vowel == PhonemeType.i1 || vowel == PhonemeType.i) {
            return PhonemeType.i;
        }
        if (vowel == PhonemeType.o1 || vowel == PhonemeType.o || vowel == PhonemeType.O1) {
            return PhonemeType.o;
        }
        if (vowel == PhonemeType.u1 || vowel == PhonemeType.u) {
            return PhonemeType.u;
        }
        if (vowel == PhonemeType.y) {
            return PhonemeType.y;
        }
        return null;
    }

    @Override
    public void onCharacterChanged() {
        if (lipdata == null || !getCharacterManager().getValueString("LIP_DATA").equals(lipdata.fileName)) {
            lipdata = new Lipdata(getCharacterManager().getValueString("LIP_DATA"));
        }
        WEIGHT_ULO = getCharacterManager().getValueDouble("WEIGHT_ULO");
        WEIGHT_LLO = getCharacterManager().getValueDouble("WEIGHT_LLO");
        WEIGHT_JAW = getCharacterManager().getValueDouble("WEIGHT_JAW");
        WEIGHT_LW = getCharacterManager().getValueDouble("WEIGHT_LW");
        WEIGHT_ULP = getCharacterManager().getValueDouble("WEIGHT_ULP");
        WEIGHT_LLP = getCharacterManager().getValueDouble("WEIGHT_LLP");
        WEIGHT_CR = getCharacterManager().getValueDouble("WEIGHT_CR");
        BaseTensionSup = (float) getCharacterManager().getValueDouble("BaseTensionSup");
        BaseTensionInf = (float) getCharacterManager().getValueDouble("BaseTensionInf");
    }
}
