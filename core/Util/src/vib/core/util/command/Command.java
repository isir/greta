/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.command;

/**
 *
 * @author Nadine
 */


public class Command {
    public String commandName = "";
    public String[] commandParameterString;

    
    public Command(String commandName, String... commandParameterString){
        this.commandName = commandName;
        this.commandParameterString = commandParameterString;
    }
}
