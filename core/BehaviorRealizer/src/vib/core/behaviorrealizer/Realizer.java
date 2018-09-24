/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.behaviorrealizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import vib.core.behaviorrealizer.keyframegenerator.FaceKeyframeGenerator;
import vib.core.behaviorrealizer.keyframegenerator.GazeKeyframeGenerator;
import vib.core.behaviorrealizer.keyframegenerator.GestureKeyframeGenerator;
import vib.core.behaviorrealizer.keyframegenerator.HeadKeyframeGenerator;
import vib.core.behaviorrealizer.keyframegenerator.KeyframeGenerator;
import vib.core.behaviorrealizer.keyframegenerator.LaughKeyframeGenerator;
import vib.core.behaviorrealizer.keyframegenerator.ShoulderKeyframeGenerator;
import vib.core.behaviorrealizer.keyframegenerator.SpeechKeyframeGenerator;
import vib.core.behaviorrealizer.keyframegenerator.TorsoKeyframeGenerator;
import vib.core.keyframes.AudioKeyFrame;
import vib.core.keyframes.Keyframe;
import vib.core.keyframes.KeyframeEmitter;
import vib.core.keyframes.KeyframePerformer;
import vib.core.keyframes.PhonemSequence;
import vib.core.repositories.SignalFiller;
import vib.core.signals.Signal;
import vib.core.signals.SignalPerformer;
import vib.core.signals.gesture.PointingSignal;
import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.IniManager;
import vib.core.util.Mode;
import vib.core.util.enums.CompositionType;
import vib.core.util.environment.Environment;
import vib.core.util.id.ID;
import vib.core.util.time.Temporizer;

/**
 *
 * @author Quoc Anh Le
 * @author Andre-Marie Pez
 *
 *
 * the following tags generate a warning in Javadoc generation because they are
 * UmlGraph tags, not javadoc tags.
 * @composed - - + vib.core.behaviorrealizer.keyframegenerator.KeyframeGenerator
 * @navassoc - - * vib.core.keyframes.Keyframe
 * @inavassoc - - * vib.core.signals.Signal
 */
public class Realizer extends CallbackSender implements SignalPerformer, KeyframeEmitter, CharacterDependent {

    // where send the resulted keyframes
    private List<KeyframePerformer> keyframePerformers;
    private List<KeyframeGenerator> generators;
    private GazeKeyframeGenerator gazeGenerator;
    private FaceKeyframeGenerator faceGenerator;
    private GestureKeyframeGenerator gestureGenerator;
    private Comparator<Keyframe> keyframeComparator;
    private Environment environment = new Environment(IniManager.getGlobals().getValueString("ENVIRONMENT"));;
    private double lastKeyFrameTime;
    private CharacterManager characterManager;

    public Realizer(CharacterManager cm) {
        setCharacterManager(cm);
        keyframePerformers = new ArrayList<KeyframePerformer>();
        generators = new ArrayList<KeyframeGenerator>();

        lastKeyFrameTime = vib.core.util.time.Timer.getTime();
        gestureGenerator = new GestureKeyframeGenerator();
        generators.add(gestureGenerator);
        generators.add(new SpeechKeyframeGenerator());
        generators.add(new HeadKeyframeGenerator());
        generators.add(new LaughKeyframeGenerator());

        //experimental
        generators.add(new ShoulderKeyframeGenerator());
        generators.add(new TorsoKeyframeGenerator());
        gazeGenerator = new GazeKeyframeGenerator(cm,generators);
        faceGenerator = new FaceKeyframeGenerator();

        keyframeComparator = new Comparator<Keyframe>() {
            @Override
            public int compare(Keyframe o1, Keyframe o2) {
                return (int) Math.signum(o1.getOffset() - o2.getOffset());
            }
        };
    }

    @Override //TODO add the use of modes: blend, replace, append
    public void performSignals(List<Signal> list, ID requestId, Mode mode) {

        // list of created keyframes
        List<Keyframe> keyframes = new ArrayList<Keyframe>();

        // Step 1: Schedule each signal independently from one to another.
        // The result of this step is to attribute abs value to possible sync points (compute absolute values from relative values).
        // The value of Start and End should be calculated in this step. So that we can sort


        for (Signal signal : list) {
            if(signal instanceof PointingSignal)
                gestureGenerator.fillPointing((PointingSignal)signal);
            else {
                SignalFiller.fillSignal(signal);
            }
        }
        Temporizer temporizer = new Temporizer();
        temporizer.add(list);
        temporizer.temporize();

        for (Signal signal : list) {
            for (KeyframeGenerator generator : generators) {
                if (generator.accept(signal)) {
                    break;
                }
            }
            gazeGenerator.accept(signal);
            faceGenerator.accept(signal);
        }


        // Step 2: Schedule signals that the computed signal is relative to the previous and the next signals
        // The result of this step is: (i) which phases are realized in each signal; (ii) when these phases are realized (abs time for each keyframe)
        // Step 3: create all key frames

        // Gaze keyframes for other modalities than eyes are generated before the others
        // and act as "shifts"
        gazeGenerator.generateBodyKeyframes(keyframes, environment);

        for (KeyframeGenerator generator : generators) {
            keyframes.addAll(generator.generateKeyframes());
        }
        Collections.sort(keyframes, keyframeComparator);

        // Gaze keyframes for the eyes are generated last
        gazeGenerator.generateEyesKeyframes(keyframes, environment);

        faceGenerator.findExistingAU(keyframes);
        keyframes.addAll(faceGenerator.generateKeyframes());


        // Step 4: adjust the timming of all key frame

        Collections.sort(keyframes, keyframeComparator);


        //  here:
        //      - we must manage the time for the three addition modes:
        //          - blend:    offset + now
        //          - replace:  offset + now
        //          - append:   offset + the previous last time
        //
        //      - finaly, update the "previous last time"
        //          - blend:   previousLastTime = max(previousLastTime, the last time of the new keyframes)
        //          - replace: previousLastTime = the last time of the new keyframes
        //          - append:  previousLastTime = the last time of the new keyframes


        double startTime = keyframes.isEmpty() ? 0 : keyframes.get(0).getOffset();
        // if the start time to start signals is less than 0, all signals' time have to be increased so that they start from 0
        double absoluteStartTime = vib.core.util.time.Timer.getTime();
        if(mode.getCompositionType() == CompositionType.append){
            absoluteStartTime = Math.max(lastKeyFrameTime, absoluteStartTime);
        }
        absoluteStartTime -= (startTime < 0 ? startTime : 0);
        if(mode.getCompositionType() != CompositionType.blend && !keyframes.isEmpty()){
            lastKeyFrameTime = 0;
        }
        //add this info to the keyframe - save this info in some special variable
        for (Keyframe keyframe : keyframes) {

            keyframe.setOnset(keyframe.getOnset() + absoluteStartTime);
            keyframe.setOffset(keyframe.getOffset() + absoluteStartTime);
            if (lastKeyFrameTime < keyframe.getOffset()) {
                lastKeyFrameTime = keyframe.getOffset();
            }
            if (keyframe instanceof AudioKeyFrame) {
                AudioKeyFrame audio = (AudioKeyFrame) keyframe;
                if (lastKeyFrameTime < audio.getOffset() + audio.getDuration()) {
                    lastKeyFrameTime = audio.getOffset() + audio.getDuration();
                }
            }
            if (keyframe instanceof PhonemSequence) {
                PhonemSequence phonems = (PhonemSequence) keyframe;
                if (lastKeyFrameTime < phonems.getOffset() + phonems.getDuration()) {
                    lastKeyFrameTime = phonems.getOffset() + phonems.getDuration();
                }
            }
        }

        this.sendKeyframes(keyframes, requestId, mode);
        // Add animation to callbacks
        if(mode.getCompositionType() == CompositionType.replace){
            this.stopAllAnims();
        }
        this.addAnimation(requestId, absoluteStartTime, lastKeyFrameTime);

    }

    @Override
    public void addKeyframePerformer(KeyframePerformer kp) {
        if (kp != null) {
            keyframePerformers.add(kp);
        }
    }

    @Override
    public void removeKeyframePerformer(KeyframePerformer kp) {
        keyframePerformers.remove(kp);
    }

    public void setEnvironment(Environment env) {
        this.environment = env;
        gazeGenerator.setEnvironment(env);
        gazeGenerator.addSignalPerformer(this);
        gestureGenerator.setEnvironment(env);
    }

    public void sendKeyframes(List<Keyframe> keyframes, ID id, Mode mode) {
        if (keyframes != null) {
            for (KeyframePerformer performer : keyframePerformers) {
                // TODO : Mode management in progress
                performer.performKeyframes(keyframes, id, mode);
            }
        }
    }

    public void setGestureModifier(GestureKeyframeGenerator.GestureModifier modifier){
        gestureGenerator.setGestureModifier(modifier);
    }

    @Override
    public void onCharacterChanged() {
        //is there something else to do ?
    }

    @Override
    public CharacterManager getCharacterManager() {
        return characterManager;
    }

    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        this.characterManager = characterManager;
    }
}
