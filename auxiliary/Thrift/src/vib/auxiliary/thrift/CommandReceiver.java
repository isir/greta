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
import vib.core.util.environment.Environment;
import vib.core.util.environment.Leaf;
import vib.core.util.environment.Node;
import vib.core.util.environment.TreeNode;
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
import java.util.Map;
import java.util.Optional;

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
        // if(messageID>cpt){
        //     cpt = messageID;
        switch (m.getType()) {
            case "animID" :
                if(m.getProperties() != null && !m.getProperties().isEmpty()){
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
                Logs.debug("animation to play received: " + m.getString_content())  ;
                String filename = (new File(m.getString_content())).getName().replaceAll("\\.xml$", "");
                ID messageID = IDProvider.createID(m.getId());
                try {
                    XMLTree xml = parser.parseFile(m.getString_content());
                    if (xml.getName().equalsIgnoreCase("bml")) {
                        Mode mode = BMLTranslator.getDefaultBMLMode();
                        setModeParametersForAnimationSignal(xml, mode);
                        propagateSignals(BMLTranslator.BMLToSignals(xml,cm), IDProvider.createID(filename, messageID), mode);
                    }
                    if (xml.getName().equalsIgnoreCase("fml-apml")) {
                        Mode mode = FMLTranslator.getDefaultFMLMode();
                        setModeParametersForAnimationSignal(xml, mode);
                        propagateIntentions(FMLTranslator.FMLToIntentions(xml, cm), IDProvider.createID(filename, messageID), mode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "object":
                Map<String, String> gameObjectProperties = m.getProperties();
                // If object has already been created
                String gameObjectId = gameObjectProperties.get("id");
                TreeNode gameObjectLeaf = (TreeNode) this.environment.getNode(gameObjectId);
                if (gameObjectLeaf != null) {
                    TreeNode gameObjectLeafParent = gameObjectLeaf.getParent();
                    // Change coordinates
                    updateNodeProperties(gameObjectLeafParent, gameObjectProperties);
                } else {
                    // Try get UnityObjectsNode
                    TreeNode unityObjectsNode = (TreeNode) this.environment.getNode("UnityObjectsNode");
                    if (unityObjectsNode == null) {
                        // If node has not been created, create the node
                        unityObjectsNode = new TreeNode();
                        unityObjectsNode.setIdentifier("UnityObjectsNode");
                        this.environment.addNode(unityObjectsNode);
                    }
                    // Create object with node as parent
                    TreeNode gameObjectLeafParent = new TreeNode();
                    gameObjectLeafParent.setIdentifier("GameObject-" + gameObjectId + "Parent");
                    updateNodeProperties(gameObjectLeafParent, gameObjectProperties);
                    unityObjectsNode.addChildNode(gameObjectLeafParent);
                    Leaf gameObjectLeaf = new Leaf();
                    gameObjectLeaf.setIdentifier("GameObject-" + gameObjectId);
                    gameObjectLeaf.setReference("GameObject-" + gameObjectId);
                    gameObjectLeafParent.addChildNode(gameObjectLeaf);
                    this.environment.addLeaf(gameObjectLeaf);
                }
                break;
            default:
                System.err.println("Error : message type not recognized in CommandReceiver. Message id : " + m.getId());
        }
        // }
    }

    private static void updateNodeProperties (TreeNode node, Map<String, String> gameObjectProperties) {
        node.setCoordinates(Float.parseFloat(gameObjectProperties.get("position.x")),
                Float.parseFloat(gameObjectProperties.get("position.y")),
                Float.parseFloat(gameObjectProperties.get("position.z")));

        node.setOrientation(Float.parseFloat(gameObjectProperties.get("quaternion.x")),
                Float.parseFloat(gameObjectProperties.get("quaternion.y")),
                Float.parseFloat(gameObjectProperties.get("quaternion.z")),
                Float.parseFloat(gameObjectProperties.get("quaternion.w")));

        node.setScale(Float.parseFloat(gameObjectProperties.get("scale.x")),
                Float.parseFloat(gameObjectProperties.get("scale.y")),
                Float.parseFloat(gameObjectProperties.get("scale.z")));
    }

    static void setModeParametersForAnimationSignal(XMLTree xml, Mode mode) {
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
}
