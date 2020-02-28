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
package greta.core.animation.mocap;

import greta.core.animation.Frame;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class MocapXML {

    public static MotionSequence load(String fileName) {
        MotionSequence ms = null;
        XMLParser parser = XML.createParser();
        XMLTree mocap = parser.parseFile(fileName);
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
