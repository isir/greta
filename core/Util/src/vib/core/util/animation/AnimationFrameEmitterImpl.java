/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import vib.core.util.id.ID;

/**
 *
 * @author Andre-Marie Pez
 */
public class AnimationFrameEmitterImpl implements AnimationFrameEmitter{

    private ArrayList<AnimationFramePerformer> performers = new ArrayList<AnimationFramePerformer>();

    @Override
    public void addAnimationFramePerformer(AnimationFramePerformer performer) {
        if (performer != null) {
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
