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
import greta.core.signals.FaceSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.util.Mode;
import greta.core.util.animationparameters.AnimationParametersFrame;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Florian Pecune
 */
public class ShoreReceiver extends APReceiver implements SignalEmitter {

private List<SignalPerformer> signalPerformers = new ArrayList<SignalPerformer>();

    private ID id1;

    public ShoreReceiver(int port) {
        super(port);
    }

    public ShoreReceiver() {
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


        if (m.APFrameList.get(0).getAnimParamList().get(5).value > 50) {
            sendFacialExpression("faceexp=joy");
        }
        if (m.APFrameList.get(0).getAnimParamList().get(6).value > 50) {
            sendFacialExpression("faceexp=sadness");
        }
        if (m.APFrameList.get(0).getAnimParamList().get(7).value > 50) {
           sendFacialExpression("faceexp=anger");
        }
        if (m.APFrameList.get(0).getAnimParamList().get(8).value > 50) {
            sendFacialExpression("faceexp=surprise");
        }
        /*if (m.APFrameList.get(0).getAnimParamList().get(12).value > 30) {
            sendFacialExpression("faceexp=left_wink");
        }
        if (m.APFrameList.get(0).getAnimParamList().get(13).value > 30) {
            sendFacialExpression("faceexp=right_wink");
        }*/


        /*Affichage des infos dans les logs
        Logs.info("% n° de Frame : " + m.APFrameList.get(0).getFrameNumber());
        Logs.info("--------------------------------------------------------------------");
        Logs.info("Face n°" + m.getId());
        Logs.info("Position du visage : Gauche " + m.APFrameList.get(0).getAnimParamList().get(1).value + " Haut " + m.APFrameList.get(0).getAnimParamList().get(2).value + " Droite " + m.APFrameList.get(0).getAnimParamList().get(3).value + " Bas " + m.APFrameList.get(0).getAnimParamList().get(4).value);
        Logs.info("% de Joie : " + m.APFrameList.get(0).getAnimParamList().get(5).value);
        Logs.info("% de Tristesse : " + m.APFrameList.get(0).getAnimParamList().get(6).value);
        Logs.info("% de Colère : " + m.APFrameList.get(0).getAnimParamList().get(7).value);
        Logs.info("% de Surprise : " + m.APFrameList.get(0).getAnimParamList().get(8).value);
        if (m.APFrameList.get(0).getAnimParamList().get(14).value = 0) {
            Logs.info("Femme");
        } else {
            Logs.info("Homme");
        }
        Logs.info("Age : " + m.APFrameList.get(0).getAnimParamList().get(15).value);
        Logs.info("Ouverture de la bouche : " + m.APFrameList.get(0).getAnimParamList().get(11).value);
        Logs.info("Ouverture des yeux : " + m.APFrameList.get(0).getAnimParamList().get(12).value + " pour l'oeil gauche et " + m.APFrameList.get(0).getAnimParamList().get(13).value + " pour l'oeil droit");*/


        //Logs.info("Emotion reçue de FAtiMA : " + m.id + " d'une intensité de " + m.type);
        //sendFacialExpression("faceexp="+m.id);
    }


    private void sendFacialExpression(String emotion) {

            List<Signal> Fsignals = new ArrayList<Signal>();
            FaceSignal faceExp = new FaceSignal("faceexp");
            faceExp.setIntensity(1);
            faceExp.getStart().setValue(0);
            faceExp.getEnd().setValue(3);
            faceExp.setReference(emotion);
            Fsignals.add(faceExp);
            for (SignalPerformer perf : signalPerformers) {
                perf.performSignals(Fsignals, id1, new Mode(CompositionType.blend));
            }
    }

    @Override
    public void addSignalPerformer(SignalPerformer sp) {
        if (sp != null) {
            signalPerformers.add(sp);
        }
    }

    @Override
    public void removeSignalPerformer(SignalPerformer sp) {
    }

    @Override
    protected AnimationParametersFrame newAnimParamFrame(int frameNumber) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
