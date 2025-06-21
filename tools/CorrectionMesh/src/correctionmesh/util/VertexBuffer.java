/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package correctionmesh.util;

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.Iterator;

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
