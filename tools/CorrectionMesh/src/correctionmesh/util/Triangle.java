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
package correctionmesh.util;

import greta.core.util.math.Vec3d;
import java.util.Comparator;

/**
 *
 * @author Andre-Marie
 */
public class Triangle {

    public Vertex v1;
    public Vertex v2;
    public Vertex v3;

    public Triangle(Vertex v1, Vertex v2, Vertex v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        rollVerticesToMinIndex();
    }

    public Triangle(Triangle t) {
        this(t.v1, t.v2, t.v3);
    }

    public void invert() {
        Vertex vtemp = v1;
        v1 = v2;
        v2 = vtemp;
        rollVerticesToMinIndex();
    }

    public Vec3d getNormal() {
        double x = (y2() - y1()) * (z3() - z1()) - (z2() - z1()) * (y3() - y1());
        double y = (z2() - z1()) * (x3() - x1()) - (x2() - x1()) * (z3() - z1());
        double z = (x2() - x1()) * (y3() - y1()) - (y2() - y1()) * (x3() - x1());
        Vec3d normal = new Vec3d(x, y, z);
        normal.normalize();
        return new Vec3d(x, y, z);
    }

    public void sortVertexByNormals() {
        Vec3d normal = getNormal();

        Vertex futurV1
                = Vec3d.substraction(v1.normal, normal).length() < Vec3d.substraction(v2.normal, normal).length()
                        ? (Vec3d.substraction(v1.normal, normal).length() < Vec3d.substraction(v3.normal, normal).length()
                                ? v1 : v3)
                        : (Vec3d.substraction(v2.normal, normal).length() < Vec3d.substraction(v3.normal, normal).length()
                                ? v2 : v3);
        Vertex futurV2 = nextVertexAfter(futurV1);
        Vertex futurV3 = nextVertexAfter(futurV2);

        v1 = futurV1;
        v2 = futurV2;
        v3 = futurV3;

    }

    public boolean contains(Vertex v) {
        return v1 == v || v2 == v || v3 == v;
    }

    public double getUVNormal() {
        return (u2() - u1()) * (v3() - v1()) - (v2() - v1()) * (u3() - u1());
    }

    public double x1() {
        return v1.x();
    }

    public double y1() {
        return v1.y();
    }

    public double z1() {
        return v1.z();
    }

    public double x2() {
        return v2.x();
    }

    public double y2() {
        return v2.y();
    }

    public double z2() {
        return v2.z();
    }

    public double x3() {
        return v3.x();
    }

    public double y3() {
        return v3.y();
    }

    public double z3() {
        return v3.z();
    }

    public double u1() {
        return v1.u();
    }

    public double v1() {
        return v1.v();
    }

    public double u2() {
        return v2.u();
    }

    public double v2() {
        return v2.v();
    }

    public double u3() {
        return v3.u();
    }

    public double v3() {
        return v3.v();
    }

    public void replace(Vertex oldVertex, Vertex newVertex) {
        if (v1 == oldVertex) {
            v1 = newVertex;
        }
        if (v2 == oldVertex) {
            v2 = newVertex;
        }
        if (v3 == oldVertex) {
            v3 = newVertex;
        }
        rollVerticesToMinIndex();
    }

    public void printUV() {
        System.out.println("---------");
        v1.printUV();
        v2.printUV();
        v3.printUV();
    }

    public double minX() {
        return Math.min(x1(), Math.min(x2(), x3()));
    }

    public double minY() {
        return Math.min(y1(), Math.min(y2(), y3()));
    }

    public double minZ() {
        return Math.min(z1(), Math.min(z2(), z3()));
    }

    public double maxX() {
        return Math.max(x1(), Math.max(x2(), x3()));
    }

    public double maxY() {
        return Math.max(y1(), Math.max(y2(), y3()));
    }

    public double maxZ() {
        return Math.max(z1(), Math.max(z2(), z3()));
    }

    private void rollVerticesToMinIndex() {
        if (v1.index > v2.index) {
            if (v2.index > v3.index) {
                Vertex temp = v1;
                v1 = v3;
                v3 = v2;
                v2 = temp;
            } else {
                Vertex temp = v1;
                v1 = v2;
                v2 = v3;
                v3 = temp;
            }
        } else {
            if (v1.index > v3.index) {
                Vertex temp = v1;
                v1 = v3;
                v3 = v2;
                v2 = temp;
            }
        }
    }

    public double angleAt(Vertex v) {
        Vec3d vec1 = null;
        Vec3d vec2 = null;
        if (v == v1) {
            vec1 = new Vec3d(v2.position);
            vec1.minus(v1.position);
            vec2 = new Vec3d(v3.position);
            vec2.minus(v1.position);
        }
        if (v == v2) {
            vec1 = new Vec3d(v3.position);
            vec1.minus(v2.position);
            vec2 = new Vec3d(v1.position);
            vec2.minus(v2.position);
        }
        if (v == v3) {
            vec1 = new Vec3d(v1.position);
            vec1.minus(v3.position);
            vec2 = new Vec3d(v2.position);
            vec2.minus(v3.position);
        }
        if (vec1 == null || vec2 == null) {
            return 0;
        }
        vec1.normalize();
        vec2.normalize();
        return Math.acos(vec1.dot3(vec2));
    }

    public static Comparator<Triangle> getComparator() {
        return new Comparator<Triangle>() {

            @Override
            public int compare(Triangle o1, Triangle o2) {
                o1.rollVerticesToMinIndex();
                o2.rollVerticesToMinIndex();
                return o1.v1.index - o2.v1.index;
            }

        };
    }

    public Vec3d getCenter() {
        Vec3d center = new Vec3d(v1.position);
        center.add(v2.position);
        center.add(v3.position);
        center.divide(3);
        return center;
    }

    public Vertex nextVertexAfter(Vertex v) {
        if (v == v1) {
            return v2;
        }
        if (v == v2) {
            return v3;
        }
        return v1;
    }

    public boolean isInsideBox(double[][] bounds) {
        return isInsideBox(bounds[0][0], bounds[1][0], bounds[0][1], bounds[1][1], bounds[0][2], bounds[1][2]);
    }

    public boolean isInsideBox(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        return v1.isInsideBox(minX, maxX, minY, maxY, minZ, maxZ) && v2.isInsideBox(minX, maxX, minY, maxY, minZ, maxZ) && v3.isInsideBox(minX, maxX, minY, maxY, minZ, maxZ);
    }

    public static double convexity(Triangle t1, Triangle t2) {
        Vec3d center1 = t1.getCenter();
        Vec3d exCenter1 = t1.getNormal();

        Vec3d center2 = t2.getCenter();
        Vec3d exCenter2 = t2.getNormal();
        center2.minus(center1);
        center2.multiply(2f / center2.length());
        exCenter2.add(center2);

        exCenter2.minus(exCenter1);
        double distExCenter = exCenter2.length();

        return distExCenter - 2;
    }

    public static int vertexShared(Triangle t1, Triangle t2) {
        return (t2.contains(t1.v1) ? 1 : 0)
                + (t2.contains(t1.v2) ? 1 : 0)
                + (t2.contains(t1.v3) ? 1 : 0);

    }

    @Override
    public String toString() {
        return "v1: " + v1.index + " v2: " + v2.index + " v3: " + v3.index;
    }

}
