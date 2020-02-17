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

import java.util.ArrayList;

/**
 * It contains only static fields and methods<br/>
 * This class manages log informations by receives them, and sends to outputs.<br/>
 * <br/>
 * exemple :<br/>
 * <code>public static void main(String[] args) {<br/><br/>
 * <dd>
 * // once in the program :<br/>
 * Logs.add(new LogPrinter()); // to print the logs in the console<br/>
 * Logs.add(new LogFile("log.txt")); // to write the logs in the file "log.txt"<br/>
 * Logs.setLevel(Logs.RELEASE); // to print all logs exept debug<br/><br/>
 * // then, anywhere in the program :<br/>
 * Logs.debug("this is a debug text");<br/>
 * Logs.info("it's just an information");<br/>
 * Logs.warning("this is a warning");<br/>
 * Logs.error("it averts that an error is occured");</dd><br/>
 * }</code><br/>
 * in the console and in the file "log.txt" we can read :<br/>
 * <code>
 * it's just an information<br/>
 * WARNING this is a warning<br/>
 * ERROR it averts that an error is occured<br/>
 * </code>
 * <br/>
 * <b>remark :</b> if no output is added, the program will work normally but logs will leave no trace
 * @see greta.core.util.log.LogOutput interface LogOutput
 * @see greta.core.util.log.LogPrinter LogPrinter
 * @see greta.core.util.log.LogFile LogFile
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @composed - - * greta.core.util.log.LogOutput
 */
public class Logs {
//Private members
    /**
     * Don't let anyone instantiate this class.
     */
    private Logs(){}

    private static ArrayList<LogOutput> outputs = new ArrayList<LogOutput>();
    private static boolean sendDebug = true;
    private static boolean sendInfo = true;
    private static boolean sendWarning = true;
    private static boolean sendError = true;

//Public members
    /**
     * No message will be send to outputs
     *
     * @see #setLevel(int)
     */
    public static final int NOLOG = 0;
    /**
     * Debug messages will be send to outputs
     *
     * @see #setLevel(int)
     */
    public static final int DEBUG = 1;
    /**
     * Info messages will be send to outputs
     *
     * @see #setLevel(int)
     */
    public static final int INFO = 2;
    /**
     * Warning messages will be send to outputs
     *
     * @see #setLevel(int)
     */
    public static final int WARNING = 4;
    /**
     * Error messages will be send to outputs
     *
     * @see #setLevel(int)
     */
    public static final int ERROR = 8;
    /**
     * All messages exept debug messages will be send to outputs
     *
     * @see #setLevel(int)
     */
    public static final int RELEASE = 14;
    /**
     * All messages will be send to outputs
     *
     * @see #setLevel(int)
     */
    public static final int ALL = 15;
    /**
     * Adds an output for logs.<br/>
     * When functions {@code debug}, {@code info}, {@code warning}
     * and {@code error} are called, the message is send to each outputs<br/>
     * @param output an output for logs
     * @see greta.core.util.log.LogOutput LogOutput
     * @see #debug(String) debug
     * @see #info(String) info
     * @see #warning(String) warning
     * @see #error(String) error
     */
    public static void add(LogOutput output){
        outputs.add(output);
    }

    /**
     * Removes the first occurrence of the specified {@code LogOutput}, if it is present.
     * @param output the {@code LogOutput} to remove
     */
    public static void remove(LogOutput output){
        outputs.remove(output);
    }

    /**
     * Sets the level of information.<br/>
     * You can use {@code NOLOG}, {@code DEBUG}, {@code INFO}, {@code WARNING},
     * {@code ERROR}, {@code RELEASE} and {@code ALL} to define the level.<br/><br/>
     * For exemple you can call
     * {@code Logs.setLevel( Logs.ERROR )}
     * to have only error messages<br/>
     * or call
     * {@code Logs.setLevel( Logs.WARNING + Logs.ERROR )}
     * to have only warning and error messages<br/>
     * or call
     * {@code Logs.setLevel( Logs.ALL - Logs.DEBUG )}
     * to have all messages exept debug messages, etc.<br/><br/>
     * The default value is {@code ALL}.
     *
     * @param level level of information
     * @see #NOLOG NOLOG
     * @see #DEBUG DEBUG
     * @see #INFO INFO
     * @see #WARNING WARNING
     * @see #ERROR ERROR
     * @see #RELEASE RELEASE
     * @see #ALL ALL
     */
    public static void setLevel(int level){
        if(level<NOLOG || level>ALL){
            warning("Invalid value in Logs.setLevel : "+level+". The level is set to ALL");
            setLevel(ALL);
        }
        else{
            int parseLevel = level;
            sendError = parseLevel >= ERROR;
            parseLevel = parseLevel % ERROR;
            sendWarning = parseLevel >= WARNING;
            parseLevel = parseLevel % WARNING;
            sendInfo = parseLevel >= INFO;
            parseLevel = parseLevel % INFO;
            sendDebug = parseLevel >= DEBUG;
        }
    }

    /**
     * If the level allows, the message is sended to the added outputs.
     * @param message a debug message
     * @see #setLevel(int) setLevel
     * @see #add(greta.core.util.log.LogOutput) add
     */
    public static void debug(String message){
        if(sendDebug) {
            for(LogOutput out : outputs) {
                out.onDebug(message);
            }
        }
    }

    /**
     * If the level allows, the message is sended to the added outputs.
     * @param message an information message
     * @see #setLevel(int) setLevel
     * @see #add(greta.core.util.log.LogOutput) add
     */
    public static void info(String message){
        if(sendInfo) {
            for(LogOutput out : outputs) {
                out.onInfo(message);
            }
        }
    }

    /**
     * If the level allows, the message is sended to the added outputs.
     * @param message a warning message
     * @see #setLevel(int) setLevel
     * @see #add(greta.core.util.log.LogOutput) add
     */
    public static void warning(String message){
        if(sendWarning) {
            for(LogOutput out : outputs) {
                out.onWarning(message);
            }
        }
    }

    /**
     * If the level allows, the message is sended to the added outputs.
     * @param message an error message
     * @see #setLevel(int) setLevel
     * @see #add(greta.core.util.log.LogOutput) add
     */
    public static void error(String message){
        if(sendError) {
            for(LogOutput out : outputs) {
                out.onError(message);
            }
        }
    }

    /**
     * Check if the current level permits to show debug messages.
     * @return {@code true} if the current level permits debug messages, {@code false} otherwise.
     */
    public static boolean hasLevelDebug(){
        return sendDebug;
    }

    /**
     * Check if the current level permits to show information messages.
     * @return {@code true} if the current level permits information messages, {@code false} otherwise.
     */
    public static boolean hasLevelInfo(){
        return sendInfo;
    }

    /**
     * Check if the current level permits to show warning messages.
     * @return {@code true} if the current level permits warning messages, {@code false} otherwise.
     */
    public static boolean hasLevelWarning(){
        return sendWarning;
    }

    /**
     * Check if the current level permits to show error messages.
     * @return {@code true} if the current level permits error messages, {@code false} otherwise.
     */
    public static boolean hasLevelError(){
        return sendError;
    }
}
