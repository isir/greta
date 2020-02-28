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
package greta.core.animation.common;

import greta.core.animation.common.IK.IKTask;
import greta.core.util.math.Vec3d;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jing Huang
 */
public class IKSkeletonParser {

    class XMLJoint {

        public int id;
        public String name = "";
        public String parentname = "";
        public Vec3d dirVector = new Vec3d();
        public Vec3d min = new Vec3d((double) -3.142, (double) -3.142, (double) -3.142);
        public Vec3d max = new Vec3d((double) 3.142, (double) 3.142, (double) 3.142);
    };
    String _filePath = "";
    XMLTree _tree;
    ArrayList<XMLJoint> _xmljoints = new ArrayList<XMLJoint>();
    HashMap<String, Integer> _xmljointNameMap = new HashMap<String, Integer>();
    ArrayList<String> _builtJointsNames = new ArrayList<String>();

    public boolean loadFile(String filePath) {

        _filePath = filePath;

        XMLParser xmlparser = XML.createParser();
        xmlparser.setValidating(false);
        _tree = xmlparser.parseFile(filePath);
        return _tree != null;
    }

    public boolean readSkeletonInfo() {
        XMLTree rootNode = _tree.getRootNode();
        List<XMLTree> list = rootNode.getChildrenElement();

        for (int i = 0; i < list.size(); i++) {
            XMLTree node = list.get(i);
            if (node.getName().equals("bones")) {
                List<XMLTree> listBones = node.getChildrenElement();
                for (int j = 0; j < listBones.size(); j++) {
                    XMLTree nodebone = listBones.get(j);
                    if (nodebone.getName().equals("bone")) {
                        readJoint(nodebone);
                    }
                }
            }


            if (node.getName().equals("bonehierarchy")) {
                List<XMLTree> listBones = node.getChildrenElement();
                for (int j = 0; j < listBones.size(); j++) {
                    XMLTree nodebone = listBones.get(j);
                    if (nodebone.getName().equals("boneparent")) {
                        readJointHierarchy(nodebone);
                    }
                }

            }

        }

        return true;
    }

    public void readJoint(XMLTree current) {
        XMLJoint xmljoint = new XMLJoint();
        double id = current.getAttributeNumber("id");
        xmljoint.id = (int) id;

        String name = current.getAttribute("name");
        xmljoint.name = name;

        List<XMLTree> lists = current.getChildrenElement();
        for (int i = 0; i < lists.size(); ++i) {
            XMLTree node = lists.get(i);
            if (node.getName().equals("position")) {
                double x = node.getAttributeNumber("x");
                double y = node.getAttributeNumber("y");
                double z = node.getAttributeNumber("z");
                xmljoint.dirVector = new Vec3d((double) x, (double) y, (double) z);
            }

            if (node.getName().equals("rotation")) {
            }
        }
        xmljoint.min = new Vec3d(-3.1415926f);
        xmljoint.max = new Vec3d(3.1415926f);
        _xmljoints.add(xmljoint);
        _xmljointNameMap.put(xmljoint.name, _xmljoints.size() - 1);
        //_xmljoints.put(xmljoint.name, xmljoint);
    }

    public void readJointHierarchy(XMLTree current) {
        String jName = current.getAttribute("bone");
        //System.err.println(jName);
        String parentName = current.getAttribute("parent");
        _xmljoints.get(_xmljointNameMap.get(jName)).parentname = parentName;
    }

    public void buildFullSkeleton(MultiTasksFramework framework) {
        Skeleton skeleton = framework.getSkeleton();
        if (skeleton == null) {
            skeleton = skeleton = new Skeleton("fullSkeleton");
        }
        skeleton.clear();

        for (int i = 0; i < _xmljoints.size(); ++i) {
            XMLJoint xj = _xmljoints.get(i);
            int idxParent = -1;
            if (!xj.parentname.isEmpty()) {
                idxParent = _xmljointNameMap.get(xj.parentname);
            }
            int jointindex = skeleton.createJoint(xj.name, idxParent);
            Vec3d dirVector = xj.dirVector;
            Joint j = skeleton.getJoint(jointindex);
            j.setOriginalDirectionalVector(dirVector);
            //j.updateLocally();
           // System.out.println(xj.name + " " + j.getWorldPosition().x() + " " + j.getWorldPosition().y() + " " + j.getWorldPosition().z());
        }
    }

    public void buildFullSkeleton( Skeleton skeleton) {
        if (skeleton == null) {
            skeleton = skeleton = new Skeleton("fullSkeleton");
        }
        skeleton.clear();

        for (int i = 0; i < _xmljoints.size(); ++i) {
            XMLJoint xj = _xmljoints.get(i);
            int idxParent = -1;
            if (!xj.parentname.isEmpty()) {
                idxParent = _xmljointNameMap.get(xj.parentname);
            }
            int jointindex = skeleton.createJoint(xj.name, idxParent);
            Vec3d dirVector = xj.dirVector;
            Joint j = skeleton.getJoint(jointindex);
            j.setOriginalDirectionalVector(dirVector);
            //j.updateLocally();
            //System.out.println(xj.name + " " + j.getWorldPosition().x() + " " + j.getWorldPosition().y() + " " + j.getWorldPosition().z());
        }
    }

    public void buildSkeletonChains(Skeleton skeleton, HashMap<String, Skeleton> chains) {
        if(skeleton == null) return;
        XMLTree rootNode = _tree.getRootNode();
        List<XMLTree> list = rootNode.getChildrenElement();
        for (int i = 0; i < list.size(); i++) {
            XMLTree node = list.get(i);
            if (node.getName().equals("chain")) {
                String name = node.getAttribute("name");
                //String task = node.getAttribute("task");
                Skeleton skelet = new Skeleton(name);
                chains.put(name, skelet);
                List<XMLTree> jointslist = node.getChildrenElement();
                for (int j = 0; j < jointslist.size(); j++) {
                    XMLTree nodejoint = jointslist.get(j);
                    if (nodejoint.getName().equals("joint")) {
                        String jointname = nodejoint.getAttribute("name");
                        int jointindex = skelet.createJoint(jointname, j - 1);

                        Vec3d dirVector = null;
                        if (j == 0) {
                            Joint jointInSkeleton = skeleton.getJoint(skelet.getJoint(jointindex).getName());
                            dirVector = jointInSkeleton.getWorldPosition();
                        } else {
                            Joint parentInSkeleton = skeleton.getJoint(skelet.getJoint(j - 1).getName());
                            Joint jointInSkeleton = skeleton.getJoint(skelet.getJoint(jointindex).getName());
                            dirVector = Vec3d.substraction(jointInSkeleton.getWorldPosition(), parentInSkeleton.getWorldPosition());
                        }
                        Joint jointcurrent = skelet.getJoint(jointindex);
                        if (nodejoint.hasAttribute("mass")) {
                            double mass = nodejoint.getAttributeNumber("mass");
                            jointcurrent.setMass((double) (mass));
                        }

                        jointcurrent.setOriginalDirectionalVector(dirVector);
                        jointcurrent.updateLocally();

                        XMLTree nodedofs = nodejoint.findNodeCalled("dof");
                        if (nodedofs != null) {
                            List<XMLTree> listDOF = nodedofs.getChildrenElement();
                            double xMinT = 0;
                            double xMaxT = 0;
                            double yMinT = 0;
                            double yMaxT = 0;
                            double zMinT = 0;
                            double zMaxT = 0;
                            for (int jd = 0; jd < listDOF.size(); ++jd) {
                                XMLTree nodeDOF = listDOF.get(jd);

                                if (nodeDOF.getName().equals("x")) {
                                    xMinT = nodeDOF.getAttributeNumber("min");
                                    xMaxT = nodeDOF.getAttributeNumber("max");
                                } else if (nodeDOF.getName().equals("y")) {
                                    yMinT = nodeDOF.getAttributeNumber("min");
                                    yMaxT = nodeDOF.getAttributeNumber("max");
                                } else if (nodeDOF.getName().equals("z")) {
                                    zMinT = nodeDOF.getAttributeNumber("min");
                                    zMaxT = nodeDOF.getAttributeNumber("max");
                                }
                                //System.out.println(name + "   " + xMinT + " "+ xMaxT);
                            }
                            jointcurrent.setDOF(DOF.DOFType.ROTATION_X, (double) xMinT, (double) xMaxT);
                            jointcurrent.setDOF(DOF.DOFType.ROTATION_Y, (double) yMinT, (double) yMaxT);
                            jointcurrent.setDOF(DOF.DOFType.ROTATION_Z, (double) zMinT, (double) zMaxT);
                        }
                    }
                }

            }
        }

    }

    public void buildIntermediaSkeletonIKTasks(MultiTasksFramework framework) {
        Skeleton skeleton = framework.getSkeleton();
        XMLTree rootNode = _tree.getRootNode();
        List<XMLTree> list = rootNode.getChildrenElement();
        for (int i = 0; i < list.size(); i++) {
            XMLTree node = list.get(i);
            if (node.getName().equals("group")) {
                String name = node.getAttribute("name");
                String task = node.getAttribute("task");
                Skeleton skelet = new Skeleton(name);

                List<XMLTree> jointslist = node.getChildrenElement();
                for (int j = 0; j < jointslist.size(); j++) {
                    XMLTree nodejoint = jointslist.get(j);
                    if (nodejoint.getName().equals("joint")) {
                        String jointname = nodejoint.getAttribute("name");


                        int jointindex = skelet.createJoint(jointname, j - 1);

                        Vec3d dirVector = null;
                        if (j == 0) {
                            Joint jointInSkeleton = skeleton.getJoint(skelet.getJoint(jointindex).getName());
                            dirVector = jointInSkeleton.getWorldPosition();
                        } else {
                            Joint parentInSkeleton = skeleton.getJoint(skelet.getJoint(j - 1).getName());
                            Joint jointInSkeleton = skeleton.getJoint(skelet.getJoint(jointindex).getName());
                            dirVector = Vec3d.substraction(jointInSkeleton.getWorldPosition(), parentInSkeleton.getWorldPosition());
                        }
                        Joint jointcurrent = skelet.getJoint(jointindex);
                        if (nodejoint.hasAttribute("mass")) {
                            double mass = nodejoint.getAttributeNumber("mass");
                            jointcurrent.setMass((double) (mass));
                        }

                        jointcurrent.setOriginalDirectionalVector(dirVector);
                        jointcurrent.updateLocally();

                        XMLTree nodedofs = nodejoint.findNodeCalled("dof");
                        if (nodedofs != null) {
                            List<XMLTree> listDOF = nodedofs.getChildrenElement();
                            double xMinT = 0;
                            double xMaxT = 0;
                            double yMinT = 0;
                            double yMaxT = 0;
                            double zMinT = 0;
                            double zMaxT = 0;
                            for (int jd = 0; jd < listDOF.size(); ++jd) {
                                XMLTree nodeDOF = listDOF.get(jd);

                                if (nodeDOF.getName().equals("x")) {
                                    xMinT = nodeDOF.getAttributeNumber("min");
                                    xMaxT = nodeDOF.getAttributeNumber("max");
                                } else if (nodeDOF.getName().equals("y")) {
                                    yMinT = nodeDOF.getAttributeNumber("min");
                                    yMaxT = nodeDOF.getAttributeNumber("max");
                                } else if (nodeDOF.getName().equals("z")) {
                                    zMinT = nodeDOF.getAttributeNumber("min");
                                    zMaxT = nodeDOF.getAttributeNumber("max");
                                }
                                //System.out.println(name + "   " + xMinT + " "+ xMaxT);
                            }
                            jointcurrent.setDOF(DOF.DOFType.ROTATION_X, (double) xMinT, (double) xMaxT);
                            jointcurrent.setDOF(DOF.DOFType.ROTATION_Y, (double) yMinT, (double) yMaxT);
                            jointcurrent.setDOF(DOF.DOFType.ROTATION_Z, (double) zMinT, (double) zMaxT);
                        }
                    }
                }

                if (task.equals("rotation")) {
                    RotationTask rotationtask = new RotationTask(name);
                    rotationtask.bindOriginalSkeleton(skeleton);
                    rotationtask.setChain(skelet);
                    if (skelet.getJoints().size() > 0) {
                        framework.addTask(rotationtask);
                    }
                } else {
                    IKTask iktask = new IKTask(name);
                    iktask.bindOriginalSkeleton(skeleton);
                    iktask.setChain(skelet);
                    if (skelet.getJoints().size() > 0) {
                        framework.addTask(iktask);
                    }
                }
            }
        }
    }


}
