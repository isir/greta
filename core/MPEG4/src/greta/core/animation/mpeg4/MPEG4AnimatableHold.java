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
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class MPEG4AnimatableHold extends MPEG4Animatable{
    
    private boolean previousHoldFrame = this.getCharacterManager().getHoldFrame();
    private int newBaseFrameTime = 0;
    private int fapFrameCounter = 0;
    private int bapFrameCounter = 0;
    private int audioFrameCounter = 0;

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
            this.fapFrameCounter = 0;
            this.bapFrameCounter = 0;
            this.audioFrameCounter = 0;
            
        this.previousHoldFrame = false;
        }
    }
    
    @Override
    public void performFAPFrames(List<FAPFrame> newFapFrames, ID requestId) {
        System.out.println("[Greta MPEG4AnimatableHold] just entered performFAPFrames.");
        waitForHoldRelease();
        for (FAPFrame fapframe : newFapFrames) {
            int currentFapFrameNumber = this.newBaseFrameTime + this.fapFrameCounter;
            fapframe.setFrameNumber(currentFapFrameNumber);
            this.fapFrameCounter++;
        }
        System.out.println("[Greta MPEG4AnimatableHold] fap counter." + this.fapFrameCounter);
        System.out.println("[Greta MPEG4AnimatableHold]Received a FAP batch of size" + newFapFrames.size());
        super.performFAPFrames(newFapFrames,requestId);
    }

    @Override
    public void performFAPFrame(FAPFrame fapFrame, ID requestId) {
        System.out.println("[Greta MPEG4AnimatableHold] just entered performFAPFrame.");
        waitForHoldRelease();
        int currentFapFrameNumber = this.newBaseFrameTime + this.fapFrameCounter;
        fapFrame.setFrameNumber(currentFapFrameNumber);
        System.out.println("[Greta MPEG4AnimatableHold] fap counter." + this.fapFrameCounter);
        super.performFAPFrame(fapFrame, requestId);
    }

    @Override
    public void performBAPFrames(List<BAPFrame> newBapFrames, ID requestId) {
        System.out.println("[Greta MPEG4AnimatableHold] just entered performBAPFrames.");
        waitForHoldRelease();
        for (BAPFrame bapframe : newBapFrames) {
            int currentBapFrameNumber = this.newBaseFrameTime + this.bapFrameCounter;
            bapframe.setFrameNumber(currentBapFrameNumber);
            this.bapFrameCounter++;
        }
        System.out.println("[Greta MPEG4AnimatableHold] bap counter." + this.bapFrameCounter);
        System.out.println("[Greta MPEG4AnimatableHold] Received a BAP batch of size" + newBapFrames.size());
        super.performBAPFrames(newBapFrames, requestId);
    }

    @Override
    public void performAudios(List<Audio> list, ID requestId, Mode mode) {
        System.out.println("[Greta MPEG4AnimatableHold] just entered performAudios.");
        waitForHoldRelease();
        System.out.println("[Greta MPEG4AnimatableHold] audio counter." + this.audioFrameCounter);
        if (!list.isEmpty()) {
            double newbaseTimeSeconds = (double) this.newBaseFrameTime / Constants.FRAME_PER_SECOND;
                
            list.get(0).setTime(newbaseTimeSeconds);
        }
        super.performAudios(list, requestId, mode);
    }
}