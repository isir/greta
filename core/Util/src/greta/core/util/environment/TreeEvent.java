/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
