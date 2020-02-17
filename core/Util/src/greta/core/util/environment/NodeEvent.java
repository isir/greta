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
