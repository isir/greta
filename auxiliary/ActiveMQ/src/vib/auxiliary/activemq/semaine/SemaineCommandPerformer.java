/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.semaine;

/**
 *
 * @author Andre-Marie Pez
 */
public interface SemaineCommandPerformer {
    public void performDataInfo(boolean hasAudio, boolean hasFAP, boolean hasBAP, String requestID);
    public void performPlayCommand(long startAt, long lifeTime, double priority, String requestID);
}
