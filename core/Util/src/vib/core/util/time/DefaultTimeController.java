/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.time;

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
