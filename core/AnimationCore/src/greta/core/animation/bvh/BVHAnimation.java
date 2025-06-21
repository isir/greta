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
