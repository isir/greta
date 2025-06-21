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
public class Animation extends _Object_{

    public Animation(long pointer) {
        super(pointer);
    }

    public VertexAnimationTrack createVertexTrack_VAT_POSE(int targetSubmesh) {
        return new VertexAnimationTrack(_createVertexTrack_VAT_POSE(getNativePointer(), targetSubmesh));
    }

    private native long _createVertexTrack_VAT_POSE(long thisPointer, int targetSubmesh);

    public VertexAnimationTrack getVertexTrack(int targetSubmesh) {
        return new VertexAnimationTrack(_getVertexTrack(getNativePointer(), targetSubmesh));
    }

    private native long _getVertexTrack(long thisPointer, int targetSubmesh);

    @Override
    protected native void delete(long nativePointer);

}
