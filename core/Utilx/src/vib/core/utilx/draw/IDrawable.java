/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */


package vib.core.utilx.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Point2D;

/**
 *
 * @author Ken Prepin
 */
public interface IDrawable {

    public abstract void draw(Graphics g);

    public abstract Shape getShape();

    public abstract boolean intersects(Point2D p);

    public abstract void setColor(Color color);
    
    public abstract Color getColor();

    public abstract void setStroke(BasicStroke stroke);
}
