/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.TimeFunctions;

/**
 *
 * @author Jing Huang
 */
public interface TimeFunction {
    /*
     * input parameter: 0 --- 1
     * output: 0 --- 1
     * change mapping of time
     *        1
     *  1|   /
     *   |  /
     *   | /
     *  0|/___1______
     */
    public abstract double getTime(double original);
}
