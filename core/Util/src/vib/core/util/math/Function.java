/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.math;

/**
 * Inteface to embed a mathematical function.
 * @author Andre-Marie Pez
 */
public interface Function {

    /**
     * This is the main method of the mathematical function.<br/>
     * It returns {@code y} for the function {@code y = f(x)}.
     * @param x the input value
     * @return the result of this function
     */
    public double f(double x);

    public String getName();

    /**
     * Returns the {@code Function} corresponding to the derivative of this {@code Function}.<br/>
     * If this function is not differentiable, it returns {@code null}.
     * @return the derivative.
     */
    public Function getDerivative();

    /**
     * Returns a simplification of his function.<br/>
     * Composed functions can be simplified. But if his {@code Function} is to
     * simple or can not be simplified. In this case, this {@code Function} MUST
     * returns {@code this}
     * @return a simplification of his function
     */
    public Function simplified();
}
