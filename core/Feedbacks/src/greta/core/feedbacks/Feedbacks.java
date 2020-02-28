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
package greta.core.feedbacks;

import greta.core.intentions.Intention;
import greta.core.intentions.IntentionPerformer;
import greta.core.signals.Signal;
import greta.core.signals.SignalPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.log.Logs;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the Greta's Feedbacks manager<br/><br/> It manages feedbacks
 * for "Intentions" and "Signals": begin, stop, end, dead of "Intentions"
 * (communicative intentions level, FML) begin, stop, end, dead of "Signals"
 * (behaviour level, BML) To do that, it maintain updated four lists of
 * "Animations" (corresponding to Intentions and Signals): a first list updated
 * when a new Animations is triggered (i.e. an Intention or a Signal has been
 * received): pending Animations three other lists updated when a callback is
 * received: dead Animations started Animations stopped Animations A feedback is
 * sent for each callback received. It transmit, the id of the animation, the
 * time, and its state (dead/started/stop/ended)
 *
 * @author Ken Prepin
 */
public class Feedbacks implements CallbackPerformer, FeedbackEmitter, SignalPerformer, IntentionPerformer {

    private class FeedbackThread extends Thread {

        private boolean threadStarted;

        public FeedbackThread() {
            this.setDaemon(true);
            threadStarted = true;
        }

        public void stopTh() {
            threadStarted = false;
        }

        @Override
        public void run() {
            while (threadStarted) {
                synchronized (Feedbacks.this) {
                    //System.out.println("listStartedAnimations.isEmpty()"+listStartedAnimations.isEmpty());
                    if (!listStartedAnimations.isEmpty()) {
                        for (int i = 0; i < listStartedAnimations.size(); i++) {

                            TemporizableList tmpList = listStartedAnimations.get(i);

                            if (speech_sgnl == null || speech_sgnl.getId().equals("")){
                                List<Temporizable> array_signal = tmpList.getPendingList();
                                for(Temporizable tze : array_signal){
                                    if (tze instanceof SpeechSignal){
                                        speech_sgnl = (SpeechSignal) tze;
                                    }
                                }
                            }

                            tmpList.update();

                            TimeMarker last_timemarker = tmpList.updateTimeMarker(speech_sgnl);

                            if(last_timemarker.getName() != "" && last_timemarker.getName() != oldTimeMarker_ID){
                                oldTimeMarker_ID = last_timemarker.getName();
                                for (FeedbackPerformer feedbackPerformer : listFeedbackPerformer) {
                                    feedbackPerformer.performFeedback(tmpList.getID(), "end", speech_sgnl, last_timemarker);
                                }
                            }

                            List<Temporizable> listLastStarted = tmpList.listLastStarted();
                            List<Temporizable> listFinished = tmpList.listFinished();
                            if (!listLastStarted.isEmpty()) {
                                for (FeedbackPerformer feedbackPerformer : listFeedbackPerformer) {
                                    //feedbackPerformer.performFeedback(tmpList.getID(), "started", listLastStarted, last_timemarker);
                                    feedbackPerformer.performFeedback(tmpList.getID(), "started", listLastStarted);
                                }
                            }
                            if (!listFinished.isEmpty()) {
                                for (FeedbackPerformer feedbackPerformer : listFeedbackPerformer) {
                                    feedbackPerformer.performFeedback(tmpList.getID(), "ended", listFinished);
                                }
                            }


                            if (tmpList.isFinished()) {
                                listStartedAnimations.remove(i);
                                i--;
                            }
                        }
                    }
                }
            }
        }
    }

    private CharacterManager charactermanager;
    /**
     * List of Pending Animations (waiting start or death) filled with every
     * incoming Intention or Signal (by IntentionPerforme and SignalPerformer)
     * emptied by "start" signals (by CallbackPerformer)
     */
    private List<TemporizableList> listPendingAnimations;
    /**
     * List of Dead Animations (waiting start or death) filled with every
     * incoming Intention or Signal (by IntentionPerforme and SignalPerformer)
     * emptied by "start" signals (by CallbackPerformer)
     */
    private List<TemporizableList> listDeadAnimations;
    private List<TemporizableList> listStoppedAnimations;
    private List<TemporizableList> listStartedAnimations;
    /**
     * In some cases, the callback "start anim id" can be received before the
     * reception of the animation "anim id", in that case, the callbalck is
     * added to listCallbackWithoutAnim The callback is stored until the
     * corresponding animation is received
     */
    private List<Callback> listCallbacksWithoutAnim;
    private List<FeedbackPerformer> listFeedbackPerformer;
    private FeedbackThread feedbackThread;
    private SpeechSignal speech_sgnl;
    public String oldTimeMarker_ID = "";

    public Feedbacks(CharacterManager cm) {
        this.charactermanager = cm;
        listPendingAnimations = new ArrayList<TemporizableList>();
        listDeadAnimations = new ArrayList<TemporizableList>();
        listStartedAnimations = new ArrayList<TemporizableList>();
        listStoppedAnimations = new ArrayList<TemporizableList>();
        listCallbacksWithoutAnim = new ArrayList<Callback>();
        listFeedbackPerformer = new ArrayList<FeedbackPerformer>();
        startThread();
    }

    public final void startThread() {
        if (feedbackThread == null || !feedbackThread.isAlive()) {
            feedbackThread = new FeedbackThread();
            feedbackThread.start();
        }
    }

    public final void stopThread() {
        if (feedbackThread != null) {
            feedbackThread.stopTh();
        }
    }

    public synchronized void performAnimation(List<? extends Temporizable> temporizables, ID requestId) {
        Logs.debug("[Feedbacks] Animation received: " + requestId);
        listPendingAnimations.add(new TemporizableList(requestId, temporizables));
        // Cases when a callback on this anim was received before the animation
        Callback callback = findCallback(requestId);

        //while (greta.core.util.time.Timer.getTime() < )
        if (callback != null) {
            Logs.debug("[Feedbacks] Animation already has a callback " + requestId);
            performCallback(callback);
            listCallbacksWithoutAnim.remove(callback);
        }
    }
    /*
     * This function update the list of pending animations when a feedback is
     * received, i.e. when a start and its associated id are received
     */

    @Override
    public synchronized void performCallback(Callback callback) {
        Logs.debug("[Feedbacks|performCallback] " + callback.type() + " on anim " + callback.animId());
        TemporizableList tmpList;
        if ("dead".equals(callback.type())) {
            tmpList = findAnim(listPendingAnimations, callback.animId());
            if (tmpList != null) {
                tmpList.setDeadTime(callback.time());
                listDeadAnimations.add(tmpList);
                listPendingAnimations.remove(tmpList);
            } else {
                listCallbacksWithoutAnim.add(callback);
                Logs.debug("[Feedbacks] callback added to listCallbacksWithoutAnim for anim " + callback.animId());
            }
        }
        if ("start".equals(callback.type())) {

            tmpList = findAnim(listPendingAnimations, callback.animId());
            if (tmpList != null) {
                tmpList.setStartTime(callback.time());
                listStartedAnimations.add(tmpList);
                listPendingAnimations.remove(tmpList);
            } else {
                listCallbacksWithoutAnim.add(callback);
                Logs.debug("[Feedbacks] callback added to listCallbacksWithoutAnim for anim " + callback.animId());
            }
        }
        if ("stopped".equals(callback.type())) {
            tmpList = findAnim(listStartedAnimations, callback.animId());
            if (tmpList != null) {
                tmpList.setStoppedTime(callback.time());
                listStoppedAnimations.add(tmpList);
                listStartedAnimations.remove(tmpList);
            }
        }
        if ("end".equals(callback.type())) {
            tmpList = findAnim(listStartedAnimations, callback.animId());
            if (tmpList != null) {
                tmpList.setEndTime(callback.time());
                listStoppedAnimations.add(tmpList);
                listStartedAnimations.remove(tmpList);
            }
        }

        for (FeedbackPerformer feedbackPerformer : listFeedbackPerformer) {
            feedbackPerformer.performFeedback(callback);
        }
        Logs.debug("[Feedbacks] Callback received. Type: \"" + callback.type() + "\" Time: " + callback.time() + " Id: \"" + callback.animId() + "\" Timer: " + Timer.getTimeMillis());
    }
    /*
     * This function find an animation in the pending, dead, started, stopped or
     * ended list using animation id.
     */

    private TemporizableList findAnim(List<TemporizableList> listAnimation, ID Id) {
        for (TemporizableList tmpList : listAnimation) {
            if (tmpList.getID()==Id) {
                return tmpList;
            }
        }
        return null;
    }

    private Callback findCallback(ID animId) {
        for (Callback cback : listCallbacksWithoutAnim) {
            if (animId == cback.animId()) {
                return cback;
            }
        }
        return null;
    }

    @Override
    public synchronized void addFeedbackPerformer(FeedbackPerformer performer) {
        listFeedbackPerformer.add(performer);
    }

    @Override
    public synchronized void removeFeedbackPerformer(FeedbackPerformer performer) {
        listFeedbackPerformer.remove(performer);
    }

    @Override
    public void performSignals(List<Signal> list, ID requestId, Mode mode) {
        performAnimation(list, requestId);
    }

    @Override
    public void performIntentions(List<Intention> list, ID requestId, Mode mode) {
        performAnimation(list, requestId);
    }
}
