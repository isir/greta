/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.bap;

import vib.core.util.id.ID;
import java.util.List;

/**
 *
 * @author Jing Huang
 */
public interface BAPFramesPerformer {
    void performBAPFrames(List<BAPFrame> bapframes, ID requestId);
}
