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
