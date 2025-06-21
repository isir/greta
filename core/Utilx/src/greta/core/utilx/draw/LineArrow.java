/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
