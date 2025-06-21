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
public class SkeletonInstance extends _Object_ {

    public SkeletonInstance(long pointer) {
        super(pointer);
    }

    public boolean hasBone(String leftEye) {
        return _hasBone(getNativePointer(), leftEye);
    }
    private native boolean _hasBone(long p, String name);

    public Bone getBone(String leftEye) {
        return new Bone(_getBone(getNativePointer(), leftEye));
    }
    private native long _getBone(long p, String name);

    public Bone getRootBone() {
        return new Bone(_getRootBone(getNativePointer()));
    }
    private native long _getRootBone(long p);

    public int getNumBones() {
        return _getNumBones(getNativePointer());
    }
    private native int _getNumBones(long p);

    public Bone getBone(int i) {
        return new Bone(_getBone(getNativePointer(), i));
    }
    private native long _getBone(long p, int index);

    @Override
    protected native void delete(long nativePointer);
}
