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
public class Quaternion extends _Object_ {

    public static Quaternion getIDENTITY() {
        return new Quaternion(_getIDENTITY());
    }
    private static native long _getIDENTITY();

    public Quaternion(long pointer){
        super(pointer);
    }

    public Quaternion(double w, double x, double y, double z) {
        super(_instanciate(w, x, y, z));
        this.gcMustDeleteThat(true);
    }
    private static native long _instanciate(double w, double x, double y, double z);

    public Quaternion(double angle, Vector3 axe) {
        super(_instanciate(angle, axe.getNativePointer()));
        this.gcMustDeleteThat(true);
    }
    private static native long _instanciate(double angle, long vector3Pointer);


    public double getw() {
        return _getw(getNativePointer());
    }
    private native double _getw(long p);

    public double getx() {
        return _getx(getNativePointer());
    }
    private native double _getx(long p);

    public double gety() {
        return _gety(getNativePointer());
    }
    private native double _gety(long p);

    public double getz() {
        return _getz(getNativePointer());
    }
    private native double _getz(long p);

    public double getPitch(boolean b) {
        return _getPitch(getNativePointer());
    }
    private native double _getPitch(long p);

    public double getYaw(boolean b) {
        return _getYaw(getNativePointer());
    }
    private native double _getYaw(long p);

    public double getRoll(boolean b) {
        return _getRoll(getNativePointer());
    }
    private native double _getRoll(long p);

    public Quaternion Inverse() {
        Quaternion inverse = new Quaternion(_Inverse(getNativePointer()));
        inverse.gcMustDeleteThat(true);
        return inverse;
    }
    private native long _Inverse(long p);

    @Override
    protected native void delete(long nativePointer);

}
