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
