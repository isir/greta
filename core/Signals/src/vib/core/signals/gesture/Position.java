/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.signals.gesture;

/**
 * This interface was designed to encapsulate different way for describing a position
 * @author Brian Ravenet
 */
public interface Position {
    public double getX();
    public double getY();
    public double getZ();
    public void setX(double x);
    public void setY(double y);
    public void setZ(double z);
    public void applySpacial(double spc);
    public String getStringPosition();
    public Position getCopy();
}
