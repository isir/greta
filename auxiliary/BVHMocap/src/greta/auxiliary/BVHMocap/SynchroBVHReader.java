/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.BVHMocap;

/**
 *
 * @author Nesrine Fourati
 * @author Radoslaw Niewiadomski
 */
public class SynchroBVHReader extends BVHReader //implements EventPerformer
{

    public SynchroBVHReader() {
        this("");
    }

    public SynchroBVHReader(String fname) {
        fileName = fname;
        dictionary = new Dictionary();
        dictionary.Initialize();
    }

//   // @Override
//    public void load(String bvhFileName) {
//        fileName = bvhFileName;
//    }
//
//    public void performEvent(String message) {   //fileName = message;
//        process();
//    }
}
