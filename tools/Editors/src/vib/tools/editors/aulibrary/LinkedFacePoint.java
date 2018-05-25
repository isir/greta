/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.aulibrary;

/**
 *
 * @author Andre-Marie Pez
 */
public class LinkedFacePoint extends FacePoint{

    private FapComponents linkedTo;
    private double percentOfMovementX;
    private double percentOfMovementY;
    private LinkedFacePoint(double originalX, double originalY){
        this(originalX, originalY, null);
    }

    public LinkedFacePoint(double originalX, double originalY, FapComponents linkedTo){
        this(originalX, originalY, linkedTo, 1);
    }

    public LinkedFacePoint(double originalX, double originalY, FapComponents linkedTo, double percentOfMovement){
        this(originalX, originalY, linkedTo, percentOfMovement, percentOfMovement);
    }

    public LinkedFacePoint(double originalX, double originalY, FapComponents linkedTo, double percentOfMovementX, double percentOfMovementY){
        super(originalX, originalY);
        this.linkedTo = linkedTo;
        this.percentOfMovementX = percentOfMovementX;
        this.percentOfMovementY = percentOfMovementY;
    }

    @Override
    public double getX(){
        this.x = (linkedTo.x - linkedTo.originalX)*percentOfMovementX + originalX;
        return super.getX();
    }

    @Override
    public double getY(){
        this.y = (linkedTo.y - linkedTo.originalY)*percentOfMovementY + originalY;
        return super.getY();
    }
}
