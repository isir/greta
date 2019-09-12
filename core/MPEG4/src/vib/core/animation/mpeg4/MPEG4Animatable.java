/*
 * This file is part of Greta.
 * 
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
 */

package vib.core.animation.mpeg4;


import java.util.List;
import vib.core.animation.mpeg4.bap.*;
import vib.core.animation.mpeg4.fap.*;
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
public class MPEG4Animatable extends Animatable implements CancelableFAPFramePerformer, CancelableBAPFramesPerformer,
        AudioPerformer, FAPFrameEmitter, BAPFramesEmitter, AudioEmitter, CharacterDependent {

    private static final String ASPECT = "ASPECT";
    private APFrameList<BAPFrame> bapFrames;
    private APFrameList<FAPFrame> fapFrames;
    private AudioTreeNode headNode;
    private BAPFramesEmitterImpl bapEmitter = new BAPFramesEmitterImpl();
    private FAPFrameEmitterImpl fapEmitter = new FAPFrameEmitterImpl();
    private AudioEmitterImpl audioEmitter = new AudioEmitterImpl();
    
    private CharacterManager characterManager;

    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        if (characterManager == null) {
            characterManager = CharacterManager.getStaticInstance();
        }
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        if (this.characterManager != null) {
            this.characterManager.remove(this);
        }
        this.characterManager = characterManager;
        characterManager.add(this);
    }    
    
    public static CharacterManager getCharacterManagerStatic(){
        return CharacterManager.getStaticInstance();
    }
    
    /*public MPEG4Animatable() {
        this(null,false);        
    }*/

    public MPEG4Animatable(CharacterManager cm) {
        this(cm,true);        
    }

    public MPEG4Animatable(CharacterManager cm, boolean connectToCharacterManager) {
        if(connectToCharacterManager)
            setCharacterManager(cm);
        BAPFrame firstBapFrame = new BAPFrame();
        firstBapFrame.setFrameNumber(0);
        bapFrames = new APFrameList<>(firstBapFrame);

        FAPFrame firstFapFrame = new FAPFrame();
        firstFapFrame.setFrameNumber(0);
        fapFrames = new APFrameList<>(firstFapFrame);

        getAttachedLeaf().setSize(0.50f, 1.75f, 0.3f);
        if (connectToCharacterManager) {
            setAspect(cm.getValueString(ASPECT));
            cm.setCurrentCharacterId(this.getIdentifier());
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
    public void performFAPFrames(List<FAPFrame> newFapFrames, ID requestId) {
        fapEmitter.sendFAPFrames(requestId, newFapFrames); //pass throw
        fapFrames.addFrames(newFapFrames, requestId);
        fapFrames.updateFrames();
    }

    @Override
    public void performFAPFrame(FAPFrame fapFrame, ID requestId) {
        fapEmitter.sendFAPFrame(requestId, fapFrame); //pass throw
        fapFrames.addFrame(fapFrame, requestId);
    }

    @Override
    public void cancelFAPFramesById(ID requestId) {
        fapEmitter.cancelFramesWithIDInLinkedPerformers(requestId);
        fapFrames.deleteFramesWithId(requestId);
    }

    @Override
    public void performBAPFrames(List<BAPFrame> newBapFrames, ID requestId) {
        bapEmitter.sendBAPFrames(requestId, newBapFrames); //pass throw
        bapFrames.addFrames(newBapFrames, requestId);
        bapFrames.updateFrames();
    }

    @Override
    public void cancelBAPFramesById(ID requestId) {
        bapEmitter.cancelFramesWithIDInLinkedPerformers(requestId);
        bapFrames.deleteFramesWithId(requestId);
    }

    public BAPFrame getCurrentBAPFrame() {
        return bapFrames.getCurrentFrame();
    }

    public FAPFrame getCurrentFAPFrame() {
        return fapFrames.getCurrentFrame();
    }

    /**
     * Deletes the given {@code FAPFrame} from the kept list.
     * @param frameToDelete frame to be deleted
     */
    public void deleteFAPFrame (FAPFrame frameToDelete) {
        fapFrames.deleteFrame(frameToDelete);
    }

    /**
     * Deletes the given {@code BAPFrame} from the kept list.
     * @param frameToDelete frame to be deleted
     */
    public void deleteBAPFrame (BAPFrame frameToDelete) {
        bapFrames.deleteFrame(frameToDelete);
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
