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
package greta.core.util.math;

import greta.core.util.log.Logs;
import java.awt.geom.Point2D;

/**
 * implementation of y = ax + b
 * @author Ken Prepin
 */
public class Affin implements Function{

    double a;
    double b;
    /**
     *
     * @param a slope
     * @param b origin deviation
     */
    public Affin(double a, double b){
        this.a = a;
        this.b = b;
    }
    /**
     *
     * @param a slope
     * The affin passes through point 0
     */

    public int solution(int K, int[] A) {
        // write your code in Java SE 6
        int pairCompt = 0;
        for(int i = 0; i<A.length;i++){
            long P = A[i];
            if(P+P==K){
                pairCompt ++;
            }
            for(int j = i+1; j<A.length;j++){
                long Q = A[j];
                if(P+Q==K){
                    pairCompt += 2;
                }
            }
        }
        return pairCompt;
    }


   public int solution1(int X1, int Y1, int X2, int Y2, int X3, int Y3, int X4, int Y4) {
        // write your code in Java SE 6
       long totalArea = 0;
       // Move rectangles in positive value area
       long bornInfX = Math.min(X1, X3);
       if(bornInfX <0){
           X1 += bornInfX;
           X3 += bornInfX;
           X2 += bornInfX;
           X4 += bornInfX;
       }
       long bornInfY = Math.min(Y1,Y3);
       if(bornInfY<0){
           Y1 += bornInfY;
           Y2 += bornInfY;
           Y4 += bornInfY;
           Y3 += bornInfY;
       }
       // Calcul of rectangles areas
       long Area1 = (X2-X1)*(Y2-Y1);
       long Area2 = (X4-X3)*(Y4-Y3);
       // Calcul of areas intersection
       long Intersect = 0;
       if(Math.min(X4,X2)-Math.max(X1,X3)>0 && Math.min(Y4,Y2)-Math.max(Y3,Y1)>0){
           Intersect = (Math.min(X4,X2)-Math.max(X1,X3)) * (Math.min(Y4,Y2)-Math.max(Y3,Y1));
       }
       // Calcul of total area
       totalArea = Area1 + Area2 - Intersect;
       if(totalArea>2147483647){
           return -1;
       } else {
           return (int)totalArea;
       }
    }
    public Affin(double a){
        this.a = a;
        this.b = 0;
    }
    public Affin(){
        this.a = 0;
        this.b = 0;
    }
    /**
     * Defintion by slope and via point
     *
     * @param a slope
     * @param pointOfTheCurve viaPoint
     */
    public Affin(double a, Point2D pointOfTheCurve){
        this.a = a;
        this.b = pointOfTheCurve.getY()-this.a*pointOfTheCurve.getX();
    }
    /**
     *  Definition by two points
     *
     * @param firstPoint
     * @param secondPoint
     */
    public Affin(Point2D firstPoint, Point2D secondPoint){
        this.a = (secondPoint.getY()-firstPoint.getY())/(secondPoint.getX()-firstPoint.getX());
        this.b = firstPoint.getY() - this.a * firstPoint.getX();
    }
    /**
     * If exists, returns the intersection point between this affin and the one passed in argument
     * @param affin
     * @return
     * @throws Exception if the affins are parallel
     */
    public Point2D intersects(Affin affin) throws Exception{
        if(affin.getA()==this.a){
            Logs.error("Intersection espected between parallel lines");
            throw new Exception("No intersection between parallel lines");
        } else {
//            if(Double.isInfinite(this.a)){
//                //there is an intersection but it miss somme thing
//            }
//            else {
//                if(Double.isInfinite(affin.getA())){
//                    //there is an intersection but it miss somme thing
//                }
//                else {
                    return new Point2D.Double((affin.getB()-this.b)/(this.a-affin.getA()),(this.a*affin.getB() - this.b*affin.getA())/(this.a-affin.getA()));
//                }
//            }

        }
    }
    /**
     *
     * @param x
     * @return Return the value of the function in a given X
     */
    @Override
    public double f(double x) {
        return a*x+b;
    }

    @Override
    public String getName() {
        return "Affin";
    }

    @Override
    public Function getDerivative() {
        return Constante.of(a);
    }

    @Override
    public Function simplified() {
        return new Affin(this.a);
    }
    /**
     * Define the current affine as the perpendicular bissector of the segment diffined by firstPoint and secondPoint
     * @param firstPoint
     * @param secondPoint
     */
    public void defineAsPerpBissectorOf(Point2D firstPoint, Point2D secondPoint)throws Exception{
        if( firstPoint.equals(secondPoint)){
            throw new IllegalArgumentException("The two points must be different (they must define a non-null vector) ");
        }
        Point2D centerStartEndSegment = new Point2D.Double((secondPoint.getX() + firstPoint.getX()) / 2, (secondPoint.getY() + firstPoint.getY()) / 2);
        this.a = -(secondPoint.getX() - firstPoint.getX())/(secondPoint.getY() - firstPoint.getY());
        this.b = centerStartEndSegment.getY()-this.a*centerStartEndSegment.getX();
    }
    public void defineAsPerpByPoint(Affin affin, Point2D point){

        this.a = -1/affin.getA();
        this.b = point.getY()-this.a*point.getX();
    }

    public double getA() {
        return this.a;
    }

    public double getB() {
        return this.b;
    }
}
