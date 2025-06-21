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
package greta.core.behaviorplanner;

import greta.core.feedbacks.Callback;
import greta.core.feedbacks.CallbackPerformer;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.signals.Signal;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class IntentionQueue implements IntentionPerformer, IntentionEmitter, CallbackPerformer{

    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    private LinkedList<Request> requestQueue = new LinkedList<Request>();

    @Override
    public synchronized void performIntentions(List<Intention> list, ID id, Mode mode) {
        requestQueue.add(new Request(id, list, mode));
        if(requestQueue.size() == 1){
            sendFirst();
        }
    }
    
    @Override
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode, List<Signal> inputSignals){
        
    };
    
    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        if(ip != null && ip != this){
            performers.add(ip);
        }
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer ip) {
        if(ip != null){
            performers.remove(ip);
        }
    }

    @Override
    public void performCallback(Callback clbck) {
        if(!requestQueue.isEmpty()){
            Request first = requestQueue.peek();
            if(clbck.animId() == first.id && (clbck.type().equals("end") || clbck.type().equals("stoped") || clbck.type().equals("dead"))){
                synchronized(this){
                    requestQueue.removeFirst();
                }
                sendFirst();
            }
        }
    }

    private void sendFirst(){
        if(!requestQueue.isEmpty()){
            final Request first = requestQueue.peek();
            for(final IntentionPerformer performer : performers){
                new Thread(this.getClass().getSimpleName()+" to "+performer.getClass().getSimpleName()){
                    public void run(){
                        performer.performIntentions(first.intentions, first.id, first.mode);
                    }
                }.start();
            }
        }
    }

    private class Request {
        private ID id;
        private List<Intention> intentions;
        private Mode mode;
        Request(ID id, List<Intention> intentions, Mode mode){
            this.id = id;
            this.intentions = intentions;
            this.mode = mode;
        }
    }

}
