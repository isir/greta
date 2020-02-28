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
