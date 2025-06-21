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
public class Viewport extends _Object_{

    public Viewport(long pointer) {
        super(pointer);
    }

    public void setBackgroundColour(ColourValue convertToColor) {
        _setBackgroundColour(getNativePointer(), convertToColor.getNativePointer());
    }
    private native void _setBackgroundColour(long thiPointer, long colourPointer);

    @Override
    protected native void delete(long nativePointer);
}
