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
import greta.core.util.xml.XMLTree;

/**
 *
 * @author Andre-Marie Pez
 */
public class WOS {

    static String wosnum = "001";

    static double h = 0.21;
    static double w = 0.297;

    static double margin = 0.005;

    public static void main(String[] ss){
         Mesh m = new Mesh();
         SubMesh sm = m.createSubMesh();
         sm.name = "wos";
         sm.material = "wos"+wosnum;
         sm.vertexBuffer = new VertexBuffer(4);
         Vertex v1 = new Vertex(margin, h-margin, 0);
         v1.setU(0);v1.setV(0);
         Vertex v2 = new Vertex(w-margin, h-margin, 0);
         v2.setU(1);v2.setV(0);
         Vertex v3 = new Vertex(w-margin, margin, 0);
         v3.setU(1);v3.setV(1);
         Vertex v4 = new Vertex(margin, margin, 0);
         v4.setU(0);v4.setV(1);

         sm.vertexBuffer.addVertex(v1);
         sm.vertexBuffer.addVertex(v2);
         sm.vertexBuffer.addVertex(v3);
         sm.vertexBuffer.addVertex(v4);
         sm.createTriangle(v1, v3, v2);
         sm.createTriangle(v1, v4, v3);

         sm.smoothNormals();


         SubMesh sm2 = m.createSubMesh();
         sm2.name = "wos_border";
         sm2.material = "wos_border";
         sm2.vertexBuffer = new VertexBuffer(4);
         Vertex v1_ = new Vertex(0, h, 0);
         v1_.setU(0);v1_.setV(0);
         Vertex v2_ = new Vertex(w, h, 0);
         v2_.setU(1);v2_.setV(0);
         Vertex v3_ = new Vertex(w, 0, 0);
         v3_.setU(1);v3_.setV(1);
         Vertex v4_ = new Vertex(0, 0, 0);
         v4_.setU(0);v4_.setV(1);


         Vertex v1i = new Vertex(margin, h-margin, 0);
         v1i.setU(0.499);v1i.setV(0.499);
         Vertex v2i = new Vertex(w-margin, h-margin, 0);
         v2i.setU(0.501);v2i.setV(0.499);
         Vertex v3i = new Vertex(w-margin, margin, 0);
         v3i.setU(0.501);v3i.setV(0.501);
         Vertex v4i = new Vertex(margin, margin, 0);
         v4i.setU(0.499);v4i.setV(0.501);

         sm2.vertexBuffer.addVertex(v1i);
         sm2.vertexBuffer.addVertex(v2i);
         sm2.vertexBuffer.addVertex(v3i);
         sm2.vertexBuffer.addVertex(v4i);
         sm2.vertexBuffer.addVertex(v1_);
         sm2.vertexBuffer.addVertex(v2_);
         sm2.vertexBuffer.addVertex(v3_);
         sm2.vertexBuffer.addVertex(v4_);


         sm2.createTriangle(v1_, v1i, v2_);
         sm2.createTriangle(v1i, v2i, v2_);

         sm2.createTriangle(v2_, v2i, v3_);
         sm2.createTriangle(v2i, v3i, v3_);

         sm2.createTriangle(v3_, v3i, v4_);
         sm2.createTriangle(v3i, v4i, v4_);

         sm2.createTriangle(v4_, v4i, v1_);
         sm2.createTriangle(v4i, v1i, v1_);


         sm2.smoothNormals();

         XMLTree xml = OgreXML.writMesh(m);
         CorrectionMesh.saveAndConvert1dot6(xml, "./Player/Data/media/office/wos/wos"+wosnum+".mesh.xml");
    }

    public static void main_simple(String[] ss){
         Mesh m = new Mesh();
         SubMesh sm = m.createSubMesh();
         sm.name = "wos";
         sm.material = "wos"+wosnum;
         sm.vertexBuffer = new VertexBuffer(4);
         Vertex v1 = new Vertex(0, 0.21, 0);
         v1.setU(0);v1.setV(0);
         Vertex v2 = new Vertex(0.297, 0.21, 0);
         v2.setU(1);v2.setV(0);
         Vertex v3 = new Vertex(0.297, 0, 0);
         v3.setU(1);v3.setV(1);
         Vertex v4 = new Vertex(0, 0, 0);
         v4.setU(0);v4.setV(1);
         sm.vertexBuffer.addVertex(v1);
         sm.vertexBuffer.addVertex(v2);
         sm.vertexBuffer.addVertex(v3);
         sm.vertexBuffer.addVertex(v4);
         sm.createTriangle(v1, v3, v2);
         sm.createTriangle(v1, v4, v3);

         sm.smoothNormals();

         XMLTree xml = OgreXML.writMesh(m);
         CorrectionMesh.saveAndConvert1dot6(xml, "./Player/Data/media/office/wos/wos"+wosnum+".mesh.xml");
    }
}
