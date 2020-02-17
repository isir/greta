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
package greta.core.behaviorrealizer.keyframegenerator;

import greta.core.keyframes.Keyframe;
import greta.core.keyframes.face.AUAPFrameInterpolator;
import greta.core.keyframes.face.AUKeyFrame;
import greta.core.repositories.AUAPFrame;
import greta.core.repositories.AUItem;
import greta.core.signals.FaceSignal;
import greta.core.signals.Signal;
import greta.core.util.Constants;
import greta.core.util.enums.Side;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class FaceKeyframeGenerator extends KeyframeGenerator {

    AUAPFrameInterpolator interpolator;
    public FaceKeyframeGenerator() {
        super(FaceSignal.class);
        interpolator = new AUAPFrameInterpolator();
    }

    public void findExistingAU(List<Keyframe> existingKeyframes){
        if( ! signals.isEmpty()){ //if the signals list is empty, generateKeyframes will not be called. So we don't need to get the existing AUs
            ListIterator<Keyframe> listIter = existingKeyframes.listIterator();
            ArrayList<AUAPFrame> aus = new ArrayList<AUAPFrame>();
            while(listIter.hasNext()){
                Keyframe kf = listIter.next();
                if(kf instanceof AUKeyFrame){
                    aus.add(((AUKeyFrame)kf).getAus());
                    listIter.remove();
                }
            }
            interpolator.blendSegment(aus);
            interpolator.cleanEmptyKeys();
        }
    }

    @Override
    protected void generateKeyframes(List<Signal> inputSignals, List<Keyframe> outputKeyframes) {
        for (Signal signal : inputSignals) {
            FaceSignal face = (FaceSignal) signal;
            //if not scheduled it does not contain good time markers
            if (face.isScheduled()) {
                boolean attackEqualsDecay = face.getTimeMarker("attack").getValue() == face.getTimeMarker("decay").getValue();

                AUAPFrame start = generateAUAPFrameFromAUItems(face, "start", 0.0);
                AUAPFrame attack = generateAUAPFrameFromAUItems(face, "attack",   attackEqualsDecay ? 0.95 : 1.0);
                AUAPFrame decay = generateAUAPFrameFromAUItems(face, "decay",     attackEqualsDecay ? 0.95 : 0.9);
                AUAPFrame sustain = generateAUAPFrameFromAUItems(face, "sustain", attackEqualsDecay ? 0.95 : 0.9);
                AUAPFrame end = generateAUAPFrameFromAUItems(face, "end", 0.0);

                interpolator.blendSegment(start, attack, decay, sustain, end);
            }
        }
        if( ! interpolator.isEmpty()){
            for(AUAPFrame frame : interpolator.getAUAPFrameList()){
                double time = frame.getFrameNumber() * Constants.FRAME_DURATION_SECONDS;
                AUKeyFrame auKeyFrame = new AUKeyFrame("AUs_at_"+time, time, frame);
                outputKeyframes.add(auKeyFrame);
            }
        }
        interpolator.clear();
    }


    private AUAPFrame generateAUAPFrameFromAUItems(FaceSignal face, String tmName, double scale) {
        return generateAUAPFrameFromAUItems(face.getActionUnits(), face.getTimeMarker(tmName).getValue(), scale * face.getIntensity());
    }

    private AUAPFrame generateAUAPFrameFromAUItems(List<AUItem> aus, double time, double scale){
        int timeIndex = (int)(time*Constants.FRAME_PER_SECOND);
        AUAPFrame auapFrame = new AUAPFrame(timeIndex);
        for(AUItem au : aus){
            if (au.getSide() == Side.LEFT || au.getSide() == Side.BOTH) {
                auapFrame.setAUAPleft(au.getAUnum(), au.getIntensity() * scale);
            }
            if (au.getSide() == Side.RIGHT || au.getSide() == Side.BOTH) {
                auapFrame.setAUAPright(au.getAUnum(), au.getIntensity() * scale);
            }
        }
        return auapFrame;
    }

    @Override
    protected Comparator<Signal> getComparator() {
        return emptyComparator;
    }

}
