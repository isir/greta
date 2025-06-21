/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.auxiliary.activemq.semaine;

import greta.core.feedbacks.Callback;
import greta.core.feedbacks.CallbackEmitter;
import greta.core.feedbacks.CallbackPerformer;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.signals.Signal;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Andre-Marie Pez
 */
public class IntentionQueuing implements IntentionPerformer, IntentionEmitter, SemaineCommandPerformer, CallbackEmitter {

    private ArrayList<IntentionPerformer> intentionPerformers;
    private ArrayList<CallbackPerformer> callbacksPerformers;
    private final LinkedList<IntentionAnimation> anims = new LinkedList<IntentionAnimation>();
    private boolean thisIsTheEnd;

    public IntentionQueuing() {
        intentionPerformers = new ArrayList<IntentionPerformer>();
        callbacksPerformers = new ArrayList<CallbackPerformer>();
        thisIsTheEnd = false;

        Thread queuProcess = new Thread() {
            @Override
            public void run() {
                while (!thisIsTheEnd) {
                    synchronized (anims) {
                        ListIterator<IntentionAnimation> iterator = anims.listIterator();
                        while (iterator.hasNext()) {
                            IntentionAnimation anim = iterator.next();
                            if (anim.startAt < Timer.getTimeMillis()) {
                                iterator.remove();
                                for (IntentionPerformer performer : intentionPerformers) {
                                    performer.performIntentions(anim.intentions, anim.id, anim.mode);
                                }
                            }
                        }
                    }
                    try {
                        sleep(5);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        };
        queuProcess.setDaemon(true);
        queuProcess.start();
    }

    @Override
    public void performIntentions(List<Intention> intentions, ID id, Mode mode) {
        synchronized (anims) {
            anims.add(new IntentionAnimation(intentions, id, mode));
        }
        for (CallbackPerformer performer : callbacksPerformers) {
            performer.performCallback(new Callback("ready", Timer.getTime(), id));
        }
    }

    @Override
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode, List<Signal> inputSignals){
        
    };    
    
    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        if (ip != null && ip != this) {
            intentionPerformers.add(ip);
        }
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer ip) {
        if (ip != null && intentionPerformers.contains(ip)) {
            intentionPerformers.remove(ip);
        }
    }

    @Override
    public void performDataInfo(boolean hasAudio, boolean hasFAP, boolean hasBAP, String requestID) {
        //we don't use it
        //Logs.debug("dataInfo : "+requestID+" "+hasAudio+" "+hasFAP+" "+hasBAP);
    }

    @Override
    public void performPlayCommand(long startAt, long lifeTime, double priority, String requestID) {
        //we just need the startAt
        //Logs.debug("playCommand : "+requestID+" "+startAt+" "+lifeTime+" "+priority);
        synchronized (anims) {
            for (IntentionAnimation anim : anims) {
                if (anim.id.equals(requestID)) {
                    anim.startAt = startAt;
                }
            }
        }
    }

    @Override
    public void addCallbackPerformer(CallbackPerformer cp) {
        if (cp != null && cp != this) {
            callbacksPerformers.add(cp);
        }
    }

    @Override
    public void removeCallbackPerformer(CallbackPerformer cp) {
        if (cp != null && callbacksPerformers.contains(cp)) {
            callbacksPerformers.remove(cp);
        }
    }

    private class IntentionAnimation {

        List<Intention> intentions;
        ID id;
        Mode mode;
        long startAt;

        private IntentionAnimation(List<Intention> intentions, ID id, Mode mode) {
            this.intentions = intentions;
            this.id = id;
            this.mode = mode;
            startAt = Long.MAX_VALUE;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        thisIsTheEnd = false;
        super.finalize();
    }
}
