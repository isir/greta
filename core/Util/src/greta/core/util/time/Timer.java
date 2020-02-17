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

import greta.core.util.Constants;

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
     * @see greta.core.util.time.TimeController TimeController
     * @see greta.core.util.time.DefaultTimeController DefaultTimeController
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
