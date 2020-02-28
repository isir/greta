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
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 *
 * @author Ken Prepin
 */
public class ArrowWithName extends PathFormDrawable<Shape> {

    String text;
    Color textColor;
    ArcArrowWithName arcArrow;
    LineArrowWithName lineArrow;
    boolean isArc;

    public ArrowWithName() {
        super();
        isArc = false;
    }

    /*
     *
     * Constructors for LineArrows
     */
    public ArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, String name) {
        this(color, startingPoint, endingPoint, name, DEFAULT_STROKE);
    }

    public ArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, String name, float stroke) {
        super(color, new Line2D.Double(startingPoint, endingPoint));
        lineArrow = new LineArrowWithName(color, startingPoint, endingPoint, name, stroke);
        this.text = name;
        //   arcArrow = new ArcArrowWithName(color, startingPoint, endingPoint, 500, "right", name, stroke);
        shape = lineArrow.shape;
        isArc = false;
        //       text.setPosition(new Point2D.Double(shape.getBounds2D().getCenterX(), shape.getBounds2D().getCenterY()), getAngle());
    }
    public ArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, String name, BasicStroke stroke) {
        super(color, new Line2D.Double(startingPoint, endingPoint));
        lineArrow = new LineArrowWithName(color, startingPoint, endingPoint, name, stroke);
        this.text = name;
        //   arcArrow = new ArcArrowWithName(color, startingPoint, endingPoint, 500, "right", name, stroke);
        shape = lineArrow.shape;
        isArc = false;
        //       text.setPosition(new Point2D.Double(shape.getBounds2D().getCenterX(), shape.getBounds2D().getCenterY()), getAngle());
    }

    /*
     *
     * Constructors for ArcArrows
     */
    public ArrowWithName(Color color, Point2D center, double radius, double angStart, double angExtent, String name) {
        this(color, center, radius, angStart, angExtent, name, DEFAULT_STROKE);
    }

    public ArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, int rotationDirection, String name) {
        this(color, startingPoint, endingPoint, newRadius, rotationDirection, name, DEFAULT_STROKE);
    }

    public ArrowWithName(Color color, Point2D startingPoint, Point2D middlePoint, Point2D endingPoint, String name) throws Exception {
        this(color, startingPoint, middlePoint, endingPoint, name, DEFAULT_STROKE);
    }

    public ArrowWithName(Color color, Point2D center, double radius, double angStart, double angExtent, String name, float stroke) {
        super(color, new Arc2D.Double(), stroke);
        arcArrow = new ArcArrowWithName(color, center, radius, angStart, angExtent, name, stroke);
        this.text = name;
        shape = arcArrow.shape;
        isArc = true;
        //       updateStartAndEndAngles();
        //       text.setPosition(new Point2D.Double(shape.getBounds2D().getCenterX(), shape.getBounds2D().getCenterY()), getAngle());
    }

    public ArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, Point2D sideOfCenter, String name, float stroke) {
        super(color, new Arc2D.Double(), stroke);
        System.out.println("new arrow with name created");
        arcArrow = new ArcArrowWithName(color, startingPoint, endingPoint, newRadius, sideOfCenter, name, stroke);
        this.text = name;
        shape = arcArrow.shape;
        isArc = true;
    }
    public ArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, Point2D sideOfCenter, String name, BasicStroke stroke) {
        super(color, new Arc2D.Double(), stroke);
        System.out.println("new arrow with name created");
        arcArrow = new ArcArrowWithName(color, startingPoint, endingPoint, newRadius, sideOfCenter, name, stroke);
        this.text = name;
        shape = arcArrow.shape;
        isArc = true;
    }

    public ArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, int rotationDirection, String name, float stroke) {
        super(color, new Arc2D.Double(), stroke);
        arcArrow = new ArcArrowWithName(color, startingPoint, endingPoint, newRadius, rotationDirection, name, stroke);
        this.text = name;
        shape = arcArrow.shape;
        isArc = true;
    }
   public ArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, int rotationDirection, String name, BasicStroke stroke) {
        super(color, new Arc2D.Double(), stroke);
        arcArrow = new ArcArrowWithName(color, startingPoint, endingPoint, newRadius, rotationDirection, name, stroke);
        this.text = name;
        shape = arcArrow.shape;
        isArc = true;
    }

    public ArrowWithName(Color color, Point2D startingPoint, Point2D middlePoint, Point2D endingPoint, String name, float stroke) throws Exception {
        super(color, new Arc2D.Double(), stroke);
        arcArrow = new ArcArrowWithName(color, startingPoint, middlePoint, endingPoint, name, stroke);
        this.text = name;
        shape = arcArrow.shape;
        isArc = true;
    }
   public ArrowWithName(Color color, Point2D startingPoint, Point2D middlePoint, Point2D endingPoint, String name, BasicStroke stroke) throws Exception {
        super(color, new Arc2D.Double(), stroke);
        arcArrow = new ArcArrowWithName(color, startingPoint, middlePoint, endingPoint, name, stroke);
        this.text = name;
        shape = arcArrow.shape;
        isArc = true;
    }

    public void setText(String text) {
        if (isArc) {
            this.text = text;
            arcArrow.setText(text);
        } else {
            this.text = text;
            lineArrow.setText(text);
        }
        updateStartAndEndPosition();
    }

    @Override
    public Point2D getStartPoint() {
        if (isArc) {
            return arcArrow.getStartPoint();
        } else {
            return lineArrow.getStartPoint();
        }
    }

    @Override
    public double getStartPointX() {
        if (isArc) {
            return arcArrow.getStartPointX();
        } else {
            return lineArrow.getStartPointX();
        }
    }

    @Override
    public double getStartPointY() {
        if (isArc) {
            return arcArrow.getStartPointY();
        } else {
            return lineArrow.getStartPointY();
        }
    }

    @Override
    public Point2D getEndPoint() {
        if (isArc) {
            return arcArrow.getEndPoint();
        } else {
            return lineArrow.getEndPoint();
        }
    }

    @Override
    public double getEndPointX() {
        if (isArc) {
            return arcArrow.getEndPointX();
        } else {
            return lineArrow.getEndPointX();
        }
    }

    @Override
    public double getEndPointY() {
        if (isArc) {
            return arcArrow.getEndPointY();
        } else {
            return lineArrow.getEndPointY();
        }
    }
    public void setStartAndEndPoints(Point2D startPoint, Point2D endPoint) {
        if (isArc) {
            arcArrow.setStartAndEndPoints(startPoint, endPoint);
            shape = arcArrow.shape;
        } else {
            lineArrow.setStartAndEndPoints(startPoint, endPoint);
            shape = lineArrow.shape;
        }
    }

    @Override
    public void setStartPoint(Point2D pos) {
        if (isArc) {
            arcArrow.setStartPoint(pos);
            shape = arcArrow.shape;
        } else {
            lineArrow.setStartPoint(pos);
            shape = lineArrow.shape;
        }
    }

    @Override
    public void setEndPoint(Point2D pos) {
        if (isArc) {
            arcArrow.setEndPoint(pos);
            shape = arcArrow.shape;
        } else {
            lineArrow.setEndPoint(pos);
            shape = lineArrow.shape;
        }
    }

    public void updateStartAndEndPosition() {
        if (isArc) {
            arcArrow.updateStartAndEndPosition();
        } else {
            lineArrow.updateStartAndEndPosition();
        }
    }

    @Override
    public void draw(Graphics g) {
        if (isArc) {
            arcArrow.draw(g);
        } else {
            lineArrow.draw(g);
        }
    }

    @Override
    public void setPosition(Point2D p) {
        if (isArc) {
            arcArrow.setPosition(p);
            shape = arcArrow.shape;

        } else {
            lineArrow.setPosition(p);
            shape = lineArrow.shape;
        }
    }

    @Override
    public void setCenter(Point2D p) {
        if (isArc) {
            arcArrow.setCenter(p);
            shape = arcArrow.shape;
        } else {
            lineArrow.setCenter(p);
            shape = lineArrow.shape;
        }
    }

    @Override
    public void setDashed(boolean dashed){
         if (isArc) {
            arcArrow.setDashed(dashed);
        } else {
            lineArrow.setDashed(dashed);
        }

    }

    @Override
    public void setLineWidth(float lineWidth){
         if (isArc) {
            arcArrow.setLineWidth(lineWidth);
        } else {
            lineArrow.setLineWidth(lineWidth);
        }

    }

     @Override
    public void setStroke(BasicStroke stroke) {
        if (isArc) {
            arcArrow.setStroke(stroke);

        } else {
            lineArrow.setStroke(stroke);
        }
    }
    @Override
    public void setColor(Color color) {
        if (isArc) {
            arcArrow.setColor(color);

        } else {
            lineArrow.setColor(color);
        }
    }
    @Override
    public void setMousePressedPosition(Point2D pos) {
        if (isArc) {
            arcArrow.setMousePressedPosition(pos);

        } else {
            lineArrow.setMousePressedPosition(pos);
        }
    }

    @Override
    public boolean intersects(Point2D p) {
        if (isArc) {
            return arcArrow.intersects(p);
        } else {
            return lineArrow.intersects(p);
        }
    }

    public void setArc(double bending) {
        this.setArc(bending, "rigth");
    }

    public void setArc() {
        this.setArc(30, "rigth");
    }

    public void setArc(double bending, String side) {
        arcArrow = new ArcArrowWithName(color, getStartPoint(), bending, getEndPoint(), this.text, side, stroke);
        isArc = true;
        updateStartAndEndPosition();
    }

    public void setLine() {
        lineArrow = new LineArrowWithName(color, getStartPoint(), getEndPoint(), this.text, stroke);
        isArc = false;
    }

    public void setTextAlongLine(boolean textAlongLine) {
        this.arcArrow.setTextAlongLine(textAlongLine);
    }

    public boolean isArc(){
        return this.isArc;
    }
}
