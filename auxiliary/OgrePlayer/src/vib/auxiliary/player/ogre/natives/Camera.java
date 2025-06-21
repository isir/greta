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
public class Camera extends _Object_ implements MovableObject{

    public Camera(long pointer) {
        super(pointer);
    }

    public Viewport getViewport() {
        return new Viewport(_getViewport(getNativePointer()));
    }
    private native long _getViewport(long thisPointer);

    public void setOrthoWindow(double i, double i0) {
        _setOrthoWindow(getNativePointer(), i, i0);
    }
    private native void _setOrthoWindow(long thisPointer, double i, double i0);

    public void setVisible(boolean b) {
        _setVisible(getNativePointer(), b);
    }
    private native void _setVisible(long thisPointer, boolean b);

    public void setDebugDisplayEnabled(boolean b) {
        _setDebugDisplayEnabled(getNativePointer(), b);
    }
    private native void _setDebugDisplayEnabled(long thisPointer, boolean b);

    public void setNearClipDistance(double f) {
        _setNearClipDistance(getNativePointer(), f);
    }
    private native void _setNearClipDistance(long thisPointer, double f);

    public SceneManager getSceneManager() {
        return new SceneManager(_getSceneManager(getNativePointer()));
    }
    private native long _getSceneManager(long thisPointer);

    public void setFOVy(double toRadians) {
        _setFOVy(getNativePointer(), toRadians);
    }
    private native void _setFOVy(long thisPointer, double toRadians);


    public Quaternion getDerivedOrientation() {
        return new Quaternion(_getDerivedOrientation(getNativePointer()));
    }
    private native long _getDerivedOrientation(long thisPointer);

    public SceneNode getParentSceneNode() {
        return new SceneNode(_getParentSceneNode(getNativePointer()));
    }
    private native long _getParentSceneNode(long thisPointer);

    public void setCastShadows(boolean b) {
        _setCastShadows(getNativePointer(), b);
    }
    private native void _setCastShadows(long thisPointer, boolean b);

    public void setAspectRatio(double f) {
        _setAspectRatio(getNativePointer(), f);
    }
    private native void _setAspectRatio(long thisPointer, double f);

    public void setPolygonMode_PM_SOLID() {
        _setPolygonMode_PM_SOLID(getNativePointer());
    }
    private native void _setPolygonMode_PM_SOLID(long thisPointer);

    public void setPolygonMode_PM_WIREFRAME() {
        _setPolygonMode_PM_WIREFRAME(getNativePointer());
    }
    private native void _setPolygonMode_PM_WIREFRAME(long thisPointer);

    public void setPolygonMode_PM_POINTS() {
        _setPolygonMode_PM_POINTS(getNativePointer());
    }
    private native void _setPolygonMode_PM_POINTS(long thisPointer);

    public String getName() {
        return _getName(getNativePointer());
    }
    private native String _getName(long thisPointer);


    public Vector3 getDerivedPosition() {
        return new Vector3(_getDerivedPosition(getNativePointer()));
    }
    private native long _getDerivedPosition(long thisPointer);

    public Vector3 getDerivedDirection() {
        Vector3 res = new Vector3(_getDerivedDirection(getNativePointer()));
        res.gcMustDeleteThat(true);
        return res;
    }
    private native long _getDerivedDirection(long thisPointer);

    @Override
    public void detatchFromParent() {
        _detatchFromParent(getNativePointer());
    }
    private native void _detatchFromParent(long thisPointer);

    public Ray getCameraToViewportRay(double f, double f0) {
        Ray ray = new Ray(_getCameraToViewportRay(getNativePointer(), f, f0));
        ray.gcMustDeleteThat(true);
        return ray;
    }
    private native long _getCameraToViewportRay(long thisPointer, double f, double f0);

    @Override
    protected native void delete(long nativePointer);

}
