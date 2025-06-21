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
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPParser;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import java.io.File;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class BAPFileReader extends BAPFrameEmitterImpl{

    private BAPParser parser = new BAPParser();

    public void load(String bapFileName) {
        List<BAPFrame> bap_animation = parser.readFromFile(bapFileName, true);

        if (!bap_animation.isEmpty()) {
            String base = (new File(bapFileName)).getName().replaceAll("\\.bap$", "");
            ID id = IDProvider.createID(base);

            //send to all BAPPerformer added
            sendBAPFrames(id, bap_animation);
        }
    }

    public java.io.FileFilter getFileFilter() {
        return new java.io.FileFilter() {
            @Override
            public boolean accept(File pathName) {
                return pathName.getName().toLowerCase().endsWith(".bap");
            }
        };
    }

}
