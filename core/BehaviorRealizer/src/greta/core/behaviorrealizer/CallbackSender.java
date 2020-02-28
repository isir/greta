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
package greta.core.behaviorrealizer;

import greta.core.feedbacks.Callback;
import greta.core.feedbacks.CallbackEmitter;
import greta.core.feedbacks.CallbackPerformer;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public class CallbackSender implements CallbackEmitter {

    private ArrayList<CallbackPerformer> callbackPerfList;
    private List<AnimationTiming> animPendingList;
    private List<AnimationTiming> animStartedList;
    private CallbackThread callbackThread;

    public class AnimationTiming {

        private ID Id;
        private double absoluteStartTime;
        private double absoluteEndTime;

        private AnimationTiming(ID requestId, double absoluteStartTime, double absoluteEndTime) {
            this.Id = requestId;
            this.absoluteStartTime = absoluteStartTime;
            this.absoluteEndTime = absoluteEndTime;
        }
    }

    public CallbackSender() {
        callbackPerfList = new ArrayList<CallbackPerformer>();
        animPendingList = new ArrayList<AnimationTiming>();
        animStartedList = new ArrayList<AnimationTiming>();
        callbackThread = new CallbackThread(this);
        startThread();
    }

    public final void startThread() {
        if (callbackThread == null || !callbackThread.isAlive()) {
            callbackThread = new CallbackThread(this);
            callbackThread.start();
        }
    }

    public final void stopThread() {
        if (callbackThread != null) {
            callbackThread.stopTh();
        }
    }

    public synchronized void addAnimation(ID id, double absoluteStartTime, double absoluteEndTime) {
        //System.out.println("Animation added, id: "+id+", start: "+ absoluteStartTime+", end: "+absoluteEndTime);
        double currentTime = greta.core.util.time.Timer.getTime();
        Callback callback = new Callback("", currentTime, id);
        if (currentTime > absoluteStartTime) {
            if (currentTime > absoluteEndTime) {
                callback.setType("dead");
                for (CallbackPerformer perf : callbackPerfList) {
                    perf.performCallback(callback);
                 //System.out.println("Anim dead");
                }
            } else {
                animStartedList.add(new AnimationTiming(id, absoluteStartTime, absoluteEndTime));
                callback.setType("start");
                 for (CallbackPerformer perf : callbackPerfList) {
                                perf.performCallback(callback);
                            }
                 //System.out.println("Anim added to started list");
            }
        } else {
            animPendingList.add(new AnimationTiming(id, absoluteStartTime, absoluteEndTime));
            //System.out.println("Anim added to pending list");
        }
    }

    public AnimationTiming findAnim(ID id) {
        AnimationTiming anim = null;
        int indexAnim = indexOf(id, animPendingList);
        if (indexAnim != -1) {
            anim = animPendingList.get(indexAnim);
        } else {
            indexAnim = indexOf(id, animStartedList);
            if (indexAnim != -1) {
                anim = animStartedList.get(indexAnim);
            }
        }
        return anim;
    }

    public int indexOf(ID id, List<AnimationTiming> animList) {
        int index = -1;
        int i = 0;
        for (AnimationTiming anim : animList) {
            if (anim.Id==id) {
                index = i;
            }
            ++i;
        }
        return index;
    }

    public synchronized void animStopped(ID id) {
        double currentTime = greta.core.util.time.Timer.getTime();
        int indexAnim = indexOf(id, animPendingList);
        Callback callback = new Callback("", currentTime, id);
        if (indexAnim != -1) {
            callback.setType("dead");
            for (CallbackPerformer perf : callbackPerfList) {
                perf.performCallback(callback);
            }
            animPendingList.remove(indexAnim);
        } else {
            indexAnim = indexOf(id, animStartedList);
            if (indexAnim != -1) {
            callback.setType("stopped");
                for (CallbackPerformer perf : callbackPerfList) {
                    perf.performCallback(callback);
                }
                animStartedList.remove(indexAnim);
            }
        }
    }

    public void stopAllAnims(){
        stopAllAnims(animPendingList);
        stopAllAnims(animStartedList);
    }
    private void stopAllAnims(List<AnimationTiming> animList){
        for(AnimationTiming anim : new ArrayList<AnimationTiming>(animList)){
            animStopped(anim.Id);
        }
    }

    private class CallbackThread extends Thread {

        final CallbackSender cbSender;
        private boolean running;

        public CallbackThread(CallbackSender cbSenderToStart) {
            running = false;
            this.setDaemon(true);
            this.cbSender = cbSenderToStart;
        }

        public void stopTh() {
            running = false;
        }

        @Override
        public void run() {
            running = true;
            //System.out.println("Callback thread started");
            ID tempId = IDProvider.createID("temp");
            while (running) {
                synchronized (cbSender) {
                    double currentTime = greta.core.util.time.Timer.getTime();
                    Callback callback = new Callback("", currentTime, tempId);
                    for (Iterator iter = cbSender.animPendingList.iterator(); iter.hasNext();) {
                        AnimationTiming anim = (AnimationTiming) iter.next();
                        callback.setAnimId(anim.Id);
                        if (currentTime > anim.absoluteStartTime) {
                            //System.out.println("Anim added to start list");
                            cbSender.animStartedList.add(anim);
                            callback.setType("start");
                            for (CallbackPerformer perf : callbackPerfList) {
                                perf.performCallback(callback);
                            }
                            iter.remove();
                        }
                    }
                    for (Iterator iter = cbSender.animStartedList.iterator(); iter.hasNext();) {
                        AnimationTiming anim = (AnimationTiming) iter.next();
                        callback.setAnimId(anim.Id);
                        if (currentTime > anim.absoluteEndTime) {
                            //System.out.println("Anim ended");
                            callback.setType("end");
                            for (CallbackPerformer perf : callbackPerfList) {
                                perf.performCallback(callback);
                            }
                            iter.remove();
                        }
                    }
                }
                try {sleep(1);} catch (Exception ex) {}
            }
        }
    }

    @Override
    public void addCallbackPerformer(CallbackPerformer cp) {
        callbackPerfList.add(cp);
    }

    @Override
    public void removeCallbackPerformer(CallbackPerformer cp) {
        callbackPerfList.remove(cp);
    }

}
