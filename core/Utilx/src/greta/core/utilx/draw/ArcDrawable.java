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

import greta.core.util.math.Affin;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;

/**
 *
 * @author Ken Prepin
 */
public class ArcDrawable extends PathFormDrawable<Arc2D> {

    PositionSetter ps;
    double angStart; // In degrees !!!!!!!!
    double angExtent;
    int closure;
    private double radius;
    public static final int CLOCK_WISE = 1;
    public static final int COUNTER_CLOCK_WISE = -1;
    int rotationDirection = COUNTER_CLOCK_WISE;

    public ArcDrawable() {
        this(DEFAULT_COLOR, new Point2D.Double(), 0, 0, 0);
    }

    public ArcDrawable(Color color, Point2D center, double radius, double angStart, double angExtent, int closure) {
        this(color, center, radius, angStart, angExtent, closure, DEFAULT_STROKE);
    }

    public ArcDrawable(Color color, Point2D center, double radius, double angStart, double angExtent) {
        this(color, center, radius, angStart, angExtent, Arc2D.OPEN, DEFAULT_STROKE);
    }

    public ArcDrawable(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, Point2D sideOfCenter) {
        this(color, startingPoint, endingPoint, newRadius, sideOfCenter, DEFAULT_STROKE);
    }

    public ArcDrawable(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, int rotationDirection) {
        this(color, startingPoint, endingPoint, newRadius, rotationDirection, DEFAULT_STROKE);
    }

    public ArcDrawable(Color color, Point2D startingPoint, Point2D middlePoint, Point2D endingPoint) {
        this(color, startingPoint, middlePoint, endingPoint, DEFAULT_STROKE);
    }

    public ArcDrawable(Point2D center, Color color, Point2D startingPoint, Point2D endingPoint) {
        this(center, color, startingPoint, endingPoint, DEFAULT_STROKE);
    }

    public ArcDrawable(Color color, Point2D center, double radius, double angStart, double angExtent, float stroke) {
        this(color, center, radius, angStart, angExtent, Arc2D.OPEN, stroke);
    }

    public ArcDrawable(Color color, Point2D center, double radius, double angStart, double angExtent, BasicStroke stroke) {
        this(color, center, radius, angStart, angExtent, Arc2D.OPEN, stroke);
    }

    public ArcDrawable(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, Point2D sideOfCenter, float stroke) {
        this(color, startingPoint, endingPoint, newRadius, sideOfCenter, new BasicStroke(stroke));
    }

    public ArcDrawable(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, Point2D sideOfCenter, BasicStroke stroke) {
        super(color, new Arc2D.Double(), stroke);
        shape = newArcByStartEndRadiusSide_safety(startingPoint, endingPoint, newRadius, sideOfCenter, rotationDirection);
    }

    public ArcDrawable(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, int rotationDirection, float stroke) {
        this(color, startingPoint, endingPoint, newRadius, rotationDirection, new BasicStroke(stroke));
    }

    public ArcDrawable(Color color, Point2D startingPoint, Point2D endingPoint, double newRadius, int rotationDirection, BasicStroke stroke) {
        super(color, new Arc2D.Double(), stroke);
        shape = newArcByStartEndRadiusSide_safety(startingPoint, endingPoint, newRadius, rotationDirection);
    }

    public ArcDrawable(Color color, Point2D startingPoint, Point2D middlePoint, Point2D endingPoint, float stroke) {
        this(color, startingPoint, middlePoint, endingPoint, new BasicStroke(stroke));
    }

    public ArcDrawable(Color color, Point2D startingPoint, Point2D middlePoint, Point2D endingPoint, BasicStroke stroke) {
        super(color, new Arc2D.Double(), stroke);
        shape = newArcByThreePoints_safety(startingPoint, middlePoint, endingPoint);
    }

    public ArcDrawable(Point2D center, Color color, Point2D startingPoint, Point2D endingPoint, float stroke) {
        this(center, color, startingPoint, endingPoint, new BasicStroke(stroke));
    }

    public ArcDrawable(Point2D center, Color color, Point2D startingPoint, Point2D endingPoint, BasicStroke stroke) {
        super(color, new Arc2D.Double(), stroke);
        shape = newArcByCenterStartEnd_safety(center, startingPoint, endingPoint, rotationDirection);
    }

    public ArcDrawable(double bending, Color color, Point2D startingPoint, Point2D endingPoint, String side, float stroke) {
        this(bending, color, startingPoint, endingPoint, side, new BasicStroke(stroke));
    }

    public ArcDrawable(double bending, Color color, Point2D startingPoint, Point2D endingPoint, String side, BasicStroke stroke) {
        super(color, new Arc2D.Double(), stroke);
        shape = newArcByStartEndBendingSide_safety(startingPoint, endingPoint, bending, side);
    }

    public ArcDrawable(Color color, Point2D center, double radius, double angStart, double angExtent, int closure, float stroke) {
        //   super(color, new Ellipse2D.Double(center.getX()-(radius+stroke/2),center.getY()-(radius+stroke/2), radius*2, radius*2), new BasicStroke(stroke));
        super(color, new Arc2D.Double(center.getX() - radius, center.getY() - radius, radius * 2, radius * 2, angStart, angExtent, closure), stroke);
        this.angStart = angStart;
        this.angExtent = angExtent;
        this.closure = closure;
        this.radius = radius;
        //   this.radius = radius+stroke/2;
        mousePressedPosition = new Point2D.Double(0, 0);
    }

    public ArcDrawable(Color color, Point2D center, double radius, double angStart, double angExtent, int closure, BasicStroke stroke) {
        //   super(color, new Ellipse2D.Double(center.getX()-(radius+stroke/2),center.getY()-(radius+stroke/2), radius*2, radius*2), new BasicStroke(stroke));
        super(color, new Arc2D.Double(center.getX() - radius, center.getY() - radius, radius * 2, radius * 2, angStart, angExtent, closure), stroke);
        this.angStart = angStart;
        this.angExtent = angExtent;
        this.closure = closure;
        this.radius = radius;
        //   this.radius = radius+stroke/2;
        mousePressedPosition = new Point2D.Double(0, 0);
    }

    @Override
    public void draw(Graphics g) {
        Color c = g.getColor();
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(stroke);
        g2d.setColor(color);
        //   System.out.println("shape " + shape.toString() + " " + this.getClass().getName());
        //   System.out.println("shape StartPoint " + shape.getStartPoint().getX() + " " + shape.getStartPoint().getY() + " EndPoint " + shape.getEndPoint().getX() + " " + shape.getEndPoint().getY());
        g2d.draw(shape);
        /* Drawing of circle center for debugging */
        //    g2d.setStroke(new BasicStroke(DEFAULT_STROKE*10));
        //   g2d.draw(new Line2D.Double(new Point2D.Double(shape.getCenterX(),shape.getCenterY()),new Point2D.Double(shape.getCenterX()+1,shape.getCenterY())));
        // set the initial color of Graphics back
        g.setColor(c);
    }

    /**
     *
     * @return Point2D the center of the circle
     */
    @Override
    public Point2D getCenter() {
        return new Point2D.Double(shape.getCenterX(), shape.getCenterY());
    }

    @Override
    public void setPosition(Point2D pos) {
        //   System.out.println("shape "+ shape.toString());
        shape = new Arc2D.Double(pos.getX() - mousePressedPosition.getX(), pos.getY() - mousePressedPosition.getY(), shape.getWidth(), shape.getHeight(), angStart, angExtent, closure);
    }

    @Override
    public void setMousePressedPosition(Point2D pos) {
        if (getStartPoint().distance(pos) < 10) {
            ps = new P1PositionSetter();
        } else if (getEndPoint().distance(pos) < 10) {
            ps = new P2PositionSetter();
        } else {
            ps = null;
        }
    }

    public double getRadius() {
        return this.radius;
    }

    public Point2D getLimitToward(Point2D pos) {
        Point2D center = this.getCenter();
        double X = pos.getX() - center.getX();
        double Y = pos.getY() - center.getY();
        double lineLenght = Math.sqrt(X * X + Y * Y);
        if (lineLenght != 0) {
            double x = getRadius() / lineLenght * X;
            double y = getRadius() / lineLenght * Y;
            return new Point2D.Double(center.getX() + x, center.getY() + y);
        } else {
            return center;
        }
    }

    @Override
    public boolean intersects(Point2D p) {
        double radius = getRadius();
        //    System.out.println("intersect of ArcDrawable");
        double angleEnd = angStart + angExtent;
        /*   if (angleEnd > 360) {
         angleEnd -= 360;
         }*/
        Point2D center = getCenter();
        //    System.out.println("Angle Start " + angStart + " Angle End " + angleEnd + " Radius " + radius + " dist2center " + center.distance(p));

        if (center.distance(p) < radius - 5) {
            return false;
        }
        if (center.distance(p) > radius + 5) {
            return false;
        }
        double anglePointRad = Math.atan2(center.getY() - p.getY(), p.getX() - center.getX());
        if (anglePointRad < 0) {
            anglePointRad += 2 * Math.PI;
        }
        double anglePoint = Math.toDegrees(anglePointRad);
        if (angStart < angleEnd) {
            if (anglePoint < angStart) {
                if (anglePoint < angleEnd) { // anglePoint < angStart && anglePoint < angleEnd
                    return true;
                } else {                    // anglePoint < angStart && anglePoint > angleEnd
                    return false;
                }
            } else {
                if (anglePoint > angleEnd) {// anglePoint > angStart && anglePoint > angleEnd
                    return false;
                } else {                    // anglePoint > angStart && anglePoint < angleEnd
                    return true;
                }
            }
        } else {
            if (anglePoint < angStart) {
                if (anglePoint < angleEnd) { // anglePoint < angStart && anglePoint < angleEnd
                    return true;
                } else {                    // anglePoint < angStart && anglePoint > angleEnd
                    return false;
                }
            } else {
                if (anglePoint < angleEnd) {// anglePoint > angStart && anglePoint > angleEnd
                    return false;
                } else {                    // anglePoint > angStart && anglePoint < angleEnd
                    return true;
                }
            }
        }

    }

    @Override
    public void setCenter(Point2D center) {
        //     shape = new Ellipse2D.Double(center.getX() - mousePressedPosition.getX()-shape.getWidth()/2,center.getY() - mousePressedPosition.getY()-shape.getHeight()/2, shape.getWidth(), shape.getHeight());
        shape = new Arc2D.Double(center.getX() - getRadius(), center.getY() - getRadius(), shape.getWidth(), shape.getHeight(), angStart, angExtent, closure);
    }

    public void setRadius(double newRadius) {
        if (Math.abs(this.radius - newRadius) > 0.1) {
            this.radius = newRadius;
            //        Point2D.Double newPosition = new Point2D.Double(getCenter().getX() - (newRadius+stroke.getLineWidth()), getCenter().getY() - (newRadius+stroke.getLineWidth()));
            Point2D.Double newPosition = new Point2D.Double(getCenter().getX() - newRadius, getCenter().getY() - newRadius);
            shape = new Arc2D.Double(newPosition.x, newPosition.y, newRadius * 2, newRadius * 2, angStart, angExtent, closure);
        }
    }

    public void defByStartEndRadiusSide(Point2D startingPoint, Point2D endingPoint, double newRadius, Point2D sideOfCenter, int rotationDirection) throws Exception {
        shape = newArcByStartEndRadiusSide(startingPoint, endingPoint, newRadius, sideOfCenter, rotationDirection);
    }

    public void defByStartEndRadiusSide(Point2D startingPoint, Point2D endingPoint, double newRadius, Point2D sideOfCenter) throws Exception {
        this.defByStartEndRadiusSide(startingPoint, endingPoint, newRadius, sideOfCenter, rotationDirection);
    }

    public void defByStartEndRadiusSide(Point2D startingPoint, Point2D endingPoint, double newRadius) throws Exception {
        shape = newArcByStartEndRadiusSide(startingPoint, endingPoint, newRadius, rotationDirection);
    }

    public void defByStartEndRadiusSide(Point2D startingPoint, Point2D endingPoint, double newRadius, int rotationDirection) throws Exception {
        shape = newArcByStartEndRadiusSide(startingPoint, endingPoint, newRadius, rotationDirection);
    }

    protected void defByThreePoints_safety(Point2D startingPoint, Point2D viaPoint, Point2D endingPoint){
        shape = newArcByThreePoints_safety(startingPoint, viaPoint, endingPoint);
    }
    public void defByThreePoints(Point2D startingPoint, Point2D viaPoint, Point2D endingPoint) throws Exception {
        shape = newArcByThreePoints(startingPoint, viaPoint, endingPoint);
    }

    protected Arc2D newArcByStartEndRadiusSide_safety(Point2D startingPoint, Point2D endingPoint, double radius, Point2D sideOfCenter, int rotationDirection){
        try {
            return newArcByStartEndRadiusSide(startingPoint, endingPoint, radius, sideOfCenter, rotationDirection);
        } catch (Exception ex) {
            try {
                //a simple way to correct this eror is to offset sideOfCenter
                return newArcByStartEndRadiusSide(startingPoint, endingPoint, radius, new Point2D.Double(sideOfCenter.getX()+0.001, sideOfCenter.getY()), rotationDirection);
            } catch (Exception ex1) {
                try {
                    return newArcByStartEndRadiusSide(startingPoint, endingPoint, radius, new Point2D.Double(sideOfCenter.getX(), sideOfCenter.getY()+0.001), rotationDirection);
                } catch (Exception ex2) {
                    return shape;
                }
            }
        }
    }

    /**
     *
     * if startingPoint equals endingPoint return the arc (circle) which center
     * is on startingPoint to sideOfCenter line and which radius is radius
     *
     * @param startingPoint
     * @param endingPoint
     * @param radius
     * @param sideOfCenter
     * @param rotationDirection
     * @return
     * @throws Exception
     */
    public final Arc2D.Double newArcByStartEndRadiusSide(Point2D startingPoint, Point2D endingPoint, double radius, Point2D sideOfCenter, int rotationDirection) throws Exception {
        double newRadius = radius;
        if (radius < startingPoint.distance(endingPoint) / 2) {
            newRadius = startingPoint.distance(endingPoint) / 2;
        }
        if (startingPoint.equals(endingPoint)) {
            if (startingPoint.equals(sideOfCenter)) {
                throw new IllegalArgumentException("At least endingPoint or sideOfCenter must be different from startingPoint");
            } else {
                Point2D center = new Point2D.Double(startingPoint.getX() + (sideOfCenter.getX() - startingPoint.getX()) * newRadius / startingPoint.distance(sideOfCenter),
                        startingPoint.getY() + (sideOfCenter.getY() - startingPoint.getY()) * newRadius / startingPoint.distance(sideOfCenter));
                return newArcByCenterStartEnd(center, startingPoint, endingPoint, rotationDirection);
            }
        }
        this.rotationDirection = rotationDirection;
        this.radius = newRadius;

        Point2D circleCenter1;
        Point2D circleCenter2;
        double distStartEnd = startingPoint.distance(endingPoint);
        Point2D centerStartEndSegment = new Point2D.Double((endingPoint.getX() + startingPoint.getX()) / 2, (endingPoint.getY() + startingPoint.getY()) / 2);
        double distCircleCenterToSegmentCenter = Math.sqrt(Math.pow(radius, 2) - Math.pow(distStartEnd / 2, 2));
        circleCenter1 = new Point2D.Double(centerStartEndSegment.getX() + (endingPoint.getY() - startingPoint.getY()) / distStartEnd * distCircleCenterToSegmentCenter, centerStartEndSegment.getY() + (startingPoint.getX() - endingPoint.getX()) / distStartEnd * distCircleCenterToSegmentCenter);
        circleCenter2 = new Point2D.Double(centerStartEndSegment.getX() - (endingPoint.getY() - startingPoint.getY()) / distStartEnd * distCircleCenterToSegmentCenter, centerStartEndSegment.getY() - (startingPoint.getX() - endingPoint.getX()) / distStartEnd * distCircleCenterToSegmentCenter);

        if (sideOfCenter.distance(circleCenter1) <= sideOfCenter.distance(circleCenter2)) {
            return newArcByCenterStartEnd(circleCenter1, startingPoint, endingPoint, rotationDirection);
        } else {
            return newArcByCenterStartEnd(circleCenter2, startingPoint, endingPoint, rotationDirection);
        }
    }

    protected Arc2D newArcByStartEndRadiusSide_safety(Point2D startingPoint, Point2D endingPoint, double radius, int rotationDirection){
        try {
            return newArcByStartEndRadiusSide(startingPoint, endingPoint, radius, rotationDirection);
        } catch (Exception ex) {
            try {
                //the only case that throws an exception is start==end and radius==0
                return newArcByStartEndRadiusSide(startingPoint, endingPoint, radius+0.0001, rotationDirection);
            } catch (Exception ex1) {
                return shape;
            }
        }
    }

    public final Arc2D.Double newArcByStartEndRadiusSide(Point2D startingPoint, Point2D endingPoint, double radius, int rotationDirection) throws Exception {
        double newRadius;
        if (radius < startingPoint.distance(endingPoint) / 2) {
            newRadius = startingPoint.distance(endingPoint) / 2;
        } else {
            newRadius = radius;
        }
        this.radius = newRadius;
        Point2D circleCenter1;
        Point2D circleCenter2;
        if (startingPoint.distance(endingPoint) == 0) {
            circleCenter1 = new Point2D.Double(startingPoint.getX() + newRadius, startingPoint.getY());
            circleCenter2 = new Point2D.Double(startingPoint.getX() - newRadius, startingPoint.getY());
        } else {
            double distStartEnd = startingPoint.distance(endingPoint);
            Point2D centerStartEndSegment = new Point2D.Double((endingPoint.getX() + startingPoint.getX()) / 2, (endingPoint.getY() + startingPoint.getY()) / 2);
            double distCircleCenterToSegmentCenter = Math.sqrt(Math.pow(newRadius, 2) - Math.pow(distStartEnd / 2, 2));
            circleCenter1 = new Point2D.Double(centerStartEndSegment.getX() + (endingPoint.getY() - startingPoint.getY()) / distStartEnd * distCircleCenterToSegmentCenter, centerStartEndSegment.getY() + (startingPoint.getX() - endingPoint.getX()) / distStartEnd * distCircleCenterToSegmentCenter);
            circleCenter2 = new Point2D.Double(centerStartEndSegment.getX() - (endingPoint.getY() - startingPoint.getY()) / distStartEnd * distCircleCenterToSegmentCenter, centerStartEndSegment.getY() - (startingPoint.getX() - endingPoint.getX()) / distStartEnd * distCircleCenterToSegmentCenter);
        }
//        Arc2D arcShape = newArcByCenterStartEnd(circleCenter2, startingPoint, endingPoint, rotationDirection);
        //   System.out.println("newArcByStartEndRadiusSide " + arcShape.toString());
        //  System.out.println("newArcByStartEndRadiusSide radius " + newRadius);
        //  System.out.println("newArcByStartEndRadiusSide StartPoint " + arcShape.getStartPoint().getX() + " " + arcShape.getStartPoint().getY() + " EndPoint " + arcShape.getEndPoint().getX() + " " + arcShape.getEndPoint().getY());

        if (rotationDirection == COUNTER_CLOCK_WISE) {
            return newArcByCenterStartEnd(circleCenter1, startingPoint, endingPoint, rotationDirection);
        } else {
            return newArcByCenterStartEnd(circleCenter2, startingPoint, endingPoint, rotationDirection);
        }
    }

    protected Arc2D newArcByStartEndBendingSide_safety(Point2D startingPoint, Point2D endingPoint, double bending, String sideOfConcavity) {
        try {
            return newArcByStartEndBendingSide(startingPoint, endingPoint, bending, sideOfConcavity);
        } catch (Exception ex) {
            try {
                //try to change the bending
                return newArcByStartEndBendingSide(startingPoint, endingPoint, bending+0.0001, sideOfConcavity);
            } catch (Exception ex1) {
                try {
                    //try to change endingPoint
                    return newArcByStartEndBendingSide(startingPoint, new Point2D.Double(endingPoint.getX()+0.0001, endingPoint.getY()), bending, sideOfConcavity);
                } catch (Exception ex2) {
                    return shape;
                }
            }
        }
    }

    public final Arc2D.Double newArcByStartEndBendingSide(Point2D startingPoint, Point2D endingPoint, double bending, String sideOfConcavity) throws Exception {

        if (startingPoint.equals(endingPoint)) {
            throw new IllegalArgumentException("Starting point and ending point of the arc must be different");
        }
        if (bending == 0) {
            throw new IllegalArgumentException("Bending of the arc must be non-null (create a line instead)");
        }
        Point2D segmentCenter = new Point2D.Double((endingPoint.getX() + startingPoint.getX()) / 2, (endingPoint.getY() + startingPoint.getY()) / 2);
        double angle = Math.atan2(startingPoint.getY() - endingPoint.getY(), endingPoint.getX() - startingPoint.getX()); // taken from PathFormDrawable.getAngle()
        if (angle < 0) {
            angle += 2 * Math.PI;
        }

        if (sideOfConcavity.equalsIgnoreCase("right")) {
            Point2D viaPoint2 = new Point2D.Double(segmentCenter.getX() + Math.sin(angle) * bending, segmentCenter.getY() - Math.cos(angle) * bending);
            return newArcByThreePoints(startingPoint, viaPoint2, endingPoint);
        } else {
            Point2D viaPoint1 = new Point2D.Double(segmentCenter.getX() - Math.sin(angle) * bending, segmentCenter.getY() + Math.cos(angle) * bending);
            return newArcByThreePoints(startingPoint, viaPoint1, endingPoint);
        }

    }


    protected Arc2D newArcByThreePoints_safety(Point2D startingPoint, Point2D middlePoint, Point2D endingPoint){
        try {
            return newArcByThreePoints(startingPoint, middlePoint, endingPoint);
        } catch (Exception ex) {
            //try to change middle point
            try {
                return newArcByThreePoints(startingPoint, new Point2D.Double(middlePoint.getX()+0.0001, middlePoint.getY()), endingPoint);
            } catch (Exception ex1) {
                try {
                    return newArcByThreePoints(startingPoint, new Point2D.Double(middlePoint.getX(), middlePoint.getY()+0.0001), endingPoint);
                } catch (Exception ex2) {
                    //try to change ending point
                    try {
                        return newArcByThreePoints(startingPoint, middlePoint, new Point2D.Double(endingPoint.getX()+0.0001, endingPoint.getY()));
                    } catch (Exception ex3) {
                        try {
                            return newArcByThreePoints(startingPoint, middlePoint, new Point2D.Double(endingPoint.getX(), endingPoint.getY()+0.0001));
                        } catch (Exception ex4) {
                            return shape;
                        }
                    }
                }
            }
        }
    }
    public final Arc2D.Double newArcByThreePoints(Point2D startingPoint, Point2D middlePoint, Point2D endingPoint) throws Exception {
        if (startingPoint.equals(endingPoint) || startingPoint.equals(middlePoint) || endingPoint.equals(middlePoint)) {
            throw new IllegalArgumentException("Starting point, ending point and via point of the arc must be different");
        }
        double angle;
        angle = getAngle(startingPoint, endingPoint, middlePoint);
        if (angle < Math.PI) {
            rotationDirection = CLOCK_WISE;
        } else {
            rotationDirection = COUNTER_CLOCK_WISE;
        }
        Affin firstPerpBissector = new Affin();
        firstPerpBissector.defineAsPerpBissectorOf(startingPoint, middlePoint);
        Affin secondPerpBissector = new Affin();
        secondPerpBissector.defineAsPerpBissectorOf(middlePoint, endingPoint);
        Point2D circleCenter;
        try {
            if(Double.isInfinite(firstPerpBissector.getA())){
                if(Double.isInfinite(secondPerpBissector.getA())){
                    throw new Exception ("Infinite center");
                }
                //startingPoint and middlePoint has the same y coordinates
                //firstPerpBissector must be any point with x=(startingPoint.x+middlePoint.x)/2.0
                double x = (startingPoint.getX() + middlePoint.getX()) / 2.0;
                circleCenter = new Point2D.Double(x, secondPerpBissector.f(x));
            }
            else{
                if(Double.isInfinite(secondPerpBissector.getA())){
                    //endingPoint and middlePoint has the same y coordinates
                    //secondPerpBissector must be any point with x=(endingPoint.x+middlePoint.x)/2.0
                    double x = (endingPoint.getX() + middlePoint.getX()) / 2.0;
                    circleCenter = new Point2D.Double(x, firstPerpBissector.f(x));
                }
                else {
                    circleCenter = firstPerpBissector.intersects(secondPerpBissector);
                }
            }
            return newArcByCenterStartEnd(circleCenter, startingPoint, endingPoint, rotationDirection);
        } catch (Exception ex) {
            //Logger.getLogger(ArcDrawable.class.getName()).log(Level.SEVERE, null, ex);
            return newArcByStartEndRadiusSide(startingPoint, endingPoint, Float.MAX_VALUE, rotationDirection);

        }

    }

    protected Arc2D newArcByCenterStartEnd_safety(Point2D circleCenter, Point2D startingPoint, Point2D endingPoint, int rotationDirection){
        try {
            return newArcByCenterStartEnd(circleCenter, startingPoint, endingPoint, rotationDirection);
        } catch (Exception ex) {
            try {
                return newArcByCenterStartEnd(new Point2D.Double(circleCenter.getX()+0.0001, circleCenter.getY()), startingPoint, endingPoint, rotationDirection);
            } catch (Exception ex1) {
                return shape;
            }
        }
    }

    /*    public final Arc2D.Double newArcByCenterStartEnd(Point2D circleCenter, Point2D startingPoint, Point2D endingPoint) {
     return this.newArcByCenterStartEnd(circleCenter, startingPoint, endingPoint, rotationDirection);
     }*/
    public final Arc2D.Double newArcByCenterStartEnd(Point2D circleCenter, Point2D startingPoint, Point2D endingPoint, int rotationDirection) throws Exception {
        Arc2D.Double newArc;
        this.rotationDirection = rotationDirection;
        if (circleCenter.equals(startingPoint) || circleCenter.equals(endingPoint)) {
            throw new IllegalArgumentException("CircleCenter must be different from both start and end points of the arc");
        }
        double newRadius = Math.max(circleCenter.distance(startingPoint), circleCenter.distance(endingPoint));
        //   double angStartRad = Math.atan2(circleCenter.getY() - startingPoint.getY(), startingPoint.getX()-circleCenter.getX());
        double angStartRad = getAngle(circleCenter, startingPoint);// getAngle angle return a value between 0 and 2*PI
        //    double angExtentRad = Math.atan2(circleCenter.getY() - endingPoint.getY() , endingPoint.getX() - circleCenter.getX()) - angStartRad;
        double angExtentRad = getAngle(circleCenter, endingPoint) - angStartRad;
        /*  while (angStartRad < 0) {
         angStartRad += 2 * Math.PI;
         angExtentRad += 2 * Math.PI;
         roundOffset += 2 * Math.PI;
         }
         angExtentRad += roundOffset;
         roundOffset = 0;*/

        if (rotationDirection == COUNTER_CLOCK_WISE && angExtentRad < 0) {
            angExtentRad += 2 * Math.PI;
        } else if (rotationDirection == CLOCK_WISE && angExtentRad > 0) {
            angExtentRad -= 2 * Math.PI;
        }
        angStart = Math.toDegrees(angStartRad);
        angExtent = Math.toDegrees(angExtentRad);
        radius = newRadius;
        newArc = new Arc2D.Double(circleCenter.getX() - newRadius, circleCenter.getY() - newRadius, newRadius * 2, newRadius * 2, angStart, angExtent, closure);
        return newArc;
    }

    @Override
    public Point2D getStartPoint() {
        return shape.getStartPoint();
    }

    @Override
    public double getStartPointX() {
        return shape.getStartPoint().getX();
    }

    @Override
    public double getStartPointY() {
        return shape.getStartPoint().getY();
    }

    @Override
    public Point2D getEndPoint() {
        return shape.getEndPoint();
    }

    @Override
    public double getEndPointX() {
        return shape.getEndPoint().getX();
    }

    @Override
    public double getEndPointY() {
        return shape.getEndPoint().getY();
    }

    @Override
    public void setStartPoint(Point2D pos) {
        shape = newArcByStartEndRadiusSide_safety(pos, shape.getEndPoint(), this.getRadius(), this.getCenter(), rotationDirection);
//        try {
//            //    System.out.println("Arc setStartPoint pos " + pos.getX() + " " + pos.getY());
//            //    System.out.println("Arc setStartPoint oldEndPoint " + shape.getEndPoint().getX() + " " + shape.getEndPoint().getY());
//            defByStartEndRadiusSide(pos, shape.getEndPoint(), this.getRadius(), this.getCenter());
//        } catch (Exception ex) {
//            try {
//                defByStartEndRadiusSide(pos, shape.getEndPoint(), this.getRadius(), new Point2D.Double(pos.getX()+0.0001,pos.getY()));
//            } catch (Exception ex1) {
//                // No problems
//            }
//        }
    }

    public void setStartAndViaPoints(Point2D startPos, Point2D viaPos) throws Exception{
            defByThreePoints(startPos, viaPos, shape.getEndPoint());
    }

    @Override
    public void setEndPoint(Point2D pos) {
        shape = newArcByStartEndRadiusSide_safety(shape.getStartPoint(), pos, this.getRadius(), this.getCenter(), rotationDirection);
//        try {
//            //     System.out.println("Arc setEndPoint pos " + pos.getX() + " " + pos.getY());
//            defByStartEndRadiusSide(shape.getStartPoint(), pos, this.getRadius(), this.getCenter());
//        } catch (Exception ex) {
//            try {
//                defByStartEndRadiusSide(shape.getStartPoint(), pos, this.getRadius(), new Point2D.Double(pos.getX()+0.0001,pos.getY()));
//            } catch (Exception ex1) {
//                // No problems
//            }
//        }
    }

    public void setEndAndViaPoints(Point2D endPos, Point2D viaPos) throws Exception{
        //     System.out.println("Arc setStartPoint pos " + endPos.getX() + " " + endPos.getY());
        //     System.out.println("Arc setStartPoint oldEndPoint " + shape.getEndPoint().getX() + " " + shape.getEndPoint().getY());
        defByThreePoints(shape.getStartPoint(), viaPos, endPos);
    }

    private abstract class PositionSetter {

        public abstract void setPosition(Point2D pos);
    }

    private class P1PositionSetter extends PositionSetter {

        @Override
        public void setPosition(Point2D pos) {
            setStartPoint(pos);
            // shape.setLine(pos, shape.getP2());
        }
    }

    private class P2PositionSetter extends PositionSetter {

        @Override
        public void setPosition(Point2D pos) {
            setEndPoint(pos);
            //shape.setLine(shape.getP1(), pos);
        }
    }
}
