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

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.fap.FAPType;
import greta.core.repositories.AUAPFrame;
import greta.core.repositories.AUItem;
import greta.core.repositories.AULibrary;
import greta.core.repositories.FLExpression;
import greta.core.repositories.FLExpression.FAPItem;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.List;

/**
 * Head movements by Action Units
 * Head Movements can be alternatively  generated with a tag HEAD
 *
 * @author Radoslaw Niewiadomski
 */
public class SimpleHeadPerformer extends BAPFrameEmitterImpl implements AUPerformer, CharacterDependent {
    private AULibrary auLibrary;
    private CharacterManager characterManager;
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

    public SimpleHeadPerformer(CharacterManager cm){
        setCharacterManager(cm);
        auLibrary = new AULibrary(cm);
    }

    public void performAUItems(AUItem animation[][][], int frame_offset, int animation_duration_frames, ID requestId) {

        //int animation_duration_frames = animation.length;

        //animation[0].length()

        int startTime = 0;

        ArrayList<BAPFrame> bapframes = new ArrayList<BAPFrame>();


        // <actiondeclaration name="AU51">
        //        <fap num="49" value="60000"/>
        // </actiondeclaration>
        //  <actiondeclaration name="AU52">
        //        <fap num="49" value="-60000"/>
        // </actiondeclaration>
        //<actiondeclaration name="AU53">
        //       <fap num="48" value="-25090"/>
        // </actiondeclaration>
        //<actiondeclaration name="AU54">
        //       <fap num="48" value="36000"/>
        //</actiondeclaration>
        //<actiondeclaration name="AU55">
        //        <fap num="50" value="-60000"/>
        // </actiondeclaration>
        // <actiondeclaration name="AU56">
        //         <fap num="50" value="60000"/>
        // </actiondeclaration>


        // 51 	Head Turn Left
        // 52 	Head Turn Right
        // 53 	Head Up
        // 54 	Head Down
        // 55 	Head Tilt Left
        // 56 	Head Tilt Right
        // 57 	Head Forward
        // 58 	Head Back

        int max_vertical = 0;
        int min_vertical = 0;
        int max_horizontal = 0;

        int min_horizontal = 0;
        int max_sagittal = 0;
        int min_sagittal = 0;

        FLExpression expression51 = auLibrary.findExpression("AU" + 51);

        List<FAPItem> au_faps_51 = expression51.getFAPs();

        for (FAPItem fap : au_faps_51) {
            if (fap.type == FAPType.head_yaw) {
                max_vertical = fap.value;
            }
        }

        FLExpression expression52 = auLibrary.findExpression("AU" + 52);

        List<FAPItem> au_faps_52 = expression52.getFAPs();

        for (FAPItem fap : au_faps_52) {
            if (fap.type == FAPType.head_yaw) {
                min_vertical = fap.value;
            }
        }

        FLExpression expression53 = auLibrary.findExpression("AU" + 53);

        List<FAPItem> au_faps_53 = expression53.getFAPs();

        for (FAPItem fap : au_faps_53) {
            if (fap.type == FAPType.head_pitch) {
                min_horizontal = fap.value;
            }
        }


        FLExpression expression54 = auLibrary.findExpression("AU" + 54);

        List<FAPItem> au_faps_54 = expression54.getFAPs();

        for (FAPItem fap : au_faps_54) {
            if (fap.type == FAPType.head_pitch) {
                max_horizontal = fap.value;
            }
        }

        FLExpression expression55 = auLibrary.findExpression("AU" + 55);

        List<FAPItem> au_faps_55 = expression55.getFAPs();

        for (FAPItem fap : au_faps_55) {
            if (fap.type == FAPType.head_roll) {
                min_sagittal = fap.value;
            }
        }


        FLExpression expression56 = auLibrary.findExpression("AU" + 56);

        List<FAPItem> au_faps_56 = expression56.getFAPs();

        for (FAPItem fap : au_faps_56) {
            if (fap.type == FAPType.head_roll) {
                max_sagittal = fap.value;
            }
        }


        for (int frame_nr = 1; frame_nr < animation_duration_frames + 1; frame_nr++) {

            //it is not important which value we use of the third dimention (left, right): both should contain the same value
            AUItem temp51 = animation[51][frame_nr][0];

            AUItem temp52 = animation[52][frame_nr][0];

            AUItem temp53 = animation[53][frame_nr][0];
            AUItem temp54 = animation[54][frame_nr][0];

            AUItem temp55 = animation[55][frame_nr][0];
            AUItem temp56 = animation[56][frame_nr][0];

            //not used at the moment
            AUItem temp57 = animation[57][frame_nr][0];
            AUItem temp58 = animation[58][frame_nr][0];


            int fap49 = (int) (temp51.getIntensity() * (double) max_horizontal + temp52.getIntensity() * (double) min_horizontal);

            int fap48 = (int) (temp53.getIntensity() * (double) min_vertical + temp54.getIntensity() * (double) max_vertical);

            int fap50 = (int) (temp55.getIntensity() * (double) min_sagittal + temp56.getIntensity() * (double) max_sagittal);

            BAPFrame bapframe = new BAPFrame(startTime + frame_nr + frame_offset);

            //OLD BAP FORMAT
            //bapframe.setBAP(98, fap49);
            //bapframe.setBAP(99, fap48);
            //bapframe.setBAP(100, fap50);

            //NEW BAP FORMAT
            bapframe.applyValue(49, fap49);
            bapframe.applyValue(50, fap48);
            bapframe.applyValue(48, fap50);

            bapframes.add(bapframe);

        }
        sendBAPFrames(requestId, bapframes);
    }

    @Override
    public void performAUAPFrame(AUAPFrame auapAnimation, ID requestId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void performAUAPFrames(List<AUAPFrame> auapAnimation, ID requestId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onCharacterChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
