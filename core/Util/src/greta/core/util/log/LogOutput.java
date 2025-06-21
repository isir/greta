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
package greta.core.util.log;

/**
 * Iterface to iplement an output for logs.
 * @see greta.core.util.log.Logs
 * @author Andre-Marie Pez
 */
public interface LogOutput {
    /**
     * This method will be called when {@code Logs.debug} is called
     * @param message a debug message
     * @see greta.core.util.log.Logs#debug(java.lang.String) Logs.debug
     */
    public void onDebug(String message);
    /**
     * This method will be called when {@code Logs.info} is called
     * @param message an information message
     * @see greta.core.util.log.Logs#info(java.lang.String) Logs.info
     */
    public void onInfo(String message);
    /**
     * This method will be called when {@code Logs.warning} is called
     * @param message a warning message
     * @see greta.core.util.log.Logs#warning(java.lang.String) Logs.warning
     */
    public void onWarning(String message);
    /**
     * This method will be called when {@code Logs.error} is called
     * @param message an error message
     * @see greta.core.util.log.Logs#error(java.lang.String) Logs.error
     */
    public void onError(String message);
}
