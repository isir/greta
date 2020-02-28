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
package greta.core.animation.performer;

/**
 *
 * @author Jing Huang
 */
public class RelativeDynamicsPerformer extends Thread {

    private static final Object mutex = new Object();
    private boolean requestStop = false;

    @Override
    public void run() {
        while (!requestStop) {

            try {
                sleep(5);
            } catch (Exception ex) {
            }  //
            synchronized (mutex) {


            }
        }
    }

}
