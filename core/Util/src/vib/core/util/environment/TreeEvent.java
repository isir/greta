/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.environment;

/**
 *
 * @author Pierre Philippe
 * @author Andre-Marie Pez
 */

public class TreeEvent {

    public static final int MODIF_ADD = 1;
    public static final int MODIF_REMOVE = 2;
    public static final int MODIF_MOVE = 3;

    public int modifType = 0;
    public TreeNode newParentNode = null;
    public TreeNode oldParentNode = null;
    public Node childNode = null;

    public String getIdNewParentNode(){
        return newParentNode==null ? null : newParentNode.getIdentifier();
    }

    public String getIdOldParentNode(){
        return oldParentNode==null ? null : oldParentNode.getIdentifier();
    }

    public String getIdChildNode(){
        return childNode==null ? null : childNode.getIdentifier();
    }

    public boolean isRemove(){
        return isModif(modifType, MODIF_REMOVE); // remove or move (shortcut for remove+add)
    }

    public boolean isAdd(){
        return isModif(modifType, MODIF_ADD); // add or move (shortcut for remove+add)
    }

    public boolean isMove(){
        return modifType == MODIF_MOVE;
    }

    private boolean isModif(int modifType, int type){
        return (modifType & type) == type;
    }
}
