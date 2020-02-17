/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.ssi;

import greta.core.util.id.ID;
import java.util.List;

/**
 *
 * @author Angelo Cafaro
 */
public interface SSIFramePerfomer {

    public void performSSIFrames(List<SSIFrame> ssi_frames_list, ID requestId);

    public void performSSIFrame(SSIFrame ssi_frame, ID requestId);

}
