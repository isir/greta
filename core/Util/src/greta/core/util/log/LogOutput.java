/*
 * This file is part of Greta.
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
