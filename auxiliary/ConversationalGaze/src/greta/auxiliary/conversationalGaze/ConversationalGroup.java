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
import greta.core.util.enums.GazeDirection;
import greta.core.util.environment.Animatable;
import greta.core.util.environment.Environment;
import greta.core.util.environment.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Donatella Simonetti
 */
public class ConversationalGroup extends Thread{

    public List<ConversationParticipant> listParticipants;
    public List<ConversationParticipant> pending_Participants;

    ConversationParticipant user = new ConversationParticipant("user");
    boolean userActive = false;
    public Environment envi;

    private boolean userIsParticipating = false;
    // User head positions
    public double head_pos_x = 0;
    public double head_pos_y = 0;
    public double head_pos_z = 0;

    public double head_rx = 0;
    public double head_ry = 0;
    public double head_rz = 0;

    // vector were we store if the User is looking to the agent(0) or not(1)
    // 0 --> mutual look
    // 1 --> look away
    private int[] vecGazeState = new int[6];
    private int counter = 0;

    public boolean groupActive = true;

    private List<GazeDirection> listGazeDirection;

    private List<String> list_names = new ArrayList <String>();

    private String speaker = "";
    private String old_speaker = "";
    private double startNotspeakingTime = 0.0;

    public ConversationalGroup(Environment env){
        this.listParticipants = new ArrayList<>();
        this.pending_Participants = new ArrayList<>();
        envi = env;

        // create a node user where can we send and store the position/orientation of user head
        Animatable user = new Animatable();
        // check if the node for the user has been already created
        Node check = envi.getNode("user");
        if (check == null){
            user.setIdentifier("user");
            envi.addNode(user);

            this.user.setGazeStatus(1);  // initialize gaze state at look away
            this.user.setOldGazeStatus(1);
            this.user.setMpeg4_id("user");
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

            if (this.userActive){
                compureUserStatusAndGazeSignal();
                if (!listParticipants.contains("user")){
                    list_names.add("user");
                }
            }
            this.userActive = false;

            synchronized(listParticipants){

                if (listParticipants.size() > 1){ // check if there are at least 2 participants

                    double currentTime = greta.core.util.time.Timer.getTimeMillis();

                    // add the environment to the list of tha participant so we can chose also to look at
                    // something different from one of the participant
                    // list_names.add("env");

                    //TO DO: now the assumption is there is one speaker at time. Change when more than 1 agent is talking contemporary
                    //int num_speaker = 0;
                    speaker = "";
                    if (startNotspeakingTime == 0.0){
                        startNotspeakingTime = currentTime;
                    }


                    for (ConversationParticipant cp : listParticipants){
                        synchronized(cp){
                            if (cp.isIsTalking()){
                                speaker = cp.getMpeg4_id();//getName();
                                startNotspeakingTime = 0.0;
                                break;
                            }
                            //speaker = "";
                        }
                    }


                    //System.out.println("oldSpeaker:" + old_speaker + "    speaker: " + speaker);

                    if (!speaker.isEmpty()){
                        for (ConversationParticipant cp : listParticipants){
                            if (!cp.getMpeg4_id().equals("user") && !cp.getMpeg4_id().equals(speaker)){ //hearer
                                // look at the speaker for a certain time and then look away

                                //create the gaze
                                GazeSignal gs = new GazeSignal("gaze");

                                // check if the agent is looking at the speaker and how long
                                if (cp.lastGazeTarget.equals(speaker)){
                                    if (currentTime - cp.timeGazingatTarget > cp.getTime_MG() + cp.Timeplus_lookingAtSpeaker){
                                        gs = createGazeSignal("", cp); // look away
                                        cp.setGazeStatus(1); // update gaze status
                                        cp.timeGazingatTarget = currentTime; // update time
                                    }else{
                                        String gazeTarget = extractNameSpeaker(speaker);
                                        gs = createGazeSignal(gazeTarget, cp); // look at the speaker

                                    }
                                    cp.addSignal(gs);
                                }else {
                                    if (currentTime - cp.timeGazingatTarget > cp.getTime_LA()){ // look at the speaker
                                        String gazeTarget = extractNameSpeaker(speaker);
                                        gs = createGazeSignal(gazeTarget, cp); // look at the speaker
                                        cp.setGazeStatus(0); // update gaze status
                                        cp.timeGazingatTarget = currentTime; // update time
                                        cp.lastGazeTarget = speaker; // update gaze target

                                        cp.addSignal(gs);
                                    }
                                }

                            }else if(cp.getMpeg4_id().equals(speaker)){ // speaker

                                // at the first moment the agent start to speak look down
                                GazeSignal gs = new GazeSignal("gaze");
                                if (!old_speaker.equals(speaker) ){
                                    // look down
                                    String gazeTarget = extractNameSpeaker(cp.lastGazeTarget);
                                    gs = createGazeSignal( gazeTarget, cp);
                                    gs.setOffsetDirection(GazeDirection.DOWN);
                                    gs.setOffsetAngle(25.0);

                                    cp.timeGazingatTarget = currentTime;
                                    cp.setGazeStatus(1);

                                    cp.addSignal(gs);
                                }else{
                                    if (cp.getGazeStatus() == 1){
                                        if (currentTime - cp.timeGazingatTarget > cp.getTime_LA() + 1000){

                                            if (list_names.size() > 2){// if more than two participant no problem
                                                String choice = RandomName(list_names);

                                                if (choice.equals(cp.getMpeg4_id())){
                                                    while(choice.equals(cp.getMpeg4_id())){
                                                        choice = RandomName(list_names);
                                                    }
                                                }
                                                // create the gaze signal to look at the agent
                                                String gazeTarget = extractNameSpeaker(choice);
                                                gs = createGazeSignal(gazeTarget, cp);
                                                cp.lastGazeTarget = choice; // update target
                                            }else{
                                                String targetname = "";
                                                for (String s: list_names){
                                                    if (!s.equals(cp.getMpeg4_id())){
                                                        targetname = s;
                                                    }
                                                }
                                                String gazeTarget = extractNameSpeaker(targetname);
                                                gs = createGazeSignal(gazeTarget, cp);
                                                cp.lastGazeTarget = targetname; // update target
                                            }

                                            cp.setGazeStatus(0);
                                            // add the signal to the list of gazeSignal of the current considered agent
                                            cp.timeGazingatTarget = currentTime; // new target so reset the time
                                        }
                                        cp.addSignal(gs);
                                    }else {
                                        if (currentTime - cp.timeGazingatTarget > cp.getTime_MG()){

                                            String gazeTarget = extractNameSpeaker(cp.lastGazeTarget);
                                            gs = createGazeSignal(gazeTarget , cp);
                                            gs.setOffsetDirection(GazeDirection.DOWN);
                                            gs.setOffsetAngle(10.0);

                                            cp.timeGazingatTarget = currentTime;
                                            cp.setGazeStatus(1);
                                            cp.addSignal(gs);
                                        }
                                    }
                                }

                            }
                        }
                    }else{ // no one is spaeking
                        for (ConversationParticipant cp : listParticipants){
                                if (!cp.getMpeg4_id().equals("user")){
                                    if (cp.getGazeStatus() == 0){// it is looking at someone
                                        if((currentTime - cp.timeGazingatTarget) > cp.getTime_MG() ){

                                                GazeSignal gs = new GazeSignal("gaze");

                                                if (list_names.size() > 2){ // randomly choose where to gaze

                                                    list_names.add("env"); // add the environment to the choices

                                                   String choice = RandomName(list_names);

                                                    if (choice.equals("env")){
                                                        gs = createGazeSignal("", cp);
                                                        cp.setGazeStatus(1);
                                                    }else if(choice.equals(cp.getMpeg4_id())){
                                                        while(choice.equals(cp.getMpeg4_id())){ // check if the target is the agent itself
                                                            choice = RandomName(list_names);
                                                        }
                                                        if (choice.equals("env")){
                                                            gs = createGazeSignal("", cp);
                                                            cp.setGazeStatus(1);
                                                        }else{
                                                            // create the gaze signal to look at the agent or user
                                                            //System.out.println("*************** target " + choice);
                                                            String gazeTarget = extractNameSpeaker(choice);
                                                            gs = createGazeSignal(gazeTarget, cp);
                                                            cp.lastGazeTarget = choice;
                                                        }
                                                    }else{
                                                        //System.out.println("*************** target " + choice);
                                                        String gazeTarget = extractNameSpeaker(choice);
                                                        gs = createGazeSignal(gazeTarget, cp);
                                                        cp.lastGazeTarget = choice;
                                                    }

                                                    list_names.remove("env");

                                                    // add the signal to the list of gazeSignal of the current considered agent
                                                    cp.addSignal(gs);
                                                    cp.timeGazingatTarget = currentTime; // new target so reset the time

                                                }else{// just two participant, so once finish to gaze to the other one, gaze at env

                                                    cp.setGazeStatus(1);
                                                    gs = createGazeSignal("", cp);
                                                    // add the signal to the list of gazeSignal of the current considered agent
                                                    cp.addSignal(gs);
                                                    cp.timeGazingatTarget = currentTime;
                                                }
                                            }
                                    }else{ // GazeStatus = 1 --> look away
                                        if((currentTime - cp.timeGazingatTarget) > cp.getTime_LA() ){
                                            GazeSignal gs = new GazeSignal("gaze");
                                            if (list_names.size() > 2){// if more than two participant no problem

                                                String choice = RandomName(list_names);

                                                if (choice.equals(cp.getMpeg4_id())){
                                                    while(choice.equals(cp.getMpeg4_id())){
                                                        choice = RandomName(list_names);
                                                    }
                                                }
                                                // create the gaze signal to look at the agent
                                                //System.out.println("*************** target " + choice);
                                                String gazeTarget = extractNameSpeaker(choice);
                                                gs = createGazeSignal(gazeTarget, cp);
                                                cp.lastGazeTarget = choice; // update target
                                            }else{
                                                String targetname = "";
                                                for (String s: list_names){
                                                    if (!s.equals(cp.getMpeg4_id())){
                                                        targetname = s;
                                                    }
                                                }
                                                //System.out.println("*************** target " + targetname);
                                                String gazeTarget = extractNameSpeaker(targetname);
                                                gs = createGazeSignal(gazeTarget, cp);
                                                cp.lastGazeTarget = targetname; // update target
                                            }

                                            cp.setGazeStatus(0);
                                            // add the signal to the list of gazeSignal of the current considered agent
                                            cp.addSignal(gs);
                                            cp.timeGazingatTarget = currentTime; // new target so reset the time
                                        }
                                    }
   //                             }
                            }
                        }
                    }
                }

                old_speaker = speaker;

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

       //System.out.println(choice);
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
            // shift angle set to 10 degree
            gs.setOffsetAngle(15); // we move also the head
            //gs.setInfluence(Influence.HEAD);
        }
        //}
        return gs;
    }

    public void compureUserStatusAndGazeSignal() {
        //this.userIsParticipating = true;
        double currentTime = greta.core.util.time.Timer.getTime();

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
    }

    public void AddParticipant(AgentGazeUser agu){

        this.pending_Participants.add(agu.getAgent());

        // add the participants name to the list
        if (!listParticipants.contains(agu.getAgent().getMpeg4_id())){
            list_names.add(agu.getAgent().getMpeg4_id());
        }

    }

    public void RemoveParticipant (AgentGazeUser agu){
        this.listParticipants.remove(agu.getAgent());

        // add the participants name to the list
        if (listParticipants.contains(agu.getAgent().getMpeg4_id())){
            listParticipants.remove(agu.getAgent().getMpeg4_id());
        }
    }

    public void AddParticipantUser(User user){

        this.pending_Participants.add(user.getUser());

        // add the participants name to the list
        if (!listParticipants.contains(user.getUser().getMpeg4_id()) && this.userActive){
            list_names.add(user.getUser().getMpeg4_id());
        }

        user.setGroup(this);
        this.user = user.getUser();
    }

    public void RemoveParticipantUser (User user){
        this.listParticipants.remove(user.getUser());

        // add the participants name to the list
        if (listParticipants.contains(user.getUser().getMpeg4_id())){
            listParticipants.remove(user.getUser().getMpeg4_id());
        }
    }

    public String extractNameSpeaker (String mpeg4_id){
        if (mpeg4_id.equals("user")){
            return "user";
        }else{
            MPEG4Animatable agentSpeaker = (MPEG4Animatable) this.envi.getNode(mpeg4_id);
            return agentSpeaker.getCharacterManager().getCurrentCharacterName();
        }
    }

    public ConversationParticipant getUser() {
        return user;
    }

    public void setUser(ConversationParticipant user) {
        this.user = user;
    }
}
