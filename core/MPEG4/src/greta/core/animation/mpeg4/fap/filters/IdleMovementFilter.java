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
package greta.core.animation.mpeg4.fap.filters;

import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import greta.core.animation.mpeg4.fap.FAPFramePerformer;
import greta.core.animation.mpeg4.fap.FAPType;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class IdleMovementFilter extends FAPFrameEmitterImpl implements FAPFramePerformer {

    @Override
    public void performFAPFrames(List<FAPFrame> fap_animation, ID requestId) {

        ArrayList<FAPFrame> newlist = new ArrayList<FAPFrame>(fap_animation.size());

        for (FAPFrame frame : fap_animation) {
            newlist.add(new FAPFrame(frame));
        }

        int frameind;
        Random randomGenerator = new Random();
        frameind = randomGenerator.nextInt(40);
        while (frameind < newlist.size() - 8) {
            bioblink(newlist, frameind);
            frameind += 71 + (randomGenerator.nextInt(50)); //  4.8 sec ~= 120 frames
        }

        sendFAPFrames(requestId, newlist);
    }

    private void bioblink(ArrayList<FAPFrame> newlist, int frameind) {


        if ((newlist.get(frameind + 1).getValue(19) > 70)
                || (newlist.get(frameind + 2).getValue(FAPType.close_t_l_eyelid) > 70)
                || (newlist.get(frameind + 3).getValue(FAPType.close_t_l_eyelid) > 70)
                || (newlist.get(frameind + 4).getValue(FAPType.close_t_l_eyelid) > 70)
                || (newlist.get(frameind + 5).getValue(FAPType.close_t_l_eyelid) > 70)
                || (newlist.get(frameind + 6).getValue(FAPType.close_t_l_eyelid) > 70)
                || (newlist.get(frameind + 7).getValue(FAPType.close_t_l_eyelid) > 70)
                || (newlist.get(frameind + 1).getValue(FAPType.close_t_r_eyelid) > 70)
                || (newlist.get(frameind + 2).getValue(FAPType.close_t_r_eyelid) > 70)
                || (newlist.get(frameind + 3).getValue(FAPType.close_t_r_eyelid) > 70)
                || (newlist.get(frameind + 4).getValue(FAPType.close_t_r_eyelid) > 70)
                || (newlist.get(frameind + 5).getValue(FAPType.close_t_r_eyelid) > 70)
                || (newlist.get(frameind + 6).getValue(FAPType.close_t_r_eyelid) > 70)
                || (newlist.get(frameind + 7).getValue(FAPType.close_t_r_eyelid) > 70)) {
            return;
        }

        applyMax(newlist.get(frameind), 0);
        applyMax(newlist.get(frameind + 1), 341);
        applyMax(newlist.get(frameind + 2), 683);
        applyMax(newlist.get(frameind + 3), 1024);
        applyMax(newlist.get(frameind + 4), 1024);
        applyMax(newlist.get(frameind + 5), 683);
        applyMax(newlist.get(frameind + 6), 341);
        applyMax(newlist.get(frameind + 7), 0);
    }

    private void applyMax(FAPFrame frame, int value){
         frame.applyValue(FAPType.close_t_l_eyelid, Math.max(frame.getValue(FAPType.close_t_l_eyelid), value));
         frame.applyValue(FAPType.close_t_r_eyelid, Math.max(frame.getValue(FAPType.close_t_r_eyelid), value));
    }

    @Override
    public void performFAPFrame(FAPFrame fap_anim, ID requestId) {
        sendFAPFrame(requestId, fap_anim);
    }
}
