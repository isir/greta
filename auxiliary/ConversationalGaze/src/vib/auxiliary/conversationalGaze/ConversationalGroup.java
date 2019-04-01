/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.auxiliary.conversationalGaze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import vib.auxiliary.ssi.SSIFrame;
import vib.auxiliary.ssi.SSIFramePerfomer;
import vib.auxiliary.ssi.SSITypes;
import vib.core.signals.GazeSignal;
import vib.core.util.enums.GazeDirection;
import vib.core.util.enums.Influence;
import vib.core.util.environment.Animatable;
import vib.core.util.environment.Environment;
import vib.core.util.environment.Node;
import vib.core.util.id.ID;
import vib.core.util.math.Vec3d;

/**
 * @author Donatella Simonetti
 */
public class ConversationalGroup extends Thread implements SSIFramePerfomer{

    public List<ConversationParticipant> listParticipants;
    
    ConversationParticipant user = new ConversationParticipant("user");
    boolean useradded = false;
    public Environment envi;
    
    private boolean userIsParticipating = false;
    // User head positions
    public double head_pos_x = 0;
    public double head_pos_y = 0;
    public double head_pos_z = 0;
    
    public double head_rx = 0;
    public double head_ry = 0;
    public double head_rz = 0;
    
    // cam position
    public double cam_px = 0.0;
    public double cam_py = 0.0;
    public double cam_pz = 0.0;
    
    public double cam_rx = 0.0;
    public double cam_ry = 0.0;
    public double cam_rz = 0.0;
    
    // vector were we store if the User is looking to the agent(0) or not(1) 
    // 0 --> mutual look
    // 1 --> look away
    private int[] vecGazeState = new int[6]; 
    private int counter = 0;
    
    public boolean groupActive = false;
    
    private List<GazeDirection> listGazeDirection;
    
    public ConversationalGroup(Environment env){
        this.listParticipants = new ArrayList<>();
        envi = env;
        
        this.user.setGazeStatus(1);  // initialize gaze state at look away
        this.user.setOldGazeStatus(1); 
        
        // create a node user where can we send and store the position/orientation of user head
        Animatable user = new Animatable();
        // check if the node for the user has been already created 
        Node check = envi.getNode("user");
        if (check == null){
            user.setIdentifier("user");
            envi.addNode(user);
        }
        
        this.listGazeDirection = new ArrayList<GazeDirection>();
        this.listGazeDirection.add(GazeDirection.FRONT);
        this.listGazeDirection.add(GazeDirection.UP);
        this.listGazeDirection.add(GazeDirection.UPRIGHT);
        this.listGazeDirection.add(GazeDirection.DOWN);
        this.listGazeDirection.add(GazeDirection.LEFT);
        this.listGazeDirection.add(GazeDirection.RIGHT);
        this.listGazeDirection.add(GazeDirection.DOWNLEFT);
        this.listGazeDirection.add(GazeDirection.UPLEFT);
        this.listGazeDirection.add(GazeDirection.DOWNRIGHT);
        
        this.start();
    }
    
    public void run() {
        while(groupActive){
            if (!useradded){
                listParticipants.remove("user");
            }
            if (listParticipants.size() > 1){ // check if there are at least 2 participants

                double currentTime = vib.core.util.time.Timer.getTimeMillis();

                //list name of participant
                List<String> list_names = new ArrayList <String>();

                // add the environment to the list of tha participant so we can chose also to look at 
                // something different from one of the participant
                // list_names.add("env");

                // add the participants name to the list 
                for (ConversationParticipant cp : listParticipants){
                    list_names.add(cp.getName());
                }

                for (ConversationParticipant cp : listParticipants){
                    if (!cp.getName().equals("user")){
                        if (cp.isIsTalking()){ // speaker

                            if (cp.getGazeStatus() == 0){// if it is looking at someone
                                if (!cp.lastGazeTarget.isEmpty()){ // make sure the last target wasn't the env or empty
                                    if((currentTime - cp.timeGazingatTarget) > cp.getTime_MG() ){ // if the mutual gaze time is over 

                                        cp.timeGazingatTarget = currentTime;

                                        //*****************
                                        // TODO: according to dominance or affiliation choose who looking at or env
                                        //******************

                                        GazeSignal gs = new GazeSignal("gaze");

                                        if (listParticipants.size() > 3){ // randomly choose where to gaze
                                            
                                            list_names.add("env"); // add the environment to the choices 
                                            
                                            // create the gaze signal to look at the agent
                                            String choice = RandomName(list_names);
                                            
                                            if (choice == "env"){
                                                gs = createGazeSignal("", cp);
                                                cp.setGazeStatus(1);
                                            }else{
                                                gs = createGazeSignal(choice, cp);
                                                cp.lastGazeTarget = choice;
                                            }
                                            // add the signal to the list of gazeSignal of the current considered agent
                                            cp.addGzSignal(gs); 
                                            list_names.remove("env");
                                        }else{// just two participant so once finish to gaze to the other one, gaze at env       
                                            cp.setGazeStatus(1);
                                            gs = createGazeSignal("", cp);
                                            // add the signal to the list of gazeSignal of the current considered agent
                                            cp.addGzSignal(gs);
                                        }

                                    }
                                }
                            }else{ // looking at the env and not at someone --> look away --> gaze status 1
                                if (cp.lastGazeTarget.isEmpty()){ // env lastTarget lastGazeTarget=""
                                    if((currentTime - cp.timeGazingatTarget) > cp.getTime_LA()){ // check time look_away is expired

                                        cp.setGazeStatus(0); // change gaze status to mutual gaze --> 0
                                        cp.timeGazingatTarget = currentTime;

                                        //*****************
                                        // TODO: should check dominance and affiliation and choose wko look at
                                        //*****************

                                        // if no model --> randomly chose another agent 
                                        list_names.remove(cp.getName());
                                        String choice = RandomName(list_names);

                                        // create the gaze signal to look at the agent 
                                        GazeSignal gs = createGazeSignal(choice, cp);

                                        // add the signal to the list of gazeSignal of the current considered agent
                                        cp.addGzSignal(gs);
                                        cp.lastGazeTarget = choice;

                                        // add again the agent
                                        list_names.add(cp.getName());

                                    }
                                }else if(!cp.lastGazeTarget.isEmpty()){ // start point
                                    cp.timeGazingatTarget = currentTime;

                                    GazeSignal gs = createGazeSignal("", cp);
                                    // add the signal to the list of gazeSignal of the current considered agent
                                    cp.addGzSignal(gs);
                                }
                            }
                        }else { // Hearer
                            boolean speakerFound = false;
                            if (cp.getGazeStatus() == 0){// it is looking at someone

                                // check if it was looking at the speaker
                                boolean targetisSpeaking = false;
                                String nametarget = "";

                                // check if there is a speaker
                                for (ConversationParticipant sp : listParticipants){                      
                                    if (sp.getName().equals(cp.lastGazeTarget)){                           
                                        if (sp.isIsTalking()){ // speaker found
                                            targetisSpeaking = true;
                                        }
                                    }else{
                                        if (sp.isIsTalking()){ // speaker found
                                            speakerFound = true;
                                            nametarget = sp.getName();
                                        }
                                    }
                                }

                                // look at someone else randomly
                                if (targetisSpeaking){ 
                                    // if mutual_gaze time + constent time  is expired
                                    if((currentTime - cp.timeGazingatTarget) > cp.getTime_MG() + cp.Timeplus_lookingAtSpeaker){ 

                                        GazeSignal gs = new GazeSignal("gaze");

                                        if (list_names.size() > 2){ // randomly choose where to gaze

                                            list_names.add("env"); // add the environment to the choices 
                                            
                                           String choice = RandomName(list_names);

                                            if (choice.equals("env")){
                                                gs = createGazeSignal("", cp);
                                                cp.setGazeStatus(1);
                                            }else if(choice.equals(cp.getName())){
                                                while(choice.equals(cp.getName())){
                                                    choice = RandomName(list_names);
                                                }
                                                // create the gaze signal to look at the agent
                                                gs = createGazeSignal(choice, cp);
                                                cp.lastGazeTarget = choice;
                                            }else{
                                                gs = createGazeSignal(choice, cp);
                                                cp.lastGazeTarget = choice;
                                            }

                                            list_names.remove("env");

                                            // add the signal to the list of gazeSignal of the current considered agent
                                            cp.addGzSignal(gs);                           
                                            cp.timeGazingatTarget = currentTime; // new target so reset the time

                                        }else{// just two participant, so once finish to gaze to the other one, gaze at env

                                            cp.setGazeStatus(1);
                                            gs = createGazeSignal("", cp);
                                            // add the signal to the list of gazeSignal of the current considered agent
                                            cp.addGzSignal(gs);
                                            cp.timeGazingatTarget = currentTime;
                                        }
                                    }
                                }else if(speakerFound){  // if time to look at the last agent is expired look at the new one that is the speaker
                                    if((currentTime - cp.timeGazingatTarget) > cp.getTime_MG()){
                                        GazeSignal gs = createGazeSignal(nametarget, cp);
                                        // add the signal to the list of gazeSignal of the current considered agent

                                        cp.addGzSignal(gs);
                                        cp.lastGazeTarget = nametarget;
                                        cp.timeGazingatTarget = currentTime; // new target so reset the time
                                    }
                                }else{ // if anyone is talking 
                                    if((currentTime - cp.timeGazingatTarget) > cp.getTime_MG()){

                                        GazeSignal gs = new GazeSignal("gaze");

                                        if (list_names.size() > 2){ // randomly choose where to gaze

                                            list_names.add("env"); // add the environment to the choices 

                                            String choice = RandomName(list_names);

                                            if(choice.equals("env")){ // if the target is the env, the GazeStatus is look away = 1
                                                cp.setGazeStatus(1);
                                                // create the gaze signal to look at the agent
                                                gs = createGazeSignal("", cp);
                                            }else if (choice.equals(cp.getName())){
                                                while(choice.equals(cp.getName())){
                                                    choice = RandomName(list_names);
                                                }
                                                // create the gaze signal to look at the agent
                                                gs = createGazeSignal(choice, cp);
                                                cp.lastGazeTarget = choice; // update target
                                            }else {
                                                gs = createGazeSignal(choice, cp);
                                                cp.lastGazeTarget = choice; // update target
                                            }
                                            // add the signal to the list of gazeSignal of the current considered agent
                                            cp.addGzSignal(gs);
                                            cp.timeGazingatTarget = currentTime; // new target so reset the time
                                            list_names.remove("env");

                                        }else{// just two participant, so once finish to gaze to the other one, gaze at env
                                            cp.setGazeStatus(1);
                                            gs = createGazeSignal("", cp);
                                            // add the signal to the list of gazeSignal of the current considered agent
                                            cp.addGzSignal(gs);
                                            cp.timeGazingatTarget = currentTime;
                                        }
                                    }
                                }
                            }else{ // GazeStatus = 1 --> look away
                                if((currentTime - cp.timeGazingatTarget) > cp.getTime_LA()){
                                    GazeSignal gs = new GazeSignal("gaze");
                                    if (list_names.size() > 2){// if more than two participant no problem
                                        
                                        String choice = RandomName(list_names);

                                        if (choice.equals(cp.getName())){
                                            while(choice.equals(cp.getName())){
                                                choice = RandomName(list_names);
                                            }
                                        }                                  
                                        // create the gaze signal to look at the agent
                                        gs = createGazeSignal(choice, cp);
                                        cp.lastGazeTarget = choice; // update target
                                    }else{                                   
                                        String targetname = "";
                                        for (String s: list_names){
                                            if (!s.equals(cp.getName())){
                                                targetname = s;
                                            } 
                                        }
                                        gs = createGazeSignal(targetname, cp);
                                        cp.lastGazeTarget = targetname; // update target
                                    }

                                    cp.setGazeStatus(0);
                                    // add the signal to the list of gazeSignal of the current considered agent
                                    cp.addGzSignal(gs);
                                    cp.timeGazingatTarget = currentTime; // new target so reset the time
                                }
                            }   
                        }
                    }
                }
            }
        }
    }
    
    public String RandomName (List<String> list_names){
        
        int max = list_names.size() - 1;
        int min = 0;
        Random rn = new Random();
        int randomName = rn.nextInt(max - min + 1) + min;

        // create the gaze signal to look at the agent
        String choice = list_names.get(randomName);
        
        return choice;
    }
    
    public GazeSignal createGazeSignal(String target, ConversationParticipant agent){
        
        // create gaze signal
        GazeSignal gs = new GazeSignal("gaze");
        
        gs.setCharacterManager(agent.getCharacterManager());
        gs.setGazeShift(true);
        //dummy time values
        gs.getTimeMarker("start").setValue(0.0);
        gs.getTimeMarker("end").setValue(0.4);

        gs.setTarget(target);           

        if (target.equals("user")){ // look at the user
            //radius to normalize the position and compute the angle via acos
            double radius_yaw = Math.sqrt(Math.pow(head_pos_z, 2) + Math.pow(head_pos_x, 2));
            double radius_pitch = Math.sqrt(Math.pow(head_pos_y, 2) + Math.pow(head_pos_z, 2));            
            double z = Math.abs(head_pos_z)/radius_yaw;
            double y = Math.abs(head_pos_y)/radius_pitch;
            
        
            // set the gaze influence
            if (Math.toDegrees(Math.acos(y)) > 15 || Math.toDegrees(Math.acos(z)) > 25){
                gs.setInfluence(Influence.HEAD);
            }else if(Math.toDegrees(Math.acos(z)) > 40){
                gs.setInfluence(Influence.TORSO);
            }else{
                gs.setInfluence(Influence.EYES);
            } 
        }else if (target.isEmpty()) { // look away
            
            gs.setTarget(agent.lastGazeTarget); 
            // take randomly a direction 
            int max = 8; // total number of GazeDirection
            int min = 0; 
            Random rn = new Random();
            int randomDirection = rn.nextInt(max - min + 1) + min;
            
            gs.setOffsetDirection(listGazeDirection.get(randomDirection));
            // shift angle set to 30
            gs.setOffsetAngle(10); // we move also the head 
            //gs.setInfluence(Influence.HEAD);
        }
        //}
        return gs;
    }
    @Override
    public void performSSIFrames(List<SSIFrame> ssi_frames_list, ID requestId) {
        for (SSIFrame ssf : ssi_frames_list) {
            performSSIFrame(ssf, requestId);
        }
    }

    @Override
    public void performSSIFrame(SSIFrame ssi_frame, ID requestId) {
        
        if (!useradded){
            this.listParticipants.add(this.user);
            useradded = true;
        }
        //this.userIsParticipating = true;
        double currentTime = vib.core.util.time.Timer.getTime();
        
        // gazeSignal to sent to the Realizer
        //ArrayList<Signal> toSend = new ArrayList<Signal>();
        
        // take the head position from the xml message
        head_pos_x = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_position_x);
        head_pos_y = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_position_y);
        head_pos_z = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_position_z);
        
        head_rx = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_orientation_pitch);
        head_ry = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_orientation_yaw);
        head_rz = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_orientation_roll);
        
        // update the head position according the camera setting
        //cam position
        if (cam_px!=0 || cam_py!=0 || cam_pz!=0){
            head_pos_x += cam_px;
            head_pos_y += cam_py;
            head_pos_z += cam_pz;
        }
        // rotate around horizontal (x for cam), i.e. z axis of GRETA
        if (cam_rx != 0.0){
            double h_pos_x = head_pos_x*Math.cos(cam_rx) + Math.sin(cam_rx)*(head_pos_z);
            double h_pos_z = -head_pos_x*Math.sin(cam_rx) + Math.cos(cam_rx)*(head_pos_z);
            
            head_pos_x = h_pos_x;
            head_pos_z = h_pos_z; 
        }
        // rotate around vertical (y for cam), i.e. y axis of GRETA
        if (cam_ry != 0.0){
            double h_pos_y = head_pos_y*Math.cos(cam_ry) - Math.sin(cam_ry)*(head_pos_z);
            double h_pos_z = head_pos_y*Math.sin(cam_ry) + Math.cos(cam_ry)*(head_pos_z);
            
            head_pos_y = h_pos_y;
            head_pos_z = h_pos_z; 
        }
         // rotate around depth axis(z for cam), i.e. x axis of GRETA
        if (cam_rz != 0.0){
            double h_pos_x = head_pos_x*Math.cos(cam_rz) - Math.sin(cam_rz)*head_pos_y;
            double h_pos_y = head_pos_x*Math.sin(cam_rz) + Math.cos(cam_rz)*head_pos_y;
            
            head_pos_x = h_pos_x;
            head_pos_y = h_pos_y; 
        }    
        
        // check that the user is looking at certain region of the screen = agent face 
        // if the user is looking at agent face the state is 0
        // otherway state = 1 (look away)
        int currentUserState = 0; // to be changed 
        
        // normalize the head position
        double posX_norm = head_pos_x/Math.sqrt(Math.pow(head_pos_x, 2)+Math.pow(head_pos_z, 2));
        double posY_norm = head_pos_y/Math.sqrt(Math.pow(head_pos_y, 2)+Math.pow(head_pos_z, 2));
        // check the user is looking at the agent face
        double ratio1 = posX_norm/Math.sin(head_ry);
        double ratio2 = posY_norm/Math.sin(head_rx);
        
        // compute currentUserState
        if(Math.abs(ratio1) > 0.3 || Math.abs(ratio2) > 0.3){ // look away
            currentUserState = 1;
        }else{// look at the agent
            currentUserState = 0;
        }
        
        // check that 6 frames are passed
        if (counter < 5){
            vecGazeState[counter] = currentUserState;
            counter += 1;        
        }else{ // check each 6 frames if generate the gaze for the agent 
            vecGazeState[counter] = currentUserState;
            double sumW = Arrays.stream(vecGazeState).sum();
            if ((sumW/vecGazeState.length) >= 0.66){
                this.user.setGazeStatus(1);
            }else{
                 this.user.setGazeStatus(0);
            }
        }
        
        
        // update the position of the user in the environment
        Animatable us = (Animatable) envi.getNode("user");
        // the eyesweb give a coordination system different from what we have in Greta
        // eyw-->Grata: x-->-z, z-->x
        us.setCoordinates(new Vec3d(1+head_pos_z,head_pos_y, -head_pos_x));
        //us.setOrientation(0, 0, 0); // send also the orientation
        
        //TODO : think a smart way to check if we are receiving the data for the user and how
        // make the variable false or true 
        //this.userIsParticipating = false;
    }

    public void AddParticipant(AgentGazeUser agu){
        this.listParticipants.add(agu.getAgent());
    }
    
    public void RemoveParticipant (AgentGazeUser agu){
        this.listParticipants.remove(agu.getAgent());
    }
}
