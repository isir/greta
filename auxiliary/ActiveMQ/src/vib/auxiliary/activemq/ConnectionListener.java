/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq;

/**
 *
 * @author Andre-Marie Pez
 */
public interface ConnectionListener {
    public void onDisconnection();
    public void onConnection();
}
