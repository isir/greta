/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.animationparameters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import vib.core.util.Constants;
import vib.core.util.time.Timer;

/**
 *
 * @author Andre-Marie Pez
 * @author Ken Prepin
 */
public class APFrameList <APF extends AnimationParametersFrame>{
    private LinkedList<APF> apFrameList;
    public APFrameList(APF firstAPFrame){
        apFrameList = new LinkedList<APF>();
        apFrameList.add(firstAPFrame);
        firstAPFrame.setFrameNumber(0);
    }

    public synchronized void addFrame(APF apFrame){
        ListIterator<APF> iter = apFrameList.listIterator(apFrameList.size());
        APF inlist=null;

        while(iter.hasPrevious()){
            inlist=iter.previous();

            if(apFrame.getFrameNumber()>inlist.getFrameNumber()) {
                iter.next();
                iter.add(apFrame);
                break;
            }

            if(apFrame.getFrameNumber() == inlist.getFrameNumber()){
                blend(inlist, apFrame);
                break;
            }
        }
    }

    public void addFrames(List<APF> apframeList){
        for(APF apf : apframeList)
        {
            addFrame(apf);
        }
    }

    public synchronized List<APF> getListCopy(){
        return new ArrayList<APF>(apFrameList);
    }
    
    private void blend(APF firstAPFrame, APF newAPFrame) {
        for (int i = 0; i < newAPFrame.size(); ++i) {
            AnimationParameter ap = (AnimationParameter) newAPFrame.getAnimationParametersList().get(i);
            if (ap.getMask()) {
                firstAPFrame.setAnimationParameter(i, ap.getValue());
            }
        }
    }

    public synchronized void updateFrames(){
        long currentFrame = (long) (Timer.getTime() * Constants.FRAME_PER_SECOND);

        APF firstAPFrame = apFrameList.peek();

        ListIterator<APF> iter = apFrameList.listIterator();
        while(iter.hasNext()){
            APF frame = iter.next();
            if(frame.getFrameNumber()>currentFrame) {
                break;
            }

            if(frame != firstAPFrame){
                blend(firstAPFrame, frame);
                iter.remove();
            }
        }
        firstAPFrame.setFrameNumber((int)currentFrame);
    }
    

    public synchronized void updateFramesToTime(long firstFrame){
        APF firstAPFrame = apFrameList.peek();

        ListIterator<APF> iter = apFrameList.listIterator();
        while(iter.hasNext()){
            APF frame = iter.next();
            if(frame.getFrameNumber()>firstFrame) {
                break;
            }

            if(frame != firstAPFrame){
                blend(firstAPFrame, frame);
                iter.remove();
            }
        }
        firstAPFrame.setFrameNumber((int)firstFrame);
    }
    public synchronized APF getFrameAtTime(long targetFrame){
        updateFramesToTime(targetFrame);
        return apFrameList.peek();
    }

    public synchronized APF getCurrentFrame(){
        updateFrames();
        return apFrameList.peek();
    }
}
