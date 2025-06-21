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
package greta.core.animation.mocap;

import greta.core.animation.Frame;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class MotionSequence {
    String _name, _startFrameRef, _endFrameRef;
    ArrayList<Frame> _sequence = new ArrayList<Frame>();
    int _frameNb = 0;

    public MotionSequence(String name, String startFrameRef, String endFrameRef) {
        this._name = name;
        this._startFrameRef = startFrameRef;
        this._endFrameRef = endFrameRef;
    }

    public MotionSequence(String name) {
        this._name = name;
    }

    public String getEndFrameRef() {
        return _endFrameRef;
    }

    public void setEndFrameRef(String endFrameRef) {
        this._endFrameRef = endFrameRef;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public ArrayList<Frame> getSequence() {
        return _sequence;
    }

    public void setSequence(ArrayList<Frame> sequence) {
        this._sequence = sequence;
        _frameNb = sequence.size();
    }

    public String getStartFrameRef() {
        return _startFrameRef;
    }

    public void setStartFrameRef(String startFrameRef) {
        this._startFrameRef = startFrameRef;
    }

    public int getFrameNb() {
        return _frameNb;
    }

}
