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

/**
 *
 * @author Andre-Marie Pez
 */
public interface CaptureListener {

    /**
     * Called when a capture is started by a {@code Capturer}.
     * @param source the {@code Capturer} which starts the capture.
     * @param time the Greta's time in milliseconds.
     */
    public void captureStarted(Capturer source, long time);

    /**
     * Called when a new frame is captured by a {@code Capturer}.
     * @param source the {@code Capturer} which captures the new frame.
     * @param time the Greta's time in milliseconds.
     */
    public void captureNewFrame(Capturer source, long time);

    /**
     * Called when a capture is ended by a {@code Capturer}.
     * @param source the {@code Capturer} which ends the capture.
     * @param time the Greta's time in milliseconds.
     */
    public void captureEnded(Capturer source, long time);
}
