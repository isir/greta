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
package greta.core.behaviorrealizer;

import greta.core.behaviorrealizer.keyframegenerator.FaceKeyframeGenerator;
import greta.core.behaviorrealizer.keyframegenerator.GazeKeyframeGenerator;
import greta.core.behaviorrealizer.keyframegenerator.GestureKeyframeGenerator;
import greta.core.behaviorrealizer.keyframegenerator.HeadKeyframeGenerator;
import greta.core.behaviorrealizer.keyframegenerator.KeyframeGenerator;
import greta.core.behaviorrealizer.keyframegenerator.LaughKeyframeGenerator;
import greta.core.behaviorrealizer.keyframegenerator.ShoulderKeyframeGenerator;
import greta.core.behaviorrealizer.keyframegenerator.SpeechKeyframeGenerator;
import greta.core.behaviorrealizer.keyframegenerator.TorsoKeyframeGenerator;
import greta.core.keyframes.AudioKeyFrame;
import greta.core.keyframes.CancelableKeyframePerformer;
import greta.core.keyframes.Keyframe;
import greta.core.keyframes.KeyframeEmitter;
import greta.core.keyframes.KeyframePerformer;
import greta.core.keyframes.PhonemSequence;
import greta.core.repositories.FaceLibrary;
import greta.core.repositories.Gestuary;
import greta.core.repositories.HeadLibrary;
import greta.core.repositories.SignalFiller;
import greta.core.repositories.TorsoLibrary;
import greta.core.signals.CancelableSignalPerformer;
import greta.core.signals.Signal;
import greta.core.signals.gesture.PointingSignal;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.environment.Environment;
import greta.core.util.id.ID;
import greta.core.util.time.Temporizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Quoc Anh Le
 * @author Andre-Marie Pez
 *
 *
 * the following tags generate a warning in Javadoc generation because they are
 * UmlGraph tags, not javadoc tags.
 * @composed - - + greta.core.behaviorrealizer.keyframegenerator.KeyframeGenerator
 * @navassoc - - * greta.core.keyframes.Keyframe
 * @inavassoc - - * greta.core.signals.Signal
 */
public class Realizer extends CallbackSender implements CancelableSignalPerformer, KeyframeEmitter, CharacterDependent {
    // where send the resulted keyframes
    private List<KeyframePerformer> keyframePerformers;
    private List<KeyframeGenerator> generators;
    
    private GestureKeyframeGenerator gestureGenerator;
    private SpeechKeyframeGenerator speechGenerator;
    private HeadKeyframeGenerator headGenerator;
    private LaughKeyframeGenerator laughGenerator;
    private ShoulderKeyframeGenerator shoulderGenerator;
    private TorsoKeyframeGenerator torsoGenerator;
    private GazeKeyframeGenerator gazeGenerator;
    private FaceKeyframeGenerator faceGenerator;

    private Comparator<Keyframe> keyframeComparator;
    private Environment environment;  //new Environment(IniManager.getGlobals().getValueString("ENVIRONMENT"));
    private double lastKeyFrameTime;
    public CharacterManager characterManager;

    public Realizer(CharacterManager cm) {
        
        setCharacterManager(cm);
        
        keyframePerformers = new ArrayList<>();
        lastKeyFrameTime = greta.core.util.time.Timer.getTime();
        keyframeComparator = (o1, o2) -> (int) Math.signum(o1.getOffset() - o2.getOffset());
        environment = characterManager.getEnvironment();
        
        gestureGenerator = new GestureKeyframeGenerator();
        speechGenerator = new SpeechKeyframeGenerator();
        headGenerator = new HeadKeyframeGenerator();
        laughGenerator = new LaughKeyframeGenerator();
        shoulderGenerator = new ShoulderKeyframeGenerator();
        torsoGenerator = new TorsoKeyframeGenerator();
        faceGenerator = new FaceKeyframeGenerator();

        generators = new ArrayList<>();
        generators.add(gestureGenerator);
        generators.add(speechGenerator);
        generators.add(headGenerator);
        generators.add(laughGenerator);
        generators.add(shoulderGenerator);
        generators.add(torsoGenerator);
        generators.add(faceGenerator);

        gazeGenerator = new GazeKeyframeGenerator(cm,generators);
        generators.add(gazeGenerator);
        
    }

    @Override //TODO add the use of modes: blend, replace, append
    public void performSignals(List<Signal> list, ID requestId, Mode mode) {
        
        // list of created keyframes
        List<Keyframe> keyframes = new ArrayList<>();

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
                generator.accept(signal);
            }
        }
        
        ///////////////////////////////////////////////////////////////////////////////////////////////////
        // Step 2: create all key frames
        // Gaze keyframes for other modalities than eyes are generated before the others
        // and act as "shifts"
        ///////////////////////////////////////////////////////////////////////////////////////////////////
        
        for (KeyframeGenerator generator : generators) {
            System.out.println("greta.core.behaviorrealizer.Realizer.performSignals(): add all keyframes: " + generator.toString());
            keyframes.addAll(generator.generateKeyframes());
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        // Step 3: Schedule signals that the computed signal is relative to the previous and the next signals
        // The result of this step is: (i) which phases are realized in each signal; (ii) when these phases are realized (abs time for each keyframe)        
        ///////////////////////////////////////////////////////////////////////////////////////////////////

        keyframes.sort(keyframeComparator);
        
//        System.out.println("keyframe's modality after Generator.generateKeyframes()");
//        for(Keyframe k:keyframes){
//            System.out.println("greta.core.behaviorrealizer.Realizer.performSignals(): " + k.getModality());
//        }
        
        ///////////////////////////////////////////////////////////////////////////////////////////////////
        // Step 4: ADJUST THE TIMING OF THE KEYFRAMES
        ///////////////////////////////////////////////////////////////////////////////////////////////////        
        
        //  here:
        //      - we must manage the time for the three addition modes:
        //          - blend:    offset + now
        //          - replace:  offset + now
        //          - append:   offset + the previous last time
        //
        //      - finally, update the "previous last time"
        //          - blend:   previousLastTime = max(previousLastTime, the last time of the new keyframes)
        //          - replace: previousLastTime = the last time of the new keyframes
        //          - append:  previousLastTime = the last time of the new keyframes
       
        double startTime = keyframes.isEmpty() ? 0 : keyframes.get(0).getOffset();
        //System.out.println("G0:"+startTime);
        // if the start time to start signals is less than 0, all signals' time have to be increased so that they start from 0
        double absoluteStartTime = greta.core.util.time.Timer.getTime();
        if (mode.getCompositionType() == CompositionType.append){
            absoluteStartTime = Math.max(lastKeyFrameTime, absoluteStartTime);
        }
        absoluteStartTime -= (startTime < 0 ? startTime : 0);
        if (mode.getCompositionType() != CompositionType.blend && !keyframes.isEmpty()) {
            lastKeyFrameTime = 0;
        }
        //add this info to the keyframe - save this info in some special variable
        for (Keyframe keyframe : keyframes) {
            //System.out.println("G1:"+keyframe.getOnset() + absoluteStartTime);
            //System.out.println("G2:"+keyframe.getOffset() + absoluteStartTime);
            keyframe.setOnset(keyframe.getOnset() + absoluteStartTime);
            keyframe.setOffset(keyframe.getOffset() + absoluteStartTime);
            if (lastKeyFrameTime < keyframe.getOffset()) {
                 //System.out.println("G10: "+keyframe.getOffset());
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

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        // Step 5: SEND KEYFRAMES AND MAKE IT REAL!
        ///////////////////////////////////////////////////////////////////////////////////////////////////        
        
        this.sendKeyframes(keyframes, requestId, mode);
        
        // Add animation to callbacks
        //System.out.println("GGGGGGGGGG:"+mode.getCompositionType());
        if (mode.getCompositionType() == CompositionType.replace) {
            this.stopAllAnims();
        }
        //System.out.println("GGGGGGGGGG:"+requestId+"  "+absoluteStartTime+"   "+lastKeyFrameTime);
        this.addAnimation(requestId, absoluteStartTime, lastKeyFrameTime);
         
    }

    @Override
    public void cancelSignalsById(ID requestId) {
        for (KeyframePerformer performer : keyframePerformers) {
            if (performer instanceof CancelableKeyframePerformer) {
                ((CancelableKeyframePerformer) performer).cancelKeyframesById(requestId);
            }
        }
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
        //gazeGenerator.setEnvironment(env);
        gazeGenerator.addSignalPerformer(this);
        gestureGenerator.setEnvironment(env);
    }

    public void sendKeyframes(List<Keyframe> keyframes, ID id, Mode mode) {

        if (keyframes != null) {
            for (KeyframePerformer performer : keyframePerformers) {
                // TODO : Mode management in progress
                performer.performKeyframes(keyframes, id, mode);
            }
            for (Keyframe kf : keyframes) {
                System.out.println("greta.auxiliary.incrementality.ChunkSenderThread.sendKeyframes(): modality - category (onset - offset): " + kf.getModality() + " - " + kf.getCategory() + "(" + kf.getOnset() + " - " + kf.getOffset() + ")");
            }
        }
    }

    public void setGestureModifier(GestureKeyframeGenerator.GestureModifier modifier) {
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
        characterManager.add(this);
    }

    public void UpdateFaceLibrary(){
        this.getCharacterManager().remove(FaceLibrary.global_facelibrary);
        FaceLibrary.global_facelibrary = new FaceLibrary(this.getCharacterManager());
        //get the default Lexicon :
        FaceLibrary.global_facelibrary.setDefaultDefinition(this.getCharacterManager().getDefaultValueString("FACELIBRARY"));
        //load additionnal Lexicon :
        for (String fileName : this.getCharacterManager().getAllValuesString("FACELIBRARY")) {
            FaceLibrary.global_facelibrary.addDefinition(fileName);
        }
        //set the current Lexicon to use :
        FaceLibrary.global_facelibrary.setDefinition(this.getCharacterManager().getValueString("FACELIBRARY"));
    }

    public void UpdateGestureLibrary(){
        this.getCharacterManager().remove(Gestuary.global_gestuary);
        Gestuary.global_gestuary = new Gestuary(this.getCharacterManager());
        Gestuary.global_gestuary.setCharacterManager(this.getCharacterManager());
        //get the default Lexicon :
        Gestuary.global_gestuary.setDefaultDefinition(getCharacterManager().getDefaultValueString("GESTUARY"));
        //set the current Lexicon to use :
        Gestuary.global_gestuary.setDefinition(getCharacterManager().getValueString("GESTUARY"));
    }

    public void UpdateHeadLibrary() {
        this.getCharacterManager().remove(HeadLibrary.globalLibrary);
        HeadLibrary.globalLibrary = new HeadLibrary(this.getCharacterManager());
        HeadLibrary.globalLibrary.setCharacterManager(this.getCharacterManager());
        HeadLibrary.globalLibrary.setDefaultDefinition(getCharacterManager().getValueString("HEADGESTURES"));
        HeadLibrary.globalLibrary.setDefinition(getCharacterManager().getValueString("HEADGESTURES"));
        // intervals = new HeadIntervals();
    }

    public void UpdateTorsoLibrary() {
        this.getCharacterManager().remove(TorsoLibrary.globalLibrary);
        TorsoLibrary.globalLibrary = new TorsoLibrary(this.getCharacterManager());
        TorsoLibrary.globalLibrary.setCharacterManager(this.getCharacterManager());
        TorsoLibrary.globalLibrary.setDefaultDefinition(getCharacterManager().getDefaultValueString("TORSOGESTURES"));
        TorsoLibrary.globalLibrary.setDefinition(getCharacterManager().getValueString("TORSOGESTURES"));
    }

    public void UpdateShoulderLibrary() {
    }

    public void UpdateHandLibrary(){
    }
}
