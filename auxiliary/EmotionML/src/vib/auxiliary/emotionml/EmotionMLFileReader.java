/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.emotionml;

import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import vib.core.intentions.FMLTranslator;
import vib.core.intentions.Intention;
import vib.core.intentions.IntentionEmitter;
import vib.core.intentions.IntentionPerformer;

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
            public boolean accept(File pathname) {
                String filename = pathname.getName().toLowerCase();
                if (filename.endsWith(".xml") || filename.endsWith(".eml")) {
                    try {
                        emotionmlparser.setValidating(false);
                        return emotionmlparser.parseFile(pathname.getAbsolutePath()).getName().equalsIgnoreCase(markup);
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
     * {@link #addIntentionPerformer(vib.core.intentions.IntentionPerformer) add}
     * function.<br/> The base file name of the EmotionML file is used as
     * {@code requestId} parameter when calling the
     * {@link vib.core.intentions.IntentionPerformer#performIntentions(java.util.List, vib.core.util.id.ID, vib.core.util.Mode) performIntentions}
     * function.
     *
     * @param emotionMLfilename the name of the file to load
     */
    public void load(String emotionMLfilename) {
        //get the base file name to use it as requestId
        String base = (new File(emotionMLfilename)).getName().replaceAll("\\.xml$", "");

        //get the signals of the BML file
        emotionmlparser.setValidating(true);
        XMLTree emotionml = emotionmlparser.parseFile(emotionMLfilename);
        List<Intention> intentions = EmotionMLTranslator.EmotionMLToIntentions(emotionml);
        //send to all SignalPerformer added
        ID id = IDProvider.createID(base);
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, FMLTranslator.getDefaultFMLMode());
        }
    }
}
