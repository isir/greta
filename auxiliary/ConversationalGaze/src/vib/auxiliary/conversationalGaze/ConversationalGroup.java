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
import java.util.logging.Level;
import java.util.logging.Logger;
import vib.auxiliary.ssi.SSIFrame;
import vib.auxiliary.ssi.SSIFramePerfomer;
import vib.auxiliary.ssi.SSITypes;
import vib.core.signals.GazeSignal;
import vib.core.util.enums.GazeDirection;
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
    public List<ConversationParticipant> pending_Participants;
    
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
    
    public boolean groupActive = true;
    
    private List<GazeDirection> listGazeDirection;
    
    private List<String> list_names = new ArrayList <String>();
    
    private String speaker = ""; 
    
    public ConversationalGroup(Environment env){
        this.listParticipants = new ArrayList<>();
        this.pending_Participants = new ArrayList<>();
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
        //this.listGazeDirection.add(GazeDirection.UP);
        //this.listGazeDirection.add(GazeDirection.UPRIGHT);
        this.listGazeDirection.add(GazeDirection.DOWN);
        this.listGazeDirection.add(GazeDirection.LEFT);
        this.listGazeDirection.add(GazeDirection.RIGHT);
        this.listGazeDirection.add(GazeDirection.DOWNLEFT);
        //this.listGazeDirection.add(GazeDirection.UPLEFT);
        this.listGazeDirection.add(GazeDirection.DOWNRIGHT);
        
        this.start();
    }
    
    public void run() {
        
        while(groupActive){
                    
            synchronized(listParticipants){
                if (listParticipants.size() > 1){ // check if there are at least 2 participants

                    double currentTime = vib.core.util.time.Timer.getTimeMillis();

                    // add the environment to the list of tha participant so we can chose also to look at 
                    // something different from one of the participant
                    // list_names.add("env");
               
                    
                    for (ConversationParticipant cp : listParticipants){
                        synchronized(cp){
                            if (cp.isIsTalking()){
                                speaker = cp.getName();
                                break;
                            }
                            speaker = "";
                        }
                    }
                    
                    //System.out.println("********************* speaker***********************: " + speaker);

                    // add the environment to the list of tha participant so we can chose also to look at 
                    // something different from one of the participant
                    // list_names.add("env");
                    
                    if (!speaker.isEmpty()){
                        for (ConversationParticipant cp : listParticipants){
                            if (!cp.getName().equals("user") && !cp.getName().equals(speaker)){ //hearer
                                GazeSignal gs = new GazeSignal("gaze");
                                gs = createGazeSignal(speaker, cp);
                                cp.addGzSignal(gs);
                                if (cp.getGazeStatus() == 1){
                                    cp.timeGazingatTarget = currentTime;
                                }
                                if (!cp.lastGazeTarget.equals(speaker)){
                                    cp.lastGazeTarget = speaker;
                                }
                                cp.setGazeStatus(0); 
                            }else if(cp.getName().equals(speaker)){ // speaker
                                GazeSignal gs = new GazeSignal("gaze");
                                gs.setOffsetDirection(GazeDirection.FRONT);
                                gs.setOffsetAngle(0.0);
                                gs.setGazeShift(true);
                                gs.getTimeMarker("start").setValue(0.0);
                                gs.getTimeMarker("end").setValue(0.4);
                                
                                cp.addGzSignal(gs);
                                if (cp.getGazeStatus() == 0){
                                    cp.timeGazingatTarget = currentTime;
                                }
                                cp.setGazeStatus(1);
                            }
                        }
                    }else{
                        for (ConversationParticipant cp : listParticipants){
                            if (!cp.getName().equals("user")){
                                if (cp.getGazeStatus() == 0){// it is looking at someone
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
                                                    if (choice.equals("env")){
                                                        gs = createGazeSignal("", cp);
                                                        cp.setGazeStatus(1);
                                                    }else{
                                                        // create the gaze signal to look at the agent
                                                        gs = createGazeSignal(choice, cp);
                                                        cp.lastGazeTarget = choice;
                                                    }
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
                    
                    /*speaker = "";
                    for (ConversationParticipant cp : listParticipants){
                        if (!cp.getName().equals("user")){
                            if (cp.isIsTalking()){ // speaker

                                speaker = cp.getName();
                                //System.out.println(cp.getName() + " is talking ");
                                GazeSignal gs = new GazeSignal("gaze");
                                gs.setOffsetDirection(GazeDirection.FRONT);
                                gs.setOffsetAngle(0.0);
                                gs.setGazeShift(true);
                                gs.getTimeMarker("start").setValue(0.0);
                                gs.getTimeMarker("end").setValue(0.4);
                                
                                cp.addGzSignal(gs);
                                cp.setGazeStatus(1); 

                            }else { // Hearer
                                //boolean speakerFound = false;
                                if (cp.getGazeStatus() == 0){// it is looking at someone
                                    
                                    if (!speaker.isEmpty()){ // look at the speaker
                                        
                                        GazeSignal gs = new GazeSignal("gaze");
                                        gs = createGazeSignal(speaker, cp);
                                        cp.addGzSignal(gs);
                                        
                                    }else { // no one is speaking
                                        
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
                                                    if (choice.equals("env")){
                                                        gs = createGazeSignal("", cp);
                                                        cp.setGazeStatus(1);
                                                    }else{
                                                        // create the gaze signal to look at the agent
                                                        gs = createGazeSignal(choice, cp);
                                                        cp.lastGazeTarget = choice;
                                                    }
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
                                    }
                                }else{ // GazeStatus = 1 --> look away
                                    
                                    if (!speaker.isEmpty()){
                                        
                                        GazeSignal gs = new GazeSignal("gaze");
                                        gs = createGazeSignal(speaker, cp);
                                        cp.addGzSignal(gs);
                                        cp.setGazeStatus(0);
                                        
                                    }else{
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
                    }*/
                }
                
                synchronized(pending_Participants){
                    if (!pending_Participants.isEmpty()){
                            for (ConversationParticipant cp :  pending_Participants){
                                listParticipants.add(cp);
                                //pending_Participants.remove(cp);
                            } 
                            
                            pending_Participants.clear();
                    }
                }
            }
            try {
                Thread.sleep(50); //ms
            } catch (InterruptedException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
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
            /*if (Math.toDegrees(Math.acos(y)) > 15 || Math.toDegrees(Math.acos(z)) > 25){
                gs.setInfluence(Influence.HEAD);
            }else if(Math.toDegrees(Math.acos(z)) > 40){
                gs.setInfluence(Influence.TORSO);
            }else{
                gs.setInfluence(Influence.EYES);
            }*/
        }else if (target.isEmpty()) { // look away
            
            gs.setTarget(agent.lastGazeTarget); 
            // take randomly a direction 
            int max = 4; // total number of GazeDirection -1
            int min = 0; 
            Random rn = new Random();
            int randomDirection = rn.nextInt(max - min + 1) + min;
            
            gs.setOffsetDirection(listGazeDirection.get(randomDirection));
            // shift angle set to 30
            gs.setOffsetAngle(15); // we move also the head 
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
            
            counter = 0;
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
        
        this.pending_Participants.add(agu.getAgent());
        
        // add the participants name to the list 
        if (!listParticipants.contains(agu.getAgent().getName())){
            list_names.add(agu.getAgent().getName());
        }

    }
    
    public void RemoveParticipant (AgentGazeUser agu){
        this.listParticipants.remove(agu.getAgent());
        
        // add the participants name to the list 
        if (listParticipants.contains(agu.getAgent().getName())){
            listParticipants.remove(agu.getAgent().getName());
        }
    }
}
