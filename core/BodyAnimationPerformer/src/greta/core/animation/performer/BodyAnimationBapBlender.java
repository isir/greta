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
package greta.core.animation.performer;

import greta.core.animation.Frame;
import greta.core.animation.mpeg4.bap.BAP;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.animation.mpeg4.bap.CancelableBAPFramePerformer;
import greta.core.animation.mpeg4.bap.JointType;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterDependentAdapterThread;
import greta.core.util.CharacterManager;
import greta.core.util.Constants;
import greta.core.util.Mode;
import greta.core.util.enums.interruptions.ReactionType;
import greta.core.util.id.ID;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jing Huang
 * @author Brice Donval
 */
public class BodyAnimationBapBlender extends CharacterDependentAdapterThread implements BAPFrameEmitter, CharacterDependent {

    private final List<BAPFramePerformer> bapFramePerformers = new ArrayList<>();

    private final SortedMap<Integer, ID> requestIds = new TreeMap<>();
    private final SortedMap<Integer, BAPFrame> requestFrames = new TreeMap<>();

    // Parameters for the replaceFramesWithInterruption methods
    private double initialHoldDurationInSecond = 0;
    private double followingHoldDurationInSecond = 0;

    public BodyAnimationBapBlender(CharacterManager cm) {
        setCharacterManager(cm);
        this.start();

        if (!getCharacterManager().getValueString("INTERRUPTION_GESTURE_HOLD_DUR").trim().isEmpty()) {
            initialHoldDurationInSecond = getCharacterManager().getValueDouble("INTERRUPTION_GESTURE_HOLD_DUR");
        }

        if (!getCharacterManager().getValueString("INTERRUPTION_GESTURE_RETRACT_DUR").trim().isEmpty()) {
            followingHoldDurationInSecond = getCharacterManager().getValueDouble("INTERRUPTION_GESTURE_RETRACT_DUR");
        }

        //getCharacterManager().add(this);
    }

    @Override
    public void addBAPFramePerformer(BAPFramePerformer bapFramePerformer) {
        if (bapFramePerformer != null) {
            bapFramePerformers.add(bapFramePerformer);
        }
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer bapFramePerformer) {
        if (bapFramePerformer != null) {
            bapFramePerformers.remove(bapFramePerformer);
        }
    }

    @Override
    public void run() {
        int currentFrameNumber;
        int lastBAPFrameSent = 0;

        while (true) {
            synchronized (this) {
                currentFrameNumber = Timer.getCurrentFrameNumber() + 1;
                SortedMap<Integer, ID> currentRequestIds = requestIds.headMap(currentFrameNumber);

                for (Map.Entry<Integer, ID> entry : currentRequestIds.entrySet()) {
                    ID currentRequestId = entry.getValue();
                    BAPFrame currentBAPFrame = requestFrames.remove(entry.getKey());
                    if (currentBAPFrame.getFrameNumber() > lastBAPFrameSent) {
                        sendFrame(currentBAPFrame, currentRequestId);
                        lastBAPFrameSent = currentBAPFrame.getFrameNumber();
                    }
                }

                currentRequestIds.clear();
            }
            try {
                Thread.sleep(10); // in ms
            } catch (InterruptedException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void sendFrame(BAPFrame bapFrame, ID requestId) {
        List<BAPFrame> bapFrames = new ArrayList<>(1);
        bapFrames.add(bapFrame);
        sendFrames(bapFrames, requestId);
    }

    private void sendFrames(List<BAPFrame> bapFrames, ID requestId) {
        for (BAPFramePerformer performer : bapFramePerformers) {
            performer.performBAPFrames(new ArrayList<>(bapFrames), requestId);
        }
    }

    public void updateFramesAndSend(List<BAPFrame> bapFrames, ID requestId, Mode mode) {
        sendFrames(bapFrames, requestId);
    }

    private void updateFrame(BAPFrame bapFrame, ID requestId) {
        synchronized (this) {
            int bapFrameNumber = bapFrame.getFrameNumber();
            requestIds.put(bapFrameNumber, requestId);
            requestFrames.put(bapFrameNumber, bapFrame);
        }
    }

    public void cancelFramesWithId(ID requestId) {
        synchronized (this) {
            Iterator<Map.Entry<Integer, ID>> requestIdsIterator = requestIds.entrySet().iterator();
            while (requestIdsIterator.hasNext()) {
                Map.Entry<Integer, ID> frameNumberIdPair = requestIdsIterator.next();
                if (frameNumberIdPair.getValue() == requestId) {
                    requestFrames.remove(frameNumberIdPair.getKey());
                    requestIdsIterator.remove();
                }
            }
            for (BAPFramePerformer performer : bapFramePerformers) {
                if (performer instanceof CancelableBAPFramePerformer) {
                    ((CancelableBAPFramePerformer) performer).cancelBAPFramesById(requestId);
                }
            }
        }
    }

    public void updateFrames(List<BAPFrame> bapFrames, ID requestId, Mode mode) {
        switch (mode.getCompositionType()) {
            case append: {
                appendFrames(bapFrames, requestId, mode);
                break;
            }
            case blend: {
                blendFrames(bapFrames, requestId, mode);
                break;
            }
            case replace: {
                switch (mode.getReactionType()) {
                    /*case NONE: {
                        replaceFrames(bapFrames, requestId, mode);
                        break;
                    }
                    case REPLAN: {
                        replaceFrames(bapFrames, requestId, mode);
                        break;
                    }*/ // Same as default case
                    case HALT: /*{
                        replaceFramesWithInterruption(bapFrames, requestId, mode);
                        break;
                    }*/ // Same as OVERLAP case
                    case OVERLAP: {
                        replaceFramesWithInterruption(bapFrames, requestId, mode);
                        break;
                    }
                    default: {
                        replaceFrames(bapFrames, requestId, mode);
                        break;
                    }
                }
                break;
            }
            default: {
                System.out.println("[BodyAnimationBapBlender] updateFrames() : mode is wrong");
                break;
            }
        }
    }

    public void appendFrames(List<BAPFrame> bapFrames, ID requestId, Mode mode) {
        for (BAPFrame bapFrame : bapFrames) {
            updateFrame(bapFrame, requestId);
        }
    }

    public void blendFrames(List<BAPFrame> bapFrames, ID requestId, Mode mode) {
        for (BAPFrame bapFrame : bapFrames) {
            updateFrame(bapFrame, requestId);
        }
    }

    public void replaceFrames(List<BAPFrame> bapFrames, ID requestId, Mode mode) {
        synchronized (this) {
            BAPFrame lastNewBAPFrame = bapFrames.get(bapFrames.size() - 1);
            int lastNewBAPFrameNumber = lastNewBAPFrame.getFrameNumber();
            int fadeDurationInFrame = Math.min(10, bapFrames.size());

            int i = 1;
            for (BAPFrame bapFrame : bapFrames) {
                if (i < fadeDurationInFrame) {
                    double weight = (double) i / (double) fadeDurationInFrame;
                    blend(bapFrame, weight);
                }
                ++i;
                updateFrame(bapFrame, requestId);
            }

            SortedMap<Integer, ID> requestIdsAfterThisOne = requestIds.tailMap(lastNewBAPFrameNumber + 1);
            SortedMap<Integer, BAPFrame> requestFramesAfterTheseOnes = requestFrames.tailMap(lastNewBAPFrameNumber + 1);

            requestIdsAfterThisOne.clear();
            requestFramesAfterTheseOnes.clear();
        }
    }

    public void replaceFramesWithInterruption(List<BAPFrame> bapFrames, ID requestId, Mode mode) {
        synchronized (this) {
            // Removes the useless first bapFrame of the input
            bapFrames.remove(0);

            /* -------------------------------------------------- */

            // In OVERLAP reactions the gesture is not held but expanded instead
            if (mode.getReactionType() == ReactionType.OVERLAP) {
                initialHoldDurationInSecond = 0;
            }

            int initialHoldDurationInFrame = (int) (initialHoldDurationInSecond * Constants.FRAME_PER_SECOND);
            int followingHoldDurationInFrame = (int) (followingHoldDurationInSecond * Constants.FRAME_PER_SECOND);

            int fadeDurationWithFollowingHoldInFrame = 10;
            int fadeDurationWithAfterInFrame = 10;

            int followingHoldSign;
            {
                int expanded = 1;
                int retracted = -1;
                followingHoldSign = (mode.getReactionType() == ReactionType.OVERLAP) ? expanded : retracted;
            }

            /* -------------------------------------------------- */

            // Defines the output list of bapFrames to send
            List<BAPFrame> bapFramesToSend = new ArrayList<>();

            // Defines the list of hold and expanded or retracted hold bapFrames to blend with the output list of bapFrames to send
            List<BAPFrame> initialAndFollowingHoldBAPFramesToBlend = new ArrayList<>();

            /* ------------------------------ */

            // Duplicates the first current frame to create the hold
            BAPFrame firstInitialHoldBAPFrame = requestFrames.get(bapFrames.get(0).getFrameNumber());

            // Filters the first hold bapFrame to keep only arms and legs baps
            for (int i = 0; i < firstInitialHoldBAPFrame.getAnimationParametersList().size(); ++i) {
                if ((i < 4) || (i > 47 && i < 123) || (i > 180)) {
                    firstInitialHoldBAPFrame.getAnimationParametersList().get(i).set(false, 0);
                }
            }

            // Creates the hold and adds it to the list of hold and expanded or retracted hold bapFrames to blend with the output list of bapFrames to send
            List<BAPFrame> initialHoldBAPFrames = new ArrayList<>();
            int lastInitialHoldBAPFrameNumber = firstInitialHoldBAPFrame.getFrameNumber();
            for (int i = 0; i < initialHoldDurationInFrame; ++i) {
                BAPFrame initialHoldBAPFrame = new BAPFrame(firstInitialHoldBAPFrame);
                initialHoldBAPFrame.setFrameNumber(lastInitialHoldBAPFrameNumber++);
                initialHoldBAPFrames.add(initialHoldBAPFrame);
            }
            initialAndFollowingHoldBAPFramesToBlend.addAll(initialHoldBAPFrames);

            /* ------------------------------ */

            // Creates a fade between the hold and the future expanded or retracted hold and adds it to the list of hold and expanded or retracted hold bapFrames to blend with the output list of bapFrames to send
            List<BAPFrame> fadeWithFollowingHoldBAPFrames = new ArrayList<>();
            int lastFadeWithFollowingHoldBAPFrameNumber = lastInitialHoldBAPFrameNumber;
            for (int i = 0; i < fadeDurationWithFollowingHoldInFrame; ++i) {
                BAPFrame fadeWithFollowingHoldBAPFrame = new BAPFrame(firstInitialHoldBAPFrame);
                fadeWithFollowingHoldBAPFrame.setFrameNumber(lastFadeWithFollowingHoldBAPFrameNumber++);
                fadeWithFollowingHoldBAPFrames.add(fadeWithFollowingHoldBAPFrame);
            }
            {
                BAPFrame emptyBAPFrame = new BAPFrame();
                int i = 1;
                for (BAPFrame fadeWithFollowingHoldBAPFrame : fadeWithFollowingHoldBAPFrames) {
                    if (i < fadeDurationWithFollowingHoldInFrame + 1) {
                        double weight = 1 + followingHoldSign * (((double) i / (double) fadeDurationWithFollowingHoldInFrame) / 4.0);
                        blend(emptyBAPFrame, fadeWithFollowingHoldBAPFrame, weight);
                    }
                    ++i;
                }
            }
            initialAndFollowingHoldBAPFramesToBlend.addAll(fadeWithFollowingHoldBAPFrames);

            /* ------------------------------ */

            // Duplicates the last pose to create the expanded or retracted hold and adds it to the list of hold and expanded or retracted hold bapFrames to blend with the output list of bapFrames to send
            BAPFrame firstFollowingHoldBAPFrame = fadeWithFollowingHoldBAPFrames.get(fadeWithFollowingHoldBAPFrames.size() - 1);

            List<BAPFrame> followingHoldBAPFrames = new ArrayList<>();
            int lastFollowingHoldBAPFrameNumber = lastFadeWithFollowingHoldBAPFrameNumber;
            for (int i = 0; i < followingHoldDurationInFrame; ++i) {
                BAPFrame followingHoldBAPFrame = new BAPFrame(firstFollowingHoldBAPFrame);
                followingHoldBAPFrame.setFrameNumber(lastFollowingHoldBAPFrameNumber++);
                followingHoldBAPFrames.add(followingHoldBAPFrame);
            }
            initialAndFollowingHoldBAPFramesToBlend.addAll(followingHoldBAPFrames);

            /* ------------------------------ */

            // Creates a fade between the expanded or retracted hold and the future gestures and adds it to the list of hold and expanded or retracted hold bapFrames to blend with the output list of bapFrames to send
            BAPFrame firstAfterBAPFrame = new BAPFrame();
            if (bapFrames.size() > initialAndFollowingHoldBAPFramesToBlend.size() + fadeDurationWithAfterInFrame) {
                firstAfterBAPFrame = bapFrames.get(initialAndFollowingHoldBAPFramesToBlend.size() + fadeDurationWithAfterInFrame);
                // Filters the first hold bapFrame to keep only arms and legs baps
                for (int i = 0; i < firstAfterBAPFrame.getAnimationParametersList().size(); ++i) {
                    if ((i < 4) || (i > 47 && i < 123) || (i > 180)) {
                        firstAfterBAPFrame.getAnimationParametersList().get(i).set(false, 0);
                    }
                }
            }

            List<BAPFrame> fadeWithAfterBAPFrames = new ArrayList<>();
            int lastFadeWithAfterBAPFrameNumber = lastFollowingHoldBAPFrameNumber;
            for (int i = 0; i < fadeDurationWithAfterInFrame; ++i) {
                BAPFrame fadeWithAfterBAPFrame = new BAPFrame(firstAfterBAPFrame);
                fadeWithAfterBAPFrame.setFrameNumber(lastFadeWithAfterBAPFrameNumber++);
                fadeWithAfterBAPFrames.add(fadeWithAfterBAPFrame);
            }
            {
                BAPFrame lastFollowingHoldBAPFrame = fadeWithFollowingHoldBAPFrames.get(fadeWithFollowingHoldBAPFrames.size() - 1);
                int i = 1;
                for (BAPFrame fadeWithAfterBAPFrame : fadeWithAfterBAPFrames) {
                    if (i < fadeDurationWithAfterInFrame) {
                        double weight = (double) i / (double) fadeDurationWithAfterInFrame;
                        blend(lastFollowingHoldBAPFrame, fadeWithAfterBAPFrame, weight);
                    }
                    ++i;
                }
            }
            initialAndFollowingHoldBAPFramesToBlend.addAll(fadeWithAfterBAPFrames);

            /* ------------------------------ */

            // Creates a map of input bapFrames indexed by frame numbers
            SortedMap<Integer, BAPFrame> indexedBAPFrames = new TreeMap<>();
            for (BAPFrame bapFrame : bapFrames) {
                indexedBAPFrames.put(bapFrame.getFrameNumber(), bapFrame);
            }

            // Blends the first and the following hold with the output list of bapFrames to send
            for (BAPFrame initialAndFollowingHoldBAPFrameToBlend : initialAndFollowingHoldBAPFramesToBlend) {
                BAPFrame bapFrame = indexedBAPFrames.get(initialAndFollowingHoldBAPFrameToBlend.getFrameNumber());

                if (bapFrame == null) {
                    bapFramesToSend.add(initialAndFollowingHoldBAPFrameToBlend);
                } else {
                    BAPFrame fromBAPFrame = initialAndFollowingHoldBAPFrameToBlend;
                    BAPFrame toBAPFrame = bapFrame;

                    for (int i = 0; i < toBAPFrame.getAnimationParametersList().size(); ++i) {
                        BAP fromBAP = fromBAPFrame.getAnimationParametersList().get(i);
                        BAP toBAP = toBAPFrame.getAnimationParametersList().get(i);
                        if (fromBAP.getMask()) {
                            toBAP.applyValue(fromBAP.getValue());
                        }
                    }
                    bapFramesToSend.add(toBAPFrame);
                }
            }

            /* ------------------------------ */

            // Adds the rest of the input bapFrames to the output list of bapFrames to send
            for (int i = bapFramesToSend.size(); i < bapFrames.size(); ++i) {
                bapFramesToSend.add(bapFrames.get(i));
            }

            /* ------------------------------ */

            // Sends the output list of bapFrames
            for (BAPFrame bapFrameToSend : bapFramesToSend) {
                updateFrame(bapFrameToSend, requestId);
            }

            /* -------------------------------------------------- */

            BAPFrame lastBAPFrameToSend = bapFramesToSend.get(bapFramesToSend.size() - 1);
            int lastBAPFrameToSendNumber = lastBAPFrameToSend.getFrameNumber();

            SortedMap<Integer, ID> requestIdsAfterThisOne = requestIds.tailMap(lastBAPFrameToSendNumber + 1);
            SortedMap<Integer, BAPFrame> requestFramesAfterTheseOnes = requestFrames.tailMap(lastBAPFrameToSendNumber + 1);

            requestIdsAfterThisOne.clear();
            requestFramesAfterTheseOnes.clear();
        }
    }

    public void blend(BAPFrame fromBAPFrame, BAPFrame toBAPFrame, double weight) {
        if (fromBAPFrame != null && toBAPFrame != null) {
            for (int i = 0; i < toBAPFrame.getAnimationParametersList().size(); ++i) {
                BAP fromBAP = fromBAPFrame.getAnimationParametersList().get(i);
                BAP toBAP = toBAPFrame.getAnimationParametersList().get(i);
                if (fromBAP.getMask() || toBAP.getMask()) {
                    toBAP.applyValue((int) (toBAP.getValue() * weight + fromBAP.getValue() * (1 - weight)));
                }
            }
        }
    }

    public void blend(BAPFrame toBAPFrame, double weight) {
        if (toBAPFrame != null) {
            BAPFrame fromBAPFrame = requestFrames.get(toBAPFrame.getFrameNumber());
            if (fromBAPFrame != null) {
                for (int i = 0; i < toBAPFrame.getAnimationParametersList().size(); ++i) {
                    BAP fromBAP = fromBAPFrame.getAnimationParametersList().get(i);
                    BAP toBAP = toBAPFrame.getAnimationParametersList().get(i);
                    if (fromBAP.getMask() || toBAP.getMask()) {
                        toBAP.applyValue((int) (toBAP.getValue() * weight + fromBAP.getValue() * (1 - weight)));
                    }
                }
            }
        }
    }

    /**
     *
     * @param info
     * @param index
     * @return the {@code BAPFrame} at the specified index
     */
    public static BAPFrame getBapFrame(Frame info, int index) {
        BAPFrame bapFrame = new BAPFrame();
        bapFrame.setFrameNumber(index);
        HashMap<String, Quaternion> results = info.getRotations();

        for (String name : results.keySet()) {
            Quaternion q = results.get(name);
            Vec3d angle = q.getEulerAngleXYZ();
            JointType joint = JointType.get(name);
            BAPType z = joint.rotationZ;
            BAPType y = joint.rotationY;
            BAPType x = joint.rotationX;
            bapFrame.setRadianValue(z, angle.z());
            bapFrame.setRadianValue(y, angle.y());
            bapFrame.setRadianValue(x, angle.x());
        }

        Vec3d t = info.getRootTranslation();
        if (!(t.x() == 0 && t.y() == 0 && t.z() == 0)) {
            bapFrame.applyValue(BAPType.HumanoidRoot_tr_lateral, (int) (t.x() * 1.9 * 10));
            bapFrame.applyValue(BAPType.HumanoidRoot_tr_vertical, (int) (t.y() * 1.9 * 10));
            bapFrame.applyValue(BAPType.HumanoidRoot_tr_frontal, (int) (t.z() * 1.9 * 10));
        }

        return bapFrame;
    }

    @Override
    public void onCharacterChanged() {
        if (!getCharacterManager().getValueString("INTERRUPTION_GESTURE_HOLD_DUR").trim().isEmpty()) {
            initialHoldDurationInSecond = getCharacterManager().getValueDouble("INTERRUPTION_GESTURE_HOLD_DUR");
        } else {
            initialHoldDurationInSecond = 0;
        }

        if (!getCharacterManager().getValueString("INTERRUPTION_GESTURE_RETRACT_DUR").trim().isEmpty()) {
            followingHoldDurationInSecond = getCharacterManager().getValueDouble("INTERRUPTION_GESTURE_RETRACT_DUR");
        } else {
            followingHoldDurationInSecond = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        getCharacterManager().remove(this);
        super.finalize();
    }
}
