/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.animation;

import java.util.List;
import vib.core.util.id.ID;

/**
 *
 * @author Andre-Marie Pez
 */
public interface AnimationFramePerformer {

    void performAnimationFrames(List<AnimationFrame> frames, ID requestId);
}
