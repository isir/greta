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
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import vib.core.animation.mpeg4.bap.BAPFrame;
import vib.core.animation.mpeg4.bap.BAPFramesPerformer;
import vib.core.util.id.ID;
import java.util.List;



/**
 *
 * @author Ken Prepin
 */
public class BAPSender extends APSender implements BAPFramesPerformer{

    public BAPSender(){

    }
    public BAPSender(String host, int port){
        super(host, port);
    }

    @Override
    public void performBAPFrames(List<BAPFrame> bapframes, ID requestId) {
       sendAnimParamFrameList(bapframes, "BAPFrames", requestId);
    }
}
