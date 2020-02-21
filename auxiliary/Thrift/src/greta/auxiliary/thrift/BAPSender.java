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
package greta.auxiliary.thrift;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.util.id.ID;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public class BAPSender extends APSender implements BAPFramePerformer{

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
