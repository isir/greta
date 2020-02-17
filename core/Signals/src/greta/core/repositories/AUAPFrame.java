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
package greta.core.repositories;

import greta.core.util.animationparameters.AnimationParametersFrame;
import greta.core.util.enums.Side;
import greta.core.util.log.Logs;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public class AUAPFrame extends AnimationParametersFrame<AUAP> {

    public static final int NUM_OF_AUS = 64;

    /**
     * AUAP for left and right
     */
    public AUAPFrame() {
        super(NUM_OF_AUS * 2);
    }

    public AUAPFrame(int frameNum) {
        super(NUM_OF_AUS * 2, frameNum);
    }

    public AUAPFrame(AUAPFrame auapFrame) {
        super(auapFrame);
    }

    @Override
    public AUAPFrame clone() {
        return new AUAPFrame(this);
    }

    private int getRightIndex(int which){
        return (which - 1) * 2;
    }

    private int getLeftIndex(int which){
        return (which - 1) * 2 + 1;
    }

    /**
     *
     * @param which num of AU (start from 1 -> (index-1)*2=right side
     * (index-1)*2+1=left side
     * @param value double
     * @param side
     */
    public void setAUAP(int which, double value, Side side) {
        switch (side) {
            case RIGHT:
                setAnimationParameter(getRightIndex(which), (int) (value * AUAP.FLOATING_FACTOR));
                break;
            case LEFT:
                setAnimationParameter(getLeftIndex(which), (int) (value * AUAP.FLOATING_FACTOR));
                break;
            default:
                setAnimationParameter(getRightIndex(which), (int) (value * AUAP.FLOATING_FACTOR));
                setAnimationParameter(getLeftIndex(which), (int) (value * AUAP.FLOATING_FACTOR));
                break;
        }
    }

    public void setAUAP(int which, double value) {// both sides
        setAUAP(which, value, Side.BOTH);
    }

    public void setAUAPboth(int which, double value) {// both sides
        setAUAP(which, value, Side.BOTH);
    }

    public void setAUAPright(int which, double value) {// right side
        setAUAP(which, value, Side.RIGHT);
    }

    public void setAUAPleft(int which, double value) {// left side
        setAUAP(which, value, Side.LEFT);
    }

    /**
     *
     * @param which num of AU (start from 1 -> (index-1)*2=right side
     * (index-1)*2+1=left side
     * @param auap
     * @param side
     */
    public void setAUAP(int which, AUAP auap, Side side) {
        switch (side) {
            case RIGHT:
                setAnimationParameter(getRightIndex(which), auap);
                break;
            case LEFT:
                setAnimationParameter(getLeftIndex(which), auap);
                break;
            default:
                setAnimationParameter(getRightIndex(which), auap);
                setAnimationParameter(getLeftIndex(which), new AUAP(auap));
                break;
        }
    }

    public void setAUAP(int which, AUAP auap) {
        setAUAP(which, auap, Side.BOTH);
    }

    public void setAUAPboth(int which, AUAP auap) {
        setAUAP(which, auap, Side.BOTH);
    }

    public void setAUAPright(int which, AUAP auap) {
        setAUAP(which, auap, Side.RIGHT);
    }

    public void setAUAPleft(int which, AUAP auap) {
        setAUAP(which, auap, Side.LEFT);
    }

    public List<AUAP> getAUAPList() {
        return getAnimationParametersList();
    }

    /**
     *
     * @param which num of AU (start from 1 -> (index-1)*2=right side
     * (index-1)*2+1=left side
     */
    public AUAP getAUAP(int which, Side side) {
        AUAP auap;
        switch (side) {
            case RIGHT:
                auap = getAUAPright(which);
                break;
            case LEFT:
                auap = getAUAPleft(which);
                break;
            default:
                auap = getAUAPright(which);
                Logs.warning("get AU animation parameter ask for both sides of face: value of right AU returned");
                break;
        }
        return auap;
    }

    /**
     *
     * @param which num of AU (start from 1 -> (index-1)*2=right side
     * (index-1)*2+1=left side
     */
    public AUAP getAUAPright(int which) {
        return getAnimationParameter(getRightIndex(which));
    }

    public int getAUAPrightValue(int which){
        return getValue(getRightIndex(which));
    }

    public boolean getAUAPrightMask(int which){
        return getMask(getRightIndex(which));
    }

    /**
     *
     * @param which num of AU (start from 1 -> (index-1)*2=right side
     * (index-1)*2+1=left side
     */
    public AUAP getAUAPleft(int which) {
        return getAnimationParameter(getLeftIndex(which));
    }

    public int getAUAPleftValue(int which){
        return getValue(getLeftIndex(which));
    }

    public boolean getAUAPleftMask(int which){
        return getMask(getLeftIndex(which));
    }

    public void setAnimationParameter(int number, double value) {
        setAnimationParameter(number, newAnimationParameter(true, (int) (value * AUAP.FLOATING_FACTOR)));
    }

    public boolean useActionUnit(int which){
        return getMask(getRightIndex(which)) || getMask(getLeftIndex(which));
    }

    @Override
    protected AUAP newAnimationParameter() {
        return new AUAP();
    }

    @Override
    public AUAP newAnimationParameter(boolean mask, int value) {
        return new AUAP(mask, value);
    }

    @Override
    protected AUAP copyAnimationParameter(AUAP ap) {
        return new AUAP(ap);
    }
}
