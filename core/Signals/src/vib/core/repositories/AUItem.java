/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.repositories;

import vib.core.util.enums.Side;
import vib.core.util.log.Logs;

/**
 * This is one Action Unit
 *
 * @author Radoslaw Niewiadomski
 */
public class AUItem {

    private int numero;
//    private boolean isitkeyframe;
    // the % of the 100 AU in a given expression.
    //e.g. an expression may have (by definition) a very small frown i.e. AU4 with intensity 0.2 = 20% of maximal AU4
    private double intensity;
    private Side side;

//    public void setKeyframeStatus(boolean value) {
//        isitkeyframe = value;
//    }
//
//    public boolean getKeyframeStatus() {
//        return isitkeyframe;
//    }

    /**
     * @deprecated use AUItem(int numero) where numero is the numero of AU
     * (easier to manipulate)
     */
    public AUItem(String name) {
        this(name, 1, Side.BOTH, false);
    }

    public AUItem(int num) {
        this(num, 1, Side.BOTH, false);
    }

    /**
     *
     * @deprecated use AUItem(int numero) where numero is the numero of AU
     * (easier to manipulate)
     */
    public AUItem(String name, double intensity, Side side) {
        this(name, intensity, side, false);
    }

    public AUItem(int numero, double intensity, Side side) {
        this(numero, intensity, side, false);
    }

    /**
     * @deprecated use AUItem(int numero) where numero is the numero of AU
     * (easier to manipulate)
     */
    public AUItem(String name, double intensity, boolean value) {
        this(name, intensity, Side.BOTH, value);
    }

    public AUItem(int numero, double intensity, boolean value) {
        this(numero, intensity, Side.BOTH, value);
    }

    /**
     *
     * @param name
     * @param intensity
     * @param value
     * @deprecated use AUItem(int numero) where numero is the numero of AU
     * (easier to manipulate)
     */
    public AUItem(String name, double intensity, Side side, boolean value) {
        int length = name.length();
        this.numero = Integer.parseInt(name.substring(2, length));
        if ((intensity > 1.0) || (intensity < 0.0)) {
            Logs.warning("AU " + name + " intensity value is not correct: " + intensity);
            //throw an exception or write to log??
            this.intensity = Math.min(1, Math.max(0, intensity));
        }
        this.intensity = intensity;
//        isitkeyframe = value;
        this.side = side;
    }

    public AUItem(int numero, double intensity, Side side, boolean value) {
        this.numero = numero;
        if ((intensity > 1.0) || (intensity < 0.0)) {
            Logs.warning("AU" + numero + " intensity value is not correct: " + intensity);
            //throw an exception or write to log??
            this.intensity = Math.min(1, Math.max(0, intensity));
        }
        this.intensity = intensity;
//        isitkeyframe = value;
        this.side = side;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public String getAU() {
        String name = "AU" + numero;
        return name;
    }

    public int getAUnum() {
        return numero;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public boolean equals(AUItem other) {
        return numero == other.numero && intensity == other.intensity && side == other.side;
    }
}

