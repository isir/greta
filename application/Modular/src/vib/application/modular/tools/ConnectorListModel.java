/*
 * This file is a part of the Modular application.
 */

package vib.application.modular.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;
import vib.application.modular.ModularXMLFile;
import vib.application.modular.tools.ConnectorListModel.ConnectorElement;
import vib.core.util.xml.XMLTree;

/**
 *
 * @author Andre-Marie Pez
 */
public class ConnectorListModel extends AbstractListModel<ConnectorElement>{


    ArrayList<ConnectorElement> connectors;

    public ConnectorListModel(){
        connectors = new ArrayList<ConnectorElement>();
        reload();
    }

    public void reload(){
        connectors.clear();
        List<XMLTree> connectorsXML = ModularXMLFile.getConnectors().getChildrenElement();
        connectors.ensureCapacity(connectorsXML.size());
        for(XMLTree connectorXML : connectorsXML){
            connectors.add(new ConnectorElement(connectorXML));
        }
        Collections.sort(connectors);
    }

    @Override
    public int getSize() {
        return connectors.size();
    }

    @Override
    public ConnectorElement getElementAt(int index) {
        return connectors.get(index);
    }

    public ConnectorElement createConnector(){
        XMLTree connectorXML = ModularXMLFile.getConnectors().createChild("connector");
        connectorXML.setAttribute("id", "");
        XMLTree inputXML = connectorXML.createChild("input");
        inputXML.setAttribute("class", "");
        inputXML.setAttribute("lib_id", "");
        XMLTree outputXML = connectorXML.createChild("output");
        outputXML.setAttribute("class", "");
        outputXML.setAttribute("lib_id", "");
        XMLTree connectXML = connectorXML.createChild("connect");
        connectXML.setAttribute("from", "null");
        connectXML.setAttribute("method", "");
        connectXML.setAttribute("to", "null");
        XMLTree disconnectXML = connectorXML.createChild("disconnect");
        disconnectXML.setAttribute("from", "null");
        disconnectXML.setAttribute("method", "");
        disconnectXML.setAttribute("to", "null");
        ConnectorElement connector = new ConnectorElement(connectorXML);
        connectors.add(connector);
        Collections.sort(connectors);
        int index = connectors.indexOf(connector);
        fireIntervalAdded(this, index, index);
        return connector;
    }

    public void deleteConnector(ConnectorElement connector){
        connector.connector.getParent().removeChild(connector.connector);
        int index = connectors.indexOf(connector);
        connectors.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    public void connectorNameChanged(ConnectorElement connector){
        Collections.sort(connectors);
    }


    public class ConnectorElement implements Comparable<ConnectorElement>{

        XMLTree connector;
        NameChanger nc;

        private ConnectorElement(XMLTree connector){
            this.connector = connector;
            updateNameChanger(false);
        }

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public int compareTo(ConnectorElement o) {
            return String.CASE_INSENSITIVE_ORDER.compare(getName(), o.getName());
        }

        public void setName(String name){
            nc.ApplyNewName(name);
            connectorNameChanged(this);
        }

        public String getName(){
            return connector.getAttribute("id");
        }

        public void setStyle(String style) {
            connector.setAttribute("style", style==null ||style.isEmpty() ? null : style);
        }

        public String getStyle() {
            return connector.getAttribute("style");
        }

        public boolean isUnique(){
            return connector.hasAttribute("unique") && connector.getAttribute("unique").equalsIgnoreCase("true");
        }

        public void setUnique(boolean unique){
            connector.setAttribute("unique", unique ? "true" : null);
        }

        public XMLTree getInput() {
            return connector.findNodeCalled("input");
        }

        public XMLTree getOutput() {
            return connector.findNodeCalled("output");
        }

        public XMLTree getConnect() {
            return connector.findNodeCalled("connect");
        }


        public XMLTree getDisconnect() {
            return connector.findNodeCalled("disconnect");
        }

        void updateNameChanger(boolean reference) {
            nc = new NameChanger(connector, "id", reference);
        }
    }
}
