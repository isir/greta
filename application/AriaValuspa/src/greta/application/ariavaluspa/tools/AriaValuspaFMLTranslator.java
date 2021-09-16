/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package greta.application.ariavaluspa.tools;

import eu.aria.util.translator.gui.MainView;
import eu.aria.util.translator.Translator;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;

/**
 *
 * @author Angelo Cafaro
 */
public class AriaValuspaFMLTranslator extends JFrame implements IntentionEmitter {


    // Configuration Directory
    public static final String ARIA_FMLTRANSLATOR_CONFIG_DIR = "/Common/Data/ARIA-ValuspaFMLTranslator/";

    // Configuration Files
    public static final String CONFIG_FILE_JSON = "aria-fmltranslator-config.json";
    public static final String CONFIG_FILE_INIT = "aria-fmltranslator.ini";

    private ArrayList<IntentionPerformer> performers = new ArrayList<>();
    private XMLParser fmlparser = XML.createParser();
    private Translator ariaTranslator;
    private IniManager localInitManager;
    public CharacterManager c;

    @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        performers.add(performer);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }

    public AriaValuspaFMLTranslator(CharacterManager cm) {
        
        this.c=cm;

        // Create the local init Manager
        localInitManager = new IniManager(IniManager.getProgramPath() + ARIA_FMLTRANSLATOR_CONFIG_DIR + CONFIG_FILE_INIT);
        
        // Set the fml parser validating property
        fmlparser.setValidating(true);

        // Creates a new ariaTranslator
        ariaTranslator = new Translator();

        // Init the aria translator with a json config file
        try {
            ariaTranslator.init(IniManager.getProgramPath() + ARIA_FMLTRANSLATOR_CONFIG_DIR + CONFIG_FILE_JSON);
        }
        catch (IOException ex) {
            Logs.error("Failed to init the ARIA-FML Translator module, either the (json) config file was not found at the following path [" +
                    IniManager.getProgramPath() + ARIA_FMLTRANSLATOR_CONFIG_DIR + CONFIG_FILE_JSON + "] " +
                    "or the config file has errors in it, in this case verify the json validity.");
            return;
        }

        String openSaveDirPath = localInitManager.getValueString("FML_TEMPLATES_PATH");
        if ((openSaveDirPath == null) || (openSaveDirPath.trim().isEmpty())) {
            openSaveDirPath = IniManager.getProgramPath();
        }
        MainView mainView = new MainView(ariaTranslator);
        mainView.setOpenDir(new File(openSaveDirPath));
        mainView.setSaveDir(new File(openSaveDirPath));

        this.setContentPane(mainView.getRootPanel());
        this.setMinimumSize(new Dimension(640,480));
        this.pack();

        mainView.registerListener(xml -> {
            XMLTree fml = null;
            fml = fmlparser.parseBuffer(xml);
            List<Intention> intentions = FMLTranslator.FMLToIntentions(fmlparser.parseBuffer(xml),c);
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
            ID id = IDProvider.createID("ARIAFMLTranslator");
            if (fml.hasAttribute("id")) {
                id = IDProvider.createID(fml.getAttribute("id"));
            }

            //send to all SignalPerformer added
            for (IntentionPerformer performer : performers) {
                performer.performIntentions(intentions, id, mode);
            }
        });
    }
}