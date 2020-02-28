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
public class Vector3 extends _Object_ {

    public Vector3(long pointer){
        super(pointer);
    }

    public Vector3(double x, double y, double z) {
        super(_instanciate(x, y, z));
        this.gcMustDeleteThat(true);
    }
    private static native long _instanciate(double x, double y, double z);

    public Vector3() {
        this(0, 0, 0);
    }

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

    public float distance(Vector3 position) {
        return _distance(getNativePointer(), position.getNativePointer());
    }
    private native float _distance(long p,long p2);

    public Vector3 operatorMultiplyAndAssign(float resizecube) {
        return new Vector3(_operatorMultiplyAndAssign(getNativePointer(), resizecube));
    }
    private native long _operatorMultiplyAndAssign(long p,float p2);

    @Override
    protected native void delete(long nativePointer);
}
