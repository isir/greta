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
abstract class _Object_ {

    private long pointer;
    private boolean gcMustDeleteMe = false;

    _Object_(long pointer){
        this.pointer = pointer;
    }

    public long getNativePointer(){
        return pointer;
    }

    /**
     * Please don't use it as possible
     * @param p
     */
    void setNativePointer(long p){
        pointer = p;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof _Object_){
            return pointer == ((_Object_)o).pointer;
        }
        return false;
    }

    public boolean isNull(){
        return pointer==0;
    }

    protected void gcMustDeleteThat(boolean b){
        gcMustDeleteMe = b;
    }

    protected abstract void delete(long nativePointer);

    public void delete(){
        if(pointer != 0){
            delete(getNativePointer());
            pointer = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if(gcMustDeleteMe){
            delete();
        }
        super.finalize();
    }
}
