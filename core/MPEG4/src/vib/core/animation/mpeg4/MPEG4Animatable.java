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
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import vib.core.animation.mpeg4.bap.BAPFrame;
import vib.core.animation.mpeg4.bap.BAPFramesEmitter;
import vib.core.animation.mpeg4.bap.BAPFramesEmitterImpl;
import vib.core.animation.mpeg4.bap.BAPFramesPerformer;
import vib.core.animation.mpeg4.bap.BAPType;
import vib.core.animation.mpeg4.fap.FAPFrame;
import vib.core.animation.mpeg4.fap.FAPFrameEmitter;
import vib.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import vib.core.animation.mpeg4.fap.FAPFramePerformer;
import vib.core.animation.mpeg4.fap.FAPType;
import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.IniParameter;
import vib.core.util.Mode;
import vib.core.util.animationparameters.APFrameList;
import vib.core.util.audio.Audio;
import vib.core.util.audio.AudioEmitter;
import vib.core.util.audio.AudioEmitterImpl;
import vib.core.util.audio.AudioPerformer;
import vib.core.util.audio.AudioTreeNode;
import vib.core.util.environment.Animatable;
import vib.core.util.id.ID;
import vib.core.util.math.Vec3d;

/**
 *
 * @author Andre-Marie Pez
 */
public class MPEG4Animatable extends Animatable implements FAPFramePerformer, BAPFramesPerformer, AudioPerformer, FAPFrameEmitter, 
        BAPFramesEmitter, AudioEmitter, CharacterDependent {

    private static final String ASPECT = "ASPECT";
    private APFrameList<BAPFrame> bapFrames;
    private APFrameList<FAPFrame> fapFrames;
    private AudioTreeNode headNode;
    private BAPFramesEmitterImpl bapEmitter = new BAPFramesEmitterImpl();
    private FAPFrameEmitterImpl fapEmitter = new FAPFrameEmitterImpl();
    private AudioEmitterImpl audioEmitter = new AudioEmitterImpl();
    
    public HashMap<String,List<IniParameter>> curPos = new HashMap<String,List<IniParameter>>();  
    public List<IniParameter> ListcurPos = Arrays.asList(new IniParameter[16]);
    
    private CharacterManager characterManager;

    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        if(characterManager==null)
            characterManager = CharacterManager.getStaticInstance();
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        if(this.characterManager!=null)
            this.characterManager.remove(this);
        this.characterManager = characterManager;
        characterManager.add(this);
    }    
    
    public static CharacterManager getCharacterManagerStatic(){
        return CharacterManager.getStaticInstance();
    }
    
    public MPEG4Animatable() {
        this(null,false);        
    }

    public MPEG4Animatable(CharacterManager cm) {
        this(cm,true);        
    }

    public MPEG4Animatable(CharacterManager cm,boolean connectToCaracterManager) {
        if(connectToCaracterManager)
            setCharacterManager(cm);
        BAPFrame firstBapFrame = new BAPFrame();
        firstBapFrame.setFrameNumber(0);
        bapFrames = new APFrameList<BAPFrame>(firstBapFrame);

        FAPFrame firstFapFrame = new FAPFrame();
        firstFapFrame.setFrameNumber(0);
        fapFrames = new APFrameList<FAPFrame>(firstFapFrame);

        getAttachedLeaf().setSize(0.50f, 1.75f, 0.3f);
        if (connectToCaracterManager) {
            setAspect(getCharacterManager().getValueString(ASPECT));
            getCharacterManager().currentCharacterId = this.getIdentifier();
            getCharacterManager().add(this);
        } else {
            getAttachedLeaf().setReference("agent.greta");
        }

        headNode = new AudioTreeNode(identifier + "_AudioTreeNode");
        this.addChildOnScaleNode(headNode);

        this.setGuest(true);
    }

    /**
     * Changes the identifier of this {@code Node}<br/> never use this until you
     * know what you are doing.
     *
     * @param id the new identifier
     */
    @Override
    public void setIdentifier(String id) {
        super.setIdentifier(id);
        headNode.setIdentifier(identifier + "_AudioTreeNode");
    }

    public void setAspect(String aspectName) {
        if (!aspectName.startsWith("agent.")) {
            aspectName = "agent." + aspectName;
        }
        getAttachedLeaf().setReference(aspectName);
    }

    public String getAspect(){
        return getAttachedLeaf().getReference();
    }
    @Override
    public void performFAPFrames(List<FAPFrame> fap_animation, ID requestId) {
        fapEmitter.sendFAPFrames(requestId, fap_animation);//pass throw
        for (FAPFrame frame : fap_animation) {
            fapFrames.addFrame(frame);
        }
        fapFrames.updateFrames();
    }

    @Override
    public void performFAPFrame(FAPFrame fap_anim, ID requestId) {
        fapEmitter.sendFAPFrame(requestId, fap_anim);//pass throw
        fapFrames.addFrame(fap_anim);
    }

    @Override
    public void performBAPFrames(List<BAPFrame> bapframes, ID requestId) {
        bapEmitter.sendBAPFrames(requestId, bapframes);//pass throw
        for (BAPFrame frame : bapframes) {
            bapFrames.addFrame(frame);
        }
        bapFrames.updateFrames();
    }

    public BAPFrame getCurrentBAPFrame() {
        BAPFrame current = bapFrames.getCurrentFrame();
        return current;
    }

    public FAPFrame getCurrentFAPFrame() {
        
        ListcurPos.set(0, new IniParameter("root_pitch", String.valueOf(this.getRotationNode().getOrientation().x()))); // here I take the orientation of all body. Is the same values found in MPEG4 gui for the orientation of the agent
        ListcurPos.set(1, new IniParameter("root_yaw", String.valueOf(this.getRotationNode().getOrientation().y())));
        ListcurPos.set(2, new IniParameter("root_roll", String.valueOf(this.getRotationNode().getOrientation().z())));
        ListcurPos.set(3, new IniParameter("root_w", String.valueOf(this.getRotationNode().getOrientation().w())));
        ListcurPos.set(4, new IniParameter("pitch_l_eyeball", String.valueOf(fapFrames.getCurrentFrame().getValue(FAPType.pitch_l_eyeball))));
        ListcurPos.set(5, new IniParameter("yaw_l_eyeball", String.valueOf(fapFrames.getCurrentFrame().getValue(FAPType.yaw_l_eyeball))));
        ListcurPos.set(6, new IniParameter("thrust_l_eyeball", String.valueOf(fapFrames.getCurrentFrame().getValue(FAPType.thrust_l_eyeball))));
        ListcurPos.set(7, new IniParameter("pitch_r_eyeball", String.valueOf(fapFrames.getCurrentFrame().getValue(FAPType.pitch_r_eyeball))));
        ListcurPos.set(8, new IniParameter("yaw_r_eyeball", String.valueOf(fapFrames.getCurrentFrame().getValue(FAPType.yaw_r_eyeball))));
        ListcurPos.set(9, new IniParameter("thrust_r_eyeball", String.valueOf(fapFrames.getCurrentFrame().getValue(FAPType.thrust_r_eyeball))));
        ListcurPos.set(10, new IniParameter("head_x", String.valueOf(headNode.getGlobalCoordinates().x())));
        ListcurPos.set(11, new IniParameter("head_y", String.valueOf(headNode.getGlobalCoordinates().y())));
        ListcurPos.set(12, new IniParameter("head_z", String.valueOf(headNode.getGlobalCoordinates().z())));
        // head angles give by the additional rotation of each cervical vertebrae
        ListcurPos.set(13, new IniParameter("head_pitch", String.valueOf(bapFrames.getCurrentFrame().getRadianValue(BAPType.vc1_tilt) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc2_tilt) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc3_tilt) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc4_tilt) + 
                                                                                  bapFrames.getCurrentFrame().getRadianValue(BAPType.vc5_tilt) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc6_tilt) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc7_tilt))));
        ListcurPos.set(14, new IniParameter("head_yaw", String.valueOf(bapFrames.getCurrentFrame().getRadianValue(BAPType.vc1_torsion) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc2_torsion) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc3_torsion) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc4_torsion) + 
                                                                                  bapFrames.getCurrentFrame().getRadianValue(BAPType.vc5_torsion) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc6_torsion) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc7_torsion))));
        ListcurPos.set(15, new IniParameter("head_roll", String.valueOf(bapFrames.getCurrentFrame().getRadianValue(BAPType.vc1_roll) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc2_roll) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc3_roll) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc4_roll) + 
                                                                                  bapFrames.getCurrentFrame().getRadianValue(BAPType.vc5_roll) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc6_roll) + bapFrames.getCurrentFrame().getRadianValue(BAPType.vc7_roll))));
        
        // save the live data in CharacterManager variable in order to be able to get the wanted data when needed
        //CharacterManager.getStaticInstance().currentPosition.put(getCharacterManager().getCurrentCharacterName() , ListcurPos); 

        return fapFrames.getCurrentFrame();
    }

    @Override
    public void onCharacterChanged() {
        setAspect(getCharacterManager().getValueString(ASPECT));
    }

    @Override
    public void performAudios(List<Audio> list, ID requestId, Mode mode) {
        headNode.performAudios(list, requestId, mode);
    }

    public AudioTreeNode getHeadNode() {
        return headNode;
    }

    @Override
    public void addFAPFramePerformer(FAPFramePerformer performer) {
        fapEmitter.addFAPFramePerformer(performer);
    }

    @Override
    public void removeFAPFramePerformer(FAPFramePerformer performer) {
        fapEmitter.removeFAPFramePerformer(performer);
    }

    @Override
    public void addBAPFramesPerformer(BAPFramesPerformer performer) {
        bapEmitter.addBAPFramesPerformer(performer);
    }

    @Override
    public void removeBAPFramesPerformer(BAPFramesPerformer performer) {
        bapEmitter.removeBAPFramesPerformer(performer);
    }

    @Override
    public void addAudioPerformer(AudioPerformer ap) {
        audioEmitter.addAudioPerformer(ap);
    }

    @Override
    public void removeAudioPerformer(AudioPerformer ap) {
        audioEmitter.removeAudioPerformer(ap);
    }

    @Override
    protected String getXMLNodeName() {
        return "mpeg4animatable";
    }
}
