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
