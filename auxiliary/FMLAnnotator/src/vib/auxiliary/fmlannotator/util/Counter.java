/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.fmlannotator.util;

/**
 *
 * @author David Panou
 */
public class Counter {

    private Integer value;

    public Counter(int value) {
        this.value = value;
    }

    public void increase() {
        ++value;
    }

    public void decrease() {
        --value;
    }

    public void set(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return value.toString();
    }

}
