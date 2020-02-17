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

import java.util.ArrayList;

/**
 *
 * @author Andre-Marie
 */
public class Mesh {

    VertexBuffer vertices;
    ArrayList<SubMesh> submeshes = new ArrayList<SubMesh>();
    String skeleton;
    //TODO poses

    public int getNumberOfSubMeshes() {
        return submeshes.size();
    }

    public SubMesh getSubMesh(int index) {
        return submeshes.get(index);
    }

    public String getSkeleton() {
        return skeleton;
    }

    public SubMesh getSubMesh(String name) {
        if (name == null) {
            return null;
        }
        for (SubMesh sub : submeshes) {
            if (name.equals(sub.name)) {
                return sub;
            }
        }
        return null;
    }

    public void addSubMesh(SubMesh subMesh) {
        submeshes.add(subMesh);
    }

    public void unShareVertices() {
        if (vertices == null) {
            return;
        }
        for (SubMesh sub : submeshes) {
            if (sub.vertexBuffer == vertices) {
                SubMesh copy = sub.duplicate();
                sub.vertexBuffer = copy.vertexBuffer;
                sub.faces = copy.faces;
                sub.removeUnusedVertices();
            }
        }
    }

    public SubMesh duplicateSubMesh(int index) {
        SubMesh copy = getSubMesh(index).duplicate();
        submeshes.add(copy);
        return copy;
    }

    public SubMesh duplicateSubMesh(String name) {
        SubMesh copy = getSubMesh(name).duplicate();
        submeshes.add(copy);
        return copy;
    }

    public void removeSubMesh(int i) {
        submeshes.remove(i);
    }

    public void removeSubMesh(String name) {
        submeshes.remove(getSubMesh(name));
    }

    public void translate(double x, double y, double z) {
        ArrayList<VertexBuffer> translated = new ArrayList<VertexBuffer>(submeshes.size() + 1);
        if (vertices != null) {
            vertices.translate(x, y, z);
            translated.add(vertices);
        }
        for (SubMesh s : submeshes) {
            if (!translated.contains(s.vertexBuffer)) {
                s.vertexBuffer.translate(x, y, z);
                translated.add(s.vertexBuffer);
            }
        }
    }

    /**
     * only positions are scaled.
     *
     * @param x
     * @param y
     * @param z
     */
    public void scale(double x, double y, double z) {
        ArrayList<VertexBuffer> scaled = new ArrayList<VertexBuffer>(submeshes.size() + 1);
        if (vertices != null) {
            vertices.scale(x, y, z);
            scaled.add(vertices);
        }
        for (SubMesh s : submeshes) {
            if (!scaled.contains(s.vertexBuffer)) {
                s.vertexBuffer.scale(x, y, z);
                scaled.add(s.vertexBuffer);
            }
        }
    }

    /**
     * scale positions and poses
     *
     * @param x
     * @param y
     * @param z
     */
    public void scaleAll(double x, double y, double z) {
        ArrayList<VertexBuffer> scaled = new ArrayList<VertexBuffer>(submeshes.size() + 1);
        if (vertices != null) {
            vertices.scale(x, y, z);
            vertices.scalePoses(x, y, z);
            scaled.add(vertices);
        }
        for (SubMesh s : submeshes) {
            if (!scaled.contains(s.vertexBuffer)) {
                s.vertexBuffer.scale(x, y, z);
                s.vertexBuffer.scalePoses(x, y, z);
                scaled.add(s.vertexBuffer);
            }
        }
    }

    public void setCoordinateZeroToTheMinPointOfTheBoundingBox() {
        double[][] bounds = this.getBoudingBox();
        translate(-bounds[0][0], -bounds[0][1], -bounds[0][2]);
    }

    public double[][] getBoudingBox() {
        double[][] bounds = new double[2][3];
        bounds[0][0] = Double.POSITIVE_INFINITY;
        bounds[0][1] = Double.POSITIVE_INFINITY;
        bounds[0][2] = Double.POSITIVE_INFINITY;
        bounds[1][0] = Double.NEGATIVE_INFINITY;
        bounds[1][1] = Double.NEGATIVE_INFINITY;
        bounds[1][2] = Double.NEGATIVE_INFINITY;
        for (SubMesh s : submeshes) {
            double[][] subBounds = s.getBoundingBox();
            bounds[0][0] = Math.min(bounds[0][0], subBounds[0][0]);
            bounds[0][1] = Math.min(bounds[0][1], subBounds[0][1]);
            bounds[0][2] = Math.min(bounds[0][2], subBounds[0][2]);

            bounds[1][0] = Math.max(bounds[1][0], subBounds[1][0]);
            bounds[1][1] = Math.max(bounds[1][1], subBounds[1][1]);
            bounds[1][2] = Math.max(bounds[1][2], subBounds[1][2]);
        }
        return bounds;
    }

    public SubMesh createSubMesh() {
        SubMesh sub = new SubMesh();
        sub.vertexBuffer = new VertexBuffer(0);
        this.addSubMesh(sub);
        return sub;
    }

    public void printSize() {
        double[][] bounds0 = this.getBoudingBox();
        System.out.print(
                "\t\t\t\t\t\t<size x=\""
                + (Math.round(bounds0[1][0] * 10000) / 10000.0) + "\" y=\""
                + (Math.round(bounds0[1][1] * 10000) / 10000.0) + "\" z=\""
                + (Math.round(bounds0[1][2] * 10000) / 10000.0) + "\" />\r\n");
    }

    public void setSkeleton(String string) {
        skeleton = string;
    }
}
