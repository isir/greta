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
package greta.core.interruptions;

import greta.core.feedbacks.Callback;
import greta.core.feedbacks.CallbackPerformer;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.interruptions.reactions.InterruptionReaction;
import greta.core.interruptions.reactions.InterruptionReactionPerformer;
import greta.core.interruptions.reactions.ReactionSignalsMapper;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.CharacterManager;
import greta.core.util.Constants;
import greta.core.util.Mode;
import greta.core.util.enums.interruptions.ReactionType;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.speech.Speech;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Brice Donval
 * @author Angelo Cafaro
 */
public class InterruptionManager extends CharacterDependentAdapter implements IntentionPerformer, InterruptionReactionPerformer, CallbackPerformer, IntentionEmitter, SignalEmitter {

    public static boolean _debugAudioOnlyMode = false;

    /* ---------------------------------------------------------------------- */

    private List<IntentionPerformer> intentionPerformers = new ArrayList<>();
    private List<SignalPerformer> signalPerformers = new ArrayList<>();

    private static final Object intentionsHistoryLock = new Object(); // Used to synchronize threads on intentionsHistory
    private final Map<ID, List<Intention>> intentionsHistory = new HashMap<>();

    private ReactionSignalsMapper reactionSignalsMapper;

    public InterruptionManager(CharacterManager cm){
        setCharacterManager(cm);
        reactionSignalsMapper = new ReactionSignalsMapper(cm);
    }

    /* ---------------------------------------------------------------------- */
    /*                           IntentionPerformer                           */
    /* ---------------------------------------------------------------------- */

    @Override
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode) {

        synchronized (intentionsHistoryLock) {

            if (mode.getReactionType() != ReactionType.NONE) {

                Speech speechIntention = null;

                for (Intention intention : intentions) {
                    if (intention instanceof Speech) {
                        speechIntention = (Speech) intention;
                        break;
                    }
                }

                if (speechIntention != null) {

                    // Adds Reaction Type and Duration to Speech intentions that are in the list of intentions that have been received
                    //
                    speechIntention.setInterruptionReactionType(mode.getReactionType());
                    speechIntention.setInterruptionReactionDuration(mode.getReactionDuration());

                    // Adds a dummy time marker that later will be repositioned by the TTS to mark the end of the reaction to an interruption
                    //
                    speechIntention.addSpeechElement(new TimeMarker(Constants._TIME_MARKER_INTERRUPTION_DETECTED_ID));
                    speechIntention.addSpeechElement(new TimeMarker(Constants._TIME_MARKER_INTERRUPTION_REACTION_STARTED_ID));
                    speechIntention.addSpeechElement(new TimeMarker(Constants.TIME_MARKER_INTERRUPTION_REACTION_END_ID));

                    // Concretizes the Speech intention in order to have real values for our parameters
                    //
                    speechIntention.schedule();
                    final TimeMarker _reactionStartedTimeMarker = speechIntention.getTimeMarker(Constants._TIME_MARKER_INTERRUPTION_REACTION_STARTED_ID);
                    final TimeMarker reactionEndTimeMarker = speechIntention.getTimeMarker(Constants.TIME_MARKER_INTERRUPTION_REACTION_END_ID);

                    if (!_debugAudioOnlyMode) {

                        // Discards from history old intentions
                        //
                        {
                            Iterator<Map.Entry<ID, List<Intention>>> historicizedRequestsIterator = intentionsHistory.entrySet().iterator();
                            while (historicizedRequestsIterator.hasNext()) {

                                Map.Entry<ID, List<Intention>> historicizedRequest = historicizedRequestsIterator.next();
                                List<Intention> historicizedIntentions = historicizedRequest.getValue();

                                Iterator<Intention> historicizedIntentionsIterator = historicizedIntentions.iterator();
                                while (historicizedIntentionsIterator.hasNext()) {

                                    Intention historicizedIntention = historicizedIntentionsIterator.next();
                                    if (historicizedIntention.getEnd().getValue() < _reactionStartedTimeMarker.getValue()) {
                                        historicizedIntentionsIterator.remove();
                                    }
                                }

                                if (historicizedIntentions.isEmpty()) {
                                    historicizedRequestsIterator.remove();
                                }
                            }
                        }

                        // Discards from history future intentions that will never be played in case of HALT or REPLAN
                        //
                        if ((mode.getReactionType() == ReactionType.HALT) || (mode.getReactionType() == ReactionType.REPLAN)) {

                            Iterator<Map.Entry<ID, List<Intention>>> historicizedRequestsIterator = intentionsHistory.entrySet().iterator();
                            while (historicizedRequestsIterator.hasNext()) {

                                Map.Entry<ID, List<Intention>> historicizedRequest = historicizedRequestsIterator.next();
                                List<Intention> historicizedIntentions = historicizedRequest.getValue();

                                Iterator<Intention> historicizedIntentionsIterator = historicizedIntentions.iterator();
                                while (historicizedIntentionsIterator.hasNext()) {

                                    Intention historicizedIntention = historicizedIntentionsIterator.next();
                                    if (historicizedIntention.getEnd().getValue() > _reactionStartedTimeMarker.getValue() + reactionEndTimeMarker.getValue()) {
                                        historicizedIntentionsIterator.remove();
                                    }
                                }

                                if (historicizedIntentions.isEmpty()) {
                                    historicizedRequestsIterator.remove();
                                }
                            }
                        }

                        // Shifts back scheduled intentions
                        //
                        for (List<Intention> historicizedIntentions : intentionsHistory.values()) {

                            for (Intention historicizedIntention : historicizedIntentions) {

                                //if (historicizedIntention.getStart().getValue() > _reactionStartedTimeMarker.getValue()) {

                                List<TimeMarker> historicizedIntentionTimeMarkers = historicizedIntention.getTimeMarkers();
                                for (TimeMarker historicizedIntentionTimeMarker : historicizedIntentionTimeMarkers) {

                                    // if historicizedIntentionTimeMarker is absolute
                                    //if (historicizedIntentionTimeMarker.getFirstSynchPointWithTarget() == null) {
                                    historicizedIntentionTimeMarker.setValue(historicizedIntentionTimeMarker.getValue() - _reactionStartedTimeMarker.getValue());
                                    //}
                                }
                                //}
                            }
                        }

                        // Discards from history intentions for which start < 0
                        //
                        Iterator<Map.Entry<ID, List<Intention>>> historicizedRequestsIterator = intentionsHistory.entrySet().iterator();
                        while (historicizedRequestsIterator.hasNext()) {

                            Map.Entry<ID, List<Intention>> historicizedRequest = historicizedRequestsIterator.next();
                            List<Intention> historicizedIntentions = historicizedRequest.getValue();

                            Iterator<Intention> historicizedIntentionsIterator = historicizedIntentions.iterator();
                            while (historicizedIntentionsIterator.hasNext()) {

                                Intention historicizedIntention = historicizedIntentionsIterator.next();
                                if (historicizedIntention.getStart().getValue() < 0) {
                                    historicizedIntentionsIterator.remove();
                                }
                            }

                            if (historicizedIntentions.isEmpty()) {
                                historicizedRequestsIterator.remove();
                            }
                        }

                        // Shifts forward replaned intentions in case of REPLAN
                        //
                        if (mode.getReactionType() == ReactionType.REPLAN) {

                            for (Intention intention : intentions) {

                                if (intention.getStart().getValue() > _reactionStartedTimeMarker.getValue()) {

                                    List<TimeMarker> intentionTimeMarkers = intention.getTimeMarkers();
                                    for (TimeMarker intentionTimeMarker : intentionTimeMarkers) {

                                        // if intentionTimeMarker is absolute
                                        //if (intentionTimeMarker.getFirstSynchPointWithTarget() == null) {
                                        intentionTimeMarker.setValue(intentionTimeMarker.getValue() + reactionEndTimeMarker.getValue());
                                        //}
                                    }
                                }
                            }
                        }

                        // Adds an interruption reaction (faceexp)
                        //
                        //double reactionIntentionStartValue = speechIntention.getStart().getValue();
                        //double reactionIntentionEndValue = reactionIntentionStartValue + 3.0;
                        //TimeMarker reactionIntentionStartTimeMarker = new TimeMarker("start", reactionIntentionStartValue);
                        //TimeMarker reactionIntentionEndTimeMarker = new TimeMarker("end", reactionIntentionEndValue);
                        //Intention reactionIntention = new BasicIntention("emotion", "react1", "hate", reactionIntentionStartTimeMarker, reactionIntentionEndTimeMarker);
                        //intentions.add(reactionIntention);

                        //
                    } // END DEBUG if (!_debugAudioOnlyMode)
                    //
                }

                if (!_debugAudioOnlyMode) {

                    // Creates the list of intentions to send to the intentionPerformers (old and new intentions)
                    //
                    List<Intention> intentionsToSend = new ArrayList<>();
                    for (List<Intention> historicizedIntentions : intentionsHistory.values()) {
                        intentionsToSend.addAll(historicizedIntentions);
                    }
                    intentionsToSend.addAll(intentions);

                    // Creates the list of intentions to historize (old and new intentions, except speech intentions)
                    //
                    List<Intention> intentionsToHistoricize = new ArrayList<>();
                    for (List<Intention> historicizedIntentions : intentionsHistory.values()) {
                        intentionsToHistoricize.addAll(historicizedIntentions);
                    }
                    for (Intention intention : intentions) {
                        if (!(intention instanceof Speech)) {
                            intentionsToHistoricize.add(intention);
                        }
                    }

                    // Historicizes old and new intentions (except speech intentions)
                    //
                    intentionsHistory.put(requestId, intentionsToHistoricize);

                    // Sends old and new intentions to the intentionPerformers
                    //
                    for (IntentionPerformer performer : intentionPerformers) {
                        performer.performIntentions(intentionsToSend, requestId, mode);
                    }

                    // Create the list of reaction signals to perform
                    List<Signal> reactionSignals = reactionSignalsMapper.getReactionSignals();

                    // Look at the camera always when reacting to interruptions
                    //GazeSignal lookAtUserSignal = new GazeSignal("BMLReactionForcedGazeAt");
                    //lookAtUserSignal.setGazeShift(true);
                    //lookAtUserSignal.setInfluence(Influence.HEAD);
                    //lookAtUserSignal.setTarget(CharacterManager.currentCameraId);
                    //reactionSignals.add(lookAtUserSignal);

                    // Set the start time corrensponding to the start time of the speech intention received in input
                    for (Signal signal : reactionSignals) {
                        signal.getStart().setValue(speechIntention.getStart().getValue());
                        TimeMarker ready = signal.getTimeMarker("ready");
                        if (ready != null) {
                            ready.setValue(speechIntention.getStart().getValue() + 0.25);
                        }
                    }

                    // Sends interruption reaction signals to all signal performers
                    ID requestIDReactionSignals = IDProvider.createID("InterruptionReactionSignals");
                    for (SignalPerformer signalPerformer : signalPerformers) {
                        signalPerformer.performSignals(reactionSignals, requestIDReactionSignals, mode);
                    }

                    //
                } // END DEBUG if (!_debugAudioOnlyMode)
                //

            } else {

                // Historicizes new intentions (except speech intentions)
                //
                List<Intention> intentionsToHistoricize = new ArrayList<>();
                for (Intention intention : intentions) {
                    if (!(intention instanceof Speech)) {
                        intentionsToHistoricize.add(intention);
                    }
                }
                intentionsHistory.put(requestId, intentionsToHistoricize);

                // Sends new intentions to the intentionPerformers
                //
                for (IntentionPerformer performer : intentionPerformers) {
                    performer.performIntentions(intentions, requestId, mode);
                }

            }

            // TODO: remove from list after using the intentions in case of reaction? otherwise callbacks will never be received? check if stopped intentions send out stop callback
            // TODO: add pair or second list for storing mode in history assiciated to intentions
        }
    }

    /* ---------------------------------------------------------------------- */
    /*                    Interruption Reaction Performer                     */
    /* ---------------------------------------------------------------------- */

    @Override
    public void performInterruptionReactions(List<InterruptionReaction> interruptionReactions, ID requestId) {

        // Clear the mapper
        reactionSignalsMapper.clearAll();

        for (InterruptionReaction interruptionReaction : interruptionReactions) {
            // Map the interruption reaction behavior to a signal that is stored in the mapper
            reactionSignalsMapper.mapInterruptionReactionToSignal(interruptionReaction);
        }
    }

    @Override
    public void performInterruptionReaction(InterruptionReaction interruptionReaction, ID requestId) {

        // Clear the mapper
        reactionSignalsMapper.clearAll();

        // Map the interruption reaction behavior to a signal that is stored in the mapper
        reactionSignalsMapper.mapInterruptionReactionToSignal(interruptionReaction);

    }

    /* ---------------------------------------------------------------------- */
    /*                           CallbackPerformer                            */
    /* ---------------------------------------------------------------------- */

    @Override
    public void performCallback(Callback clbck) {

        Logs.debug("Interruption Manager received callback ID [" + clbck.animId() + "] of Type [" + clbck.type() + "] at Time [" + String.format("%.2f", clbck.time()) + "].");

        if (clbck.animId().getSource().startsWith("InterruptionReactionSignals")) {
            return;
        }

        if (clbck.animId().getSource().startsWith("FML-Halt-IGA")) {
            return;
        }

        // Remove from intention history according to ID
        if (!clbck.type().equalsIgnoreCase("start")) {
            synchronized (intentionsHistoryLock) {
                if (intentionsHistory.containsKey(clbck.animId())) {
                    intentionsHistory.remove(clbck.animId());
                } else {
                    Logs.warning("Interruption Manager received a callback ID [" + clbck.animId() + "] of Type [" + clbck.type() + "] at Time [" + String.format("%.2f", clbck.time()) + "] not stored in the history.");
                }
            }
        }
    }

    /* ---------------------------------------------------------------------- */
    /*                            IntentionEmitter                            */
    /* ---------------------------------------------------------------------- */

    @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        intentionPerformers.add(performer);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        intentionPerformers.remove(performer);
    }

    /* ---------------------------------------------------------------------- */
    /*                             Signal Emitter                             */
    /* ---------------------------------------------------------------------- */

    @Override
    public void addSignalPerformer(SignalPerformer performer) {
        signalPerformers.add(performer);
    }

    @Override
    public void removeSignalPerformer(SignalPerformer performer) {
        signalPerformers.remove(performer);
    }

    @Override
    public void onCharacterChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
