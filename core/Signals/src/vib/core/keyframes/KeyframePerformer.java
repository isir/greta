/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes;

import java.util.List;
import vib.core.util.Mode;
import vib.core.util.id.ID;

/**
 *
 * @author Quoc Anh Le
 */
public interface KeyframePerformer {

    public void performKeyframes(List<Keyframe> keyframes, ID requestId);

    // TODO : Mode management in progress
    public void performKeyframes(List<Keyframe> keyframes, ID requestId, Mode mode);

}
