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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

/**
 *
 * @author Ken Prepin
 */
public class LineArrowWithName extends LineArrow {

    TextDrawable textDrawable;
    boolean drawText = false;
//    LineDrawable lineBegin;
//    LineArrow lineEndWithArrow;
    Color textColor;

    public LineArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, String name) {
        this(color, startingPoint, endingPoint, name, DEFAULT_STROKE);
    }
    public LineArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, String name, BasicStroke stroke) {
        super(color, startingPoint, endingPoint, stroke);
        textDrawable = new TextDrawable(color, name);
        textColor = color;
        updateStartAndEndPosition();
        //       text.setPosition(new Point2D.Double(shape.getBounds2D().getCenterX(), shape.getBounds2D().getCenterY()), getAngle());
    }

    public LineArrowWithName(Color color, Point2D startingPoint, Point2D endingPoint, String name, float stroke) {
        super(color, startingPoint, endingPoint, stroke);
        textDrawable = new TextDrawable(color, name);
        textColor = color;
        updateStartAndEndPosition();
        //       text.setPosition(new Point2D.Double(shape.getBounds2D().getCenterX(), shape.getBounds2D().getCenterY()), getAngle());
    }

    @Override
    public void draw(Graphics g) {
       //super.draw(g); // Do not draw super, super is just here for its shape
//        lineBegin.draw(g);
//        lineEndWithArrow.draw(g);
        if(drawText){
            textDrawable.draw(g);
            drawArrow(g);
            Shape originalClip = g.getClip();
            Area clip = new Area(originalClip);
            clip.subtract(textDrawable.getArea());
//            Area arrowArea = new Area(arrow);
//            AffineTransform arrowTransform = new AffineTransform();
//            arrowTransform.translate(shape.getX2(),shape.getY2());
//            arrowTransform.rotate(arrowDirection);
//            arrowArea.transform(arrowTransform);
//            clip.add(arrowArea);
            g.setClip(clip);
            drawLine(g);
            g.setClip(originalClip);
        }
        else{
            super.draw(g);
        }
    }
    // where the control point for the intersection of the V needs calculating
    // by projecting where the ends meet

    @Override
    public void setStartPoint(Point2D pos) {
        super.setStartPoint(pos);
        updateStartAndEndPosition();

    }
    public void setStartAndEndPoints(Point2D startPoint, Point2D endPoint){
        setStartPoint(startPoint);
        setEndPoint(endPoint);
    }
    public void setText(String text) {
        this.textDrawable = new TextDrawable(textColor, text);
        updateStartAndEndPosition();
    }

    @Override
    public void setEndPoint(Point2D pos) {
        super.setEndPoint(pos);
        updateStartAndEndPosition();
    }

    public void updateStartAndEndPosition() {
  //      lineBegin = new LineDrawable(color, shape.getP1(), new Point2D.Double(shape.getP1().getX() + (shape.getP1().distance(shape.getP2()))* Math.cos(getAngle()), shape.getP1().getY()));// + shape.getP2().getY() + Math.sin(getAngle()) * textDrawable.getLength()) / 2));
   //     lineEndWithArrow = new LineArrow(color, new Point2D.Double(shape.getP2().getX(), shape.getP1().getY() + (shape.getP1().distance(shape.getP2()))* Math.sin(getAngle())), shape.getP2());
//      lineBegin = new LineDrawable(color, shape.getP1(), new Point2D.Double((shape.getP1().getX() + shape.getP2().getX() - Math.cos(getAngle()) * textDrawable.getLength()) / 2, (shape.getP1().getY() + shape.getP2().getY() + Math.sin(getAngle()) * textDrawable.getLength()) / 2), stroke);
//        lineEndWithArrow = new LineArrow(color, new Point2D.Double((shape.getP1().getX() + shape.getP2().getX() + Math.cos(getAngle()) * textDrawable.getLength()) / 2, (shape.getP1().getY() + shape.getP2().getY()) / 2 - Math.sin(getAngle()) * textDrawable.getLength() / 2), shape.getP2(), stroke);
        double angle = 0;
        try {
            angle = getAngle();
        } catch (Exception e) {
        }
        textDrawable.setPosition(new Point2D.Double(shape.getBounds2D().getCenterX(), shape.getBounds2D().getCenterY()), angle);
        drawText = textDrawable.getText()!=null && !textDrawable.getText().isEmpty() && textDrawable.getLength()<shape.getP1().distance(shape.getP2())-2*ARROW_LENGTH;
     }

    public Dimension getTextSize(String text, Graphics g) {
        // get metrics from the graphics
        FontMetrics metrics = g.getFontMetrics();
        // get the height of a line of text in this
        // font and render context
        int hgt = metrics.getHeight();
        // get the advance of my text in this font
        // and render context
        int adv = metrics.stringWidth(text);
        // calculate the size of a box to hold the
        // text with some padding.
        Dimension size = new Dimension(adv + 2, hgt + 2);
        return size;
    }
}
