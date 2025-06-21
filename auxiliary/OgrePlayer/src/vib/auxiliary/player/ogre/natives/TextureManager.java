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
public class TextureManager extends _Object_ {

    public static TextureManager getSingleton() {
        return new TextureManager(_getSingleton());
    }
    private static native long _getSingleton();

    public TextureManager(long pointer) {
        super(pointer);
    }

    public void remove(String string) {
        _remove(getNativePointer(), string);
    }
    private native void _remove(long p, String s);

    public Texture createRenderTexture(String string, int textureWidth, int textureHeight) {
        return new Texture(_createRenderTexture(getNativePointer(), string, textureWidth, textureHeight));
    }


//        greta.auxiliary.player.ogre.natives.TextureManager.getSingleton().createManual(
//                            texturePtr,
//                            "GretaRenderTexture-"+this.toString(),
//                            greta.auxiliary.player.ogre.natives.ResourceGroupManager.getDEFAULT_RESOURCE_GROUP_NAME(),
//                            greta.auxiliary.player.ogre.natives.TextureType.TEX_TYPE_2D,
//                            textureWidth,
//                            textureHeight,
//                            1,
//                            0,
//                            greta.auxiliary.player.ogre.natives.PixelFormat.PF_BYTE_RGB,
//                            greta.auxiliary.player.ogre.natives.TextureUsage.TU_RENDERTARGET.getValue(),
//                            new greta.auxiliary.player.ogre.natives.ManualResourceLoader(WithoutNativeObject.I_WILL_DELETE_THIS_OBJECT),
//                            false,
//                            4);
    private native long _createRenderTexture(long p, String string, int textureWidth, int textureHeight);

    @Override
    protected native void delete(long nativePointer);
}
