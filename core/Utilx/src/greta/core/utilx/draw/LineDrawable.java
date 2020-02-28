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
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 *
 * @author Ken Prepin
 */
public class LineDrawable extends PathFormDrawable<Line2D> {

    PositionSetter ps;
    String name;
    boolean drawName;

    public LineDrawable(Color color, Point2D startingPoint, Point2D endingPoint) {
        super(color, new Line2D.Double(startingPoint, endingPoint));
        ps = new P2PositionSetter();
        this.name = "";
        drawName = false;
    }

    public LineDrawable(Color color, Point2D startingPoint, Point2D endingPoint, float stroke) {
        super(color, new Line2D.Double(startingPoint, endingPoint), stroke);
        ps = new P2PositionSetter();
        this.name = "";
        drawName = false;
    }
    public LineDrawable(Color color, Point2D startingPoint, Point2D endingPoint, BasicStroke stroke) {
        super(color, new Line2D.Double(startingPoint, endingPoint), stroke);
        ps = new P2PositionSetter();
        this.name = "";
        drawName = false;
    }

    public LineDrawable(Color color, Point2D startingPoint, Point2D endingPoint, String name) {

        super(color, new Line2D.Double(startingPoint, endingPoint));
        ps = new P2PositionSetter();
        this.name = name;
        drawName = true;
    }

    public LineDrawable(Color color, Point2D startingPoint, Point2D endingPoint, String name, float stroke) {
        super(color, new Line2D.Double(startingPoint, endingPoint), stroke);
        ps = new P2PositionSetter();
        this.name = name;
        drawName = true;
    }

    public LineDrawable(Color color, Point2D startingPoint, Point2D endingPoint, String name, BasicStroke stroke) {
        super(color, new Line2D.Double(startingPoint, endingPoint), stroke);
        ps = new P2PositionSetter();
        this.name = name;
        drawName = true;
    }

    @Override
    public void draw(Graphics g) {
        Color c = g.getColor();
        Graphics2D g2d = (Graphics2D) g.create();
  /*      if(drawName){
            Dimension nameSize = getTextSize(name,g);
            Shape lineBeforeText = new Line2D.Double(startingPoint, new Point2D(shape.getBounds2D().getHeight()- nameSize.))
        }*/
        g2d.setStroke(stroke);
        g2d.setColor(color);
  //      System.out.println("shape " + shape.toString());
   //     System.out.println("shape StartPoint " + shape.getX1() +" "+shape.getY1() +" EndPoint "+shape.getX2() +" "+shape.getY2());
        g2d.draw(shape);
        // set the initial color of Graphics back
        g.setColor(c);
    }

    @Override
    public void setPosition(Point2D pos) {
        if (ps != null) {
            ps.setPosition(pos);
        }
    }

    @Override
    public void setMousePressedPosition(Point2D pos) {
        if (shape.getP1().distance(pos) < 10) {
            ps = new P1PositionSetter();
        } else if (shape.getP2().distance(pos) < 10) {
            ps = new P2PositionSetter();
        } else {
            ps = null;
        }
    }

    /**
     * Return the distance from a point to a segment
     *
     * @param ps,pe the start/end of the segment
     * @param p the given point
     * @return the distance from the given point to the segment
     */
    private static double distanceToSegment(Point2D ps, Point2D pe, Point2D p) {

        if (ps.getX() == pe.getX() && ps.getY() == pe.getY()) {
            return distance(ps, p);
        }

        double sx = pe.getX() - ps.getX();
        double sy = pe.getY() - ps.getY();

        double ux = p.getX() - ps.getX();
        double uy = p.getY() - ps.getY();

        double dp = sx * ux + sy * uy;
        if (dp < 0) {
            return distance(ps, p);
        }

        double sn2 = sx * sx + sy * sy;
        if (dp > sn2) {
            return distance(pe, p);
        }

        double ah2 = dp * dp / sn2;
        double un2 = ux * ux + uy * uy;
        return Math.sqrt(un2 - ah2);
    }

    /**
     * return the distance between two points
     *
     * @param p1,p2 the two points
     * @return dist the distance
     */
    private static double distance(Point2D p1, Point2D p2) {
        double d2 = (p2.getX() - p1.getX()) * (p2.getX() - p1.getX()) + (p2.getY() - p1.getY()) * (p2.getY() - p1.getY());
        return Math.sqrt(d2);
    }

    @Override
    public void setCenter(Point2D p) {
        Point2D newP1 = new Point2D.Double(p.getX() - shape.getBounds2D().getWidth() / 2, p.getY() - shape.getBounds2D().getHeight() / 2);
        Point2D newP2 = new Point2D.Double(p.getX() + shape.getBounds2D().getWidth() / 2, p.getY() + shape.getBounds2D().getHeight() / 2);
        shape.setLine(newP1, newP2);
    }

    @Override
    public Point2D getStartPoint() {
        return shape.getP1();
    }

    @Override
    public double getStartPointX() {
        return shape.getX1();
    }

    @Override
    public double getStartPointY() {
        return shape.getY1();
    }

    @Override
    public Point2D getEndPoint() {
        return shape.getP2();
    }

    @Override
    public double getEndPointX() {
        return shape.getX2();
    }

    @Override
    public double getEndPointY() {
        return shape.getY2();
    }

    @Override
    public void setStartPoint(Point2D pos) {
        shape.setLine(pos, shape.getP2());
    }

    @Override
    public void setEndPoint(Point2D pos) {
        shape.setLine(shape.getP1(), pos);
    }

     @Override
    public boolean intersects(Point2D p) {
        // System.out.println("check intersects on line " + distanceToSegment(shape.getP1(), shape.getP2(), p) <= 5));
        return distanceToSegment(shape.getP1(), shape.getP2(), p) <= 5;
    }
    private abstract class PositionSetter {

        public abstract void setPosition(Point2D pos);
    }

    private class P1PositionSetter extends PositionSetter {

        @Override
        public void setPosition(Point2D pos) {
            shape.setLine(pos, shape.getP2());
        }
    }

    private class P2PositionSetter extends PositionSetter {

        @Override
        public void setPosition(Point2D pos) {
            shape.setLine(shape.getP1(), pos);
        }
    }

}
