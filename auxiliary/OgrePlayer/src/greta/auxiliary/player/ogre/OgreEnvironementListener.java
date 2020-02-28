/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.player.ogre;

import greta.auxiliary.player.ogre.agent.AgentFactory;
import greta.auxiliary.player.ogre.agent.MPEG4Agent;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.util.IniManager;
import greta.core.util.environment.Animatable;
import greta.core.util.environment.Environment;
import greta.core.util.environment.EnvironmentEventListener;
import greta.core.util.environment.Leaf;
import greta.core.util.environment.LeafEvent;
import greta.core.util.environment.Node;
import greta.core.util.environment.NodeEvent;
import greta.core.util.environment.TreeEvent;
import greta.core.util.environment.TreeNode;
import greta.core.util.log.Logs;
import java.util.ArrayList;
import java.util.ListIterator;
import vib.auxiliary.player.ogre.natives.ColourValue;
import vib.auxiliary.player.ogre.natives.Entity;
import vib.auxiliary.player.ogre.natives.Light;
import vib.auxiliary.player.ogre.natives.Material;
import vib.auxiliary.player.ogre.natives.MaterialManager;
import vib.auxiliary.player.ogre.natives.MovableObject;
import vib.auxiliary.player.ogre.natives.SceneManager;
import vib.auxiliary.player.ogre.natives.SceneManager.PrefabType;
import vib.auxiliary.player.ogre.natives.SceneNode;
import vib.auxiliary.player.ogre.natives.Vector3;

/**
 *
 * @author Andre-Marie Pez
 */
public class OgreEnvironementListener implements EnvironmentEventListener{

    private static IniManager meshMap = new IniManager("./Player/Data/objectList.ini");
    Environment env;
    SceneManager sceneManager;
    SceneNode rootSceneNode;
    public static ArrayList<MPEG4Agent> agents = new ArrayList<MPEG4Agent>();

    private static ArrayList<OgreEnvironementListener> EnvironmentListenerList = new ArrayList<OgreEnvironementListener>();
    public static synchronized OgreEnvironementListener getEnvironmentListener(Environment env){
        for(OgreEnvironementListener oel : EnvironmentListenerList) {
            if(oel.env == env) {
                return oel;
            }
        }
        OgreEnvironementListener oel = new OgreEnvironementListener(env);
        EnvironmentListenerList.add(oel);
        oel.readEnvironment();
        oel.env.addEnvironementListener(oel);
        return oel;
    }
    private OgreEnvironementListener(Environment env){
        this.env = env;
        sceneManager = Ogre.getSceneManager(env.toString());
        rootSceneNode = sceneManager.getRootSceneNode();
        rootSceneNode.removeAllChildren();
    }

    private void readEnvironment(){
        SceneNode sceneNode = retreiveOrCreateSceneNode(rootSceneNode, env.getRoot().getIdentifier());
        readTree(env.getRoot(),sceneNode);
    }
    private void readTree(TreeNode treeNode, SceneNode sceneNode){

        //parse children
        for(greta.core.util.environment.Node childTreeNode : treeNode.getChildren()){
            if(childTreeNode instanceof TreeNode){
                SceneNode childSceneNode = retreiveOrCreateSceneNode(sceneNode, childTreeNode.getIdentifier());
                readTree((TreeNode)childTreeNode,childSceneNode);
            }
            else{
                if(childTreeNode instanceof Leaf){
                    Leaf leaf = (Leaf)childTreeNode;
                    createObject(leaf.getIdentifier(), leaf.getReference().toString(), Ogre.convert(leaf.getSize()), sceneNode, sceneManager);
                }
            }
        }

        //copie position, orientation and scale
        sceneNode.setPosition(treeNode.getCoordinates());
        if( ! (treeNode instanceof Animatable)){
            sceneNode.setOrientation(Ogre.convert(treeNode.getOrientation()));
            sceneNode.setScale(Ogre.convert(treeNode.getScale()));
        }

        //Create agent
        if(treeNode instanceof MPEG4Animatable){
            MPEG4Animatable mpeg4 = (MPEG4Animatable)treeNode;
            MPEG4Agent agent = findAgent(mpeg4.getAttachedLeaf().getIdentifier());
            if(agent!=null){
                agent.setMPEG4Animatable(mpeg4);
                agent.start();
            }
        }
    }


    @Override
    public void onTreeChange(final TreeEvent te) {
        Ogre.call(new OgreThread.Callback() {
            @Override
            public void run() {
                try{
                    if(te.modifType==TreeEvent.MODIF_ADD){
                        if(te.childNode instanceof TreeNode){
                            TreeNode treeNode = (TreeNode)te.childNode;
                            SceneNode parentSceneNode = findSceneNode(te.getIdNewParentNode());
                            if(treeNode!=null && parentSceneNode!=null) {
                                readTree(treeNode, retreiveOrCreateSceneNode(parentSceneNode, treeNode.getIdentifier()));
                            }
                        }
                        else{
                            if(te.childNode instanceof Leaf){
                                Leaf leaf = (Leaf)te.childNode;
                                SceneNode parentSceneNode = findSceneNode(te.getIdNewParentNode());
                                if(leaf!=null && parentSceneNode!=null) {
                                    createObject(leaf.getIdentifier(), leaf.getReference().toString(), Ogre.convert(leaf.getSize()), parentSceneNode, sceneManager);
                                }
                            }
                        }
                    }
                    else{
                        if(te.modifType==TreeEvent.MODIF_REMOVE){
                            remove(te.getIdChildNode());
                        } else {
                            if(te.modifType==TreeEvent.MODIF_MOVE){

                                SceneNode oldParent = findSceneNode(te.getIdOldParentNode());
                                SceneNode newParent = findSceneNode(te.getIdNewParentNode());

                                if(te.childNode instanceof TreeNode){
                                    SceneNode child = findSceneNode(te.getIdChildNode());
                                    if(child != null){
                                        oldParent.removeChild(child);
                                        newParent.addChild(child);
                                    }
                                } else{
                                    if(te.childNode instanceof Leaf){
                                        MovableObject object = getMovableObject(te.getIdChildNode());
                                        if(object!=null){
                                            object.detatchFromParent();
                                            newParent.attachObject(object);
                                        } else{
                                            //background color
                                            //fog color
                                            //ambient light
                                            Logs.debug("onTreeChange not fully supported yet.");
                                        }
                                    }
                                }
                            } else{
                                Logs.debug("onTreeChange not fully supported yet."); //an other modif type???
                            }
                        }
                    }
                }catch (Throwable t){t.printStackTrace();}
            }
        });
    }


    @Override
    public void onNodeChange(final NodeEvent ne) {
        Ogre.call(new OgreThread.Callback() {
            @Override
            public void run() {
                final SceneNode targetNode = findSceneNode(ne.getIdNode());
                if(targetNode!=null){
                    final Node envNode = ne.node;
                    if(envNode!=null && envNode instanceof TreeNode){
                        if(ne.isPositionChanged()){
                            Ogre.call(new OgreThread.Callback() {
                                @Override
                                public void run() {
                                    targetNode.setPosition(((TreeNode)envNode).getCoordinates());
                                }
                            });
                        }
                        if(ne.isRotationChanged()){
                            Ogre.call(new OgreThread.Callback() {
                                @Override
                                public void run() {
                                    targetNode.setOrientation(Ogre.convert(((TreeNode)envNode).getOrientation()));
                                }
                            });
                        }
                        if(ne.isScaleChanged()){
                            Ogre.call(new OgreThread.Callback() {
                                @Override
                                public void run() {
                                    targetNode.setScale(((TreeNode)envNode).getScale());
                                }
                            });
                        }
                    }
                    Ogre.updateNode(targetNode, false, false);
                }
            }
        });
    }

    @Override
    public void onLeafChange(final LeafEvent le) {
        final Leaf leaf = le.leaf;
        if(leaf.getReference().toLowerCase().startsWith("light.ambient")){
            sceneManager.setAmbientLight(Ogre.convertToColor(leaf.getSize()));
            return;
        }
        if (le.isReferenceChanged()) {
            //try agent
            final MPEG4Agent agent = findAgent(leaf.getIdentifier());
            if (agent != null) {
                agents.remove(agent);
                Ogre.call(new OgreThread.Callback() {
                    @Override
                    public void run() {
                        MPEG4Animatable mpeg4 = agent.getMPEG4Animatable();
                        agent.kill();
                        SceneNode agentNode = agent.getAgentNode();

                        SceneNode parent = agentNode.getParentSceneNode();
                        clearTree(parent);
                        createObject(leaf.getIdentifier(), leaf.getReference(), Ogre.convert(leaf.getSize()), parent, sceneManager);
                        MPEG4Agent agent2 = findAgent(leaf.getIdentifier());
                        agent2.setMPEG4Animatable(mpeg4);
                        agent2.start();

                    }
                });
            } else {
                Ogre.call(new OgreThread.Callback() {
                    @Override
                    public void run() {
                        final MovableObject object = getMovableObject(leaf.getIdentifier());
                        if (object != null) {
                            Ogre.callSync(new OgreThread.Callback() {

                                @Override
                                public void run() {
                                    object.detatchFromParent();
                                    sceneManager.destroyMovableObject(object);
                                }
                            });
                            SceneNode parenNode = findSceneNode(leaf.getParent().getIdentifier());
                            createObject(leaf.getIdentifier(), leaf.getReference(), Ogre.convert(leaf.getSize()), parenNode, sceneManager);
                        } else {
                            //background color
                            //fog color
                            Logs.debug("onLeafChange not fully supported yet.");
                        }
                    }
                });
            }
        } else { //size changed (color for lights)
            Vector3 size = Ogre.convert(leaf.getSize());
            if(leaf.getReference().toLowerCase().startsWith("light.")){
                if(sceneManager.hasLight(leaf.getIdentifier())){
                    ColourValue colour = Ogre.convertToColor(size);
                    Light l = sceneManager.getLight(leaf.getIdentifier());
                    l.setDiffuseColour(colour);
                    l.setSpecularColour(colour);
                }
            }else{
                SceneNode sizeNode = findSceneNode(leaf.getIdentifier()+"!!sizeNode"); //try to find a size node
                if(sizeNode!=null){
                    float resizecube = 0.01f;
                    sizeNode.setScale(size.operatorMultiplyAndAssign(resizecube));
                    sizeNode.setPosition(size.operatorMultiplyAndAssign(0.5f/resizecube));
                }
            }

            //background color
            //fog color
            Logs.debug("onLeafChange not fully supported yet.");
        }
    }

    private void remove(String id){
        if(sceneManager.hasSceneNode(id)) {
            clearTree(sceneManager.getSceneNode(id));
        }
        final MovableObject object = getMovableObject(id);
        if(object!=null){
            Ogre.callSync(new OgreThread.Callback() {

                @Override
                public void run() {
                    object.detatchFromParent();
                    sceneManager.destroyMovableObject(object);
                }
            });
        }
    }

    private MovableObject getMovableObject(String objectId){
        if(sceneManager.hasEntity(objectId)){
            return sceneManager.getEntity(objectId);
        }
        if(sceneManager.hasLight(objectId)){
            return sceneManager.getLight(objectId);
        }
        return null;
    }


    private void clearTree(final SceneNode parent){
        Ogre.callSync(new OgreThread.Callback() {
            @Override
            public void run() {
                _clearTree(parent);
            }
        });
    }
    private void _clearTree(SceneNode parent){
        ListIterator<MPEG4Agent> agentIterator = agents.listIterator();
        while(agentIterator.hasNext()){
            MPEG4Agent agent = agentIterator.next();
            if(agent.getAgentNode().equals(parent)){
                agentIterator.remove();
                agent.kill();
            }
        }
        for(int i=parent.numAttachedObjects()-1; i>=0; --i){
            sceneManager.destroyMovableObject(parent.getAttachedObject(i));
        }

        for(int i=0; i<parent.numChildren(); ++i){
            String childName = parent.getChild_getName(i);
            if(sceneManager.hasSceneNode(childName)){
                //node is a sceneNode
                //I don't want to cast with this f*** JNIs
                //I prefer to call the scene manager to access to the scene node
                _clearTree(sceneManager.getSceneNode(childName));
            }
        }
        parent.removeAndDestroyAllChildren();
    }

    private SceneNode findSceneNode(String idNode){
        if(sceneManager.hasSceneNode(idNode)) {
            return sceneManager.getSceneNode(idNode);
        }
        return null;
    }

    public static MPEG4Agent findAgent(String id){
        for(MPEG4Agent agent : agents) {
            if(agent.getAgentId().equals(id)) {
                return agent;
            }
        }
        return null;
    }

    private SceneNode retreiveOrCreateSceneNode(SceneNode parent, String name){
        if(sceneManager.hasSceneNode(name)) {
            SceneNode retreived = sceneManager.getSceneNode(name);
            retreived.getParentSceneNode().removeChild(retreived);
            parent.addChild(retreived);
            return retreived;
        }
        return  parent.createChildSceneNode(name);
    }

    private void createObject(String id, String rescourceId, Vector3 size, SceneNode parent, SceneManager sceneManager){
        Entity result = null;

        if(rescourceId.toLowerCase().startsWith("agent.") || rescourceId.equalsIgnoreCase("agent")){
            MPEG4Agent agent = findAgent(id);
            if(agent==null){
                agents.add(AgentFactory.instanciate(id, rescourceId.equalsIgnoreCase("agent") ? "greta" : rescourceId.substring(6), parent, sceneManager));
            }
            return ;
        }

        if(rescourceId.toLowerCase().startsWith("light")){
            if(rescourceId.toLowerCase().startsWith("light.ambient")){
                sceneManager.setAmbientLight(Ogre.convertToColor(size));
            }
            else{
                Light l = createLight(id, parent, sceneManager, size);
                if(rescourceId.toLowerCase().startsWith("light.spot")){
                    l.setType(Light.LightTypes.LT_SPOTLIGHT);
                    l.setSpotlightOuterAngle(Math.PI/2);
                }
                else{
                    if(rescourceId.toLowerCase().startsWith("light.directional")){
                        l.setType(Light.LightTypes.LT_DIRECTIONAL);
                        l.setDirection(0, -1, 0);
                    }
                }
            }
            return ;
        }
        if(rescourceId.toLowerCase().startsWith("color")){
            if(rescourceId.toLowerCase().startsWith("color.background")){
                //TODO find all Cameras and change background color
            }
            else {
                if(rescourceId.toLowerCase().startsWith("color.fog")){
                    //TODO setup fog
                    sceneManager.setFog_FOG_EXP(Ogre.convertToColor(size), 0.01f, 0, 1);
                }
            }
            return ;
        }

        String meshName = meshMap.getValueString(rescourceId);
        if(rescourceId.toLowerCase().startsWith("debug") && !Ogre.DEBUG) {
                return ;
        }
        if(meshName!=null && !meshName.isEmpty() && Ogre.isResourceExists(meshName)){
            result=createObject(id, meshName, sceneManager, parent);
        }

        if(result==null){
            SceneNode sizeNode = parent.createChildSceneNode(id+"!!sizeNode");
            float resizecube = 0.01f;
            sizeNode.setScale(size.operatorMultiplyAndAssign(resizecube));
            sizeNode.setPosition(size.operatorMultiplyAndAssign(0.5f/resizecube));

            result = Ogre.createEntity(sceneManager, id, PrefabType.PT_CUBE, true);
            Ogre.setMaterial(result, "camera_debug_marker");
//            Ogre.setMaterial(result, "unknown_object");
            sizeNode.attachObject(result);
        }
    }

    private static Entity createObject(String id, String meshName, SceneManager sceneManager, SceneNode parent){
        Entity entity = null;
        if(sceneManager.hasEntity(id)) {
            entity = sceneManager.getEntity(id);
        }
        else {
            entity = Ogre.createEntity(sceneManager, id, meshName, true);
        }//sceneManager.createEntity(id, meshName);
        parent.attachObject(entity);
        entity.setCastShadows(true);
        return entity;
    }

    private static SceneNode createSceneNode(String id, SceneNode parent, double x, double y, double z){
        return parent.createChildSceneNode(id, new vib.auxiliary.player.ogre.natives.Vector3(x, y, z));
    }

    private static Light createLight(String id, SceneNode parent, SceneManager sceneManager, Vector3 size){
        if(sceneManager.hasLight(id)) {
            return sceneManager.getLight(id);
        }

        ColourValue colour = Ogre.convertToColor(size);

        if(Ogre.DEBUG){
            Entity debugSphere = Ogre.createEntity(sceneManager, id+"_debug_sphere", "light.mesh", true);
                  //  Ogre.createEntity(sceneManager, id+"_debug_sphere", PrefabType.PT_SPHERE, true);
            SceneNode debugSphereNode = createSceneNode(id, parent, 0, 0, 0);
            debugSphereNode.attachObject(debugSphere);
            double radius = Math.max(size.getx(), Math.max(size.gety(), size.getz()));
            debugSphereNode.setScale(radius, radius, radius);
            debugSphere.setCastShadows(false);


            String materialName = "light_debug_marker";
            String newMaterialName = materialName+"-"+debugSphere.getName();

//            greta.auxiliary.player.ogre.natives.ResourcePtr resourceMaterialPTR = new greta.auxiliary.player.ogre.natives.ResourcePtr();
//            Ogre.dontDelete(resourceMaterialPTR);//added to prevent crash
            Material originalMaterialPtr = MaterialManager.getSingleton().getByName(materialName);


            Material matClonePtr = originalMaterialPtr.clone(newMaterialName, false, "");

            matClonePtr.getTechnique(0).getPass(0).setAmbient(colour);
//            ColourValue colourDif = new ColourValue(0, 0, 0, 0.9f);
//            matClonePtr.getTechnique(0).getPass(0).setDiffuse(colour);
//            matClonePtr.getTechnique(0).getPass(0).setSelfIllumination(colour);
//            matClonePtr.getTechnique(0).getPass(0).setMaxSimultaneousLights(1);
//            matClonePtr.getTechnique(0).getPass(0).setIteratePerLight(true, false, Light.LightTypes.LT_POINT);
//            matClonePtr.getTechnique(0).getPass(0).setLightCountPerIteration(1);


            Ogre.setMaterial(debugSphere, newMaterialName);

        }
        Light light = sceneManager.createLight(id);
        light.setCastShadows(true);
        parent.attachObject(light);
        light.setDiffuseColour(colour);
        light.setSpecularColour(colour);
        return light;
    }

}
