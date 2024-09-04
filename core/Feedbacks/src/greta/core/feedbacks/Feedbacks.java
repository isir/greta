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

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.transport.udp.OSCPortOut;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionPerformer;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.signals.gesture.GestureSignal;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.DistanceType;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import greta.core.util.time.Timer;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class Feedbacks implements CallbackPerformer, FeedbackEmitter, SignalPerformer, IntentionPerformer, SignalEmitter {

     private ArrayList<SignalPerformer> signal_performers = new ArrayList<SignalPerformer>();
     List<Signal> signals = new ArrayList<Signal>();
     

    private class FeedbackThread extends Thread {

        private boolean threadStarted;
        private boolean still_running=false;
        private Map map=new HashMap();  
        public CharacterManager charactermanager;
        private List<Boolean> li= new ArrayList<Boolean>();

        public FeedbackThread(CharacterManager cm) {
            this.setDaemon(true);
            threadStarted = true;
            charactermanager=cm;
        }

        public void stopTh() {
            threadStarted = false;
        }
        
        TimeMarker start_timemarker;

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
                            start_timemarker=last_timemarker;

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
               // System.out.println("IS STILL RUNNING:"+listStartedAnimations.size()+"   "+oscOut);
                if(listStartedAnimations.size()>0){
                    still_running=true;
                    charactermanager.setIsrunning(still_running);
                }else{
                    still_running=false;
                    charactermanager.setIsrunning(still_running);
                }
                
                
                li.add(still_running);
                OSCMessage msg = new OSCMessage("/unity/animation_running",li); 
                if(oscOut!=null){
                    sendOSC("/", map);
                    map.put("unity/animation_running", still_running);
                    try {
                        //System.out.println(oscOut+"  "+msg.toString());
                        oscOut.send(msg);
                    } catch (IOException ex) {
                    } catch (OSCSerializeException ex) {
                    }
                }
                
                if(still_running && this.charactermanager.isTouch_computed() && this.charactermanager.getDistance()==DistanceType.INTIMATE){
                    
                    GestureSignal gs = new GestureSignal("1");
                    String[] gesture = this.charactermanager.getTouch_gesture_computed().split("=");
                    gs.setCategory(gesture[0]);
                    gs.setReference(gesture[1]);
                    gs.setStart(start_timemarker);
                    TimeMarker end_timeMarker=start_timemarker;
                    end_timeMarker.setValue(start_timemarker.getValue()+2);
                    gs.setEnd(end_timeMarker);
                    ID id = IDProvider.createID("1");
                    Mode mode = new Mode("blend");
                    for (SignalPerformer performer : signal_performers) {
                         performer.performSignals(signals, id, mode);
                    }
                    
                }
                   
                
            }
            
        }
    }
    
            
    private void sendOSC(String root, Map<String,Boolean> map){
        try {
            for (String key : map.keySet()) {
                final List<Boolean> args = new ArrayList<>();
                args.add(map.get(key));
                OSCMessage msg = new OSCMessage(root+key, args);  
                if(oscOut!=null)
                    oscOut.send(msg);            
            }
        } catch (OSCSerializeException | IOException ex) {
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
    private OSCPortOut oscOut = null;

    public OSCPortOut getOscOut() {
        return oscOut;
    }

    public void setOscOut(OSCPortOut oscOut) {
        this.oscOut = oscOut;
    }

    public int getOscPort() {
        return oscPort;
    }

    public void setOscPort(int oscPort) {
        this.oscPort = oscPort;
    }
    private int oscPort = 9000;   
    

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

    public List<TemporizableList> getListPendingAnimations() {
        return listPendingAnimations;
    }


    public void setListPendingAnimations(List<TemporizableList> listPendingAnimations) {
        this.listPendingAnimations = listPendingAnimations;
    }

    public List<TemporizableList> getListDeadAnimations() {
        return listDeadAnimations;
    }

    public void setListDeadAnimations(List<TemporizableList> listDeadAnimations) {
        this.listDeadAnimations = listDeadAnimations;
    }

    public List<TemporizableList> getListStoppedAnimations() {
        return listStoppedAnimations;
    }

    public void setListStoppedAnimations(List<TemporizableList> listStoppedAnimations) {
        this.listStoppedAnimations = listStoppedAnimations;
    }

    public List<TemporizableList> getListStartedAnimations() {
        return listStartedAnimations;
    }

    public void setListStartedAnimations(List<TemporizableList> listStartedAnimations) {
        this.listStartedAnimations = listStartedAnimations;
    }

    public SpeechSignal getSpeech_sgnl() {
        return speech_sgnl;
    }

    public void setSpeech_sgnl(SpeechSignal speech_sgnl) {
        this.speech_sgnl = speech_sgnl;
    }

    public final void startThread() {
        System.out.println("START FEEDBACK THREAD");
        if (feedbackThread == null || !feedbackThread.isAlive()) {
            feedbackThread = new FeedbackThread(charactermanager);
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
    
        @Override
    public void addSignalPerformer(SignalPerformer sp) {
        signal_performers.add(sp);
    }

    @Override
    public void removeSignalPerformer(SignalPerformer sp) {
        signal_performers.remove(sp);
    }
}
