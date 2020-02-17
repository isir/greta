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
