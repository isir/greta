/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.environment;

import vib.core.util.math.Vec3d;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLTree;

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
