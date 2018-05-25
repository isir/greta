/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.log;

/**
 * Iterface to iplement an output for logs.
 * @see vib.core.util.log.Logs
 * @author Andre-Marie Pez
 */
public interface LogOutput {
    /**
     * This method will be called when {@code Logs.debug} is called
     * @param message a debug message
     * @see vib.core.util.log.Logs#debug(java.lang.String) Logs.debug
     */
    public void onDebug(String message);
    /**
     * This method will be called when {@code Logs.info} is called
     * @param message an information message
     * @see vib.core.util.log.Logs#info(java.lang.String) Logs.info
     */
    public void onInfo(String message);
    /**
     * This method will be called when {@code Logs.warning} is called
     * @param message a warning message
     * @see vib.core.util.log.Logs#warning(java.lang.String) Logs.warning
     */
    public void onWarning(String message);
    /**
     * This method will be called when {@code Logs.error} is called
     * @param message an error message
     * @see vib.core.util.log.Logs#error(java.lang.String) Logs.error
     */
    public void onError(String message);
}
