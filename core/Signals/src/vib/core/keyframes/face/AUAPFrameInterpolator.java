/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes.face;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import vib.core.repositories.AUAP;
import vib.core.repositories.AUAPFrame;
import vib.core.util.Constants;
import vib.core.util.math.Functions;
import vib.core.util.time.Timer;

/**
 *
 * @author Andre-Marie Pez
 */
public class AUAPFrameInterpolator {
    private LinkedList<AUAPFrame> keysOfAnim;

    public AUAPFrameInterpolator() {
        keysOfAnim = new LinkedList<AUAPFrame>();
    }

    public boolean isEmpty(){
        return keysOfAnim.isEmpty();
    }

    public boolean isEmptyAt(int timeFrame) {
        return keysOfAnim.isEmpty() || keysOfAnim.getLast().getFrameNumber()<timeFrame;
    }

    public synchronized void cleanOldFrames(){
        double currentTimeFrame = Timer.getTime()*Constants.FRAME_PER_SECOND;
        while(keysOfAnim.size()>1 && keysOfAnim.get(1).getFrameNumber()<currentTimeFrame){
            keysOfAnim.removeFirst();
        }
    }

    public synchronized List<AUAPFrame> getAUAPFrameList(){
        return new ArrayList<AUAPFrame>(keysOfAnim);
    }

    public void blendSegment(AUAPFrame... auSegment){
        blendSegment(Arrays.asList(auSegment));
    }

    public void blendSegment(List<AUAPFrame> auSegment){
        if(auSegment.isEmpty()){
            return;
        }
        Collections.sort(auSegment);

        ArrayList<AUAPFrame> toadd = new ArrayList<AUAPFrame>(auSegment.size());
        for(AUAPFrame frame : auSegment){
            AUAPFrame interpolated = getAUAPFrameAt(frame.getFrameNumber(), false, true);
            blendMax(interpolated, frame);
            toadd.add(interpolated);
        }

        //TODO update existing frames
        synchronized(this){
            ListIterator<AUAPFrame> keyAnimIter = keysOfAnim.listIterator();
            ListIterator<AUAPFrame> newAnimIter = auSegment.listIterator();
            AUAPFrame previous = null;
            AUAPFrame next = newAnimIter.next();
            while(keyAnimIter.hasNext()){
                AUAPFrame currentFrame = keyAnimIter.next();
                while(next.getFrameNumber()<currentFrame.getFrameNumber() && newAnimIter.hasNext()){
                    previous = next;
                    next = newAnimIter.next();
                }
                if(previous !=null && previous.compareTo(currentFrame)<=0 && next.compareTo(currentFrame)>=0){
                    blendMax(currentFrame, interpolate(previous, next, currentFrame.getFrameNumber(), false));
                }
            }
        }

        insertAllFrame(toadd);
//        synchronized(this){
//             System.out.println("***************************");
//             for(AUAPFrame frame: auSegment){
//                System.out.println(frame.getFrameNumber()+": "+
//                        (frame.getAUAPleftMask(1)? frame.getAUAPleftValue(1): "X")+" "+
//                        (frame.getAUAPleftMask(61)? frame.getAUAPleftValue(61): "X")+" "+
//                        (frame.getAUAPleftMask(62)? frame.getAUAPleftValue(62): "X")+" "+
//                        (frame.getAUAPleftMask(63)? frame.getAUAPleftValue(63): "X")+" "+
//                        (frame.getAUAPleftMask(64)? frame.getAUAPleftValue(64): "X"));
//            }
//             System.out.println("****");
//            for(AUAPFrame frame: keysOfAnim){
//                System.out.println(frame.getFrameNumber()+": "+
//                        (frame.getAUAPleftMask(1)? frame.getAUAPleftValue(1): "X")+" "+
//                        (frame.getAUAPleftMask(61)? frame.getAUAPleftValue(61): "X")+" "+
//                        (frame.getAUAPleftMask(62)? frame.getAUAPleftValue(62): "X")+" "+
//                        (frame.getAUAPleftMask(63)? frame.getAUAPleftValue(63): "X")+" "+
//                        (frame.getAUAPleftMask(64)? frame.getAUAPleftValue(64): "X"));
//            }
//             System.out.println("***************************");
//        }

    }

    public synchronized void cleanEmptyKeys(){
//        int sizebefore = keysOfAnim.size();
        ListIterator<AUAPFrame> keys = keysOfAnim.listIterator();
        while(keys.hasNext()){
            AUAPFrame frame = keys.next();
            boolean isEmpty = true;
            for(AUAP au : frame.APVector){
                if(au.getMask()){
                    isEmpty = false;
                    break;
                }
            }
            if(isEmpty){
                keys.remove();
            }
        }
//        System.out.println("size before:"+sizebefore+" after:"+keysOfAnim.size());

    }

    private void insertAllFrame(List<AUAPFrame> frames){
        for(AUAPFrame frame : frames){
            insertFrame(frame);
        }
    }

    private synchronized void insertFrame(AUAPFrame frame){
        ListIterator<AUAPFrame> iter = keysOfAnim.listIterator(keysOfAnim.size());
        AUAPFrame inlist;

        while(iter.hasPrevious()){
            inlist=iter.previous();

            if(frame.getFrameNumber()>inlist.getFrameNumber()) {
                iter.next();
                iter.add(frame);
                return;
            }

            if(frame.getFrameNumber() == inlist.getFrameNumber()){
                blendMax(inlist, frame);
                return;
            }
        }
        keysOfAnim.addFirst(frame);
    }

    public AUAPFrame getAUAPFrameAt(int timeIndex){
        return getAUAPFrameAt(timeIndex, true, false);
    }

    private synchronized AUAPFrame getAUAPFrameAt(int timeIndex, boolean doMaskOptimisation, boolean emptyWhenEnded){
        if(keysOfAnim.isEmpty()){
            return new AUAPFrame(timeIndex);
        }
        AUAPFrame before = keysOfAnim.getFirst();
        if(before.getFrameNumber() > timeIndex){
            //get a frame before the start of the list
            return new AUAPFrame(timeIndex);
        }
        for(AUAPFrame frame : keysOfAnim){
            if(frame.getFrameNumber() == timeIndex){
                return new AUAPFrame(frame);
            }
            if(frame.getFrameNumber() < timeIndex){
                before = frame;
            } else {
                // frame.getFrameNumber() is more than timeIndex
                return interpolate(before, frame, timeIndex, doMaskOptimisation);
            }
        }
        //we reach the end of the list so we are after
        if(doMaskOptimisation || emptyWhenEnded){
            return new AUAPFrame(timeIndex);
        }
        AUAPFrame result = new AUAPFrame(before);
        result.setFrameNumber(timeIndex);
        return result;
    }

    private synchronized AUAPFrame interpolate(AUAPFrame first, AUAPFrame second, int time, boolean doMaskOptimisation){
        AUAPFrame result = new AUAPFrame(first);
        for(int i = 0; i < result.size(); ++i){
            if(second.getMask(i)){
                result.applyValue(i, (int) val(i, time, first, second));
            } else {
                if(doMaskOptimisation){
                    first.setMask(i, false);
                }
            }
        }
        result.setFrameNumber(time);
        return result;
    }

    private synchronized void blendMax(AUAPFrame inlist, AUAPFrame frame) {
        for(int i = 0; i < frame.size(); ++i){
            if(frame.getMask(i) && frame.getValue(i)>=inlist.getValue(i)){
                inlist.applyValue(i, frame.getValue(i));
            }
        }
    }

    private AUAPFrame getIntersection(int auNum, AUAPFrame start1, AUAPFrame end1, AUAPFrame start2, AUAPFrame end2){
        if(start1.getMask(auNum) && end1.getMask(auNum) && start2.getMask(auNum) && end2.getMask(auNum)){
            //they use the same au
            int startMax = Math.max(start1.getFrameNumber(), start2.getFrameNumber());
            int endMin = Math.min(end1.getFrameNumber(), end2.getFrameNumber());
            if(startMax<endMin){
                //share the same time
                //try compute the linear parameter value(t) = a*t + b
                double b1 = val(auNum, 0, start1, end1);
                double b2 = val(auNum, 0, start2, end2);
                double a1 = val(auNum, 1, start1, end1) - b1;
                double a2 = val(auNum, 1, start2, end2) - b2;
                if(a1!=a2){
                    //they don't have the same gradient. so there is one cross point at :
                    // t = (b2-b1) / (a1-a2)
                    double crossTime = (b2-b1) / (a1-a2);
                    if(startMax<=crossTime && crossTime<=endMin){
                        //they cross during the common time interval
                        AUAPFrame result = interpolate(start1, end1, (int)crossTime, false);
                        blendMax(result, interpolate(start2, end2, (int)crossTime, false));
                        return result;
                    }
                } else {
                    //they share the same gradient
                    //if b1 = b2 they are the same and any point is correct
                    //else no point is correct
                    if(b1==b2){
                        if(start1.getFrameNumber() < start2.getFrameNumber()){
                            AUAPFrame result = new AUAPFrame(start2);
                            blendMax(result, interpolate(start1, end1, start2.getFrameNumber(), false));
                            return result;
                        } else {
                            AUAPFrame result = new AUAPFrame(start1);
                            blendMax(result, interpolate(start2, end2, start1.getFrameNumber(), false));
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    private double val(int auIndex, double t, AUAPFrame first, AUAPFrame second){
        return Functions.changeInterval(t,
                first.getFrameNumber(),
                second.getFrameNumber(),
                first.getValue(auIndex),
                second.getValue(auIndex));
    }

    public synchronized void clear() {
        keysOfAnim.clear();
    }

}
