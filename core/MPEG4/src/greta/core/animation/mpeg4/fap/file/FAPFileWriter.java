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
package greta.core.animation.mpeg4.fap.file;

import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.util.Constants;
import greta.core.util.id.ID;
import greta.core.util.log.Logs;
import java.util.List;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class FAPFileWriter implements greta.core.animation.mpeg4.fap.FAPFramePerformer {

    private String path;

    public FAPFileWriter() {
        path = "";
    }

    public FAPFileWriter(String path) {
        this.path = path;
    }

    @Override
    public void performFAPFrames(List<FAPFrame> fapframes, ID requestId) {
        String name = (path + requestId + ".fap");
        try {

            java.io.FileWriter fos = new java.io.FileWriter(name);

            String first_line =  "2.1 "+ name + " "+Constants.FRAME_PER_SECOND+" "+ fapframes.size()+"\n";
            fos.write(first_line);

            int first = fapframes.get(0).getFrameNumber();
            for(FAPFrame frame : fapframes) {
                fos.write(frame.AnimationParametersFrame2String(frame.getFrameNumber() - first));
            }

            fos.close();
        } catch (Exception ignored2) {
            Logs.warning("Error saving file: " + ignored2);
        }

    }//end of perorm

    @Override
    public void performFAPFrame(FAPFrame fapf, ID string) {

    }
}
