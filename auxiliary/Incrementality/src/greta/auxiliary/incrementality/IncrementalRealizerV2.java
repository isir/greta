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
 /*---                   USAGE: Planner -> This                     ---*/
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
import greta.core.intentions.IntentionPerformer;
import greta.core.keyframes.AudioKeyFrame;
import greta.core.keyframes.CancelableKeyframePerformer;
import greta.core.keyframes.Keyframe;
import greta.core.keyframes.KeyframeEmitter;
import greta.core.keyframes.KeyframePerformer;
import greta.core.keyframes.PhonemSequence;
import greta.core.keyframes.KeyframesFeedbackPerformer;
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

import greta.core.keyframes.face.AUKeyFrame;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * MODIFIED VERSION OF BEHAVIOR REALIZER
 *
 * @author Sean Graux
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
public class IncrementalRealizerV2 extends CallbackSender implements CancelableSignalPerformer, KeyframeEmitter, CharacterDependent, IncrementalityInteractionPerformer, IncrementalityInteractionEmitter {

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
    private CharacterManager characterManager;

    private ID currentID;

    private List<Keyframe> storeKeyframe;

    private int currentIndex;

    private ChunkSenderThread chunkSenderThread;

    private double firstKeyframeOffset;
    private double lastKeyframeOffset;

    private List<IncrementalityInteractionPerformer> performerList = new ArrayList<>();

    public IncrementalRealizerV2(CharacterManager cm) {
        
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
        gazeGenerator = new GazeKeyframeGenerator(cm,generators);
        faceGenerator = new FaceKeyframeGenerator();

        generators = new ArrayList<>();
        generators.add(gestureGenerator);
        generators.add(speechGenerator);
        generators.add(headGenerator);
        generators.add(laughGenerator);
        generators.add(shoulderGenerator);
        generators.add(torsoGenerator);
        generators.add(gazeGenerator);
        generators.add(faceGenerator);
        
        storeKeyframe = new ArrayList<>();

        chunkSenderThread = new ChunkSenderThread(keyframePerformers);
        chunkSenderThread.setDaemon(true);
        chunkSenderThread.start();

    }

    @Override //TODO add the use of modes: blend, replace, append
    public void performSignals(List<Signal> list, ID requestId, Mode mode) {
        
        //mode.setCompositionType(CompositionType.append);
        
        // list of created keyframes
        List<Keyframe> keyframes = new ArrayList<>();
        currentID = requestId;

        //TreeMap<Integer, List<Keyframe>> treeList = new TreeMap<Integer, List<Keyframe>>();
        // Step 1: Schedule each signal independently from one to another.
        // The result of this step is to attribute abs value to possible sync points (compute absolute values from relative values).
        // The value of Start and End should be calculated in this step. So that we can sort
        for (Signal signal : list) {
            if (signal instanceof PointingSignal) {
                gestureGenerator.fillPointing((PointingSignal) signal);
            } else {
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

        for (Keyframe kf : keyframes) {
            System.out.format("Keyframes before sort : %s - %s - %.3f - %.3f%n", kf.getModality(), kf.getId(), kf.getOnset(), kf.getOffset());
        }

        keyframes.sort(keyframeComparator);
        
        for (Keyframe kf : keyframes) {
            System.out.format("Keyframes after sort : %s - %.3f - %.3f%n", kf.getModality(), kf.getOnset(), kf.getOffset());
        }
        
//        System.out.println("keyframe's modality after Generator.generateKeyframes()");
//        for(Keyframe k:keyframes){
//            System.out.println("greta.core.behaviorrealizer.Realizer.performSignals(): " + k.getModality());
//        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        // Step 4: ADJUST THE TIMING OF THE KEYFRAMES
        ///////////////////////////////////////////////////////////////////////////////////////////////////    

        /*for(Keyframe kf : keyframes){
            System.out.println(kf.toString() + " --- " + kf.getOffset());
        }*/
        //System.out.println(keyframes.get(keyframes.size() - 1).getOffset());
        
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
        if (mode.getCompositionType() == CompositionType.append) {
            absoluteStartTime = Math.max(lastKeyFrameTime, absoluteStartTime);
        }
        absoluteStartTime -= (startTime < 0 ? startTime : 0);
        if (mode.getCompositionType() != CompositionType.blend && !keyframes.isEmpty()) {
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

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        // Step 5: SEND KEYFRAMES AND MAKE IT REAL!
        ///////////////////////////////////////////////////////////////////////////////////////////////////           
        
        System.out.println(" ------------------------------------ START OF " + requestId + " ------------------------------------");

        //In case of stop interaction, thread stop sending chunk, stop keyframe will bypass thread to ensure correrct stop
//        if (requestId.toString().contains("stop")) {
        if (requestId.toString().contains("stop") || requestId.toString().contains("renew")) {
            double absoluteTime = greta.core.util.time.Timer.getTime(); //ABSOLUTE TIME = time when clicking the stop button where 0 is the start of modular
            double duration = lastKeyframeOffset - firstKeyframeOffset; //DURATION = Duration of the stopped execution
            double stopTime = absoluteTime - firstKeyframeOffset; //STOPTIME = time when clicking the stop button where 0 is the start of the execution

            System.out.println("----- Stop -----");
            System.out.println("ABSOLUTE TIME = " + absoluteTime + " --- DURATION = " + duration + " --- STOPTIME = " + stopTime);
            this.stopAllAnims();
            chunkSenderThread.wakeUp();
            chunkSenderThread.emptyChunkList();
            chunkSenderThread.closeQueue();
            for (KeyframePerformer performer : keyframePerformers) {
                performer.performKeyframes(keyframes, requestId, mode);
            }
            //Otherwise, normal execution: chunking keyframes and sending chunks to thread
        } else {
            //gather offset of first keyframe and last keyframe to calculate duration and stopTime in case of stop
            firstKeyframeOffset = keyframes.get(0).getOffset();
            lastKeyframeOffset = keyframes.get(keyframes.size()-1).getOffset();

            //CHUNKING KEYFRAMES
            TreeMap<Integer, List<Keyframe>> treeList = this.createChunk(keyframes);

            System.out.println("\n -----------------------------      SENDING CHUNKS TO THREAD      -------------------------------");

            if (mode.getCompositionType() == CompositionType.replace || mode.getCompositionType() == CompositionType.blend) {
                this.stopAllAnims();
//                for (IncrementalityInteractionPerformer pf : performerList) {
//                    pf.performIncInteraction("stop");
//                }
                chunkSenderThread.wakeUp();
                chunkSenderThread.emptyChunkList();
                chunkSenderThread.closeQueue();
            }

            // Add animation to callbacks
            this.addAnimation(requestId, absoluteStartTime, lastKeyFrameTime);

            chunkSenderThread.send(treeList, requestId, mode);

        }
    }

    @Override
    public void cancelSignalsById(ID requestId
    ) {
        for (KeyframePerformer performer : keyframePerformers) {
            if (performer instanceof CancelableKeyframePerformer) {
                ((CancelableKeyframePerformer) performer).cancelKeyframesById(requestId);
            }
        }
    }

    @Override
    public void addKeyframePerformer(KeyframePerformer kp
    ) {
        if (kp != null) {
            keyframePerformers.add(kp);
            chunkSenderThread.addKeyframePerformer(kp);
        }
    }

    @Override
    public void removeKeyframePerformer(KeyframePerformer kp
    ) {
        keyframePerformers.remove(kp);
        chunkSenderThread.removeKeyframePerformer(kp);
    }

    //Create chunk of keyframes based on their offset
    /* A chunk is a group of keyframes with offsets that share a time period.
    For exemple, if you want to work with chunks of 2 seconds,
    the first chunk will contain the keyframes with offset ranging from 
    0 to 2 seconds, then the second chunk will contain the keyframes with offset from 2 to 4 seconds
    etc ...
    You can chose which size of chunk you want to work with down below*/
    //NOTE: because of what appear to be a synchronization down the execution, chunks of size 2 or 3 are recommended.
    public TreeMap<Integer, List<Keyframe>> createChunk(List<Keyframe> listKeyframe) {
        TreeMap<Integer, List<Keyframe>> treeList = new TreeMap<Integer, List<Keyframe>>();
        List<Keyframe> processKeyframesList = new ArrayList<>();
        currentIndex = (int) listKeyframe.get(0).getOffset();

        int chunkSize = 3; //Modify this value to chose the chunk size

        for (Keyframe kf : listKeyframe) { //goes through keyframes
            int offsetInt = (int) kf.getOffset();

            if (offsetInt % chunkSize == 0 && offsetInt > currentIndex) { //create chunk based on indicated size
                currentIndex = offsetInt;
            }
            int index = currentIndex;

            if (treeList.containsKey(index)) {
                processKeyframesList = treeList.get(index);
            } else {
                processKeyframesList = new ArrayList<>();
            }
            processKeyframesList.add(kf);
            treeList.put(index, processKeyframesList);
        }

        for (Map.Entry<Integer, List<Keyframe>> entry : treeList.entrySet()) {
//            System.out.println("Keyframes in chunk " + entry.getKey());
            for (Keyframe kf : entry.getValue()) {
                System.out.format("Keyframes in chunk %s : %s - %.3f - %.3f%n", entry.getKey(), kf.getModality(), kf.getOnset(), kf.getOffset());
            }
        }
        
        return treeList;
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

    //[CAUTHION: not used usually]
    //Interaction with the realizer from the outside, ex interuption
    @Override
    public void performIncInteraction(String parParam) {
        if (parParam.equals("pauseGesture")) {
            chunkSenderThread.closeQueue();
            this.stopAllAnims();
        } else if (parParam.equals("resume")) {
            chunkSenderThread.putInQueue();
//        } else if (parParam.equals("clearQueue")) {
        } else if (parParam.equals("stop")) {
            chunkSenderThread.emptyChunkList();
            chunkSenderThread.closeQueue();
        }
        System.out.println("RECEIVED " + parParam);
    }

    @Override
    public void addIncInteractionPerformer(IncrementalityInteractionPerformer performer) {
        performerList.add(performer);
    }

    @Override
    public void removeIncInteractionPerformer(IncrementalityInteractionPerformer performer) {
        performerList.add(performer);
    }
}
