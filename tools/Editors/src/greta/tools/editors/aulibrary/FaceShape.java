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
package greta.tools.editors.aulibrary;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class FaceShape {

    private List<List<FacePoint>> allpoints;
    public FaceShape(){
        allpoints = new ArrayList<List<FacePoint>>();
    }

    public FaceShape(FacePoint... facePoints){
        this();
        appendSegment(facePoints);
    }

    public final void appendSegment(FacePoint... facePoints){
        allpoints.add(Arrays.asList(facePoints));
    }

    public Shape getShape() {
        Path2D.Double shape = new Path2D.Double();
        for(List<FacePoint> points : allpoints){
            FacePoint first = points.get(0);
            shape.moveTo(first.getX(), first.getY());
            for (int i=0; i<points.size(); ++i) {
                FacePoint point = points.get(i);
                shape.lineTo(point.getX(), point.getY());
            }
        }
        return shape;
    }
}
