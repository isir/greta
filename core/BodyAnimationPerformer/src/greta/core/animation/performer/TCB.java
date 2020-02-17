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
package greta.core.animation.performer;

import greta.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class TCB {
    public static Vec3d computePosition(double s, Vec3d p1, Vec3d p2, Vec3d p3, Vec3d p4, double tension, double continuity, double bias) {

        if (s == 0) {
            return p2;
        }
        if (s == 1) {
            return p3;
        }
        double h1 = 2 * java.lang.Math.pow(s, 3.0) - 3 * Math.pow(s, 2.0) + 1;
        double h2 = (-2) * Math.pow(s, 3.0) + 3 * Math.pow(s, 2);
        double h3 = Math.pow(s, 3.0) - 2 * Math.pow(s, 2.0) + s;
        double h4 = Math.pow(s, 3.0) - Math.pow(s, 2);

        double TDix = (1 - tension) * (1 + continuity) * (1 + bias) * (p2.x() - p1.x()) / 2.0 + (1 - tension) * (1 - continuity) * (1 - bias) * (p3.x() - p2.x()) / 2.0;
        double TDiy = (1 - tension) * (1 + continuity) * (1 + bias) * (p2.y() - p1.y()) / 2.0 + (1 - tension) * (1 - continuity) * (1 - bias) * (p3.y() - p2.y()) / 2.0;
        double TDiz = (1 - tension) * (1 + continuity) * (1 + bias) * (p2.z() - p1.z()) / 2.0 + (1 - tension) * (1 - continuity) * (1 - bias) * (p3.z() - p2.z()) / 2.0;

        double TSix = (1 - tension) * (1 - continuity) * (1 + bias) * (p3.x() - p2.x()) / 2.0 + (1 - tension) * (1 + continuity) * (1 - bias) * (p4.x() - p3.x()) / 2.0;
        double TSiy = (1 - tension) * (1 - continuity) * (1 + bias) * (p3.y() - p2.y()) / 2.0 + (1 - tension) * (1 + continuity) * (1 - bias) * (p4.y() - p3.y()) / 2.0;
        double TSiz = (1 - tension) * (1 - continuity) * (1 + bias) * (p3.z() - p2.z()) / 2.0 + (1 - tension) * (1 + continuity) * (1 - bias) * (p4.z() - p3.z()) / 2.0;

        double ppx = h1 * p2.x() + h2 * p3.x() + h3 * TDix + h4 * TSix;
        double ppy = h1 * p2.y() + h2 * p3.y() + h3 * TDiy + h4 * TSiy;
        double ppz = h1 * p2.z() + h2 * p3.z() + h3 * TDiz + h4 * TSiz;
        Vec3d r = new Vec3d((double) ppx, (double) ppy, (double) ppz);
//        System.out.println("PathInterpolation p1" + p1);
//        System.out.println("PathInterpolation added" + r);
//        System.out.println("PathInterpolation p2" + p2);
//        System.out.println("PathInterpolation p3" + p3);
//        System.out.println("PathInterpolation p4" + p4);
        return r;
    }
}
