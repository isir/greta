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
package greta.auxiliary.emotionml;

import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mathieu Chollet
 */
public class EmotionMLFileReader implements IntentionEmitter {

    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    private XMLParser emotionmlparser = XML.createParser();
    private static String markup = "emotionml";

    @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        performers.add(performer);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }

    /**
     * Returns a {@code java.io.FileFilter} corresponding to EML Files.
     *
     * @return a {@code java.io.FileFilter} corresponding to EML Files
     */
    public java.io.FileFilter getFileFilter() {
        return new java.io.FileFilter() {
            @Override
            public boolean accept(File pathName) {
                String fileName = pathName.getName().toLowerCase();
                if (fileName.endsWith(".xml") || fileName.endsWith(".eml")) {
                    try {
                        emotionmlparser.setValidating(false);
                        return emotionmlparser.parseFile(pathName.getAbsolutePath()).getName().equalsIgnoreCase(markup);
                    } catch (Exception e) {
                    }
                }
                return false;
            }
        };
    }

    /**
     * Loads an EmotionML file.<br/> The communicative intentions in the
     * specified file will be send to all {@code IntentionPerformer} added with
     * the
     * {@link #addIntentionPerformer(greta.core.intentions.IntentionPerformer) add}
     * function.<br/> The base file name of the EmotionML file is used as
     * {@code requestId} parameter when calling the
     * {@link greta.core.intentions.IntentionPerformer#performIntentions(java.util.List, greta.core.util.id.ID, greta.core.util.Mode) performIntentions}
     * function.
     *
     * @param emotionMLfileName the name of the file to load
     */
    public void load(String emotionMLfileName) {
        //get the base file name to use it as requestId
        String base = (new File(emotionMLfileName)).getName().replaceAll("\\.xml$", "");

        //get the signals of the BML file
        emotionmlparser.setValidating(true);
        XMLTree emotionml = emotionmlparser.parseFile(emotionMLfileName);
        List<Intention> intentions = EmotionMLTranslator.EmotionMLToIntentions(emotionml);
        //send to all SignalPerformer added
        ID id = IDProvider.createID(base);
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, FMLTranslator.getDefaultFMLMode());
        }
    }
}
