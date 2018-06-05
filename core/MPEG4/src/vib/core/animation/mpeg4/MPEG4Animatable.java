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

import java.util.List;
import vib.core.animation.mpeg4.bap.BAPFrame;
import vib.core.animation.mpeg4.bap.BAPFramesEmitter;
import vib.core.animation.mpeg4.bap.BAPFramesEmitterImpl;
import vib.core.animation.mpeg4.bap.BAPFramesPerformer;
import vib.core.animation.mpeg4.fap.FAPFrame;
import vib.core.animation.mpeg4.fap.FAPFrameEmitter;
import vib.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import vib.core.animation.mpeg4.fap.FAPFramePerformer;
import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.Mode;
import vib.core.util.animationparameters.APFrameList;
import vib.core.util.audio.Audio;
import vib.core.util.audio.AudioEmitter;
import vib.core.util.audio.AudioEmitterImpl;
import vib.core.util.audio.AudioPerformer;
import vib.core.util.audio.AudioTreeNode;
import vib.core.util.environment.Animatable;
import vib.core.util.id.ID;

/**
 *
 * @author Andre-Marie Pez
 */
public class MPEG4Animatable extends Animatable implements FAPFramePerformer, BAPFramesPerformer, AudioPerformer, FAPFrameEmitter, BAPFramesEmitter, AudioEmitter, CharacterDependent {

    private static final String ASPECT = "ASPECT";
    private APFrameList<BAPFrame> bapFrames;
    private APFrameList<FAPFrame> fapFrames;
    private AudioTreeNode headNode;
    private BAPFramesEmitterImpl bapEmitter = new BAPFramesEmitterImpl();
    private FAPFrameEmitterImpl fapEmitter = new FAPFrameEmitterImpl();
    private AudioEmitterImpl audioEmitter = new AudioEmitterImpl();

    public MPEG4Animatable() {
        this(true);
    }

    public MPEG4Animatable(boolean connectToCaracterManager) {
        BAPFrame firstBapFrame = new BAPFrame();
        firstBapFrame.setFrameNumber(0);
        bapFrames = new APFrameList<BAPFrame>(firstBapFrame);

        FAPFrame firstFapFrame = new FAPFrame();
        firstFapFrame.setFrameNumber(0);
        fapFrames = new APFrameList<FAPFrame>(firstFapFrame);

        getAttachedLeaf().setSize(0.50f, 1.75f, 0.3f);
        if (connectToCaracterManager) {
            setAspect(CharacterManager.getValueString(ASPECT));
            CharacterManager.currentCharacterId = this.getIdentifier();
            CharacterManager.add(this);
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
        return fapFrames.getCurrentFrame();
    }

    @Override
    public void onCharacterChanged() {
        setAspect(CharacterManager.getValueString(ASPECT));
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
