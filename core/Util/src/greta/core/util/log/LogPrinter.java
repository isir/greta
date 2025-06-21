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
 * This class print logs in the Java System outputs.<br/>
 * <b>Be carefull :</b> add twice (or more) in greta.core.util.log.Logs may print twice (or more) the same log.
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
