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
