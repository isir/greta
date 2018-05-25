/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.behaviorrealizer.keyframegenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import vib.core.keyframes.Keyframe;
import vib.core.keyframes.face.AUAPFrameInterpolator;
import vib.core.keyframes.face.AUKeyFrame;
import vib.core.repositories.AUAPFrame;
import vib.core.repositories.AUItem;
import vib.core.signals.FaceSignal;
import vib.core.signals.Signal;
import vib.core.util.Constants;
import vib.core.util.enums.Side;

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
