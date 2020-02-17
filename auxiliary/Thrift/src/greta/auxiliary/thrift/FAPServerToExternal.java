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

import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPFramePerformer;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public class FAPServerToExternal extends APServerToExternal implements FAPFramePerformer {
   public FAPServerToExternal() {
    }

    public FAPServerToExternal(int port) {
        super(port);
    }

    @Override
    public void performFAPFrames(List<FAPFrame> fapFrameList, ID idRequest) {
        updateAnimParamFrameList(fapFrameList, "FAPFrames", idRequest.toString());
    }

    @Override
    public void performFAPFrame(FAPFrame fapf, ID idRequest) {
        List<FAPFrame> fapFrameList = new ArrayList<FAPFrame>(1);
        if(fapFrameList.add(fapf)) {
            updateAnimParamFrameList(fapFrameList, "FAPFrames", idRequest.toString());
        }
    }

}
