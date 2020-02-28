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

import greta.core.util.audio.Mixer;
import greta.core.util.environment.Environment;
import greta.core.util.math.Vec3d;
import vib.auxiliary.player.ogre.natives.Quaternion;
import vib.auxiliary.player.ogre.natives.SceneManager;
import vib.auxiliary.player.ogre.natives.SceneNode;
import vib.auxiliary.player.ogre.natives.Vector3;

/**
 * This class is an helper to manipulate the cameras from Ogre.<br/>
 * It is composed by four {@code SceneNode}s and one Ogre camera:<br/>
 * &nbsp;&nbsp;&nbsp;translation node<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_ yaw node (rotation on local z axis)<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_ pitch node (rotation on local y axis)<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_ roll node (rotation on local x axis)<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_ camera
 * @author Andre-Marie Pez
 */
public class Camera {

    private SceneNode translationNode;
    private SceneNode yawNode;
    private SceneNode pitchNode;
    private SceneNode rollNode;
    private vib.auxiliary.player.ogre.natives.Camera cam;
    private Mixer mic;
    private greta.core.util.math.Quaternion rotationAdaptor;
    private greta.core.util.math.Vec3d backgroundColor;

    /**
     * Creates a new {@code Camera}

     * @param sceneManager the {@code SceneManager} that will creates the OgreCamera
     * @param parentNode the {@code SceneNode} where this {@code Camera} is attached to
     * @param idCamera the identifier of this {@code Camera}, it must be unique in the sceneManager
     */
    public Camera(SceneManager sceneManager, SceneNode parentNode, String idCamera){

        translationNode = parentNode.createChildSceneNode();
        yawNode         = translationNode.createChildSceneNode();
        pitchNode       = yawNode.createChildSceneNode();
        rollNode        = pitchNode.createChildSceneNode();

        cam = sceneManager.createCamera(idCamera);
        cam.setNearClipDistance(0.01);
        cam.setFOVy(Math.toRadians(30)); //Field of view
        rollNode.attachObject(cam);

        if(Ogre.DEBUG){
            cam.setDebugDisplayEnabled(true);
            cam.setVisible(true);
        }
        parentNode._update(true, false);
        cam.setOrthoWindow(100, 100);
        mic = new Mixer();
        mic.setGuest(true);
        rotationAdaptor = new greta.core.util.math.Quaternion(new Vec3d(0, 1, 0), (float)Math.PI);
        backgroundColor = new Vec3d(0.5f, 0.5f, 0.5f);

        //cm.currentCameraId = mic.getIdentifier();
    }

    public void setBackgroundColor(Vec3d color){
        if(color!=null){
            backgroundColor = color;
        }
        applyBackgroundColor();
    }

    public void setBackgroundColorWithoutApply(Vec3d color){
        if(color!=null){
            backgroundColor = color;
        }
    }
    public Vec3d getBackgroundColor(){
        return backgroundColor;
    }

    public void applyBackgroundColor(){
        cam.getViewport().setBackgroundColour(Ogre.convertToColor(backgroundColor));
    }
    /**
     * @return the translation node of this {@code Camera}
     */
    public SceneNode getTranslationNode(){
        return translationNode;
    }

    /**
     * Set the position of this {@code Camera}.<br/>
     * Only the translation node is affected.
     * @param x the coordinate on the x axis
     * @param y the coordinate on the y axis
     * @param z the coordinate on the z axis
     */
    public void setPosition(final double x, final double y, final double z){
        Ogre.call(new OgreThread.Callback() {
            @Override
            public void run() {
                translationNode.setPosition(x, y, z);
                translationNode._update(true, true);
                updateMicPosition();
            }
        });
    }

    /**
     * @return the Ogre object of this {@code Camera}
     */
    public vib.auxiliary.player.ogre.natives.Camera getOgreCamera(){
        return cam;
    }

    /**
     * Rotate this {@code Camera} around its y axis.<br/>
     * Only the yaw node is affected.
     * @param angle the angle value in radian
     */
    public void yaw(final double angle) {
        Ogre.call(new OgreThread.Callback() {
            @Override
            public void run() {
                yawNode.yaw(angle);
                updateMicOrientation();
            }
        });
    }

    /**
     * Rotate this {@code Camera} around its x axis.<br/>
     * Only the pitch node is affected.<br/>
     * To avoid gimbal lock, the pitch is limited.
     * @param angle the angle value in radian
     * @return {@code true} if the orientation changes. {@code false} otherwise.
     */
    public boolean pitch(final double angle) {
        if(Math.abs(getPitch()+angle) > Math.PI/2.1) {
            return false;
        }
        Ogre.call(new OgreThread.Callback() {
            @Override
            public void run() {
                pitchNode.pitch(angle);
                updateMicOrientation();
            }
        });
        return true;
    }

    /**
     * Rotate this {@code Camera} around its z axis.<br/>
     * Only the roll node is affected.
     * @param angle the angle value in radian
     */
    public void roll(final double angle) {
        Ogre.call(new OgreThread.Callback() {
            @Override
            public void run() {
                rollNode.roll(angle);
                updateMicOrientation();
            }
        });
    }

    /**
     * Translate this {@code Camera}<br/>
     * Only the translation node is affected.
     * @param x the offset on the x axis
     * @param y the offset on the y axis
     * @param z the offset on the z axis
     */
    public void translate(final double x, final double y, final double z){
        Ogre.call(new OgreThread.Callback() {
            @Override
            public void run() {

                Vector3 vect = new Vector3(x, y, z);

                Quaternion orientation = cam.getDerivedOrientation();
                translationNode.setOrientation(orientation);
                translationNode.translate(vect);
                translationNode.setOrientation(Quaternion.getIDENTITY());
                translationNode._update(true, true);
                updateMicPosition();
            }
        });
    }

    /**
     * @return the current pitch of this {@code Camera}
     */
    public double getPitch(){
        return pitchNode.getOrientation().getPitch(true);
    }

    /**
     * @return the current yaw of this {@code Camera}
     */
    public double getYaw(){
        return yawNode.getOrientation().getYaw(true);
    }

    /**
     * @return the current roll of this {@code Camera}
     */
    public double getRoll(){
        return rollNode.getOrientation().getRoll(true);
    }

    public void setEnvironment(Environment env){
        mic.setEnvironment(env);
    }

    private void updateMicPosition(){
        mic.setCoordinates(Ogre.convert(rollNode._getDerivedPosition()));
    }
    private void updateMicOrientation(){
        greta.core.util.math.Quaternion q = Ogre.convert(rollNode._getDerivedOrientation());
        q.multiply(rotationAdaptor);
        mic.setOrientation(q);
    }

    public Mixer getMic(){
        return mic;
    }
/*
    @Override
    public void onCharacterChanged() {
        getCharacterManager().currentCameraId = mic.getIdentifier();
    }
    */
}
