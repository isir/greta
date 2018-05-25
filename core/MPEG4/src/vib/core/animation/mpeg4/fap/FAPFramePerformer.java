/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.fap;
import vib.core.util.id.ID;
import java.util.List;


/**
 *
 * @author Radoslaw Niewiadomski
 */


public interface FAPFramePerformer {

    public void performFAPFrames(List<FAPFrame> fap_animation, ID requestId);

    public void performFAPFrame(FAPFrame fap_anim, ID requestId);

}



