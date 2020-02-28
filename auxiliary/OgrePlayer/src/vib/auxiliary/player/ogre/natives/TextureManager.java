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
