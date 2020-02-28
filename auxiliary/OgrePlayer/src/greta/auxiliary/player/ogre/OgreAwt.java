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

import greta.auxiliary.player.ogre.capture.Capturable;
import greta.core.util.audio.AudioOutput;
import greta.core.util.environment.Environment;
import greta.core.util.environment.Leaf;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import vib.auxiliary.player.ogre.natives.PixelBox;
import vib.auxiliary.player.ogre.natives.RenderWindow;
import vib.auxiliary.player.ogre.natives.SceneManager;
import vib.auxiliary.player.ogre.natives.SceneNode;
import vib.auxiliary.player.ogre.natives.Vector3;

/**
 * Display the Ogre's rendering in an AWT {@code Component}
 * @author Andre-Marie Pez
 */
public class OgreAwt extends java.awt.Canvas implements Capturable{

    //static fields
    private static int cameraCount = 0;
    //instance fields
    private Environment env;
    private RenderWindow renderWindow;
    private Camera camera;
    private double cameraDefaultPosX;
    private double cameraDefaultPosY;
    private double cameraDefaultPosZ;
    private double cameraDefaultPitch;
    private double cameraDefaultYaw;
    private double cameraDefaultRoll;
    private boolean initialized;
    private AudioOutput defaultAudioOutput;


    public OgreAwt(AudioOutput audioOutput) {
        cameraDefaultPosX = 0;
        cameraDefaultPosY = 0.7;
        cameraDefaultPosZ = 1.3;
        cameraDefaultPitch = 0;
        cameraDefaultYaw = 0;
        cameraDefaultRoll = 0;
        initialized = false;
        defaultAudioOutput = audioOutput;

    }

    public OgreAwt() {
        this(null);
    }

    @Override
    public void paint(java.awt.Graphics g) {
        super.paint(g);
        if (env != null && !initialized) {
            initializeOgre();
            if(defaultAudioOutput!=null){
                camera.getMic().setEnvironment(env);
                camera.getMic().addAudioOutput(defaultAudioOutput);
                camera.getMic().startPlaying();
            }
            initialized = true;
        }
    }

    public void resizeRenderWindow(final float width, final float height) {
        Ogre.call(new OgreThread.Callback() {
            @Override
            public void run() {
                camera.getOgreCamera().setAspectRatio(width / height);
                renderWindow.resize((long) width, (long) height);//linux
                renderWindow.windowMovedOrResized();
            }
        });
    }

    public void initializeOgre() {
        renderWindow = Ogre.createWindow(ReflectUtilities.getWindowId(this), this.getWidth(), this.getHeight());

        final OgreEnvironementListener oel = OgreEnvironementListener.getEnvironmentListener(env);

        Ogre.callSync(new OgreThread.Callback() {
            @Override
            public void run() {
                SceneManager sceneManager = oel.sceneManager;
                SceneNode rootSceneNode = oel.rootSceneNode;

                // create camera
                camera = new Camera(sceneManager, rootSceneNode, "AWT Camera " + (cameraCount++));
                camera.setPosition(cameraDefaultPosX, cameraDefaultPosY, cameraDefaultPosZ);
                camera.pitch(cameraDefaultPitch);
                camera.yaw(cameraDefaultYaw);
                camera.roll(cameraDefaultRoll);
                camera.getOgreCamera().setCastShadows(true);
                camera.getOgreCamera().setAspectRatio(((double) getWidth()) / ((double) getHeight()));
                renderWindow.addViewport(camera.getOgreCamera());
                for(Leaf leaf : env.getListLeaf()){
                    if(leaf.getReference().equalsIgnoreCase("color.background")){
                        camera.setBackgroundColor(leaf.getSize());
                    }
                }
                camera.applyBackgroundColor();
            }
        });

        OgreMouseListener listener = new OgreMouseListener(camera, this);
        this.addMouseListener(listener);
        this.addMouseWheelListener(listener);
        this.addMouseMotionListener(listener);

        addComponentListener(new ComponentAdapter() {
            int oldH = 0;
            int oldW = 0;

            @Override
            public void componentResized(ComponentEvent e) {
                if (OgreAwt.this.getHeight() != oldH || OgreAwt.this.getWidth() != oldW) {
                    oldH = OgreAwt.this.getHeight();
                    oldW = OgreAwt.this.getWidth();
                    resizeRenderWindow(Math.max(1, oldW), Math.max(1, oldH));
                }
            }
        });

        this.addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                    if (!OgreAwt.this.isValid()) {
                        detach();
                    }
                }
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        detach();
        super.finalize();
    }

    public void detach(){
        Ogre.callSync(new OgreThread.Callback() {
            @Override
            public void run() {
                System.out.println("detach");
                Ogre.getRoot().detachRenderTarget(renderWindow);
            }
        });
    }

    public boolean isPrimary() {
        return renderWindow == null ? false : renderWindow.isPrimary();
    }

    public void setEnvironment(Environment environment) {
        env = environment;
    }

    @Override
    public Camera getCamera() {
        return camera;
    }

    protected void setCameraDefaultPosX(double value) {
        cameraDefaultPosX = value;
        if (camera != null) {
            Vector3 pos = camera.getTranslationNode().getPosition();
            camera.setPosition(cameraDefaultPosX, pos.gety(), pos.getz());
        }
    }

    protected void setCameraDefaultPosY(double value) {
        cameraDefaultPosY = value;
        if (camera != null) {
            Vector3 pos = camera.getTranslationNode().getPosition();
            camera.setPosition(pos.getx(), cameraDefaultPosY, pos.getz());
        }
    }

    protected void setCameraDefaultPosZ(double value) {
        cameraDefaultPosZ = value;
        if (camera != null) {
            Vector3 pos = camera.getTranslationNode().getPosition();
            camera.setPosition(pos.getx(), pos.gety(), cameraDefaultPosZ);
        }
    }

    public void setCameraDefaultPitch(double cameraDefaultPitch) {
        this.cameraDefaultPitch = cameraDefaultPitch;
        if (camera != null) {
            camera.pitch(cameraDefaultPitch);
        }
    }

    public void setCameraDefaultYaw(double cameraDefaultYaw) {
        this.cameraDefaultYaw = cameraDefaultYaw;
        if (camera != null) {
            camera.yaw(cameraDefaultYaw);
        }
    }

    public void setCameraDefaultRoll(double cameraDefaultRoll) {
        this.cameraDefaultRoll = cameraDefaultRoll;
        if (camera != null) {
            camera.roll(cameraDefaultRoll);
        }
    }

    public double getCameraDefaultPosX() {
        return cameraDefaultPosX;
    }

    public double getCameraDefaultPosY() {
        return cameraDefaultPosY;
    }

    public double getCameraDefaultPosZ() {
        return cameraDefaultPosZ;
    }

    public double getCameraDefaultPitch() {
        return cameraDefaultPitch;
    }

    public double getCameraDefaultYaw() {
        return cameraDefaultYaw;
    }

    public double getCameraDefaultRoll() {
        return cameraDefaultRoll;
    }


    /////////////////////////////////////////////
    //// Capturable part
    /////////////////////////////////////////////

    private int captureHeight = 4;
    private int captureWidth = 4 ;
    private PixelBox pib = null;

    private int round4(int value){
        return Math.max(4, (value/4)*4);
    }

    @Override
    public synchronized void prepareCapture() {
        captureWidth = round4(getWidth());
        captureHeight = round4(getHeight());
        pib = new PixelBox(captureWidth, captureHeight);
    }

    @Override
    public int getCaptureWidth() {
        return captureWidth;
    }

    @Override
    public int getCaptureHeight() {
        return captureHeight;
    }

    @Override
    public boolean isSizeChanged() {
        return
            (captureWidth  != round4(getWidth())) ||
            (captureHeight != round4(getHeight()));
    }

    @Override
    public synchronized byte[] getCaptureData() {
        Ogre.callSync(new CaptureMe());
        return pib.getByteBuffer();
    }

    private class CaptureMe extends OgreThread.Callback{
        @Override
        public void run() {
            renderWindow.copyContentsToMemory(pib);
        }
    };
}
