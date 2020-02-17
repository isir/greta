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
 * This {@code TimeController} use the system time.
 * @author Andre-Marie Pez
 */
public class DefaultTimeController implements TimeController{

    /**
     * The offset between the current time and the system one. (in milliseconds)
     */
    private long offset = System.currentTimeMillis();

    @Override
    public long getTimeMillis(){
        return System.currentTimeMillis() - offset;
    }

    @Override
    public void setTimeMillis(long milliSeconds){
        offset = System.currentTimeMillis() - milliSeconds;
    }

    @Override
    public double getTime(){
        return getTimeMillis() / 1000.0;
    }

    @Override
    public void setTime(double seconds){
        setTimeMillis((long)(seconds * 1000.0));
    }

    @Override
    public void sleep(long millis) {
        try {Thread.sleep(millis);} catch (Exception ex) {}
    }

}
