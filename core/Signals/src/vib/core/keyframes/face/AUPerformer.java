/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.keyframes.face;

import java.util.List;
import vib.core.repositories.AUAPFrame;
import vib.core.util.id.ID;

/**
 *
 * @author Radoslaw Niewiadomski
 */

public interface AUPerformer {

    public void performAUAPFrame(AUAPFrame auapAnimation, ID requestId);

    public void performAUAPFrames(List<AUAPFrame> auapAnimation,  ID requestId);


}
