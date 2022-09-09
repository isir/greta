package greta.auxiliary.incrementality;

import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
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
 * @author Sean
 */
public class IncRealizerInteraction implements IncrementalityInteractionEmitter, IntentionEmitter, IncrementalityInteractionPerformer {

    private List<IncrementalityInteractionPerformer> performerList = new ArrayList<>();
    private ArrayList<IntentionPerformer> intentionPerformers = new ArrayList<IntentionPerformer>();
    
    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    private XMLParser fmlparser = XML.createParser();
    private static String markup = "fml-apml";
    private CharacterManager cm;
    private String stopFileLocation;
    
    public IncRealizerInteraction(CharacterManager cm){
        this.cm = cm;
        //Should work with every installation but verify path in case of issues
        this.stopFileLocation = "./Examples/DemoIncrementality/stop.xml";
    }
    

    @Override
    public void addIncInteractionPerformer(IncrementalityInteractionPerformer performer) {
        performerList.add(performer);
    }

    @Override
    public void removeIncInteractionPerformer(IncrementalityInteractionPerformer performer) {
        performerList.remove(performer);
    }

    //send signal to stop sending new chunks
    //NOTE: doesn't stop voice
    public void sendPauseGesture() {
        for (IncrementalityInteractionPerformer pf : performerList) {
            pf.performIncInteraction("pauseGesture");
        }
    }
    
    //send signal to resume sending chunks after an interrupt
    public void sendResume(){
        for (IncrementalityInteractionPerformer pf : performerList) {
            pf.performIncInteraction("resume");
        }
    }
    
    //Send signal to fully stop execution
    public void sendStop(){
        String base = (new File(stopFileLocation)).getName().replaceAll("\\.xml$", "");

        String fml_id = "";
        //get the intentions of the FML file
        fmlparser.setValidating(true);
        XMLTree fml = fmlparser.parseFile(stopFileLocation);
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
        }else{
            fml_id = "fml_1";
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
        for (IntentionPerformer performer : intentionPerformers) {
            performer.performIntentions(intentions, id, mode);
        }
    }
    
    //send signal to clear chunk list and close queue
    public void sendClearQueue(){
        for (IncrementalityInteractionPerformer pf : performerList) {
            pf.performIncInteraction("clearQueue");
        }
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        intentionPerformers.add(ip);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer ip) {
        intentionPerformers.remove(ip);
    }

    @Override
    public void performIncInteraction(String parParam) {
        if (parParam.equals("stop")) {
            this.sendStop();
        }
    }
}
