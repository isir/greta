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
