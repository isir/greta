/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.enums;

/**
 * Open set item for BML influences (specific to VIB).
 * @author Mathieu Chollet
 */


public enum Influence {
    EYES/*(0)*/,
    HEAD/*(1)*/,
    SHOULDER/*(2)*/,
    TORSO/*(3)*/,
    WHOLE/*(4)*/;

    /* useless: look at ordinal() method ...

    public final int order;

    private Influence(int order) {
        this.order=order;
    }
   //*/
}
