/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.utilx.draw;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 *
 * @author Ken Prepin
 */
public class DiscDrawable extends CircleDrawable {

    public DiscDrawable(Color color, Point2D center, double radius) {
        super(color, center, radius, 2);
    }
    public DiscDrawable(Color color, Point2D center, double radius, float stroke) {
        super(color, center, radius, stroke);
    }

    @Override
    public void draw(Graphics g) {
        Color c = g.getColor();
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(stroke);
        g2d.setColor(color);
        g2d.fill(shape);
//        g2d.draw(shape); //makes some artifacts for little radius
        // set the initial color of Graphics back
        g.setColor(c);
    }
}
