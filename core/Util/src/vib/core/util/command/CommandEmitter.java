/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.command;

/**
 *
 * @author Nadine
 */


public interface CommandEmitter {
    public void setCommandPerformer(CommandPerformer discoCommandPerformer);
    public String sendCommandReturnString(String commandName);
    public int sendCommandReturnInt(String commandName, int defaultValue);
    public boolean sendCommandReturnBoolean(String commandName, boolean defaultValue);
}
