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
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;

/**
 *
 * @author Andre-Marie Pez
 */
public class OfficeObjects {

    static String path = "./Player/Data/media/office/";
    static String baseFileName = "paper";


    public static void main(String[] args){

        CorrectionMesh.convert1dot6(path+baseFileName+".mesh");
        XMLParser parser = XML.createParser();
        parser.setValidating(false);
        Mesh m = OgreXML.readMesh(parser.parseFile(path+baseFileName+".mesh.xml"));

        for(int i=0; i<m.getNumberOfSubMeshes(); ++i)
            System.out.println("submesh "+i+":  material="+m.getSubMesh(i).material);


//        m.getSubMesh(0).rotate(0, 0, 102.5);
//        m.getSubMesh(0).rotate(0, 90, 0);
        m.getSubMesh(0).scale(0.0001, 0.0001, 0.0001);
        m.getSubMesh(0).material = "objets_divers_2";
        m.getSubMesh(0).mergeVertices(true, false, true);
        m.getSubMesh(0).smoothNormals();
        m.getSubMesh(0).removeUnusedVertices();
        m.setCoordinateZeroToTheMinPointOfTheBoundingBox();
        System.out.println("<size x=\""+m.getBoudingBox()[1][0]+"\" y=\""+m.getBoudingBox()[1][1]+"\" z=\""+m.getBoudingBox()[1][2]+"\" />");

        m.getSubMesh(0).createUVImage(1000, 1000);
        OgreXML.writMesh(m).save(path+baseFileName+"_.mesh.xml");
        CorrectionMesh.convert1dot6(path+baseFileName+"_.mesh.xml");
    }
}
