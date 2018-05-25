/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.time;

import vib.core.util.Constants;

/**
 * This class contains static methods to get ans set the current global time.
 * @author Andre-Marie Pez
 */
public class Timer {

    private static TimeController controller;
    private static long minSleepDuration;

    static {
        controller = new DefaultTimeController();
//        long before = getTimeMillis();
//        try {Thread.sleep(1);} catch (Exception ex) {}
//        long after = getTimeMillis();
//        minSleepDuration = after - before;
    }

    /**
     * Returns the current time in milliseconds.
     * @return the current time in milliseconds.
     */
    public static long getTimeMillis(){
        return controller.getTimeMillis();
    }

    /**
     * Set the current time in milliseconds.
     * @param milliSeconds the current time in milliseconds.
     */
    public static void setTimeMillis(long milliSeconds){
        controller.setTimeMillis(milliSeconds);
    }
   /**
     * Returns the current frame number.
     * @return the current frame number.
     */
    public static int getCurrentFrameNumber(){
        return (int)(controller.getTime()*Constants.FRAME_PER_SECOND);
    }


    /**
     * Returns the current time in seconds.
     * @return the current time in seconds.
     */
    public static double getTime(){
        return controller.getTime();
    }

    /**
     * Set the current time in seconds.
     * @param seconds the current time in seconds.
     */
    public static void setTime(double seconds){
        controller.setTime(seconds);
    }

    /**
     * Set the {@code TimeController} of this global {@code Timer}.<br/>
     * If the specified {@code TimeController} is {@code null}
     * then a {@code DefaultTimeController} is set.
     * @param timeController the {@code TimeController} to use.
     * @see vib.core.util.time.TimeController TimeController
     * @see vib.core.util.time.DefaultTimeController DefaultTimeController
     */
    public static void setTimeController(TimeController timeController){
        if(timeController==null){
            timeController = new DefaultTimeController();
        }
        timeController.setTimeMillis(getTimeMillis());
        controller = timeController;
    }

    public static void sleep(long millis){
        controller.sleep(millis);
    }

}
