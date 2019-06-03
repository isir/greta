/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package correctionmesh.util;

import java.util.ArrayList;
import java.util.Iterator;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;

/**
 *
 * @author Andre-Marie
 */
public class VertexBuffer implements Iterable<Vertex> {

    ArrayList<Vertex> vertices;

    public VertexBuffer(int intitialVertexNumber) {
        vertices = new ArrayList<Vertex>(intitialVertexNumber);
    }

    public void updateVerticesIndices() {
        for (int i = 0; i < vertices.size(); ++i) {
            vertices.get(i).index = i;
        }
    }

    public void addVertex(Vertex v) {
        if (v != null && !vertices.contains(v)) {
            vertices.add(v);
        }
    }

    public int indexOf(Vertex v) {
        return vertices.indexOf(v);
    }

    public VertexBuffer(VertexBuffer toCopy) {
        vertices = new ArrayList<Vertex>(toCopy.vertices);
    }

    public Vertex get(int index) {
        return vertices.get(index);
    }

    public int size() {
        return vertices.size();
    }

    public void translate(double x, double y, double z) {
        for (int i = 0; i < vertices.size(); ++i) {
            vertices.get(i).translate(x, y, z);
        }
    }

    public void translate(Vec3d vect) {
        for (int i = 0; i < vertices.size(); ++i) {
            vertices.get(i).translate(vect);
        }
    }

    public void rotate(Quaternion q) {
        for (int i = 0; i < vertices.size(); ++i) {
            vertices.get(i).rotate(q);
        }
    }

    public void scale(Vec3d vect) {
        for (int i = 0; i < vertices.size(); ++i) {
            vertices.get(i).scale(vect);
        }
    }

    public void rotate(double x, double y, double z) {
        for (int i = 0; i < vertices.size(); ++i) {
            vertices.get(i).rotate(x, y, z);
        }
    }

    public void scale(double x, double y, double z) {
        for (int i = 0; i < vertices.size(); ++i) {
            vertices.get(i).scale(x, y, z);
        }
    }

    public void scalePoses(double x, double y, double z) {
        for (int i = 0; i < vertices.size(); ++i) {
            vertices.get(i).scalePose(x, y, z);
        }
    }

    @Override
    public Iterator<Vertex> iterator() {
        return vertices.iterator();
    }

    public void ensureCapacity(int i) {
        vertices.ensureCapacity(i);
    }

    void set(int index, Vertex vert) {
        vertices.set(index, vert);
    }
}
