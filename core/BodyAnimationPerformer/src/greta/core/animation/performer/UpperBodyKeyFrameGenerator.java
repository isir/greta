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
package greta.core.animation.performer;

import greta.core.animation.body.Arm;
import greta.core.animation.body.Head;
import greta.core.animation.body.Shoulder;
import greta.core.animation.body.Torso;
import greta.core.keyframes.GestureKeyframe;
import greta.core.keyframes.HeadKeyframe;
import greta.core.keyframes.Keyframe;
import greta.core.keyframes.KeyframePerformer;
import greta.core.keyframes.ShoulderKeyframe;
import greta.core.keyframes.TorsoKeyframe;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.Side;
import greta.core.util.id.ID;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jing Huang
 * new class for receive key frame, then generate upper body key frames for each body, need to gather then rescatter
 */

//TODO: wait for AndreMarie pour nouveau structure de keyframe
public class UpperBodyKeyFrameGenerator  implements KeyframePerformer{
    SymbolicConverter _symbolicConverter;

    public UpperBodyKeyFrameGenerator(CharacterManager cm){
        _symbolicConverter = new SymbolicConverter(cm);
    }
    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID id) {
        LinkedList<Arm> _left = new LinkedList<Arm>();
        LinkedList<Arm> _right = new LinkedList<Arm>();
        LinkedList<Torso> _torse = new LinkedList<Torso>();
        LinkedList<Head> _head = new LinkedList<Head>();
        LinkedList<Shoulder> _leftShoulder = new LinkedList<Shoulder>();
        LinkedList<Shoulder> _rightShoulder = new LinkedList<Shoulder>();

        for (Keyframe kf : keyframes) {
            if (kf instanceof GestureKeyframe) {
                GestureKeyframe keyframe = (GestureKeyframe) kf;
                Side side = keyframe.getSide();
                if (side == Side.LEFT) {
                    Arm arm = _symbolicConverter.getArm(keyframe);
                    _left.add(arm);
                } else if (side == Side.RIGHT) {
                    Arm arm = _symbolicConverter.getArm(keyframe);
                    _right.add(arm);
                    //System.out.println(arm.getTarget() + " " + arm.getTime());
                } else {
                    System.out.println("IKKeyFramePerformer: GestureKeyframe side error:" + side);
                }
            } else if (kf instanceof TorsoKeyframe) {
                TorsoKeyframe keyframe = (TorsoKeyframe) kf;
                _torse.add(_symbolicConverter.getTorse(keyframe));
            } else if (kf instanceof HeadKeyframe) {
                HeadKeyframe keyframe = (HeadKeyframe) kf;
                _head.add(_symbolicConverter.getHead(keyframe));
            } else if (kf instanceof ShoulderKeyframe) {
                ShoulderKeyframe keyframe = (ShoulderKeyframe) kf;
                String side = keyframe.getSide();
                if (side.equalsIgnoreCase("LEFT")) {
                    _leftShoulder.add(_symbolicConverter.getShoulder(keyframe));
                } else if (side.equalsIgnoreCase("RIGHT")) {
                    _rightShoulder.add(_symbolicConverter.getShoulder(keyframe));
                }

            } else {
                //System.out.println("IKKeyFramePerformer: Keyframe type error : "+kf.getClass().getSimpleName());
            }
        }
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId, Mode mode) {
        // TODO : Mode management in progress
        performKeyframes(keyframes, requestId);
    }
}
