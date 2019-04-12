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
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import vib.auxiliary.thrift.gen_java.Message;
import vib.auxiliary.thrift.services.Receiver;
import vib.core.intentions.FMLTranslator;
import vib.core.intentions.Intention;
import vib.core.intentions.IntentionEmitter;
import vib.core.intentions.IntentionPerformer;
import vib.core.signals.BMLTranslator;
import vib.core.signals.Signal;
import vib.core.signals.SignalEmitter;
import vib.core.signals.SignalPerformer;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.log.Logs;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import vib.auxiliary.socialparameters.SocialDimension;
import vib.auxiliary.socialparameters.SocialParameterEmitter;
import vib.auxiliary.socialparameters.SocialParameterFrame;
import vib.auxiliary.socialparameters.SocialParameterPerformer;
import vib.core.util.CharacterManager;
import vib.core.util.environment.TreeNode;
import vib.core.util.environment.Environment;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;
import vib.core.util.time.Timer;

/**
 *
 * @author Ken Prepin
 */
public class CommandReceiver extends Receiver  implements IntentionEmitter, SignalEmitter, SocialParameterEmitter{

    private Environment environment;
    
    private ArrayList<IntentionPerformer> intentionPerformers;
    private ArrayList<SocialParameterPerformer> socialParamPerformers = new ArrayList<SocialParameterPerformer>();

    private ArrayList<SignalPerformer> signalPerformers;
    private XMLParser parser;
    int cpt ;
    private CharacterManager cm;


    public CommandReceiver(CharacterManager cm) {
        super();
        this.cm = cm;
        this.environment = cm.getEnvironment();
        intentionPerformers = new ArrayList<IntentionPerformer>();
        signalPerformers = new ArrayList<SignalPerformer>();
        parser = XML.createParser();
        parser.setValidating(false);
        cpt=0;
    }

    public CommandReceiver(CharacterManager cm,int port) {
        super(port);
        this.cm = cm;
        this.environment = cm.getEnvironment();
        intentionPerformers = new ArrayList<IntentionPerformer>();
        signalPerformers = new ArrayList<SignalPerformer>();
        parser = XML.createParser();
        parser.setValidating(false);
        cpt=0;
    }

    @Override
    public void perform(Message m) {
        boolean isAnObject = false;

        if(m.getProperties() != null && !m.getProperties().isEmpty() && m.getType().equals("object")){
            addObjectInEnvironment((HashMap<String,String>) m.getProperties(), m.getId(), m.getType());
            isAnObject = true;
        }else{
            double dominance = Double.parseDouble(m.getProperties().get("Dominance"));
            double liking = Double.parseDouble(m.getProperties().get("Liking"));
            ArrayList<SocialParameterFrame> listSPF = new ArrayList<SocialParameterFrame>();
                SocialParameterFrame spf = new SocialParameterFrame(Timer.getCurrentFrameNumber());
                spf.setDoubleValue(SocialDimension.Dominance, dominance);
                spf.setDoubleValue(SocialDimension.Liking, liking);
                listSPF.add(spf);
            for(SocialParameterPerformer spp : socialParamPerformers)
            {
                spp.performSocialParameter(listSPF, IDProvider.createID("Thrift command receiver"));
            }
            Logs.debug("attitude sent : Dominance " +dominance+"; Liking "+liking);
        }
        
        // if it is not an objec t could be a bml or fml
        if (!isAnObject){
            Logs.debug("animation to play received: " + m.getString_content())  ;
            String filename = (new File(m.getString_content())).getName().replaceAll("\\.xml$", "");
            ID messageID = IDProvider.createID(m.getId());
            try {
                XMLTree xml = parser.parseFile(m.getString_content());
                if (xml.getName().equalsIgnoreCase("bml")) {

                    Mode mode = BMLTranslator.getDefaultBMLMode();
                    if (xml.hasAttribute("composition")) {
                        mode.setCompositionType(xml.getAttribute("composition"));
                    }
                    if (xml.hasAttribute("reaction_type")) {
                        mode.setReactionType(xml.getAttribute("reaction_type"));
                    }
                    if (xml.hasAttribute("reaction_duration")) {
                        mode.setReactionDuration(xml.getAttribute("reaction_duration"));
                    }
                    if (xml.hasAttribute("social_attitude")) {
                        mode.setSocialAttitude(xml.getAttribute("social_attitude"));
                    }

                    propagateSignals(BMLTranslator.BMLToSignals(xml,cm), IDProvider.createID(filename, messageID), mode);
                }
                if (xml.getName().equalsIgnoreCase("fml-apml")) {

                    Mode mode = FMLTranslator.getDefaultFMLMode();
                    if (xml.hasAttribute("composition")) {
                        mode.setCompositionType(xml.getAttribute("composition"));
                    }
                    if (xml.hasAttribute("reaction_type")) {
                        mode.setReactionType(xml.getAttribute("reaction_type"));
                    }
                    if (xml.hasAttribute("reaction_duration")) {
                        mode.setReactionDuration(xml.getAttribute("reaction_duration"));
                    }
                    if (xml.hasAttribute("social_attitude")) {
                        mode.setSocialAttitude(xml.getAttribute("social_attitude"));
                    }

                    propagateIntentions(FMLTranslator.FMLToIntentions(xml, cm), IDProvider.createID(filename, messageID), mode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
       // }
    }
    private void propagateSignals(List<Signal> signals, ID request, Mode mode) {
        for (SignalPerformer performer : signalPerformers) {
            performer.performSignals(signals, request, mode);
        }
    }

    private void propagateIntentions(List<Intention> intentions, ID requestId, Mode mode) {
        for (IntentionPerformer performer : intentionPerformers) {
            performer.performIntentions(intentions, requestId, mode);
        }
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        if (ip != null) {
            intentionPerformers.add(ip);
        }
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer ip) {
        intentionPerformers.remove(ip);
    }

    @Override
    public void addSignalPerformer(SignalPerformer sp) {
        if (sp != null) {
            signalPerformers.add(sp);
        }
    }

    @Override
    public void removeSignalPerformer(SignalPerformer sp) {
        signalPerformers.remove(sp);
    }

    @Override
    public void addSocialParameterPerformer(SocialParameterPerformer performer) {
        if (performer != null) {
            socialParamPerformers.add(performer);
        }
    }

    @Override
    public void removeSocialParameterPerformer(SocialParameterPerformer performer) {
        if (performer != null) {
            socialParamPerformers.remove(performer);
        }
    }
    
    public void addObjectInEnvironment(HashMap<String,String> properties, String Id, String type){

        TreeNode object = (TreeNode) this.environment.getNode(properties.get("id"));
        // check if the object it's already in the environment
        if(object != null){ // if so, just update in the env
            
            Vec3d position = new Vec3d(Double.parseDouble(properties.get("position.x")), Double.parseDouble(properties.get("position.y")), Double.parseDouble(properties.get("position.z")));
            object.setCoordinates(position);
            
            Quaternion orientation = new Quaternion(Double.parseDouble(properties.get("quaternion.x")), Double.parseDouble(properties.get("quaternion.y")), Double.parseDouble(properties.get("quaternion.z")), Double.parseDouble(properties.get("quaternion.w")));
            object.setOrientation(orientation);
            
            Vec3d scale = new Vec3d(Double.parseDouble(properties.get("scale.x")), Double.parseDouble(properties.get("scale.y")), Double.parseDouble(properties.get("scale.z")));
            object.setScale(scale);

        }else{// if not, add it in the env
            
            TreeNode obj = new TreeNode(Double.parseDouble(properties.get("position.x")), Double.parseDouble(properties.get("position.y")), Double.parseDouble(properties.get("position.z")), 
                                            Double.parseDouble(properties.get("quaternion.x")), Double.parseDouble(properties.get("quaternion.y")), Double.parseDouble(properties.get("quaternion.z")), Double.parseDouble(properties.get("quaternion.w")),
                                                Double.parseDouble(properties.get("scale.x")), Double.parseDouble(properties.get("scale.y")), Double.parseDouble(properties.get("scale.z")));           
            obj.setIdentifier(properties.get("id"));
            this.environment.addNode(obj);
        }
        
    }


}
