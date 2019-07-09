/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.If not, see <http://www.gnu.org/licenses/>.
 *//*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.fap;

import vib.core.util.id.ID;

/**
 * This interface describes a {@code FAPFramePerformer} which's frames are cancelable.
 *
 * @author Nawhal Sayarh
 */
public interface CancelableFAPFramePerformer extends FAPFramePerformer {
    /**
     * Cancels all the {@code FAPFrame} with the given {@code ID} if possible.
     * @param requestId ID of the frames to cancel
     */
    void cancelFAPFramesById (ID requestId);
}
