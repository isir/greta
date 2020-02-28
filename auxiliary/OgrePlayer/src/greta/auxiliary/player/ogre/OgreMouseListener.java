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

import greta.core.util.IniManager;
import greta.core.util.math.Vec3d;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import vib.auxiliary.player.ogre.natives.Entity;
import vib.auxiliary.player.ogre.natives.Ray;
import vib.auxiliary.player.ogre.natives.RaySceneQuery;
import vib.auxiliary.player.ogre.natives.RaySceneQueryResult;
import vib.auxiliary.player.ogre.natives.SceneManager;
import vib.auxiliary.player.ogre.natives.SceneNode;
import vib.auxiliary.player.ogre.natives.Vector3;

/**
 *
 * @author Andre-Marie Pez
 */
public class OgreMouseListener implements MouseListener, MouseWheelListener, MouseMotionListener{

    private Camera camera;
    private OgreAwt view;
    private int x;
    private int y;
    private Vec3d pivot;

    private SceneNode marqueurNode;
    private Entity marqueur;

    private static boolean SHOW_MOUSE_BALL = IniManager.getGlobals().getValueBoolean("OGRE_SHOW_MOUSE_BALL");

    private boolean mouseDown = false; //fix some bug

    OgreMouseListener(Camera camera, OgreAwt composite) {
        this.camera = camera;
        this.view = composite;
        pivot = null;

        if(SHOW_MOUSE_BALL){
            marqueurNode = camera
                    .getOgreCamera()
                    .getSceneManager()
                    .getRootSceneNode()
                    .createChildSceneNode();
            marqueur = Ogre.createEntity(
                    camera.getOgreCamera().getSceneManager(),
                    "marker_"+camera.getOgreCamera().getName(),
                    SceneManager.PrefabType.PT_SPHERE,
                    false);
            Ogre.setMaterial(marqueur, "camera_debug_marker");
            marqueurNode.attachObject(marqueur);
            marqueurNode.setScale(0.0005, 0.0005, 0.0005);
            marqueur.setCastShadows(false);
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {}

    @Override
    public void mousePressed(MouseEvent me) {
        //view.setFocus();
        updateXY(me);

        Ogre.callSync(new OgreThread.Callback() {

            @Override
            public void run() {
                Ogre.getRoot()._fireFrameStarted();
                Ray ray = camera.getOgreCamera().getCameraToViewportRay(
                        (float) x / (float) view.getWidth(),
                        (float) y / (float) view.getHeight());

                RaySceneQuery query = camera.getOgreCamera().getSceneManager().createRayQuery(ray);
                query.setSortByDistance(true, 0);

                RaySceneQueryResult result = query.execute();
                pivot = null;

                if(SHOW_MOUSE_BALL) {
                    marqueur.setVisible(false);
                }

                for (int i = 0; i < result.size() && pivot == null; ++i) {
                    if (result.at(i).distance() > 0) {
                        pivot = Ogre.convert(ray.getPoint(result.at(i).distance()));
                        pivot = Vec3d.division(pivot,
                                camera.getOgreCamera()
                                .getSceneManager()
                                .getRootSceneNode()
                                .getScale().getx());
        //                Ogre.print(pivot);
                        if(SHOW_MOUSE_BALL){
                            Ogre.call(new OgreThread.Callback(){public void run() {marqueurNode.setPosition(pivot.x(), pivot.y(), pivot.z());}});
                            marqueur.setVisible(true);
                            Ogre.updateNode(marqueurNode, true, false);
                        }
                    }
                }
                Ogre.getRoot()._fireFrameEnded() ;
            }
        });
        mouseDown = true;
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        if(SHOW_MOUSE_BALL){
            marqueur.setVisible(false);
            Ogre.call(new OgreThread.Callback(){public void run() {marqueurNode.setPosition(0,0,0);}});
            Ogre.updateNode(marqueurNode,true, false);
        }
        mouseDown = false;
    }

    @Override
    public void mouseEntered(MouseEvent me) {}

    @Override
    public void mouseExited(MouseEvent me) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent me) {
        int count = - me.getUnitsToScroll();
        if (me.isControlDown()) {
            SceneNode toScale = camera.getOgreCamera().getSceneManager().getRootSceneNode();
            double factor = toScale.getScale().getx()*(1.0f + count*0.1);
            toScale.setScale(factor, factor, factor);
            System.out.println(camera.getOgreCamera().getSceneManager().getRootSceneNode().getScale().getx());
        }
        else{
            camera.translate(0, 0, -count*0.2);
        }
    }


    private void updateXY(MouseEvent me) {
        x = me.getX();
        y = me.getY();
    }

    private Vec3d getCameraPositionFromPivot(){
        Vec3d posFromPivot = Ogre.convert(camera.getOgreCamera().getDerivedPosition());
        posFromPivot.divide(camera.getOgreCamera().getSceneManager().getRootSceneNode().getScale().getx());
        posFromPivot.minus(pivot);
        return posFromPivot;
    }

    private Vec3d getCameraDirection(){
        return Ogre.convert(camera.getOgreCamera().getDerivedDirection());
    }

    @Override
    public void mouseDragged(final MouseEvent me) {
        boolean updateXY = false;
        int modifiers = me.getModifiers();
        if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                if (me.isControlDown() || pivot == null) {
                    camera.yaw((me.getX() - x) / ((double) (view.getWidth())));
                    camera.pitch((me.getY() - y) / ((double) (view.getHeight())));
                    updateXY = true;
                }
                else{
                    Ogre.callSync(new OgreThread.Callback() {

                        @Override
                        public void run() {
                            double yawAngle   = - 3.0 * (me.getX() - x) / ((double) (view.getWidth()));
                            double pitchAngle = - 3.0 * (me.getY() - y) / ((double) (view.getHeight()));

                            Vector3 parentCameraPos = camera.getTranslationNode().getParentSceneNode()._getDerivedPosition();

                            Vec3d direction = getCameraDirection();
                            Vec3d posFromPivot = getCameraPositionFromPivot();

                            camera.yaw(yawAngle);
                            if (!camera.pitch(pitchAngle)) {
                                pitchAngle = 0;
                            }

                            greta.core.util.math.Quaternion yaw = new greta.core.util.math.Quaternion(new Vec3d(0,1,0), yawAngle);

                            Vec3d pitchAxis = (new Vec3d(0,-1,0)).cross3(direction);
                            greta.core.util.math.Quaternion pitch = new greta.core.util.math.Quaternion(pitchAxis, pitchAngle);

                            Vec3d newPos = yaw.rotate(pitch.rotate(posFromPivot));
                            newPos.add(pivot);

                            camera.setPosition(
                                    newPos.x() - parentCameraPos.getx(),
                                    newPos.y() - parentCameraPos.gety(),
                                    newPos.z() - parentCameraPos.getz());
                        }
                    });

                    updateXY = true;

                    //waiting for the end of the OgreThread stack :
//                    Ogre.waitingForEndStack();
                }
            }

            if ((modifiers & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK) {
                if (me.isControlDown()) {
                    camera.roll((me.getX() - x) / ((double) (view.getWidth())));
                }
                else{
                    double factor = 2.0;
                    if(pivot != null){
                        Vec3d posFromPivot = getCameraPositionFromPivot();
                        factor *= posFromPivot.length();
                    }
                    camera.translate(0, 0, factor*(me.getY() - y)/((double)view.getHeight()));
                }
                updateXY = true;
            }

            if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                double factor = 2.0;
                if(pivot!=null){
                    Vec3d posFromPivot = getCameraPositionFromPivot();
                    factor *= 0.25 * posFromPivot.length();
                }

                camera.translate(
                        -(me.getX() - x) / ((double) view.getHeight())*factor,
                        (me.getY() - y) / ((double) view.getHeight())*factor,
                        0);
                updateXY = true;
            }
            if (updateXY) {
                updateXY(me);
            }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

}
