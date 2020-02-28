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
public class Root extends _Object_ {

    public Root(String pluginsFile, String string, String logOgrelog) {
        super(_instanciate(pluginsFile, string, logOgrelog));
    }
    private static native long _instanciate(String pluginsFile, String string, String logOgrelog);

    public void renderOneFrame() {
        _renderOneFrame(getNativePointer());
    }
    private native void _renderOneFrame(long p);

    public SceneManager getSceneManager(String sceneName) {
        return new SceneManager(_getSceneManager(getNativePointer(), sceneName));
    }
    private native long _getSceneManager(long p, String name);

    public void setRenderSystem(String DirectX9) {
        _setRenderSystem(getNativePointer(), DirectX9);
    }
//        root.setRenderSystem(root.getRenderSystemByName(name));
    private native void _setRenderSystem(long p, String name);


    public void shutdown() {
        _shutdown(getNativePointer());
    }
    private native void _shutdown(long p);

    public SceneManager createSceneManager(String terrainSceneManager, String sceneName) {
        return new SceneManager(_createSceneManager(getNativePointer(), terrainSceneManager, sceneName));
    }
    private native long _createSceneManager(long p, String type, String name);

    public void _fireFrameStarted() {
        __fireFrameStarted(getNativePointer());
    }
    private native void __fireFrameStarted(long p);

    public void _fireFrameEnded() {
        __fireFrameEnded(getNativePointer());
    }
    private native void __fireFrameEnded(long p);

    public void detachRenderTarget(RenderWindow renderWindow) {
        _detachRenderTarget(getNativePointer(), renderWindow.getNativePointer());
    }
    private native void _detachRenderTarget(long p, long p2);

    public void initialise(boolean b, String string, String string0) {
        _initialise(getNativePointer(), b, string, string0);
    }
    private native void _initialise(long p, boolean b, String string, String string0);

    public RenderWindow createRenderWindow(String string, int width, int height, boolean b, NameValuePairList params) {
        return new RenderWindow(_createRenderWindow(getNativePointer(), string, width, height, b, params.getNativePointer()));
    }
    private native long _createRenderWindow(long p, String string, int width, int height, boolean b, long params);

    @Override
    protected native void delete(long nativePointer);
}
