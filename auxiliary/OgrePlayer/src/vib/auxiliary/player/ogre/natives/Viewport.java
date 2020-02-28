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
public class Viewport extends _Object_{

    public Viewport(long pointer) {
        super(pointer);
    }

    public void setBackgroundColour(ColourValue convertToColor) {
        _setBackgroundColour(getNativePointer(), convertToColor.getNativePointer());
    }
    private native void _setBackgroundColour(long thiPointer, long colourPointer);

    @Override
    protected native void delete(long nativePointer);
}
