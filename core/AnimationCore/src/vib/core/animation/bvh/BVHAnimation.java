/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.core.animation.bvh;

import java.util.ArrayList;
import vib.core.animation.Skeleton;

/**
 *
 * @author  Jing HUANG
 */
public class BVHAnimation {

    private Skeleton _skeleton;
    private int _nbFrames;
    private double _frameTime;
    String _name;
    ArrayList<BVHFrame> _sequence = new ArrayList<BVHFrame>();
    BVHFrame _channel = new BVHFrame();
    
    public BVHAnimation(String name) {
        _name = name;
    }

    public double getFrameTime() {
        return _frameTime;
    }

    public void setFrameTime(double frameTime) {
        this._frameTime = frameTime;
    }

    public Skeleton getSkeleton() {
        return _skeleton;
    }

    public void setSkeleton(Skeleton skeleton) {
        this._skeleton = skeleton;
    }

    public int getNbFrames() {
        return _nbFrames;
    }

    public void setNbFrames(int nbFrames) {
        this._nbFrames = nbFrames;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public ArrayList<BVHFrame> getSequence() {
        return _sequence;
    }

    public void setSequence(ArrayList<BVHFrame> sequence) {
        this._sequence = sequence;
    }

    public BVHFrame getChannel() {
        return _channel;
    }

    public void setChannel(BVHFrame channel) {
        this._channel = channel;
    }
    
    
}
