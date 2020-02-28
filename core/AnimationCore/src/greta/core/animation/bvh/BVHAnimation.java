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
package greta.core.animation.bvh;

import greta.core.animation.Skeleton;
import java.util.ArrayList;

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
