/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.aulibrary;

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
