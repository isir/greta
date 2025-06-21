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
public class _Node extends _Object_ implements Node{

    public _Node(long pointer) {
        super(pointer);
    }

    @Override
    public void _update(boolean updateChildren, boolean parentHasChanged) {
        __update(getNativePointer(), updateChildren, parentHasChanged);
    }
    private native void __update(long thisPointer, boolean updateChildren, boolean parentHasChanged);

    @Override
    public Quaternion _getDerivedOrientation() {
        return new Quaternion(__getDerivedOrientation(getNativePointer()));
    }
    private native long __getDerivedOrientation(long thisPointer);

    @Override
    protected native void delete(long nativePointer);
}
