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
package greta.auxiliary.player.ogre.capture;
        

import greta.core.util.Constants;
import greta.core.util.audio.AudioOutput;
import greta.core.util.time.Timer;
import java.util.LinkedList;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author Andre-Marie Pez
 */
public class RealTimeVideoCapturer implements Runnable, Capturer {

    private Capturable capturable;
    private CaptureOutput captureOutput;
    private boolean stop;
    private boolean alreadyStarted;
    private CaptureListenerListManager captureListeners;
    private String currentId;
    
    private boolean useFixedIndex;

    public RealTimeVideoCapturer() {
        this(null, null);
    }

    public RealTimeVideoCapturer(Capturable capturable, CaptureOutput captureOutput) {
        this.capturable = capturable;
        this.captureOutput = captureOutput;
        stop = false;
        alreadyStarted = false;
        captureListeners = new CaptureListenerListManager();
    }

    @Override
    public void run() {
        AudioOutput audioOutput = new AudioOutput() {
                @Override
                public void setCurrentAudio(byte[] bytes) {
                    audiBlocks.add(new AudioBlock(bytes, Timer.getTimeMillis()));
                }
            };
        synchronized (capturable) {
            long startTime = Timer.getTimeMillis();
            captureListeners.notifyCaptureStarted(this, startTime);
            capturable.prepareCapture();
            captureOutput.begin(capturable.getCaptureWidth(), capturable.getCaptureHeight(), startTime, currentId);
            //greta.core.util.audio.Mixer.requestNotBlocking();
            //capturable.getCamera().getMic().addAudioOutput(audioOutput);
        }
        while (!stop && !capturable.isSizeChanged()) {
            synchronized (capturable) {
                long currentTime = Timer.getTimeMillis();
                byte[] image = capturable.getCaptureData();
                flushAudiTo(currentTime);
                captureOutput.newFrame(image, currentTime, useFixedIndex);
                flushAudio();
                captureListeners.notifyCaptureNewFrame(this, currentTime);
                try {
                    Thread.sleep(Math.max(0, Constants.FRAME_DURATION_MILLIS + currentTime - Timer.getTimeMillis()));
                } catch (Exception ex) {
                }
            }
        }
        //capturable.getCamera().getMic().removeAudioOutput(audioOutput);
        greta.core.util.audio.Mixer.releaseNotBlocking();
        flushAudio();
        captureOutput.end();
        capturable.getCamera().getMic().startPlaying();
        captureListeners.notifyCaptureEnded(this, Timer.getTimeMillis());
        alreadyStarted = false;
        System.gc();
    }

    @Override
    public void startCapture(String id, boolean useFixedIndexLocal) {
        useFixedIndex = useFixedIndexLocal;
        if (!alreadyStarted && capturable != null && captureOutput != null) {
            alreadyStarted = true;
            stop = false;
            currentId = id;
//            captureOutput.setBaseFileName(currentId);
            new Thread(() -> {
            startServer(); // This will start the server and wait for a client connection to send the boolean
                }).start();
            Thread t = new Thread(this);
            t.start();
        }
    }

    @Override
    public void stopCapture() {
        stop = true;
    }

    private void stopAndWait() {
        stopCapture();
        while (alreadyStarted) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void setCapturable(Capturable capturable) {
        stopAndWait();
        this.capturable = capturable;
    }

    @Override
    public void setCaptureOutput(CaptureOutput captureOutput) {
        stopAndWait();
        this.captureOutput = captureOutput;
    }

    @Override
    public void addCaptureListener(CaptureListener captureListener) {
        captureListeners.add(captureListener);
    }

    @Override
    public void removeCaptureListener(CaptureListener captureListener) {
        captureListeners.remove(captureListener);
    }
    private LinkedList<AudioBlock> audiBlocks = new LinkedList<AudioBlock>();

    private class AudioBlock {

        byte[] buffer;
        long time;

        private AudioBlock(byte[] buffer, long timeMillis) {
            this.buffer = buffer;
            time = timeMillis;
        }
    }

    private void flushAudio() {
        if (captureOutput != null) {
            while (!audiBlocks.isEmpty()) {
                AudioBlock block = audiBlocks.poll();
                captureOutput.newAudioPacket(block.buffer, block.time);
            }
        }
    }
    private void flushAudiTo(long time) {
        if (captureOutput != null) {
            while (!audiBlocks.isEmpty() && audiBlocks.peek().time<=time) {
                AudioBlock block = audiBlocks.poll();
                captureOutput.newAudioPacket(block.buffer, block.time);
            }
        }
    }
    
    public void startServer() {
        
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server started, waiting for connections...");
            try (Socket clientSocket = serverSocket.accept();
                 DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
                System.out.println("Client connected, sending boolean...");
                out.writeBoolean(true); // Assuming you're sending true; adjust as needed
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
