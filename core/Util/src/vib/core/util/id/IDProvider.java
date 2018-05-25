/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.id;

import vib.core.util.time.Timer;
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
