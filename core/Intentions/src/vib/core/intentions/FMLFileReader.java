/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.intentions;

import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is an implementation of {@code IntentionEmitter} interface.<br>
 * When calling the {@code load} function, It sends the {@code Intentions}
 * contained in a specified FML file to all {@code IntentionPerformers} added
 * with the {@code add} function.
 *
 * @author Andre-Marie Pez
 */
public class FMLFileReader implements IntentionEmitter {

    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    private XMLParser fmlparser = XML.createParser();
    private static String markup = "fml-apml";

    @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        performers.add(performer);
    }

    /**
     * Loads an FML file.<br> The communicative intentions in the specified
     * file will be send to all {@code IntentionPerformer} added with the
     * {@link #addIntentionPerformer(vib.core.intentions.IntentionPerformer) add}
     * function.<br> The base file name of the FML file is used as
     * {@code requestId} parameter when calling the
     * {@link vib.core.intentions.IntentionPerformer#performIntentions(java.util.List, vib.core.util.id.ID, vib.core.util.Mode) performIntentions}
     * function.
     *
     * @param fmlfilename the name of the file to load
     * @return The ID of the generated event
     */
    public ID load(String fmlfilename) {
        //get the base file name to use it as requestId
        String base = (new File(fmlfilename)).getName().replaceAll("\\.xml$", "");

        //get the intentions of the FML file
        fmlparser.setValidating(true);
        XMLTree fml = fmlparser.parseFile(fmlfilename);
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml);
        Mode mode = FMLTranslator.getDefaultFMLMode();
        if (fml.hasAttribute("composition")) {
            mode.setCompositionType(fml.getAttribute("composition"));
        }
        if (fml.hasAttribute("reaction_type")) {
            mode.setReactionType(fml.getAttribute("reaction_type"));
        }
        if (fml.hasAttribute("reaction_duration")) {
            mode.setReactionDuration(fml.getAttribute("reaction_duration"));
        }
        if (fml.hasAttribute("social_attitude")) {
            mode.setSocialAttitude(fml.getAttribute("social_attitude"));
        }
        ID id = IDProvider.createID(base);
        //send to all SignalPerformer added
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, mode);
        }
        return id;
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }

    /**
     * Returns a {@code java.io.FileFilter} corresponding to FML Files.
     *
     * @return a {@code java.io.FileFilter} corresponding to FML Files
     */
    public java.io.FileFilter getFileFilter() {
        return new java.io.FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String filename = pathname.getName().toLowerCase();
                if (filename.endsWith(".xml") || filename.endsWith(".fml")) {
                    try {
                        fmlparser.setValidating(false);
                        return fmlparser.parseFile(pathname.getAbsolutePath()).getName().equalsIgnoreCase(markup);
                    } catch (Exception e) {
                    }
                }
                return false;
            }
        };
    }
}
