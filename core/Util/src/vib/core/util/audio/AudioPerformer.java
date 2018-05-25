/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.audio;

import vib.core.util.Mode;
import vib.core.util.id.ID;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public interface AudioPerformer {
    public void performAudios(List<Audio> audios, ID requestId, Mode mode);
}
