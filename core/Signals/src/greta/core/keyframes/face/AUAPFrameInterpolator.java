/*
 * This file is part of Greta.
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
package greta.core.keyframes.face;

import greta.core.repositories.AUAP;
import greta.core.repositories.AUAPFrame;
import greta.core.util.Constants;
import greta.core.util.id.ID;
import greta.core.util.math.Functions;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Keeps a list of AUAPFrame, blending and interpolating them when needed.
 * @author Andre-Marie Pez
 * @author Nawhal Sayarh
 */
public class AUAPFrameInterpolator {
    /**
     * List of AUAPFrame saved in the interpolator.
     */
    private LinkedList<AUAPFrame> keysOfAnim;

    /**
     * Ids of frames corresponding to their frame number.
     * We consider that we can't have 2 frames at the same frame number as in insertFrame we blend in this case.
     */
    private Map<Integer, ID> idsForFrameNumbers;

    /**
     * Constructs an interpolator with an empty list of frames.
     */
    public AUAPFrameInterpolator() {
        keysOfAnim = new LinkedList<>();
        idsForFrameNumbers = new TreeMap<>();
    }

    /**
     * Indicates whether the list of AUAPFrames saved in the interpolator is empty.
     * @return true if the list is empty
     */
    public boolean isEmpty(){
        return keysOfAnim.isEmpty();
    }

    /**
     * Indicates whether no AUAPFrame can be given for the given time frame.
     * @param timeFrame The time for which you want a frame
     * @return true if no frame can be given for timeFrame
     */
    public boolean isEmptyAt(int timeFrame) {
        return keysOfAnim.isEmpty() || keysOfAnim.getLast().getFrameNumber()<timeFrame;
    }

    /**
     * Cleans the list of AUAPFrame of all frames happening before the current time.
     */
    public synchronized void cleanOldFrames(){
        double currentTimeFrame = Timer.getTime()*Constants.FRAME_PER_SECOND;
        while(keysOfAnim.size()>1 && keysOfAnim.get(1).getFrameNumber()<currentTimeFrame){
            idsForFrameNumbers.remove(keysOfAnim.get(0).getFrameNumber());
            keysOfAnim.removeFirst();
        }
    }

    /**
     * Returns a copy of the list of AUAPFrame saved in the interpolator.
     * @return the list of frames saved in the interpolator
     */
    public synchronized List<AUAPFrame> getAUAPFrameList(){
        return new ArrayList<>(keysOfAnim);
    }

    /**
     * Blends the given segment with the existing frames.
     * @param auSegment frames to blend in the interpolator
     */
    public void blendSegment(AUAPFrame... auSegment){
        blendSegment(null, auSegment);
    }

    /**
     * Blends the given segment with the existing frames, saving the ID linked to the segment.
     * @param requestId ID linked to the segment
     * @param auSegment frames to blend in the interpolator
     */
    public void blendSegment(ID requestId, AUAPFrame... auSegment){
        blendSegment(Arrays.asList(auSegment), requestId);
    }

    /**
     * Blends the given segment with the existing frames.
     * @param auSegment frames to blend in the interpolator
     */
    public void blendSegment(List<AUAPFrame> auSegment) {
        blendSegment(auSegment, null);
    }

    /**
     * Blends the given segment with the existing frames, saving the ID linked to the segment.
     * @param requestId ID linked to the segment
     * @param auSegment frames to blend in the interpolator
     */
    public void blendSegment(List<AUAPFrame> auSegment, ID requestId){
        if(auSegment.isEmpty()){
            return;
        }
        Collections.sort(auSegment);

        ArrayList<AUAPFrame> toadd = new ArrayList<>(auSegment.size());
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

        insertAllFrame(toadd, requestId);
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

    /**
     * Cleans the list of AUAPFrame of all the frames that are actually empty.
     */
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
                idsForFrameNumbers.remove(frame.getFrameNumber());
                keys.remove();
            }
        }
//        System.out.println("size before:"+sizebefore+" after:"+keysOfAnim.size());
    }

    /**
     * Inserts the given frames in the list of AUAPFrame in the interpolator.
     * @param frames frames to be inserted in the interpolator's list of frames
     * @param requestId ID linked to the given frames
     */
    private void insertAllFrame(List<AUAPFrame> frames, ID requestId){
        for(AUAPFrame frame : frames){
            insertFrame(frame, requestId);
        }
    }

    /**
     * Inserts the given frame in the list of AUAPFrame in the interpolator.<br/>
     * Keeps a crescent order in the list according to when the frames happen.
     * If a frame happens in the same time as the given frame, the two frames are blended.
     * @param frame frame to be inserted in the interpolator's list of frames
     * @param requestId ID linked to the given frames
     */
    private synchronized void insertFrame(AUAPFrame frame, ID requestId) {
        ListIterator<AUAPFrame> framesIterator = keysOfAnim.listIterator(keysOfAnim.size());
        AUAPFrame inlist;

        while (framesIterator.hasPrevious()) {
            inlist = framesIterator.previous();
            int frameNumber = frame.getFrameNumber();

            if (frameNumber > inlist.getFrameNumber()) {
                framesIterator.next();
                framesIterator.add(frame);
                idsForFrameNumbers.put(frameNumber, requestId);
                return;
            }

            if (frameNumber == inlist.getFrameNumber()) {
                blendMax(inlist, frame);
                return;
            }
        }
        keysOfAnim.addFirst(frame);
        idsForFrameNumbers.put(frame.getFrameNumber(), requestId);
    }

    /**
     * Returns the frame corresponding to the given timeIndex in the interpolator.
     * @param timeIndex time at which the frame should happen
     * @return the frame happening at the given timeIndex
     */
    public AUAPFrame getAUAPFrameAt(int timeIndex){
        return getAUAPFrameAt(timeIndex, true, false);
    }

    /**
     * Returns the frame corresponding to the given timeIndex in the interpolator.
     * @param timeIndex time at which the frame should happen
     * @param doMaskOptimisation indicates whether to do a mask optimisation in the interpolation
     * @param emptyWhenEnded indicates whether to return an empty frame if there is nothing corresponding to the given
     * timeIndex in the interpolator's list of frames
     * @return the frame happening at the given timeIndex
     */
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
            int frameNumber = frame.getFrameNumber();
            if(frameNumber == timeIndex){
                return new AUAPFrame(frame);
            }
            if(frameNumber < timeIndex){
                before = frame;
            } else {
                // frameNumber is more than timeIndex
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

    /**
     * Returns lists of frames corresponding to IDs of requests. There is one frame per time between timeStart and timeEnd.
     * @param timeStart start time of the sequence of frames
     * @param timeEnd end time of the sequence of frames
     * @return lists of frames corresponding to IDs of requests
     */
    public synchronized Map<ID, List<AUAPFrame>> getAUAPFramesWithIdAt (int timeStart, int timeEnd) {
        return getAUAPFramesWithIdAt(timeStart, timeEnd, true, false);
    }

    /**
     * Returns lists of frames corresponding to IDs of requests. There is one frame per time between timeStart and timeEnd.
     * @param timeStart start time of the sequence of frames
     * @param timeEnd end time of the sequence of frames
     * @param doMaskOptimisation indicates whether to do a mask optimisation in the interpolation
     * @param emptyWhenEnded indicates whether to return an empty frame if there is nothing corresponding to the given
     * timeIndex in the interpolator's list of frames
     * @return lists of frames corresponding to IDs of requests
     */
    public synchronized Map<ID, List<AUAPFrame>> getAUAPFramesWithIdAt (int timeStart, int timeEnd,
            boolean doMaskOptimisation, boolean emptyWhenEnded) {
        // Clean useless old frames
        cleanOldFrames();
        // Initialize result map
        Map<ID, List<AUAPFrame>> auapFramesWithId = new HashMap<>();

        // If no frames in the interpolator, return a map with a null ID and AUAPFrames for the time interval
        if (keysOfAnim.isEmpty()) {
            List<AUAPFrame> auapFrames = new ArrayList<>();
            for (int i = timeStart; i < timeEnd; ++i) {
                auapFrames.add(new AUAPFrame(i));
            }
            auapFramesWithId.put(null, auapFrames);
            return auapFramesWithId;
        }

        // Otherwise, check for each time what frame to return
        int frameIndex = 0;
        AUAPFrame frameBefore = keysOfAnim.getFirst();

        for (int i = timeStart; i < timeEnd; ++i) {
            AUAPFrame frameForTime = null;
            ID idForTime = null;
            // If the first frame of the interpolator is after the time, create a new frame for the time with a null id
            if (frameBefore.getFrameNumber() > i) {
                frameForTime = new AUAPFrame(i);
            } else {
                for (; frameIndex < keysOfAnim.size(); frameIndex++) {
                    AUAPFrame frame = keysOfAnim.get(frameIndex);
                    int frameNumber = frame.getFrameNumber();
                    if (frameNumber == i) {
                        // If there's a frame in the interpolator at this exact time, retrieve it with its id
                        frameForTime = new AUAPFrame(frame);
                        idForTime = idsForFrameNumbers.get(frameForTime.getFrameNumber());
                        break;
                    }
                    if (frameNumber < i) {
                        // Continue checking as long as the frame in the interpolator happens before our time
                        frameBefore = frame;
                    } else {
                        // There is no frame happening at our time, so we interpolate the 2 frames around that time
                        frameForTime = interpolate(frameBefore, frame, i, doMaskOptimisation);
                        // If both frames belong to the same movement/have the same ID, we retrieve that ID
                        ID idBefore = idsForFrameNumbers.get(frameBefore.getFrameNumber());
                        if (idBefore == idsForFrameNumbers.get(frameNumber)) {
                            idForTime = idBefore;
                        }
                        break;
                    }
                }
                // All frames happen before our time
                if (frameForTime == null && (doMaskOptimisation || emptyWhenEnded)) {
                    frameForTime = new AUAPFrame(i);
                    idForTime = null;
                }
            }
            // All frames happen before our time and we don't want to optimize / have an empty frame
            if (frameForTime == null) {
                frameForTime = new AUAPFrame(frameBefore);
                frameForTime.setFrameNumber(i);
                idForTime = null;
            }
            // We add the frames to the list corresponding to their ID
            if (!auapFramesWithId.containsKey(idForTime)) {
                auapFramesWithId.put(idForTime, new ArrayList<>());
            }
            auapFramesWithId.get(idForTime).add(frameForTime);
        }
        return auapFramesWithId;
    }

    private synchronized AUAPFrame interpolate(AUAPFrame first, AUAPFrame second, int time, boolean doMaskOptimisation){
        AUAPFrame result = new AUAPFrame(first);
        for(int i = 0; i < result.size(); ++i){
            if(second.getMask(i)){
                result.applyValue(i, (int) val(i, time, first, second));
            } else if(doMaskOptimisation){
                first.setMask(i, false);
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
                        int start1FrameNumber = start1.getFrameNumber();
                        int start2FrameNumber = start1.getFrameNumber();
                        if(start1FrameNumber < start2FrameNumber){
                            AUAPFrame result = new AUAPFrame(start2);
                            blendMax(result, interpolate(start1, end1, start2FrameNumber, false));
                            return result;
                        } else {
                            AUAPFrame result = new AUAPFrame(start1);
                            blendMax(result, interpolate(start2, end2, start1FrameNumber, false));
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

    /**
     * Clears the list of AUAPFrame.
     */
    public synchronized void clear() {
        keysOfAnim.clear();
        idsForFrameNumbers.clear();
    }


    /**
     * Cleans the list of AUAPFrame of all the frames that are linked to the given ID.
     * @param requestId request id which's frames to delete
     */
    public synchronized void cleanKeysWithId (ID requestId) {
        ListIterator<AUAPFrame> keys = keysOfAnim.listIterator();
        while (keys.hasNext()) {
            AUAPFrame frame = keys.next();
            int frameNumber = frame.getFrameNumber();
            if (idsForFrameNumbers.get(frameNumber) == requestId){
                idsForFrameNumbers.remove(frameNumber);
                keys.remove();
            }
        }
    }
}
