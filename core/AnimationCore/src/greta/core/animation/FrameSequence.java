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
package greta.core.animation;

import java.util.ArrayList;

/**
 *
 * @author  Jing HUANG
 */
public class FrameSequence {

    double _start;
    double _end;
    ArrayList<Frame> _sequence = new ArrayList<Frame>();

    public FrameSequence() {
    }

    public FrameSequence(double start, double end) {
        _start = start;
        _end = end;
    }

     public FrameSequence(double start, double end, ArrayList<Frame> s) {
        _start = start;
        _end = end;
        _sequence = s;
    }

    public double getEndTime() {
        return _end;
    }

    public void setEndTime(double end) {
        this._end = end;
    }

    public double getStartTime() {
        return _start;
    }

    public void setStartTime(double start) {
        this._start = start;
    }

    public void add(Frame f) {
        _sequence.add(f);
    }

    public ArrayList<Frame> getSequence() {
        return _sequence;
    }

    public ArrayList<Frame> getCompressedSequence(int nbframes) {
        int nb_original = _sequence.size();
        double factor = (double) nb_original / (double) nbframes;
        ArrayList<Frame> sequence = new ArrayList<Frame>();
        for (int i = 0; i < nbframes; ++i) {
            double idxf = i * factor;
            int idx = (int) (idxf);
            Frame f = new Frame();
            if (idx + 1 >= nbframes) {
                f = _sequence.get(idx).clone();
            } else {
                Frame f0 = _sequence.get(idx);
                Frame f1 = _sequence.get(idx + 1);
                f.interpolation(f0, f1, idxf - idx);
            }
            sequence.add(f);
        }
        return sequence;
    }

    public Frame getFrameByTime(double time) {
        if (_sequence.size() < 1) {
            return null;
        }
        if (time == _end) {
            return _sequence.get(_sequence.size() - 1).clone();
        }
        double duration = _end - _start;
        double timestep = duration / (double)(_sequence.size() - 1);
        Frame frame = new Frame();
        if (time < _start || time > _end) {
            return null;
        }
        int idx = (int) ((_sequence.size() - 1) * (time - _start) / (_end - _start));

        Frame frame0 = _sequence.get(idx);
        Frame frame1 = _sequence.get(idx + 1);
        double t0 = idx * timestep + _start;
        double ratio = (time - t0) / timestep;
        frame.interpolation(frame0, frame1, ratio);
        //System.out.println("size: " + _sequence.size() + " idx + 1 : "+ (idx + 1) + " ratio:" + ratio + " start:" + _start + " _end:" + _end + " time: " + time);
        return frame;
    }
}
