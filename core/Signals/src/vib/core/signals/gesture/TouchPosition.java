/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.signals.gesture;

import vib.core.util.math.Vec3d;
import vib.core.util.parameter.Parameter;

/**
 * This class is used to describe a position for the wrist using reference points
 * The id of a TouchPosition object will be looked for in an xml file and a real position will then be retrieved
 * @author Brian Ravenet
 */
public class TouchPosition extends UniformPosition implements Parameter<TouchPosition> {
    /**
     * The Id of the touchpoint
     */
    private String id;

    /**
     * The reference of the bone
     */
    private String reference;

    /**
     * The position offset of the touchpoint from the bone
     */
    private Vec3d posOffset;

    /**
     * The rotation offset of the touchpoint from the bone
     */
    private Vec3d rotOffset;


    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }


    @Override
    public String getStringPosition() {
        String returnString = "       <touchPosition>" + id + "</touchPosition>\n";
        return returnString;
    }

    @Override
    public String getParamName() {
        return getId();
     }

    @Override
    public void setParamName(String string) {
        setId(string);
    }

    @Override
    public boolean equals(TouchPosition p) {
        return (this.getId().equals(p.getId()));
    }

    /**
     * @return the reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * @param reference the reference to set
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * @return the posOffset
     */
    public Vec3d getPosOffset() {
        return posOffset;
    }

    /**
     * @param posOffset the posOffset to set
     */
    public void setPosOffset(Vec3d posOffset) {
        this.posOffset = posOffset;
    }

    /**
     * @return the rotOffset
     */
    public Vec3d getRotOffset() {
        return rotOffset;
    }

    /**
     * @param rotOffset the rotOffset to set
     */
    public void setRotOffset(Vec3d rotOffset) {
        this.rotOffset = rotOffset;
    }

    @Override
    public void applySpacial(double spc) {
        //spacial parameter don't affect this kind of position
    }

    @Override
    public double getX() {
        return Math.random();//TODO here is just for testing, must return a correct value later
    }

    @Override
    public double getY() {
        return Math.random();//TODO here is just for testing, must return a correct value later
    }

    @Override
    public double getZ() {
        return Math.random();//TODO here is just for testing, must return a correct value later
    }

    @Override
    public Position getCopy() {
        TouchPosition pos = new TouchPosition();
        pos.id = id;
        pos.posOffset = posOffset==null ? null : new Vec3d(posOffset);
        pos.reference = reference;
        pos.rotOffset = rotOffset==null ? null : new Vec3d(rotOffset);
        return pos;
    }

}
