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
package greta.auxiliary.thrift;

import greta.auxiliary.socialparameters.SocialDimension;
import greta.auxiliary.socialparameters.SocialParameterEmitter;
import greta.auxiliary.socialparameters.SocialParameterFrame;
import greta.auxiliary.socialparameters.SocialParameterPerformer;
import greta.auxiliary.thrift.gen_java.Message;
import greta.auxiliary.thrift.services.Receiver;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.signals.BMLTranslator;
import greta.core.signals.CancelableSignalPerformer;
import greta.core.signals.GazeSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.Influence;
import greta.core.util.environment.Environment;
import greta.core.util.environment.Leaf;
import greta.core.util.environment.Node;
import greta.core.util.environment.TreeNode;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.time.Timer;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that receives and handles Thrift messages.
 * @author Ken Prepin
 * @author Nawhal Sayarh
 * @author Fajrian Yunus
 */
public class CommandReceiver extends Receiver implements IntentionEmitter, SignalEmitter, SocialParameterEmitter {

    private Environment environment;

    private ArrayList<IntentionPerformer> intentionPerformers;
    private ArrayList<SocialParameterPerformer> socialParamPerformers = new ArrayList<>();

    private ArrayList<SignalPerformer> signalPerformers;
    private XMLParser parser;
    private int cpt;
    private CharacterManager cm;
    private HashMap<String, HashMap<LocalDateTime, ID>> gameObjectLastGazes = new HashMap<>();
    private static NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());

    /**
     * Creates a CommandReceiver.
     * @param cm character manager
     */
    public CommandReceiver (CharacterManager cm) {
        super();
        this.cm = cm;
        this.environment = cm.getEnvironment();
        intentionPerformers = new ArrayList<>();
        signalPerformers = new ArrayList<>();
        parser = XML.createParser();
        parser.setValidating(false);
        cpt = 0;
    }

    /**
     * Creates a CommandReceiver.
     * @param cm character manager
     * @param port port on which to receive messages
     */
    public CommandReceiver (CharacterManager cm,int port) {
        super(port);
        this.cm = cm;
        this.environment = cm.getEnvironment();
        intentionPerformers = new ArrayList<>();
        signalPerformers = new ArrayList<>();
        parser = XML.createParser();
        parser.setValidating(false);
        cpt = 0;
    }

    @Override
    public void perform (Message message) {
        // int messageNumber = Integer.parseInt(message.getId());
        // if(messageNumber > cpt){ // We only consider messages that have been sent after the last one received TODO ?
        // cpt = messageNumber;
        synchronized (environment) {
            switch (message.getType()) {
                case "animID":
                    handleAnimIdMessage(message);
                    break;
                case "character":
                    handleCharacterMessage(message);
                    break;
                case "character_head":
                    handleCharacterMemberMessage(message);
                    break;
                case "character_left_eye":
                    handleCharacterMemberMessage(message);
                    break;
                case "character_right_eye":
                    handleCharacterMemberMessage(message);
                    break;
                case "character_mouth":
                    handleCharacterMessage(message);
                    break;
                case "character_left_hand":
                    handleCharacterMemberMessage(message);
                    break;
                case "character_right_hand":
                    handleCharacterMemberMessage(message);
                    break;
                case "character_left_foot":
                    handleCharacterMemberMessage(message);
                    break;
                case "character_right_foot":
                    handleCharacterMemberMessage(message);
                    break;
                case "object":
                    handleObjectMessage(message);
                    break;
                default:
                    System.err.println("Error : message type not recognized in CommandReceiver. Message id : " + message.getId());
            }
        }
        // }
    }

    /**
     * Performs the animation with the id provided in the message.
     * @param message message with all data concerning the animation to be played
     */
    private void handleAnimIdMessage (Message message) {
        sendDominanceLikingFromMessage(message);
        Logs.debug("animation to play received: " + message.getString_content());

        XMLTree xml;
        try {
            xml = readFileFromPath(message.getString_content());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String fileName = (new File(message.getString_content())).getName().replaceAll("\\.xml$", "");
        ID messageId = IDProvider.createID(message.getId());
        sendBmlFmlFromXml(xml, fileName, messageId);
    }

    /**
     * Sends the dominance and liking properties of the given message to all linked {@code SocialParameterPerformer}
     * If the messages' properties are empty or null, nothing happens
     * @param message message which's dominance and liking have to be performed
     */
    private void sendDominanceLikingFromMessage (Message message) {
        if (message.getProperties() != null && !message.getProperties().isEmpty()) {
            double dominance = Double.parseDouble(message.getProperties().get("Dominance"));
            double liking = Double.parseDouble(message.getProperties().get("Liking"));
            ArrayList<SocialParameterFrame> listSPF = new ArrayList<>();
            SocialParameterFrame spf = new SocialParameterFrame(Timer.getCurrentFrameNumber());
            spf.setDoubleValue(SocialDimension.Dominance, dominance);
            spf.setDoubleValue(SocialDimension.Liking, liking);
            listSPF.add(spf);
            for (SocialParameterPerformer spp : socialParamPerformers) {
                spp.performSocialParameter(listSPF, IDProvider.createID("Thrift command receiver"));
            }
            Logs.debug("attitude sent : Dominance " + dominance + "; Liking " + liking);
        }
    }

    /**
     * Reads the file with the given path and returns it as a XMLTree.
     * @param path path of the file to read
     * @return file as a XMLTree
     * @throws FileNotFoundException if nothing is found at the given path
     */
    private XMLTree readFileFromPath (String path) throws FileNotFoundException {
        XMLTree xml = parser.parseFile(path);
        if (xml == null) {
            throw new FileNotFoundException("No XML file was found at path " + path);
        }
        return xml;
    }

    /**
     * Sends the xml file with its right type - either BML or FML - to all SignalPerformer linked.
     * @param xml file to send
     * @param fileName name of the file
     * @param messageId id of the message
     */
    private void sendBmlFmlFromXml (XMLTree xml, String fileName, ID messageId) {
        String xmlName = xml.getName();
        if (xmlName.equalsIgnoreCase("bml")) {
            Mode mode = BMLTranslator.getDefaultBMLMode();
            setModeParametersForAnimationSignal(xml, mode);
            propagateSignals(BMLTranslator.BMLToSignals(xml, cm), IDProvider.createID(fileName, messageId), mode);
        } else if (xmlName.equalsIgnoreCase("fml-apml")) {
            Mode mode = FMLTranslator.getDefaultFMLMode();
            setModeParametersForAnimationSignal(xml, mode);
            propagateIntentions(FMLTranslator.FMLToIntentions(xml, cm), IDProvider.createID(fileName, messageId), mode);
        }
    }

    /**
     * Sets the mode according to the given xml.
     * @param xml xml with data concerning the mode
     * @param mode base mode that will be modified
     */
    static void setModeParametersForAnimationSignal (XMLTree xml, Mode mode) {
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

    /**
     * Updates the character's position.
     * @param message message with all data concerning the unity game character
     */
    private void handleCharacterMessage (Message message) {

        Map<String, String> gameCharacterProperties = message.getProperties();
        String gameCharacterId = gameCharacterProperties.get("id");

        TreeNode gameCharacterNode = null;
        List<Node> environmentGuests = this.environment.getGuests();
        for (Node environmentGuest : environmentGuests) {
            if (environmentGuest instanceof MPEG4Animatable) {
                MPEG4Animatable mpeg4Animatable = (MPEG4Animatable) environmentGuest;
                String mpeg4AnimatableName = mpeg4Animatable.getCharacterManager().getCurrentCharacterName();
                if (gameCharacterId.equalsIgnoreCase(mpeg4AnimatableName)) {
                    gameCharacterNode = mpeg4Animatable;
                }
            }
        }

        if (gameCharacterNode == null) {
            gameCharacterNode = createObjectNode(gameCharacterId);
        }

        // Update coordinates
        updateNodeProperties(gameCharacterNode, gameCharacterProperties);

        if (gameCharacterProperties.get("gaze") != null) {
            handleGazeMessage(gameCharacterId, gameCharacterProperties.get("influence"));
        }
    }

    /**
     * Updates the character's member's position and updates the gaze if needed.
     * @param message message with all data concerning the unity game character's member
     */
    private void handleCharacterMemberMessage (Message message) {

        Map<String, String> gameCharacterMemberProperties = message.getProperties();
        String gameCharacterMemberId = gameCharacterMemberProperties.get("id");

        // Get the character's member's node
        TreeNode gameCharacterMemberNode = (TreeNode) this.environment.getNode(gameCharacterMemberId + "_TreeNode");
        if (gameCharacterMemberNode == null) {
            gameCharacterMemberNode = createCharacterMemberNode(gameCharacterMemberId);
            if (message.getType().equals("character_head")) {
                this.cm.setCurrentCharacterHeadFromUnity(gameCharacterMemberNode);
            }
        }

        // Update coordinates
        updateNodeProperties(gameCharacterMemberNode, gameCharacterMemberProperties);

        if (gameCharacterMemberProperties.get("gaze") != null) {
            handleGazeMessage(gameCharacterMemberId, gameCharacterMemberProperties.get("influence"));
        }
    }

    /**
     * Updates the object's position and updates the gaze if needed.
     * @param message message with all data concerning the unity game object
     */
    private void handleObjectMessage (Message message) {

        Map<String, String> gameObjectProperties = message.getProperties();
        String gameObjectId = gameObjectProperties.get("id");

        // Get the object's node
        TreeNode gameObjectNode = (TreeNode) this.environment.getNode(gameObjectId + "_TreeNode");
        if (gameObjectNode == null) {
            gameObjectNode = createObjectNode(gameObjectId);
        }

        // Update coordinates
        updateNodeProperties(gameObjectNode, gameObjectProperties);

        if (gameObjectProperties.get("metadata.objectToGazeAt") != null) {
            Leaf gameObjectLeaf = (Leaf) this.environment.getNode(gameObjectId);
            gameObjectLeaf.createMetadataLeaf("objectToGazeAt", gameObjectProperties.get("metadata.objectToGazeAt"));
        }
        if (gameObjectProperties.get("gaze") != null) {
            handleGazeMessage(gameObjectId, gameObjectProperties.get("influence"));
        }
    }

    /**
     * Creates and returns the node corresponding to the game character's member with the given id.
     * @param gameCharacterMemberId id of the game character's member who's node shall be created
     * @return the node corresponding to the given id
     */
    private TreeNode createCharacterMemberNode (String gameCharacterMemberId) {
        TreeNode unityObjectsNode = getUnityObjectsNode();
        // Create member with node as parent
        TreeNode gameCharacterMemberNode = new TreeNode();
        gameCharacterMemberNode.setIdentifier(gameCharacterMemberId + "_TreeNode");
        unityObjectsNode.addChildNode(gameCharacterMemberNode);
        Leaf gameCharacterMemberLeaf = new Leaf();
        gameCharacterMemberLeaf.setIdentifier(gameCharacterMemberId);
        gameCharacterMemberLeaf.setReference("greta_unity." + cm.getCurrentCharacterName().toLowerCase() + ".member");
        gameCharacterMemberNode.addChildNode(gameCharacterMemberLeaf);
        environment.addLeaf(gameCharacterMemberLeaf);
        return gameCharacterMemberNode;
    }

    /**
     * Creates and returns the node corresponding to the game object with the given id.
     * @param gameObjectId id of the game object who's node shall be created
     * @return the node corresponding to the given id
     */
    private TreeNode createObjectNode (String gameObjectId) {
        TreeNode unityObjectsNode = getUnityObjectsNode();
        // Create object with node as parent
        TreeNode gameObjectNode = new TreeNode();
        gameObjectNode.setIdentifier(gameObjectId + "_TreeNode");
        unityObjectsNode.addChildNode(gameObjectNode);
        Leaf gameObjectLeaf = new Leaf();
        gameObjectLeaf.setIdentifier(gameObjectId);
        gameObjectLeaf.setReference("greta_unity.object." + gameObjectId);
        gameObjectNode.addChildNode(gameObjectLeaf);
        environment.addLeaf(gameObjectLeaf);
        return gameObjectNode;
    }

    /**
     * Returns the node corresponding to unity game objects.
     * @return node corresponding to unity game objects
     */
    private TreeNode getUnityObjectsNode () {
        // Try get UnityObjectsNode
        TreeNode unityObjectsNode = (TreeNode) this.environment.getNode("UnityObjects");
        if (unityObjectsNode == null) {
            // If node has not been created, create the node
            unityObjectsNode = new TreeNode();
            unityObjectsNode.setIdentifier("UnityObjects");
            this.environment.addNode(unityObjectsNode);
        }
        return unityObjectsNode;
    }

    /**
     * Updates the given node's properties according to the given game object's properties.<br/>
     * Properties queried are : "position.x", "position.y", "position.z", "orientation.x",
     * "orientation.y", "orientation.z", "orientation.w", "scale.x", "scale.y", "scale.z"
     * @param node node to update
     * @param gameObjectProperties map with the needed properties and their values
     */
    private static void updateNodeProperties (TreeNode node, Map<String, String> gameObjectProperties) {
        /*
        node.setCoordinates(
                Float.parseFloat(gameObjectProperties.get("position.x")),
                Float.parseFloat(gameObjectProperties.get("position.y")),
                Float.parseFloat(gameObjectProperties.get("position.z"))
        );
        node.setOrientation(
                Float.parseFloat(gameObjectProperties.get("orientation.x")),
                Float.parseFloat(gameObjectProperties.get("orientation.y")),
                Float.parseFloat(gameObjectProperties.get("orientation.z")),
                Float.parseFloat(gameObjectProperties.get("orientation.w"))
        );
        node.setScale(
                Float.parseFloat(gameObjectProperties.get("scale.x")),
                Float.parseFloat(gameObjectProperties.get("scale.y")),
                Float.parseFloat(gameObjectProperties.get("scale.z"))
        );*/
        node.setCoordinates(
                parseFloat(gameObjectProperties.get("position.x")),
                parseFloat(gameObjectProperties.get("position.y")),
                parseFloat(gameObjectProperties.get("position.z"))
        );
        node.setOrientation(
                parseFloat(gameObjectProperties.get("orientation.x")),
                parseFloat(gameObjectProperties.get("orientation.y")),
                parseFloat(gameObjectProperties.get("orientation.z")),
                parseFloat(gameObjectProperties.get("orientation.w"))
        );
        node.setScale(
                parseFloat(gameObjectProperties.get("scale.x")),
                parseFloat(gameObjectProperties.get("scale.y")),
                parseFloat(gameObjectProperties.get("scale.z"))
        );
    }
    
    private static float parseFloat(String s){
        float f = Float.NaN;
        try {
            f = nf.parse(s).floatValue();
        } catch (ParseException ex) {
            Logger.getLogger(CommandReceiver.class.getName()).log(Level.WARNING, null, ex);
        }
        return f;
    }

    /**
     * Creates a gaze signal and sends it to all linked SignalPerformer.
     * @param gameObjectId id of the game object which's signal is to be sent
     */
    private void handleGazeMessage (String gameObjectId, String gazeInfluence) {
        GazeSignal gazeSignal = new GazeSignal("Gaze" + gameObjectId);
        gazeSignal.getEnd().setValue(gazeSignal.getStartValue() + 0.4);
        gazeSignal.setTarget(gameObjectId);
        gazeSignal.setGazeShift(true);
        gazeSignal.setInfluence(Influence.valueOf(gazeInfluence));

        propagateGaze(gazeSignal, gameObjectId);
    }

    /**
     * Sends the given signal for the game object with the given id to all linked SignalPerformer.
     * @param gazeSignal signal to propagate
     * @param gameObjectId id of the game object which's signal is to be sent
     */
    private void propagateGaze (GazeSignal gazeSignal, String gameObjectId) {
        // If gaze towards that object has already been updated
        HashMap<LocalDateTime, ID> lastGazesUpdates = gameObjectLastGazes.get(gameObjectId);
        if (lastGazesUpdates != null) {
            for (Map.Entry<LocalDateTime, ID> gazeUpdate : lastGazesUpdates.entrySet()) {
                long timeUntilNow = gazeUpdate.getKey().until(LocalDateTime.now(), ChronoUnit.MILLIS);
                if (timeUntilNow > 500) {
                    // If the last gaze was 500ms or more before now, we delete it
                    // Not really a way to know if it's continuous, but still cleans a bit of the overflowing
                    // BAPs and FAPs... so it's kind of a hot fix.
                    cancelSignalById(gazeUpdate.getValue());
                }
            }
            // Clean the map anyway because it's only useful if it's recent and hasn't been removed yet
            lastGazesUpdates.clear();
        }
        // Save the signal's ID and time
        ID gazeID = IDProvider.createID(gameObjectId);
        gameObjectLastGazes.computeIfAbsent(gameObjectId, k -> new HashMap<>());
        gameObjectLastGazes.get(gameObjectId).put(LocalDateTime.now(), gazeID);
        // Send the signal
        List<Signal> signals = new ArrayList<>();
        signals.add(gazeSignal);
        propagateSignals(signals,
                gazeID,
                BMLTranslator.getDefaultBMLMode());
    }

    /**
     * Cancels the signal with the given signal id if it hasn't happened yet.
     * @param signalId id of the signal to cancel
     */
    private void cancelSignalById (ID signalId) {
        for (SignalPerformer performer : signalPerformers) {
            if (performer instanceof CancelableSignalPerformer) {
                ((CancelableSignalPerformer) performer).cancelSignalsById(signalId);
            }
        }
    }

    /**
     * Sends the given signals to all linked SignalPerformer with the given request ID and the given Mode.
     * @param signals list of Signal to send to the performers
     * @param requestId ID of the request
     * @param mode Mode for the request
     */
    private void propagateSignals (List<Signal> signals, ID requestId, Mode mode) {
        for (SignalPerformer performer : signalPerformers) {
            performer.performSignals(signals, requestId, mode);
        }
    }

    /**
     * Sends the given intentions to all linked IntentionPerformer with the given request ID and the given Mode.
     * @param intentions list of Intention to send to the performers
     * @param requestId ID of the request
     * @param mode Mode for the request
     */
    private void propagateIntentions (List<Intention> intentions, ID requestId, Mode mode) {
        for (IntentionPerformer performer : intentionPerformers) {
            performer.performIntentions(intentions, requestId, mode);
        }
    }

    @Override
    public void addIntentionPerformer (IntentionPerformer ip) {
        if (ip != null) {
            intentionPerformers.add(ip);
        }
    }

    @Override
    public void removeIntentionPerformer (IntentionPerformer ip) {
        intentionPerformers.remove(ip);
    }

    @Override
    public void addSignalPerformer (SignalPerformer sp) {
        if (sp != null) {
            signalPerformers.add(sp);
        }
    }

    @Override
    public void removeSignalPerformer (SignalPerformer sp) {
        signalPerformers.remove(sp);
    }

    @Override
    public void addSocialParameterPerformer (SocialParameterPerformer performer) {
        if (performer != null) {
            socialParamPerformers.add(performer);
        }
    }

    @Override
    public void removeSocialParameterPerformer (SocialParameterPerformer performer) {
        if (performer != null) {
            socialParamPerformers.remove(performer);
        }
    }
}
