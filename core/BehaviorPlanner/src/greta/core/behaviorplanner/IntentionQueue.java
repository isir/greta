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
package greta.core.behaviorplanner;

import greta.core.feedbacks.Callback;
import greta.core.feedbacks.CallbackPerformer;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
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
