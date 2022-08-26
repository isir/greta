/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.multilayerattitudeplanner;

/**
 *
 * @author Mathieu
 */
public interface AttitudeEmitter {
       
    public void addAttitudePerformer(AttitudePerformer performer);

    public void removeAttitudePerformer(AttitudePerformer performer);
}
