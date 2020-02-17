/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.utilx.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Point2D;

/**
 *
 * @author Ken Prepin
 */
public class ArcArrow extends ArcDrawable {

    static final float ARROW_WIDTH = 2.5f;
    static final float ARROW_LENGTH = 7f;
    Polygon arrow;
    double arrowDirection;

    public ArcArrow() {
        super();
        updateArrow();
    }

    public ArcArrow(Color color, Point2D center, double radius, double angStart, double angExtent) {
        super(color, center, radius, angStart, angExtent);
        updateArrow();
    }

    public ArcArrow(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, int rotationDirection) {
        super(color, startingPoint, endingPoint, newRadius, rotationDirection);
        updateArrow();
    }

    public ArcArrow(Color color, Point2D startingPoint, Point2D middlePoint, Point2D endingPoint) {
        super(color, startingPoint, middlePoint, endingPoint);
        updateArrow();
    }

    public ArcArrow(Point2D center, Color color, Point2D startingPoint, Point2D endingPoint) {
        super(center, color, startingPoint, endingPoint);
        updateArrow();
    }

    public ArcArrow(Color color, Point2D center, double radius, double angStart, double angExtent, float stroke) {
        super(color, center, radius, angStart, angExtent, stroke);
        updateArrow();
    }

    public ArcArrow(Color color, Point2D center, double radius, double angStart, double angExtent, BasicStroke stroke) {
        super(color, center, radius, angStart, angExtent, stroke);
        updateArrow();
    }

    public ArcArrow(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, Point2D sideOfCenter, float stroke) {
        super(color, startingPoint, endingPoint, newRadius, sideOfCenter, stroke);
        updateArrow();
    }

    public ArcArrow(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, Point2D sideOfCenter, BasicStroke stroke) {
        super(color, startingPoint, endingPoint, newRadius, sideOfCenter, stroke);
        updateArrow();
    }

    public ArcArrow(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, int rotationDirection, float stroke) {
        super(color, startingPoint, endingPoint, newRadius, rotationDirection, stroke);
        updateArrow();
    }

    public ArcArrow(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, int rotationDirection, BasicStroke stroke) {
        super(color, startingPoint, endingPoint, newRadius, rotationDirection, stroke);
        updateArrow();
    }

    public ArcArrow(Color color, Point2D startingPoint, Point2D middlePoint, Point2D endingPoint, float stroke) {
        super(color, startingPoint, middlePoint, endingPoint, stroke);
        updateArrow();
    }

    public ArcArrow(Color color, Point2D startingPoint, Point2D middlePoint, Point2D endingPoint, BasicStroke stroke) {
        super(color, startingPoint, middlePoint, endingPoint, stroke);
        updateArrow();
    }

    public ArcArrow(Point2D center, Color color, Point2D startingPoint, Point2D endingPoint, float stroke) {
        super(center, color, startingPoint, endingPoint, stroke);
        updateArrow();
    }

    public ArcArrow(Point2D center, Color color, Point2D startingPoint, Point2D endingPoint, BasicStroke stroke) {
        super(center, color, startingPoint, endingPoint, stroke);
        updateArrow();
    }

    public ArcArrow(Color color, Point2D center, double radius, double angStart, double angExtent, int closure) {
        super(color, center, radius, angStart, angExtent, closure);
        updateArrow();
    }

    public ArcArrow(double bending, Color color, Point2D startingPoint, Point2D endingPoint, String side, float stroke) {
        super(bending, color, startingPoint, endingPoint, side, stroke);
        updateArrow();
    }

    public ArcArrow(double bending, Color color, Point2D startingPoint, Point2D endingPoint, String side, BasicStroke stroke) {
        super(bending, color, startingPoint, endingPoint, side, stroke);
        updateArrow();
    }

    public ArcArrow(Color color, Point2D center, double radius, double angStart, double angExtent, int closure, float stroke) {
        super(color, center, radius, angStart, angExtent, closure, stroke);
        updateArrow();
    }

    public ArcArrow(Color color, Point2D center, double radius, double angStart, double angExtent, int closure, BasicStroke stroke) {
        super(color, center, radius, angStart, angExtent, closure, stroke);
        updateArrow();
    }

    @Override
    public void draw(Graphics g) {
        drawArc(g);
        drawArrow(g);
    }

    protected void drawArc(Graphics g) {
        super.draw(g);
    }

    protected void drawArrow(Graphics g) {
        Color c = g.getColor();
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(stroke);
        g2d.setColor(color);
        g2d.translate(shape.getEndPoint().getX(), shape.getEndPoint().getY());
        updateArrowDirection();
        g2d.rotate(arrowDirection);
        g2d.fill(arrow);
        g2d.rotate(-arrowDirection);
        // set the initial color of Graphics back
        g.setColor(c);
    }
            // where the control point for the intersection of the V needs calculating
    // by projecting where the ends meet

    private void updateArrow() {
        int[] xPoints = {-(int) (ARROW_LENGTH + 2.6 * lineWidth), -(int) (ARROW_LENGTH + 2.6 * lineWidth), 0};
        int[] yPoints = {-(int) (ARROW_WIDTH + lineWidth), (int) (ARROW_WIDTH + lineWidth), 0};
        arrow = new Polygon(xPoints, yPoints, 3);
        updateArrowDirection();

    }

    @Override
    public void setStartPoint(Point2D pos) {
        super.setStartPoint(pos);
        updateArrowDirection();
    }

    @Override
    public void setEndPoint(Point2D pos) {
        super.setEndPoint(pos);
        updateArrowDirection();
    }

    private void updateArrowDirection() {
        try {
            arrowDirection = -getAngle(getCenter(), getEndPoint()) - Math.PI / 2 + Math.atan2(ARROW_LENGTH, getCenter().distance(getEndPoint())) / 2;
            //   arrowDirection = Math.atan2(shape.getCenterY()-shape.getEndPoint().getY(), shape.getEndPoint().getX() - shape.getCenterX()) - Math.PI/2;
            //   arrowDirection = Math.atan2(shape.getCenterY()-shape.getEndPoint().getY(), shape.getEndPoint().getX() - shape.getCenterX()) - Math.PI/2;
        } catch (Exception ex) {}
    }

}
