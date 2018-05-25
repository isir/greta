/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.aulibrary;

import java.awt.geom.Point2D;

/**
 *
 * @author Andre-Marie Pez
 */
public class FacePoint extends Point2D.Double {

    protected final double originalX;
    protected final double originalY;

    public FacePoint(double originalX, double originalY) {
        super(originalX, originalY);
        this.originalX = originalX;
        this.originalY = originalY;
    }
}
