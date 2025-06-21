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
package greta.auxiliary.player.ogre.capture;

import greta.auxiliary.player.ogre.Camera;

/**
 * This interface describes an object that can be captured.
 * @author Andre-Marie Pez
 */
public interface Capturable {

    public Camera getCamera();

    /**
     * Call when the capture begin.<br/>
     * Must compute the width and the height of the catured image.
     */
    public void prepareCapture();

    /**
     * Returns the width of the image.<br/>
     * It must be a multiple of 4
     * @return the width of the image.
     */
    public int getCaptureWidth();

    /**
     * Returns the height of the image.<br/>
     * It must be a multiple of 4
     * @return the height of the image.
     */
    public int getCaptureHeight();

    /**
     * Checks if the width or the height have changed since the last call of {@code prepareCapture()}.
     * @return {@code true} if the width or the height have changed. {@code false} otherwise.
     */
    public boolean isSizeChanged();

    /**
     * Returns the pixel data of the current image.<br/>
     * It is supposed that the pixel are in 3 bytes RGB.
     * @return the pixel data of the current image.
     */
    public byte[] getCaptureData();
}
