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
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Ken Prepin
 */
public abstract class FormDrawable<SHAPE extends Shape> implements IMovableDrawable {

    protected SHAPE shape;
    protected Color color;
    protected BasicStroke stroke;
    protected boolean dashed;
    protected float lineWidth;
    protected static final float DEFAULT_STROKE = 0.5f;
    protected static final float DEFAULT_LINE_EMPHASIS = 0.5f;
    protected static final Color DEFAULT_COLOR = Color.BLACK;
    protected Point2D.Double mousePressedPosition;
    protected boolean selected;

    public FormDrawable() {
        super();
    }

    public FormDrawable(Color color, SHAPE shape) {
        this(color, shape, DEFAULT_STROKE);
    }

    public FormDrawable(Color color, SHAPE shape, float stroke) {
        this(color, shape, new BasicStroke(stroke));
    }

    public FormDrawable(Color color, SHAPE shape, BasicStroke stroke) {
        this.color = color;
        this.shape = shape;
        this.stroke = stroke;
        this.lineWidth = stroke.getLineWidth();
        mousePressedPosition = null;
        this.selected = false;
    }

    @Override
    public abstract void draw(Graphics g);

    @Override
    public SHAPE getShape() {
        return shape;
    }

    @Override
    public Point2D getPosition() {
        return new Point2D.Double(this.getBounds().getX(), this.getBounds().getY());
    }

    @Override
    public Point2D getCenter() {
        return new Point2D.Double(this.getBounds().getCenterX(), this.getBounds().getCenterY());
    }

    @Override
    public Rectangle2D getBounds() {
        //   return stroke.createStrokedShape(shape).getBounds2D();
        return shape.getBounds2D();
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Color getColor(){
        return color;
    }

    @Override
    public void setStroke(BasicStroke stroke) {
        this.stroke = stroke;
        this.lineWidth = stroke.getLineWidth();
    }

    public void setLineWidth(float lineWidth) {
        BasicStroke newStroke;
        if (dashed) {
            newStroke = new BasicStroke(lineWidth, stroke.getEndCap(), stroke.getLineJoin(), stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase());
        } else {
            newStroke = new BasicStroke(lineWidth);
        }
        setStroke(newStroke);
    }

    public void setDashed(boolean dashed) {
        BasicStroke newStroke;
        this.dashed = dashed;
        if (dashed) {
            float dash1[] = {5.0f};
            newStroke = new BasicStroke(this.stroke.getLineWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash1, 0.0f);
        } else {
            newStroke = new BasicStroke(this.stroke.getLineWidth());
        }
        setStroke(newStroke);
    }

    public void setSelected(boolean selected, float strokeEmphasis) {
        if (this.selected && !selected) {
            BasicStroke newStroke;
            this.dashed = dashed;
            if (dashed) {
                float dash1[] = {5.0f};
                newStroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash1, 0.0f);
            } else {
                newStroke = new BasicStroke(lineWidth);
            }
            setStroke(newStroke);
        } else if (selected && !this.selected) {
            BasicStroke newStroke;
            this.dashed = dashed;
            if (dashed) {
                float dash1[] = {10.0f};
                newStroke = new BasicStroke(lineWidth + strokeEmphasis, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
            } else {
                newStroke = new BasicStroke(lineWidth + strokeEmphasis);
            }
            setStroke(newStroke);
        }
    }

        public void setSelected(boolean selected) {
            setSelected(selected,DEFAULT_LINE_EMPHASIS);
        }

}
