/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.time;

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
