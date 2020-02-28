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
public class RaySceneQuery extends _Object_ {

    public RaySceneQuery(long pointer) {
        super(pointer);
    }

    public void setSortByDistance(boolean b, int i) {
        _setSortByDistance(getNativePointer(), b, i);
    }
    private native void _setSortByDistance(long p, boolean b, int i);

    public RaySceneQueryResult execute() {
        return new RaySceneQueryResult(_execute(getNativePointer()));
    }
    private native long _execute(long p);

    @Override
    protected native void delete(long nativePointer);

}
