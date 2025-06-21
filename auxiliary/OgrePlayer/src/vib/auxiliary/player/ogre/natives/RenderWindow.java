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
public class RenderWindow extends _Object_ implements RenderTarget{

    public RenderWindow(long pointer) {
        super(pointer);
    }

    public void resize(long l, long l0) {
        _resize(getNativePointer(), l, l0);
    }
    private native void _resize(long p, long l, long l0);

    public void windowMovedOrResized() {
        _windowMovedOrResized(getNativePointer());
    }
    private native void _windowMovedOrResized(long p);

    public void addViewport(Camera ogreCamera, int i, double i0, double i1, double i2, double i3) {
        _addViewport(getNativePointer(), ogreCamera.getNativePointer(), i, i0, i1, i2, i3);
    }
    public void addViewport(Camera ogreCamera, int i, double i0, double i1, double i2) {
        _addViewport(getNativePointer(), ogreCamera.getNativePointer(), i, i0, i1, i2, 1);
    }
    public void addViewport(Camera ogreCamera, int i, double i0, double i1) {
        _addViewport(getNativePointer(), ogreCamera.getNativePointer(), i, i0, i1, 1,1);
    }
    public void addViewport(Camera ogreCamera, int i, double i0) {
        _addViewport(getNativePointer(), ogreCamera.getNativePointer(), i, i0, 0,1,1);
    }
    public void addViewport(Camera ogreCamera, int i) {
        _addViewport(getNativePointer(), ogreCamera.getNativePointer(), i, 0,0,1,1);
    }
    public void addViewport(Camera ogreCamera) {
        _addViewport(getNativePointer(), ogreCamera.getNativePointer(), 0,0,0,1,1);
    }
    private native void _addViewport(long p, long ogreCamera, int i, double i0, double i1, double i2, double i3);

    public boolean isPrimary() {
        return _isPrimary(getNativePointer());
    }
    private native boolean _isPrimary(long p);

    public void setVisible(boolean b) {
        _setVisible(getNativePointer(), b);
    }
    private native void _setVisible(long p, boolean b);

    public void setAutoUpdated(boolean b) {
        _setAutoUpdated(getNativePointer(), b);
    }
    private native void _setAutoUpdated(long p, boolean b);

    public void setActive(boolean b) {
        _setActive(getNativePointer(), b);
    }
    private native void _setActive(long p, boolean b);

    public void copyContentsToMemory(PixelBox pib) {
        _copyContentsToMemory(getNativePointer(), pib.getNativePointer());
    }
    private native void _copyContentsToMemory(long p, long b);

    @Override
    protected native void delete(long nativePointer);

}
