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

import greta.core.util.math.Functions;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Andre-Marie
 */
public class SubMesh {

    public VertexBuffer vertexBuffer;
    ArrayList<Triangle> faces = new ArrayList<Triangle>();
    public String material;
    public String name;
    List<SubMesh> groups;

    public Vertex getVertex(int vertexIndex) {
        return vertexBuffer.get(vertexIndex);
    }

    public void smoothNormals() {
        smoothNormalsPond(1);
    }

    public void smoothNormalsPond(double pond) {
        for (Vertex vertex : vertexBuffer.vertices) {
            smoothNormalPond(vertex, pond);
        }
    }

    public void smoothNormal(int vertexIndex) {
        smoothNormalPond(getVertex(vertexIndex), 1);
    }

    public void smoothNormal(Vertex vertex) {
        smoothNormalPond(vertex, 1);
    }

    public void smoothNormalPond(int vertexIndex, double pond) {
        smoothNormalPond(getVertex(vertexIndex), pond);
    }

    public void smoothNormalPond(Vertex vertex, double pond) {

        List<Triangle> triangles = getTianglesContaining(vertex);
        if (!triangles.isEmpty()) {
            Vec3d newNormal = new Vec3d(0, 0, 0);
            for (Triangle tri : triangles) {
                Vec3d tn = tri.getNormal();
                tn.multiply(tri.angleAt(vertex));
                newNormal.add(tn);
            }
            // newNormal.divide(Math.PI*2.0);

            newNormal.normalize();
            if (vertex.normal == null) {
                vertex.normal = new Vec3d(1, 0, 0);
            }
            vertex.normal.normalize();

            newNormal.multiply(pond);
            vertex.normal.multiply(1 - pond);

            vertex.normal.add(newNormal);

            vertex.normal.normalize();
        }
    }

    public List<Triangle> getFaces() {
        return faces;
    }

    public List<Vertex> getVerticesOfPose(String poseName) {
        ArrayList<Vertex> toReturn = new ArrayList<Vertex>(100);
        for (Vertex v : vertexBuffer) {
            if (v.getPose(poseName) != null) {
                toReturn.add(v);
            }
        }
        return toReturn;
    }

    public List<PoseAssignment> getPoseAssignments(String poseName) {
        ArrayList<PoseAssignment> toReturn = new ArrayList<PoseAssignment>(100);
        for (Vertex v : vertexBuffer) {
            PoseAssignment p = v.getPose(poseName);
            if (p != null) {
                toReturn.add(p);
            }
        }
        return toReturn;
    }

    public List<Triangle> getTianglesContaining(Vertex v) {
        return getTianglesContaining(v, faces);
    }

    private static List<Triangle> getTianglesContaining(Vertex v, List<Triangle> triangles) {
        ArrayList<Triangle> result = new ArrayList<Triangle>(triangles.size());
        for (Triangle triangle : triangles) {
            if (triangle.contains(v)) {
                result.add(triangle);
            }
        }
        return result;
    }

    public void invertFaces() {
        for (Triangle t : faces) {
            t.invert();
        }
    }

    public int getGroupCount() {
        if (groups == null) {
            computeGroups();
        }
        return groups.size();
    }

    public void createTriangle(int indexV1, int indexV2, int indexV3) {
        createTriangle(vertexBuffer.get(indexV1), vertexBuffer.get(indexV2), vertexBuffer.get(indexV3));
    }

    public void createTriangle(Vertex v1, Vertex v2, Vertex v3) {
        faces.add(new Triangle(v1, v2, v3));
    }

    public List<Vertex> getVerticesInUV(double[] uvs) {
        return getVerticesInUV(uvs[0], uvs[1], uvs[2], uvs[3]);
    }

    public List<Vertex> getVerticesInUV(double minU, double maxU, double minV, double maxV) {
        ArrayList<Vertex> verticess = new ArrayList<Vertex>();
        for (Vertex v : vertexBuffer) {
            if (minU < v.u() && v.u() < maxU && minV < v.v() && v.v() < maxV) {
                verticess.add(v);
            }
        }
        return verticess;
    }

    public List<Vertex> getVerticesInBounds(double[][] bounds) {
        return getVerticesInBounds(bounds[0][0], bounds[1][0], bounds[0][1], bounds[1][1], bounds[0][2], bounds[1][2]);
    }

    public List<Vertex> getVerticesInBounds(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        ArrayList<Vertex> verticess = new ArrayList<Vertex>();
        for (Vertex v : vertexBuffer) {
            if (v.isInsideBox(minX, maxX, minY, maxY, minZ, maxZ)) {
                verticess.add(v);
            }
        }
        return verticess;
    }

    public List<Triangle> getTrianglesInUV(double[] uvs) {
        return getTrianglesInUV(uvs[0], uvs[1], uvs[2], uvs[3]);
    }

    public List<Triangle> getTrianglesInUV(double minU, double maxU, double minV, double maxV) {
        ArrayList<Triangle> triangles = new ArrayList<Triangle>();
        for (Vertex v : getVerticesInUV(minU, maxU, minV, maxV)) {
            for (Triangle t : getTianglesContaining(v)) {
                if (!triangles.contains(t)) {
                    triangles.add(t);
                }
            }
        }
        return triangles;
    }

    public List<Triangle> getTrianglesInBounds(double[][] bounds) {
        return getTrianglesInBounds(bounds[0][0], bounds[1][0], bounds[0][1], bounds[1][1], bounds[0][2], bounds[1][2]);
    }

    public List<Triangle> getTrianglesInBounds(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        ArrayList<Triangle> triangles = new ArrayList<Triangle>();
        for (Triangle t : faces) {
            if (t.isInsideBox(minX, maxX, minY, maxY, minZ, maxZ)) {
                triangles.add(t);
            }
        }
        return triangles;
    }

    public void scalePose(String poseName, double scale) {
        for (Vertex v : vertexBuffer) {
            PoseAssignment p = v.getPose(poseName);
            if (p != null) {
                p.offset.multiply(scale);
            }
        }
    }

    public SubMesh getGroup(int index) {
        if (groups == null) {
            computeGroups();
        }
        return groups.get(index);
    }

    public void computeGroups() {
        LinkedList<Triangle> alone = new LinkedList<Triangle>(this.faces);
        groups = new ArrayList<SubMesh>();
        while (!alone.isEmpty()) {
            Triangle t = alone.pollFirst();
            SubMesh group = new SubMesh();
            group.vertexBuffer = this.vertexBuffer;
            group.faces = new ArrayList<Triangle>();
            group.faces.add(t);
            groups.add(group);
            computeGroupsRecurs(t, alone, group.faces);
        }
    }

    private void computeGroupsRecurs(Triangle t, List<Triangle> alone, List<Triangle> group) {
        List<Triangle> triangles1 = getTianglesContaining(t.v1, alone);
        alone.removeAll(triangles1);
        List<Triangle> triangles2 = getTianglesContaining(t.v2, alone);
        alone.removeAll(triangles2);
        triangles1.addAll(triangles2);
        List<Triangle> triangles3 = getTianglesContaining(t.v3, alone);
        alone.removeAll(triangles3);
        triangles1.addAll(triangles3);
        for (Triangle tri : triangles1) {
            group.add(tri);
            computeGroupsRecurs(tri, alone, group);
        }
    }

    public SubMesh duplicate() {
        SubMesh copy = new SubMesh();
        copy.material = material;
        copy.faces = new ArrayList<Triangle>(faces.size());
        for (Triangle t : faces) {
            copy.faces.add(new Triangle(t));
        }
        copy.vertexBuffer = new VertexBuffer(vertexBuffer.vertices.size());

        for (Vertex v : vertexBuffer.vertices) {
            Vertex vCopy = new Vertex(v);
            copy.vertexBuffer.vertices.add(vCopy);
            List<Triangle> triangles = getTianglesContaining(v, copy.faces);
            for (Triangle tri : triangles) {
                tri.replace(v, vCopy);
            }
        }

        return copy;
    }

    public void removeGroup(int index) {
        List<Triangle> toRemove = getGroup(index).faces;
        removeTriangles(toRemove);
        removeUnusedVertices();
    }

    public void retainGroup(int index) {
        List<Triangle> teRetain = getGroup(index).faces;
        retainTriangles(teRetain);
        removeUnusedVertices();
    }

    public void removeGroups(int[] indices) {
        List<Triangle> toRemove = new ArrayList<Triangle>();
        for (int index : indices) {
            if (index < getGroupCount()) {
                toRemove.addAll(getGroup(index).faces);
            }
        }
        removeTriangles(toRemove);
        removeUnusedVertices();
    }

    public void retainGroups(int[] indices) {
        List<Triangle> teRetain = new ArrayList<Triangle>();
        for (int index : indices) {
            if (index < getGroupCount()) {
                teRetain.addAll(getGroup(index).faces);
            }
        }
        retainTriangles(teRetain);
        removeUnusedVertices();
    }

    public boolean isVertexUsed(Vertex v) {
        for (Triangle triangle : faces) {
            if (triangle.contains(v)) {
                return true;
            }
        }
        return false;
    }

    public void removeUnusedVertices() {
        ArrayList<Vertex> toRemove = new ArrayList<Vertex>(vertexBuffer.vertices.size());
        for (Vertex v : vertexBuffer) {
            if (!isVertexUsed(v)) {
                toRemove.add(v);
            }
        }
        vertexBuffer.vertices.removeAll(toRemove);
    }

    public void removeUnusedVertices2() {
        vertexBuffer.updateVerticesIndices();
        LinkedList<Integer> toKipIndex = new LinkedList<Integer>();
        ArrayList<Vertex> toRemove = new ArrayList<Vertex>(vertexBuffer.vertices);
        for (Triangle triangle : faces) {
            toKipIndex.add(triangle.v1.index);
            toKipIndex.add(triangle.v2.index);
            toKipIndex.add(triangle.v3.index);

        }
        sortAndDeleteDuplicate(toKipIndex);
        ListIterator<Integer> iter = toKipIndex.listIterator(toKipIndex.size());
        while (iter.hasPrevious()) {
            toRemove.remove(iter.previous().intValue());
        }
        vertexBuffer.vertices.removeAll(toRemove);
    }

    public void invertNormals() {
        for (Vertex v : vertexBuffer.vertices) {
            v.invertNormal();
        }
    }

    public void minimizeUVDistortion(int vertexIndex) {
        minimizeUVDistortion(vertexBuffer.get(vertexIndex));
    }

    public void minimizeUVDistortion(Vertex v) {
        minimizeUVDistortion(v, 1);
    }

    public void minimizeUVDistortion(Vertex v, double step) {
        minimizeUVDistortion(v, step, true);
    }

    public void minimizeUVDistortion(Vertex v, double step, boolean proxima) {
        List<Triangle> tris = getTianglesContaining(v);
        cleanFakeTriangles(tris);
        if (tris == null || tris.isEmpty()) {
            return;
        }

        ListIterator<Triangle> triter = tris.listIterator();
        double[] uv = getUVForMinimizeUVDistortionFunction(triter.next(), v, proxima);

        while (triter.hasNext()) {
            double[] uvNext = getUVForMinimizeUVDistortionFunction(triter.next(), v, proxima);
            uv[0] += uvNext[0];
            uv[1] += uvNext[1];
        }
        v.setU(v.u() + (uv[0] / tris.size() - v.u()) * step);
        v.setV(v.v() + (uv[1] / tris.size() - v.v()) * step);
    }

    private double[] getUVForMinimizeUVDistortionFunction(Triangle tri, Vertex v, boolean proxima) {
        Vertex ref1 = tri.nextVertexAfter(v);
        Vertex ref2 = tri.nextVertexAfter(ref1);

        Vec3d dir0 = Vec3d.substraction(ref2.position, ref1.position);
        Vec3d dir = Vec3d.substraction(v.position, ref1.position);

        double scale = dir.length() / dir0.length();
        double angle = Math.acos(dir0.dot3(dir) / (dir0.length() * dir.length()));

        Vec3d uvDir0 = Vec3d.substraction(ref2.textureCoord, ref1.textureCoord);
        uvDir0.multiply(scale);

        Quaternion r = new Quaternion(new Vec3d(0, 0, 1), angle);
        Vec3d uvDir1 = r.rotate(uvDir0);
        uvDir1.add(ref1.textureCoord);
        Quaternion rt = new Quaternion(new Vec3d(0, 0, -1), angle);
        Vec3d uvDir2 = rt.rotate(uvDir0);
        uvDir2.add(ref1.textureCoord);

        Vec3d near = null;
        Vec3d far = null;

        if (Vec3d.substraction(uvDir1, v.textureCoord).length() < Vec3d.substraction(uvDir2, v.textureCoord).length()) {
            near = uvDir1;
            far = uvDir2;
        } else {
            near = uvDir2;
            far = uvDir1;
        }
        if (proxima || tri.getUVNormal() < 0) {
            return new double[]{near.x(), near.y()};
        }
        //else
        return new double[]{far.x(), far.y()};

    }

    public void mergeVertices(boolean position, boolean normal, boolean uv) {
        mergeVertices(position, normal, uv, false);
    }

    public void mergeVertices(boolean position, boolean normal, boolean uv, boolean remove) {
        int count = 0;
        int merges = 0;
        for (int i = 0; i < vertexBuffer.size() - count; ++i) {
            Vertex v1 = vertexBuffer.get(i);
            for (int j = i + 1; j < vertexBuffer.size() - count; ++j) {
                Vertex v2 = vertexBuffer.get(j);
                if (v1.equals(v2, position, normal, uv)) {
                    List<Triangle> triangles = getTianglesContaining(v2, faces);
                    if (triangles.isEmpty()) {
                        continue;
                    }
                    for (Triangle tri : triangles) {
                        tri.replace(v2, v1);
                    }
                    merges++;
//                    if(remove){
                    count++;
                    vertexBuffer.set(j, vertexBuffer.get(vertexBuffer.size() - count));
                    vertexBuffer.set(vertexBuffer.size() - count, v2);
                    --j;
//                    }
                }
            }
        }
        System.out.println("mergeVertices: " + merges + " merges");
        if (remove) {
            vertexBuffer.vertices.subList(vertexBuffer.size() - count, vertexBuffer.size()).clear();
//            System.out.println("mergeVertices: "+count+" removed");
        }
    }

    public void removeTrianglesByIndices(int[] indices) {
        removeTriangles(getTriangles(indices));
    }

    public void removeTrianglesByIndices(List<Integer> indices) {
        removeTriangles(getTriangles(indices));
    }

    public void removeTriangles(List<Triangle> triangles) {
        faces.removeAll(triangles);
    }

    public void removeTrianglesByVertex(int vertexIndex) {
        removeTrianglesByVertex(vertexBuffer.get(vertexIndex));
    }

    public void removeTrianglesByVertex(Vertex v) {
        removeTriangles(getTianglesContaining(v));
    }

    public void removeTrianglesByVertices(int... verticesIndices) {
        for (int i : verticesIndices) {
            removeTrianglesByVertex(i);
        }
    }

    public void removeTrianglesByVertices(Vertex... vertices) {
        for (Vertex v : vertices) {
            removeTrianglesByVertex(v);
        }
    }

    public void removeAndRepare(int vertexindex) {
        Vertex v = vertexBuffer.get(vertexindex);
        List<Triangle> tris = getTianglesContaining(v);
        Triangle tri0 = tris.remove(0);
        Vertex v1 = tri0.nextVertexAfter(v);
        Vertex v2 = tri0.nextVertexAfter(v1);
        faces.remove(tri0);
        while (!tris.isEmpty()) {
            Triangle tri = null;
            for (Triangle t : tris) {
                if (t.contains(v2)) {
                    tri = t;
                    break;
                }
            }
            if (tri == null) {
                tris.add(tri0);
                tri0 = tris.remove(0);
                v1 = tri0.nextVertexAfter(v);
                v2 = tri0.nextVertexAfter(v1);
                faces.remove(tri0);
                continue;

            }
            tris.remove(tri);
            faces.remove(tri);
            Vertex v3 = tri.nextVertexAfter(v2);
            if (v1 == v3) {
                continue;
            }
            createTriangle(v1, v2, v3);
            v2 = v3;
        }
    }

    public void retainTrianglesByIndices(int[] indices) {
        retainTriangles(getTriangles(indices));
    }

    public void retainTrianglesByIndices(List<Integer> indices) {
        retainTriangles(getTriangles(indices));
    }

    public void retainTriangles(List<Triangle> triangles) {
        faces.retainAll(triangles);
    }

    public Triangle getTriangle(int index) {
        return faces.get(index);
    }

    public List<Triangle> getTriangles(int[] indices) {
        ArrayList<Triangle> result = new ArrayList<Triangle>(indices.length);
        for (int i : indices) {
            result.add(faces.get(i));
        }
        return result;
    }

    public List<Triangle> getTriangles(List<Integer> indices) {
        ArrayList<Triangle> result = new ArrayList<Triangle>(indices.size());
        for (Integer i : indices) {
            result.add(faces.get(i));
        }
        return result;
    }

    public static void sortAndDeleteDuplicate(List<Integer> list) {
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        ListIterator<Integer> iterator = list.listIterator();
        int lastIndex = -1;
        while (iterator.hasNext()) {
            int currentIndex = iterator.next();
            if (currentIndex == lastIndex) {
                iterator.remove();
            } else {
                lastIndex = currentIndex;
            }
        }
    }

    public void rescale(double factor) {
        rescale(factor, factor, factor);
    }

    public void rescale(double factorX, double factorY, double factorZ) {
        for (Vertex v : vertexBuffer.vertices) {
            v.setX(v.x() * factorX);
            v.setY(v.y() * factorY);
            v.setZ(v.z() * factorZ);
        }
    }

    public void rescaleUVFromTo(double[] from, double[] to) {
        rescaleUVFromTo(from[0], from[1], from[2], from[3], to[0], to[1], to[2], to[3]);
    }

    public void rescaleUVFromTo(double minUF, double maxUF, double minVF, double maxVF, double minUT, double maxUT, double minVT, double maxVT) {
        for (Vertex v : vertexBuffer.vertices) {
            if (!getTianglesContaining(v).isEmpty()) {
                v.setU(Functions.changeInterval(v.u(), minUF, maxUF, minUT, maxUT));
                v.setV(Functions.changeInterval(v.v(), minVF, maxVF, minVT, maxVT));
            }
        }
    }

    public void rescaleUVFrom(double[] uvs) {
        rescaleUVFrom(uvs[0], uvs[1], uvs[2], uvs[3]);
    }

    public void rescaleUVFrom(double minU, double maxU, double minV, double maxV) {
        rescaleUVFromTo(minU, maxU, minV, maxV, 0, 1, 0, 1);
    }

    public void rescaleUVTo(double[] uvs) {
        rescaleUVTo(uvs[0], uvs[1], uvs[2], uvs[3]);
    }

    public void rescaleUVTo(double minU, double maxU, double minV, double maxV) {
        rescaleUVFromTo(0, 1, 0, 1, minU, maxU, minV, maxV);
    }

    public void normalizeUV() {
        double minU = Double.POSITIVE_INFINITY;
        double minV = Double.POSITIVE_INFINITY;
        double maxU = Double.NEGATIVE_INFINITY;
        double maxV = Double.NEGATIVE_INFINITY;
        for (Vertex v : vertexBuffer.vertices) {
            if (!getTianglesContaining(v).isEmpty()) {
                minU = Math.min(minU, v.u());
                minV = Math.min(minV, v.v());
                maxU = Math.max(maxU, v.u());
                maxV = Math.max(maxV, v.v());
            }
        }
        rescaleUVFrom(minU, maxU, minV, maxV);
    }

    public void projectUV(int axisU, int axisV) {
        projectUV(axisU, axisV, false, false);
    }

    public void projectUV(int axisU, int axisV, boolean inverseU, boolean inverseV) {
        double[][] boundingbox = getBoundingBox();
        for (Triangle t : faces) {
            t.v1.setU(Functions.changeInterval(t.v1.getPosition(axisU), boundingbox[0][axisU], boundingbox[1][axisU], inverseU ? 1 : 0, inverseU ? 0 : 1));
            t.v1.setV(Functions.changeInterval(t.v1.getPosition(axisV), boundingbox[0][axisV], boundingbox[1][axisV], inverseV ? 1 : 0, inverseV ? 0 : 1));

            t.v2.setU(Functions.changeInterval(t.v2.getPosition(axisU), boundingbox[0][axisU], boundingbox[1][axisU], inverseU ? 1 : 0, inverseU ? 0 : 1));
            t.v2.setV(Functions.changeInterval(t.v2.getPosition(axisV), boundingbox[0][axisV], boundingbox[1][axisV], inverseV ? 1 : 0, inverseV ? 0 : 1));

            t.v3.setU(Functions.changeInterval(t.v3.getPosition(axisU), boundingbox[0][axisU], boundingbox[1][axisU], inverseU ? 1 : 0, inverseU ? 0 : 1));
            t.v3.setV(Functions.changeInterval(t.v3.getPosition(axisV), boundingbox[0][axisV], boundingbox[1][axisV], inverseV ? 1 : 0, inverseV ? 0 : 1));
        }
    }

    public void createUVImage(int width, int height) {
        createUVImage(width, height, false);
    }

    public void createUVImage(int width, int height, boolean printVertices) {
        createUVImage(width, height, printVertices, false);
    }

    public void createUVImage(int width, int height, boolean printVertices, boolean onlyClockVised) {
        createUVImage(width, height, printVertices, onlyClockVised, null);
    }

    public void createUVImage(int width, int height, boolean printVertices, boolean onlyClockVised, String fileName) {
        createUVImage(width, height, printVertices, onlyClockVised, fileName, 0, 0, 1, 1);
    }

    public void createUVImage(int width, int height, boolean printVertices, boolean onlyClockVised, String fileName, double x, double y, double scalex, double scaley) {

        BufferedImage off_Image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = off_Image.createGraphics();

        if (fileName != null && (new File(fileName)).exists()) {
            try {
                BufferedImage img = ImageIO.read(new File(fileName));

//                System.out.format("x=%d y=%d w=%d h=%d\n",(int)(x * width * scalex), (int)(y * height * scaley), (int) (off_Image.getWidth()*scalex), (int) (off_Image.getHeight()*scaley));
                g2.drawImage(img, (int) (x * width * scalex), (int) (y * height * scaley), (int) (width * scalex), (int) (height * scaley), null);
            } catch (IOException ex) {
                Logger.getLogger(SubMesh.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {

            g2.setColor(Color.white);
            g2.fillRect(0, 0, width, height);
        }

        drawUV(g2, width, height, printVertices, onlyClockVised);

        try {
            File outputfile = new File("./Player/Data/media/" + name + ".png");
            if (ImageIO.write(off_Image, "png", outputfile)) {
                System.out.println("Image saved as: " + outputfile.getCanonicalPath());
            } else {
                System.out.println("Can not save image: " + outputfile.getCanonicalPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void drawUV(Graphics g2, int width, int height, boolean printVertices, boolean onlyClockVised) {

        g2.setColor(Color.BLACK);
        Color green = new Color(0, 100, 0);

        if (printVertices) {
            vertexBuffer.updateVerticesIndices();
        }

        int faceCount = 0;
        for (Triangle t : faces) {
            if ((t.v1.u() >= 0 && t.v1.u() <= 1) || (t.v1.v() >= 0 && t.v1.v() <= 1)
                    || (t.v2.u() >= 0 && t.v2.u() <= 1) || (t.v2.v() >= 0 && t.v2.v() <= 1)
                    || (t.v3.u() >= 0 && t.v3.u() <= 1) || (t.v3.v() >= 0 && t.v3.v() <= 1)) {
                if (!onlyClockVised || t.getUVNormal() < 0) {
                    int[] p1 = {(int) (width * (t.u1()) + 0.5), (int) (height * (t.v1()) + 0.5)};
                    int[] p2 = {(int) (width * (t.u2()) + 0.5), (int) (height * (t.v2()) + 0.5)};
                    int[] p3 = {(int) (width * (t.u3()) + 0.5), (int) (height * (t.v3()) + 0.5)};
                    g2.setColor(Color.BLACK);
                    g2.drawLine(p1[0], p1[1], p2[0], p2[1]);
                    g2.drawLine(p2[0], p2[1], p3[0], p3[1]);
                    g2.drawLine(p3[0], p3[1], p1[0], p1[1]);
//                }
//            }
//        }
                    if (printVertices) {
//            vertexBuffer.updateVerticesIndices();
//            int faceCount = 0;
//            for(Triangle t : faces){
//                if(t.v1.u()>=0 && t.v1.u()<=1 && t.v1.v()>=0 && t.v1.v()<=1 &&
//                   t.v2.u()>=0 && t.v2.u()<=1 && t.v2.v()>=0 && t.v2.v()<=1 &&
//                   t.v3.u()>=0 && t.v3.u()<=1 && t.v3.v()>=0 && t.v3.v()<=1){
//                    if(!onlyClockVised || t.getUVNormal()<0){
//                        int[] p1 = {(int)(width*(t.u1())+0.5), (int)(height*(t.v1())+0.5)};
//                        int[] p2 = {(int)(width*(t.u2())+0.5), (int)(height*(t.v2())+0.5)};
//                        int[] p3 = {(int)(width*(t.u3())+0.5), (int)(height*(t.v3())+0.5)};
                        g2.setColor(Color.RED);
                        g2.drawString("" + t.v1.index, p1[0], p1[1]);
                        g2.drawString("" + t.v2.index, p2[0], p2[1]);
                        g2.drawString("" + t.v3.index, p3[0], p3[1]);
                        g2.setColor(green);
                        g2.drawString("" + faceCount, (p1[0] + p2[0] + p3[0]) / 3, (p1[1] + p2[1] + p3[1]) / 3);
                    }
                }
            }
            faceCount++;
        }
    }

    public double[][] getBoundingBox() {
        double[][] bounds = new double[2][3];
        bounds[0][0] = Double.POSITIVE_INFINITY;
        bounds[0][1] = Double.POSITIVE_INFINITY;
        bounds[0][2] = Double.POSITIVE_INFINITY;
        bounds[1][0] = Double.NEGATIVE_INFINITY;
        bounds[1][1] = Double.NEGATIVE_INFINITY;
        bounds[1][2] = Double.NEGATIVE_INFINITY;
        for (Triangle t : faces) {
            bounds[0][0] = Math.min(bounds[0][0], t.minX());
            bounds[0][1] = Math.min(bounds[0][1], t.minY());
            bounds[0][2] = Math.min(bounds[0][2], t.minZ());

            bounds[1][0] = Math.max(bounds[1][0], t.maxX());
            bounds[1][1] = Math.max(bounds[1][1], t.maxY());
            bounds[1][2] = Math.max(bounds[1][2], t.maxZ());
        }
        return bounds;
    }

    public void sortTiangles() {
        vertexBuffer.updateVerticesIndices();
        Collections.sort(faces, Triangle.getComparator());
    }

    public void sortVertices(double x, double y, double z) {
        sortVertices(new Vec3d(x, y, z));
    }

    public void sortVertices() {
        sortVertices(vertexBuffer.get(0).position);
    }

    public void sortVertices(final Vec3d ref) {
        Collections.sort(vertexBuffer.vertices, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex o1, Vertex o2) {
                Vec3d dist1 = new Vec3d(o1.position);
                Vec3d dist2 = new Vec3d(o2.position);
                dist1.minus(ref);
                dist2.minus(ref);
                double d1 = dist1.length();
                double d2 = dist2.length();
                if (d1 < d2) {
                    return -1;
                }
                if (d1 > d2) {
                    return 1;
                }
                return 0;
            }

        });
    }

    public void convex(List<Triangle> trianglesOrigine) {
        System.out.println("size before : " + faces.size() + " - " + trianglesOrigine.size());
        ArrayList<Triangle> triangles = new ArrayList<Triangle>(trianglesOrigine);
        this.removeTriangles(trianglesOrigine);
        int debugCount = 0;
        for (int i = 0; i < triangles.size(); ++i) {
            Triangle t1 = triangles.get(i);
            for (int j = i + 1; j < triangles.size(); ++j) {
                Triangle t2 = triangles.get(j);
                if (Triangle.vertexShared(t1, t2) == 2) {
                    double convexity = Triangle.convexity(t1, t2);
                    if (convexity < 0) {
                        Vertex v1 = null;
                        if (!t2.contains(t1.v1)) {
                            v1 = t1.v1;
                        } else {
                            if (!t2.contains(t1.v2)) {
                                v1 = t1.v2;
                            } else {
                                v1 = t1.v3;
                            }
                        }
                        Vertex v2 = t1.nextVertexAfter(v1);
                        Vertex v3 = t2.nextVertexAfter(v2);
                        Vertex v4 = t2.nextVertexAfter(v3);

                        Triangle newT1 = new Triangle(v1, v2, v3);
                        Triangle newT2 = new Triangle(v1, v3, v4);
                        double newConvexcity = Triangle.convexity(newT1, newT2);
                        if (convexity < newConvexcity //&& newConvexcity<0.5
                                ) {
                            System.out.println("modify quad " + v1.index + " " + v2.index + " " + v3.index + " " + v4.index);
                            triangles.set(i, newT1);
                            triangles.set(j, newT2);
                            debugCount++;
                            break;
                        }
                    }
                }
            }
        }
        this.faces.addAll(triangles);
        System.out.println("size after : " + faces.size() + " - " + triangles.size());

        System.out.println("nombre d'invertions : " + debugCount);
    }

    public void replaceVertex(int indexOfOldVertex, int indexOfNewVertex) {
        replaceVertex(vertexBuffer.get(indexOfOldVertex), vertexBuffer.get(indexOfNewVertex));
    }

    public void replaceVertex(Vertex oldVertex, Vertex newVertex) {
        if (!vertexBuffer.vertices.contains(newVertex)) {
            vertexBuffer.vertices.add(newVertex);
        }
        for (Triangle t : getTianglesContaining(oldVertex)) {
            t.replace(oldVertex, newVertex);
        }
    }

    public void cleanFakeTriangles() {
        cleanFakeTriangles(faces);
    }

    public static void cleanFakeTriangles(List<Triangle> tris) {
        ListIterator<Triangle> iter = tris.listIterator();
        while (iter.hasNext()) {
            Triangle tri = iter.next();
            if (tri.v1 == tri.v2 || tri.v1 == tri.v3 || tri.v2 == tri.v3) {
                iter.remove();
            }
        }
    }

    public void inversQuad(int indexTriangle1, int indexTriangle2) {
        Triangle t1 = faces.get(indexTriangle1);
        Triangle t2 = faces.get(indexTriangle2);
        Vertex v1 = null;
        if (!t2.contains(t1.v1)) {
            v1 = t1.v1;
        } else {
            if (!t2.contains(t1.v2)) {
                v1 = t1.v2;
            } else {
                v1 = t1.v3;
            }
        }
        Vertex v2 = t1.nextVertexAfter(v1);
        Vertex v3 = t2.nextVertexAfter(v2);
        Vertex v4 = t2.nextVertexAfter(v3);

        Triangle newT1 = new Triangle(v1, v2, v3);
        Triangle newT2 = new Triangle(v1, v3, v4);

        faces.set(indexTriangle1, newT1);
        faces.set(indexTriangle2, newT2);
    }

    public void detachAllFaces() {
        for (Triangle t : faces) {
            t.v1 = new Vertex(t.v1);
            t.v2 = new Vertex(t.v2);
            t.v3 = new Vertex(t.v3);
            vertexBuffer.vertices.add(t.v1);
            vertexBuffer.vertices.add(t.v2);
            vertexBuffer.vertices.add(t.v3);
        }
        this.removeUnusedVertices();
    }

    public void translate(double x, double y, double z) {
        vertexBuffer.translate(x, y, z);
    }

    public void translate(Vec3d vect) {
        vertexBuffer.translate(vect);
    }

    /**
     *
     * @param rx angle in degree
     * @param ry angle in degree
     * @param rz angle in degree
     */
    public void rotate(double rx, double ry, double rz) {
        rotate(rx, ry, rz, 0, 0, 0);
    }

    public void rotate(double rx, double ry, double rz, double x, double y, double z) {
        Quaternion q = new Quaternion();
        q.fromEulerXYZByAngle(rx, ry, rz);
        rotate(q, x, y, z);
    }

    public void rotate(Quaternion q) {
        rotate(q, 0, 0, 0);
    }

    public void rotate(Quaternion q, double x, double y, double z) {
        rotate(q, new Vec3d(x, y, z));
    }

    public void rotate(Quaternion q, Vec3d pivot) {
        Vec3d usedPivot = new Vec3d(pivot); //ensure that it is never used
        usedPivot.negate();
        vertexBuffer.translate(usedPivot);
        vertexBuffer.rotate(q);
        usedPivot.negate();
        vertexBuffer.translate(usedPivot);
    }

    public void scale(double x, double y, double z) {
        vertexBuffer.scale(x, y, z);
    }

}
