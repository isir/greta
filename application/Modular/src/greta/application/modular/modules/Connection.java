/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.application.modular.modules;

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
