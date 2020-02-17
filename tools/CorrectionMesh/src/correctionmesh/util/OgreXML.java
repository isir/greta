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

import greta.core.util.environment.Node;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Andre-Marie
 */
public class OgreXML {
    //TODO pose support

    public static Bone readSkeleton(XMLTree ogreXML) {
        XMLTree bonesXML = ogreXML.findNodeCalled("bones");
        HashMap<String, Bone> bones = new HashMap<String, Bone>();
        for (XMLTree boneXML : bonesXML.getChildrenElement()) {
            if (boneXML.isNamed("bone")) {
                Bone bone = readBone(boneXML);
                bones.put(bone.getIdentifier(), bone);
            }
        }
        if (bones.isEmpty()) {
            return null;
        }
        XMLTree bonehierarchyXML = ogreXML.findNodeCalled("bonehierarchy");
        for (XMLTree boneparentXML : bonehierarchyXML.getChildrenElement()) {
            if (boneparentXML.isNamed("boneparent")) {
                String targetBoneName = boneparentXML.getAttribute("bone");
                String parentBoneName = boneparentXML.getAttribute("parent");
                Bone targetBone = bones.get(targetBoneName);
                Bone parentBone = bones.get(parentBoneName);
                parentBone.appendChild(targetBone);
            }
        }
        return (Bone) bones.entrySet().iterator().next().getValue().getRoot();
    }

    public static XMLTree writeSkeleton(Bone bone) {
        XMLTree skeletonXML = XML.createTree("skeleton");
        XMLTree bonesXML = skeletonXML.createChild("bones");
        XMLTree bonehierarchyXML = skeletonXML.createChild("bonehierarchy");
        writeBoneRecurs(bone, bonesXML, bonehierarchyXML, bone);
        return skeletonXML;
    }

    private static void writeBoneRecurs(Bone bone, XMLTree bonesXML, XMLTree bonehierarchyXML, Bone root) {
        XMLTree boneXML = bonesXML.createChild("bone");
        boneXML.setAttribute("id", Integer.toString(bone.ogreID));
        boneXML.setAttribute("name", bone.getIdentifier());
        writePosition(boneXML, bone.getCoordinates());
        writeRotation(boneXML, bone.getOrientation());
        writeScale(boneXML, bone.getScale());
        if (bone != root && bone.getParent() != null) {
            XMLTree boneparentXML = bonehierarchyXML.createChild("boneparent");
            boneparentXML.setAttribute("bone", bone.getIdentifier());
            boneparentXML.setAttribute("parent", bone.getParent().getIdentifier());
        }

        for (Node child : bone.getChildren()) {
            if (child instanceof Bone) {
                writeBoneRecurs((Bone) child, bonesXML, bonehierarchyXML, root);
            }
        }
    }

    private static Bone readBone(XMLTree boneXML) {
        Bone b = new Bone();
        b.ogreID = Integer.parseInt(boneXML.getAttribute("id"));
        b.setIdentifier(boneXML.getAttribute("name"));
        b.setCoordinates(readPosition(boneXML));
        b.setOrientation(readRotation(boneXML));
        b.setScale(readScale(boneXML));
        return b;
    }

    public static Mesh readMesh(XMLTree ogreXML) {
        Mesh m = new Mesh();
        XMLTree sharedGeometry = ogreXML.findNodeCalled("sharedgeometry");
        if (sharedGeometry != null) {
            m.vertices = readVertexBuffer(sharedGeometry);
        }

        XMLTree submeshes = ogreXML.findNodeCalled("submeshes");
        for (XMLTree submesh : submeshes.getChildrenElement()) {
            if (submesh.getName().equalsIgnoreCase("submesh")) {
                SubMesh sub = new SubMesh();
                m.submeshes.add(sub);
                sub.material = submesh.getAttribute("material");
                if (submesh.getAttribute("usesharedvertices").equalsIgnoreCase("true")) {
                    sub.vertexBuffer = m.vertices;
                } else {
                    sub.vertexBuffer = readVertexBuffer(submesh.findNodeCalled("geometry"));
                }

                XMLTree faces = submesh.findNodeCalled("faces");
                sub.faces = new ArrayList<Triangle>((int) faces.getAttributeNumber("count"));
                for (XMLTree face : faces.getChildrenElement()) {
                    if (face.getName().equalsIgnoreCase("face")) {
                        int v1 = (int) face.getAttributeNumber("v1");
                        int v2 = (int) face.getAttributeNumber("v2");
                        int v3 = (int) face.getAttributeNumber("v3");
                        sub.faces.add(
                                new Triangle(
                                        sub.vertexBuffer.get(v1),
                                        sub.vertexBuffer.get(v2),
                                        sub.vertexBuffer.get(v3)));
                    }
                }

                XMLTree boneassignments = submesh.findNodeCalled("boneassignments");
                if (boneassignments != null) {
                    for (XMLTree vertexboneassignment : boneassignments.getChildrenElement()) {
                        if (vertexboneassignment.getName().equalsIgnoreCase("vertexboneassignment")) {
                            int vertexindex = (int) vertexboneassignment.getAttributeNumber("vertexindex");
                            String boneindex = vertexboneassignment.getAttribute("boneindex");
                            double weight = vertexboneassignment.getAttributeNumber("weight");
                            Vertex v = sub.vertexBuffer.get(vertexindex);
                            v.add(new BoneAssignment(boneindex, weight));
                        }
                    }
                }
            }
        }

        XMLTree skeletonLink = ogreXML.findNodeCalled("skeletonlink");
        if (skeletonLink != null) {
            m.skeleton = skeletonLink.getAttribute("name");
        }

        XMLTree submeshNames = ogreXML.findNodeCalled("submeshnames");
        if (submeshNames != null) {
            for (XMLTree submeshname : submeshNames.getChildrenElement()) {
                if (submeshname.getName().equalsIgnoreCase("submeshname")) {
                    m.submeshes.get((int) submeshname.getAttributeNumber("index")).name = submeshname.getAttribute("name");
                }
            }
        }

        XMLTree poses = ogreXML.findNodeCalled("poses");
        if (poses != null) {
            for (XMLTree pose : poses.getChildrenElement()) {
                if (pose.isNamed("pose")) {
                    String posename = pose.getAttribute("name");
                    VertexBuffer target = m.submeshes.get(Integer.parseInt(pose.getAttribute("index"))).vertexBuffer;
                    for (XMLTree poseassignment : pose.getChildrenElement()) {
                        if (poseassignment.isNamed("poseoffset")) {
                            int vertexIndex = Integer.parseInt(poseassignment.getAttribute("index"));
                            double x = poseassignment.getAttributeNumber("x");
                            double y = poseassignment.getAttributeNumber("y");
                            double z = poseassignment.getAttributeNumber("z");
                            Vertex v = target.get(vertexIndex);
                            v.add(new PoseAssignment(posename, x, y, z));
                        }
                    }
                }
            }
        }

        return m;
    }

    private static VertexBuffer readVertexBuffer(XMLTree geometry) {
        int vertexCount = (int) geometry.getAttributeNumber("vertexcount");
        VertexBuffer vertices = new VertexBuffer(vertexCount);

        List<XMLTree> verticesPositions = null;
        List<XMLTree> verticesNormals = null;
        List<XMLTree> verticesMapping = null;
        for (XMLTree vertexbuffer : geometry.getChildrenElement()) {
            if (vertexbuffer.getName().equalsIgnoreCase("vertexbuffer")) {
                if (vertexbuffer.getAttribute("positions").equalsIgnoreCase("true")) {
                    verticesPositions = vertexbuffer.getChildrenElement();
                }
                if (vertexbuffer.getAttribute("normals").equalsIgnoreCase("true")) {
                    verticesNormals = vertexbuffer.getChildrenElement();
                }
                if (vertexbuffer.getAttribute("texture_coord_dimensions_0").equalsIgnoreCase("2")) {
                    verticesMapping = vertexbuffer.getChildrenElement();
                }
            }
        }

        for (int i = 0; i < vertexCount; ++i) {
            Vertex v = new Vertex();
            if (verticesPositions != null) {
                v.position = readPosition(verticesPositions.get(i));
            }
            if (verticesNormals != null) {
                v.normal = readNormal(verticesNormals.get(i));
            }
            if (verticesMapping != null) {
                v.textureCoord = readUV(verticesMapping.get(i));
            }
            vertices.vertices.add(v);
        }

        return vertices;
    }

    private static Vec3d readPosition(XMLTree vertex) {
        XMLTree vect = vertex.findNodeCalled("position");
        return new Vec3d(
                vect.getAttributeNumber("x"),
                vect.getAttributeNumber("y"),
                vect.getAttributeNumber("z"));
    }

    private static Vec3d readScale(XMLTree vertex) {
        XMLTree vect = vertex.findNodeCalled("scale");
        if (vect == null) {
            return new Vec3d(1, 1, 1);
        }
        return new Vec3d(
                vect.getAttributeNumber("x"),
                vect.getAttributeNumber("y"),
                vect.getAttributeNumber("z"));
    }

    private static Quaternion readRotation(XMLTree vertex) {
        XMLTree vect = vertex.findNodeCalled("rotation");

        XMLTree axisXML = vect.findNodeCalled("axis");
        Vec3d axis = new Vec3d(
                axisXML.getAttributeNumber("x"),
                axisXML.getAttributeNumber("y"),
                axisXML.getAttributeNumber("z"));

        double angle = vect.getAttributeNumber("angle");
        Quaternion q = new Quaternion(axis, angle);
        return q;
    }

    private static Vec3d readNormal(XMLTree vertex) {
        XMLTree vect = vertex.findNodeCalled("normal");
        return new Vec3d(
                vect.getAttributeNumber("x"),
                vect.getAttributeNumber("y"),
                vect.getAttributeNumber("z"));
    }

    private static Vec3d readUV(XMLTree vertex) {
        XMLTree vect = vertex.findNodeCalled("texcoord");
        return new Vec3d(
                vect.getAttributeNumber("u"),
                vect.getAttributeNumber("v"),
                0);
    }

    private static void writeVertexBuffer(VertexBuffer vertices, XMLTree parentOfGeometry, boolean shared) {
        if (vertices == null || vertices.vertices.isEmpty()) {
            return;
        }
        XMLTree geometry = parentOfGeometry.createChild(shared ? "sharedgeometry" : "geometry");
        geometry.setAttribute("vertexcount", "" + vertices.vertices.size());

        Vertex v0 = vertices.vertices.get(0);
        XMLTree vertexBufferPosition = geometry.createChild("vertexbuffer");
        XMLTree vertexBufferTexture = null;

        boolean doPositions = v0.position != null;
        boolean doNormals = v0.normal != null;
        boolean doTextureCoords = v0.textureCoord != null;

        if (doPositions) {
            vertexBufferPosition.setAttribute("positions", "true");
        }
        if (doNormals) {
            vertexBufferPosition.setAttribute("normals", "true");
        }
        if (doTextureCoords) {
            vertexBufferTexture = geometry.createChild("vertexbuffer");
            vertexBufferTexture.setAttribute("texture_coord_dimensions_0", "2");
            vertexBufferTexture.setAttribute("texture_coords", "1");
        }

        for (Vertex v : vertices.vertices) {
            XMLTree vertex = vertexBufferPosition.createChild("vertex");
            if (doPositions) {
                writePosition(vertex, v.position);
            }
            if (doNormals) {
                writeNormal(vertex, v.normal);
            }
            if (doTextureCoords) {
                writeUV(vertexBufferTexture.createChild("vertex"), v.textureCoord);
            }
        }
    }

    private static void writePosition(XMLTree vertex, Vec3d position) {
        XMLTree pos = vertex.createChild("position");
        pos.setAttribute("x", "" + position.x());
        pos.setAttribute("y", "" + position.y());
        pos.setAttribute("z", "" + position.z());
    }

    private static void writeRotation(XMLTree vertex, Quaternion rotation) {
        XMLTree rot = vertex.createChild("rotation");
        rot.setAttribute("angle", "" + rotation.angle());
        XMLTree axisXML = rot.createChild("axis");
        Vec3d axis = rotation.axis();
        axisXML.setAttribute("x", "" + axis.x());
        axisXML.setAttribute("y", "" + axis.y());
        axisXML.setAttribute("z", "" + axis.z());
    }

    private static void writeScale(XMLTree vertex, Vec3d scale) {
        if (scale.x() != 1 || scale.y() != 1 || scale.z() != 1) {
            XMLTree pos = vertex.createChild("scale");
            pos.setAttribute("x", "" + scale.x());
            pos.setAttribute("y", "" + scale.y());
            pos.setAttribute("z", "" + scale.z());
        }
    }

    private static void writeNormal(XMLTree vertex, Vec3d normal) {
        normal.normalize();
        XMLTree norm = vertex.createChild("normal");
        norm.setAttribute("x", "" + normal.x());
        norm.setAttribute("y", "" + normal.y());
        norm.setAttribute("z", "" + normal.z());
    }

    private static void writeUV(XMLTree vertex, Vec3d textureCoord) {
        XMLTree uv = vertex.createChild("texcoord");
        uv.setAttribute("u", "" + textureCoord.x());
        uv.setAttribute("v", "" + textureCoord.y());
    }

    private static void writePosesFromSubMesh(XMLTree poses, SubMesh sm, int smIndex) {
        HashMap<String, XMLTree> existing = new HashMap<String, XMLTree>();
        for (Vertex v : sm.vertexBuffer) {
            if (v.poseAssignments != null) {
                for (PoseAssignment assignment : v.poseAssignments) {
                    if (assignment.offset.x() != 0 || assignment.offset.y() != 0 || assignment.offset.z() != 0) {
                        XMLTree pose = existing.get(assignment.poseName);
                        if (pose == null) {
                            pose = poses.createChild("pose");
                            pose.setAttribute("target", "submesh");
                            pose.setAttribute("index", "" + smIndex);
                            pose.setAttribute("name", assignment.poseName);
                            existing.put(assignment.poseName, pose);
                        }
                        XMLTree poseoffset = pose.createChild("poseoffset");
                        poseoffset.setAttribute("index", "" + v.index);
                        poseoffset.setAttribute("x", "" + assignment.offset.x());
                        poseoffset.setAttribute("y", "" + assignment.offset.y());
                        poseoffset.setAttribute("z", "" + assignment.offset.z());
                    }
                }
            }
        }
    }

    public static XMLTree writMesh(Mesh m) {
        XMLTree ogreXML = XML.createTree("mesh");
        writeVertexBuffer(m.vertices, ogreXML, true);

        XMLTree submeshes = ogreXML.createChild("submeshes");

        for (SubMesh sub : m.submeshes) {
            XMLTree submesh = submeshes.createChild("submesh");

            submesh.setAttribute("material", sub.material);
            submesh.setAttribute("use32bitindexes", "false");
            submesh.setAttribute("operationtype", "triangle_list");

            if (sub.vertexBuffer == m.vertices) {
                submesh.setAttribute("usesharedvertices", "true");
            } else {
                submesh.setAttribute("usesharedvertices", "false");
                writeVertexBuffer(sub.vertexBuffer, submesh, false);
            }
            sub.vertexBuffer.updateVerticesIndices();

            XMLTree faces = submesh.createChild("faces");
            faces.setAttribute("count", "" + sub.faces.size());
            for (Triangle triangle : sub.faces) {
                XMLTree face = faces.createChild("face");
                face.setAttribute("v1", "" + triangle.v1.index);
                face.setAttribute("v2", "" + triangle.v2.index);
                face.setAttribute("v3", "" + triangle.v3.index);
            }
            XMLTree boneassignments = XML.createTree("boneassignments");
            boolean empty = true;
            //TODO submeshes
            for (Vertex v : sub.vertexBuffer.vertices) {
                if (v.boneAssignments != null) {
                    for (BoneAssignment assignment : v.boneAssignments) {
                        XMLTree vertexboneassignment = boneassignments.createChild("vertexboneassignment");
                        vertexboneassignment.setAttribute("vertexindex", "" + v.index);
                        vertexboneassignment.setAttribute("boneindex", assignment.bone);
                        vertexboneassignment.setAttribute("weight", "" + assignment.weight);
                        empty = false;
                    }
                }
            }
            if (!empty) {
                submesh.addChild(boneassignments);
            }
        }

        XMLTree poses = XML.createTree("poses");
        for (int i = 0; i < m.submeshes.size(); i++) {
            writePosesFromSubMesh(poses, m.getSubMesh(i), i);
        }
        if (!poses.getChildrenElement().isEmpty()) {
            ogreXML.addChild(poses);
        }

        if (m.skeleton != null) {
            XMLTree skeletonLink = ogreXML.createChild("skeletonlink");
            skeletonLink.setAttribute("name", m.skeleton);
        }

        XMLTree submeshNames = ogreXML.createChild("submeshnames");
        for (int i = 0; i < m.submeshes.size(); ++i) {
            SubMesh sub = m.submeshes.get(i);
            if (sub.name == null) {
                sub.name = "submesh_" + (count++);
            }
            XMLTree submeshname = submeshNames.createChild("submeshname");
            submeshname.setAttribute("name", sub.name);
            submeshname.setAttribute("index", "" + i);
        }

        return ogreXML;
    }
    private static int count = 0;

}
