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
