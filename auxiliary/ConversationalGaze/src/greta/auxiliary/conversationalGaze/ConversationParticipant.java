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
package greta.auxiliary.conversationalGaze;

import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.signals.GazeSignal;
import greta.core.signals.Signal;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.environment.Environment;
import greta.core.util.environment.Node;
import greta.core.util.environment.TreeNode;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Donatella Simonetti
 */
public class ConversationParticipant implements CharacterDependent{

    private CharacterManager characterManager;
    private String name;
    private String mpeg4_id;
    private Role role;
    private boolean isTalking;

    private double dominance;
    private double levelOfIntimacy;

    private double time_MG; // fixed term --> how long the agent look at the user tring to make him/her look at it back
    private double time_LA; // how long the Agent gaze aversion should be

    private int gazeStatus; // 0 if mutual gaze; 1 if look away
    private int oldGazeStatus;

    private ArrayList<Signal> ListSignals;
    public String lastGazeTarget = "";
    public double timeGazingatTarget = 0.0;

    public double Timeplus_lookingAtSpeaker = 1000; // msec

    public boolean isGazing = true;

    private List<Node> idleGazeTargets = new ArrayList<>();
    private String idleGazeTargetsRootID;


    public ConversationParticipant(CharacterManager cm){

        this.characterManager = cm;
        this.name = cm.getCurrentCharacterName();
        this.mpeg4_id = cm.getCurrentCharacterId();
        this.isTalking = false;
        this.ListSignals = new ArrayList <Signal>();

        idleGazeTargetsRootID = IDProvider.createID("IdleGazeTargetsRoot").toString();

    }

    public ConversationParticipant(String user){
        //setCharacterManager(cm);
        this.name = user;
        this.isTalking = false;
    }

    @Override
    public void onCharacterChanged() {
        //
    }

    @Override
    public CharacterManager getCharacterManager() {
        if(characterManager==null)
            characterManager = CharacterManager.getStaticInstance();
        return characterManager;
    }

    @Override
    public void setCharacterManager(CharacterManager cm) {
       this.characterManager = cm;
    }

    /**
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * @return the isTalking
     */
    public boolean isIsTalking() {
        return isTalking;
    }

    /**
     * @param isTalking the isTalking to set
     */
    public void setIsTalking(boolean isTalking) {
        this.isTalking = isTalking;
    }

    /**
     * @return the dominance
     */
    public double getDominance() {
        return dominance;
    }

    /**
     * @param dominance the dominance to set
     */
    public void setDominance(double dominance) {
        this.dominance = dominance;
    }

    /**
     * @return the levelOfIntimacy
     */
    public double getLevelOfIntimacy() {
        return levelOfIntimacy;
    }

    /**
     * @param levelOfIntimacy the levelOfIntimacy to set
     */
    public void setLevelOfIntimacy(double levelOfIntimacy) {
        this.levelOfIntimacy = levelOfIntimacy;
    }

    /**
     * @return the time_MG
     */
    public double getTime_MG() {
        return time_MG;
    }

    /**
     * @param time_MG the time_MG to set
     */
    public void setTime_MG(double time_MG) {
        this.time_MG = time_MG;
    }

    /**
     * @return the time_LA
     */
    public double getTime_LA() {
        return time_LA;
    }

    /**
     * @param time_LA the time_LA to set
     */
    public void setTime_LA(double time_LA) {
        this.time_LA = time_LA;
    }

    /**
     * @return the gazeStatus
     */
    public int getGazeStatus() {
        return gazeStatus;
    }

    /**
     * @param gazeStatus the gazeStatus to set
     */
    public void setGazeStatus(int gazeStatus) {
        this.gazeStatus = gazeStatus;
    }

    /**
     * @return the oldGazeStatus
     */
    public int getOldGazeStatus() {
        return oldGazeStatus;
    }

    /**
     * @param oldGazeStatus the oldGazeStatus to set
     */
    public void setOldGazeStatus(int oldGazeStatus) {
        this.oldGazeStatus = oldGazeStatus;
    }

    public List<Signal> addSignal(GazeSignal gs){
        this.getSignal().add(gs);
        return getSignal();
    }

    public List<Signal> addListGazeSignal(List<GazeSignal> lgs){
        for (GazeSignal gs : lgs){
            this.getSignal().add(gs);
        }
        return getSignal();
    }

    public List<Signal> addListSignal(List<Signal> lgs){
        for (Signal gs : lgs){
            this.getSignal().add(gs);
        }
        return getSignal();
    }

    /**
     * @return the ListSignals
     */
    public List<Signal> getSignal() {
        return ListSignals;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the mpeg4_id
     */
    public String getMpeg4_id() {
        return mpeg4_id;
    }

    /**
     * @param mpag4_id the mpeg4_id to set
     */
    public void setMpeg4_id(String mpag4_id) {
        this.mpeg4_id = mpag4_id;
    }

    public void generateRandomIdleGazeTargets(String getMpeg4_id) {

        Environment envi = this.getCharacterManager().getEnvironment();
        if (envi != null)
        {
                getIdleGazeTargets().clear();
                //System.out.println(this.characterManager);
                //System.out.println(this.characterManager.getCurrentCharacterId());
                //MPEG4Animatable agentmpeg = new MPEG4Animatable(this.characterManager);
                String identifier = getMpeg4_id;
                TreeNode agentNode = (TreeNode) envi.getNode(identifier);
                if (agentNode == null){
                    for (Node nd : envi.getRoot().getChildren()){
                        if (nd instanceof MPEG4Animatable){
                            System.out.println(nd.getIdentifier());
                            System.out.println(identifier);
                            if (nd.getIdentifier().equals(identifier)){
                                agentNode = (TreeNode) nd;
                            }
                        }
                    }
                }

                if (agentNode != null){
                    TreeNode rootNodeIdleGazeTargets = (TreeNode) envi.getNode(idleGazeTargetsRootID);

                if (rootNodeIdleGazeTargets == null) {

                    // Create root node
                    TreeNode root = new TreeNode();
                    root.setIdentifier(idleGazeTargetsRootID);
                    envi.addNode(root, agentNode);

                    // Set root node coordinates
                    /*GazeSignal gs = new GazeSignal("gazeLeft");
                    gs.setCharacterManager(characterManager);
                    gs.setGazeShift(true);
                    gs.setOffsetAngle(30);
                    gs.setOffsetDirection(GazeDirection.LEFT);
                    //gs.set
                    idleGazeTargets.add(gs);

                    GazeSignal gs2 = new GazeSignal("gazeRight");
                    gs.setCharacterManager(characterManager);
                    gs.setGazeShift(true);
                    gs.setOffsetAngle(30);
                    gs.setOffsetDirection(GazeDirection.RIGHT);

                    idleGazeTargets.add(gs2);

                    GazeSignal gs3 = new GazeSignal("gazeRight");
                    gs.setCharacterManager(characterManager);
                    gs.setGazeShift(true);
                    gs.setOffsetAngle(30);
                    gs.setOffsetDirection(GazeDirection.DOWN);

                    idleGazeTargets.add(gs3);

                    GazeSignal gs4 = new GazeSignal("gazeRight");
                    gs.setCharacterManager(characterManager);
                    gs.setGazeShift(true);
                    gs.setOffsetAngle(30);
                    gs.setOffsetDirection(GazeDirection.UP);

                    idleGazeTargets.add(gs4);*/
                    root.setCoordinates(0.0, 1.5, 1.5);

                    // Create nodes for random IDLE gaze targets
                    TreeNode nodeAgentLeft = new TreeNode();
                    nodeAgentLeft.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                    envi.addNode(nodeAgentLeft, root);
                    TreeNode nodeAgentLeftTarget = new TreeNode();
                    nodeAgentLeftTarget.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                    envi.addNode(nodeAgentLeftTarget, nodeAgentLeft);

                    nodeAgentLeft.setCoordinates(1.5, 0.0, 0.0);
                    getIdleGazeTargets().add(nodeAgentLeftTarget);

                    TreeNode nodeAgentRight = new TreeNode();
                    nodeAgentRight.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                    envi.addNode(nodeAgentRight, root);
                    TreeNode nodeAgentRightTarget = new TreeNode();
                    nodeAgentRightTarget.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                    envi.addNode(nodeAgentRightTarget, nodeAgentRight);

                    nodeAgentRight.setCoordinates(-1.5, 0.0, 0.0);
                    getIdleGazeTargets().add(nodeAgentRightTarget);

                    TreeNode nodeAgentUp = new TreeNode();
                    nodeAgentUp.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                    envi.addNode(nodeAgentUp, root);
                    TreeNode nodeAgentUpTarget = new TreeNode();
                    nodeAgentUpTarget.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                    envi.addNode(nodeAgentUpTarget, nodeAgentUp);

                    nodeAgentUp.setCoordinates(0.0, 0.2, 0.0);
                    getIdleGazeTargets().add(nodeAgentUpTarget);

                    TreeNode nodeAgentDown = new TreeNode();
                    nodeAgentDown.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                    envi.addNode(nodeAgentDown, root);
                    TreeNode nodeAgentDownTarget = new TreeNode();
                    nodeAgentDownTarget.setIdentifier(IDProvider.createID("IdleGazeTarget").toString());
                    envi.addNode(nodeAgentDownTarget, nodeAgentDown);

                    nodeAgentDown.setCoordinates(0.0, -0.1, 0.0);
                    getIdleGazeTargets().add(nodeAgentDownTarget);

                    // Debug
                    /*
                    Leaf right = new Leaf();
                    right.setSize(0.1, 0.1, 0.1);
                    environment.addNode(right, nodeAgentRightTarget);
                    Leaf left = new Leaf();
                    left.setSize(0.1, 0.1, 0.1);
                    environment.addNode(left, nodeAgentLeftTarget);
                    Leaf up = new Leaf();
                    up.setSize(0.1, 0.1, 0.1);
                    environment.addNode(up, nodeAgentUpTarget);
                    Leaf down = new Leaf();
                    down.setSize(0.1, 0.1, 0.1);
                    environment.addNode(down, nodeAgentDownTarget);

                    // End Debug
                    */
                    Logs.info(": random idle gaze target nodes generated.");
                }else {
                    // Load nodes
                    //getIdleGazeTargets().addAll(rootNodeIdleGazeTargets.getChildren());
                }
                }


        }else {
            Logs.warning(": Environment not set, cannot generate random idle gaze target nodes.");
        }
    }

        /**
     * @return the idleGazeTargets
     */
    public List<Node> getIdleGazeTargets() {
        return idleGazeTargets;
    }

    /**
     * @param idleGazeTargets the idleGazeTargets to set
     */
    public void setIdleGazeTargets(List<Node> idleGazeTargets) {
        this.idleGazeTargets = idleGazeTargets;
    }
}
