/**
 *
 * @author Paul Leroux
 */

package greta.core.animation.mpeg4;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.audio.Audio;
import greta.core.util.id.ID;
import greta.core.util.time.Timer;
import greta.core.util.Constants;

import java.util.List;
import java.lang.Integer;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class MPEG4AnimatableHold extends MPEG4Animatable{
    
    private boolean previousHoldFrame = this.getCharacterManager().getHoldFrame();
    private int newBaseFrameTime = 0;
    private Integer fapShift = null;
    private Integer bapShift = null;
    private boolean isShiftCalculated = false;

    public MPEG4AnimatableHold(CharacterManager cm) {
        super(cm,true);
    }
    
    public MPEG4AnimatableHold(CharacterManager cm, boolean connectToCharacterManager){
        super(cm,connectToCharacterManager);
    }
    
    private void waitForHoldRelease() {
        while (this.getCharacterManager().getHoldFrame()) {
            this.previousHoldFrame = true;
            try {
            Thread.sleep(10);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
        if (this.previousHoldFrame && !this.getCharacterManager().getHoldFrame()){
            this.newBaseFrameTime = (int) (Timer.getTime() * Constants.FRAME_PER_SECOND);
            System.out.println("[Greta MPEG4AnimatableHold] Stopped Holding, new base for time :" + this.newBaseFrameTime);
            this.fapShift = null;
            this.bapShift = null;
            
        this.previousHoldFrame = false;
        }
    }
    
    @Override
    public void performFAPFrames(List<FAPFrame> newFapFrames, ID requestId) {
        System.out.println("[Greta MPEG4AnimatableHold] just entered performFAPFrames.");
        waitForHoldRelease();
        if (fapShift == null) {
            fapShift = this.newBaseFrameTime - newFapFrames.get(0).getFrameNumber();
            System.out.println("[Greta MPEG4AnimatableHold] Calculated FAP shift: " + fapShift);
        }
        for (FAPFrame fapframe : newFapFrames) {
            fapframe.setFrameNumber(fapframe.getFrameNumber() + fapShift);
        }
        super.performFAPFrames(newFapFrames,requestId);
    }

    @Override
    public void performFAPFrame(FAPFrame fapFrame, ID requestId) {
        System.out.println("[Greta MPEG4AnimatableHold] just entered performFAPFrame.");
        waitForHoldRelease();
        if (fapShift == null) {
            fapShift = this.newBaseFrameTime - fapFrame.getFrameNumber();
            System.out.println("[Greta MPEG4AnimatableHold] Calculated FAP shift: " + fapShift);
        }
        fapFrame.setFrameNumber(fapFrame.getFrameNumber() + fapShift);
        super.performFAPFrame(fapFrame, requestId);
    }

    @Override
    public void performBAPFrames(List<BAPFrame> newBapFrames, ID requestId) {
        System.out.println("[Greta MPEG4AnimatableHold] just entered performBAPFrames.");
        waitForHoldRelease();
        if (bapShift == null) {
            bapShift = this.newBaseFrameTime - newBapFrames.get(0).getFrameNumber();
            System.out.println("[Greta MPEG4AnimatableHold] Calculated BAP shift: " + bapShift);
        }
        for (BAPFrame bapframe : newBapFrames) {
            bapframe.setFrameNumber(bapframe.getFrameNumber() + bapShift);
        }
        super.performBAPFrames(newBapFrames, requestId);
    }

    @Override
    public void performAudios(List<Audio> list, ID requestId, Mode mode) {
        System.out.println("[Greta MPEG4AnimatableHold] just entered performAudios.");
        waitForHoldRelease();
        if (!list.isEmpty()) {
            double newbaseTimeSeconds = (double) this.newBaseFrameTime / Constants.FRAME_PER_SECOND;
            list.get(0).setTime(newbaseTimeSeconds);
        }
        super.performAudios(list, requestId, mode);
    }
}