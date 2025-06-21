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
package correctionmesh;

import correctionmesh.util.Mesh;
import correctionmesh.util.OgreXML;
import correctionmesh.util.SubMesh;
import correctionmesh.util.Vertex;
import correctionmesh.util.VertexBuffer;
import greta.core.util.math.Vec3d;

/**
 *
 * @author Andre-Marie Pez
 */
public class CreateBone {

    public static void main(String[] aaa){
        Vec3d p0 = new Vec3d(0, 0, 0);

        Vec3d p1 = new Vec3d(0.1f, 0.2f, 0.1f);
        Vec3d p2 = new Vec3d(0.1f, 0.2f, -0.1f);
        Vec3d p3 = new Vec3d(-0.1f, 0.2f, -0.1f);
        Vec3d p4 = new Vec3d(-0.1f, 0.2f, 0.1f);

        Vec3d p5 = new Vec3d(0, 1, 0);

        Mesh mesh = new Mesh();
        SubMesh sm = mesh.createSubMesh();
        sm.vertexBuffer = new VertexBuffer(24);
        sm.name = "bone";
        sm.material = "bone";

        addTriangle(sm, p0, p2, p1);
        addTriangle(sm, p0, p3, p2);
        addTriangle(sm, p0, p4, p3);
        addTriangle(sm, p0, p1, p4);


        addTriangle(sm, p2, p5, p1);
        addTriangle(sm, p3, p5, p2);
        addTriangle(sm, p4, p5, p3);
        addTriangle(sm, p1, p5, p4);

        sm.smoothNormals();

        String fileName = "./Player/Data/media/bone.mesh.xml";
        OgreXML.writMesh(mesh).save(fileName);
        CorrectionMesh.convert1dot6(fileName);
    }

    private static void addTriangle(SubMesh sm, Vec3d p1, Vec3d p2, Vec3d p3){
        Vertex v1 = new Vertex(p1.x(), p1.y(), p1.z());
        Vertex v2 = new Vertex(p2.x(), p2.y(), p2.z());
        Vertex v3 = new Vertex(p3.x(), p3.y(), p3.z());

        sm.vertexBuffer.addVertex(v1);
        sm.vertexBuffer.addVertex(v2);
        sm.vertexBuffer.addVertex(v3);

        sm.createTriangle(v1, v2, v3);
    }
}
