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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 *
 * @author Ken Prepin
 */
public class DiscDrawable extends CircleDrawable {

    public DiscDrawable(Color color, Point2D center, double radius) {
        super(color, center, radius, 2);
    }
    public DiscDrawable(Color color, Point2D center, double radius, float stroke) {
        super(color, center, radius, stroke);
    }

    @Override
    public void draw(Graphics g) {
        Color c = g.getColor();
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(stroke);
        g2d.setColor(color);
        g2d.fill(shape);
//        g2d.draw(shape); //makes some artifacts for little radius
        // set the initial color of Graphics back
        g.setColor(c);
    }
}
