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

import greta.core.animation.Frame;
import greta.core.animation.FrameSequence;
import greta.core.animation.FrameSequencesMixer;
import greta.core.animation.PositionFrame;
import greta.core.animation.SequenceIKCharacterBody;
import greta.core.animation.body.Arm;
import greta.core.animation.body.Body;
import greta.core.animation.body.ExpressiveTorso;
import greta.core.animation.body.Head;
import greta.core.animation.body.Shoulder;
import greta.core.animation.body.Torso;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.keyframes.GestureKeyframe;
import greta.core.keyframes.HeadKeyframe;
import greta.core.keyframes.Keyframe;
import greta.core.keyframes.KeyframePerformer;
import greta.core.keyframes.ShoulderKeyframe;
import greta.core.keyframes.TorsoKeyframe;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.CharacterManager;
import greta.core.util.Constants;
import greta.core.util.Mode;
import greta.core.util.enums.Side;
import greta.core.util.id.ID;
import greta.core.util.math.Function;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jing Huang
 */
public class SequenceAnimationKeyframePerformer extends CharacterDependentAdapter implements KeyframePerformer, BAPFrameEmitter, CharacterDependent {

    SymbolicConverter _symbolicConverter;
    SequenceIKCharacterBody _cb;
    ExpressiveTorso _exTorso = new ExpressiveTorso();
    boolean expressiveTorso = true;
//    int _framePerSecond = Constants.FRAME_PER_SECOND;
    BodyAnimationBAPFrameEmitter _be = new BodyAnimationBAPFrameEmitter();

    public SequenceAnimationKeyframePerformer(CharacterManager cm) {
        setCharacterManager(cm);
        _symbolicConverter = new SymbolicConverter(cm);
        _cb = new SequenceIKCharacterBody(_symbolicConverter.getOriginalSkeleton());
        _cb.initMassSystemByOriginalSkeleton();
    }

    @Override
    public void onCharacterChanged() {
        _cb = new SequenceIKCharacterBody(_symbolicConverter.getOriginalSkeleton());
        _cb.initMassSystemByOriginalSkeleton();
    }

    public void setExpressiveTorso(boolean t) {
        expressiveTorso = t;
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId) {
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
                    //System.out.println("Left: " + keyframe.getPhaseType() + keyframe.getOffset()+ " pos: "+ arm.getTarget());
                    _left.add(arm);
                } else if (side == Side.RIGHT) {
                    Arm arm = _symbolicConverter.getArm(keyframe);
                    _right.add(arm);
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
        FrameSequence fsBody = this.generateGestureSequence(_left, _right, 0.001f);
        FrameSequence fsHead = this.generateHeadSequence(_head, Constants.FRAME_PER_SECOND, null);
        FrameSequence fsTorso = this.generateTorsoSequence(_torse, Constants.FRAME_PER_SECOND, null);
        FrameSequence fsShoulderLeft = this.generateShoulderSequence(_leftShoulder, Constants.FRAME_PER_SECOND, null);
        FrameSequence fsShoulderRight = this.generateShoulderSequence(_rightShoulder, Constants.FRAME_PER_SECOND, null);

        FrameSequence fs = new FrameSequence();
        if (fsBody != null) {
            fs = FrameSequencesMixer.mixTwoSequences(fs, fsBody, Constants.FRAME_PER_SECOND);
        }
        if (fsHead != null) {
            fs = FrameSequencesMixer.mixTwoSequences(fs, fsHead, Constants.FRAME_PER_SECOND);
        }
        if (fsTorso != null) {
            fs = FrameSequencesMixer.mixTwoSequences(fs, fsTorso, Constants.FRAME_PER_SECOND);
        }
        if (fsShoulderLeft != null) {
            fs = FrameSequencesMixer.mixTwoSequences(fs, fsShoulderLeft, Constants.FRAME_PER_SECOND);
        }
        if (fsShoulderRight != null) {
            fs = FrameSequencesMixer.mixTwoSequences(fs, fsShoulderRight, Constants.FRAME_PER_SECOND);
        }


        _be.updateFrameList(fs);
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId, Mode mode) {
        // TODO : Mode management in progress
        performKeyframes(keyframes, requestId);
    }

    /// for gestures
    /**
     * *
     *
     * @param left
     * @param right
     * @param timeThreshold
     * @return generate GesturesAnimation
     */
    FrameSequence generateGestureSequence(LinkedList<Arm> left, LinkedList<Arm> right, double timeThreshold) {
        LinkedList<Body> keyframegroupList = new LinkedList<Body>();
        for (Arm a : left) {
            Body g = getBodys(keyframegroupList, a.getTime(), timeThreshold);
            g.setLeftArm(a);
        }
        for (Arm a : right) {
            Body g = getBodys(keyframegroupList, a.getTime(), timeThreshold);
            g.setRightArm(a);
        }

        Collections.sort(keyframegroupList, new Comparator<Body>() {

            @Override
            public int compare(Body o1, Body o2) {
                return (int) Math.signum(o1.getTime() - o2.getTime());
            }
        });

        if (keyframegroupList.size() < 1) {
            return null;
        }

        ArrayList<Frame> frames = new ArrayList<Frame>();
        Frame previous = null;
        double preT = 0;
        Frame following = null;
        double floT = 0;
        PositionFrame pframe = null;
        for (Body body : keyframegroupList) {
            if (previous == null) {
                previous = new Frame();
                preT = body.getTime();
                pframe = new PositionFrame();
            } else {
                ArrayList<Frame> current = body.computeSequence(_cb, pframe, previous);
                pframe = _cb.getFinalPositionFrame();
                frames.addAll(generateFrameSequence(current, preT, body.getTime(), Constants.FRAME_PER_SECOND, null));
                //System.out.println(preT + "-->"+ body.getTime() + "  framenb: "+ current.size());
                if(current.size() > 1){
                    previous = current.get(current.size() - 1);
                    preT = body.getTime();
                }
            }
        }

        FrameSequence fs = new FrameSequence(keyframegroupList.get(0).getTime(), keyframegroupList.get(keyframegroupList.size() - 1).getTime(), frames);

        return fs;
    }

    static Body getBodys(List<Body> glist, double time, double threshold) {
        for (Body g : glist) {
            if (java.lang.Math.abs(g.getTime() - time) < threshold) {
                return g;
            }
        }
        Body g = new Body(time);
        glist.add(g);
        return g;
    }

    ArrayList<Frame> generateFrameSequence(ArrayList<Frame> frames, double start, double end, int framePerSecond, Function function) {
        if(frames.size() < 2){
            return frames;
        }
        ArrayList<Frame> fs = new ArrayList<Frame>();
        double duration = end - start;
        int fNb = (int) (duration * framePerSecond);
        for (int idx = 1; idx <= fNb; ++idx) {
            Frame f = new Frame();
            double r = (double) idx / (double) fNb;
            if (function != null) {
                r = (double) function.f(r);
            }
            int previous = (int) ((frames.size() - 1) * r);
            if(previous == frames.size() - 1){
                fs.add(frames.get(frames.size() - 1).clone());
            }else{
                double ratio = (frames.size() - 1) * r - previous;
                int following = previous + 1;
                f.interpolation(frames.get(previous), frames.get(following), ratio);
                fs.add(f.clone());
            }
        }
        //System.out.println("fnb:" + fs.size());
        return fs;
    }


    /// for head
    FrameSequence generateHeadSequence(LinkedList<Head> head, int framePerSecond, Function function) {
        if (head.size() < 1) {
            //Logs.debug("class: AnimationKeyframePerformer: less than 1 keyframes for head");
            //System.out.println("class: AnimationKeyframePerformer: less than 1 keyframes for head");
            return null;
        } else if (head.size() == 1) {
            FrameSequence fs = new FrameSequence();
            fs.setStartTime(head.get(0).getTime());
            fs.setEndTime(head.get(0).getTime());
            fs.add(head.get(0));
            return fs;
        }
        FrameSequence fs = new FrameSequence();
        fs.setStartTime(head.get(0).getTime());
        fs.setEndTime(head.get(head.size() - 1).getTime());
        for (int i = 0; i < head.size() - 1; i++) {
            Frame f0 = head.get(i);
            Frame f1 = head.get(i + 1);
            double time0 = head.get(i).getTime();
            double time1 = head.get(i + 1).getTime();
            double duration = time1 - time0;
            int fNb = (int) (duration * framePerSecond);
            for (int idx = 0; idx < fNb; ++idx) {
                Frame f = new Frame();
                double r = (double) idx / (double) fNb;
                if (function != null) {
                    r = (double) function.f(r);
                }
                f.interpolation(f0, f1, r);
                fs.add(f);
            }
        }
        return fs;
    }

    /// for torso
    FrameSequence generateTorsoSequence(LinkedList<Torso> torso, int framePerSecond, Function function) {
        if (torso.size() < 1) {
            //Logs.debug("class: AnimationKeyframePerformer: less than 1 keyframes for torso");
            //System.out.println("class: AnimationKeyframePerformer: less than 1 keyframes for torso");
            return null;
        } else if (torso.size() == 1) {
            FrameSequence fs = new FrameSequence();
            fs.setStartTime(torso.get(0).getTime());
            fs.setEndTime(torso.get(0).getTime());
            fs.add(torso.get(0));
            return fs;
        }
        FrameSequence fs = new FrameSequence();
        fs.setStartTime(torso.get(0).getTime());
        fs.setEndTime(torso.get(torso.size() - 1).getTime());
        for (int i = 0; i < torso.size() - 1; i++) {
            Frame f0 = torso.get(i);
            Frame f1 = torso.get(i + 1);
            double time0 = torso.get(i).getTime();
            double time1 = torso.get(i + 1).getTime();
            double duration = time1 - time0;
            int fNb = (int) (duration * framePerSecond);
            for (int idx = 0; idx < fNb; ++idx) {
                Frame f = new Frame();
                double r = (double) idx / (double) fNb;
                if (function != null) {
                    r = (double) function.f(r);
                }
                f.interpolation(f0, f1, r);
                fs.add(f);
            }
        }
        return fs;
    }

    FrameSequence generateShoulderSequence(LinkedList<Shoulder> shoulder, int framePerSecond, Function function) {
        if (shoulder.size() < 1) {
            //Logs.debug("class: AnimationKeyframePerformer: less than 1 keyframes for shoulder");
            //System.out.println("class: AnimationKeyframePerformer: less than 1 keyframes for shoulder");
            return null;
        } else if (shoulder.size() == 1) {
            FrameSequence fs = new FrameSequence();
            fs.setStartTime(shoulder.get(0).getTime());
            fs.setEndTime(shoulder.get(0).getTime());
            fs.add(shoulder.get(0));
            return fs;
        }
        FrameSequence fs = new FrameSequence();
        fs.setStartTime(shoulder.get(0).getTime());
        fs.setEndTime(shoulder.get(shoulder.size() - 1).getTime());
        for (int i = 0; i < shoulder.size() - 1; i++) {
            Frame f0 = shoulder.get(i);
            Frame f1 = shoulder.get(i + 1);
            double time0 = shoulder.get(i).getTime();
            double time1 = shoulder.get(i + 1).getTime();
            double duration = time1 - time0;
            int fNb = (int) (duration * framePerSecond);
            for (int idx = 0; idx < fNb; ++idx) {
                Frame f = new Frame();
                double r = (double) idx / (double) fNb;
                if (function != null) {
                    r = (double) function.f(r);
                }
                f.interpolation(f0, f1, r);
                fs.add(f1);
            }
        }
        return fs;
    }

    @Override
    public void addBAPFramePerformer(BAPFramePerformer bapfp) {
        _be.addBAPFramePerformer(bapfp);
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer bapfp) {
        _be.removeBAPFramePerformer(bapfp);
    }
}
