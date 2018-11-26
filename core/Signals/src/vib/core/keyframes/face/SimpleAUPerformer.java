/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes.face;

import java.util.ArrayList;
import java.util.List;
import vib.core.animation.mpeg4.fap.FAPFrame;
import vib.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import vib.core.animation.mpeg4.fap.FAPType;
import vib.core.repositories.AUAP;
import vib.core.repositories.AUAPFrame;
import vib.core.repositories.AULibrary;
import vib.core.repositories.FLExpression;
import vib.core.repositories.FLExpression.FAPItem;
import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.enums.Side;
import vib.core.util.id.ID;

/**
 *
 * @author Radoslaw Niewiadomski
 * @author Ken Prepin
 * @author Andre-Marie Pez
 */
public class SimpleAUPerformer extends FAPFrameEmitterImpl implements AUPerformer, CharacterDependent {

    private static Side[] wantedSides = {Side.RIGHT, Side.LEFT};
    private CharacterManager characterManager;
    private AULibrary auLibrary;

    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        if(characterManager==null)
            characterManager = CharacterManager.getStaticInstance();
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        this.characterManager = characterManager;
    }
    
    public SimpleAUPerformer(CharacterManager cm){
        characterManager = cm;
        auLibrary = new AULibrary(cm);
    }

    @Override
    public void performAUAPFrame(AUAPFrame auapAnimation, ID requestId) {
        sendFAPFrame(requestId, toFAPFrame(auapAnimation));
    }

    @Override
    public void performAUAPFrames(List<AUAPFrame> auapsAnimation, ID requestId) {
        ArrayList<FAPFrame> fapframes = new ArrayList<FAPFrame>(auapsAnimation.size());
        for (AUAPFrame auFrame : auapsAnimation) {
            fapframes.add(toFAPFrame(auFrame));
        }
        sendFAPFrames(requestId, fapframes);
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
        for (int au_nr = 1; au_nr <= AUAPFrame.NUM_OF_AUS; au_nr++) {
            if (auFrame.useActionUnit(au_nr)) {
                FLExpression expression = auLibrary.findExpression("AU" + au_nr);
                if (expression != null) {
                    List<FAPItem> au_faps = expression.getFAPs();
                    for (Side side : wantedSides) {
                        AUAP auap = auFrame.getAUAP(au_nr, side);
                        if (auap.getMask()) {
                            double temp_intensity = auap.getNormalizedValue();
                            for (FAPItem fap : au_faps) {
                                if (((side == Side.RIGHT) && fap.type.isRight()) || ((side == Side.LEFT) && fap.type.isLeft())) {
                                    int fap_intensity = (int) (fap.value * temp_intensity);
                                    //find max
                                    if ((!max.getMask(fap.type)) || max.getValue(fap.type) < fap_intensity) {
                                        max.applyValue(fap.type, fap_intensity);
                                    }
                                    //find min
                                    if ((!min.getMask(fap.type)) || min.getValue(fap.type) > fap_intensity) {
                                        min.applyValue(fap.type, fap_intensity);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FAPFrame result = max;
        for (int i = 0; i < FAPType.NUMFAPS; i++) {
            if (min.getMask(i)) {
                if (max.getMask(i)) {
                    //if there is only positive values, we use the max.
                    if (min.getValue(i) < 0) {
                        if (max.getValue(i) <= 0) {
                            //if ther is only negative values, we use the min. (max of the absolute values)
                            result.applyValue(i, min.getValue(i));
                        } else {
                            //if there is positive AND negative values, we use the sum of min and max.
                            result.applyValue(i, min.getValue(i) + max.getValue(i));
                        }
                    }
                } else {
                    result.applyValue(i, min.getValue(i));
                }
            }
        }
        return result;
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
