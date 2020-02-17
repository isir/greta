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

import java.io.FileWriter;
import java.io.IOException;

/**
 * This class print logs in a specified file.
 * @author Andre-Marie Pez
 */
public class LogFile implements LogOutput{
    private FileWriter out;
    private boolean oppenned;
    private String fileName;
    private void write(String text){
        if(oppenned){
            try {
                out.write(text + "\n");
                out.flush();
            } catch (IOException ex) {
                //Logs.warning("Can not write in "+fileName); // can make an infinite loop
            }
        }
    }

    /**
     * Build a LogFile with a target file name.<br/>
     * The logs will be written in the target file.
     * @param logFileName the name of the file
     */
    public LogFile(String logFileName){
        oppenned = false;
        fileName = logFileName;
        try {
            out = new FileWriter(fileName);
            oppenned = true;
        }
        catch (IOException ex){
            Logs.warning("Can not open "+fileName);
        }
    }

    public void onDebug(String message) {
        write(message);
    }

    public void onInfo(String message) {
        write(message);
    }

    /**
     * This method will be called when {@code Logs.warning} is called.<br/>
     * The tag {@code WARNING} is added before the message.
     */
    public void onWarning(String message) {
        write("WARNING "+message);
    }

    /**
     * This method will be called when {@code Logs.error} is called.<br/>
     * The tag {@code ERROR} is added before the message.
     */
    public void onError(String message) {
        write("ERROR "+message);
    }

    @Override
    public String toString(){
        return getClass().getName()+" - target : "+fileName;
    }
}
