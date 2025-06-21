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
