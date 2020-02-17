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
