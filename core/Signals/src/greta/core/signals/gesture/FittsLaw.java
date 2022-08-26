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
package greta.core.signals.gesture;

import greta.core.util.math.Quaternion;

/**
 *
 * @author Quoc Anh Le
 * @author Andre-Marie Pez
 */
public class FittsLaw {
    private double incompressibleDistance = 0.1; //unknown value for now
    private double incompressibleFingerDistance = 0.2; //unknown value for now
    private double angularFactor = Math.PI/4.0; //unknown value for now
    private double intercept = 0.09;
    private double baseSlope = 0.3;
    private double width = 1;


    private double getWristDistance(Hand hand1, Hand hand2){

        if(hand1.getPosition()==null || hand2.getPosition()==null){
            return incompressibleDistance;
        }

        double dx = hand2.getPosition().getX()-hand1.getPosition().getX();
        double dy = hand2.getPosition().getY()-hand1.getPosition().getY();
        double dz = hand2.getPosition().getZ()-hand1.getPosition().getZ();

        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);

        if(hand1.getTrajectory()!=null && hand1.getTrajectory()._name.equalsIgnoreCase("circle")){
            double a = Math.max(Math.max(hand1.getTrajectory().getAmplitude()[0],hand1.getTrajectory().getAmplitude()[1]),hand1.getTrajectory().getAmplitude()[2]);
            double b = Math.max(Math.max(hand1.getTrajectory().getFrequency()[0],hand1.getTrajectory().getFrequency()[1]),hand1.getTrajectory().getFrequency()[2]);
            dist = (a*3.14)*100*b;
        }

        return dist;
    }

    private double getFingerDistance(Hand hand1, Hand hand2){
        return hand1.getHandShape()!=null && hand1.getHandShape().equals(hand2.getHandShape()) ? 0 : incompressibleFingerDistance;
    }

    private double qetWristOrientationDistance(Hand hand1, Hand hand2){
        if(hand1.getWristOrientation()==null || hand2.getWristOrientation()==null){
            return 0;
        }
        return Quaternion.multiplication(hand1.getWristOrientation().conjugate(), hand2.getWristOrientation()).normalized().angle() * angularFactor;
    }

    public double getMovementTime(Hand hand1, Hand hand2, double TMP, double PWR, double SPC, double FLD){
        double distance = incompressibleDistance;
        if(hand1!=null && hand2!=null){
            distance = Math.max(Math.max(Math.max(getFingerDistance(hand1, hand2), getWristDistance(hand1, hand2)), qetWristOrientationDistance(hand1, hand2)), incompressibleDistance);
        }
        double slope = (0.2+(1.6*(1-TMP)))*(1.0-0.3*PWR)*this.baseSlope;
        return fittsLaw(intercept, slope, distance, width);
    }


    private static double fittsLaw(double intercept, double slope, double distance, double width){
        return intercept+slope*(Math.log1p(distance/width)/Math.log(2));
    }

}
