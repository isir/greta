/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.ideationalunits;

/**
 *
 * @author Brice Donval
 */
public abstract class Unit {

    private String id;

    /* ---------------------------------------------------------------------- */

    private Unit() {}

    public Unit(String id) {
        this.id = id;
    }

    /* ---------------------------------------------------------------------- */

    public String getId() {
        return id;
    }

}
