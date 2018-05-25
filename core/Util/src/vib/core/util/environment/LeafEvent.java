/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.environment;

/**
 *
 * @author Andre-Marie Pez
 */
public class LeafEvent {
    public static final int MODIF_REFERENCE = 1;
    public static final int MODIF_SIZE = 2;

    public int modifType = 0;
    public Leaf leaf = null;

    public String getIdLeaf(){
        return leaf==null ? null : leaf.getIdentifier();
    }

    public boolean isReferenceChanged(){
        return isModif(modifType, MODIF_REFERENCE);
    }

    public boolean isSizeChanged(){
        return isModif(modifType, MODIF_SIZE);
    }

    private boolean isModif(int modifType, int type){
        return (modifType & type) == type;
    }

}
