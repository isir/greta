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
public class HardwarePixelBuffer extends _Object_ {

    public HardwarePixelBuffer(long pointer) {
        super(pointer);
    }

    public RenderTexture getRenderTarget(int i) {
        return new RenderTexture(_getRenderTarget(getNativePointer(), i));
    }
    private native long _getRenderTarget(long p, int i);

    public void blitToMemory(PixelBox pib) {
        _blitToMemory(getNativePointer(), pib.getNativePointer());
    }
    private native void _blitToMemory(long p, long i);

    @Override
    protected native void delete(long nativePointer);

}
