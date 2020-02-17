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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Ken Prepin
 */
public class TextDrawable extends FormDrawable<Shape> {

    public static final Font DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    private AffineTransform affineTransform;
    static final int MARGIN = 10;
    private Font font;
    private String text;
    private Rectangle2D bounds;

    public TextDrawable(Color color, String text) {
        this(color, new Point2D.Double(0, 0), 0, text, DEFAULT_FONT);
    }

    public TextDrawable(Color color, Point2D initPosition, double rotationRadian, String text) {
        this(color, initPosition, rotationRadian, text, DEFAULT_FONT);
    }

    public TextDrawable(Color color, Point2D initPosition, double rotationRadian, String text, Font font) {
        super(color, generateShapeFromText(font, text));
        this.font = font;
        this.text = text;
        affineTransform = new AffineTransform();
        bounds = shape.getBounds2D();
  //      setPosition(new Point2D.Double(-500,-500));
        //     setPosition(initPosition, rotationRadian);
    }

    public static Shape generateShapeFromText(Font font, char ch) {
        return generateShapeFromText(font, String.valueOf(ch));
    }

    public static Shape generateShapeFromText(Font font, String string) {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        try {
            GlyphVector vect = font.createGlyphVector(g2.getFontRenderContext(), string);
            Shape shape = vect.getOutline(0f, (float) -vect.getVisualBounds().getY());

            return shape;
        } finally {
            g2.dispose();
        }
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
        this.shape = generateShapeFromText(font, this.text);
        this.bounds = this.shape.getBounds2D();
    }

    @Override
    public Rectangle2D getBounds() {
        return bounds;
    }

    @Override
    public void draw(Graphics g) {
        Color c = g.getColor();
        Graphics2D g2d = (Graphics2D) g.create();
        // g2d.setStroke(stroke);
        g2d.transform(affineTransform);
        g2d.setColor(color);
        g2d.fill(shape);
        // set the initial color of Graphics back
        g.setColor(c);
    }

    public double getLength() {
        return shape.getBounds2D().getWidth() + MARGIN;
    }

    public double getHeight() {
        return shape.getBounds2D().getHeight();
    }

    public double getMargin() {
        return MARGIN;
    }

    public Area getArea() {
        Area area = new Area(new RoundRectangle2D.Double(bounds.getX() - MARGIN / 2.0, bounds.getY() - MARGIN / 4.0, bounds.getWidth() + MARGIN, bounds.getHeight() + MARGIN / 2.0, bounds.getHeight(), bounds.getHeight()));
        area.transform(affineTransform);
        return area;
    }

    @Override
    public void setPosition(Point2D newCenter) {
        Point2D currentCenter = new Point2D.Double(shape.getBounds2D().getCenterX(), shape.getBounds2D().getCenterY());
        affineTransform.setToTranslation(newCenter.getX() - currentCenter.getX(), newCenter.getY() - currentCenter.getY());
    }

    public void setPosition(Point2D newCenter, double radianAngle) {
//        Point2D currentCenter = new Point2D.Double(shape.getBounds2D().getCenterX(),shape.getBounds2D().getCenterY());
        if (radianAngle > Math.PI / 2 && radianAngle < 3 * Math.PI / 2) {
            radianAngle += Math.PI; // Change the orientation of the text.
        }
        //  affineTransform.setToTranslation(newCenter.getX()- Math.cos(radianAngle)*(getLength()-MARGIN)/2 + Math.sin(radianAngle)*getHeight()/2 - currentCenter.getX(),newCenter.getY() - Math.sin(radianAngle)*(getLength()-MARGIN)/2-Math.cos(radianAngle)*getHeight()/2 - currentCenter.getY());
        affineTransform.setToTranslation(newCenter.getX() - Math.cos(radianAngle) * (getLength() - MARGIN) / 2 - Math.sin(radianAngle) * getHeight() / 2, newCenter.getY() + Math.sin(radianAngle) * (getLength() - MARGIN) / 2 - Math.cos(radianAngle) * getHeight() / 2);
        affineTransform.rotate(-radianAngle);

    }

    @Override
    public void setCenter(Point2D newCenter) {
        Point2D currentCenter = new Point2D.Double(shape.getBounds2D().getCenterX(), shape.getBounds2D().getCenterY());
        affineTransform.setToTranslation(newCenter.getX() - currentCenter.getX(), newCenter.getY() - currentCenter.getY());
    }

    public void setRotation(double radianAngle) {
        affineTransform.rotate(radianAngle);
    }

    @Override
    public void setMousePressedPosition(Point2D pos) {

    }

    @Override
    public boolean intersects(Point2D p) {
        return false;
    }

    public void setText(String text) {
        if (!text.equals(this.text)) {
            this.text = text;
            shape = generateShapeFromText(font, text);
            bounds = shape.getBounds2D();
        }
    }

    public String getText() {
        return this.text;
    }
}
