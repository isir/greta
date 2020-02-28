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
package greta.core.animation.common;

import greta.core.animation.common.body.Arm;
import greta.core.animation.common.body.Body;
import greta.core.animation.common.body.Head;
import greta.core.animation.common.body.Shoulder;
import greta.core.animation.common.body.Torse;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
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
 * this class will receive all the key info (in previous module they call kf)
 * please check     performeKeyframes()        function, we receive by this function.
 * so if do debug, first check if we receive good informations, please.
 *     getKeyFrameGroup()  is used to merge body parts, we do interpolations for missing informations, cause the kfs are from different modulars.
 *
 * @author Jing Huang
 */
public class IKKeyframePerformer implements KeyframePerformer, BAPFrameEmitter {

    IKFramesGenerator _ikFramesGenerator = new IKFramesGenerator();
    FramesController _framesController = new FramesController();
    HumanAgent _agent;
    GesturePathInterpolator path = new GesturePathInterpolator();

    public IKKeyframePerformer(CharacterManager cm) {
        super();
        _agent = new HumanAgent(cm);
        addFramesReceiver(_framesController);
        _ikFramesGenerator.start();
        _framesController.start();
    }

    public IKFramesGenerator getIKFramesGenerator() {
        return _ikFramesGenerator;
    }

    /**
     *
     * @param keyframes
     * @param requestId
     * receive all key frames, then send to FramesGenerator  to interpolate into frames
     */

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId) {
        //System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        generateBodyParts(keyframes, requestId);
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId, Mode mode) {
        // TODO : Mode management in progress
        performKeyframes(keyframes, requestId);
    }

    void generateBodyParts(List<Keyframe> keyframes, ID requestId){
        LinkedList<Arm> _left = new LinkedList<Arm>();
        LinkedList<Arm> _right = new LinkedList<Arm>();
        LinkedList<Torse> _torse = new LinkedList<Torse>();
        LinkedList<Head> _head = new LinkedList<Head>();


        LinkedList<Shoulder> _leftShoulder = new LinkedList<Shoulder>();
        LinkedList<Shoulder> _rightShoulder = new LinkedList<Shoulder>();
        for (Keyframe kf : keyframes) {
            //System.out.print("key time : " + kf.getOffset());
            if (kf instanceof GestureKeyframe) {
                GestureKeyframe keyframe = (GestureKeyframe) kf;
                Side side = keyframe.getSide();
                if (side == Side.LEFT) {
                    Arm arm = _agent.getSymbolicConverter().getArm(keyframe);
                    //System.out.println("l traj : " + arm.getTrajectory().getName() + "   " + arm.getTime());
                    _left.add(arm);
                } else if (side == Side.RIGHT) {
                    Arm arm = _agent.getSymbolicConverter().getArm(keyframe);
                    _right.add(arm);
                    System.out.println("R traj : " + arm.getTarget().getPosition() + "   "  + arm.getTarget().getUpDirectionVector());
                } else {
                    System.out.println("IKKeyFramePerformer: GestureKeyframe side error:" + side);
                }
                //System.out.println("GestureKeyframe "+side);
            } else if (kf instanceof TorsoKeyframe) {
                TorsoKeyframe keyframe = (TorsoKeyframe) kf;
                _torse.add(_agent.getSymbolicConverter().getTorse(keyframe));
                //System.out.println("TorsoKeyframe ");
            } else if (kf instanceof HeadKeyframe) {
                HeadKeyframe keyframe = (HeadKeyframe) kf;
                _head.add(_agent.getSymbolicConverter().getHead(keyframe));
                //System.out.println("HeadKeyframe ");
            } else if (kf instanceof ShoulderKeyframe) {
                ShoulderKeyframe keyframe = (ShoulderKeyframe) kf;
                String side = keyframe.getSide();
                if(side.equalsIgnoreCase("LEFT")){
                    _leftShoulder.add(_agent.getSymbolicConverter().getShoulder(keyframe));
                }else if (side.equalsIgnoreCase("RIGHT")){
                    _rightShoulder.add(_agent.getSymbolicConverter().getShoulder(keyframe));
                }
            } else {
                //System.out.println("IKKeyFramePerformer: Keyframe type error : "+kf.getClass().getSimpleName());
            }
        }
        _left =  path.mixPathDriven(_left);
        _right = path.mixPathDriven(_right);
        for(Arm arm:_right){
            System.out.println(arm.getTime() +"   "+ arm.getPosition());
        }

        LinkedList<Arm> _leftF = new LinkedList<Arm>();
        LinkedList<Arm> _rightF = new LinkedList<Arm>();
        LinkedList<Torse> _torseF = new LinkedList<Torse>();
        LinkedList<Head> _headF = new LinkedList<Head>();
        _headF = _head;
        LinkedList<Body> bodys = KeyFrameMixer.mix(_left, _right, _torse, 0.1f);
        for(Body b: bodys){
            b.computeKeyFrame(_agent);
            Arm l = b.getLeftArm();
            Arm r = b.getRightArm();
            Torse t = b.getTorse();
            if(l != null){
                _leftF.add(l);
            }
            if(r != null){
                _rightF.add(r);
            }
            if(t != null){
                _torseF.add(t);
            }
        }
        //System.out.println("\n-----------------------------------------------------------------------");
        _ikFramesGenerator.updateKeyFrameList(_leftF, _rightF, _torseF, _headF, _leftShoulder, _rightShoulder, 0, requestId);

    }


    @Override
    public void addBAPFramePerformer(BAPFramePerformer performer) {
        _framesController.addBAPFramePerformer(performer);
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer performer) {
        _framesController.removeBAPFramePerformer(performer);
    }


    public void addFramesReceiver(FramesReceiver fr) {
        _ikFramesGenerator.addFramesReceiver(fr);
        //_handShapeGenerator.addFramesReceiver(fr);
    }

    public void setExpressiveTorso(boolean active){
        _agent.getExpressiveTorso().setActive(active);
    }

    public boolean getExpressiveTorso(){
        return _agent.getExpressiveTorso().isActive();
    }
}
