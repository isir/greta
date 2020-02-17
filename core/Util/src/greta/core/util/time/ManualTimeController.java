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
package greta.core.util.time;

/**
 * This {@code TimeController} use a fixed time.
 * @author Andre-Marie Pez
 */
public class ManualTimeController implements TimeController{

    /**
     * The current time. (in milliseconds)
     */
    private long time = 0;

    @Override
    public long getTimeMillis() {
        return time;
    }

    @Override
    public void setTimeMillis(long milliSeconds) {
        time = milliSeconds;
    }

    @Override
    public double getTime() {
        return time/1000.0;
    }

    @Override
    public void setTime(double seconds) {
        time = (long)(seconds*1000.0);
    }

    /**
     * Increases or decreases the time by a specified value in milliseconds.<br/>
     * Use a positive value to increase, or a negative value to decrease.
     * @param milliSeconds the number of milliseconds
     */
    public void stepMillis(long milliSeconds){
        if(milliSeconds<0){
            throw new IllegalArgumentException("\"Back to the Future\" is a movie!");
        }
        time += milliSeconds;
    }

    /**
     * Increases or decreases the time by a specified value in seconds.<br/>
     * Use a positive value to increase, or a negative value to decrease.
     * @param seconds the number of seconds
     */
    public void step(double seconds){
        stepMillis((long)(seconds*1000.0));
    }

    @Override
    public void sleep(long millis) {
        long now = Timer.getTimeMillis();
        long sleepTo = now+millis;
        while(now<sleepTo){
            try {Thread.sleep(1);} catch (Exception ex) {}
            now = Timer.getTimeMillis();
        }
    }
}
