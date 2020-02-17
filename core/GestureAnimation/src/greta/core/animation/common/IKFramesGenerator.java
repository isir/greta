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

import greta.core.animation.common.Frame.ExtendedKeyFrame;
import greta.core.animation.common.Frame.KeyFrame;
import greta.core.animation.common.TimeFunctions.TimeFunctionController;
import greta.core.animation.common.body.Arm;
import greta.core.animation.common.body.Head;
import greta.core.animation.common.body.Shoulder;
import greta.core.animation.common.body.Torse;
import greta.core.animation.common.symbolic.EaseTimeFunction;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Jing Huang
 */
public class IKFramesGenerator extends Thread {

    ArrayList<ExtendedKeyFrame> _left = new ArrayList<ExtendedKeyFrame>();
    ArrayList<ExtendedKeyFrame> _right = new ArrayList<ExtendedKeyFrame>();
    ArrayList<ExtendedKeyFrame> _leftShoulder = new ArrayList<ExtendedKeyFrame>();
    ArrayList<ExtendedKeyFrame> _rightShoulder = new ArrayList<ExtendedKeyFrame>();
    ArrayList<ExtendedKeyFrame> _torso = new ArrayList<ExtendedKeyFrame>();
    ArrayList<ExtendedKeyFrame> _head = new ArrayList<ExtendedKeyFrame>();
    ArrayList<FramesReceiver> _framesController = new ArrayList<FramesReceiver>();
    private boolean requestStop = false;
    int _numberOfInterpolation = 100;
    TimeFunctionController _timefunctionController = new TimeFunctionController();
    Random rand = new Random();
    double seconds = 0.5;
    double start = -1;
    boolean _smoothness = false;   //if need the filter for fluity of motion, bit weird
    double _valueSmoothness = 0.5;
    private static final Object mutex = new Object();

    public IKFramesGenerator() {
    }

    public void addFramesReceiver(FramesReceiver frameRceiver) {
        _framesController.add(frameRceiver);
    }

    //int nnnn = 0;
    @Override
    public void run() {
        while (!requestStop) {

            try {
                sleep(5);
            } catch (Exception ex) {
            }  //
            synchronized (mutex) {
                int nbLeft = _left.size();
                int nbRight = _right.size();
                int nbTorso = _torso.size();
                int nbHead = _head.size();
                int nbLS = _leftShoulder.size();
                int nbRS = _rightShoulder.size();
                if (start == Double.POSITIVE_INFINITY) {

                    if (nbLeft > 0) {
                        start = _left.get(0).getTime();
                    }
                    if (nbRight > 0) {
                        start = Math.min(start, _right.get(0).getTime());
                    }
                    if (nbTorso > 0) {
                        start = Math.min(start, _torso.get(0).getTime());
                    }
                    if (nbHead > 0) {
                        start = Math.min(start, _head.get(0).getTime());
                    }
                    if (nbLS > 0) {
                        start = Math.min(start, _leftShoulder.get(0).getTime());
                    }
                    if (nbRS > 0) {
                        start = Math.min(start, _rightShoulder.get(0).getTime());
                    }
                }
                if (start != Double.POSITIVE_INFINITY && (nbLeft > 0 || nbRight > 0 || nbTorso > 0 || nbHead > 0 || nbLS > 0 || nbRS > 0)) {
                    //System.out.println(start);
                    interpolation((double) start, (double) (start + seconds));
                } else {
                    start = Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    /**
     *
     * @param keyframes accepte "Symblic keyframes" from KeyframePerformer
     * @param mode define how to insert the list into runtime computation list
     * in the class
     */
    public void updateKeyFrameList(List<Arm> left, List<Arm> right, List<Torse> torso, List<Head> head, List<Shoulder> leftS, List<Shoulder> rightS, int mode, ID requestId) {
        synchronized (mutex) {
            if (mode == 0) {
                _left.addAll(left);
                Collections.sort(_left, new Comparator<KeyFrame>() {

                    @Override
                    public int compare(KeyFrame t, KeyFrame t1) {
                        return (int) Math.signum(t.getTime() - t1.getTime());
                    }
                });
                _right.addAll(right);
                Collections.sort(_right, new Comparator<KeyFrame>() {

                    @Override
                    public int compare(KeyFrame t, KeyFrame t1) {
                        return (int) Math.signum(t.getTime() - t1.getTime());
                    }
                });

                _leftShoulder.addAll(leftS);
                Collections.sort(_leftShoulder, new Comparator<KeyFrame>() {

                    @Override
                    public int compare(KeyFrame t, KeyFrame t1) {
                        return (int) Math.signum(t.getTime() - t1.getTime());
                    }
                });
                _rightShoulder.addAll(rightS);
                Collections.sort(_rightShoulder, new Comparator<KeyFrame>() {

                    @Override
                    public int compare(KeyFrame t, KeyFrame t1) {
                        return (int) Math.signum(t.getTime() - t1.getTime());
                    }
                });
                _torso.addAll(torso);
                Collections.sort(_torso, new Comparator<KeyFrame>() {

                    @Override
                    public int compare(KeyFrame t, KeyFrame t1) {
                        return (int) Math.signum(t.getTime() - t1.getTime());
                    }
                });
                _head.addAll(head);
                Collections.sort(_head, new Comparator<KeyFrame>() {

                    @Override
                    public int compare(KeyFrame t, KeyFrame t1) {
                        return (int) Math.signum(t.getTime() - t1.getTime());
                    }
                });
            }
            if (_smoothness) {
                smoothKeyFrame(_left);
                smoothKeyFrame(_right);
            }
        }
    }

    void smoothKeyFrame(ArrayList<ExtendedKeyFrame> keyframe){
        for(int i = 1; i < keyframe.size() - 1; ++i){
            ExtendedKeyFrame k = keyframe.get(i);
            ExtendedKeyFrame pre = keyframe.get(i- 1);
            ExtendedKeyFrame next = keyframe.get(i + 1);
            double weight = (k.getTime() - pre.getTime()) /(next.getTime() - pre.getTime()) ;
            ExtendedKeyFrame keymiddle = new ExtendedKeyFrame(k.getTime());
            keymiddle.interpolation(pre, next, weight);
            k.interpolation(k, keymiddle, (double)k.getExpressivityParameters().fld * 0.2f);
        }
    }

    void interpolation(double starttime, double endtime) {
        //starttime = (double) Math.max(starttime, Timer.getTime());
        //System.out.println("now time " + Timer.getTime());
        //System.out.println("starttime " + starttime + " frames:" + endtime);
        double time_dure = endtime - starttime;
        int frames = (int) (time_dure * KeyFrame.getFramePerSecond());
        ArrayList<KeyFrame> currentFrames = new ArrayList<KeyFrame>();
        for (int loop = 0; loop < frames; loop++) {
            double tNow = starttime + (double) loop / (double) (KeyFrame.getFramePerSecond());
            KeyFrame arml = getCurrentKeyFrame(_left, tNow);
            KeyFrame armr = getCurrentKeyFrame(_right, tNow);
            KeyFrame head = getCurrentKeyFrame(_head, tNow);
            KeyFrame torso = getCurrentKeyFrame(_torso, tNow);
            KeyFrame sl = getCurrentKeyFrame(_leftShoulder, tNow);
            KeyFrame sr = getCurrentKeyFrame(_rightShoulder, tNow);
            KeyFrame ky = new KeyFrame(tNow);
            if (arml != null) {
                ky.addJointFrames(arml.getJointFrames());
            }
            if (armr != null) {
                ky.addJointFrames(armr.getJointFrames());
            }
            if (sl != null) {
                ky.addJointFrames(sl.getJointFrames());
            }
            if (sr != null) {
                ky.addJointFrames(sr.getJointFrames());
            }
            if (head != null) {
                ky.addJointFrames(head.getJointFrames());
            }
            if (torso != null) {
                ky.addJointFrames(torso.getJointFrames());
            }

            currentFrames.add(ky);
        }

        start = endtime;
        sendFrames(currentFrames, "request");
    }

    /**
     *
     * @param frames container the rotation info : quaternions
     */
    public void sendFrames(ArrayList<KeyFrame> frames, String requestId) {
        for (FramesReceiver fr : _framesController) {
            fr.updateFramesInfoList(frames, requestId);
        }
        //_framesController.updateSendDirect(frames, requestId);
    }

    public void requestStop() {
        requestStop = true;
    }

    KeyFrame getCurrentKeyFrame(ArrayList<ExtendedKeyFrame> kframes, double time) {
        ExtendedKeyFrame previous = null;
        ExtendedKeyFrame current = null;

        boolean has = true;
        while (has) {
            if (kframes.size() == 0) {
                return null;
            } else if (kframes.size() == 1) {
                current = kframes.get(0);
                if (current.getTime() > time) {
                    kframes.clear();
                    return current;
                } else {
                    //TODO  //
                    current.setTime(time);
                    kframes.clear();
                    return current;
                }

            } else {
                previous = kframes.get(0);
                current = kframes.get(1);
                if (previous.getTime() > time) {
                    return null;
                }
                if (current.getTime() < time) {
                    kframes.remove(0);
                } else {
                    KeyFrame kf = new KeyFrame(time);
                    double weight = (time - previous.getTime()) / (current.getTime() - previous.getTime());
                    if (current.getExpressivityParameters() != null) {
                        double tensionvalue = generateTensionSamplers(current.getExpressivityParameters().tension, 0.1, weight, current.getTime() - previous.getTime());
                        weight = EaseTimeFunction.getInstance().getTime(weight, current.getExpressivityParameters(), current.getFunction()) + tensionvalue;
                    } else {
                    }
                    kf.interpolation(previous, current, weight);
                    return kf;
                }
            }
        }
        return null;
    }

    double generateTensionSamplers(double tension, double scale, double time, double duration) {
        if (tension == 0) {
            return 0;
        }
        //double a = (double) (rand.nextDouble() * scale * tension);
        double a = (double) ((double) scale * tension * Math.sin((int) (113 * duration) * Math.PI * time) * Math.sin(time * 1000.0f));
        return a;
    }
}
