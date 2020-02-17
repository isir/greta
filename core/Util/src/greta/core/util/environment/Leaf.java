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
package greta.core.util.environment;

import greta.core.util.math.Vec3d;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;

/**
 *
 * @author Pierre Philippe
 * @author Andre-Marie Pez
 */
public class Leaf extends Node {

    // 3D size
    private Vec3d size;
    // other informations
    private String reference = null;

    public Leaf() {
        this(null, new Vec3d(1, 1, 1), "");
    }

    public Leaf(String intid, Vec3d s, String ref) {
        if (intid != null) {
            this.identifier = intid;
        }
        size = new Vec3d(s);
        this.reference = ref;
    }

    public void setSize(double x, double y, double z) {
        size.set(x, y, z);
        fireLeafEvent(LeafEvent.MODIF_SIZE);
    }

    public void setSize(Vec3d pos) {
        setSize(pos.x(), pos.y(), pos.z());
    }

    public Vec3d getSize() {
        return size;
    }

    public void setReference(String ref) {
        if (!reference.equals(ref)) {
            reference = ref;
            fireLeafEvent(LeafEvent.MODIF_REFERENCE);
        }
    }

    public String getReference() {
        return reference;
    }

    protected void fireLeafEvent(int eventType) {
        Node root = getRoot();
        if (root instanceof Root) {
            LeafEvent event = new LeafEvent();
            event.leaf = this;
            event.modifType = eventType;
            Environment env = ((Root) root).getEnvironment();
            env.fireLeafEvent(event);
        }
    }

    @Override
    protected String getXMLNodeName() {
        return "leaf";
    }

    @Override
    protected XMLTree asXML(boolean doNonGuest, boolean doGest) {

        XMLTree leaf = XML.createTree(getXMLNodeName());

        leaf.setAttribute("id", identifier);
        leaf.setAttribute("reference", reference);

        XMLTree size = leaf.createChild("size");
        size.setAttribute("x", "" + this.size.x());
        size.setAttribute("y", "" + this.size.y());
        size.setAttribute("z", "" + this.size.z());

        return leaf;
    }
}
