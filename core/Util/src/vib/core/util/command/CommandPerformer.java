/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.command;

/**
 *
 * @author Nadine
 */


public interface CommandPerformer {
    public String performCommandReturnString(Command command);
    public int performCommandReturnInt(Command command);
    public boolean performCommandReturnBoolean(Command command);
}
