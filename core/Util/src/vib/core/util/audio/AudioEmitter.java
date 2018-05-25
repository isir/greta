/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.audio;

/**
 *
 * @author Andre-Marie Pez
 */
public interface AudioEmitter {
    public void addAudioPerformer(AudioPerformer audioPerformer);
    public void removeAudioPerformer(AudioPerformer audioPerformer);
}
