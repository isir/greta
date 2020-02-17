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
