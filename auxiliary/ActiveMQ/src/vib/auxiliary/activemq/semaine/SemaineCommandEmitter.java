/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.semaine;

/**
 *
 * @author Andre-Marie Pez
 */
public interface SemaineCommandEmitter {
    public void addSemaineCommandPerformer(SemaineCommandPerformer semaineCommandPerformer);
    public void removeSemaineCommandPerformer(SemaineCommandPerformer semaineCommandPerformer);
}
