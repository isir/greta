/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.bap;

/**
 *
 * @author Jing Huang
 */
public interface BAPFramesEmitter {
    void addBAPFramesPerformer(BAPFramesPerformer performer);
    void removeBAPFramesPerformer(BAPFramesPerformer performer);
}
