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
