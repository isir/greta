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
package greta.core.animation.performer;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Brian Ravenet
 */
public class BodyAnimationArmNoise implements BAPFrameEmitter, BAPFramePerformer {

    private final List<BAPFramePerformer> bapFramePerformers = new ArrayList<BAPFramePerformer>();
    private double leftShoulderTimer = 0;
    private double rightShoulderTimer = 0;
    private double leftElbowFlexTimer = 0;
    private double rightElbowFlexTimer = 0;
    private double leftElbowTwistTimer = 0;
    private double rightElbowTwistTimer = 0;

    public BodyAnimationArmNoise() {

    }

    @Override
    public void addBAPFramePerformer(BAPFramePerformer bapFramePerformer) {
        if (bapFramePerformer != null) {
            bapFramePerformers.add(bapFramePerformer);
        }
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer bapFramePerformer) {
        if (bapFramePerformer != null) {
            bapFramePerformers.remove(bapFramePerformer);
        }
    }

    @Override
    public void performBAPFrames(List<BAPFrame> list, ID id) {
        //
        Random r = new Random();
        for (BAPFrame bf : list) {
            leftShoulderTimer += r.nextDouble() / 10;
            bf.setValue(BAPType.l_shoulder_flexion, (int) (bf.getValue(BAPType.l_shoulder_flexion) * (1 + Math.sin(leftShoulderTimer) / 40)));
            rightShoulderTimer += r.nextDouble() / 10;
            bf.setValue(BAPType.r_shoulder_flexion, (int) (bf.getValue(BAPType.r_shoulder_flexion) * (1 + Math.sin(rightShoulderTimer) / 40)));
            leftElbowFlexTimer += r.nextDouble() / 10;
            bf.setValue(BAPType.l_elbow_flexion, (int) (bf.getValue(BAPType.l_elbow_flexion) * (1 + Math.sin(leftElbowFlexTimer) / 40)));
            rightElbowFlexTimer += r.nextDouble() / 10;
            bf.setValue(BAPType.r_elbow_flexion, (int) (bf.getValue(BAPType.r_elbow_flexion) * (1 + Math.sin(rightElbowFlexTimer) / 40)));
            leftElbowTwistTimer += r.nextDouble() / 10;
            bf.setValue(BAPType.l_elbow_twisting, (int) (bf.getValue(BAPType.l_elbow_twisting) * (1 + Math.sin(leftElbowTwistTimer) / 40)));
            rightElbowTwistTimer += r.nextDouble() / 10;
            bf.setValue(BAPType.r_elbow_twisting, (int) (bf.getValue(BAPType.r_elbow_twisting) * (1 + Math.sin(rightElbowTwistTimer) / 40)));

            // leftShoulderFlex = leftShoulderFlex * ((Math.sin(leftElbowFlex)+1)/2);
        }

        for (BAPFramePerformer bfp : bapFramePerformers) {
            bfp.performBAPFrames(list, id);
        }//To change body of generated methods, choose Tools | Templates.
    }

    // Function to linearly interpolate between a0 and a1
    // Weight w should be in the range [0.0, 1.0]
    private double lerp(double a0, double a1, double w) {
        return (1.0 - w) * a0 + w * a1;
    }

}
