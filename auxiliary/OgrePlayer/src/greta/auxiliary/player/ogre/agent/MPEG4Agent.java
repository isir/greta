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
package greta.auxiliary.player.ogre.agent;

import greta.auxiliary.player.ogre.Ogre;
import greta.auxiliary.player.ogre.OgreThread;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.util.IniManager;
import greta.core.util.time.Timer;
import vib.auxiliary.player.ogre.natives.Entity;
import vib.auxiliary.player.ogre.natives.Quaternion;
import vib.auxiliary.player.ogre.natives.SceneNode;
import vib.auxiliary.player.ogre.natives.Vector3;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class MPEG4Agent extends Thread {

    protected abstract void applyFapFrame(FAPFrame fapFrame);

    protected abstract void applyBapFrame(BAPFrame bapFrame);
    private MPEG4Animatable mpeg4;
    private String id;
    private boolean killed;
    protected SceneNode agentNode;
    protected SceneNode bapPositionNode;
    private float currentX = 0;
    private float currentY = 0;
    private float currentZ = 0;
    private boolean showSkeleton = IniManager.getGlobals().getValueBoolean("OGRE_SHOW_SKELETON");
    private boolean showBody = !showSkeleton;
    private Skeleton skeleton;
    boolean visible;

    public MPEG4Agent(String id, SceneNode parent) {
        super("MPEG4Agent-" + id);
        this.agentNode = createChildSceneNode(parent, id + "_agentNode");
        this.bapPositionNode = createChildSceneNode(this.agentNode, id + "_bapPositionNode");
        this.id = id;
        this.killed = false;
        this.setDaemon(true);
    }

    public void setMPEG4Animatable(MPEG4Animatable animatable) {
        mpeg4 = animatable;
        requestUpdateHead();
    }

    public MPEG4Animatable getMPEG4Animatable() {
        return mpeg4;
    }

    @Override
    public void run() {
        if(!visible && !killed){
            Ogre.callSync(new OgreThread.Callback() {

                @Override
                public void run() {
                    setVisible(true);
                }
            });
        }
        while (!killed) {
            if(Ogre.realTime) {
                update();
            }
            Timer.sleep(5);
        }
    }

    public void update() {
        //here we use double synchronisation in this order to prevent mutual blocking between this thread and the OgreThread.
        //in general, all synchronizations on this object must be call by the OgreThread.
        Ogre.callSync(new greta.auxiliary.player.ogre.OgreThread.Callback() {public void run() {
            synchronized (MPEG4Agent.this) {
                if (mpeg4 != null) {
                    FAPFrame ff = mpeg4.getCurrentFAPFrame();
                    BAPFrame bf = mpeg4.getCurrentBAPFrame();
                    applyFapFrame(ff);
                    if(bf.getMask(BAPType.HumanoidRoot_tr_lateral) ||
                       bf.getMask(BAPType.HumanoidRoot_tr_vertical) ||
                       bf.getMask(BAPType.HumanoidRoot_tr_frontal)){
                        float newX = bf.getValue(BAPType.HumanoidRoot_tr_lateral)/1000f;
                        float newY = bf.getValue(BAPType.HumanoidRoot_tr_vertical)/1000f;
                        float newZ = bf.getValue(BAPType.HumanoidRoot_tr_frontal)/1000f;
                        if(newX!=currentX || newY!=currentY || newZ!=currentZ){
                            currentX = newX;
                            currentY = newY;
                            currentZ = newZ;
                            agentNode.setPosition(currentX, currentY, currentZ);
                            Ogre.updateNode(agentNode, false, false);
                        }
                    }
                    applyBapFrame(bf);
                }
            }
        }});
    }

    public String getAgentId() {
        return id;
    }

    public void kill() {
        //here we use double sychronizations in this order to prevent mutual blocking between this thread and the OgreThread.
        //in general, all synchronizations on this object must be call by the OgreThread.
        Ogre.callSync(new greta.auxiliary.player.ogre.OgreThread.Callback() {public void run() {
            synchronized (MPEG4Agent.this) {
                mpeg4 = null;
            }
        }});
        setVisible(false);
        killed = true;
    }

    protected static SceneNode createChildSceneNode(SceneNode parent, String id) {
        return parent.createChildSceneNode(id);
    }

    public SceneNode getAgentNode() {
        return agentNode;
    }

    public void setVisible(final boolean visible) {
        //here we use double sychronizations in this order to prevent mutual blocking between this thread and the OgreThread.
        //in general, all synchronizations on this object must be call by the OgreThread.
        Ogre.callSync(new greta.auxiliary.player.ogre.OgreThread.Callback() {public void run() {
            synchronized (MPEG4Agent.this) {
                agentNode.setVisible(visible, true);
                if(visible){
                    setEntitiesVisible(showBody);
                }
                if(visible){
                    if(//showSkeleton &&
                            /* even if the skeleton is not visible, entities created
                            with the instance of Skeleton class will update the
                            bounding box of the main entity and then ensure a better
                            visibility */
                        skeleton==null){
                        Entity body = getMainEntityWithSkeleton();
                        if(body != null){
                            skeleton = new Skeleton(body);
                        }

                    }
                    if(skeleton!=null){
                        skeleton.setVisible(showSkeleton);
                    }
                }
                MPEG4Agent.this.visible = visible;
            }
        }});
    }

    protected abstract void requestUpdateHead();

    protected void updateHeadPosition(Vector3 position){
        updateHeadPosition(Ogre.convert(position));
    }
    protected void updateHeadPosition(greta.core.util.math.Vec3d position){
        mpeg4.getHeadNode().setCoordinates(position);
    }

    protected void updateHeadOrientation(Quaternion orientation){
        updateHeadOrientation(Ogre.convert(orientation));
    }
    protected void updateHeadOrientation(greta.core.util.math.Quaternion orientation){
        mpeg4.getHeadNode().setOrientation(orientation);
    }

    public void scale(Vector3 vect){
        agentNode.scale(vect);
    }

    public void scale(double x, double y, double z){
        scale(new Vector3((float)x, (float)y, (float)z));
    }

    public void scale(double factor){
        scale(factor,factor,factor);
    }

    public abstract Entity getMainEntityWithSkeleton();

    protected abstract void setEntitiesVisible(boolean visible);
}
