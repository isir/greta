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
package greta.core.animation.common.body;

import greta.core.animation.common.Frame.ExtendedKeyFrame;
import greta.core.animation.common.Frame.JointFrame;
import greta.core.util.math.Quaternion;
import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */
public class Hand extends ExtendedKeyFrame {

    String _side;

    public Hand(double time) {
        super(time);
    }

    public void setSide(String side) {
        _side = side;
    }

    public String getSide() {
        return _side;
    }

    public static Hand interpolation(Hand hand0, Hand hand1, double t) {
        double time = hand0.getTime() + (hand1.getTime() - hand0.getTime()) * t;
        Hand hand = new Hand(time);
        HashMap<String, JointFrame> h0 = hand0.getJointFrames();
        HashMap<String, JointFrame> h1 = hand1.getJointFrames();
        HashMap<String, JointFrame> h = new HashMap<String, JointFrame>();
        for (String name : h1.keySet()) {
            Quaternion q = Quaternion.slerp(h0.get(name)._localrotation, h1.get(name)._localrotation, t, true);
            JointFrame jf = new JointFrame();
            jf._localrotation = q;
            h.put(name, jf);
        }
        hand.addJointFrames(h);
        hand.setSide(hand1.getSide());
        return hand;
    }
}
