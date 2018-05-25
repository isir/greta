/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes;

import vib.core.util.audio.Audio;
import vib.core.util.speech.Speech;
import javax.sound.sampled.AudioFormat;
import vib.core.util.laugh.Laugh;

/**
 *
 * @author Andre-Marie Pez
 */
public class AudioKeyFrame implements Keyframe {

    private String id;
    private double onset;
    private double offset;
    private final Audio audio;

    public AudioKeyFrame(Speech speech) {
        this(
                speech.getId() + "_audio",
                speech.getAudio(),
                speech.getStart().getValue());
    }

    public AudioKeyFrame(Laugh laugh) {
        this(
                laugh.getId() + "_audio",
                laugh.getAudio(),
                laugh.getStart().getValue());
    }

    public AudioKeyFrame(String identifier, Audio audio, double startTime) {
        id = identifier;
        this.audio = audio;
        offset = startTime;
        onset = 0;
    }

    public byte[] getBuffer() {
        return audio.getBuffer();
    }

    public AudioFormat getAudioFormat() {
        return audio.getFormat();
    }

    public Audio getAudio() {
        return audio;
    }

    @Override
    public double getOffset() {
        return offset;
    }

    @Override
    public void setOffset(double time) {
        offset = time;
    }

    @Override
    public double getOnset() {
        return onset;
    }

    @Override
    public void setOnset(double time) {
        onset = time;
    }

    @Override
    public String getModality() {
        return "speech";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPhaseType() {          //??????
        return "audio";
    }

    @Override
    public String getCategory() {           //??????
        return "audio";
    }

    public String getTrajectoryType() {     //??????
        return "audio";
    }

    public double getDuration() {
        if (audio == null) {
            return 0;
        }
        return audio.getDuration();
    }
}
