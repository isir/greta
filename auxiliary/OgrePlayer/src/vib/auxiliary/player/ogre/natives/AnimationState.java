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
public class AnimationState extends _Object_{

    public AnimationState(long pointer) {
        super(pointer);
    }

    public void setTimePosition(double i) {
        _setTimePosition(getNativePointer(), i);
    }
    private native void _setTimePosition(long thisPointer, double i);

    public void setEnabled(boolean b) {
        _setEnabled(getNativePointer(), b);
    }
    private native void _setEnabled(long thisPointer, boolean b);

    public void getParent_notifyDirty() {
        _getParent_notifyDirty(getNativePointer());
    }
    private native void _getParent_notifyDirty(long thisPointer);

    @Override
    protected native void delete(long nativePointer);
}
