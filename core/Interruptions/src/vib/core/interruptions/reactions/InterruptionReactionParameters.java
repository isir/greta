/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.interruptions.reactions;

/**
 *
 * @author Angelo Cafaro
 */
public class InterruptionReactionParameters {

    public static final float MAX_DURATION = 2.0f;

    private float amplitude;
    private float duration;

    public InterruptionReactionParameters(float amplitude, float duration) {
        this.amplitude = amplitude;
        this.duration = duration;
    }

    public InterruptionReactionParameters() {
        this(0.0f, 0.0f);
    }

    public float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "InterruptionReactionParameters{" + "amplitude [" + amplitude + "] duration[" + duration + "]}";
    }

}
