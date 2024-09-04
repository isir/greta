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

import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import greta.core.animation.mpeg4.fap.FAPType;
import greta.core.repositories.AUAP;
import greta.core.repositories.AUAPFrame;
import greta.core.repositories.AULibrary;
import greta.core.repositories.FLExpression;
import greta.core.repositories.FLExpression.FAPItem;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.enums.Side;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Radoslaw Niewiadomski
 * @author Ken Prepin
 * @author Andre-Marie Pez
 */
public class SimpleAUPerformer extends FAPFrameEmitterImpl implements CancelableAUPerformer, CharacterDependent {
    private static Side[] wantedSides = {Side.RIGHT, Side.LEFT};
    private CharacterManager characterManager;
    private AULibrary auLibrary;

    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager () {
        if (characterManager == null) {
            characterManager = CharacterManager.getStaticInstance();
        }
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager (CharacterManager characterManager) {
        this.characterManager = characterManager;
    }

    public SimpleAUPerformer (CharacterManager cm){
        characterManager = cm;
        auLibrary = new AULibrary(cm);
    }

    @Override
    public void performAUAPFrame (AUAPFrame auapAnimation, ID requestId) {
        sendFAPFrame(requestId, toFAPFrame(auapAnimation));
    }

    @Override
    public void performAUAPFrames (List<AUAPFrame> auapsAnimation, ID requestId) {
        ArrayList<FAPFrame> fapFrames = new ArrayList<>(auapsAnimation.size());
        for (AUAPFrame auFrame : auapsAnimation) {
            fapFrames.add(toFAPFrame(auFrame));
        }
        sendFAPFrames(requestId, fapFrames);
    }

    @Override
    public void cancelAUKeyFramesById(ID requestId) {
        cancelFramesWithIDInLinkedPerformers(requestId);
    }

    /**
     * Translate one {@code AUAPFrame} to one {@code FAPFrame};
     *
     * @param auFrame the Action Unit frame to translate
     * @return the corresponding {@code FAPFrame}
     */
    public FAPFrame toFAPFrame(AUAPFrame auFrame) {
        FAPFrame min = new FAPFrame(auFrame.getFrameNumber());
        FAPFrame max = new FAPFrame(auFrame.getFrameNumber());
        for (int auNb = 1; auNb <= AUAPFrame.NUM_OF_AUS; auNb++) {
            if (auFrame.useActionUnit(auNb)) {
                FLExpression expression = auLibrary.findExpression("AU" + auNb);
                if (expression != null) {
                    List<FAPItem> auFaps = expression.getFAPs();
                    for (Side side : wantedSides) {
                        AUAP auap = auFrame.getAUAP(auNb, side);
                        if (auap.getMask()) {
                            double auapNormalizedValue = auap.getNormalizedValue();
                            for (FAPItem fap : auFaps) {
                                if (((side == Side.RIGHT) && fap.type.isRight()) || ((side == Side.LEFT) && fap.type.isLeft())) {
                                    int fapIntensity = (int) (fap.value * auapNormalizedValue);
                                    //find max
                                    if ((!max.getMask(fap.type)) || max.getValue(fap.type) < fapIntensity) {
                                        max.applyValue(fap.type, fapIntensity);
                                    }
                                    //find min
                                    if ((!min.getMask(fap.type)) || min.getValue(fap.type) > fapIntensity) {
                                        min.applyValue(fap.type, fapIntensity);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < FAPType.NUMFAPS; i++) {
            if (min.getMask(i)) {
                if (max.getMask(i)) {
                    //if there is only positive values, we use the max.
                    if (min.getValue(i) < 0) {
                        if (max.getValue(i) <= 0) {
                            //if there is only negative values, we use the min. (max of the absolute values)
                            max.applyValue(i, min.getValue(i));
                        } else {
                            //if there is positive AND negative values, we use the sum of min and max.
                            max.applyValue(i, min.getValue(i) + max.getValue(i));
                        }
                    }
                } else {
                    max.applyValue(i, min.getValue(i));
                }
            }
        }
        return max;
    }

    @Override
    public void onCharacterChanged() {
        System.out.println("SimpleAUPerformer.onCharacterChanged(): nothing done.");
    }

    public void UpdateLexicon(){
        //remove the old lexicon to be sure to not have two lexicons
        this.getCharacterManager().remove(auLibrary);
        auLibrary = new AULibrary(this.getCharacterManager());
    }
}
