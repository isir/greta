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
public class RenderTexture extends _Object_ {

    public RenderTexture(long pointer) {
        super(pointer);
    }

    public void setActive(boolean b) {
        _setActive(getNativePointer(), b);
    }
    private native void _setActive(long p, boolean b);

    public void setAutoUpdated(boolean b) {
        _setAutoUpdated(getNativePointer(), b);
    }
    private native void _setAutoUpdated(long p, boolean b);

    public void removeAllViewports() {
        _removeAllViewports(getNativePointer());
    }
    private native void _removeAllViewports(long p);

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

    @Override
    protected native void delete(long nativePointer);

}
