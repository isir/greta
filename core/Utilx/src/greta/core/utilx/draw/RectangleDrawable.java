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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Ken Prepin
 */
public class RectangleDrawable extends FormDrawable<Rectangle2D> {

    PositionSetter ps;
    TextDrawable name;
    boolean drawName;
    protected int cornerSensibility = 10;

    public RectangleDrawable(Color color, Point2D startingPoint, Point2D endingPoint) {
        this(color, startingPoint, endingPoint, "");
    }

    public RectangleDrawable(Color color, Point2D startingPoint, Point2D endingPoint, String name) {
        super(color, new Rectangle2D.Double(startingPoint.getX(), startingPoint.getY(), endingPoint.getX() - startingPoint.getX(), endingPoint.getY() - startingPoint.getX()));
        ps = new CornerPositionSetter(startingPoint);
        if (name.equalsIgnoreCase("")) {
            drawName = false;
        } else {
            drawName = true;
            this.name = new TextDrawable(color, name);
        }
    }

    public RectangleDrawable(Color color, Point2D startingPoint, Point2D endingPoint, float stroke) {
        this(color, startingPoint, endingPoint, "", stroke);
    }

    public RectangleDrawable(Color color, Point2D startingPoint, Point2D endingPoint, String name, float stroke) {
        super(color, new Rectangle2D.Double(startingPoint.getX(), startingPoint.getY(), endingPoint.getX() - startingPoint.getX(), endingPoint.getY() - startingPoint.getX()), stroke);
        ps = new CornerPositionSetter(startingPoint);
        if (name.equalsIgnoreCase("")) {
            drawName = false;
        } else {
            drawName = true;
            this.name = new TextDrawable(color, name);
        }
    }

    public RectangleDrawable(Color color, Point2D startingPoint, Point2D endingPoint, BasicStroke stroke) {
        this(color, startingPoint, endingPoint, "", stroke);
    }

    public RectangleDrawable(Color color, Point2D startingPoint, Point2D endingPoint, String name, BasicStroke stroke) {
        super(color, new Rectangle2D.Double(startingPoint.getX(), startingPoint.getY(), endingPoint.getX() - startingPoint.getX(), endingPoint.getY() - startingPoint.getX()), stroke);
        ps = new CornerPositionSetter(startingPoint);
        if (name.equalsIgnoreCase("")) {
            drawName = false;
        } else {
            drawName = true;
            this.name = new TextDrawable(color, name);
        }
    }

    public void setName(String name) {
        drawName = true;
        this.name = new TextDrawable(Color.BLACK, name);
        this.name.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setCenter(this.getCenter());
    }

    @Override
    public void draw(Graphics g) {
        Color c = g.getColor();
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(stroke);
        g2d.setColor(color);
        g2d.draw(shape);
        if (drawName) {
            name.draw(g);
        }
        // set the initial color of Graphics back
        g.setColor(c);
    }

    @Override
    public void setPosition(Point2D p) {
        if (ps != null) {
            ps.setPosition(p);
        }
    }

    @Override
    public void setCenter(Point2D center) {
        //     shape = new Ellipse2D.Double(center.getX() - mousePressedPosition.getX()-shape.getWidth()/2,center.getY() - mousePressedPosition.getY()-shape.getHeight()/2, shape.getWidth(), shape.getHeight());
        shape = new Rectangle2D.Double(center.getX() - shape.getWidth() / 2, center.getY() - shape.getHeight() / 2, shape.getWidth(), shape.getHeight());
        if (drawName) {
            name.setCenter(new Point2D.Double(center.getX(), center.getY() + shape.getHeight() / 2 + name.getHeight()));
        }
    }

    @Override
    public void setMousePressedPosition(Point2D pos) {
        if (this.getUpLeftPoint().distance(pos) < cornerSensibility) {
            ps = new CornerPositionSetter(getDownRightPoint());
        } else if (this.getDownRightPoint().distance(pos) < cornerSensibility) {
            ps = new CornerPositionSetter(getUpLeftPoint());
        } else if (this.getUpRightPoint().distance(pos) < cornerSensibility) {
            ps = new CornerPositionSetter(getDownLeftPoint());
        } else if (this.getDownLeftPoint().distance(pos) < cornerSensibility) {
            ps = new CornerPositionSetter(getUpRightPoint());
        } else {
            ps = new MousePositionSetter(pos);
        }
    }

    public Point2D getStartPoint() {
        return new Point2D.Double(shape.getX(), shape.getY());
    }

    public double getStartPointX() {
        return shape.getX();
    }

    public double getStartPointY() {
        return shape.getY();
    }

    public Point2D getEndPoint() {
        return new Point2D.Double(shape.getX() + shape.getWidth(), shape.getY() + shape.getHeight());
    }

    public double getEndPointX() {
        return shape.getX() + shape.getWidth();
    }

    public double getEndPointY() {
        return shape.getY() + shape.getHeight();
    }

    public void setStartPoint(Point2D pos) {
        shape.setRect(pos.getX(), pos.getY(), getEndPointX() - pos.getX(), getEndPointY() - pos.getY());
    }

    public void setEndPoint(Point2D pos) {
        shape.setRect(getStartPointX(), getStartPointY(), pos.getX() - getStartPointX(), pos.getY() - getStartPointY());
    }

    public double getUp() {
        return shape.getY();
    }

    public double getDown() {
        return shape.getY() + shape.getHeight();
    }

    public double getLeft() {
        return shape.getX();
    }

    public double getRight() {
        return shape.getX() + shape.getWidth();
    }

    public Point2D getUpLeftPoint() {
        return new Point2D.Double(getLeft(), getUp());
    }

    public Point2D getUpRightPoint() {
        return new Point2D.Double(getRight(), getUp());
    }

    public Point2D getDownLeftPoint() {
        return new Point2D.Double(getLeft(), getDown());
    }

    public Point2D getDownRightPoint() {
        return new Point2D.Double(getRight(), getDown());
    }

    @Override
    public boolean intersects(Point2D p) {
        Rectangle2D.Double rect = (Rectangle2D.Double) shape;
        return rect.contains(p);
    }

    private abstract class PositionSetter {

        public abstract void setPosition(Point2D pos);
    }

    private class CornerPositionSetter extends PositionSetter {

        Point2D oppositCorner;

        CornerPositionSetter(Point2D opposit) {
            oppositCorner = opposit;
        }

        @Override
        public void setPosition(Point2D pos) {
            double x = Math.min(pos.getX(), oppositCorner.getX());
            double y = Math.min(pos.getY(), oppositCorner.getY());
            double w = Math.abs(pos.getX() - oppositCorner.getX());
            double h = Math.abs(pos.getY() - oppositCorner.getY());
            shape.setRect(x, y, w, h);
            if (drawName) {
                name.setCenter(new Point2D.Double(shape.getCenterX(), shape.getCenterY() + shape.getHeight() / 2 + name.getHeight()));
            }

        }
    }

    private class MousePositionSetter extends PositionSetter {

        Point2D mousePressedPosition;

        MousePositionSetter(Point2D pos) {
            mousePressedPosition = new Point2D.Double(pos.getX() - getStartPointX(), pos.getY() - getStartPointY());
        }

        @Override
        public void setPosition(Point2D pos) {
            shape.setRect(
                    pos.getX() - mousePressedPosition.getX(),
                    pos.getY() - mousePressedPosition.getY(),
                    shape.getWidth(),
                    shape.getHeight());
            if (drawName) {
                name.setCenter(new Point2D.Double(shape.getCenterX(), shape.getCenterY() + shape.getHeight() / 2 + name.getHeight()));
            }

        }
    }
}
