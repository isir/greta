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
