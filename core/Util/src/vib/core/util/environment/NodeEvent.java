/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.environment;

/**
 *
 * @author Pierre Philippe
 * @author Andre-Marie Pez
 */
public class NodeEvent {

    public static final int MODIF_NONE     = 0x000;
    public static final int MODIF_POSITION = 0x001;
    public static final int MODIF_ROTATION = 0x010;
    public static final int MODIF_SCALE    = 0x100;

    public int modifType;
    public TreeNode node;


    public String getIdNode(){
        return node==null ? null : node.getIdentifier();
    }

    public boolean isPositionChanged(){
        return isModif(modifType, MODIF_POSITION);
    }

    public boolean isRotationChanged(){
        return isModif(modifType, MODIF_ROTATION);
    }

    public boolean isScaleChanged(){
        return isModif(modifType, MODIF_SCALE);
    }

    private boolean isModif(int modifType, int type){
        return (modifType & type) == type;
    }

}
