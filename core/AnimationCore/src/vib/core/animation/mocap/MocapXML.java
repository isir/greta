package vib.core.animation.mocap;

import java.util.ArrayList;
import vib.core.animation.Frame;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;

/**
 *
 * @author Jing Huang
 */
public class MocapXML {

    public static MotionSequence load(String filename) {
        MotionSequence ms = null;
        XMLParser parser = XML.createParser();
        XMLTree mocap = parser.parseFile(filename);
        if (mocap != null) {
            String id = mocap.getAttribute("id");
            ms = new MotionSequence(id);
            int size = (int) mocap.getAttributeNumber("size");
            ArrayList<Frame> frames = new ArrayList<Frame>();
            for (XMLTree framet : mocap.getChildrenElement()) {
                if (framet.getName().equalsIgnoreCase("frame")) {
                    int idex = (int) framet.getAttributeNumber("index");
                    Frame frame = new Frame();
                    frames.add(frame);
                    for (XMLTree elementf : framet.getChildrenElement()) {
                        if (elementf.getName().equalsIgnoreCase("translation")) {
                            frame.setRootTranslation(new Vec3d((double) elementf.getAttributeNumber("x"), (double) elementf.getAttributeNumber("y"), (double) elementf.getAttributeNumber("z")));
                        } else {
                            String name = elementf.getName();
                            frame.addRotation(name, new Quaternion((double) elementf.getAttributeNumber("x"), (double) elementf.getAttributeNumber("y"), (double) elementf.getAttributeNumber("z"), (double) elementf.getAttributeNumber("w")));
                        }
                    }
                }
            }
            ms.setSequence(frames);
        }
        return ms;
    }

    public static void save(MotionSequence ms, String filePath) {
        XMLTree mocap = XML.createTree("mocap");
        mocap.setAttribute("id", ms.getName());
        mocap.setAttribute("size", String.valueOf(ms.getFrameNb()));
        ArrayList<Frame> frames = ms.getSequence();
        for (int i = 0; i < frames.size(); ++i) {
            Frame f = frames.get(i);
            XMLTree frame = mocap.createChild("frame");
            frame.setAttribute("index", String.valueOf(i));
            Vec3d t = f.getRootTranslation();
            XMLTree tr = frame.createChild("translation");
            tr.setAttribute("x", String.valueOf(t.x()));
            tr.setAttribute("y", String.valueOf(t.y()));
            tr.setAttribute("z", String.valueOf(t.z()));
            for (String name : f.getRotations().keySet()) {
                if(name.isEmpty()) continue;
                Quaternion q = f.getRotations().get(name);
                XMLTree r = frame.createChild(name);
                r.setAttribute("x", String.valueOf(q.x()));
                r.setAttribute("y", String.valueOf(q.y()));
                r.setAttribute("z", String.valueOf(q.z()));
                r.setAttribute("w", String.valueOf(q.w()));
            }
        }
        mocap.save(filePath);
    }
}
