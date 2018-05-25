/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.ssi;

import vib.core.util.id.ID;
import java.util.List;

/**
 *
 * @author Angelo Cafaro
 */
public interface SSIFramePerfomer {

    public void performSSIFrames(List<SSIFrame> ssi_frames_list, ID requestId);

    public void performSSIFrame(SSIFrame ssi_frame, ID requestId);

}
