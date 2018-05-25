/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.log;

/**
 * This class print logs in the Java System outputs.<br/>
 * <b>Be carefull :</b> add twice (or more) in vib.core.util.log.Logs may print twice (or more) the same log.
 * @see System#out System.out
 * @see System#err System.err
 * @author Andre-Marie Pez
 */

public class LogPrinter implements LogOutput{

    public void onDebug(String message) {
        System.out.println(message);
    }

    public void onInfo(String message) {
        System.out.println(message);
    }

    /**
     * This method will be called when {@code Logs.warning} is called.<br/>
     * The tag {@code WARNING} is added before the message.
     */
    public void onWarning(String message) {
        System.out.println("WARNING "+message);
    }

    /**
     * This method will be called when {@code Logs.error} is called.<br/>
     * The tag {@code ERROR} is added before the message.
     */
    public void onError(String message) {
        System.err.println("ERROR "+message);
    }

    @Override
    public String toString(){
        return getClass().getName();
    }
}
