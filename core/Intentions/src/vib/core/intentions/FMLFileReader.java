/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
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
import vib.core.util.CharacterManager;

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
    private CharacterManager cm;
    
    public FMLFileReader(CharacterManager cm){
        this.cm = cm;
    }

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

        String fml_id = "";
        //get the intentions of the FML file
        fmlparser.setValidating(true);
        XMLTree fml = fmlparser.parseFile(fmlfilename);
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml,cm);
        Mode mode = FMLTranslator.getDefaultFMLMode();
        for (XMLTree fmlchild : fml.getChildrenElement()) {
            // store the bml id in the mode class in order
            if (fmlchild.isNamed("bml")) {   
                //System.out.println(fmlchild.getName());
                if(fmlchild.hasAttribute("id")){
                    mode.setBml_id(fmlchild.getAttribute("id"));
                }
            }
        }
        if(fml.hasAttribute("id")){
            fml_id = fml.getAttribute("id");
        }
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
        id.setFmlID(fml_id);
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
