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
import java.awt.Shape;
import java.awt.geom.Point2D;

/**
 *
 * @author Ken Prepin
 */
public abstract class PathFormDrawable<SHAPE extends Shape> extends FormDrawable<SHAPE> {

    public PathFormDrawable() {
        super();
    }

    public PathFormDrawable(Color color, SHAPE shape, float stroke) {
        super(color, shape, stroke);
    }

    public PathFormDrawable(Color color, SHAPE shape, BasicStroke stroke) {
        super(color, shape, stroke);
    }

    public PathFormDrawable(Color color, SHAPE shape) {
        this(color, shape, DEFAULT_STROKE);
    }

    public abstract Point2D getStartPoint();

    public abstract double getStartPointX();

    public abstract double getStartPointY();

    public abstract Point2D getEndPoint();

    public abstract double getEndPointX();

    public abstract double getEndPointY();

    public abstract void setStartPoint(Point2D pos);

    public abstract void setEndPoint(Point2D pos);

    /**
     *  This function has been tested and the angle returned is the value between 0 and 2*PI (in radian)
     *
     * @return a value between 0 and 2*PI (in radian)
     */
    public double getAngle() throws Exception{
   //     System.out.println("Angle: "+ angle);
        return getAngle(getStartPoint(),getEndPoint());
    }
    /**
     *
     * @param startPoint
     * @param endPoint
     * @return  a value between 0 and 2*PI (in radian)
     */
    public double getAngle(Point2D startPoint, Point2D endPoint) throws Exception{
        if(startPoint.equals(endPoint)){
            throw new IllegalArgumentException("The two points must be different (they must define a non-null vector) ");
        }
        double angle = Math.atan2(startPoint.getY()- endPoint.getY(), endPoint.getX() - startPoint.getX());
        if (angle < 0) {
            angle += 2 * Math.PI;
        }
   //     System.out.println("Angle: "+ angle);
        return angle;
    }
   public double getAngle(Point2D anglePoint, Point2D startPoint, Point2D endPoint) throws Exception{
        double angleStartPoint = getAngle(anglePoint,  startPoint);
        double angleEndPoint = getAngle(anglePoint,  endPoint);
        double angle = angleEndPoint - angleStartPoint;
        if (angle < 0) {
            angle += 2 * Math.PI;
        }
   //     System.out.println("Angle: "+ angle);
        return angle;
    }
}
