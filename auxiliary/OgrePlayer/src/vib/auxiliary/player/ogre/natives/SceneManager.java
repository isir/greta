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
public class SceneManager extends _Object_ {

    public SceneManager(long pointer) {
        super(pointer);
    }

    public enum PrefabType {
        PT_SPHERE, PT_CUBE
    }

    public Entity createEntity(String id, PrefabType prefabType) {
        switch(prefabType){
            case PT_CUBE : return new Entity(_createEntity_PT_CUBE(getNativePointer(), id));
            case PT_SPHERE : return new Entity(_createEntity_PT_SPHERE(getNativePointer(), id));
        }
        return null;
    }
    private native long _createEntity_PT_SPHERE(long p, String id);
    private native long _createEntity_PT_CUBE(long p, String id);

    public void destroyMovableObject(MovableObject object) {
        _destroyMovableObject(getNativePointer(), object.getNativePointer());
    }
     private native void _destroyMovableObject(long p, long p2);

    public void setFog_FOG_EXP(ColourValue convertToColor, double f, double i, double i0) {
        _setFog_FOG_EXP(getNativePointer(), convertToColor.getNativePointer(), f, i, i0);
    }
    private native void _setFog_FOG_EXP(long p, long p2, double f, double i, double i0);

    public RaySceneQuery createRayQuery(Ray ray, int i) {
        return new RaySceneQuery(_createRayQuery(getNativePointer(), ray.getNativePointer(), i));
    }
    public RaySceneQuery createRayQuery(Ray ray) {
        return createRayQuery(ray, 0xFFFFFFFF);
    }
    private native long _createRayQuery(long p, long p2, int i);

    public SceneNode getRootSceneNode() {
        return new SceneNode(_getRootSceneNode(getNativePointer()));
    }
    private native long _getRootSceneNode(long p);

    public Camera createCamera(String id) {
        return new Camera(_createCamera(getNativePointer(), id));
    }
    private native long _createCamera(long p, String id);

    public void showBoundingBoxes(boolean b) {
        _showBoundingBoxes(getNativePointer(), b);
    }
    private native void _showBoundingBoxes(long p, boolean b);

    public Entity createEntity(String id, String meshName) {
        return new Entity(_createEntity(getNativePointer(), id, meshName));
    }
    private native long _createEntity(long p, String id, String name);

    public boolean hasSceneNode(String id) {
        return _hasSceneNode(getNativePointer(), id);
    }
    private native boolean _hasSceneNode(long p, String id);

    public SceneNode getSceneNode(String id) {
        return new SceneNode(_getSceneNode(getNativePointer(), id));
    }
    private native long _getSceneNode(long p, String id);

    public void setAmbientLight(ColourValue convertToColor) {
        _setAmbientLight(getNativePointer(), convertToColor.getNativePointer());
    }
    private native void _setAmbientLight(long p, long p2);

    public boolean hasEntity(String id) {
        return _hasEntity(getNativePointer(), id);
    }
    private native boolean _hasEntity(long p, String id);

    public Entity getEntity(String id) {
        return new Entity(_getEntity(getNativePointer(), id));
    }
    private native long _getEntity(long p, String id);

    public Light createLight(String id) {
        return new Light(_createLight(getNativePointer(), id));
    }
    private native long _createLight(long p, String id);

    public boolean hasLight(String id) {
        return _hasLight(getNativePointer(), id);
    }
    private native boolean _hasLight(long p, String id);

    public Light getLight(String id) {
        return new Light(_getLight(getNativePointer(), id));
    }
    private native long _getLight(long p, String id);

    @Override
    protected native void delete(long nativePointer);
}
