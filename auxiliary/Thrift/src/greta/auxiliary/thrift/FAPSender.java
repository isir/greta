/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
public class FAPSender extends APSender<FAPFrame> implements FAPFramePerformer {

    public FAPSender() {
    }

    public FAPSender(String host, int port) {
        super(host, port);
    }

    @Override
    public void performFAPFrames(List<FAPFrame> fapFrameList, ID idRequest) {
        sendAnimParamFrameList(fapFrameList, "FAPFrames", idRequest);
    }

    @Override
    public void performFAPFrame(FAPFrame fapf, ID idRequest) {
        List<FAPFrame> fapFrameList = new ArrayList<>(1);
        fapFrameList.add(fapf);
        sendAnimParamFrameList(fapFrameList, "FAPFrames", idRequest);
    }
}
