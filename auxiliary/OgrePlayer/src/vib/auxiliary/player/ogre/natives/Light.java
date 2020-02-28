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
public class Light extends _Object_ implements MovableObject {

    public Light(long pointer) {
        super(pointer);
    }

    public void setSpotlightOuterAngle(double d) {
        _setSpotlightOuterAngle(getNativePointer(), d);
    }
    private native void _setSpotlightOuterAngle(long p, double i1);

    public enum LightTypes{
        LT_SPOTLIGHT, LT_DIRECTIONAL
    }

    public void setType(LightTypes lightTypes) {
        switch(lightTypes){
            case LT_DIRECTIONAL : _setType_LT_DIRECTIONAL(getNativePointer()); break;
            case LT_SPOTLIGHT : _setType_LT_SPOTLIGHT(getNativePointer()); break;
        }
    }
    private native void _setType_LT_SPOTLIGHT(long thisPointer);
    private native void _setType_LT_DIRECTIONAL(long thisPointer);

    public void setDirection(double i, double i0, double i1) {
        _setDirection(getNativePointer(), i, i0, i1);
    }
    private native void _setDirection(long p, double i, double i0, double i1);

    public void setCastShadows(boolean b) {
        _setCastShadows(getNativePointer(), b);
    }
    private native void _setCastShadows(long thisPointer, boolean b);

    public void setDiffuseColour(ColourValue colour) {
        _setDiffuseColour(getNativePointer(), colour.getNativePointer());
    }
    private native void _setDiffuseColour(long p, long p2);

    public void setSpecularColour(ColourValue colour) {
        _setSpecularColour(getNativePointer(), colour.getNativePointer());
    }
    private native void _setSpecularColour(long p, long p2);

    @Override
    public void detatchFromParent() {
        _detatchFromParent(getNativePointer());
    }
    private native void _detatchFromParent(long thisPointer);

    @Override
    protected native void delete(long nativePointer);
}
