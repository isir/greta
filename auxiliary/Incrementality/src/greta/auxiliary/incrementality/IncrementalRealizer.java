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

 /*--------------------------------------------------------------------*/
 /*---     MODIFIED REALIZER FOR INCREMENTALITY IMPLEMENTATION      ---*/
 /*---        USAGE: Planner -> SignalScheduler -> This             ---*/
 /*--------------------------------------------------------------------*/
package greta.auxiliary.incrementality;

import greta.core.behaviorrealizer.CallbackSender;
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
 * @composed - - +
 * greta.core.behaviorrealizer.keyframegenerator.KeyframeGenerator
 * @navassoc - - * greta.core.keyframes.Keyframe
 * @inavassoc - - * greta.core.signals.Signal
 */
public class IncrementalRealizer extends CallbackSender implements CancelableSignalPerformer, KeyframeEmitter, CharacterDependent, IncrementalityFeedbackEmitter {

    // where send the resulted keyframes
    private List<KeyframePerformer> keyframePerformers;
    private List<KeyframeGenerator> generators;
    private GazeKeyframeGenerator gazeGenerator;
    private FaceKeyframeGenerator faceGenerator;
    private GestureKeyframeGenerator gestureGenerator;
    private Comparator<Keyframe> keyframeComparator;
    private Environment environment;  //new Environment(IniManager.getGlobals().getValueString("ENVIRONMENT"));
    private double lastKeyFrameTime;
    private CharacterManager characterManager;

    private ID currentID;

    private List<IncrementalityFeedbackPerformer> incFeedbackPerformers;

    private List<Signal> previousSignalBurst;
    private List<Keyframe> previousKeyframesList;

    private List<Signal> currentSignalBurst;

    private List<Signal> nextSignalBurst;

    private List<Signal> currentAndNeighbors;

    public IncrementalRealizer(CharacterManager cm) {
        setCharacterManager(cm);
        keyframePerformers = new ArrayList<>();
        generators = new ArrayList<>();

        lastKeyFrameTime = greta.core.util.time.Timer.getTime();
        gestureGenerator = new GestureKeyframeGenerator();
        generators.add(gestureGenerator);
        generators.add(new SpeechKeyframeGenerator());
        generators.add(new HeadKeyframeGenerator());
        generators.add(new LaughKeyframeGenerator());

        //experimental
        generators.add(new ShoulderKeyframeGenerator());
        generators.add(new TorsoKeyframeGenerator());
        gazeGenerator = new GazeKeyframeGenerator(cm, generators);
        faceGenerator = new FaceKeyframeGenerator();

        keyframeComparator = (o1, o2) -> (int) Math.signum(o1.getOffset() - o2.getOffset());

        // environment
        environment = characterManager.getEnvironment();

        incFeedbackPerformers = new ArrayList<>();

        previousSignalBurst = new ArrayList<>();
        previousKeyframesList = new ArrayList<>();

        currentSignalBurst = new ArrayList<>();

        nextSignalBurst = new ArrayList<>();

        currentAndNeighbors = new ArrayList();
    }

    @Override //TODO add the use of modes: blend, replace, append
    public void performSignals(List<Signal> list, ID requestId, Mode mode) {

        // list of created keyframes
        List<Keyframe> keyframes = new ArrayList<>();

        List<Keyframe> currentKeyframes = new ArrayList<>();

        System.out.println("PERFORM SIGNALS : " + list);
        
        if (currentID == null) {
            currentID = requestId;
        } else if (currentID != requestId) {
            currentID = requestId;
            currentSignalBurst = new ArrayList<>();
            System.out.println("NEW REQUEST ID FOUND");
        }

        if(currentSignalBurst.isEmpty()){
            double pivotStartTime = list.get(0).getStart().getValue();
            for (Signal sig : list) {
                System.out.println(sig + " : " + sig.getStart().getValue());
                if (sig.getStart().getValue() == pivotStartTime) {
                    currentSignalBurst.add(sig);
                } else {
                    nextSignalBurst.add(sig);
                }
            }
        }
        else{
            for (Signal sig : list) {
                if(!currentSignalBurst.contains(sig)){
                    nextSignalBurst.add(sig);
                }
            }
        }
        
        
        
        
        System.out.println("\033[31;1m current = " + currentSignalBurst + " --- \033[34;1m next = " + nextSignalBurst + "\n");

        if (previousSignalBurst.isEmpty()) {
            currentAndNeighbors.addAll(list);
        } else {
            currentAndNeighbors.addAll(previousSignalBurst);
            currentAndNeighbors.addAll(list);
        }

        for (Signal signal : currentAndNeighbors) { //KEYFRAME FOR CURRENT AND NEIGHBORS
            for (KeyframeGenerator generator : generators) {
                if (generator.accept(signal)) {
                    //System.out.println("accepted : " + signal + " --- into : " + generator); //DEBUG
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
        gazeGenerator.generateBodyKeyframes(keyframes);

        for (KeyframeGenerator generator : generators) {
            keyframes.addAll(generator.generateKeyframes());
        }

        keyframes.sort(keyframeComparator);

        // Gaze keyframes for the eyes are generated last
        gazeGenerator.generateEyesKeyframes(keyframes);

        faceGenerator.findExistingAU(keyframes);

        keyframes.addAll(faceGenerator.generateKeyframes());

        // Step 4: adjust the timing of all key frame
        keyframes.sort(keyframeComparator);
        
        generators = new ArrayList<>();

        lastKeyFrameTime = greta.core.util.time.Timer.getTime();
        gestureGenerator = new GestureKeyframeGenerator();
        generators.add(gestureGenerator);
        generators.add(new SpeechKeyframeGenerator());
        generators.add(new HeadKeyframeGenerator());
        generators.add(new LaughKeyframeGenerator());

        //experimental
        generators.add(new ShoulderKeyframeGenerator());
        generators.add(new TorsoKeyframeGenerator());
        gazeGenerator = new GazeKeyframeGenerator(characterManager, generators);
        faceGenerator = new FaceKeyframeGenerator();

        for (Signal signal : currentSignalBurst) { //KEYFRAME FOR CURRENT ONLY
            for (KeyframeGenerator generator : generators) {
                if (generator.accept(signal)) {
                    //System.out.println("accepted : " + signal + " --- into : " + generator); //DEBUG
                    break;
                }
            }
            gazeGenerator.accept(signal);
            faceGenerator.accept(signal);
        }
        gazeGenerator.generateBodyKeyframes(currentKeyframes);

        for (KeyframeGenerator generator : generators) {
            currentKeyframes.addAll(generator.generateKeyframes());
        }

        currentKeyframes.sort(keyframeComparator);

        // Gaze keyframes for the eyes are generated last
        gazeGenerator.generateEyesKeyframes(currentKeyframes);

        faceGenerator.findExistingAU(currentKeyframes);

        currentKeyframes.addAll(faceGenerator.generateKeyframes());

        if (!previousSignalBurst.isEmpty()) {
            /*System.out.println("BEFORE --- " + keyframes);
            keyframes.subList(0, previousKeyframesList.size() - 1).clear();
            System.out.println("PREVIOUS REMOVED --- " + keyframes);
            keyframes = keyframes.subList(0, currentKeyframes.size() - 1);
            System.out.println("AFTER --- " + keyframes);*/
            //System.out.println("Signal size previous = " + previousSignalBurst.size() + " --- Signal size current = " + currentSignalBurst.size() + " --- Signal size next = " + nextSignalBurst.size());
            //System.out.println("Keyframe.size = " + keyframes.size() + " ---- Previous Size = " + previousKeyframesList.size() + " --- current size = " + currentKeyframes.size());
            keyframes = keyframes.subList(previousKeyframesList.size(), previousKeyframesList.size() + currentKeyframes.size()-1);
        }
        else{
            keyframes = keyframes.subList(0, currentKeyframes.size() - 1);
        }

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
        // if the start time to start signals is less than 0, all signals' time have to be increased so that they start from 0
        double absoluteStartTime = greta.core.util.time.Timer.getTime();

        if (mode.getCompositionType()
                == CompositionType.append) {
            absoluteStartTime = Math.max(lastKeyFrameTime, absoluteStartTime);
        }
        absoluteStartTime -= (startTime < 0 ? startTime : 0);

        if (mode.getCompositionType()
                != CompositionType.blend && !keyframes.isEmpty()) {
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

        this.sendFeedback(
                true);

        this.sendKeyframes(keyframes, requestId, mode);

        //previousSignalBurst = list;
        previousSignalBurst = currentSignalBurst;
        previousKeyframesList = currentKeyframes;

        //currentSignalBurst = new ArrayList<>();
        currentSignalBurst = nextSignalBurst;
        
        nextSignalBurst = new ArrayList<>();
        currentAndNeighbors = new ArrayList();

        // Add animation to callbacks
        if (mode.getCompositionType()
                == CompositionType.replace) {
            this.stopAllAnims();
        }

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

    @Override
    public void addIncFeedbackPerformer(IncrementalityFeedbackPerformer performer) {
        incFeedbackPerformers.add(performer);
    }

    @Override
    public void removeIncFeedbackPerformer(IncrementalityFeedbackPerformer performer) {
        incFeedbackPerformers.remove(performer);
    }

    public void sendFeedback(boolean parFeedback) {
        for (IncrementalityFeedbackPerformer performer : incFeedbackPerformers) {
            performer.performIncFeedback(parFeedback);
        }
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
    }

    public void UpdateFaceLibrary() {
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

    public void UpdateGestureLibrary() {
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

    public void UpdateHandLibrary() {
    }
}
