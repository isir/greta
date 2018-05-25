/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.time;

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
