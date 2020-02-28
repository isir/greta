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
