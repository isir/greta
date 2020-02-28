/*
 * This file is part of the auxiliaries of Greta.
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
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class MeshManager {

    public static Mesh load(String meshFileName, String resourceGroup) {
        return new Mesh(_load(meshFileName, resourceGroup));
    }

//        MeshManager.getSingleton().load(meshFileName, resourceGroup, Usage.HBU_STATIC_WRITE_ONLY, Usage.HBU_STATIC_WRITE_ONLY, true, true);
    private static native long _load(String meshFileName, String resourceGroup);

    public static native float getBoundsPaddingFactor();

}
