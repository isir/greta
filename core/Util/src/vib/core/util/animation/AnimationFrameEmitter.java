/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.animation;

/**
 *
 * @author Andre-Marie Pez
 */
public interface AnimationFrameEmitter {
    void addAnimationFramePerformer(AnimationFramePerformer performer);
    void removeAnimationFramePerformer(AnimationFramePerformer performer);
}
