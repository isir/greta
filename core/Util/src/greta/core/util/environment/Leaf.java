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
import java.util.List;

/**
 *
 * @author Pierre Philippe
 * @author Andre-Marie Pez
 * @author Brice Donval
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
        this.size = new Vec3d(s);
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

    public Leaf createMetadataLeaf(String metadataShortName, String metadataValue) {

        MetadataLeaf metadataLeaf = null;

        if (!(this instanceof MetadataLeaf)) {
            metadataLeaf = (MetadataLeaf) getMetadataLeaf(metadataShortName);
            if (metadataLeaf != null) {
                metadataLeaf.setMetadataValue(metadataValue);
            } else {
                metadataLeaf = new MetadataLeaf(this, metadataShortName, metadataValue);
            }
        }

        return metadataLeaf;
    }

    public Leaf getMetadataLeaf(String metadataShortName) {

        Leaf metadataLeaf = null;

        List<Node> siblings = getParent().getChildren();
        for (Node sibling : siblings) {
            if (sibling instanceof MetadataLeaf) {
                String siblingReference = ((Leaf) sibling).getReference();
                if (siblingReference.equals(getReference() + ".metadata")) {
                    String siblingIdentifier = sibling.getIdentifier();
                    if (siblingIdentifier.startsWith(getIdentifier() + "." + metadataShortName + ":")) {
                        metadataLeaf = (Leaf) sibling;
                        break;
                    }
                }
            }
        }

        return metadataLeaf;
    }

    public String getMetadataLeafValue(String metadataShortName) {

        String metadataValue = null;

        MetadataLeaf metadataLeaf = (MetadataLeaf) getMetadataLeaf(metadataShortName);
        if (metadataLeaf != null) {
            metadataValue = metadataLeaf.getMetadataValue();
        }

        return metadataValue;
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

    private class MetadataLeaf extends Leaf {

        private MetadataLeaf(Leaf associatedLeaf, String metadataShortName, String metadataValue) {

            String metadataLongName = associatedLeaf.getIdentifier() + "." + metadataShortName;

            setIdentifier(metadataLongName + ":" + metadataValue);
            setReference(associatedLeaf.getReference() + ".metadata");
            setSize(0, 0, 0);

            associatedLeaf.getParent().addChildNode(this);
        }

        private String getMetadataLongName() {
            String metadataLongName = getIdentifier().substring(0, getIdentifier().lastIndexOf(":"));
            return metadataLongName;
        }

        private String getMetadataShortName() {
            String metadataLongName = getMetadataLongName();
            String metadataShortName = metadataLongName.substring(metadataLongName.lastIndexOf(".") + 1);
            return metadataShortName;
        }

        private String getMetadataValue() {
            String metadataValue = getIdentifier().substring(getIdentifier().lastIndexOf(":") + 1);
            return metadataValue;
        }

        private void setMetadataValue(String metadataValue) {
            String metadataLongName = getMetadataLongName();
            setIdentifier(metadataLongName + ":" + metadataValue);
        }

    }
}
