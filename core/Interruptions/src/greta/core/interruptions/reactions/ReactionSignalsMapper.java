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
package greta.core.interruptions.reactions;

import greta.core.signals.FaceSignal;
import greta.core.signals.HeadSignal;
import greta.core.signals.ShoulderSignal;
import greta.core.signals.Signal;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.CharacterManager;
import greta.core.util.enums.Side;
import greta.core.util.log.Logs;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Angelo Cafaro
 */
public class ReactionSignalsMapper extends CharacterDependentAdapter {

    private int idBML;
    private List<Signal> reactionSignals;

    public ReactionSignalsMapper(CharacterManager cm) {
        setCharacterManager(cm);
        idBML = 0;
        reactionSignals = new ArrayList<Signal>();
    }

    public void clearAll() {
        idBML = 0;
        reactionSignals.clear();
    }

    public List<Signal> getReactionSignals() {
        return reactionSignals;
    }

    public void mapInterruptionReactionToSignal(InterruptionReaction interruptionReaction) {


        if (interruptionReaction == null) {
            Logs.error("Reaction to Signal Mapper received a null pointer to map from InterruptionReactio to Signal.");
        }
        else {

            Signal convertedSignal = null;
            idBML++;
            String ID = "bml_" + idBML;
            float amplitude = interruptionReaction.getParameters().getAmplitude();
            float duration = interruptionReaction.getParameters().getDuration();
            DecimalFormat decimalFormat = new DecimalFormat("#.00");

            switch(interruptionReaction.getBehaviorType()){

                case HEAD_TILT: {
                    if (amplitude > 0) {
                        convertedSignal = new HeadSignal(ID);
                        String side = (Math.random() < 0.5)?"Left":"Right";
                        ((HeadSignal) convertedSignal).setReference("Aside_" + side + "_Reaction");
                        ((HeadSignal) convertedSignal).setLexeme("Aside_" + side + "_Reaction");
                        ((HeadSignal) convertedSignal).setSPC(amplitude);
                    }
                    break;
                }
                case HEAD_NOD_TOSS: {
                    convertedSignal = new HeadSignal(ID);
                    String lexeme = "Neutral";
                    String reference = "Neutral";

                    if (amplitude < 0) {
                        lexeme = "Nod";
                        ((HeadSignal) convertedSignal).setLexeme("NOD");
                    }
                    else if (amplitude > 0) {
                        lexeme = "Toss";
                        ((HeadSignal) convertedSignal).setLexeme("TOSS");
                    }
                    else {
                        lexeme = "Neutral";
                        ((HeadSignal) convertedSignal).setLexeme("Neutral");
                    }

                    float absAmplitude = Math.abs(amplitude);
                    if ((absAmplitude > 0) && (absAmplitude <= 0.25)) {
                        reference = lexeme + "_Small";
                    } else if ((absAmplitude > 0.25) && (absAmplitude <= 0.5)) {
                        reference = lexeme + "_Middle";
                    } else if ((absAmplitude > 0.5) && (absAmplitude <= 0.75)) {
                        reference = lexeme + "_Big";
                    } else if (absAmplitude > 0.75) {
                        reference = lexeme + "_VeryBig";
                    }
                    else{
                        reference = "Neutral";
                    }
                    ((HeadSignal) convertedSignal).setReference(reference);

                    break;
                }
                case EYES_LIDS_CLOSE: {
                    if (amplitude > 0) {
                        convertedSignal = new FaceSignal(ID);
                        ((FaceSignal) convertedSignal).setIntensity(amplitude);
                        ((FaceSignal) convertedSignal).setCategory("faceexp");
                        ((FaceSignal) convertedSignal).setReference("faceexp=eyes_closed");
                    }
                    break;
                }
                case EYES_BROWS: {
                    if (amplitude > 0) {
                        convertedSignal = new FaceSignal(ID);
                        ((FaceSignal) convertedSignal).setIntensity(amplitude);
                        ((FaceSignal) convertedSignal).setCategory("faceexp");
                        ((FaceSignal) convertedSignal).setReference("faceexp=raise_brows");
                    }
                    else if (amplitude < 0) {
                        convertedSignal = new FaceSignal(ID);
                        ((FaceSignal) convertedSignal).setIntensity(Math.abs(amplitude));
                        ((FaceSignal) convertedSignal).setCategory("faceexp");
                        ((FaceSignal) convertedSignal).setReference("faceexp=lower_brows");
                    }
                    break;
                }
                case EYES_SQUEEZE: {
                    if (amplitude > 0) {
                        convertedSignal = new FaceSignal(ID);
                        ((FaceSignal) convertedSignal).setIntensity(amplitude);
                        ((FaceSignal) convertedSignal).setCategory("faceexp");
                        ((FaceSignal) convertedSignal).setReference("faceexp=AU7");
                    }
                    break;
                }
                case SMILE: {
                    if (amplitude > 0) {
                        convertedSignal = new FaceSignal(ID);
                        ((FaceSignal) convertedSignal).setIntensity(amplitude);
                        ((FaceSignal) convertedSignal).setCategory("faceexp");
                        ((FaceSignal) convertedSignal).setReference("faceexp=joy");
                    }
                    else if (amplitude < 0) {
                        convertedSignal = new FaceSignal(ID);
                        ((FaceSignal) convertedSignal).setIntensity(Math.abs(amplitude));
                        ((FaceSignal) convertedSignal).setCategory("faceexp");
                        ((FaceSignal) convertedSignal).setReference("faceexp=negative_pout");
                    }
                    break;
                }
                case GESTURE_HOLD: {
                    getCharacterManager().setValueDouble("INTERRUPTION_GESTURE_HOLD_DUR", (double) Math.round(duration * 100) / 100);
                    break;
                }
                case GESTURE_RETRACT: {
                    getCharacterManager().setValueDouble("INTERRUPTION_GESTURE_RETRACT_DUR", (double) Math.round(duration * 100) / 100);
                    break;
                }
                case SHOULDERS_UP_FORWARD: {
                    if (amplitude > 0) {
                        convertedSignal = new ShoulderSignal(ID);
                        ((ShoulderSignal) convertedSignal).setReference("custom");
                        ((ShoulderSignal) convertedSignal).setUp(amplitude);
                        ((ShoulderSignal) convertedSignal).setFront(amplitude);
                        ((ShoulderSignal) convertedSignal).setIntensity(amplitude);
                        ((ShoulderSignal) convertedSignal).setSide(Side.BOTH);
                        ((ShoulderSignal) convertedSignal).setRepetition(1.0d);
                        ((ShoulderSignal) convertedSignal).setMode(1);
                        ((ShoulderSignal) convertedSignal).setTorso(0);
                    }
                    break;
                }
                default:{ convertedSignal = null;}
            } // End switch

            // Sets the end of the signal corrensponding to the duration specified in the reaction's parameters
            if (convertedSignal != null) {
                convertedSignal.getEnd().addReference(convertedSignal.getStart(), duration);
                reactionSignals.add(convertedSignal);
            }
        }
    }

    @Override
    public void onCharacterChanged() {
        throw new UnsupportedOperationException("ReactionSignalsMapper.onCharacterChanged() Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
