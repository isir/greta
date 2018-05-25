/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.feedbacks;

import vib.core.util.id.ID;

/**
 *
 * @author Ken Prepin
 */
public class Callback {

    private String type;
    private double time;
    private ID animId;

    public Callback(Callback callback){
        this(callback.type(),callback.time(), callback.animId());
    }

    public Callback(String type, double time, ID animId) {
        this.type = type;
        this.time = time;
        this.animId = animId;
    }

    public String type() {
        return type;
    }

    public double time() {
        return time;
    }

    public ID animId() {
        return animId;
    }
    public void setType(String type){
        this.type = type;
    }
     public void setTime(double time){
        this.time = time;
    }
    public void setAnimId(ID animId){
        this.animId = animId;
    }
}
