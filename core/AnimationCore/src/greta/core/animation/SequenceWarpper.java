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
