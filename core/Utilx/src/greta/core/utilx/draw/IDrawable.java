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

/**
 *
 * @author Ken Prepin
 */
public interface IDrawable {

    public abstract void draw(Graphics g);

    public abstract Shape getShape();

    public abstract boolean intersects(Point2D p);

    public abstract void setColor(Color color);

    public abstract Color getColor();

    public abstract void setStroke(BasicStroke stroke);
}
