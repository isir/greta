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
package greta.core.util.id;

import greta.core.util.time.Timer;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 * @author Ken Prepin
 */
public class IDProvider {

    private static long count = 0;

    private static synchronized long getNum(){
        return count++;
    }

    public static ID createID(String source){
        return new ID(getNum(), Timer.getTimeMillis(), source, getPID());
    }

    public static ID createID(String source, ID... parents){
        ID newID = new ID(getNum(), Timer.getTimeMillis(), source, getPID());
        newID.addParents(Arrays.asList(parents));
        return newID;
    }

    public static ID createID(String source, List<ID> parents){
        ID newID = new ID(getNum(), Timer.getTimeMillis(), source, getPID());
        newID.addParents(parents);
        return newID;
    }

    public static ID createID(String source, String pid){
        return new ID(getNum(), Timer.getTimeMillis(), source, pid);
    }

    public static ID createID(String source, String pid, ID... parents){
        ID newID = new ID(getNum(), Timer.getTimeMillis(), source, pid);
        newID.addParents(Arrays.asList(parents));
        return newID;
    }

    public static ID createID(String source, String pid, List<ID> parents){
        ID newID = new ID(getNum(), Timer.getTimeMillis(), source, pid);
        newID.addParents(parents);
        return newID;
    }

    private static String getPID(){
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
    }
}
