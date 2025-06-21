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
package greta.core.util.animation;

import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class AnimationFrameEmitterImpl implements AnimationFrameEmitter{

    private ArrayList<AnimationFramePerformer> performers = new ArrayList<AnimationFramePerformer>();

    @Override
    public void addAnimationFramePerformer(AnimationFramePerformer performer) {
        if (performer != null) {
            System.out.println(" ANIMATION ENVOYEEEEE ????");
            performers.add(performer);
        }
    }

    @Override
    public void removeAnimationFramePerformer(AnimationFramePerformer performer) {
        if (performer != null) {
            performers.remove(performer);
        }
    }

    public void sendAnimationFrames(ID requestId, AnimationFrame... frames){
        sendAnimationFrames(requestId, Arrays.asList(frames));
    }


    public void sendAnimationFrames(ID requestId, List<AnimationFrame> frames){
        for(AnimationFramePerformer performer : performers){
            
            performer.performAnimationFrames(frames, requestId);
        }
    }

    public void sendAnimationFrame(ID requestId, AnimationFrame frame){
        sendAnimationFrames(requestId, frame);
    }

}
