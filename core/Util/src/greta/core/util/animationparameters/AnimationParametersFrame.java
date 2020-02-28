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
package greta.core.util.animationparameters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Ken Prepin
 */
public abstract class AnimationParametersFrame<AP extends AnimationParameter> implements Iterable<AP>, Comparable<AnimationParametersFrame> {

    public ArrayList<AP> APVector;
    private int frameNumber;

    public AnimationParametersFrame(int numAPs) {
        this(numAPs, 0);
    }

    public AnimationParametersFrame(AnimationParametersFrame<AP> apFrame) {
        this(apFrame.size(), apFrame.getFrameNumber());
        for (int i = 0; i < apFrame.size(); ++i) {
            setAnimationParameter(i, copyAnimationParameter(apFrame.APVector.get(i)));
        }
    }
    public AnimationParametersFrame(int numAPs, int frameNum) {
        APVector = new ArrayList<>(numAPs);
        this.frameNumber = frameNum;
        init(numAPs);
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(int num) {
        frameNumber = num;
    }

    public void setAnimationParameter(int number, int value) {
        setAnimationParameter(number, newAnimationParameter(true, value));
    }

    public void setAnimationParameter(int number, int value, boolean mask) {
        setAnimationParameter(number, newAnimationParameter(mask, value));
    }

    public void setAnimationParameter(int number, AP ap) {
        APVector.set(number, ap);
    }

    public int size() {
        return APVector.size();
    }

    public List<AP> getAnimationParametersList() {
        return APVector;
    }

    public AP getAnimationParameter(int index){
        return APVector.get(index);
    }

    public boolean getMask(int index){
        return APVector.get(index).getMask();
    }
    public int getValue(int index){
        return APVector.get(index).getValue();
    }

    public void setMask(int index, boolean mask){
        APVector.get(index).setMask(mask);
    }
    public void setValue(int index, int value){
        APVector.get(index).setValue(value);
    }
    public void applyValue(int index, int value){
        APVector.get(index).applyValue(value);
    }
    public void setMaskAndValue(int index, boolean mask, int value){
        APVector.get(index).setMask(mask);
        APVector.get(index).setValue(value);
    }

    @Override
    public String toString() {
        return AnimationParametersFrame2String();
    }


    public String AnimationParametersFrame2String() {
        return AnimationParametersFrame2String(frameNumber);
    }

    public String AnimationParametersFrame2String(int frameNum) {
        String buffer = "";
        String mask = "";
        buffer = frameNum + " ";

        for (int i = 1; i < APVector.size(); ++i) {
            AP ap = APVector.get(i);
            if (ap.getMask()) {
                mask += "1 ";
                buffer += ap.getValue() + " ";
            } else {
                mask += "0 ";
            }
        }
        String apbuffer = mask + "\n" + buffer + "\n";
        return apbuffer;
    }

    public static String AnimParamFramesList2String(List<AnimationParametersFrame> frames) {
        String apList = "";
        int first = frames.get(0).getFrameNumber();
        for (AnimationParametersFrame apframe : frames) {

            //fapframe.setFrameNumber(fapframe.getFrameNumber() - first);
            apList += apframe.AnimationParametersFrame2String(apframe.getFrameNumber() - first);
        }
        return apList;
    }


    public void readFromString(String twoLines){
        int posLn = twoLines.indexOf('\n');
        if(posLn >-1){
            readFromString(twoLines.substring(0, posLn), twoLines.substring(posLn+1));
        }
    }

    public void readFromString(String masksLine, String valuesLine){
        StringTokenizer masks = new StringTokenizer(masksLine);
        StringTokenizer values = new StringTokenizer(valuesLine);

        setFrameNumber(Integer.parseInt(values.nextToken()));

        int apCount = 1;

        while (masks.hasMoreTokens()) {
            boolean mask = Integer.parseInt(masks.nextToken()) == 1;
            AP ap = getAnimationParameter(apCount);
            ap.setMask(mask);
            if(mask) {
                ap.setValue(Integer.parseInt(values.nextToken()));
            }
            apCount++;
        }
    }

    protected abstract AP newAnimationParameter();

    public abstract AP newAnimationParameter(boolean mask, int value);

    protected abstract AP copyAnimationParameter(AP ap);

    public void init(int size) {
        for (int i = 0; i < size; i++) {
            AP ap = newAnimationParameter();
            APVector.add(ap);
            ap.setMask(false);
        }
    }

   /**
     * CAUTION!
     * Here, the default Oject.clone() function is overwritten as a copy constructor!
     * @return a copy of {@code this}
     */
    @Override
    public abstract AnimationParametersFrame clone();


    @Override
    public Iterator<AP> iterator() {
        return APVector.iterator();
    }

    /**
     * Compares the frame number.<br/>
     * It return: <br/>
     * &nbsp;- a negative value if this frame is before the given frame<br/>
     * &nbsp;- {@code 0} if they have the same time<br/>
     * &nbsp;- a positive value if this frame is after the given frame<br/>
     * Note: this class has a natural ordering that is inconsistent with equals.
     *
     * @param apFrame the frame to compare
     * @return the frame number difference
     */
    @Override
    public int compareTo(AnimationParametersFrame apFrame) {
        return this.getFrameNumber() - apFrame.getFrameNumber();
    }
}
