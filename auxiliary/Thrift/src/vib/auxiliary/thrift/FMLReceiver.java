/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import vib.auxiliary.thrift.gen_java.Message;
import vib.auxiliary.thrift.services.Receiver;
import vib.core.intentions.FMLTranslator;
import vib.core.intentions.Intention;
import vib.core.intentions.IntentionEmitter;
import vib.core.intentions.IntentionPerformer;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ken Prepin
 * @author Brian Ravenet
 */
public class FMLReceiver extends Receiver implements IntentionEmitter {

    public FMLReceiver() {
        super();
        performers = new ArrayList<IntentionPerformer>();
    }

    public FMLReceiver(int port) {
        super(port);
        performers = new ArrayList<IntentionPerformer>();
    }

    private final ArrayList<IntentionPerformer> performers;
    private final XMLParser fmlparser = XML.createParser();

    @Override
    public void perform(Message m) {
        XMLTree fml = fmlparser.parseBuffer(m.getString_content());
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
        //send to all IntentionPerformer added
        ID id = IDProvider.createID(m.getId());
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, mode);
        }
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        performers.add(performer);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }

}
