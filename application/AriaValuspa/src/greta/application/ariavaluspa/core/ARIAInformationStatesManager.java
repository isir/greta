/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package greta.application.ariavaluspa.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import static greta.application.ariavaluspa.core.InteractionState.NUM_INTERACTION_STATES;
import greta.auxiliary.activemq.aria.ARIAInformationStatePerformer;
import greta.core.signals.GazeSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.signals.gesture.GestureSignal;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.enums.Influence;
import greta.core.util.environment.Environment;
import greta.core.util.environment.Leaf;
import greta.core.util.environment.Node;
import greta.core.util.environment.TreeNode;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.time.TimeMarker;

/**
 *
 * @author Angelo Cafaro
 */
public class ARIAInformationStatesManager implements ARIAInformationStatePerformer, SignalEmitter {

    private static final int MIN_NEXT_RANDOM_IDLE_CHANGE_TIME = 6;
    private static final int MAX_NEXT_RANDOM_IDLE_CHANGE_TIME = 10;
 
    private static final int MIN_READY_IDLEGAZE_TIME = 4;
    private static final int MAX_READY_IDLEGAZE_TIME = 6;
    
    private static final double MAX_IDLE_GAZE_TARGET_COORD_OFFSET = 0.2;

    private InteractionState currentInteractionState;
    private final Thread statePerformer;
    private List<SignalPerformer> signalPerformers = new ArrayList<>();
    private final Object lock = new Object(); // used to synchronize this thread on currentInteractionState
    private Environment environment;
    private List<Node> idleGazeTargets = new ArrayList<>();

    private double lastTimeRandomIdleMovementSent;
    private double randomDurationNextIdleMovement;
    private Random randomGenerator;
    private boolean[] enteringStatesFlags;
    private boolean[] exitingStatesFlags;
    private String idleGazeTargetsRootID;
    public CharacterManager cm;

    public ARIAInformationStatesManager(CharacterManager c) {

        randomGenerator = new Random();
        lastTimeRandomIdleMovementSent = 0.0;
        randomDurationNextIdleMovement = this.getRandomInteger(MIN_NEXT_RANDOM_IDLE_CHANGE_TIME, MAX_NEXT_RANDOM_IDLE_CHANGE_TIME);
        idleGazeTargetsRootID = IDProvider.createID("IdleGazeTargetsRoot").toString();

        this.generateRandomIdleGazeTargets();

        enteringStatesFlags = new boolean[NUM_INTERACTION_STATES];
        Arrays.fill(enteringStatesFlags, false);
        exitingStatesFlags = new boolean[NUM_INTERACTION_STATES];
        Arrays.fill(exitingStatesFlags, false);

        currentInteractionState = InteractionState.IDLE;
        enteringStatesFlags[InteractionState.IDLE.ordinal()] = true;

        statePerformer = new Thread(() -> {
            while (!Thread.interrupted())
            {
                statePerformerThreadUpdate();
                try {
                    synchronized (lock) {
                        lock.wait(1000);
                    }
                } catch (InterruptedException ex) {
                    Logs.warning(ARIAInformationStatesManager.class.getName() + "[" + ex + "]");
                }
            }
        });
        statePerformer.start();
        this.cm=c;
    }

    public void setEnvironment(Environment env) {
        this.environment = env;
        Logs.info(ARIAInformationStatesManager.class.getName() + ": Environment set.");
        this.generateRandomIdleGazeTargets();
    }

    @Override
    protected void finalize() throws Throwable {
        statePerformer.interrupt();
        super.finalize();
    }

    private void statePerformerThreadUpdate() {
        switch (currentInteractionState) {
            case IDLE:
            {
                boolean forceIdleGazeModality = false;
                if (enteringStatesFlags[InteractionState.IDLE.ordinal()]) {
                    forceIdleGazeModality = true;
                    enteringStatesFlags[InteractionState.IDLE.ordinal()] = false;
                }

                // Gets current time in VIB
                double currentTime = greta.core.util.time.Timer.getTime();

                // Generates Random idle gaze shifts
                if (currentTime >= lastTimeRandomIdleMovementSent + randomDurationNextIdleMovement)
                {
                    // Get a random modality type betaeen 1 and 3
                    // 0: nothing
                    // 1: only idle gaze change
                    // 2: only idle posture change
                    // 3: both idle gaze and posture change
                    int randomModalityType = getRandomInteger(0, 3);
                    if ((forceIdleGazeModality) && (randomModalityType % 2 == 0)) {
                        randomModalityType++;
                    }
                    
                    // Create the list of idle signals to perform
                    List<Signal> idleMovementSignals = new ArrayList<>();
                    
                    // Idle gaze generation
                    if (((randomModalityType == 1) || (randomModalityType == 3)) && (!idleGazeTargets.isEmpty())) {

                        int randomGazeTargetIndex = getRandomInteger(0, idleGazeTargets.size() - 1);
                        Node randomGazeTargetNode = idleGazeTargets.get(randomGazeTargetIndex);

                        // We add noise to the node coords within the specified MAX_IDLE_GAZE_TARGET_COORD_OFFSET
                        ((TreeNode)randomGazeTargetNode).setCoordinates(
                                Math.random() * 2 * MAX_IDLE_GAZE_TARGET_COORD_OFFSET - MAX_IDLE_GAZE_TARGET_COORD_OFFSET, 
                                Math.random() * 2 * MAX_IDLE_GAZE_TARGET_COORD_OFFSET - MAX_IDLE_GAZE_TARGET_COORD_OFFSET, 
                                Math.random() * 2 * MAX_IDLE_GAZE_TARGET_COORD_OFFSET - MAX_IDLE_GAZE_TARGET_COORD_OFFSET);
                        
                        // Generate a random ready time for the idle gaze shift
                        int randomReadyTime = getRandomInteger(MIN_READY_IDLEGAZE_TIME, MAX_READY_IDLEGAZE_TIME);

                        // Look at the chosen random target
                        GazeSignal lookAtTargetSignal = new GazeSignal("BMLRandomIdleGaze");
                        lookAtTargetSignal.setGazeShift(true);
                        lookAtTargetSignal.setInfluence(Influence.HEAD);
                        lookAtTargetSignal.setTarget(randomGazeTargetNode.getIdentifier());

                        lookAtTargetSignal.getStart().setValue(0.0);
                        TimeMarker ready = lookAtTargetSignal.getTimeMarker("ready");
                        if (ready != null) {
                            ready.setValue(randomReadyTime);
                        }

                        idleMovementSignals.add(lookAtTargetSignal);
                    }
                    
                    // Idle posture change generation
                    if ((randomModalityType == 2) || (randomModalityType == 3)) {
                                              
                        GestureSignal idleRestPose = new GestureSignal("BMLRandomIdleRestPose");
                        idleRestPose.getStart().setValue(0);
                        String randomRestPoseName = "Along";
                        double p = Math.random();
                        if (p < 0.33) {
                            randomRestPoseName = "ArmsCrossed";
                        }
                        else if (p < 0.66) {
                            randomRestPoseName = "Akimbo";
                        }
                        idleRestPose.setReference("rest=" + randomRestPoseName);
                        idleMovementSignals.add(idleRestPose);
                    }
                    
                    if (!idleMovementSignals.isEmpty())
                    {
                        // Sends the idle movements signals to all signal performers
                        ID requestIDIdleMovementSignals = IDProvider.createID("IdleMovementSignals");
                        for (SignalPerformer signalPerformer : signalPerformers) {
                            signalPerformer.performSignals(idleMovementSignals, requestIDIdleMovementSignals, new Mode(CompositionType.blend));
                        }
                    }
                    
                    lastTimeRandomIdleMovementSent = greta.core.util.time.Timer.getTime();
                    randomDurationNextIdleMovement = this.getRandomInteger(MIN_NEXT_RANDOM_IDLE_CHANGE_TIME, MAX_NEXT_RANDOM_IDLE_CHANGE_TIME);
                }
                
                break;
            }
            case ENGAGING:
            {
                if (enteringStatesFlags[InteractionState.ENGAGING.ordinal()])
                {
                    lastTimeRandomIdleMovementSent = 0;
                    randomDurationNextIdleMovement = 0;
                    
                    // Create the list of engaging signals to perform
                    List<Signal> engagingSignals = new ArrayList<>();
                    
                    // Sets arms along as rest pose
                    GestureSignal engagingRestPose = new GestureSignal("BMLEngagingRestPose");
                    engagingRestPose.getStart().setValue(0);
                    engagingRestPose.setReference("rest=Along");
                    engagingSignals.add(engagingRestPose);

                    // Look at the user
                    GazeSignal lookAtUserSignal = new GazeSignal("BMLEngagingGazeAtUser");
                    lookAtUserSignal.setGazeShift(true);
                    lookAtUserSignal.setInfluence(Influence.HEAD);
                    lookAtUserSignal.setTarget(this.cm.currentCameraId);
                    lookAtUserSignal.getStart().setValue(0.0);
                    TimeMarker ready = lookAtUserSignal.getTimeMarker("ready");
                    if (ready != null) {
                        ready.setValue(1);
                    }

                    // Add the gaze at user to the list of engaging signals
                    engagingSignals.add(lookAtUserSignal);

                    // Sends the engaging signals to all signal performers
                    ID requestIDEngagingSignals = IDProvider.createID("EngagingSignals");
                    for (SignalPerformer signalPerformer : signalPerformers) {
                        signalPerformer.performSignals(engagingSignals, requestIDEngagingSignals, new Mode(CompositionType.blend));
                    }

                    enteringStatesFlags[InteractionState.ENGAGING.ordinal()] = false;
                }
                break;
            }
            case ENGAGED:
            {
                if (enteringStatesFlags[InteractionState.ENGAGED.ordinal()])
                {
                    lastTimeRandomIdleMovementSent = 0;
                    randomDurationNextIdleMovement = 0;
                    
                    // Create the list of engaging signals to perform
                    List<Signal> engagedSignals = new ArrayList<>();
                    
                    // Sets arms along as rest pose
                    GestureSignal engagedRestPose = new GestureSignal("BMLEngagedRestPose");
                    engagedRestPose.getStart().setValue(0);
                    engagedRestPose.setReference("rest=Along");
                    engagedSignals.add(engagedRestPose);
                    
                    // Look at the user
                    GazeSignal lookAtUserSignal = new GazeSignal("BMLEngagedGazeAtUser");
                    lookAtUserSignal.setGazeShift(true);
                    lookAtUserSignal.setInfluence(Influence.HEAD);
                    lookAtUserSignal.setTarget(this.cm.currentCameraId);
                    lookAtUserSignal.getStart().setValue(0.0);
                    TimeMarker ready = lookAtUserSignal.getTimeMarker("ready");
                    if (ready != null) {
                        ready.setValue(1);
                    }
                    engagedSignals.add(lookAtUserSignal);
                    
                    // Sends the engaged signals to all signal performers
                    ID requestIDEngagedSignals = IDProvider.createID("EngagedSignals");
                    for (SignalPerformer signalPerformer : signalPerformers) {
                        signalPerformer.performSignals(engagedSignals, requestIDEngagedSignals, new Mode(CompositionType.blend));
                    }
                    
                    enteringStatesFlags[InteractionState.ENGAGED.ordinal()] = false;
                }
                break;
            }
            case DISENGAGING:
            {
                if (enteringStatesFlags[InteractionState.DISENGAGING.ordinal()])
                {
                    lastTimeRandomIdleMovementSent = 0;
                    randomDurationNextIdleMovement = 0;
                    
                    // Create the list of disengaging signals to perform
                    List<Signal> disengagedSignals = new ArrayList<>();
                    
                    // Sets arms crossed as rest pose
                    GestureSignal disengagedRestPose = new GestureSignal("BMLDisengagedRestPose");
                    disengagedRestPose.getStart().setValue(0);
                    disengagedRestPose.setReference("rest=ArmsCrossed");
                    disengagedSignals.add(disengagedRestPose);
                    
                    // Sends the disengaged signals to all signal performers
                    ID requestIDDisengagedSignals = IDProvider.createID("DisengagedSignals");
                    for (SignalPerformer signalPerformer : signalPerformers) {
                        signalPerformer.performSignals(disengagedSignals, requestIDDisengagedSignals, new Mode(CompositionType.blend));
                    }
                    
                    enteringStatesFlags[InteractionState.DISENGAGING.ordinal()] = false;
                }
                break;
            }
            default: break;
        }
    }

    private int getRandomInteger(int min, int max) {

        return randomGenerator.nextInt(max + 1 - min) + min;
    }


    @Override
    public void performStateChange(String newStateName) {

        InteractionState newState = InteractionState.interpret(newStateName.toUpperCase());

        if (newState != currentInteractionState) {

            // Set the exiting flag for the current state before changing state
            Arrays.fill(exitingStatesFlags, false);
            exitingStatesFlags[currentInteractionState.ordinal()] = true;

             // Change the current state
            Logs.debug("Changing interaction state from [" + currentInteractionState + "] to [" + newState + "]");
            currentInteractionState = newState;

            // Set the entering flag for the new current state
            Arrays.fill(enteringStatesFlags, false);
            enteringStatesFlags[newState.ordinal()] = true;

            synchronized (lock) {
                lock.notify();
            }
        }
    }

    @Override
    public void performLanguageChange(String language) {

        ARIALanguage newLanguage = ARIALanguage.interpret(language.toLowerCase());
        
        if (newLanguage == ARIALanguage.english) {
            this.cm.setValueString("CEREPROC_LANG", "en-GB");
            this.cm.setValueString("CEREPROC_VOICE", "sarah");
        }
        else if (newLanguage == ARIALanguage.french) {
            this.cm.setValueString("CEREPROC_LANG", "fr-FR");
            this.cm.setValueString("CEREPROC_VOICE", "suzanne");
        }
        else if (newLanguage == ARIALanguage.german) {
            Logs.warning(ARIAInformationStatesManager.class.getName() + ": The german language is currently not supported in ARIA-Greta by Cereproc.");
        }
        else {
            Logs.warning(ARIAInformationStatesManager.class.getName() + ": The [" + language + "] language is currently not supported in ARIA.");
        }
    }

    private void generateRandomIdleGazeTargets() {

        if (environment != null)
        {
            idleGazeTargets.clear();
            TreeNode agentNode = (TreeNode) environment.getNode(this.cm.currentCharacterId);
            TreeNode rootNodeIdleGazeTargets = (TreeNode) environment.getNode(idleGazeTargetsRootID);

            if (rootNodeIdleGazeTargets == null) {

                // Create root node
                TreeNode root = new TreeNode();
                root.setIdentifier(idleGazeTargetsRootID);
                environment.addNode(root, agentNode);

                // Set root node coordinates
                root.setCoordinates(0.0, 1.5, 1.5);

                // Create nodes for random IDLE gaze targets
                TreeNode nodeAgentLeft = new TreeNode();
                nodeAgentLeft.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                environment.addNode(nodeAgentLeft, root);
                TreeNode nodeAgentLeftTarget = new TreeNode();
                nodeAgentLeftTarget.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                environment.addNode(nodeAgentLeftTarget, nodeAgentLeft);

                nodeAgentLeft.setCoordinates(1.5, 0.0, 0.0);
                idleGazeTargets.add(nodeAgentLeftTarget);

                TreeNode nodeAgentRight = new TreeNode();
                nodeAgentRight.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                environment.addNode(nodeAgentRight, root);
                TreeNode nodeAgentRightTarget = new TreeNode();
                nodeAgentRightTarget.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                environment.addNode(nodeAgentRightTarget, nodeAgentRight);

                nodeAgentRight.setCoordinates(-1.5, 0.0, 0.0);
                idleGazeTargets.add(nodeAgentRightTarget);
                
                TreeNode nodeAgentUp = new TreeNode();
                nodeAgentUp.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                environment.addNode(nodeAgentUp, root);
                TreeNode nodeAgentUpTarget = new TreeNode();
                nodeAgentUpTarget.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                environment.addNode(nodeAgentUpTarget, nodeAgentUp);

                nodeAgentUp.setCoordinates(0.0, 0.2, 0.0);
                idleGazeTargets.add(nodeAgentUpTarget);
                
                TreeNode nodeAgentDown = new TreeNode();
                nodeAgentDown.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                environment.addNode(nodeAgentDown, root);
                TreeNode nodeAgentDownTarget = new TreeNode();
                nodeAgentDownTarget.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                environment.addNode(nodeAgentDownTarget, nodeAgentDown);

                nodeAgentDown.setCoordinates(0.0, -0.1, 0.0);
                idleGazeTargets.add(nodeAgentDownTarget);
                
                // Debug
                /*
                Leaf right = new Leaf();
                right.setSize(0.1, 0.1, 0.1);
                environment.addNode(right, nodeAgentRightTarget);
                Leaf left = new Leaf();
                left.setSize(0.1, 0.1, 0.1);
                environment.addNode(left, nodeAgentLeftTarget);
                Leaf up = new Leaf();
                up.setSize(0.1, 0.1, 0.1);
                environment.addNode(up, nodeAgentUpTarget);
                Leaf down = new Leaf();
                down.setSize(0.1, 0.1, 0.1);
                environment.addNode(down, nodeAgentDownTarget);
                */
                // End Debug

                Logs.info(ARIAInformationStatesManager.class.getName() + ": random idle gaze target nodes generated.");
            }
            else {
                // Load nodes
                idleGazeTargets.addAll(rootNodeIdleGazeTargets.getChildren());
            }
        }
        else {
            Logs.warning(ARIAInformationStatesManager.class.getName() + ": Environment not set, cannot generate random idle gaze target nodes.");
        }
    }

    @Override
    public void addSignalPerformer(SignalPerformer performer) {
        signalPerformers.add(performer);
    }

    @Override
    public void removeSignalPerformer(SignalPerformer performer) {
        signalPerformers.remove(performer);
    }

}