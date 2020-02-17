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
package greta.tools.editors.aulibrary;

import greta.core.animation.mpeg4.fap.FAPType;
import greta.tools.editors.SliderAndText;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class Face {
    //signums for axis
    private static final int RIGHT = -1;
    private static final int LEFT = -RIGHT;
    private static final int UP = -1;
    private static final int DOWN = -UP;
    private static final int FORWARD = 1;
    private static final int BACKWARD = -FORWARD;

    private static final double ES = (0.66-0.31)/1024;
    private static final double ENS = (0.53-0.35)/1024;
    private static final double IRISD = (0.375-0.32)/1024;
    private static final double MNS = (0.64-0.53)/1024;
    private static final double MW = (0.63-0.35)/1024;
    private static final double AU = IRISD/64;// multiply by something

    FapComponents _2_1, _2_2, _2_3, _2_4, _2_5, _2_6, _2_7, _2_8, _2_9, _2_10; //jaw, inner lip and chin
    FapComponents _3_1, _3_2, _3_3, _3_4, _3_5, _3_6; // eyes
    FapComponents _4_1, _4_2, _4_3, _4_4, _4_5, _4_6; // eyebrows
    FapComponents _5_1, _5_2, _5_3, _5_4; // cheek
    FapComponents _6_1, _6_2, _6_3_6_4; // tongue
    FapComponents _7_1; // skull base
    FapComponents _8_1, _8_2, _8_3, _8_4, _8_5, _8_6, _8_7, _8_8; // outer lip
    FapComponents _9_1, _9_2, _9_3; // nose
    FapComponents _10_1, _10_2, _10_3, _10_4; // ears

    List<FapComponents> allCotrolPoints = new ArrayList<FapComponents>(45);
    SliderAndText[] fapMapping = new SliderAndText[FAPType.values().length];

    FaceShape outerMouth;
    FaceShape innerMouth;
    FaceShape leftEye;
    FaceShape rightEye;
    FaceShape brow;
    FaceShape leftEyebrow;
    FaceShape rightEyebrow;
    FaceShape nose;
    FaceShape nosetrils;
    FaceShape head;
    FaceShape neck;

    public Face(FacePanel facePanel) {
        _2_1 = new FapComponents(facePanel, "2.1", "jaw", 0.5, 0.83);
        _2_1.setVerticalMovement(FAPType.open_jaw, 0, 1080, DOWN, MNS);
        _2_1.setDepthMovement(FAPType.thrust_jaw, 0, 600, FORWARD, MNS);
        _2_1.setHorizontalMovement(FAPType.shift_jaw, -1080, 1080, RIGHT, MW);
        _2_2 = new FapComponents(facePanel, "2.2", "middle of inner upper lip", 0.5, 0.64);
        _2_2.setVerticalMovement(FAPType.lower_t_midlip, -600, 600, DOWN, MNS);
        _2_2.setDepthMovement(FAPType.push_t_lip, -1080, 1080, FORWARD, MNS);
        _2_3 = new FapComponents(facePanel, "2.3", "middle of inner lower lip", 0.5, 0.655);
        _2_3.setVerticalMovement(FAPType.raise_b_midlip, -1860, 1860, UP, MNS);
        _2_3.setDepthMovement(FAPType.push_b_lip, -1080, 1080, FORWARD, MNS);
        _2_4 = new FapComponents(facePanel, "2.4", "left corner of inner lip", 0.61, 0.64);
        _2_4.setHorizontalMovement(FAPType.stretch_l_cornerlip, -600, 600, LEFT, MW);
        _2_4.setVerticalMovement(FAPType.raise_l_cornerlip, -600, 600, UP, MNS);
        _2_5 = new FapComponents(facePanel, "2.5", "right corner of inner lip", 0.39, 0.64);
        _2_5.setHorizontalMovement(FAPType.stretch_r_cornerlip, -600, 600, RIGHT, MW);
        _2_5.setVerticalMovement(FAPType.raise_r_cornerlip, -600, 600, UP, MNS);
        _2_6 = new FapComponents(facePanel, "2.6", "left of inner upper lip", 0.56, 0.63);
        _2_6.setVerticalMovement(FAPType.lower_t_lip_lm, -600, 600, DOWN, MNS);
        _2_7 = new FapComponents(facePanel, "2.7", "right of inner upper lip", 0.44, 0.63);
        _2_7.setVerticalMovement(FAPType.lower_t_lip_rm, -600, 600, DOWN, MNS);
        _2_8 = new FapComponents(facePanel, "2.8", "left of inner lower lip", 0.555, 0.65);
        _2_8.setVerticalMovement(FAPType.raise_b_lip_lm, -1860, 1860, UP, MNS);
        _2_9 = new FapComponents(facePanel, "2.9", "right of inner lower lip", 0.445, 0.65);
        _2_9.setVerticalMovement(FAPType.raise_b_lip_rm, -1860, 1860, UP, MNS);
        _2_10 = new FapComponents(facePanel, "2.10", "chin", 0.5, 0.75);
        _2_10.setVerticalMovement(FAPType.depress_chin, -420, 420, UP, MNS);

        _3_1 = new FapComponents(facePanel, "3.1", "left upper eyelid", 0.69, 0.32);
        _3_1.setVerticalMovement(FAPType.close_t_l_eyelid, -1080, 1080, DOWN, IRISD);
        _3_2 = new FapComponents(facePanel, "3.2", "right upper eyelid", 0.31, 0.32);
        _3_2.setVerticalMovement(FAPType.close_t_r_eyelid, -1080, 1080, DOWN, IRISD);
        _3_3 = new FapComponents(facePanel, "3.3", "left lower eyelid", 0.69, 0.375);
        _3_3.setVerticalMovement(FAPType.close_b_l_eyelid, -600, 600, UP, IRISD);
        _3_4 = new FapComponents(facePanel, "3.4", "right lower eyelid", 0.31, 0.375);
        _3_4.setVerticalMovement(FAPType.close_b_r_eyelid, -600, 600, UP, IRISD);
        _3_5 = new FapComponents(facePanel, "3.5", "left eye", 0.69, 0.35);
        _3_5.setHorizontalMovement(FAPType.yaw_l_eyeball, -153600, 153600, LEFT, AU);
        _3_5.setVerticalMovement(FAPType.pitch_l_eyeball, -115200, 115200, DOWN, AU);
        _3_5.setDepthMovement(FAPType.thrust_l_eyeball, -600, 600, FORWARD, ES);
        _3_6 = new FapComponents(facePanel, "3.6", "right eye", 0.31, 0.35);
        _3_6.setHorizontalMovement(FAPType.yaw_r_eyeball, -153600, 153600, LEFT, AU);
        _3_6.setVerticalMovement(FAPType.pitch_r_eyeball, -115200, 115200, DOWN, AU);
        _3_6.setDepthMovement(FAPType.thrust_r_eyeball, -600, 600, FORWARD, ES);

        _4_1 = new FapComponents(facePanel, "4.1", "inner of left eyebrow", 0.59, 0.28);
        _4_1.setVerticalMovement(FAPType.raise_l_i_eyebrow, -900, 900, UP, ENS);
        _4_1.setHorizontalMovement(FAPType.squeeze_l_eyebrow, -900, 900, RIGHT, ES);
        _4_2 = new FapComponents(facePanel, "4.2", "inner of right eyebrow", 0.41, 0.28);
        _4_2.setVerticalMovement(FAPType.raise_r_i_eyebrow, -900, 900, UP, ENS);
        _4_2.setHorizontalMovement(FAPType.squeeze_r_eyebrow, -900, 900, LEFT, ES);
        _4_3 = new FapComponents(facePanel, "4.3", "middle of left eyebrow", 0.71, 0.25);
        _4_3.setVerticalMovement(FAPType.raise_l_m_eyebrow, -900, 900, UP, ENS);
        _4_4 = new FapComponents(facePanel, "4.4", "middle of right eyebrow", 0.29, 0.25);
        _4_4.setVerticalMovement(FAPType.raise_r_m_eyebrow, -900, 900, UP, ENS);
        _4_5 = new FapComponents(facePanel, "4.5", "outer of left eyebrow", 0.85, 0.275);
        _4_5.setVerticalMovement(FAPType.raise_l_o_eyebrow, -900, 900, UP, ENS);
        _4_6 = new FapComponents(facePanel, "4.6", "outer of right eyebrow", 0.15, 0.275);
        _4_6.setVerticalMovement(FAPType.raise_r_o_eyebrow, -900, 900, UP, ENS);

        _5_1 = new FapComponents(facePanel, "5.1", "left cheek", 0.75, 0.625);
        _5_1.setHorizontalMovement(FAPType.puff_l_cheek, -900, 900, LEFT, ES);
        _5_2 = new FapComponents(facePanel, "5.2", "right cheek", 0.25, 0.625);
        _5_2.setHorizontalMovement(FAPType.puff_r_cheek, -900, 900, RIGHT, ES);
        _5_3 = new FapComponents(facePanel, "5.3", "left cheekbone", 0.74, 0.47);
        _5_3.setVerticalMovement(FAPType.lift_l_cheek, -900, 900, UP, ENS);
        _5_4 = new FapComponents(facePanel, "5.4", "right cheekbone", 0.26, 0.47);
        _5_4.setVerticalMovement(FAPType.lift_r_cheek, -900, 900, UP, ENS);

        //don't trust the tongue... it is not normalized
        //here it is the good repartition
        _6_1 = new FapComponents(facePanel, "6.1", "tongue tip", -1, -1);
        _6_1.setHorizontalMovement(FAPType.shift_tongue_tip, -1080, 1080, RIGHT, MW);
        _6_1.setVerticalMovement(FAPType.raise_tongue_tip, -1080, 1080, UP, MNS);
        _6_1.setDepthMovement(FAPType.thrust_tongue_tip, -1080, 1080, FORWARD, MW);
        _6_2 = new FapComponents(facePanel, "6.2", "tongue center", -1, -1);
        _6_2.setVerticalMovement(FAPType.raise_tongue, -1080, 1080, UP, MNS);
        _6_3_6_4 = new FapComponents(facePanel, "6.3, 6.4", "sides of the tongue", -1, -1);
        _6_3_6_4.setVerticalMovement(FAPType.tongue_roll, -300, 300, UP, AU);

        _7_1 = new FapComponents(facePanel, "7.1", "skull base", 2, 2);
        _7_1.setHorizontalMovement(FAPType.head_pitch, -1860, 1860, LEFT, AU);//we don't care about signum
        _7_1.setVerticalMovement(FAPType.head_yaw, -1860, 1860, DOWN, AU);//we don't care about signum
        _7_1.setDepthMovement(FAPType.head_roll, -1860, 1860, FORWARD, AU);//we don't care about signum

        _8_1 = new FapComponents(facePanel, "8.1", "middle of outer upper lip", 0.5, 0.62);
        _8_1.setVerticalMovement(FAPType.lower_t_midlip_o, -600, 600, DOWN, MNS);
        _8_2 = new FapComponents(facePanel, "8.2", "middle of outer lower lip", 0.5, 0.68);
        _8_2.setVerticalMovement(FAPType.raise_b_midlip_o, -1860, 1860, UP, MNS);
        _8_3 = new FapComponents(facePanel, "8.3", "left corner of outer lip", 0.65, 0.64);
        _8_3.setHorizontalMovement(FAPType.stretch_l_cornerlip_o, -600, 600, LEFT, MW);
        _8_3.setVerticalMovement(FAPType.raise_l_cornerlip_o, -600, 600, UP, MNS);
        _8_4 = new FapComponents(facePanel, "8.4", "right corner of outer lip", 0.35, 0.64);
        _8_4.setHorizontalMovement(FAPType.stretch_r_cornerlip_o, -600, 600, RIGHT, MW);
        _8_4.setVerticalMovement(FAPType.raise_r_cornerlip_o, -600, 600, UP, MNS);
        _8_5 = new FapComponents(facePanel, "8.5", "left of outer upper lip", 0.555, 0.605);
        _8_5.setVerticalMovement(FAPType.lower_t_lip_lm_o, -600, 600, DOWN, MNS);
        _8_6 = new FapComponents(facePanel, "8.6", "right of outer upper lip", 0.445, 0.605);
        _8_6.setVerticalMovement(FAPType.lower_t_lip_rm_o, -600, 600, DOWN, MNS);
        _8_7 = new FapComponents(facePanel, "8.7", "left of outer lower lip", 0.55, 0.67);
        _8_7.setVerticalMovement(FAPType.raise_b_lip_lm_o, -1860, 1860, UP, MNS);
        _8_8 = new FapComponents(facePanel, "8.8", "right of outer lower lip", 0.45, 0.67);
        _8_8.setVerticalMovement(FAPType.raise_b_lip_rm_o, -1860, 1860, UP, MNS);

        _9_1 = new FapComponents(facePanel, "9.1", "nostril left", 0.6, 0.54);
        _9_1.setHorizontalMovement(FAPType.stretch_l_nose, -540, 540, LEFT, ENS);
        _9_2 = new FapComponents(facePanel, "9.2", "nostril right", 0.4, 0.54);
        _9_2.setHorizontalMovement(FAPType.stretch_r_nose, -540, 540, RIGHT, ENS);
        _9_3 = new FapComponents(facePanel, "9.3", "nose tip", 0.5, 0.53);
        _9_3.setVerticalMovement(FAPType.raise_nose, -680, 680, UP, ENS);
        _9_3.setHorizontalMovement(FAPType.bend_nose, -900, 900, RIGHT, ENS);

        _10_1 = new FapComponents(facePanel, "10.1", "upper left ear", 0.965, 0.325);
        _10_1.setVerticalMovement(FAPType.raise_l_ear, -900, 900, UP, ENS);
        _10_2 = new FapComponents(facePanel, "10.2", "upper right ear", 0.035, 0.325);
        _10_2.setVerticalMovement(FAPType.raise_r_ear, -900, 900, UP, ENS);
        _10_3 = new FapComponents(facePanel, "10.1", "upper left ear", 0.95, 0.495);
        _10_3.setHorizontalMovement(FAPType.pull_l_ear, -900, 900, LEFT, ENS);
        _10_4 = new FapComponents(facePanel, "10.2", "upper right ear", 0.05, 0.495);
        _10_4.setHorizontalMovement(FAPType.pull_r_ear, -900, 900, RIGHT, ENS);

        allCotrolPoints.add(_2_1);allCotrolPoints.add(_2_2);allCotrolPoints.add(_2_3);allCotrolPoints.add(_2_4);allCotrolPoints.add(_2_5);allCotrolPoints.add(_2_6);allCotrolPoints.add(_2_7);allCotrolPoints.add(_2_8);allCotrolPoints.add(_2_9);allCotrolPoints.add(_2_10);
        allCotrolPoints.add(_3_1);allCotrolPoints.add(_3_2);allCotrolPoints.add(_3_3);allCotrolPoints.add(_3_4);allCotrolPoints.add(_3_5);allCotrolPoints.add(_3_6);
        allCotrolPoints.add(_4_1);allCotrolPoints.add(_4_2);allCotrolPoints.add(_4_3);allCotrolPoints.add(_4_4);allCotrolPoints.add(_4_5);allCotrolPoints.add(_4_6);
        allCotrolPoints.add(_5_1);allCotrolPoints.add(_5_2);allCotrolPoints.add(_5_3);allCotrolPoints.add(_5_4);
        allCotrolPoints.add(_6_1);allCotrolPoints.add(_6_2);allCotrolPoints.add(_6_3_6_4);
        allCotrolPoints.add(_7_1);
        allCotrolPoints.add(_8_1);allCotrolPoints.add(_8_2);allCotrolPoints.add(_8_3);allCotrolPoints.add(_8_4);allCotrolPoints.add(_8_5);allCotrolPoints.add(_8_6);allCotrolPoints.add(_8_7);allCotrolPoints.add(_8_8);
        allCotrolPoints.add(_9_1);allCotrolPoints.add(_9_2);allCotrolPoints.add(_9_3);
        allCotrolPoints.add(_10_1);allCotrolPoints.add(_10_2);allCotrolPoints.add(_10_3);allCotrolPoints.add(_10_4);

        for(FapComponents fapComp : allCotrolPoints){
            fapComp.buildPanel();
            if(fapComp.horizontal!=null){
                fapMapping[fapComp.horizontal.type.ordinal()] = fapComp.horizontal.value;
            }
            if(fapComp.vertical!=null){
                fapMapping[fapComp.vertical.type.ordinal()] = fapComp.vertical.value;
            }
            if(fapComp.depth!=null){
                fapMapping[fapComp.depth.type.ordinal()] = fapComp.depth.value;
            }
        }

        innerMouth = new FaceShape(_2_2, _2_6, _2_4, _2_8, _2_3, _2_9, _2_5, _2_7,_2_2);
        outerMouth = new FaceShape(_8_1, _8_5, _8_3, _8_7, _8_2, _8_8, _8_4, _8_6,_8_1);
        leftEye = new FaceShape(
                _3_1,
                new LinkedFacePoint(0.74, 0.33, _3_1, 0.7),
                new FacePoint(0.78, 0.35),
                new LinkedFacePoint(0.74, 0.365, _3_3, 0.7),
                _3_3,
                new LinkedFacePoint(0.64, 0.365, _3_3, 0.7),
                new FacePoint(0.6, 0.35),
                new LinkedFacePoint(0.64, 0.33, _3_1, 0.7),
                _3_1);
        rightEye = new FaceShape(
                _3_2,
                new LinkedFacePoint(0.26, 0.33, _3_2, 0.7),
                new FacePoint(0.22, 0.35),
                new LinkedFacePoint(0.26, 0.365, _3_4, 0.7),
                _3_4,
                new LinkedFacePoint(0.36, 0.365, _3_4, 0.7),
                new FacePoint(0.4, 0.35),
                new LinkedFacePoint(0.36, 0.33, _3_2, 0.7),
                _3_2);
        FacePoint firstLEB = new LinkedFacePoint(0.59, 0.29, _4_1);
        leftEyebrow = new FaceShape(firstLEB, new LinkedFacePoint(0.59, 0.27, _4_1), new LinkedFacePoint(0.71, 0.24, _4_3), new LinkedFacePoint(0.85, 0.265, _4_5), new LinkedFacePoint(0.85, 0.285, _4_5), new LinkedFacePoint(0.71, 0.26, _4_3), firstLEB);
        FacePoint firstREB = new LinkedFacePoint(0.41, 0.29, _4_2);
        rightEyebrow = new FaceShape(firstREB, new LinkedFacePoint(0.41, 0.27, _4_2), new LinkedFacePoint(0.29, 0.24, _4_4), new LinkedFacePoint(0.15, 0.265, _4_6), new LinkedFacePoint(0.15, 0.285, _4_6), new LinkedFacePoint(0.29, 0.26, _4_4), firstREB);
        nosetrils = new FaceShape(new FacePoint(0.41,0.5), _9_2, new FacePoint( 0.47, 0.55));
        nosetrils.appendSegment(new FacePoint( 0.53, 0.55), _9_1,new FacePoint(0.59,0.5));
        FacePoint midnose = new LinkedFacePoint(0.5, 0.47, _9_3, 0.7);
        nose = new FaceShape( _9_1, _9_3,_9_2);
        nose.appendSegment( _9_1, _9_3,midnose);
        nose.appendSegment( midnose, _9_3,_9_2);


        FacePoint h4 = new FacePoint(0.3, -0.135);
        FacePoint h6 = new FacePoint(0.7, -0.135);

        brow = new FaceShape(h4,h6, _4_5, _4_3, _4_1, _4_2, _4_4, _4_6);
        head = new FaceShape();
        head.appendSegment(
                new FacePoint(0.15, 0.6),
                new FacePoint(0.12, 0.59),
                _10_4,
                _10_2,
                new FacePoint(0.085, 0.35));
        head.appendSegment(
                new FacePoint(0.1, 0.43),
                new FacePoint(0.085, 0.35),
                new FacePoint(0.08, 0.27),
                new FacePoint(0.084, 0.15),
                new FacePoint(0.14, 0),
                h4,
                new FacePoint(0.5, -0.163),
                h6,
                new FacePoint(0.86, 0),
                new FacePoint(0.916, 0.15),
                new FacePoint(0.92, 0.27),
                new FacePoint(0.915, 0.35),
                new FacePoint(0.9, 0.43));
        head.appendSegment(
                new FacePoint(0.915, 0.35),
                _10_1,
                _10_3,
                new FacePoint(0.88, 0.59),
                new FacePoint(0.85, 0.6));
        head.appendSegment(
                new FacePoint(0.87, 0.53),
                new FacePoint(0.85, 0.6),
                new LinkedFacePoint(0.8, 0.68, _2_1, 0.3),
                new LinkedFacePoint(0.67, 0.78, _2_1, 0.9),
                new LinkedFacePoint(0.6, 0.82, _2_1),
                _2_1,
                new LinkedFacePoint(0.4, 0.82, _2_1),
                new LinkedFacePoint(0.33, 0.78, _2_1,0.9),
                new LinkedFacePoint(0.2, 0.68, _2_1, 0.3),
                new FacePoint(0.15, 0.6),
                new FacePoint(0.13, 0.53));

        neck = new FaceShape(
                new FacePoint(0.085, 0.35),
                _10_2,
                _10_4,
                new FacePoint(0.12, 0.59),
                new FacePoint(0.15, 0.6),
                new LinkedFacePoint(0.2, 0.68, _2_1, 0.1),
                new LinkedFacePoint(0.24, 0.77, _2_1, 0.05),
                new FacePoint(0.23, 1),
                new FacePoint(0.2, 1.05),
                new FacePoint(-0.2, 1.1),

                new FacePoint(1.2, 1.1),
                new FacePoint(0.8, 1.05),
                new FacePoint(0.77, 1),
                new LinkedFacePoint(0.76, 0.77, _2_1, 0.05),
                new LinkedFacePoint(0.8, 0.68, _2_1, 0.1),
                new FacePoint(0.85, 0.6),
                new FacePoint(0.88, 0.59),
                _10_3,
                _10_1,
                new FacePoint(0.915, 0.35)
                );
    }
}
