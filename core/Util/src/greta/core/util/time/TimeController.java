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
package greta.core.util.time;

/**
 * @author Andre-Marie Pez
 */
public interface TimeController {

    /**
     * Returns the current time in milliseconds.
     * @return the current time in milliseconds.
     */
    public long getTimeMillis();

    /**
     * Set the current time in milliseconds.
     * @param milliSeconds the current time in milliseconds.
     */
    public void setTimeMillis(long milliSeconds);

    /**
     * Returns the current time in seconds.
     * @return the current time in seconds.
     */
    public double getTime();

    /**
     * Set the current time in seconds.
     * @param seconds the current time in seconds.
     */
    public void setTime(double seconds);

    /**
     * Stops the current thread until the specified duration is elapsed.
     * @param millis time to sleep in milli seconds
     */
    public void sleep(long millis);
}
