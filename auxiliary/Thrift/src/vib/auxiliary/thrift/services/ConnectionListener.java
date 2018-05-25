/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift.services;

/**
 *
 * @author Ken Prepin
 */
public interface ConnectionListener {

    public void onDisconnection();

    public void onConnection();
}
