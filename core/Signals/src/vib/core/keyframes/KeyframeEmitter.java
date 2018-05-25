/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes;

/**
 *
 * @author Quoc Anh Le
 */
public interface KeyframeEmitter {
    public void addKeyframePerformer(KeyframePerformer performer);
    public void removeKeyframePerformer(KeyframePerformer performer);

}
