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
package greta.core.intentplanner.listener;

import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.signals.HeadSignal;
import greta.core.signals.ParametricSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.log.Logs;
import greta.core.util.time.TimeMarker;
import greta.core.util.time.Timer;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Andre-Marie
 * @author Elisabetta Bevacqua
 */
public class ListenerIntentPlanner extends CharacterDependentAdapter implements IntentionEmitter, SignalEmitter, SignalPerformer, CharacterDependent {

    public static final String BACKCHANNEL_SET_NAME = "listener_function";
    private static final long SLEEP_TIME = 50; //miliiseconds
    private static final double MAX_DURATION_WHEN_UNKNOWN = 3; //seconds
    private static final long SILENCE_THRESHOLD = 2000 /* 6000 comes from old code but it looks like a little bit long */; //miliseconds

    private boolean speakerPresent;
    private long stopSpeaking;
    private BackchannelTrigger btr;
    private final LinkedList<EmbededSignal> currentUserSignals;
    private final LinkedList<EmbededSignal> pendingUserSignals;
    private Thread listenLoop;
    private boolean isListening;

    public ListenerIntentPlanner(CharacterManager cm) {
        setCharacterManager(cm);
        btr = new BackchannelTrigger();
        stopSpeaking = -1;
        currentUserSignals = new LinkedList<EmbededSignal>();
        pendingUserSignals = new LinkedList<EmbededSignal>();
        getCharacterManager().add(this);
        speakerPresent = true; // needs to be detected
        startListening();
    }

    private void startListening(){
        isListening = true;
        listenLoop = new Thread(new Runnable() {

            @Override
            public void run() {
                while(isListening){
                    listen();
                    Timer.sleep(SLEEP_TIME);
                }
            }
        });
        listenLoop.setDaemon(true);
        listenLoop.start();
    }

    /* copy of old code
    public void react() {
        //<editor-fold defaultstate="collapsed" desc="semaine stuffs">
        // if you receive a state message
            SEMAINEStateMessage ssm = (SEMAINEStateMessage) m;
            String type = ssm.getDatatype();
            StateInfo stateinfo = ssm.getState();

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="User states">
            // Information about the user's behaviours
            if (type.equals("UserState")) {
                if (stateinfo.hasInfo("headGesture")) {
                    UserSignal sig = new UserSignal("head", stateinfo.getInfo("headGesture"));
                    currentUserSignals.add(sig);
                }

                if (stateinfo.hasInfo("facialActionUnits")) {
                    String auList = stateinfo.getInfo("facialActionUnits");
                    String[] actionUnits = auList.split(" ");
                    for (String au : actionUnits) {
                        if (!au.equals("0")) {
                            UserSignal sig = new UserSignal("face", "AU" + au);
                            currentUserSignals.add(sig);
                        }
                    }
                }

                if (stateinfo.hasInfo("pitchDirection")) {
                    UserSignal sig = new UserSignal("speech", stateinfo.getInfo("pitchDirection"));
                    currentUserSignals.add(sig);
                }

                if (stateinfo.hasInfo("speaking")) {
                    String speakingState = stateinfo.getInfo("speaking");
                    if (speakingState.equalsIgnoreCase("true")) {
                        stopSpeaking = 0;
                    } else {
                        stopSpeaking = System.currentTimeMillis();
                    }
                }
            }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Context states">
            // Information about the context
            if (type.equals("ContextState")) {
                if (stateinfo.hasInfo("userPresent")) {
                    String up = stateinfo.getInfo("userPresent");
                    if (up.equals("false")) {
                        speakerPresent = false;
                    } else {
                        speakerPresent = true;
                    }
                }
            }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Agent states">
            // information about the agent's communicative intention (it modifies the agent state)
            if (type.equals("AgentState")) {
                if (stateinfo.hasInfo("agreement")) {
                    double value = Double.parseDouble(stateinfo.getInfo("agreement"));
                    state.modifyIntentions("agreement", 0);
                    state.modifyIntentions("disagreement", 0);
                    if (value > 0.5) {
                        state.modifyIntentions("agreement", (value - 0.5) * 2);
                    } else if (value < 0.5) {
                        state.modifyIntentions("disagreement", Math.abs(value - 0.5) * 2);
                    }
                }

                if (stateinfo.hasInfo("acceptance")) {
                    double value = Double.parseDouble(stateinfo.getInfo("acceptance"));
                    state.modifyIntentions("acceptance", 0);
                    state.modifyIntentions("refuse", 0);
                    if (value > 0.5) {
                        state.modifyIntentions("acceptance", (value - 0.5) * 2);
                    } else if (value < 0.5) {
                        state.modifyIntentions("refuse", Math.abs(value - 0.5) * 2);
                    }
                }

                if (stateinfo.hasInfo("belief")) {
                    double value = Double.parseDouble(stateinfo.getInfo("belief"));
                    state.modifyIntentions("belief", 0);
                    state.modifyIntentions("disbelief", 0);
                    if (value > 0.5) {
                        state.modifyIntentions("belief", (value - 0.5) * 2);
                    } else if (value < 0.5) {
                        state.modifyIntentions("disbelief", Math.abs(value - 0.5) * 2);
                    }
                }

                if (stateinfo.hasInfo("liking")) {
                    double value = Double.parseDouble(stateinfo.getInfo("liking"));
                    state.modifyIntentions("liking", 0);
                    state.modifyIntentions("disliking", 0);
                    if (value > 0.5) {
                        state.modifyIntentions("liking", (value - 0.5) * 2);
                    } else if (value < 0.5) {
                        state.modifyIntentions("disliking", Math.abs(value - 0.5) * 2);
                    }
                }

                if (stateinfo.hasInfo("interest")) {
                    double value = Double.parseDouble(stateinfo.getInfo("interest"));
                    state.modifyIntentions("interest", 0);
                    state.modifyIntentions("no_interest", 0);
                    if (value > 0.5) {
                        state.modifyIntentions("interest", (value - 0.5) * 2);
                    } else if (value < 0.5) {
                        state.modifyIntentions("no_interest", Math.abs(value - 0.5) * 2);
                    }
                }

                if (stateinfo.hasInfo("understanding")) {
                    double value = Double.parseDouble(stateinfo.getInfo("understanding"));
                    state.modifyIntentions("understanding", 0);
                    state.modifyIntentions("no_understanding", 0);
                    if (value > 0.5) {
                        state.modifyIntentions("understanding", (value - 0.5) * 2);
                    } else if (value < 0.5) {
                        state.modifyIntentions("no_understanding", Math.abs(value - 0.5) * 2);
                    }
                }

                if (stateinfo.hasInfo("anticipation")) {
                    double value = Double.parseDouble(stateinfo.getInfo("anticipation"));
                    state.modifyIntentions("high-anticipation", 0);
                    state.modifyIntentions("low-anticipation", 0);
                    if (value > 0.5) {
                        state.modifyIntentions("high-anticipation", (value - 0.5) * 2);
                    } else if (value < 0.5) {
                        state.modifyIntentions("low-anticipation", Math.abs(value - 0.5) * 2);
                    }
                }

                if (stateinfo.hasInfo("solidarity")) {
                    double value = Double.parseDouble(stateinfo.getInfo("solidarity"));
                    state.modifyIntentions("high-solidarity", 0);
                    state.modifyIntentions("low-solidarity", 0);
                    if (value > 0.5) {
                        state.modifyIntentions("high-solidarity", (value - 0.5) * 2);
                    } else if (value < 0.5) {
                        state.modifyIntentions("low-solidarity", Math.abs(value - 0.5) * 2);
                    }
                }

                if (stateinfo.hasInfo("antagonism")) {
                    double value = Double.parseDouble(stateinfo.getInfo("antagonism"));
                    state.modifyIntentions("high-antagonism", 0);
                    state.modifyIntentions("low-antagonism", 0);
                    if (value > 0.5) {
                        state.modifyIntentions("high-antagonism", (value - 0.5) * 2);
                    } else if (value < 0.5) {
                        state.modifyIntentions("low-antagonism", Math.abs(value - 0.5) * 2);
                    }
                }

                if (stateinfo.hasInfo("anger")) {
                    state.modifyIntentions("anger", Double.parseDouble(stateinfo.getInfo("anger")));
                }

                if (stateinfo.hasInfo("sadness")) {
                    state.modifyIntentions("sadness", Double.parseDouble(stateinfo.getInfo("sadness")));
                }

                if (stateinfo.hasInfo("amusement")) {
                    state.modifyIntentions("amusement", Double.parseDouble(stateinfo.getInfo("amusement")));
                }

                if (stateinfo.hasInfo("happiness")) {
                    state.modifyIntentions("happiness", Double.parseDouble(stateinfo.getInfo("happiness")));
                }

                if (stateinfo.hasInfo("contempt")) {
                    state.modifyIntentions("contempt", Double.parseDouble(stateinfo.getInfo("contempt")));
                }
            }
            //</editor-fold>
    }
    //*/
    public void listen() {
        //move current Signals from peding to current list
        long pendingStopSpeaking = -1;
        synchronized (pendingUserSignals){
            ListIterator<EmbededSignal> pending = pendingUserSignals.listIterator();
            while (pending.hasNext()) {
                EmbededSignal aSignal = pending.next();
                double start = aSignal.getStart();
                double end = aSignal.getEnd();
                if(start <= Timer.getTime()){
                    if(aSignal.getSignal() instanceof SpeechSignal){
                        pendingStopSpeaking = Math.max(pendingStopSpeaking, ((long)(end*1000)));
                        //specific check for some instantaneous speech information
                        //(silences should be kept in the loop, but not pitch changes for instance)
                        if(isInstantaneous (aSignal))
                        {
                            pending.remove();
                            if(end <= Timer.getTime()){
                                synchronized (currentUserSignals){
                                    currentUserSignals.add(aSignal);
                                }
                            }
                        }
                        else
                        {
                            if(end <= Timer.getTime()){
                                pending.remove();
                            }
                        }
                    }
                    else{
                        pending.remove();
                        if(end <= Timer.getTime()){
                            synchronized (currentUserSignals){
                                currentUserSignals.add(aSignal);
                            }
                        }
                    }
                }
            }
        }

        //speech time may be changed (interruption)
        if(pendingStopSpeaking == -1 && stopSpeaking>Timer.getTimeMillis()){
            stopSpeaking = Timer.getTimeMillis();
        }
        else{
            stopSpeaking = Math.max(pendingStopSpeaking, stopSpeaking);
        }

        if (stopSpeaking != -1 && (Timer.getTimeMillis() - stopSpeaking) > SILENCE_THRESHOLD) {
            Logs.debug("silence at = "+Timer.getTimeMillis());
            SpeechSignal sig = new SpeechSignal(getCharacterManager());
            sig.setReference("silence");
            EmbededSignal es = new EmbededSignal(sig);
            es.receivedTime = stopSpeaking/1000.0;
            synchronized (currentUserSignals){
                currentUserSignals.add(es);
            }
            stopSpeaking = -1;
        }

        if (! currentUserSignals.isEmpty() && speakerPresent) {
            btr.findAndTrigger(currentUserSignals);
        }

        //TODO: remove signal used to trigger backchanels ?
        //according to the old code, Signals was suppressed after each loop
        //It's not a good thing in this architecture but the rules are not adapted for keeping Signals
        currentUserSignals.clear(); // it make the next lines useless but it's temporary solution

        //clean the current inputs signals
        synchronized (currentUserSignals){
            ListIterator<EmbededSignal> current = currentUserSignals.listIterator();
            while (current.hasNext()) {
                EmbededSignal aSignal = current.next();
                double end = aSignal.getEnd();
                if (Timer.getTime() > end) {
                    current.remove();
                }
            }
        }
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        btr.addIntentionPerformer(ip);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer ip) {
        btr.removeIntentionPerformer(ip);
    }

    @Override
    public void addSignalPerformer(SignalPerformer sp) {
        btr.addSignalPerformer(sp);
    }

    @Override
    public void removeSignalPerformer(SignalPerformer sp) {
        btr.removeSignalPerformer(sp);
    }

    @Override
    public void performSignals(List<Signal> list, ID requestId, Mode mode) {
        for(Signal signal : list) {
        //    System.out.println("   add(signal) "+signal.getId()+" modality "+signal.getModality()
        //            +" ref "+((SpeechSignal)signal).getReference());
            addSignal(signal);
        }
    }

    public void setSpeakerPresence(boolean speakerIsPresent){
        speakerPresent = speakerIsPresent;
    }

    public void setEmotionIntentionOfAgentAppreciation(String emotionIntentionOfAgentAppreciation){
        btr.setEmotionIntentionOfAgentAppreciation(emotionIntentionOfAgentAppreciation);
    }

    private void addSignal(Signal signal){
        //does it means that the speaker is present ? then speakerPresent must be set to true
        synchronized (pendingUserSignals){
            pendingUserSignals.add(new EmbededSignal(signal));
         //   System.out.println("   embededsignal "+pendingUserSignals.getLast().getId()
         //           +" modality "+pendingUserSignals.getLast().getModality()+" ref " +
        //            pendingUserSignals.getLast().getReference());
        }
    }

    @Override
    public void onCharacterChanged() {
        btr.onCharacterChanged();
    }

    @Override
    protected void finalize() throws Throwable {
        isListening = false;
        getCharacterManager().remove(this);
        super.finalize();
    }

    private class EmbededSignal extends ProtoSignal{

        Signal embeded;
        double receivedTime;

        public EmbededSignal(Signal toEmbed){
            super(toEmbed.getModality(), "");
            if(toEmbed instanceof ParametricSignal){
                if(toEmbed instanceof HeadSignal){
                    this.setReference(((HeadSignal)toEmbed).getLexeme());
                } else {
                    this.setReference(((ParametricSignal)toEmbed).getReference());
                }
            }
            if(toEmbed instanceof SpeechSignal){
                this.setReference(((SpeechSignal)toEmbed).getReference());
            }
            embeded = toEmbed;
            receivedTime = Timer.getTime();
        }

        public String getId(){
            return embeded.getId();
        }

        public double getStart(){
            TimeMarker start = embeded.getStart();
            return receivedTime + (start.isConcretized() ? start.getValue() : 0 /* what else? */);
        }

        public double getEnd(){
            TimeMarker end = embeded.getEnd();
            return  end.isConcretized() ? receivedTime + end.getValue() : getStart() + MAX_DURATION_WHEN_UNKNOWN /* what else? */;
        }
        public Signal getSignal(){
            return embeded;
        }
    }

    private boolean isInstantaneous(EmbededSignal aSignal){

        if (aSignal.getReference().equalsIgnoreCase("rise")
                || aSignal.getReference().equalsIgnoreCase("fall")
                || aSignal.getReference().equalsIgnoreCase("rise-fall")
                || aSignal.getReference().equalsIgnoreCase("fall-rise")
                || aSignal.getReference().equalsIgnoreCase("appreciation-positive")
                || aSignal.getReference().equalsIgnoreCase("appreciation-negative")) {
            return true;
        }

        return false;
    }
}
