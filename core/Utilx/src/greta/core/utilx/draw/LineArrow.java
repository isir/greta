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
public class LineArrow extends LineDrawable {

    static final float ARROW_WIDTH = 2.5f;
    static final float ARROW_LENGTH = 7f;
    Polygon arrow;
    double arrowDirection;

    public LineArrow(Color color, Point2D startingPoint, Point2D endingPoint) {
        super(color, startingPoint, endingPoint);
        initArrow();
    }

    public LineArrow(Color color, Point2D startingPoint, Point2D endingPoint, float stroke) {
        super(color, startingPoint, endingPoint, stroke);
        initArrow();
    }

    public LineArrow(Color color, Point2D startingPoint, Point2D endingPoint, BasicStroke stroke) {
        super(color, startingPoint, endingPoint, stroke);
        initArrow();
    }

    @Override
    public void draw(Graphics g) {
        drawArrow(g);
        drawLine(g);
    }

    protected void drawArrow(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(stroke);
        g2d.setColor(color);
        g2d.translate(shape.getX2(), shape.getY2());
        updateArrowDirection();
        g2d.rotate(arrowDirection);
        g2d.fill(arrow);
    }

    protected void drawLine(Graphics g) {
        Point2D realEnd = super.getEndPoint();
        double x2rel = shape.getX2() - shape.getX1();
        double y2rel = shape.getY2() - shape.getY1();
        if(x2rel!=0 || y2rel!=0){
            double lengthRatio = 1.0 - ARROW_LENGTH/Math.sqrt(x2rel*x2rel + y2rel*y2rel);
            super.setEndPoint(new Point2D.Double(
                    shape.getX1() + x2rel*lengthRatio,
                    shape.getY1() + y2rel*lengthRatio
            ));
        }
        super.draw(g);
        super.setEndPoint(realEnd);

    }
            // where the control point for the intersection of the V needs calculating
    // by projecting where the ends meet

    private void initArrow() {
        int[] xPoints = {0, -(int) (ARROW_LENGTH + 2.6 * lineWidth), -(int) (ARROW_LENGTH + 2.6 * lineWidth)};
        int[] yPoints = {0, -(int) (ARROW_WIDTH + lineWidth), (int) (ARROW_WIDTH + lineWidth)};
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
        arrowDirection = -Math.atan2(shape.getX2() - shape.getX1(), shape.getY2() - shape.getY1()) + Math.PI / 2;
    }
}
