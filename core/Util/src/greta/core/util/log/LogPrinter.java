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
