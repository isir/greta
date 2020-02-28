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
public class ByteBuffer extends _Object_{
    private byte[] javaBuffer;
    public ByteBuffer(int size) {
        super(_instanciate(size));
        javaBuffer = new byte[size];
    }

    private static native long _instanciate(int size);

    public byte[] getBuffer(){
        return javaBuffer;
    }

    public void updateJavaBuffer(){
        _updateJavaBuffer(getNativePointer(), javaBuffer, javaBuffer.length);
    }

    private static native void _updateJavaBuffer(long thispointer, byte[] buffer, int size);

    public void setIndex(int index, byte i) {
        _setIndex(getNativePointer(), index, i);
    }
    private native void _setIndex(long thispointer, int index, byte value);

    @Override
    protected native void delete(long nativePointer);
}
