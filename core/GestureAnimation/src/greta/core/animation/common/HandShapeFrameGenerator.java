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
package greta.core.animation.common;

import greta.core.animation.common.Frame.JointFrame;
import greta.core.animation.common.Frame.KeyFrame;
import greta.core.util.math.Quaternion;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */
public class HandShapeFrameGenerator extends Thread {

    ArrayList<FramesReceiver> _framesController = new ArrayList<FramesReceiver>();
    ArrayList<KeyFrame> _leftHand = new ArrayList<KeyFrame>();
    ArrayList<KeyFrame> _rightHand = new ArrayList<KeyFrame>();
    private boolean requestStop = false;

    @Override
    public void run() {
        while (!requestStop) {

            try {
                sleep(5);
            } catch (Exception ex) {
            }  //
            synchronized (_leftHand) {
                if (_leftHand.size() > 1) {
                    KeyFrame info0 = _leftHand.get(0);
                    KeyFrame info1 = _leftHand.get(1);
                    double t0 = info0.getTime();
                    double t1 = info1.getTime();
                    double time_dure = t1 - t0;
                    int frames = (int) (time_dure * KeyFrame.getFramePerSecond());
                    HashMap<String, JointFrame> r0 = info0.getJointFrames();
                    HashMap<String, JointFrame> r1 = info1.getJointFrames();


                    ArrayList<KeyFrame> currentFrames = new ArrayList<KeyFrame>();
                    for (int j = 0; j < frames; ++j) {
                        double currenttime = (double) (t0 + (double) j / (double) KeyFrame.getFramePerSecond());
                        KeyFrame current = new KeyFrame(currenttime);
                        for (String name : r0.keySet()) {
                            JointFrame q0 = r0.get(name);
                            JointFrame q1 = r1.get(name);
                            Quaternion currentRotationJoint = Quaternion.slerp(q0._localrotation, q1._localrotation, (currenttime - t0) / (t1 - t0), true);
                            JointFrame jf = new JointFrame();
                            jf._localrotation = currentRotationJoint;
                            current.addJointFrame(name, jf);
                        }
                        currentFrames.add(current);
                    }
                    _leftHand.remove(0);
                    sendFrames(currentFrames, "");
                }
            }

            synchronized (_rightHand) {
                if (_rightHand.size() > 1) {

                    KeyFrame info0 = _rightHand.get(0);
                    KeyFrame info1 = _rightHand.get(1);
                    double t0 = info0.getTime();
                    double t1 = info1.getTime();
                    double time_dure = t1 - t0;
                    int frames = (int) (time_dure * KeyFrame.getFramePerSecond());
                    HashMap<String, JointFrame> r0 = info0.getJointFrames();
                    HashMap<String, JointFrame> r1 = info1.getJointFrames();

                    ArrayList<KeyFrame> currentFrames = new ArrayList<KeyFrame>();
                    for (int j = 0; j < frames; ++j) {
                        double currenttime = (double) (t0 + (double) j / (double) KeyFrame.getFramePerSecond());
                        KeyFrame current = new KeyFrame(currenttime);
                        for (String name : r0.keySet()) {
                            JointFrame q0 = r0.get(name);
                            JointFrame q1 = r1.get(name);
                            Quaternion currentRotationJoint = Quaternion.slerp(q0._localrotation, q1._localrotation, (currenttime - t0) / (t1 - t0), true);
                            JointFrame jf = new JointFrame();
                            jf._localrotation = currentRotationJoint;
                            current.addJointFrame(name, jf);
                        }
                        currentFrames.add(current);
                    }
                    _rightHand.remove(0);
                    sendFrames(currentFrames, "");
                }
            }
        }
    }

    public void updateHandShape(ArrayList<KeyFrame> left, ArrayList<KeyFrame> right) {
        synchronized (_leftHand) {
            _leftHand.addAll(left);
        }
        synchronized (_rightHand) {
            _rightHand.addAll(right);
        }
    }

    public void addFramesReceiver(FramesReceiver frameRceiver) {
        _framesController.add(frameRceiver);
    }

    public void sendFrames(ArrayList<KeyFrame> frames, String requestId) {
        for (FramesReceiver fr : _framesController) {
            fr.updateFramesInfoList(frames, requestId);
        }
        //_framesController.updateSendDirect(frames, requestId);
    }

    public void requestStop() {
        requestStop = true;
    }
}
