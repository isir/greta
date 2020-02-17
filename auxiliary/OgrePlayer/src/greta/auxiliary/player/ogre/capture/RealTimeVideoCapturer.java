/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.player.ogre.capture;

import greta.core.util.Constants;
import greta.core.util.audio.AudioOutput;
import greta.core.util.time.Timer;
import java.util.LinkedList;

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
            greta.core.util.audio.Mixer.requestNotBlocking();
            capturable.getCamera().getMic().addAudioOutput(audioOutput);
        }
        while (!stop && !capturable.isSizeChanged()) {
            synchronized (capturable) {
                long currentTime = Timer.getTimeMillis();
                byte[] image = capturable.getCaptureData();
                flushAudiTo(currentTime);
                captureOutput.newFrame(image, currentTime);
                flushAudio();
                captureListeners.notifyCaptureNewFrame(this, currentTime);
                try {
                    Thread.sleep(Math.max(0, Constants.FRAME_DURATION_MILLIS + currentTime - Timer.getTimeMillis()));
                } catch (Exception ex) {
                }
            }
        }
        capturable.getCamera().getMic().removeAudioOutput(audioOutput);
        greta.core.util.audio.Mixer.releaseNotBlocking();
        flushAudio();
        captureOutput.end();
        captureListeners.notifyCaptureEnded(this, Timer.getTimeMillis());
        alreadyStarted = false;
        System.gc();
    }

    @Override
    public void startCapture(String id) {
        if (!alreadyStarted && capturable != null && captureOutput != null) {
            alreadyStarted = true;
            stop = false;
            currentId = id;
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
}
