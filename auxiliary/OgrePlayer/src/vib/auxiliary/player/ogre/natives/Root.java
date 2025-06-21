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
