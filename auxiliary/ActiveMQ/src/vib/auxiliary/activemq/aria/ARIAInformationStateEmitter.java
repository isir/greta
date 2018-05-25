/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.aria;

/**
 *
 * @author Angelo Cafaro
 */
public interface ARIAInformationStateEmitter {
    
    public void addARIAInformationStatePerformer(ARIAInformationStatePerformer ariaInformationStatePerformer);
    public void removeARIAInformationStatePerformer(ARIAInformationStatePerformer ariaInformationStatePerformer);
    
}