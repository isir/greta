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
import java.awt.geom.Area;
import java.awt.geom.Point2D;

/**
 *
 * @author Ken Prepin
 */
public class ArcArrowWithName extends ArcArrow {

    TextDrawable textDrawable;
//    ArcDrawable arcBegin;
//    ArcArrow arcEndWithArrow;
    Color textColor;
    Point2D textPosition; // viaPoint of the arc
    double distanceFromLine;
    boolean textAlongLine;
    // For debugging
/*    LineDrawable centerPoint;
     LineDrawable startPoint;
     LineDrawable endPoint;
     LineDrawable viaPoint;*/

    public ArcArrowWithName() {
        super();
        init(DEFAULT_COLOR, "");
        this.textPosition = computeViaPoint(getStartPoint(), getEndPoint());
    }

    public ArcArrowWithName(Color color, Point2D center, double radius, double angStart, double angExtent, String name) {
        this(color, center, radius, angStart, angExtent, name, DEFAULT_STROKE);
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, int rotationDirection, String name) {
        this(color, startingPoint, endingPoint, newRadius, rotationDirection, name, DEFAULT_STROKE);
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, Point2D middlePoint, Point2D endingPoint, String name) throws Exception {
        this(color, startingPoint, middlePoint, endingPoint, name, DEFAULT_STROKE);
    }

    public ArcArrowWithName(Color color, Point2D center, double radius, double angStart, double angExtent, String name, float stroke) {
        super(color, center, radius, angStart, angExtent, Arc2D.OPEN, stroke);
        init(color, name);
        this.textPosition = computeViaPoint(getStartPoint(), getEndPoint());
        //       updateStartAndEndAngles();
        //       text.setPosition(new Point2D.Double(shape.getBounds2D().getCenterX(), shape.getBounds2D().getCenterY()), getAngle());
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, Point2D sideOfCenter, String name, float stroke) {
        super(color, startingPoint, endingPoint, newRadius, sideOfCenter, stroke);
        double angle = 0;
        try {
            angle = getAngle();
        } catch (Exception e) {
        }
        textPosition = new Point2D.Double(this.getCenter().getX() + Math.cos(angle + Math.PI / 2) * newRadius, this.getCenter().getY() + Math.sin(angle + Math.PI / 2) * newRadius);
        init(color, name);
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, Point2D sideOfCenter, String name, BasicStroke stroke) {
        super(color, startingPoint, endingPoint, newRadius, sideOfCenter, stroke);
        double angle = 0;
        try {
            angle = getAngle();
        } catch (Exception e) {
        }
        textPosition = new Point2D.Double(this.getCenter().getX() + Math.cos(angle + Math.PI / 2) * newRadius, this.getCenter().getY() + Math.sin(angle + Math.PI / 2) * newRadius);
        init(color, name);
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, int rotationDirection, String name, float stroke) {
        super(color, startingPoint, endingPoint, newRadius, rotationDirection, stroke);
        double angle = 0;
        try {
            angle = getAngle();
        } catch (Exception e) {
        }
        textPosition = new Point2D.Double(this.getCenter().getX() + Math.cos(angle + Math.PI / 2) * newRadius, this.getCenter().getY() + Math.sin(angle + Math.PI / 2) * newRadius);
        init(color, name);
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, int rotationDirection, String name, BasicStroke stroke) {
        super(color, startingPoint, endingPoint, newRadius, rotationDirection, stroke);
        double angle = 0;
        try {
            angle = getAngle();
        } catch (Exception e) {
        }
        textPosition = new Point2D.Double(this.getCenter().getX() + Math.cos(angle + Math.PI / 2) * newRadius, this.getCenter().getY() + Math.sin(angle + Math.PI / 2) * newRadius);
        init(color, name);
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, Point2D textPosition, Point2D endingPoint, String name, float stroke) throws Exception {
        super(color, startingPoint, textPosition, endingPoint, stroke);
        this.textPosition = textPosition;
        init(color, name);
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, Point2D textPosition, Point2D endingPoint, String name, BasicStroke stroke) throws Exception {
        super(color, startingPoint, textPosition, endingPoint, stroke);
        this.textPosition = textPosition;
        init(color, name);
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, double textDistanceFromLine, Point2D endingPoint, String name, float stroke) {
        this(color, startingPoint, textDistanceFromLine, endingPoint, name, "right", stroke);
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, double textDistanceFromLine, Point2D endingPoint, String name, BasicStroke stroke) {
        this(color, startingPoint, textDistanceFromLine, endingPoint, name, "right", stroke);
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, double textDistanceFromLine, Point2D endingPoint, String name) {
        this(color, startingPoint, textDistanceFromLine, endingPoint, name, DEFAULT_STROKE);
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, double textDistanceFromLine, Point2D endingPoint, String name, String side, float stroke) {
        super(textDistanceFromLine, color, startingPoint, endingPoint, side, stroke);
        this.distanceFromLine = textDistanceFromLine;
        init(color, name);
        this.textPosition = computeViaPoint(getStartPoint(), getEndPoint());
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, double textDistanceFromLine, Point2D endingPoint, String name, String side, BasicStroke stroke) {
        super(textDistanceFromLine, color, startingPoint, endingPoint, side, stroke);
        this.distanceFromLine = textDistanceFromLine;
        init(color, name);
        this.textPosition = computeViaPoint(getStartPoint(), getEndPoint());
    }

    public ArcArrowWithName(Color color, Point2D startingPoint, double textDistanceFromLine, Point2D endingPoint, String name, String side) {
        this(color, startingPoint, textDistanceFromLine, endingPoint, name, side, DEFAULT_STROKE);
    }

    public void init(Color color, String name) {
        textDrawable = new TextDrawable(color, name);
        textAlongLine = true;
        textColor = color;
//        arcBegin = new ArcDrawable();
//        arcEndWithArrow = new ArcArrow();
    }


    public void setText(String text) {
        this.textDrawable = new TextDrawable(textColor, text);
        updateStartAndEndPosition();
    }


    @Override
    public void draw(Graphics g) {
        /*       // For debugging
         // super.draw(g); // Do not draw super, super is just here for its shape
         centerPoint = new LineDrawable(color.red, getCenter(), new Point2D.Double(getCenter().getX() + 2, getCenter().getY()), 2);
         startPoint = new LineDrawable(color.GREEN, getStartPoint(), new Point2D.Double(getStartPointX() + 2, getStartPointY()), 2);
         endPoint = new LineDrawable(color.red, getEndPoint(), new Point2D.Double(getEndPointX() + 2, getEndPointY()), 2);*/
        /*       centerPoint.draw(g);
         startPoint.draw(g);
         endPoint.draw(g);
         viaPoint.draw(g);
         // End debugging*/
      //   System.out.println("shape " + shape.toString() + " " + this.getClass().getName());
        //   System.out.println("shape StartPoint " + shape.getStartPoint().getX() + " " + shape.getStartPoint().getY() + " EndPoint " + shape.getEndPoint().getX() + " " + shape.getEndPoint().getY());

        /* Drawing of circle center for debugging */
    //    g2d.setStroke(new BasicStroke(DEFAULT_STROKE*10));
        //   g2d.draw(new Line2D.Double(new Point2D.Double(shape.getCenterX(),shape.getCenterY()),new Point2D.Double(shape.getCenterX()+1,shape.getCenterY())));
        // set the initial color of Graphics back


//        arcBegin.draw(g);
//        arcEndWithArrow.draw(g);


        if (textDrawable.getText() != null && !textDrawable.getText().isEmpty()) {
            textDrawable.draw(g);
            drawArrow(g);

            Shape originalClip = g.getClip();
            Area clip = new Area(originalClip);
            clip.subtract(textDrawable.getArea());

            g.setClip(clip);
            drawArc(g);
            g.setClip(originalClip);
        } else {
            super.draw(g);
        }
    }
    // where the control point for the intersection of the V needs calculating
    // by projecting where the ends meet

    public void setStartAndEndPoints(Point2D startPoint, Point2D endPoint) {
        /*      //Debugging
         centerPoint = new LineDrawable(color.red, getCenter(), new Point2D.Double(getCenter().getX() + 2, getCenter().getY()), 2);
         //  this.startPoint = new LineDrawable(color.GREEN, getStartPoint(), new Point2D.Double(getStartPointX() + 2, getStartPointY()), 2);
         // this.endPoint = new LineDrawable(color.red, getEndPoint(), new Point2D.Double(getEndPointX() + 2, getEndPointY()), 2);
         this.startPoint = new LineDrawable(color.GREEN, startPoint, new Point2D.Double(startPoint.getX() + 2, startPoint.getY()), 2);
         this.endPoint = new LineDrawable(color.red, endPoint, new Point2D.Double(endPoint.getX() + 2, endPoint.getY()), 2);
         this.viaPoint = new LineDrawable(color.orange, new Point2D.Double((startPoint.getX() + endPoint.getX()) / 2 + Math.sin(getAngle()) * distanceFromLine, (startPoint.getY() + endPoint.getY()) / 2 + Math.cos(getAngle()) * distanceFromLine), new Point2D.Double((startPoint.getX() + endPoint.getX()) / 2 + Math.sin(getAngle()) * distanceFromLine+2, (startPoint.getY() + endPoint.getY()) / 2 + Math.cos(getAngle()) * distanceFromLine), 2);
         // EndDebugging*/
//        Point2D viaPoint = computeViaPoint(startPoint, endPoint);
//        shape = newArcByThreePoints_safety(startPoint, viaPoint, endPoint);
//        super.defByThreePoints(startPoint, viaPoint, endPoint);
//        this.textPosition = viaPoint;
        //       super.setStartAndViaPoints(startPoint, new Point2D.Double((startPoint.getX() + endPoint.getX()) / 2 + Math.sin(getAngle()) * distanceFromLine, (startPoint.getY() + endPoint.getY()) / 2 + Math.cos(getAngle()) * distanceFromLine));
        //      super.setEndAndViaPoints(endPoint, new Point2D.Double((endPoint.getX() + startPoint.getX()) / 2 + Math.sin(getAngle()) * distanceFromLine, (endPoint.getY() + startPoint.getY()) / 2 + Math.cos(getAngle()) * distanceFromLine));
//        updateStartAndEndPosition();
        updateArc(startPoint, computeViaPoint(startPoint, endPoint), endPoint);
    }

    @Override
    public void setStartPoint(Point2D pos) {
        setStartAndEndPoints(pos, getEndPoint());
//        updateStartAndEndPosition();

    }

    @Override
    public void setStartAndViaPoints(Point2D startPoint, Point2D viaPoint) {
        updateArc(startPoint, viaPoint, getEndPoint());
//        super.setStartAndViaPoints(startPoint, viaPoint);
//        this.textPosition = viaPoint;
//        updateStartAndEndPosition();

    }

    @Override
    public void setEndPoint(Point2D pos) {
        setStartAndEndPoints(getStartPoint(), pos);
//        updateStartAndEndPosition();
    }

    @Override
    public void setEndAndViaPoints(Point2D endPoint, Point2D viaPoint) {
        updateArc(getStartPoint(), viaPoint, endPoint);
//        super.setEndAndViaPoints(endPoint, viaPoint);
//        this.textPosition = viaPoint;
//        updateStartAndEndPosition();

    }

    private void updateArc(Point2D startPoint, Point2D viaPoint, Point2D endPoint){
        defByThreePoints_safety(startPoint, viaPoint, endPoint);
        this.textPosition = viaPoint;
        updateStartAndEndPosition();
    }
    private Point2D computeViaPoint(Point2D startPoint, Point2D endPoint){
        double angle = getAngle_safety(startPoint, endPoint);
        return new Point2D.Double((startPoint.getX()+ endPoint.getX()) / 2 + Math.sin(angle) * distanceFromLine, (startPoint.getY()+ endPoint.getY()) / 2 + Math.cos(angle) * distanceFromLine);
    }


    public double getAngle_safety(){
        try {
            return getAngle();
        } catch (Exception e) {
            return 0;
        }
    }

    public double getAngle_safety(Point2D p1, Point2D p2){
        try {
            return getAngle(p1,p2);
        } catch (Exception e) {
            return 0;
        }
    }

    public void updateStartAndEndPosition() {
        if (textAlongLine) {
//            arcBegin = new ArcDrawable(getCenter(), color, this.getStartPoint(), new Point2D.Double((getStartPointX() + getEndPointX()) / 2 + Math.sin(getAngle()) * distanceFromLine - Math.cos(getAngle()) * textDrawable.getLength() / 2, (getStartPointY() + getEndPointY()) / 2 + Math.cos(getAngle()) * distanceFromLine + Math.sin(getAngle()) * textDrawable.getLength() / 2),stroke);
//            arcEndWithArrow = new ArcArrow(getCenter(), color, new Point2D.Double((getStartPointX() + getEndPointX()) / 2 + Math.sin(getAngle()) * distanceFromLine + Math.cos(getAngle()) * textDrawable.getLength() / 2, (getStartPointY() + getEndPointY()) / 2 + Math.cos(getAngle()) * distanceFromLine - Math.sin(getAngle()) * textDrawable.getLength() / 2), this.getEndPoint(), stroke);
//            textDrawable.setPosition(new Point2D.Double((getStartPointX() + getEndPointX()) / 2 + Math.sin(angle) * distanceFromLine, (getStartPointY() + getEndPointY()) / 2 + Math.cos(angle) * distanceFromLine), angle);
            textDrawable.setPosition(textPosition, getAngle_safety());

        } else {
            /*      //Debugging
             centerPoint = new LineDrawable(color.red, getCenter(), new Point2D.Double(getCenter().getX() + 2, getCenter().getY()), 2);
             startPoint = new LineDrawable(color.GREEN, getStartPoint(), new Point2D.Double(getStartPointX() + 2, getStartPointY()), 2);
             endPoint = new LineDrawable(color.red, getEndPoint(), new Point2D.Double(getEndPointX() + 2, getEndPointY()), 2);
             // EndDebugging */
//            arcBegin = new ArcDrawable(getCenter(), color, this.getStartPoint(), new Point2D.Double((getStartPointX() + getEndPointX()) / 2 + Math.sin(getAngle()) * distanceFromLine - Math.cos(getAngle()) * textDrawable.getLength() / 4, (getStartPointY() + getEndPointY()) / 2 + Math.cos(getAngle()) * distanceFromLine + Math.sin(getAngle()) * textDrawable.getLength() / 4), stroke);
//            arcEndWithArrow = new ArcArrow(getCenter(), color, new Point2D.Double((getStartPointX() + getEndPointX()) / 2 + Math.sin(getAngle()) * distanceFromLine + Math.cos(getAngle()) * (textDrawable.getLength()) / 4, (getStartPointY() + getEndPointY()) / 2 + Math.cos(getAngle()) * distanceFromLine - Math.sin(getAngle()) * textDrawable.getLength() / 4), this.getEndPoint(), stroke);
            textDrawable.setPosition(new Point2D.Double(textPosition.getX() + textDrawable.getLength() / 2 - textDrawable.getMargin() + 2, textPosition.getY() - textDrawable.getHeight() / 2), 0);
        }
//        this.setRadius(arcEndWithArrow.getRadius());
    }

    public void setTextAlongLine(boolean textAlongLine) {
        this.textAlongLine = textAlongLine;
    }
}
