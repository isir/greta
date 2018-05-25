/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.behaviorplanner.mseconstraints;

/**
 *
 * @author Radoslaw Niewiadomski
 */

public class ShortSignal {
    double start;
    double end;
    String label;
    int id;

    public ShortSignal(){
    start = -1;
    end =-1;
    label="";
    id=-1;
    }

    public void setStart(double start){
        this.start = start;
    }

    public void setEnd(double end){
        this.end = end;
    }

    public void setLabel(String label){
        this.label = label;
    }

    public void setId(int id){
        this.id = id;
    }

    public double getStart(){
        return this.start;
    }

    public double getEnd(){
        return this.end;
    }

    public String getLabel(){
        return this.label;
    }

    public int getId(){
        return this.id;
    }

}
