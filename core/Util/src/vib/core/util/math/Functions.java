/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.math;

/**
 * contains some usfull functions
 * @author Andre-Marie Pez
 */
public class Functions {

    /**
     * Convert x from the interval [a b] to x' in the interval [c d]
     * @param x original value
     * @param a inferior bound of the original interval
     * @param b supperior bound of the original interval
     * @param c inferior bound of the new interval
     * @param d supperior bound of the new interval
     * @return the new value x'
     */
    public static double changeInterval(double x, double a, double b, double c, double d){
        return a==b ? c : (x-a)/(b-a) * (d-c) + c;
    }
}
