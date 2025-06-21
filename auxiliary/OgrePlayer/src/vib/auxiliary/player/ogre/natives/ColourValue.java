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
public class ColourValue extends _Object_ {

    public ColourValue(long pointer){
        super(pointer);
    }
    public ColourValue(double r, double g, double b, double alpha) {
        super(_instanciate(r, g, b, alpha));
        this.gcMustDeleteThat(true);
    }

    private static native long _instanciate(double r, double g, double b, double alpha);

    @Override
    protected native void delete(long nativePointer);

}
