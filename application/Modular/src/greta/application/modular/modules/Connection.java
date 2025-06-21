/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
