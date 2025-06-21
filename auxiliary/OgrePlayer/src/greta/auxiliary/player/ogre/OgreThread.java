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
package greta.auxiliary.player.ogre;

import java.util.LinkedList;

/**
 *
 * @author Andre-Marie Pez
 */
public class OgreThread extends Thread {

    private static int count = 0;
    private LinkedList<Callback> callbacks;
    private static OgreThread singleton;
    private boolean run = true;

    public static final Callback WAIT = new Callback() {
        @Override
        public void run() {
            try {
                sleep(5);
            } catch (Exception e) {}
        }
    };

    public static final Callback NO_WAIT = new Callback() {
        @Override
        public void run() {}
    };

    public static OgreThread getSingleton() {
        if (singleton == null) {
            singleton = new OgreThread();
            singleton.start();
        }
        return singleton;
    }

    private Callback waitCallBack = null;

    private OgreThread() {
        super(OgreThread.class.getName()+"-" + (count++));
        this.setDaemon(true); //kills this thread when all non-daemon are finished
        callbacks = new LinkedList<Callback>();
        waitCallBack = WAIT;
    }

    @Override
    public void run() {
        while (run) {
            Callback cb = null;
            synchronized (callbacks) {
                if (!callbacks.isEmpty()) {
                    cb = callbacks.remove();
                }
            }
            if(cb !=null){
                synchronized(cb){
                    cb.execute();
                    cb.notify();
                }
            }
            else {
                waitCallBack.run();
            }
        }
    }

    public void shutdown() {
        run = false;
    }

    public void setWaitCallback(Callback callback) {
        if (callback == null) {
            waitCallBack = WAIT;
        } else {
            waitCallBack = callback;
        }
    }

    public Callback getWaitCallback(){
        return waitCallBack;
    }

    public void call(Callback callback) {
        _call(callback, false);
    }

    public void callSync(Callback callback) {
        _call(callback, true);
    }

    private void _call(Callback callback, boolean sync) {
        if (Thread.currentThread() == this) {
            callback.execute();
        } else {
            synchronized(callback){
                synchronized (callbacks) {
                    callbacks.add(callback);
                }
                if (sync) {
                    try {callback.wait();} catch (Exception ex) {}
                }
            }
        }
    }

    public static abstract class Callback implements Runnable{
        protected void execute() {
            Callback.this.run();
        }
    }
}
