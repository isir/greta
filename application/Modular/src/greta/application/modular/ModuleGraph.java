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
package greta.application.modular;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.handler.mxCellMarker;
import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.view.mxStylesheet;
import greta.application.modular.compilation.SourceFile;
import greta.application.modular.modules.Connection;
import greta.application.modular.modules.Connector;
import greta.application.modular.modules.Library;
import greta.application.modular.modules.Module;
import greta.application.modular.modules.ModuleFactory;
import greta.application.modular.modules.Style;
import greta.application.modular.tools.PrimitiveTypes;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.Tree;
import greta.core.util.log.LogPrinter;
import greta.core.util.log.Logs;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Andre-Marie Pez
 */
public class ModuleGraph extends com.mxgraph.swing.mxGraphComponent {

    private List<Connection> connections = new ArrayList<Connection>();
    private List<Module> modules = new ArrayList<Module>();
    private Tree<Module> moduleTree = new Tree<Module>();
    private JInternalFrame internalFrame = null;
    private Container internalFrameContent = null;
    private Container emptyPanel = new JPanel();
    private Module currentModuleDisplayed = null;
    protected JFrame parentFrame;
    private LinkedList<Module> garbage = new LinkedList<Module>();

    /**
     * Default contructor
     */
    public ModuleGraph() {
        super(new com.mxgraph.view.mxGraph());
        loadSylesInTheGraph();
        this.getGraph().setEdgeLabelsMovable(false);
        this.getGraph().setDropEnabled(false);
    }

    private Tree<Module> getModuleNode(Tree<Module> curNode, Module module){
        if(module.equals(curNode.getData()))
            return curNode;

        for(Tree<Module> child : curNode.getChildren()){
            Tree<Module> node = ModuleGraph.this.getModuleNode(child,module);
            if(node!=null)
                return node;
        }
        return null;
    }

    public Tree<Module> getModuleNode(Module module){
        return ModuleGraph.this.getModuleNode(moduleTree, module);
    }

    /**
     * Clear all the graph.<br/>
     * All module will be disconnected and deleted.
     */
    public void clearGraph() {
        for(int i=connections.size()-1; i>=0; --i){
            deleteConnection(connections.get(i));
        }
        for(int i=modules.size()-1; i>=0; --i){
            deleteModule(modules.get(i));
        }
        connections.clear();
        moduleTree.clear();
        updateInternalFrame(null);

        gc();
    }


    private void createXMLTreeElement(Tree<Module> currentNode, XMLTree currentXmlNode){
        Module module = currentNode.getData();
        XMLTree element = currentXmlNode.createChild("element");
        element.setAttribute("id", module.getId());
        element.setAttribute("module", module.getType());
        element.setAttribute("name", module.getCell().getValue().toString());
        element.setAttribute("x", "" + module.getCell().getGeometry().getX());
        element.setAttribute("y", "" + module.getCell().getGeometry().getY());
        element.setAttribute("w", "" + module.getCell().getGeometry().getWidth());
        element.setAttribute("h", "" + module.getCell().getGeometry().getHeight());
        Map<String, String> params = module.getParams();
        if (params != null && !params.isEmpty()) {
            for (Entry<String, String> param : params.entrySet()) {
                XMLTree parameter = element.createChild("parameter");
                parameter.setAttribute("name", param.getKey());
                parameter.setAttribute("value", param.getValue());
            }
        }
        JFrame jf = module.getFrame();
        if (jf != null) {
            XMLTree window = element.createChild("window");
            window.setAttribute("visible", "" + jf.isVisible());
            window.setAttribute("x", "" + jf.getLocation().x);
            window.setAttribute("y", "" + jf.getLocation().y);
            window.setAttribute("w", "" + jf.getWidth());
            window.setAttribute("h", "" + jf.getHeight());
        }


        List<Tree> children = currentNode.getChildren();
        if(children.size()>0){
            for(Tree node:children){
                createXMLTreeElement(node,element);
            }
        }
    }

    /**
     * Save the modules and their connections into an XML file.
     * @param modulesFile the path of the XML file.
     */
    public void saveGraph(String modulesFile) {
        XMLTree modulated = XML.createTree("modulated");

        XMLTree elements = modulated.createChild("elements");
        XMLTree connectionsXML = modulated.createChild("connections");

        for(Tree<Module> moduleNode :moduleTree.getChildren()){
            Module module = moduleNode.getData();
            if (module != null) {
                createXMLTreeElement(moduleNode,elements);
            }
        }

        for(Connection connection : connections){
            if (connection != null) {
                XMLTree connectionXML = connectionsXML.createChild("connection");
                connectionXML.setAttribute("source", connection.getIn().getId());
                connectionXML.setAttribute("target", connection.getOut().getId());
                connectionXML.setAttribute("connector", connection.getConnector().getId());
            }
        }

        modulated.save(modulesFile);

    }

    private void loadGraph(Tree<Module> moduleNode, XMLTree elements){
        Tree<Module> currentNode = moduleNode;
        Module parent = currentNode.getData();
        CharacterManager cm=null ;
        if(parent!=null && parent.getObject() instanceof CharacterManager)
            cm = (CharacterManager)parent.getObject();
        for (XMLTree element : elements.getChildrenElement()) {
            if (element.isNamed("element")) {
                Map<String, String> params = new HashMap<String, String>();
                for (XMLTree parameter : element.getChildrenElement()) {
                    if (parameter.isNamed("parameter")) {
                        params.put(parameter.getAttribute("name"), parameter.getAttribute("value"));
                    }
                }


                Module module = ModuleFactory.create(parentFrame, graph,
                        element.getAttribute("module"),
                        element.getAttribute("name"),
                        element.getAttribute("id"),
                        element.getAttributeNumber("x"),
                        element.getAttributeNumber("y"),
                        element.getAttributeNumber("w"),
                        element.getAttributeNumber("h"),
                        params, parent);
                if (module != null) {
                    if(parent!=null)
                        module.setParent(parent);
                    currentNode = moduleNode.addChild(module);
                    modules.add(module);
                    XMLTree window = element.findNodeCalled("window");
                    JFrame jf = module.getFrame();
                    if (window != null && jf != null) {
                        jf.setLocation((int) window.getAttributeNumber("x"), (int) window.getAttributeNumber("y"));
                        jf.setSize((int) window.getAttributeNumber("w"), (int) window.getAttributeNumber("h"));
                        jf.setTitle(element.getAttribute("name"));
                        jf.setVisible(Boolean.parseBoolean(window.getAttribute("visible")));
                    }
                }
                else
                    System.err.println(String.format("Could not create module : %s",element.getAttribute("module")));

                loadGraph(currentNode,element);
            }
        }
    }

    /**
     * Load the modules and their connections from an XML file.
     * @param modulesFile the path of the XML file.
     */
    public void loadGraph(String modulesFile) {
        this.graph.getModel().beginUpdate();
        LogPrinter lp = new LogPrinter();
        Logs.add(lp);
        XMLParser parser = XML.createParser();
        XMLTree modulated = parser.parseFileWithXSD(modulesFile, ModularXMLFile.MODULAR_XSD);
        Logs.remove(lp);
        XMLTree elements = modulated.findNodeCalled("elements");
        XMLTree connectionsXML = modulated.findNodeCalled("connections");

        loadGraph(moduleTree,elements);

        for (XMLTree connectionXML : connectionsXML.getChildrenElement()) {
            if (connectionXML.isNamed("connection")) {
                String sourceId = connectionXML.getAttribute("source");
                String targetId = connectionXML.getAttribute("target");
                Module sourceModule = findModuleByID(sourceId);
                Module targetModule = findModuleByID(targetId);
                if (sourceModule != null && targetModule != null) {
                    Connector connector;
                    mxCell edge = (mxCell) graph.insertEdge(null, null, null, sourceModule.getCell(), targetModule.getCell());
                    if (connectionXML.hasAttribute("connector")) {
                        connector = Connector.findConnector(connectionXML.getAttribute("connector"));
                        if (connector != null
                                && connector.isIn(sourceModule.getObject())
                                && connector.isOut(targetModule.getObject())) {
                            Connection connection = new Connection(edge, connector, sourceModule, targetModule);
                            connections.add(connection);
                            connection.connect();
                            Style.getMapper().checkConnectionStyle(graph, connection);
                        }
                    }
                }
            }
        }
        checkArrows();
        checkConnectables();
        this.graph.getModel().endUpdate();
    }


    private List<Connection> getConnectionsOfModule(Module m){
        List<Connection> connec = new ArrayList<Connection>();
        for(Connection c : connections){
            if(c.getIn()==m || c.getOut()==m){
                connec.add(c);
            }
        }
        return connec;
    }

    private void sortCellsAndModulesTree(Tree<Module> node){
        for(Tree<Module> m :node.getChildren()){
            for(Connection c : getConnectionsOfModule(m.getData())){
                graph.orderCells(false, new Object[]{c.getCell()});
            }
            graph.orderCells(false, new Object[]{m.getData().getCell()});
            sortCellsAndModulesTree(m);
        }
    }

    private void sortCellsAndModules(){

        Module selsected = findModuleByCell((mxCell)graph.getSelectionCell());
        if(selsected!=null){
            modules.remove(selsected);
            modules.add(selsected);
        }
        sortCellsAndModulesTree(moduleTree);
    }


    /**
     * Loads all known {@code Style} in the graph.
     */
    public final void loadSylesInTheGraph() {
        updateDefaultStyles();
        mxStylesheet stylesheet = graph.getStylesheet();
        for (Style s : Style.getAllStyles()) {
            stylesheet.putCellStyle(s.getName(), s.getMap());
        }
    }

    public void updateStyles() {
        updateDefaultStyles();
        updateModuleStyles();
        updateConectionStyle();
    }

    private void updateDefaultStyles(){
        mxStylesheet stylesheet = graph.getStylesheet();
        stylesheet.getDefaultVertexStyle().putAll(Style.getCurrentDefautlVertexMap());
        stylesheet.getDefaultEdgeStyle().putAll(Style.getCurrentDefaultEdgesMap());
        updateBackgroundForEdges(this.getBackground());
    }

    private void updateModuleStyles(){
        for(Module m : modules){
            Style.getMapper().setupModule(graph, m);
        }
    }

    private void updateConectionStyle(){
        this.getConnectionHandler().getMarker().setValidColor(Style.getMapper().getHighLightColor());
        for(Connection c : connections){
            Style.getMapper().checkConnectionStyle(graph, c, true);
        }
    }

    private void updateBackgroundForEdges(Color bg) {
        mxStylesheet stylesheet = graph.getStylesheet();
        stylesheet.getDefaultEdgeStyle().put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, Style.convertColor(bg));
        if(Style.getCurrentDefaultEdgesMap().containsKey(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR)){
            stylesheet.getDefaultEdgeStyle().put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, Style.getCurrentDefaultEdgesMap().get(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR));
        }
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if(graph!=null) {
//            graph.getModel().beginUpdate();
            updateBackgroundForEdges(bg);
            updateConectionStyle();// why it doesn't work immediately???
//            graph.getModel().endUpdate();
        }
    }

    /**
     * Instanciates and adds in the graph a new {@code Module}
     * @param moduleType the type of the {@code Module} to add.
     * @param parent the optionnal parent Module.
     */

    public void addModule(String moduleType, Module parent) {
        Module module = ModuleFactory.create(parentFrame, graph, moduleType, parent);
        if (module != null){
            if(parent!=null&&module.hasParent()){
                getModuleNode(parent).addChild(module);
            }else
                moduleTree.addChild(module);
            modules.add(module);
        }
        checkConnectables();
    }

    private void deleteModule(Module module) {
        if (currentModuleDisplayed == module) {
            updateInternalFrame(null);
        }
        deleteCell(module.getCell());

        modules.remove(module);
        if (module.getFrame() != null) {
            module.getFrame().dispose();
            module.getFrame().invalidate();
        }

        //clean up the CharacterManager
        if(module.getObject() instanceof CharacterDependent){
            module.getCharacterManager().remove((CharacterDependent)(module.getObject()));
        }
        if(module.getFrame() instanceof CharacterDependent){
            module.getCharacterManager().remove((CharacterDependent)(module.getFrame()));
        }

        moduleTree.removeChild(module);
        garbage.add(module);
    }

    private void gc(){
        while( ! garbage.isEmpty()){
            boolean finalized = false;
            Module module = garbage.poll();
            Class clazz = module.getObject().getClass();
            while(!finalized && clazz!=null){
                try {
                    Method method = clazz.getDeclaredMethod("finalize");
                    method.setAccessible(true);
                    method.invoke(module.getObject());
                    finalized = true;
                } catch (Exception ex) {}
                clazz = clazz.getSuperclass();
            }
        }
        System.gc();
    }

    private void deleteConnection(mxCell edge) {
        Connection connection = getConnectionOfArrow(edge);
        if(connection!=null) {
            deleteConnection(getConnectionOfArrow(edge));
        }
        else{
            deleteCell(edge);
        }
    }
    private void deleteConnection(Connection connection){
        if (connection != null) {
            connection.disconnect();
            connections.remove(connection);
            deleteCell(connection.getCell());
        }
    }

    private void deleteCell(mxCell cell) {
        graph.removeCells(new Object[]{cell}, false);
        graph.getModel().remove(cell);
    }


    /**
     * Gives an access to the internal frame of the {@code ModularWindow}
     * @param jif the internal frame of the {@code ModularWindow}
     */
    public void setInternalFrame(JInternalFrame jif) {
        this.internalFrame = jif;
        internalFrameContent = jif.getContentPane();
    }

    /**
     * Externalizes the frame of the current {@code Module} displayed.
     */
    public void extenalize() {
        if (currentModuleDisplayed == null) {
            return;
        }
        JFrame frame = currentModuleDisplayed.getFrame();
        if (frame == null || frame.isVisible()) {
            return;
        }
        frame.setTitle(internalFrame.getTitle());
        frame.setContentPane(internalFrame.getContentPane());
        frame.setJMenuBar(internalFrame.getJMenuBar());
        frame.setVisible(true);
        updateInternalFrame(null);
    }

    /**
     * Search the {@code Module} represented by a specific cell in the graph.
     * @param cell the specified graph cell
     * @return the corresponding {@code Module}
     */
    protected Module findModuleByCell(mxCell cell) {
        for (Module module : modules) {
            if (module.getCell() == cell) {
                return module;
            }
        }
        return null;
    }

    /**
     * Returns the number of {@code Module} in the graph that match to the given type.
     * @param type the type of {@code Module to count}
     * @return the number of {@code Module} in the graph that match to the given type
     */
    protected int countModuleByType(String type) {
        int count = 0;
        for (Module module : modules) {
            if (module.getType().equals(type)) {
                count++;
            }
        }
        return count;
    }

    /**
     *
     * @return the list of all {@code Modules} in this graph
     */
    protected List<Module> getModules() {
        return modules;
    }

    @Override
    protected void installDoubleClickHandler() {
        graphControl.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    if (!e.isConsumed() && isEditEvent(e)) {
                        Object cell = getCellAt(e.getX(), e.getY(), false);
                        Module module = findModuleByCell((mxCell) cell);
                        if (module != null) {
                            updateInternalFrame(module);
                        }
                        if (cell != null && getGraph().isCellEditable(cell)) {
                            startEditingAtCell(cell, e);
                        }
                    } else {
                        stopEditing(!invokesStopCellEditing);
                        Object cell = getCellAt(e.getX(), e.getY(), false);
                        Module module = findModuleByCell((mxCell) cell);
                        if (module != null) {
                            updateInternalFrame(module);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected mxConnectionHandler createConnectionHandler() {
        return new ConnectionHandler();
    }

    private class ConnectionHandler extends mxConnectionHandler implements mxEventSource.mxIEventListener {

        public ConnectionHandler() {
            super(ModuleGraph.this);
            this.marker.setValidColor(Style.getMapper().getHighLightColor());
            this.marker.addListener(mxEvent.MARK, this);

            removeGraphListeners(graph);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            if (this.source == null) {
                return;
            }
            Module module = findModuleByCell((mxCell) this.source.getCell());
            if (module == null) {
                return;
            }
            checkConnectablesFrom(module);
        }

        @Override
        public synchronized void invoke(Object o, mxEventObject eo) {
            if (o instanceof mxCellMarker) {
                mxCellMarker mark = (mxCellMarker) o;
                if (mark.isVisible()) {
                    Module m = findModuleByCell((mxCell) (mark.getMarkedState().getCell()));
                    highLight(m);
                } else {
                    unHighLight();
                }
            }
        }


        Module highlighted = null;
        private void highLight(Module m) {
            if (m != null) {
                if (highlighted != m) {
                    unHighLight();
                    Style.getMapper().highLightModule(graph, m);
                    //Style.getMapper().greyModule(graph, m); //To test the style
                    highlighted = m;
                }
            } else {
                unHighLight();
            }
        }
        private void unHighLight() {
            if (highlighted != null) {
                Style.getMapper().unHighLightModule(graph, highlighted);
                highlighted = null;
            }
        }
    }


    @Override
    protected void installKeyHandler() {
        super.installKeyHandler();
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (ModuleGraph.this.equals(e.getComponent())) {
                        boolean cellRemoved = false;
                        for (Object o : graph.getSelectionCells()) {
                            if (o instanceof mxCell) {
                                mxCell cell = (mxCell) o;
                                if (cell.isVertex()) {
                                    deleteModule(findModuleByCell(cell));
                                    cellRemoved = true;
                                } else {
                                    if (cell.isEdge()) {
                                        deleteConnection(cell);
                                        cellRemoved = true;
                                    }
                                }
                            }
                        }
                        if (cellRemoved) {
                            checkArrows();
                            gc();
                        }
                    }
                }
            }
        });
    }

    @Override
    protected mxGraphHandler createGraphHandler() {
        return new mxGraphHandler(this) {
            @Override
            public void mouseClicked(MouseEvent me) {
                super.mouseClicked(me);
                if(me.getButton() == MouseEvent.BUTTON3){
                    if(ModuleGraph.this.getGraph().getSelectionCells() != null &&
                            ModuleGraph.this.getGraph().getSelectionCells().length==1){
                        Module module = findModuleByCell((mxCell)ModuleGraph.this.getGraph().getSelectionCell());
                        if(module!=null){
                            ModuleFactory.ModuleInfo moduleInfo = module.getInfo();
                            if( ! moduleInfo.parameterInfos.isEmpty()){
                                ModuleParametersDialog dialog = new ModuleParametersDialog(parentFrame, false, module, moduleInfo.parameterInfos);
                                dialog.setTitle(module.getCell().getValue().toString());
                                showDialog(dialog);
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                super.mouseReleased(me);

                sortCellsAndModules();
                checkArrows();
                checkConnectables();
            }
        };
    }

    private void showDialog(JDialog dialog){
        Point p = parentFrame.getLocationOnScreen();
        Dimension d = parentFrame.getSize();
        Dimension d2 = dialog.getSize();
        dialog.setLocation(p.x + (d.width-d2.width)/2, p.y + (d.height-d2.height)/2);
        dialog.setVisible(true);
    }
    private void checkConnectables() {
        for (Module module : modules) {
            module.getCell().setConnectable(Connector.isInput(module.getObject()));
        }
    }

    private void checkConnectablesFrom(Module module) {
        boolean hasoutput = false;
        for (Module outModule : modules) {
            boolean isoutput = Connector.hasConnector(module.getObject(), outModule.getObject());
            outModule.getCell().setConnectable(isoutput);
            hasoutput = hasoutput || isoutput;
        }
        module.getCell().setConnectable(hasoutput);
    }

    public Module getSelectedModule(){
        return findModuleByCell((mxCell)graph.getSelectionCell());
    }

    private void checkArrows() {
        mxGraphModel model = (mxGraphModel) graph.getModel();
        model.beginUpdate();
        for (String s : model.getCells().keySet().toArray(new String[]{})) {
            mxCell cell = (mxCell) model.getCell(s);
            if (cell.isEdge()) {
                mxCell source = (mxCell) cell.getSource();
                mxCell target = (mxCell) cell.getTarget();
                if (source == null || target == null) {
                    deleteConnection(cell);
                } else {
                    Connection connection = getConnectionOfArrow(cell);
                    if (connection == null) { //create connection
                        Module in = findModuleByCell(source);
                        Module out = findModuleByCell(target);
                        if (in != null && out != null) {
                            List<Connector> connectors = Connector.findConnectors(in.getObject(), out.getObject());
                            if (connectors.isEmpty()) {
                                deleteConnection(cell); //can not be connected
                            } else {
                                Connector connector = null;
                                if(connectors.size()>1){
                                    ConnectorChooser chooser = new ConnectorChooser(parentFrame,true,connectors);
                                    showDialog(chooser);
                                    if(chooser.getReturnStatus()==ConnectorChooser.RET_OK){
                                        connector = chooser.getSelectedConnector();
                                    }
                                }
                                else{
                                    connector = connectors.get(0);
                                }
                                if(connector==null){
                                    deleteConnection(cell);
                                }
                                else{
                                    connection = new Connection(cell, connector, in, out);
                                    connections.add(connection);
                                }
                            }
                        }
                    }
                }
            }
        }

        //try to connect if it is not the case
        for(Connection connection : connections){
            if( ! connection.isConnected()) {
                connection.connect();
                Style.getMapper().checkConnectionStyle(graph, connection);
            }
        }

        //sort connections
        sortConnections();

        //set the number
        int startValue = 1; //start from 0 or 1 ?
        int count = startValue;
        for(int i=0; i<connections.size();++i){
            Connection current = connections.get(i);
            if(!current.isConnected() || current.getConnector().isUnique()){
                getGraph().cellLabelChanged(current.getCell(),null,false);
                count = startValue;
            }
            else {
                if(i==0 || ! shareModuleAndConnector(connections.get(i-1), current)){
                    count = startValue;
                }
                if(count==startValue){
                    Connection next = i<connections.size()-1 ? connections.get(i+1) : null;
                    if(next!=null && shareModuleAndConnector(next,current) && next.isConnected()){
                        getGraph().cellLabelChanged(current.getCell(),new Integer(count),false);
                        count++;
                    }
                    else{
                        getGraph().cellLabelChanged(current.getCell(),null,false);
                    }
                }
                else{
                    getGraph().cellLabelChanged(current.getCell(),new Integer(count),false);
                    count++;
                }
            }
        }

        model.endUpdate();
    }

    private boolean shareModuleAndConnector(Connection c1, Connection c2){
        return c1.getIn()==c2.getIn() && c1.getConnector()==c2.getConnector();
    }

    private void sortConnections(){
        ArrayList<Connection> sorted = new ArrayList<Connection>(connections);
        Collections.sort(sorted, new Comparator<Connection>(){
            @Override
            public int compare(Connection o1, Connection o2) {
                if(o1.getIn()==o2.getIn()){
                    if(o1.getConnector()==o2.getConnector()){
                        if(o1.isConnected()==o2.isConnected()) {
                            return connections.indexOf(o1)-connections.indexOf(o2);
                        }
                        else{
                            return o1.isConnected() ? -1 : 1;
                        }
                    }
                    return o2.getConnector().hashCode()-o1.getConnector().hashCode();
                }
                return o2.getIn().hashCode()-o1.getIn().hashCode();
            }
        });
        connections = sorted;
    }

    private Connection getConnectionOfArrow(mxCell arrow){
        for (Connection connection : connections) {
            if(connection.getCell() == arrow){
                return connection;
            }
        }
        return null;
    }

    private Module findModuleByID(String id) {
        for (Module module : modules) {
            if (module.getId().equals(id)) {
                return module;
            }
        }
        return null;
    }

    private void updateInternalFrame(Module module) {
        if (internalFrame == null) {
            return;
        }
        currentModuleDisplayed = module;
        if (currentModuleDisplayed == null) {
            internalFrame.setTitle("");
            internalFrame.setContentPane(internalFrameContent);
            internalFrame.setJMenuBar(null);
            internalFrame.setMaximizable(false);
            internalFrame.repaint();
        } else {
            JFrame frame = currentModuleDisplayed.getFrame();
            if (frame == null) {
                internalFrame.setTitle(currentModuleDisplayed.getCell().getValue().toString());
                internalFrame.setContentPane(emptyPanel);
                internalFrame.setJMenuBar(null);
                internalFrame.setMaximizable(false);
                internalFrame.repaint();
            } else {
                if ( ! frame.isVisible()){
                    internalFrame.setTitle(currentModuleDisplayed.getCell().getValue().toString());
                    internalFrame.setContentPane(frame.getContentPane());
                    internalFrame.setJMenuBar(frame.getJMenuBar());
                    internalFrame.setMaximizable(true);
                    internalFrame.repaint();
                }
            }
        }
    }

    public SourceCode generateSourceCode(String className, String packageName, String baseFileName) {
        checkArrows();

        FrameChooser frameChooserPanel = new FrameChooser(modules);
        javax.swing.JScrollPane scrollpane = new javax.swing.JScrollPane();
        int maxDialogSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height *2 /3;
        scrollpane.setViewportView(frameChooserPanel);
        scrollpane.setPreferredSize(new Dimension(scrollpane.getPreferredSize().width, Math.min(scrollpane.getPreferredSize().height, maxDialogSize)));
        int okPressed = JOptionPane.showConfirmDialog(this, scrollpane, "Frames",JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(okPressed != JOptionPane.OK_OPTION){
            return null;
        }
        Map<Module,FrameChooser.Result> framesStatus = frameChooserPanel.getResults();

        SourceCode source = new SourceCode(packageName+"."+className);

        greta.core.util.IniManager iniManager = null;
        String iniManagerCodeName = null;
        String iniFileName = null;
        Map<String,String> iniMapName = new HashMap<String, String>();

        if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Create ini file ?", "Ini File", JOptionPane.YES_NO_OPTION)){
            IniManagerConfiguration iniConfigPanel = new IniManagerConfiguration(modules, framesStatus);
            scrollpane = new javax.swing.JScrollPane();
            scrollpane.setViewportView(iniConfigPanel);
            scrollpane.setPreferredSize(new Dimension(scrollpane.getPreferredSize().width, Math.min(scrollpane.getPreferredSize().height, maxDialogSize)));
            okPressed = JOptionPane.showConfirmDialog(this, scrollpane, "Ini File",JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if(okPressed == JOptionPane.OK_OPTION){
                iniFileName = "./"+baseFileName+".ini";
                iniManagerCodeName = "_IniManager_";
                iniManager = new IniManager(iniFileName){
                    @Override
                    protected BufferedReader getBufferedReader(String fileName) throws Exception {
                        throw new ThisIsNotAnExeption("WARNING: "+ModuleGraph.class.getSimpleName()+" doesn't want to read the file "+fileName);
                    }
                    class ThisIsNotAnExeption extends Exception{
                        ThisIsNotAnExeption(String message){
                            super(message);
                        }
                    }
                };
                iniMapName = iniConfigPanel.getResult();
                source.setIniManager(iniManager);
            }
        }

        String code = "";
        code += "/*\n * This file was auto-generated by the Greta's Modular software.\n */\n";
        code += "package "+packageName+";\n\n";

        //imports for each module classes
        ArrayList<String> imported = new ArrayList<String>();
        if(iniManager !=null){
            code += "import greta.core.util.IniManager;\n";
            imported.add(iniManager.getClass().getName());
            source.addLibrary(Library.getLibrary("greta_util")); //it's a little tricky....
        }
        for(int i=0; i< modules.size(); ++i){
            Module m = modules.get(i);
            String moduleClass = m.getObject().getClass().getCanonicalName();
            if( ! imported.contains(moduleClass)){
                code += "import "+moduleClass+";\n";
                imported.add(moduleClass);
            }
            source.addLibrary(m.getInfo().objectLib);

            if(m.getFrame()!=null && framesStatus.get(m) != FrameChooser.Result.FRAME_DELETED){
                String frameClass = m.getFrame().getClass().getCanonicalName();
                if( ! imported.contains(frameClass)){
                    code += "import "+frameClass+";\n";
                    imported.add(frameClass);
                }
                source.addLibrary(m.getInfo().frameLib);
            }
        }

        code += "import javax.annotation.Generated;\n\n";

        code += "/**\n * This class builds a Greta application.\n * @author Modular\n */\n";
        java.util.Date today = new java.util.Date();
        code += "@Generated(value = \"Modular\", date = \""+today.toString()+"\")\n";

        code += "public class "+className+" {\n" +
                "    public static void main (String[] args) {\n";

        // the next is only if their is at least one window. we can know it with the printed TODO
        code += "        // Try to setup of the appearance of the GUI to the OS one:\n"+
                "        try {javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());} catch (Exception e) {}\n\n" +
                "        // Load the icon for frames\n"+
                "        java.awt.Image icon = null;\n" +
                "        java.net.URL url = "+className+".class.getClassLoader().getResource(\"icon.png\");\n" +
                "        if(url!=null) {icon = java.awt.Toolkit.getDefaultToolkit().getImage(url);}\n\n";
        if(iniManager != null){
            code += "        IniManager "+iniManagerCodeName+" = new IniManager(\""+iniFileName+"\");\n\n";
        }
        //instanciate modules
        code += "        // Instanciate modules\n";
        for(int i=0; i< modules.size(); ++i){
            Module m = modules.get(i);
            FrameChooser.Result frameStatus = framesStatus.get(m);
            JFrame frame = frameStatus==FrameChooser.Result.FRAME_DELETED ? null : m.getFrame();

            String moduleClass = m.getObject().getClass().getSimpleName();
            code += "        "+moduleClass+" "+m.getObjectVariableName()+" = new "+moduleClass+"();\n";
            Map<String,String> parameters = m.getParams();

            for(ModuleFactory.ParameterInfo paramInfo : m.getInfo().parameterInfos){
                if(paramInfo.getSetOn().equals("object")){
                    String value = parameters.get(paramInfo.getName());
                    String iniParamName = iniMapName.get(IniManagerConfiguration.inputName(m, paramInfo));
                    code += "        "+m.getObjectVariableName()+"."+paramInfo.getSetMethod().getName()+"("+getCode(iniManager, iniManagerCodeName, iniParamName, paramInfo.getType(), value)+");\n";
                }
            }

            if(frame!=null && frame!=m.getObject()){
                String frameClass = frame.getClass().getSimpleName();
                code += "        "+frameClass+" "+m.getFrameVariableName()+" = new "+frameClass+"();\n";
                if(m.getInfo().linkOn != null){
                    if(m.getInfo().linkOn.equals("object")){
                        code += "        "+m.getObjectVariableName()+"."+m.getInfo().linkMethod.getName()+"("+m.getFrameVariableName()+");\n";
                    }
                    else{
                        code += "        "+m.getFrameVariableName()+"."+m.getInfo().linkMethod.getName()+"("+m.getObjectVariableName()+");\n";
                    }
                }

            }
            if(frame!=null){
                for(ModuleFactory.ParameterInfo paramInfo : m.getInfo().parameterInfos){
                    if(paramInfo.getSetOn().equals("frame")){
                        String value = parameters.get(paramInfo.getName());
                        String iniParamName = iniMapName.get(IniManagerConfiguration.inputName(m, paramInfo));
                        code += "        "+m.getFrameVariableName()+"."+paramInfo.getSetMethod().getName()+"("+getCode(iniManager, iniManagerCodeName, iniParamName, paramInfo.getType(), value)+");\n";
                    }
                }
                if(frameStatus == FrameChooser.Result.FRAME_VISIBLE) {
                    code += "        if(icon!=null){"+m.getFrameVariableName()+".setIconImage(icon);}\n";
                    code += "        "+m.getFrameVariableName()+".setTitle(\""+frame.getTitle().replaceAll("\\s+", " ") +"\");\n";
                    String xCode = getCode(iniManager, iniManagerCodeName, iniMapName.get(IniManagerConfiguration.inputNameForJFrame(m, "x")), "integer", Integer.toString(frame.getLocation().x));
                    String yCode = getCode(iniManager, iniManagerCodeName, iniMapName.get(IniManagerConfiguration.inputNameForJFrame(m, "y")), "integer", Integer.toString(frame.getLocation().y));
                    code += "        "+m.getFrameVariableName()+".setLocation("+xCode+", "+yCode+");\n";
                    String wCode = getCode(iniManager, iniManagerCodeName, iniMapName.get(IniManagerConfiguration.inputNameForJFrame(m, "w")), "integer", Integer.toString(frame.getWidth()));
                    String hCode = getCode(iniManager, iniManagerCodeName, iniMapName.get(IniManagerConfiguration.inputNameForJFrame(m, "h")), "integer", Integer.toString(frame.getHeight()));
                    code += "        "+m.getFrameVariableName()+".setSize("+wCode+", "+hCode+");\n";
                    code += "        "+m.getFrameVariableName()+".setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);\n";
                    code += "        "+m.getFrameVariableName()+".setVisible(true);\n";
                }
                else{
                     code += "        "+m.getFrameVariableName()+".dispose();\n";
                }
            }
            code += "\n";
        }

        //connect modules
        code += "        // Connect modules\n";
        for(Connection c : connections){
             code += "        "+c.getConnectionCode();
        }

        //TODO: add an option for it:
        //in the cas of theire is no visible frame:
        //if theire is a non-deamon thread started with an infinit loop, we need a way to exit (ctrl+c exist in command prompt)
        //or if theire is no non-deamon thread started we need a way to keep running.
        //prompt input is a good one, but need to start with the command prompt.
        code += "\n"+
                "        // To prevent the situation where there would be no frame or no non-thread, the program is kept alive:\n"+
                "        java.util.Scanner scanner = new java.util.Scanner(System.in);\n" +
                "        String input=\"\";\n" +
                "        while(!input.equalsIgnoreCase(\"exit\") && !input.equalsIgnoreCase(\"quit\") && !input.equalsIgnoreCase(\"end\")){\n" +
                "            input = scanner.next();\n" +
                "        }\n" +
                "        System.out.println(\"Exiting Greta.\");\n" +
                "        System.exit(0);\n";

        //end
        code += "    }\n" +
                "}\n";

        source.setSourceCode(code);
        return source;
    }


    private String getCode(IniManager iniManager, String iniManagerCodeName, String paramName, String type, String value){
        if("string".equals(type)){
            value = value
                    .replaceAll("\\\\", "\\\\\\\\")
                    .replaceAll("\\\"", "\\\\\"")
                    .replaceAll("\\n", "\\\\n")
                    .replaceAll("\\t", "\\\\t");
        }
        if(iniManagerCodeName!=null && paramName != null){
            iniManager.addValueString(paramName, value);
            value = iniManagerCodeName+"."+PrimitiveTypes.getIniManagerCall(type)+"(\""+paramName+"\")";
        }
        else{
            if("string".equals(type)){
                value = "\""+value+"\"";
            }
        }
        if("short".equals(type) || "byte".equals(type) || "float".equals(type)){
            value = "("+type+")"+value;
        }
        return value;
    }

    public static class SourceCode extends SourceFile{
        private IniManager iniManager;
        private List<Library> librariesUsed;
        public SourceCode(String name) {
            super(name, "");
            librariesUsed = new ArrayList<Library>();
        }

        public void addLibrary(Library lib){
            if ((lib != null) && (! librariesUsed.contains(lib))) {
                librariesUsed.add(lib);
            }
        }

        public List<Library> getLibrariesUsed(){
            return librariesUsed;
        }

        public List<Library> getLibrariesNeeded(){
            return Library.getAllDependenciesFor(librariesUsed);
        }

        public boolean useIniManagerFile(){
            return iniManager!=null;
        }

        public String getIniFileName(){
            return iniManager.getDefaultDefinition().getName();
        }

        public void setIniManager(IniManager ini){
            iniManager = ini;
        }

        public IniManager getIniManager(){
            return iniManager;
        }
    }
}
