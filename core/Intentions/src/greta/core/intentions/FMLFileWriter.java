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
package greta.core.intentions;

import greta.core.util.IniManager;
import greta.core.signals.Signal;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import java.util.List;

/**
 * This class is an implementation of {@code IntentionPerformer} interface.<br>
 * When {@code performIntentions} function is called, the {@code Intentions}
 * received are saved in a file in FML format.
 *
 * @author Andre-Marie Pez
 */
public class FMLFileWriter implements IntentionPerformer {

    private String fmlfolder;

    /**
     * Constructs an {@code FMLFileWriter} where the FML files will be saved in
     * the directory defined in the global {@code IniManager} as
     * "FML_FOLDER".<br> If this parameter is not set (or empty) the files will
     * be saved in the execution path of the program.
     */
    public FMLFileWriter() {
        this(IniManager.getGlobals().getValueString("FML_FOLDER"));
    }

    /**
     * Constructs an {@code FMLFileWriter} where the FML files will be saved in
     * the specified directory.
     *
     * @param directory the directory where the FML files will be saved
     */
    public FMLFileWriter(String directory) {
        //choose the current folder if directory is null or an empty string
        fmlfolder = directory == null || directory.isEmpty() ? "./" : directory;
        //check slashes (linux comtibility)
        fmlfolder = fmlfolder.replaceAll("\\\\", "/");
        //add one at the end if not present
        fmlfolder += fmlfolder.endsWith("/") ? "" : "/";
    }

    /**
     * Saves a list of {@code Intentions} in a FML file.<br> It will be save in
     * the directory defined by the construtor of this {@code FMLFileWriter},
     * and the name of the file will be the {@code requestId} parameter prefixed
     * by "FML-" and suffixed by the XML extention.
     *
     * @param intentions the list of {@code Intention}
     * @param requestId the identifier of the request
     * @param mode blend, replace, append
     */
    @Override
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode) {
        FMLTranslator.IntentionsToFML(intentions, mode).save(fmlfolder + "FML-" + requestId + ".xml");
    }
    
    @Override
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode, List<Signal> inputSignals) {
    }    
}
