/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.aria;

/**
 *
 * @author Angelo Cafaro
 */
public interface ARIAInformationStatePerformer {
    
    public void performStateChange(String state);
    public void performLanguageChange(String language);
    
}