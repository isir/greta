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

import greta.auxiliary.ssi.SSIFrame;
import greta.auxiliary.ssi.SSIFramePerfomer;
import greta.auxiliary.ssi.SSITypes;
import greta.core.util.environment.Animatable;
import greta.core.util.environment.Environment;
import greta.core.util.environment.Node;
import greta.core.util.id.ID;
import greta.core.util.math.Vec3d;
import java.util.List;

/**
 *
 * @author Donatella Simonetti
 */
public class User implements SSIFramePerfomer{

    private AgentGazeUser agent;
    private ConversationalGroup group;
    private Environment envi;
    private ConversationParticipant user;

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

    private double voiceEnergy = 0.0;
    private boolean userIsSpeaking = false;
    private double threshouldIntensity = 0.25;

    public User (Environment envi){
        this.envi = envi;

        this.user =new ConversationParticipant("user");
        this.user.setGazeStatus(1);  // initialize gaze state at look away
        this.user.setOldGazeStatus(1);
        this.user.setMpeg4_id("user");

        Node check = this.envi.getNode("user");
        if (check == null){
            Animatable user = new Animatable();
            user.setIdentifier("user");
            envi.addNode(user);
        }

    }

    @Override
    public void performSSIFrames(List<SSIFrame> ssi_frame, ID requestId) {


    }

    @Override
    public void performSSIFrame(SSIFrame ssi_frame, ID requestId) {

        // take the head position from the xml message (in meters)
        head_pos_x = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_position_x);
        head_pos_y = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_position_y);
        head_pos_z = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_position_z);

        // (degree)
        head_rx = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_orientation_pitch);
        head_ry = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_orientation_yaw);
        head_rz = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_orientation_roll);


        this.voiceEnergy = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.prosody_opensmile_energy_cat);

        if (getVoiceEnergy() > threshouldIntensity){
            user.setIsTalking(true);
        }else{
            user.setIsTalking(false);
        }
        // update the head position according the camera settings
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

            head_rx += - cam_rx;//Math.toDegrees(cam_rx);
            head_pos_x = h_pos_x;
            head_pos_z = h_pos_z;
        }
        // rotate around vertical (y for cam), i.e. y axis of GRETA
        if (cam_ry != 0.0){
            double h_pos_y = head_pos_y*Math.cos(cam_ry) - Math.sin(cam_ry)*(head_pos_z);
            double h_pos_z = head_pos_y*Math.sin(cam_ry) + Math.cos(cam_ry)*(head_pos_z);

            head_ry += - cam_ry;//Math.toDegrees(cam_ry);
            head_pos_y = h_pos_y;
            head_pos_z = h_pos_z;
        }
         // rotate around depth axis(z for cam), i.e. x axis of GRETA
        if (cam_rz != 0.0){
            double h_pos_x = head_pos_x*Math.cos(cam_rz) - Math.sin(cam_rz)*head_pos_y;
            double h_pos_y = head_pos_x*Math.sin(cam_rz) + Math.cos(cam_rz)*head_pos_y;

            head_rz += - cam_rz;//Math.toDegrees(cam_rz);
            head_pos_x = h_pos_x;
            head_pos_y = h_pos_y;
        }

        // update the position of the user in the environment
        Animatable us = (Animatable) envi.getNode("user");
        // the eyesweb give a coordination system different from what we have in Greta
        // eyw-->Grata: x-->-z, z-->x
        us.setCoordinates(new Vec3d(1+head_pos_z,head_pos_y,-head_pos_x));
        //us.setOrientation(0, 0, 0); // send also the orientation

        this.sendUserInfo(head_pos_x, head_pos_y, head_pos_z, head_rx, head_ry, head_rz, true);

    }

    public void sendUserInfo(double posx, double posy, double posz, double rotx, double roty, double rotz, boolean userActive){


        if (this.agent != null){
            this.agent.userActive = true;
            this.agent.head_pos_x = posx;
            this.agent.head_pos_y = posy;
            this.agent.head_pos_z = posz;

            this.agent.head_rx = rotx;
            this.agent.head_ry = roty;
            this.agent.head_rz = rotz;
        }else{

            this.group.userActive = true;
            this.group.head_pos_x = posx;
            this.group.head_pos_y = posy;
            this.group.head_pos_z = posz;

            this.group.head_rx = rotx;
            this.group.head_ry = roty;
            this.group.head_rz = rotz;
        }
    }

    public void setAgent(AgentGazeUser agu){
        this.agent = agu;
        this.agent.setUser(this.getUser());
    }

    public void setGroup(ConversationalGroup group){
        this.group = group;
        group.setUser(this.getUser());
    }

    public AgentGazeUser getAgent() {
        return agent;
    }

    /**
     * @return the voiceEnergy
     */
    public double getVoiceEnergy() {
        return voiceEnergy;
    }

    /**
     * @return the threshouldIntensity
     */
    public double getThreshouldIntensity() {
        return threshouldIntensity;
    }

    /**
     * @param threshouldIntensity the threshouldIntensity to set
     */
    public void setThreshouldIntensity(double threshouldIntensity) {
        this.threshouldIntensity = threshouldIntensity;
    }


    /**
     * @return the user
     */
    public ConversationParticipant getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(ConversationParticipant user) {
        this.user = user;
    }
}
