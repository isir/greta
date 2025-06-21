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
package greta.core.animation.mpeg4.bap.file;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.util.Constants;
import greta.core.util.IniManager;
import greta.core.util.id.ID;
import greta.core.util.log.Logs;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 * @author Ken Prepin
 */
public class BAPFileWriter implements greta.core.animation.mpeg4.bap.BAPFramePerformer {

    private boolean sequencialWriting = true;
    private String path;
    private int firstForUnique = 0;
    private String uniqueFileName = "unique";
    private List<ID> ids = new ArrayList<ID>();

    public BAPFileWriter() {
        path = IniManager.getProgramPath();
    }

    public BAPFileWriter(String path) {
        this.path = path;
    }

    public void setSequencial(boolean sequencial) {
        sequencialWriting = sequencial;
    }

    public void setUniqueFileName(String fileName) {
        if (!fileName.toLowerCase().endsWith(".bap")) {
            fileName += ".bap";
        }
        sequencialWriting = false;
        uniqueFileName = fileName;
        ids = new ArrayList<ID>();
    }

    @Override
    public void performBAPFrames(List<BAPFrame> bapframes, ID requestId) {
        if (sequencialWriting) {
            writeSequencial(bapframes, requestId);
        } else {
            writeUniqueFile(bapframes, requestId);
        }
    }

    private void writeSequencial(List<BAPFrame> bapframes, ID requestId) {
        String fileName = (path + "/" + requestId + ".bap");
        writeFile(bapframes, (int) (requestId.getTime() / Constants.FRAME_DURATION_MILLIS), fileName, requestId.toString());
    }

    public void writeSequencial(List<BAPFrame> bapframes, String name) {
        String fileName = (path + "/" + name + ".bap");
        writeFile(bapframes, 0, fileName, name);
    }

    private void writeUniqueFile(List<BAPFrame> bapframes, ID requestId) {
        String fileName = path + "/" + uniqueFileName;

        if (!(new File(fileName).exists())) {
            firstForUnique = (int) (requestId.getTime() / Constants.FRAME_DURATION_MILLIS);
        }

        if (!ids.contains(requestId)) {
            ids.add(requestId);
        }

        String completeID = "";
        for (ID id : ids) {
            if (!completeID.isEmpty()) {
                completeID += "+";
            }
            completeID += id;
        }

        writeFile(bapframes, firstForUnique, fileName, completeID);
    }

    private void writeFile(List<BAPFrame> bapframes, int firstNum, String fileName, String animName) {
        try {
            java.io.FileWriter fos;
            if (new File(fileName).exists()) {
                fos = new java.io.FileWriter(fileName, true);

                //TODO rewrite header

            } else {
                fos = new java.io.FileWriter(fileName);
                String first_line = "2.1 " + animName + " " + Constants.FRAME_PER_SECOND + " " + bapframes.size() + "\n"; // is it good?
                fos.write(first_line);
            }

            for (BAPFrame bapframe : bapframes) {
                fos.write(bapframe.AnimationParametersFrame2String(bapframe.getFrameNumber() - firstNum));
            }

            fos.close();
        } catch (Exception ignored2) {
            Logs.warning("Error saving file: " + ignored2);
        }
    }
}
