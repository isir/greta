/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.ssi;

import java.util.ArrayList;
import java.util.List;
import vib.core.signals.GazeSignal;
import vib.core.signals.Signal;
import vib.core.signals.SignalEmitter;
import vib.core.signals.SignalPerformer;
import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.Mode;
import vib.core.util.enums.CompositionType;
import vib.core.util.enums.Influence;
import vib.core.util.environment.Animatable;
import vib.core.util.environment.Environment;
import vib.core.util.environment.Node;
import vib.core.util.id.ID;
import vib.core.util.math.Vec3d;

/**
 *
 * @author donat
 */
public class SSIFrameToGazeSignal implements SSIFramePerfomer, SignalEmitter, SignalPerformer, CharacterDependent{
    
    private List<SignalPerformer> signalPerformers = new ArrayList<SignalPerformer>();
    private CharacterManager characterManager;
    public Environment envi;
    
    //Vec3d anentHeadPos; 
    // head positions
    private double head_pos_x = 0;
    private double head_pos_y = 0;
    private double head_pos_z = 0;
    
    public double cam_x = 0.0;
    public double cam_y = 0.0;
    public double cam_z = 0.0;
    
    public double cam_yaw = 0.0;
    public double cam_pitch = 0.0;
    public double cam_roll = 0.0;
    
    
    public SSIFrameToGazeSignal(CharacterManager cm){  
        
        setCharacterManager(cm);
        envi = characterManager.getEnvironment();
        
        // create a node user where can we send and store the position/orientation of head
        Animatable user = new Animatable();
        user.setIdentifier("user");
        envi.addNode(user);
    }

    @Override
    public void performSSIFrames(List<SSIFrame> ssi_frames_list, ID requestId) {
        for (SSIFrame ssf : ssi_frames_list) {
            performSSIFrame(ssf, requestId);
        }
        
    }

    @Override
    public void performSSIFrame(SSIFrame ssi_frame, ID requestId) {     
        ArrayList<Signal> toSend = new ArrayList<Signal>();
        
        head_pos_x = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_position_x);
        head_pos_y = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_position_y);
        head_pos_z = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_position_z);
        
        // check if the camera is used
        // if so made translation and rotation according to the different position
        // between camera anche eyes of the agent 
        if (cam_x != 0.0 || cam_y != 0.0 || cam_z != 0.0){
             head_pos_z = head_pos_z + cam_x;
             head_pos_y = head_pos_y + cam_y;
             head_pos_x = -(-head_pos_x + cam_z);
        }
        // rotate around horizontal, i.e. z axis of GRETA
        if (cam_yaw != 0.0){
            head_pos_y = head_pos_y*Math.cos(cam_yaw) - Math.sin(cam_yaw)*(head_pos_x);
            head_pos_x = -(head_pos_y*Math.sin(cam_yaw) + Math.cos(cam_yaw)*(head_pos_x));
        }
        // rotate around vertical, i.e. y axis of GRETA
        if (cam_pitch != 0.0){
            head_pos_z = head_pos_z*Math.cos(cam_pitch) + Math.sin(cam_pitch)*(head_pos_x);
            head_pos_x = -(-head_pos_z*Math.sin(cam_pitch) + Math.cos(cam_pitch)*(head_pos_x));
        }
        // rotate around depyh axis, i.e. x axis of GRETA
        if (cam_roll != 0.0){
            head_pos_z = head_pos_z*Math.cos(cam_roll) - Math.sin(cam_roll)*head_pos_y;
            head_pos_y = head_pos_z*Math.sin(cam_roll) + Math.cos(cam_roll)*head_pos_y;

        }
        
        if (head_pos_x != 0 && head_pos_y != 0 && head_pos_z != 0) {
            GazeSignal gs = new GazeSignal("gaze");
            gs.setGazeShift(true);
            
            //dummy time values
            gs.getTimeMarker("start").setValue(0.0);
            gs.getTimeMarker("end").setValue(0.5);

            gs.setTarget("user");
            
            
            // set the gaze influence
            if (Math.abs(head_pos_x) > 45.0 || Math.abs(head_pos_y) > 50.0){
                gs.setInfluence(Influence.HEAD);
            }else if(Math.abs(head_pos_x)  > 60.0 ){
                 gs.setInfluence(Influence.SHOULDER);
            }else if(Math.abs(head_pos_x)  > 75.0 ){
                 gs.setInfluence(Influence.TORSO);
            }else{  
                 gs.setInfluence(Influence.EYES);
            } 
            
            //Vec3d agent_head = envi.getRoot().getChildren()
            //*************************************
            // update the position of the user in the environment
            Animatable us = (Animatable) envi.getNode("user");
            // the eyesweb give a coordination system differetn from what we have in Greta
            // eyw-->Gratq: x-->-z, z-->x
            us.setCoordinates(new Vec3d(head_pos_z/10,head_pos_y/10, -head_pos_x/10));
            //us.setOrientation(0, 0, 0); // send also the orientation
            
            toSend.add(gs);
        }
        
        this.performSignals(toSend, requestId, new Mode(CompositionType.replace));
        
    }
    
    @Override
    public void performSignals(List<Signal> signals, ID requestId, Mode mode) {
    
        // send the Gaze Signals to Realizer
        for(SignalPerformer sp : signalPerformers){
            sp.performSignals(signals, requestId, mode);
        }
        
    }

    @Override
    public void addSignalPerformer(SignalPerformer performer) {
        signalPerformers.add(performer);
    }

    @Override
    public void removeSignalPerformer(SignalPerformer performer) {
        signalPerformers.add(performer);
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
    public void setCharacterManager(CharacterManager characterManager) {
        this.characterManager = characterManager;
    }

    
    
    
}
