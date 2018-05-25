/*
 * This file is a part of the Modular application.
 */

package vib.application.modular.modules;

import com.mxgraph.model.mxCell;

/**
 *
 * @author Andre-Marie Pez
 */
public class Connection {
    private Connector connector;
    private Module in;
    private Module out;
    private boolean connected = false;
    private mxCell cell;

    public Connection(mxCell cell, Connector connector, Module in, Module out) {
        this.cell = cell;
        this.connector = connector;
        this.in = in;
        this.out = out;
    }

    public void connect() {
        if (!connected) {
            connected = connector.connect(in.getObject(), out.getObject());
        }
    }

    public void disconnect() {
        if (connected) {
            connected = !connector.disconnect(in.getObject(), out.getObject());
        }
    }

    public boolean isConnected(){
        return connected;
    }

    public Module getIn(){
        return in;
    }

    public Module getOut(){
        return out;
    }

    public Connector getConnector(){
        return connector;
    }

    public mxCell getCell(){
        return cell;
    }

    public String getConnectionCode() {
        return connector.getConnectionCode(in, out);
    }
}
