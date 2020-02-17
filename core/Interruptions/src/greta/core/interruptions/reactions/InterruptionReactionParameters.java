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
package greta.core.interruptions.reactions;

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
