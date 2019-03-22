/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.auxiliary.conversationalGaze;

import java.util.ArrayList;
import java.util.List;
import vib.core.signals.GazeSignal;
import vib.core.signals.Signal;
import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;

/**
 *
 * @author Donatella Simonetti
 */
public class ConversationParticipant implements CharacterDependent{

    private CharacterManager characterManager;
    private String name;
    private Role role;
    private boolean isTalking;
    
    private double dominance;
    private double levelOfIntimacy;
    
    private double time_MG; // fixed term --> how long the agent look at the user tring to make him/her look at it back
    private double time_LA; // how long the Agent gaze aversion should be
    
    private int gazeStatus; // 0 if mutual gaze; 1 if look away
    private int oldGazeStatus;
    
    private ArrayList<Signal> gzSignals;
    public String lastGazeTarget = "";
    public double timeGazingatTarget = 0.0;
    
    public double Timeplus_lookingAtSpeaker = 1000; // msec 
    
    public boolean isGazing = true;
    
    public ConversationParticipant(CharacterManager cm){
        
        this.characterManager = cm;
        this.name = cm.getCurrentCharacterName();
        this.isTalking = false;
        this.gzSignals = new ArrayList <Signal>();
        
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
    
    public List<Signal> addGzSignal(GazeSignal gs){
        this.getGzSignals().add(gs);
        return getGzSignals();
    }
    
    public List<Signal> addGzSignal(List<GazeSignal> lgs){
        for (GazeSignal gs : lgs){
            this.getGzSignals().add(gs);
        }
        return getGzSignals();
    }
    
    /**
     * @return the gzSignals
     */
    public List<Signal> getGzSignals() {
        return gzSignals;
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
}
