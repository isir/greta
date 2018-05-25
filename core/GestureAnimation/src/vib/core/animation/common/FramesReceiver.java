/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common;

import vib.core.animation.common.Frame.KeyFrame;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public interface FramesReceiver {
    public void updateFramesInfoList(ArrayList<KeyFrame> frames, String requestId);
    public void reset();

}
