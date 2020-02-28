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
package greta.core.util.animationparameters;

import greta.core.util.Constants;
import greta.core.util.id.ID;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

// TODO see if we don't need to clear sometimes ?
/**
 *
 * @author Andre-Marie Pez
 * @author Ken Prepin
 */
public class APFrameList <APF extends AnimationParametersFrame> {
    private LinkedList<APF> apFrameList;

    /**
     * Ids of frames corresponding to their frame number.
     * We consider that we can't have 2 frames at the same frame number as in addFrame we blend in this case.
     */
    private Map<Integer, ID> idsForFrameNumbers;

    public APFrameList (APF firstAPFrame) {
        apFrameList = new LinkedList<>();
        apFrameList.add(firstAPFrame);
        firstAPFrame.setFrameNumber(0);
        idsForFrameNumbers = new TreeMap<>();
        idsForFrameNumbers.put(0, null);
    }

    public synchronized void addFrame (APF apFrame) {
        addFrame(apFrame, null);
    }

    public synchronized void addFrame (APF apFrame, ID requestId) {
        ListIterator<APF> iter = apFrameList.listIterator(apFrameList.size());
        APF inlist;

        while (iter.hasPrevious()) {
            inlist = iter.previous();
            int frameNumber = apFrame.getFrameNumber();

            if (frameNumber > inlist.getFrameNumber()) {
                iter.next();
                iter.add(apFrame);
                idsForFrameNumbers.put(frameNumber, requestId);
                break;
            }

            if (frameNumber == inlist.getFrameNumber()) {
                blend(inlist, apFrame);
                break;
            }
        }
    }

    public void addFrames (List<APF> apframeList) {
        addFrames(apframeList, null);
    }

    public void addFrames (List<APF> apframeList, ID requestId) {
        for (APF apf : apframeList) {
            addFrame(apf, requestId);
        }
    }

    public synchronized List<APF> getListCopy () {
        return new ArrayList<>(apFrameList);
    }

    private void blend (APF firstAPFrame, APF newAPFrame) {
        for (int i = 0; i < newAPFrame.size(); ++i) {
            AnimationParameter ap = (AnimationParameter) newAPFrame.getAnimationParametersList().get(i);
            if (ap.getMask()) {
                firstAPFrame.setAnimationParameter(i, ap.getValue());
            }
        }
    }

    public synchronized void updateFrames () {
        updateFramesToTime((int) (Timer.getTime() * Constants.FRAME_PER_SECOND));
    }

    public synchronized void updateFramesToTime (int firstFrame) {
        if (apFrameList.isEmpty()) { return; }

        APF firstAPFrame = apFrameList.getFirst();
        ListIterator<APF> iter = apFrameList.listIterator();

        while (iter.hasNext()) {
            APF frame = iter.next();
            int frameNumber = frame.getFrameNumber();
            if (frameNumber > firstFrame) {
                break;
            }
            if (frame != firstAPFrame) {
                blend(firstAPFrame, frame);
                iter.remove();
                idsForFrameNumbers.remove(frameNumber);
            }
        }

        int firstAPFrameNumber = firstAPFrame.getFrameNumber();
        ID firstAPFrameId = idsForFrameNumbers.get(firstAPFrameNumber);
        idsForFrameNumbers.remove(firstAPFrameNumber);
        firstAPFrame.setFrameNumber(firstFrame);
        idsForFrameNumbers.put(firstFrame, firstAPFrameId);
    }

    public synchronized APF getFrameAtTime (int targetFrame) {
        updateFramesToTime(targetFrame);
        return apFrameList.peek();
    }

    public synchronized APF getCurrentFrame () {
        updateFrames();
        return apFrameList.peek();
    }

    /**
     * Deletes from apFrameList the frames with the given {@code ID}.
     * @param requestId id which's corresponding frames have to be deleted
     */
    public synchronized void deleteFramesWithId (ID requestId) {
        Iterator<APF> framesIterator = apFrameList.iterator();
        while (framesIterator.hasNext()) {
            APF frame = framesIterator.next();
            int frameNumber = frame.getFrameNumber();
            if (idsForFrameNumbers.get(frameNumber) == requestId){
                idsForFrameNumbers.remove(frameNumber);
                framesIterator.remove();
            }
        }
    }

    /**
     * Removes the given frame if it is in the list.
     * @param frameToDelete frame to be deleted
     */
    public synchronized void deleteFrame (APF frameToDelete) {
        apFrameList.remove(frameToDelete);
    }
}
