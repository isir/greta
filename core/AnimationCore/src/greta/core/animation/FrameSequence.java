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
