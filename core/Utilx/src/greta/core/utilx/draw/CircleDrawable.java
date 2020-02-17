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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 *
 * @author Ken Prepin
 */
public class CircleDrawable extends FormDrawable<Ellipse2D> {

    TextDrawable name;
    boolean drawName;

    private static Ellipse2D getEllipse(Point2D center, double radius){
        return getEllipse(center.getX(), center.getY(), radius);
    }
    private static Ellipse2D getEllipse(double centerX, double centerY, double radius){
        return new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    public CircleDrawable(Color color, Point2D center, double radius) {
        this(color, center, radius,"", DEFAULT_STROKE);
    }
   public CircleDrawable(Color color, Point2D center, double radius, float lineWidth) {
        this(color, center, radius,"", lineWidth);
    }
   public CircleDrawable(Color color, Point2D center, double radius, String name) {
        this(color, center, radius, name, DEFAULT_STROKE);
    }
    public CircleDrawable(Color color, Point2D center, double radius, String name, float lineWidth) {
        this(color, center, radius, name, new BasicStroke(lineWidth));
    }
    public CircleDrawable(Color color, Point2D center, double radius, BasicStroke stroke) {
        this(color, center, radius, "", stroke);
    }
    public CircleDrawable(Color color, Point2D center, double radius, String name, BasicStroke stroke) {
        //   super(color, new Ellipse2D.Double(center.getX()-(radius+stroke/2),center.getY()-(radius+stroke/2), radius*2, radius*2), new BasicStroke(stroke));
        super(color, getEllipse(center, radius), stroke);
        //   this.radius = radius+stroke/2;
        mousePressedPosition = new Point2D.Double(0, 0);
        if (name.equalsIgnoreCase("")) {
            drawName = false;
        } else {
            setName(name);
        }
    }

    public void setName(String name){
        drawName = true;
        if(this.name!=null){
            this.name.setText(name);
        } else {
            this.name = new TextDrawable(Color.BLACK,name);
            this.name.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));
        }
        setCenter(this.getCenter());
    }
    @Override
    public void draw(Graphics g) {
        Color c = g.getColor();
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(stroke);
        g2d.setColor(color);
        g2d.draw(shape);
        if(drawName){
            name.draw(g);
        }
        // set the initial color of Graphics back
        g.setColor(c);
    }

    @Override
    public void setPosition(Point2D pos) {
        shape = getEllipse(pos.getX() - mousePressedPosition.getX(), pos.getY() - mousePressedPosition.getY(), getRadius());
        if(drawName){
            name.setPosition(new Point2D.Double(this.getCenter().getX(),this.getCenter().getY()+ this.getRadius()+name.getHeight()));
        }

    }

    @Override
    public void setMousePressedPosition(Point2D p) {
        mousePressedPosition.setLocation(p.getX() - getCenter().getX(), p.getY() - getCenter().getY());
    }

    public double getRadius() {
        return this.getBounds().getWidth() / 2;
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
        Point2D center = getCenter();
        return center.distance(p) <= radius;

    }


    @Override
    public void setCenter(Point2D center) {
        //     shape = new Ellipse2D.Double(center.getX() - mousePressedPosition.getX()-shape.getWidth()/2,center.getY() - mousePressedPosition.getY()-shape.getHeight()/2, shape.getWidth(), shape.getHeight());
        shape = getEllipse(center, getRadius());
        if(drawName){
            name.setCenter(new Point2D.Double(this.getCenter().getX(),this.getCenter().getY()+ this.getRadius()+name.getHeight()));
        }
    }

    public void setRadius(double newRadius) {
        double oldRadius = getRadius();
        if (Math.abs(oldRadius - newRadius) > 0.1) {
            //        Point2D.Double newPosition = new Point2D.Double(getCenter().getX() - (newRadius+stroke.getLineWidth()), getCenter().getY() - (newRadius+stroke.getLineWidth()));
            shape = getEllipse(getCenter(), newRadius);
        }
    }
}
