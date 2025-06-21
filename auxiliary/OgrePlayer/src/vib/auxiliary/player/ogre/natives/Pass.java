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
public class Pass extends _Object_ {

    public Pass(long pointer) {
        super(pointer);
    }


    public boolean hasFragmentProgram() {
        return _hasFragmentProgram(getNativePointer());
    }
    private native boolean _hasFragmentProgram(long p);

    public GpuProgramParameters getFragmentProgramParameters() {
        return new GpuProgramParameters(_getFragmentProgramParameters(getNativePointer()));
    }
    private native long _getFragmentProgramParameters(long p);

    public void setAmbient(ColourValue colour) {
       _setAmbient(getNativePointer(), colour.getNativePointer());
    }
    private native void _setAmbient(long p, long colourp);

    @Override
    protected native void delete(long nativePointer);
}
