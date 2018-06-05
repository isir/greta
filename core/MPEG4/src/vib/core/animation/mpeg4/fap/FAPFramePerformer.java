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
import java.util.List;


/**
 *
 * @author Radoslaw Niewiadomski
 */


public interface FAPFramePerformer {

    public void performFAPFrames(List<FAPFrame> fap_animation, ID requestId);

    public void performFAPFrame(FAPFrame fap_anim, ID requestId);

}



