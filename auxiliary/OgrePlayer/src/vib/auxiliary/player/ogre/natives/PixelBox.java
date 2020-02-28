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
public class PixelBox extends _Object_{

    private ByteBuffer javaBuffer;

    public PixelBox(int captureWidth, int captureHeight) {
        super(0);
        javaBuffer = new ByteBuffer(captureWidth * captureHeight * 3);
        setNativePointer(_instanciate(captureWidth, captureHeight, javaBuffer.getNativePointer()));
        javaBuffer.gcMustDeleteThat(true);
    }

//    new PixelBox(captureWidth, captureHeight, 1, PixelFormat.PF_BYTE_RGB, new VoidPointer(buff.getInstancePointer()));
    private static native long _instanciate(int captureWidth, int captureHeight, long buffp);

    public byte[] getByteBuffer() {
        javaBuffer.updateJavaBuffer();
        return javaBuffer.getBuffer();
    }

    @Override
    protected native void delete(long nativePointer);
}
