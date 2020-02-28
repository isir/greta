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
