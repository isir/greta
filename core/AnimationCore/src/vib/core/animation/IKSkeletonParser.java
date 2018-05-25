/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import vib.core.util.math.Vec3d;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;

/**
 *
 * @author Jing Huang
 */
public class IKSkeletonParser {

    String _filepath = "";
    XMLTree _tree;
    HashMap<String, Integer> _xmljointNameMap = new HashMap<String, Integer>();
    ArrayList<String> _builtJointsNames = new ArrayList<String>();

    public boolean loadFile(String filepath) {

        _filepath = filepath;

        XMLParser xmlparser = XML.createParser();
        xmlparser.setValidating(false);
        _tree = xmlparser.parseFile(filepath);
        return _tree != null;
    }

    public boolean readSkeletonInfo(Skeleton skeleton) {
        XMLTree rootNode = _tree.getRootNode();
        List<XMLTree> list = rootNode.getChildrenElement();
        skeleton.clearAll();
        for (int i = 0; i < list.size(); i++) {
            XMLTree node = list.get(i);
            if (node.getName().equals("bones")) {
                List<XMLTree> listBones = node.getChildrenElement();
                for (int j = 0; j < listBones.size(); j++) {
                    XMLTree nodebone = listBones.get(j);
                    if (nodebone.getName().equals("bone")) {
                        readJoint(nodebone, skeleton);
                    }
                }
            }



            if (node.getName().equals("bonehierarchy")) {
                List<XMLTree> listBones = node.getChildrenElement();
                for (int j = 0; j < listBones.size(); j++) {
                    XMLTree nodebone = listBones.get(j);
                    if (nodebone.getName().equals("boneparent")) {
                        readJointHierarchy(nodebone, skeleton);
                    }
                }

            }

        }

        for(Joint j : skeleton.getJoints()){
            j.update();
        }

        return true;
    }

    public void readJoint(XMLTree current, Skeleton skeleton) {

        double id = current.getAttributeNumber("id");
        String name = current.getAttribute("name");
        Joint j = skeleton.createJoint(name, -1);
        List<XMLTree> lists = current.getChildrenElement();
        for (int i = 0; i < lists.size(); ++i) {
            XMLTree node = lists.get(i);
            if (node.getName().equals("position")) {
                double x = node.getAttributeNumber("x");
                double y = node.getAttributeNumber("y");
                double z = node.getAttributeNumber("z");
                j.setLocalPosition(new Vec3d((double) x, (double) y, (double) z));
            }

            if (node.getName().equals("rotation")) {
            }
        }
        
    }

    public void readJointHierarchy(XMLTree current, Skeleton skeleton) {
        String jName = current.getAttribute("bone");
        String parentName = current.getAttribute("parent");
        Joint j = skeleton.getJoint(jName);
        Joint jp = skeleton.getJoint(parentName);
        j.setParent(jp);
    }

    public static void main(String[] args) {

        Skeleton skeleton = new Skeleton("test");
        IKSkeletonParser parser = new IKSkeletonParser();
        if (parser.loadFile("C:\\Users\\Jing\\Desktop\\greta_svn\\java\\bin\\BehaviorRealizer\\Skeleton\\greta_skeleton.xml")) {
            parser.readSkeletonInfo(skeleton);
            System.out.println("finish loading skeleton");
        }
    }
}
