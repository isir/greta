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
