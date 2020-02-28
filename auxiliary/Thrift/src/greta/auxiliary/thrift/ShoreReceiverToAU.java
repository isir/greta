/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.thrift;

import greta.auxiliary.thrift.gen_java.Message;
import greta.core.keyframes.face.AUEmitter;
import greta.core.keyframes.face.AUPerformer;
import greta.core.repositories.AUAPFrame;
import greta.core.util.animationparameters.AnimationParametersFrame;
import greta.core.util.enums.Side;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.time.Timer;
import java.util.ArrayList;

/**
 *
 * @author Florian Pecune
 */
public class ShoreReceiverToAU extends APReceiver implements AUEmitter {


    private AUAPFrame _auframe = new AUAPFrame();
    ArrayList<AUPerformer> AUperformers = new ArrayList<AUPerformer>();
    private double joyIntensity;
    private double sadnessIntensity;
    private double angerIntensity;
    private double surprisedIntensity;
    private double mouthOpenness;
    private double leftEyeOpenness;
    private double rightEyeOpenness;
    public ShoreReceiverToAU(int port) {
        super(port);
    }

    public ShoreReceiverToAU() {
        super();
    }

    @Override
    public void perform(Message m) {

        /*
         * AP List Content
         *
         * 0 = Face id
         * 1 = Left Position of the head
         * 2 = Top Position of the head
         * 3 = Right Position of the head
         * 4 = Bottom Position of the head
         * 5 = % of chance the user is Happy
         * 6 = % of chance the user is Sad
         * 7 = % of chance the user is Angry
         * 8 = % of chance the user is Surprised
         * 9 = % of chance the user is a female
         * 10 = % of chance the user is a male
         * 11 = % of chance user's mouth is opened
         * 12 = % of chance user's left eye is closed
         * 13 = % of chance user's right eye is closed
         * 14 = Gender (0=male, 1=female)
         * 15 = Age
         * 16 = Age Dieviation
         * 17 = Position de l'oeil gauche sur l'axe X
         * 18 = Position de l'oeil gauche sur l'axe Y
         * 19 = Position de l'oeil droit sur l'axe X
         * 20 = Position de l'oeil droit sur l'axe Y
         * 21 = Position du coin gauche de la bouche sur l'axe X
         * 22 = Position du coin gauche de la bouche sur l'axe Y
         * 23 = Position du coin droit de la bouche sur l'axe X
         * 24 = Position du coin droit de la bouche sur l'axe Y
         * 25 = Position du bout du nez sur l'axe X
         * 26 = Position du bout du nez sur l'axe Y
         *
         */

        joyIntensity = (m.APFrameList.get(0).getAnimParamList().get(5).value/100.0);
        sadnessIntensity = (m.APFrameList.get(0).getAnimParamList().get(6).value/100.0);
        angerIntensity = (m.APFrameList.get(0).getAnimParamList().get(7).value/100.0);
        surprisedIntensity = (m.APFrameList.get(0).getAnimParamList().get(8).value/100.0);
        mouthOpenness = (m.APFrameList.get(0).getAnimParamList().get(11).value/100.0);
        leftEyeOpenness = (m.APFrameList.get(0).getAnimParamList().get(12).value/100.0);
        rightEyeOpenness = (m.APFrameList.get(0).getAnimParamList().get(13).value/100.0);


        resetAU();
        _auframe.setAUAP(1, Math.max(sadnessIntensity, surprisedIntensity));
        _auframe.setAUAP(2, surprisedIntensity);
        _auframe.setAUAP(4, Math.max(sadnessIntensity, angerIntensity*0.8));
        _auframe.setAUAP(5, Math.max(surprisedIntensity, angerIntensity*0.3));
        _auframe.setAUAP(6, joyIntensity);
        _auframe.setAUAP(7, (angerIntensity*0.3));
        _auframe.setAUAP(12, joyIntensity);
        _auframe.setAUAP(15, sadnessIntensity);
        _auframe.setAUAP(23, angerIntensity);
        _auframe.setAUAP(26, surprisedIntensity);
        _auframe.setAUAP(27, mouthOpenness);
        _auframe.setAUAP(43, leftEyeOpenness, Side.LEFT);
        _auframe.setAUAP(43, rightEyeOpenness, Side.RIGHT);
        _auframe.setFrameNumber(Timer.getCurrentFrameNumber()+1);
        sendAUAPFrame(_auframe, "from Shore");

    }

    private void sendAUAPFrame(AUAPFrame auapAnimation, String requestId) {
        ArrayList<AUAPFrame> list = new ArrayList<AUAPFrame>();
        list.add(auapAnimation);
        ID id = IDProvider.createID("Shore");
        for (AUPerformer performer : AUperformers) {
            performer.performAUAPFrames(list, id);
        }
    }

    private void resetAU() {
    int i;
        for (i = 1; i <= AUAPFrame.NUM_OF_AUS; i++) {
            _auframe.setAUAP(i, 0);
        }
        sendAUAPFrame(_auframe, "Shore reset");
    }

    @Override
    protected AnimationParametersFrame newAnimParamFrame(int frameNumber) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addAUPerformer(AUPerformer aup) {
        if (aup != null) {
            AUperformers.add(aup);
        }
    }

    @Override
    public void removeAUPerformer(AUPerformer aup) {
        AUperformers.remove(aup);
    }

}
