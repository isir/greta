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
 * @author Jing Huang
 */
public class SequenceWarpper {

    /**
     *
     * @param input
     * @param distance the first element needs to be 0, the distance of e0 - e0,
     * the second is e1-e0, the third is e2 - e1
     * @param frameNb
     * @return
     */
    static public FrameSequence getDistanceWarppedSequence(FrameSequence input, ArrayList<Double> distance, int frameNb) {
        FrameSequence fs = new FrameSequence();
        double totalWeight = 0;
        for (double dis : distance) {
            totalWeight += dis;
        }
        double perstep = totalWeight / frameNb;

        int currentDistance = 0;

        ArrayList<Frame> frames = input.getSequence();
        int currentIdx = 0;
        Frame f0 = frames.get(currentIdx);
        int distance0 = 0;
        Frame f1 = frames.get(currentIdx);
        int distance1 = 0;

        for (int i = 0; i < frameNb; ++i) {
            currentDistance += i * perstep;
            while (distance1 < currentDistance) {
                currentIdx++;
                distance0 = distance1;
                distance1 += distance.get(currentIdx);
                f0 = f1;
                f1 = frames.get(currentIdx);
            }
            Frame f = new Frame();
            if (distance1 - distance0 == 0) {
                f = f0.clone();
            } else {
                f.interpolation(f0, f1, (currentDistance - distance0) / (distance1 - distance0));
            }
        }

        return fs;
    }


    static public ArrayList<Frame> getDistanceWarppedSequence(ArrayList<Frame> input, ArrayList<Double> distance, int frameNb) {
        ArrayList<Frame> fs = new ArrayList<Frame>();
        double totalWeight = 0;
        for (int i = 1; i < distance.size(); ++i) {
            distance.set(i, distance.get(i) + distance.get(i - 1));
        }
        totalWeight = distance.get(distance.size() - 1);
        double perstep = totalWeight / frameNb;
        double currentDistance = 0;

        ArrayList<Frame> frames = input;
        int currentIdx = 0;
        Frame f0 = frames.get(currentIdx);
        double distance0 = 0;
        Frame f1 = frames.get(currentIdx);
        double distance1 = 0;

        for (int i = 0; i < frameNb; ++i) {
            currentDistance = i * perstep;
            while (distance1 < currentDistance) {
                currentIdx++;
                distance0 = distance1;
                distance1 = distance.get(currentIdx);
                f0 = f1;
                f1 = frames.get(currentIdx);
            }
            Frame f = new Frame();
            if (distance1 - distance0 == 0) {
                f = f0.clone();
            } else {
                f.interpolation(f0, f1, (currentDistance - distance0) / (distance1 - distance0));
            }
            fs.add(f);
        }

        return fs;
    }
}
