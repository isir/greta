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
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import java.util.ArrayList;

/**
 *
 * Contains the function that created the walls of the office :<code>createWalls</code><br/>
 * and the functions that created the debug meshes for audio (<code>createAudioMesh</code>) and light (<code>createLightMesh</code>)
 * @author Andre-Marie
 */
public class MiscObjects {

    public static void createWalls() {

        String meshFile = "./Player/Data/media/office/murs.mesh";
        CorrectionMesh.convert1dot6(meshFile);
        XMLParser parser = XML.createParser();
        parser.setValidating(false);
        Mesh mesh = OgreXML.readMesh(parser.parseFile(meshFile + ".xml"));

        SubMesh allWall = mesh.getSubMesh(0);
        allWall.mergeVertices(true, true, true);
        allWall.removeUnusedVertices();
        allWall.sortVertices(12.11, -1.78, -3.33);
        allWall.sortTiangles();
        for (Vertex v : allWall.vertexBuffer) {
            System.out.println(v);
        }
        System.out.println(allWall.vertexBuffer.size() + " vertices");


        //Wall0
        int count = 0;
        createWall(mesh, 12.1, 12.4, -1, 3, -3.5, 7.75, count++);
        createWall(mesh, 2.4, 12.4, -1, 3, 7.4, 7.75, count++);
        createWall(mesh, 2.4, 2.6, -1, 3, 0.1, 7.75, count++);
        createWall(mesh, -6.4, 2.6, -1, 3, 0.1, 0.3, count++);
        createWall(mesh, -6.4, -6.3, -1, 3, -7.4, 0.3, count++);
        createWall(mesh, -6.4, -5.2, -1, 3, -7.4, -7.3, count++);
        createWall(mesh, -5.4, -5.2, -1, 3, -7.4, -5.5, count++);
        createWall(mesh, -5.4, -0.3, -1, 3, -5.7, -5.5, count++);
        createWall(mesh, -0.4, -0.3, -1, 3, -5.7, -3.8, count++);
        createWall(mesh, -0.4, 4.5, -1, 3, -3.9, -3.8, count++);
        createWall(mesh, 4.4, 4.5, -1, 3, -3.9, 0.3, count++);
        createWall(mesh, 4.3, 12.2, -1, 3, -3.5, -3.2, count++);
        createWall(mesh, 7.7, 12.2, -1, 3, 0.8, 1, count++);
        createWall(mesh, 7.9, 8.2, -1, 3, 0.8, 7.7, count++);
        createWall(mesh, 10, 10.1, -1, 3, -3.4, -0.9, count++);
        createWall(mesh, 8.2, 10.1, -1, 3, -1.1, -0.9, count++);
        createWall(mesh, 8.2, 8.4, -1, 3, -1.3, -0.9, count++);
        createWall(mesh, 7.4, 8.4, -1, 3, -1.3, -1.1, count++);

        createWall(mesh, 6.9, 7.1, -1, 3, -0.4, 0.4, count++);
        createWall(mesh, 3.7, 7.1, -1, 3, 0.1, 0.3, count++);

        createOblicWall(mesh, 6.9, 7.6, -1, 3, -1.3, -0.3, count++);//oblique
        createOblicWall(mesh, 6.9, 7.9, -1, 3, 0.2, 1, count++);//oblique
        createOblicWall(mesh, 1.9, 4.7, -1, 3, -0.3, 2.3, count++);//oblique

        for (int i = 0; i < count; ++i) {
            System.out.println("office.wall." + i + " = office/wall" + i + ".mesh");
        }
    }

    public static void createWall(Mesh mesh, double minX, double maxX, double minY, double maxY, double minZ, double maxZ, int idWall) {
        double[] min = {minX, minY, minZ};
        double[] max = {maxX, maxY, maxZ};
        createWall(mesh, new double[][]{min, max}, idWall);
    }

    public static void createWall(Mesh mesh, double[][] bounds, int idWall) {
        SubMesh wall0 = mesh.getSubMesh(0).duplicate();
        wall0.retainTriangles(wall0.getTrianglesInBounds(bounds));
        wall0.removeUnusedVertices();
        double[][] bounds0 = wall0.getBoundingBox();
        System.out.print(
                "\t\t\t\t<node id=\"mur" + idWall + "\">\r\n"
                + "\t\t\t\t\t<position x=\""
                + (Math.round(bounds0[0][0] * 10000) / 10000.0) + "\" y=\""
                + (Math.round(bounds0[0][1] * 10000) / 10000.0) + "\" z=\""
                + (Math.round(bounds0[0][2] * 10000) / 10000.0) + "\" />\r\n");
        wall0.translate(-bounds0[0][0], -bounds0[0][1], -bounds0[0][2]);

        wall0.sortVertices(0, 0, 0);
        wall0.sortTiangles();
        Mesh wall0mesh = new Mesh();
        wall0mesh.addSubMesh(wall0);
        bounds0 = wall0.getBoundingBox();
        System.out.print(
                "\t\t\t\t\t<leaf id=\"wall" + idWall + "\" reference=\"office.wall." + idWall + "\" >\r\n"
                + "\t\t\t\t\t\t<size x=\""
                + (Math.round(bounds0[1][0] * 10000) / 10000.0) + "\" y=\""
                + (Math.round(bounds0[1][1] * 10000) / 10000.0) + "\" z=\""
                + (Math.round(bounds0[1][2] * 10000) / 10000.0) + "\" />\r\n"
                + "\t\t\t\t\t</leaf>\r\n"
                + "\t\t\t\t</node>\r\n");
        OgreXML.writMesh(wall0mesh).save("./Player/Data/media/office/wall" + idWall + ".mesh.xml");
        CorrectionMesh.convert1dot6("./Player/Data/media/office/wall" + idWall + ".mesh.xml");

        //remove used triangles
        SubMesh sub0 = mesh.getSubMesh(0);
        sub0.removeTriangles(sub0.getTrianglesInBounds(bounds));
        sub0.removeUnusedVertices();
    }

    public static void createOblicWall(Mesh mesh, double minX, double maxX, double minY, double maxY, double minZ, double maxZ, int idWall) {
        double[] min = {minX, minY, minZ};
        double[] max = {maxX, maxY, maxZ};
        createOblicWall(mesh, new double[][]{min, max}, idWall);
    }

    public static void createOblicWall(Mesh mesh, double[][] bounds, int idWall) {

        SubMesh wall0 = mesh.getSubMesh(0).duplicate();
        wall0.retainTriangles(wall0.getTrianglesInBounds(bounds));
        wall0.removeUnusedVertices();

        double rotation = 0;
        double[][] minbounds = wall0.getBoundingBox();
        double targetRotation = 0;
        while (rotation < 180) {
            SubMesh wall0x = wall0.duplicate();
            wall0x.rotate(0, rotation, 0);
            double[][] currentBounds = wall0x.getBoundingBox();
//            System.out.println("r: "+rotation+" ["+currentBounds[0][2]+", "+currentBounds[1][2]+"] -> "+(currentBounds[1][2]-currentBounds[0][2]));
            if (minbounds[1][2] - minbounds[0][2] > currentBounds[1][2] - currentBounds[0][2]) {
                minbounds = currentBounds;
                targetRotation = rotation;
            }
            rotation += 0.5;
        }
        Quaternion rot = new Quaternion();
        rot.fromEulerXYZ(0, Math.toRadians(targetRotation), 0);
        wall0.rotate(0, targetRotation, 0);
        double[][] bounds0 = wall0.getBoundingBox();
        Vec3d pos = rot.inverseRotate(new Vec3d(bounds0[0][0], bounds0[0][1], bounds0[0][2]));
        System.out.print(
                "\t\t\t\t<node id=\"mur" + idWall + "\">\r\n"
                + "\t\t\t\t\t<orientation x=\"0\" y=\"" + (-targetRotation) + "\" z=\"0\" />\r\n"
                + "\t\t\t\t\t<position x=\""
                + (Math.round(pos.x() * 10000) / 10000.0) + "\" y=\""
                + (Math.round(pos.y() * 10000) / 10000.0) + "\" z=\""
                + (Math.round(pos.z() * 10000) / 10000.0) + "\" />\r\n");
        wall0.translate(-bounds0[0][0], -bounds0[0][1], -bounds0[0][2]);

        wall0.sortVertices(0, 0, 0);
        wall0.sortTiangles();
        Mesh wall0mesh = new Mesh();
        wall0mesh.addSubMesh(wall0);
        bounds0 = wall0.getBoundingBox();
        System.out.print(
                "\t\t\t\t\t<leaf id=\"wall" + idWall + "\" reference=\"office.wall." + idWall + "\" >\r\n"
                + "\t\t\t\t\t\t<size x=\""
                + (Math.round(bounds0[1][0] * 10000) / 10000.0) + "\" y=\""
                + (Math.round(bounds0[1][1] * 10000) / 10000.0) + "\" z=\""
                + (Math.round(bounds0[1][2] * 10000) / 10000.0) + "\" />\r\n"
                + "\t\t\t\t\t</leaf>\r\n"
                + "\t\t\t\t</node>\r\n");
        OgreXML.writMesh(wall0mesh).save("./Player/Data/media/office/wall" + idWall + ".mesh.xml");
        CorrectionMesh.convert1dot6("./Player/Data/media/office/wall" + idWall + ".mesh.xml");

        //remove used triangles
        SubMesh sub0 = mesh.getSubMesh(0);
        sub0.removeTriangles(sub0.getTrianglesInBounds(bounds));
        sub0.removeUnusedVertices();
    }

    public static void recentrerSurMin(String meshFile) {
        String meshXMLFile = meshFile + ".xml";
        CorrectionMesh.convert1dot6(meshFile);
        XMLParser parser = XML.createParser();
        parser.setValidating(false);
        Mesh mesh = OgreXML.readMesh(parser.parseFile(meshXMLFile));
        mesh.setCoordinateZeroToTheMinPointOfTheBoundingBox();
        OgreXML.writMesh(mesh).save(meshXMLFile);
        CorrectionMesh.convert1dot6(meshXMLFile);
    }

    public static void createAudioMesh() {
        Mesh audio = new Mesh();

        int num = 36;

        int circNum = 5;
        double minAngle = 30;

        int waveCircNum = 12;
        double waveOpenAngle = 40;
        double waveAngle = 30;
        double waveIntraRadius = 0.2;

        //SPEAKER BASE
        SubMesh base = audio.createSubMesh();

        Vertex center = new Vertex(0, 0, 0);
        base.vertexBuffer.addVertex(center);
        ArrayList<Vertex> round1 = new ArrayList<Vertex>(num);
        for (int i = 0; i < num; ++i) {
            Vertex v = new Vertex(0, 1, 0);
            round1.add(v);
            base.vertexBuffer.addVertex(v);
            v.rotate(0, 0, -(360.0 / num) * i);
        }
        for (int i = 0; i < num - 1; ++i) {
            base.createTriangle(center, round1.get(i), round1.get(i + 1));
        }
        base.createTriangle(center, round1.get(num - 1), round1.get(0));

        ArrayList<Vertex> round2 = nextRound(base, round1, 0, 0, 0.4);
        ring(base, round1, round2);

        ArrayList<Vertex> round3 = nextRound(base, round2, 0, 0, 1, 2, 2, 1);
        ring(base, round2, round3);

        ArrayList<Vertex> round4 = nextRound(base, round3, 0, 0, 0.1);
        ring(base, round3, round4);

        ArrayList<Vertex> round5 = nextRound(base, round4, 0, 0, 0, 0.9, 0.9, 1);
        ring(base, round4, round5);

        //MEMBRANE
        SubMesh membrane = audio.createSubMesh();
        ArrayList<Vertex> round6 = nextRound(membrane, round5);
        ArrayList<Vertex> round7 = nextRound(membrane, round6, 0, 0, -0.5, 0.4, 0.4, 1);
        ring(membrane, round6, round7);

        //BUBBLE
        SubMesh bubble = audio.createSubMesh();
        ArrayList<Vertex> round8 = nextRound(bubble, round7);

        double deltaAngle = (90.0 - minAngle) / (circNum + 2);
        double r = round8.get(0).y() / Math.cos(Math.toRadians(minAngle));
        Vertex temp = new Vertex(0, r, 0);
        temp.rotate(minAngle, 0, 0);

        ArrayList<Vertex> last = round8;
        for (int i = 0; i < circNum; ++i) {
            double deg = deltaAngle * (i + 1) + minAngle;
            Vertex temp2 = new Vertex(0, r, 0);
            temp2.rotate(deg, 0, 0);
            ArrayList<Vertex> next = nextRound(bubble, round8, 0, 0, temp2.z() - temp.z(), temp2.y() / temp.y(), temp2.y() / temp.y(), 1);
            ring(bubble, last, next);
            last = next;
        }

        Vertex bubbleCenter = new Vertex(0, r, 0);
        bubbleCenter.rotate(90, 0, 0);
        bubbleCenter.setZ(round8.get(0).z() + bubbleCenter.z() - temp.z());
        bubble.vertexBuffer.addVertex(bubbleCenter);
        for (int i = 0; i < num - 1; ++i) {
            bubble.createTriangle(last.get(i), bubbleCenter, last.get(i + 1));
        }
        bubble.createTriangle(last.get(num - 1), bubbleCenter, last.get(0));

        //WAVES

        SubMesh wave1 = audio.createSubMesh();
        double realWaveAngle = buildWave(wave1, num, waveOpenAngle, waveCircNum, waveIntraRadius, 1.5);
        buildWave(wave1, num, waveOpenAngle, waveCircNum, waveIntraRadius, 2.5);
        buildWave(wave1, num, waveOpenAngle, waveCircNum, waveIntraRadius, 3.5);

        wave1.rotate(0, 0, 90 + realWaveAngle / 2);
        wave1.rotate(0, waveAngle, 0);
        wave1.translate(0, 0, bubbleCenter.z());

        SubMesh wave2 = audio.createSubMesh();
        realWaveAngle = buildWave(wave2, num, waveOpenAngle, waveCircNum, waveIntraRadius, 1.5);
        buildWave(wave2, num, waveOpenAngle, waveCircNum, waveIntraRadius, 2.5);
        buildWave(wave2, num, waveOpenAngle, waveCircNum, waveIntraRadius, 3.5);

        wave2.rotate(0, 0, 270 + realWaveAngle / 2);
        wave2.rotate(0, -waveAngle, 0);
        wave2.translate(0, 0, bubbleCenter.z());


        finalizeSubmesh(base, "black");
        finalizeSubmesh(membrane, "dark_grey");
        finalizeSubmesh(bubble, "black");
        finalizeSubmesh(wave1, "camera_debug_marker");
        finalizeSubmesh(wave2, "camera_debug_marker");


        String meshFile = "./Player/Data/media/audio.mesh";
        OgreXML.writMesh(audio).save(meshFile + ".xml");
        CorrectionMesh.convert1dot6(meshFile + ".xml");
    }

    private static void ring(SubMesh bafl, ArrayList<Vertex> firstRound, ArrayList<Vertex> secondRound) {
        int num = firstRound.size();
        for (int i = 0; i < num - 1; ++i) {
            bafl.createTriangle(firstRound.get(i), secondRound.get(i), firstRound.get(i + 1));
            bafl.createTriangle(secondRound.get(i + 1), firstRound.get(i + 1), secondRound.get(i));
        }
        bafl.createTriangle(firstRound.get(num - 1), secondRound.get(num - 1), firstRound.get(0));
        bafl.createTriangle(secondRound.get(0), firstRound.get(0), secondRound.get(num - 1));
    }

    private static ArrayList<Vertex> nextRound(SubMesh bafl, ArrayList<Vertex> firstRound, double tx, double ty, double tz, double sx, double sy, double sz) {
        int num = firstRound.size();
        ArrayList<Vertex> secondRound = new ArrayList<Vertex>(num);
        for (int i = 0; i < num; ++i) {
            Vertex v = new Vertex(firstRound.get(i));
            secondRound.add(v);
            bafl.vertexBuffer.addVertex(v);
            v.translate(tx, ty, tz);
            v.scale(sx, sy, sz);
        }
        return secondRound;
    }

    private static ArrayList<Vertex> nextRound(SubMesh bafl, ArrayList<Vertex> firstRound, double tx, double ty, double tz) {
        return nextRound(bafl, firstRound, tx, ty, tz, 1, 1, 1);
    }

    private static ArrayList<Vertex> nextRound(SubMesh base, ArrayList<Vertex> round1) {
        return nextRound(base, round1, 0.0, 0.0, 0.0);
    }

    private static void finalizeSubmesh(SubMesh sub, String material) {
        sub.removeUnusedVertices();
        sub.vertexBuffer.updateVerticesIndices();
        sub.sortTiangles();
        sub.material = material;
        sub.smoothNormals();
        sub.scale(0.1, 0.1, 0.1);
//        sub.translate(0, 2, 0);

    }

    private static double buildWave(SubMesh wave1, int num, double waveAngle, int waveCircNum, double waveIntraRadius, double firstWaveRadius) {
        int circNum = 5;
        double realWaveAngle = 0;
        ArrayList<Vertex> lastCirc = null;
        for (int i = 0; i < num; ++i) {
            double angle = -(360.0 / num) * i;
            if (-angle <= waveAngle) {
                realWaveAngle = -angle;
                ArrayList<Vertex> nextCirc = new ArrayList<Vertex>(waveCircNum);
                for (int j = 0; j < waveCircNum; ++j) {
                    Vertex v = new Vertex(0, 1, 0);
                    v.rotate(-(360.0 / waveCircNum) * j, 0, 0);
                    v.scale(waveIntraRadius, waveIntraRadius, waveIntraRadius);
                    v.translate(0, firstWaveRadius, 0);
                    v.rotate(0, 0, angle);
                    nextCirc.add(v);
                    wave1.vertexBuffer.addVertex(v);
                }
                if (lastCirc != null) {
                    ring(wave1, lastCirc, nextCirc);
                } else {


                    double deltaAngle = 90.0 / (circNum + 1);
                    ArrayList<Vertex> last = nextCirc;
                    for (int k = 0; k < circNum; ++k) {
                        double deg = deltaAngle * (k + 1);
                        ArrayList<Vertex> next = new ArrayList<Vertex>();
                        for (int l = 0; l < waveCircNum; ++l) {
                            Vertex v = new Vertex(0, 1, 0);
                            v.rotate(0, 0, deg);
                            v.rotate(-(360.0 / waveCircNum) * l, 0, 0);
                            v.scale(waveIntraRadius, waveIntraRadius, waveIntraRadius);
                            v.translate(0, firstWaveRadius, 0);
                            v.rotate(0, 0, angle);
                            next.add(v);
                            wave1.vertexBuffer.addVertex(v);
                        }
                        ring(wave1, next, last);
                        last = next;
                    }

                    Vertex bubbleCenter = new Vertex(0, 1, 0);
                    bubbleCenter.rotate(0, 0, 90);
                    wave1.vertexBuffer.addVertex(bubbleCenter);
                    for (int k = 0; k < waveCircNum - 1; ++k) {
                        wave1.createTriangle(last.get(k), last.get(k + 1), bubbleCenter);
                    }
                    wave1.createTriangle(last.get(waveCircNum - 1), last.get(0), bubbleCenter);

                    bubbleCenter.scale(waveIntraRadius, waveIntraRadius, waveIntraRadius);
                    bubbleCenter.translate(0, firstWaveRadius, 0);
                    bubbleCenter.rotate(0, 0, angle);


                }
                lastCirc = nextCirc;
            } else {
                if (lastCirc != null) {
                    angle = -(360.0 / num) * (i - 1);


                    double deltaAngle = -90.0 / (circNum + 1);
                    ArrayList<Vertex> last = lastCirc;
                    for (int k = 0; k < circNum; ++k) {
                        double deg = deltaAngle * (k + 1);
                        ArrayList<Vertex> next = new ArrayList<Vertex>();
                        for (int l = 0; l < waveCircNum; ++l) {
                            Vertex v = new Vertex(0, 1, 0);
                            v.rotate(0, 0, deg);
                            v.rotate(-(360.0 / waveCircNum) * l, 0, 0);
                            v.scale(waveIntraRadius, waveIntraRadius, waveIntraRadius);
                            v.translate(0, firstWaveRadius, 0);
                            v.rotate(0, 0, angle);
                            next.add(v);
                            wave1.vertexBuffer.addVertex(v);
                        }
                        ring(wave1, last, next);
                        last = next;
                    }

                    Vertex bubbleCenter = new Vertex(0, 1, 0);
                    bubbleCenter.rotate(0, 0, -90);
                    wave1.vertexBuffer.addVertex(bubbleCenter);
                    for (int k = 0; k < waveCircNum - 1; ++k) {
                        wave1.createTriangle(last.get(k), bubbleCenter, last.get(k + 1));
                    }
                    wave1.createTriangle(last.get(waveCircNum - 1), bubbleCenter, last.get(0));

                    bubbleCenter.scale(waveIntraRadius, waveIntraRadius, waveIntraRadius);
                    bubbleCenter.translate(0, firstWaveRadius, 0);
                    bubbleCenter.rotate(0, 0, angle);

                    lastCirc = null;
                }
            }
        }
        return realWaveAngle;
    }

    public static void createLightMesh() {
        Mesh lightMesh = new Mesh();

        int num = 36;
        int circNum = 1;

        SubMesh base = lightMesh.createSubMesh();
        base.vertexBuffer.ensureCapacity(num * circNum + 2);
        double deltaAngle = 180.0 / (circNum + 2);
        ArrayList<Vertex> last = null;
        for (int i = 0; i < circNum; ++i) {
            double deg = deltaAngle * (i + 1);
            ArrayList<Vertex> next = new ArrayList<Vertex>();
            for (int j = 0; j < num; ++j) {
                double angle = 360.0 / num * j;
                Vertex v = new Vertex(0, 1, 0);
                v.rotate(deg, 0, 0);
                v.rotate(0, angle, 0);
                next.add(v);
                base.vertexBuffer.addVertex(v);
            }
            if (last == null) {

        Vertex bubbleCenter = new Vertex(0, 1, 0);
        base.vertexBuffer.addVertex(bubbleCenter);
        for (int k = 0; k < num - 1; ++k) {
            base.createTriangle(next.get(k), bubbleCenter, next.get(k + 1));
        }
        base.createTriangle(next.get(num - 1), bubbleCenter, next.get(0));

            } else {
                ring(base, next, last);
            }
            last = next;
        }
        Vertex bubbleCenter = new Vertex(0, 1, 0);
        bubbleCenter.rotate(0, 0, 180);
        base.vertexBuffer.addVertex(bubbleCenter);
        for (int k = 0; k < num - 1; ++k) {
            base.createTriangle(bubbleCenter, last.get(k), last.get(k + 1));
        }
        base.createTriangle(bubbleCenter, last.get(num - 1), last.get(0));
//        base.rotate(90, 0, 0);
//        base.removeUnusedVertices();
        base.smoothNormals();
        base.material = "light_debug_marker";


        String meshFile = "./Player/Data/media/light.mesh";
        OgreXML.writMesh(lightMesh).save(meshFile + ".xml");
        CorrectionMesh.convert1dot6(meshFile + ".xml");
    }
}
