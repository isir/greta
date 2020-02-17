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

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andre-Marie
 */
public class Vertex {

    public Vec3d position;
    Vec3d normal;
    Vec3d textureCoord;
    public List<BoneAssignment> boneAssignments;
    public List<PoseAssignment> poseAssignments;
    public int index;

    public Vertex() {
    }

    public Vertex(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
    }

    public Vertex(Vertex v) {
        position = v.position == null ? null : new Vec3d(v.position);
        normal = v.normal == null ? null : new Vec3d(v.normal);
        textureCoord = v.textureCoord == null ? null : new Vec3d(v.textureCoord);
        boneAssignments = v.boneAssignments == null ? null : new ArrayList<BoneAssignment>(v.boneAssignments);
        poseAssignments = v.poseAssignments == null ? null : new ArrayList<PoseAssignment>(v.poseAssignments);
    }

    public void add(BoneAssignment boneAssignment) {
        if (boneAssignments == null) {
            boneAssignments = new ArrayList<BoneAssignment>();
        }

        boneAssignments.add(boneAssignment);
    }

    public void add(PoseAssignment poseAssignment) {
        if (poseAssignments == null) {
            poseAssignments = new ArrayList<PoseAssignment>();
        }

        poseAssignments.add(poseAssignment);
    }

    public void invertNormal() {
        normal.multiply(-1);
    }

    public PoseAssignment getPose(String name) {
        if (poseAssignments == null) {
            return null;
        }
        for (PoseAssignment pose : poseAssignments) {
            if (pose.poseName.equals(name)) {
                return pose;
            }
        }
        return null;
    }

    public double getPosition(int axis) {
        switch (axis) {
            case 0:
                return x();
            case 1:
                return y();
            case 2:
                return z();
        }
        return 0;
    }

    public double getNormal(int axis) {
        switch (axis) {
            case 0:
                return nx();
            case 1:
                return ny();
            case 2:
                return nz();
        }
        return 0;
    }

    public double getUV(int axis) {
        switch (axis) {
            case 0:
                return u();
            case 1:
                return v();
        }
        return 0;
    }

    public void setPosition(int axis, double value) {
        switch (axis) {
            case 0:
                setX(value);
                break;
            case 1:
                setY(value);
                break;
            case 2:
                setZ(value);
                break;
        }
    }

    public void setNormal(int axis, double value) {
        switch (axis) {
            case 0:
                setNX(value);
                break;
            case 1:
                setNY(value);
                break;
            case 2:
                setNZ(value);
                break;
        }
    }

    public void setUV(int axis, double value) {
        switch (axis) {
            case 0:
                setU(value);
                break;
            case 1:
                setV(value);
                break;
        }
    }

    public double x() {
        return position == null ? 0 : position.x();
    }

    public double y() {
        return position == null ? 0 : position.y();
    }

    public double z() {
        return position == null ? 0 : position.z();
    }

    public double nx() {
        return normal == null ? 0 : normal.x();
    }

    public double ny() {
        return normal == null ? 0 : normal.y();
    }

    public double nz() {
        return normal == null ? 0 : normal.z();
    }

    public double u() {
        return textureCoord == null ? 0 : textureCoord.x();
    }

    public double v() {
        return textureCoord == null ? 0 : textureCoord.y();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vertex) {
            return equals((Vertex) obj);
        }
        return false;
    }

    public boolean equals(Vertex v) {
        return equals(v, true, true, true);
    }

    public boolean equals(Vertex v, boolean position, boolean normal, boolean uv) {
        return ((!position) || equalsPosition(v))
                && ((!normal) || equalsNormal(v))
                && ((!uv) || equalsUV(v));
    }

    public boolean equalsPosition(Vertex v) {
        if (this == v) {
            return true;
        }
        if (v == null) {
            return false;
        }
        return equalsVector(position, v.position, 0.0001);
    }

    public boolean equalsNormal(Vertex v) {
        if (this == v) {
            return true;
        }
        if (v == null) {
            return false;
        }
        return equalsVector(normal, v.normal, 0.00001);
    }

    public boolean equalsUV(Vertex v) {
        if (this == v) {
            return true;
        }
        if (v == null) {
            return false;
        }
        return equalsVector(textureCoord, v.textureCoord, 0.00001);
    }

    private static boolean equalsVector(Vec3d v1, Vec3d v2, double threshold) {
        if (v1 == null) {
            if (v2 != null) {
                return false;
            }
        } else {
            if (v2 == null) {
                return false;
            }
            if (Vec3d.substraction(v1, v2).length() > threshold) {
                return false;
            }
        }
        return true;
    }

    public void warpU() {
        if (textureCoord.x() < 0) {
            textureCoord.setX(textureCoord.x() - ((int) textureCoord.x()) + 1);
        }
        if (textureCoord.x() > 1) {
            textureCoord.setX(textureCoord.x() - ((int) textureCoord.x()));
        }
    }

    public void warpV() {
        if (textureCoord.y() < 0) {
            textureCoord.setY(textureCoord.y() - ((int) textureCoord.y()) + 1);
        }
        if (textureCoord.y() > 1) {
            textureCoord.setY(textureCoord.y() - ((int) textureCoord.y()));
        }
    }

    public void warpUV() {
        warpU();
        warpV();
    }

    public void printPosition() {
        System.out.println("x: " + x() + " y: " + y() + " z: " + z());
    }

    public void printNormal() {
        System.out.println("nx: " + nx() + " ny: " + ny() + " nz: " + nz());
    }

    public void printUV() {
        System.out.println("u: " + u() + " v: " + v());
    }

    @Override
    public String toString() {
        return "[" + x() + ", " + y() + ", " + z() + "] [" + nx() + ", " + ny() + ", " + nz() + "] [" + u() + ", " + v() + "]";
    }

    private void ensurePosition() {
        if (position == null) {
            position = new Vec3d(0, 0, 0);
        }
    }

    private void ensureNormal() {
        if (normal == null) {
            normal = new Vec3d(0, 0, 0);
        }
    }

    private void ensureUV() {
        if (textureCoord == null) {
            textureCoord = new Vec3d(0, 0, 0);
        }
    }

    public final void setX(double x) {
        ensurePosition();
        position.setX(x);
    }

    public final void setY(double y) {
        ensurePosition();
        position.setY(y);
    }

    public final void setZ(double z) {
        ensurePosition();
        position.setZ(z);
    }

    public void setNX(double x) {
        ensureNormal();
        normal.setX(x);
    }

    public void setNY(double y) {
        ensureNormal();
        normal.setY(y);
    }

    public void setNZ(double z) {
        ensureNormal();
        normal.setZ(z);
    }

    public void setU(double u) {
        ensureUV();
        textureCoord.setX(u);
    }

    public void setV(double v) {
        ensureUV();
        textureCoord.setY(v);
    }

    public void translate(Vec3d vec) {
        setX(x() + vec.x());
        setY(y() + vec.y());
        setZ(z() + vec.z());
    }

    public void translate(double x, double y, double z) {
        setX(x() + x);
        setY(y() + y);
        setZ(z() + z);
    }

    public void scale(Vec3d vec) {
        setX(x() * vec.x());
        setY(y() * vec.y());
        setZ(z() * vec.z());
    }

    public void scale(double x, double y, double z) {
        setX(x() * x);
        setY(y() * y);
        setZ(z() * z);
    }

    public void scalePose(double x, double y, double z) {
        if (poseAssignments != null) {
            for (PoseAssignment p : poseAssignments) {
                p.offset.setX(x * p.offset.x());
                p.offset.setY(y * p.offset.y());
                p.offset.setZ(z * p.offset.z());
            }
        }
    }

    public void rotate(Quaternion q) {
        position = q.rotate(position);
        if (normal != null) {
            normal = q.rotate(normal);
        }
    }

    public void rotate(double x, double y, double z) {
        Quaternion q = new Quaternion();
        q.fromEulerXYZByAngle(x, y, z);
        rotate(q);
    }

    public boolean isInsideBox(double[][] bounds) {
        return isInsideBox(bounds[0][0], bounds[1][0], bounds[0][1], bounds[1][1], bounds[0][2], bounds[1][2]);
    }

    public boolean isInsideBox(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        return minX < x() && x() < maxX
                && minY < y() && y() < maxY
                && minZ < z() && z() < maxZ;
    }

    public double distanceTo(Vertex other) {
        double dx = x() - other.x();
        double dy = y() - other.y();
        double dz = z() - other.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static Vertex interpolate(Vertex v1, Vertex v2, double t) {
        Vertex v = new Vertex();
        v.position = Vec3d.interpolation(v1.position, v2.position, t);
        if (v1.textureCoord != null) {
            if (v2.textureCoord != null) {
                v.textureCoord = Vec3d.interpolation(v1.textureCoord, v2.textureCoord, t);
            } else {
                v.textureCoord = v1.textureCoord;
            }
        } else {
            if (v2.textureCoord != null) {
                v.textureCoord = v2.textureCoord;
            }
        }

        if (v1.normal != null) {
            if (v2.normal != null) {
                v.normal = Vec3d.interpolation(v1.normal, v2.normal, t);//todo use angle
                v.normal.normalize();
            } else {
                v.normal = v1.normal;
            }
        } else {
            if (v2.normal != null) {
                v.normal = v2.normal;
            }
        }
        return v;
    }

    public static Vertex mean(Vertex v1, Vertex v2) {
        return interpolate(v1, v2, 0.5);
    }
}
