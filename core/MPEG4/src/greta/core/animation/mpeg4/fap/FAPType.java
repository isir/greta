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
package greta.core.animation.mpeg4.fap;

import greta.core.util.enums.Side;

/**
 *
 * @author Andre-Marie Pez
 */
public enum FAPType {

    null_fap, //0
    viseme,
    expression,
    open_jaw(Side.BOTH),
    lower_t_midlip(Side.BOTH),
    raise_b_midlip(Side.BOTH),
    stretch_l_cornerlip(Side.LEFT),
    stretch_r_cornerlip(Side.RIGHT),
    lower_t_lip_lm(Side.LEFT),
    lower_t_lip_rm(Side.RIGHT),
    raise_b_lip_lm(Side.LEFT), //10
    raise_b_lip_rm(Side.RIGHT),
    raise_l_cornerlip(Side.LEFT),
    raise_r_cornerlip(Side.RIGHT),
    thrust_jaw(Side.BOTH),
    shift_jaw(Side.BOTH),
    push_b_lip(Side.BOTH),
    push_t_lip(Side.BOTH),
    depress_chin(Side.BOTH),
    close_t_l_eyelid(Side.LEFT),
    close_t_r_eyelid(Side.RIGHT), //20
    close_b_l_eyelid(Side.LEFT),
    close_b_r_eyelid(Side.RIGHT),
    yaw_l_eyeball(Side.LEFT),
    yaw_r_eyeball(Side.RIGHT),
    pitch_l_eyeball(Side.LEFT),
    pitch_r_eyeball(Side.RIGHT),
    thrust_l_eyeball(Side.LEFT),
    thrust_r_eyeball(Side.RIGHT),
    dilate_l_pupil(Side.LEFT),
    dilate_r_pupil(Side.RIGHT), //30
    raise_l_i_eyebrow(Side.LEFT),
    raise_r_i_eyebrow(Side.RIGHT),
    raise_l_m_eyebrow(Side.LEFT),
    raise_r_m_eyebrow(Side.RIGHT),
    raise_l_o_eyebrow(Side.LEFT),
    raise_r_o_eyebrow(Side.RIGHT),
    squeeze_l_eyebrow(Side.LEFT),
    squeeze_r_eyebrow(Side.RIGHT),
    puff_l_cheek(Side.LEFT),
    puff_r_cheek(Side.RIGHT), //40
    lift_l_cheek(Side.LEFT),
    lift_r_cheek(Side.RIGHT),
    shift_tongue_tip(Side.BOTH),
    raise_tongue_tip(Side.BOTH),
    thrust_tongue_tip(Side.BOTH),
    raise_tongue(Side.BOTH),
    tongue_roll(Side.BOTH),
    head_pitch(Side.BOTH),
    head_yaw(Side.BOTH),
    head_roll(Side.BOTH), //50
    lower_t_midlip_o(Side.BOTH),
    raise_b_midlip_o(Side.BOTH),
    stretch_l_cornerlip_o(Side.LEFT),
    stretch_r_cornerlip_o(Side.RIGHT),
    lower_t_lip_lm_o(Side.LEFT),
    lower_t_lip_rm_o(Side.RIGHT),
    raise_b_lip_lm_o(Side.LEFT),
    raise_b_lip_rm_o(Side.RIGHT),
    raise_l_cornerlip_o(Side.LEFT),
    raise_r_cornerlip_o(Side.RIGHT), //60
    stretch_l_nose(Side.LEFT),
    stretch_r_nose(Side.RIGHT),
    raise_nose(Side.BOTH),
    bend_nose(Side.BOTH),
    raise_l_ear(Side.LEFT),
    raise_r_ear(Side.RIGHT),
    pull_l_ear(Side.LEFT),
    pull_r_ear(Side.RIGHT); //69


    public static final int NUMFAPS = 70; //counting the null_fap

    private final Side side;

    private FAPType(){
        this.side = null;
    }

    private FAPType(Side side){
        this.side = side;
    }

    public static FAPType get(String name){
        try {
            return valueOf(name);
        } catch (Throwable t) {
            return null_fap;
        }
    }

    public static FAPType get(int ordinal){
        return ordinal<0 || ordinal>69 ? null_fap : values()[ordinal];
    }

    public boolean isLeft(){
         return side == Side.LEFT || side == Side.BOTH;
    }

    public static boolean isLeft(int ordinal){
        return isLeft(get(ordinal));
    }

    private static boolean isLeft(FAPType fapType) {
        return fapType.isLeft();
    }

    public boolean isRight(){
         return side == Side.RIGHT || side == Side.BOTH;
    }

    public static boolean isRight(int ordinal){
        return isRight(get(ordinal));
    }

    private static boolean isRight(FAPType fapType) {
        return fapType.isRight();
    }

    public static boolean isCenter(int ordinal){
        return isCenter(get(ordinal));
    }

    private static boolean isCenter(FAPType fapType) {
        return fapType.isCenter();
    }

    public boolean isCenter(){
         return side == Side.BOTH;
    }
}
