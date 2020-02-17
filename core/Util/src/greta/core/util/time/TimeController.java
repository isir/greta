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
 * @author Andre-Marie Pez
 */
public interface TimeController {

    /**
     * Returns the current time in milliseconds.
     * @return the current time in milliseconds.
     */
    public long getTimeMillis();

    /**
     * Set the current time in milliseconds.
     * @param milliSeconds the current time in milliseconds.
     */
    public void setTimeMillis(long milliSeconds);

    /**
     * Returns the current time in seconds.
     * @return the current time in seconds.
     */
    public double getTime();

    /**
     * Set the current time in seconds.
     * @param seconds the current time in seconds.
     */
    public void setTime(double seconds);

    /**
     * Stops the current thread until the specified duration is elapsed.
     * @param millis time to sleep in milli seconds
     */
    public void sleep(long millis);
}
