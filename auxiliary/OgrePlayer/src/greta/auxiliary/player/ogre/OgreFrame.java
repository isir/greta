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
import greta.core.util.audio.Line;
import greta.core.util.environment.Environment;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;

/**
 *
 * @author Andre-Marie Pez
 */
public class OgreFrame extends JFrame implements Capturable{

    OgreAwt ogreView;
    protected static final int BUTTON_WIREFRAME = 1;
    public OgreFrame(){
        this(Ogre.DEBUG ? 1 : 0); // 0 - Without the 'Wireframe' button
                                  // 1 - With the 'Wireframe' button
    }
    protected OgreFrame(int buttons){
        super("Player");
        Dimension d = new Dimension(
                720, 576 //HD
                //1920, 1080 //full HD
                );
        setSize(d);

        ogreView = new OgreAwt( new Line());
        ogreView.setPreferredSize(d);
        add(ogreView);
        if(buttons>0){
            boolean buttonAdded = false;
            javax.swing.JPanel buttonsPanel = new javax.swing.JPanel();
            buttonsPanel.setBackground(this.getBackground());
            if((buttons & BUTTON_WIREFRAME) != 0){
                final javax.swing.JButton wireframebutton = new javax.swing.JButton("Wireframe");
                wireframebutton.addActionListener(new ActionListener() {
                    int wireframe=0;
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        wireframe=(wireframe+1)%3;
                        switch (wireframe){
                            case 0 :
                                getCamera().getOgreCamera().setPolygonMode_PM_SOLID();
                                wireframebutton.setText("Wireframe");
                                break;
                            case 1 :
                                getCamera().getOgreCamera().setPolygonMode_PM_WIREFRAME();
                                wireframebutton.setText("Points");
                                break;
                            case 2 :
                                getCamera().getOgreCamera().setPolygonMode_PM_POINTS();
                                wireframebutton.setText("Solid");
                                break;

                        }
                    }
                });
                buttonsPanel.add(wireframebutton);
                buttonAdded = true;
            }
            if(buttonAdded) {
                add(buttonsPanel, BorderLayout.SOUTH);
            }
        }
        pack();
    }

    @Override
    public void dispose() {
        if(getCamera() != null) {
            getCamera().getMic().stopPlaying();
        }
        if(ogreView.isPrimary()){
            setVisible(false);
            Ogre.dontDelete(this);
            ReflectUtilities.unregister(this);
        }
        else{
            super.dispose();
        }
    }


    public void setEnvironment(Environment env){
        // copy the environment
        /*AllObj = env;
        // create a Leaf where put the info abou the camra position, id and orientation
        Leaf Cam = new Leaf();
        //id
        Cam.setIdentifier("Camera");
        Cam.setReference("Camera");

        TreeNode camer = new TreeNode();
        //position
        camer.setCoordinates(new Vec3d(ogreView.getCameraDefaultPosX(), ogreView.getCameraDefaultPosY(),ogreView.getCameraDefaultPosZ()));
        //orientation
        camer.setOrientation(ogreView.getCameraDefaultPitch(), ogreView.getCameraDefaultYaw(), ogreView.getCameraDefaultRoll());
        // scale
        camer.setScale(1.0, 1.0, 1.0);

        Cam.setParent(camer);
        env.addLeaf(Cam);//addNode((Node) Cam);*/

        ogreView.setEnvironment(env);
        //env.setCameraInfo(this);
        ogreView.repaint();
    }


    @Override
    public Camera getCamera() {
        return ogreView.getCamera();
    }

    public double getCameraPositionX(){
        if(ogreView.getCamera()!=null) {
            return ogreView.getCamera().getTranslationNode().getPosition().getx();
        }
        else {
            return ogreView.getCameraDefaultPosX();
        }
    }

    public double getCameraPositionY(){
        if(ogreView.getCamera()!=null) {
            return ogreView.getCamera().getTranslationNode().getPosition().gety();
        }
        else {
            return ogreView.getCameraDefaultPosY();
        }
    }

    public double getCameraPositionZ(){
        if(ogreView.getCamera()!=null) {
            return ogreView.getCamera().getTranslationNode().getPosition().getz();
        }
        else {
            return ogreView.getCameraDefaultPosZ();
        }
    }

    public void setCameraPositionX(double value){
        ogreView.setCameraDefaultPosX(value);
    }
    public void setCameraPositionY(double value){
        ogreView.setCameraDefaultPosY(value);
    }
    public void setCameraPositionZ(double value){
        ogreView.setCameraDefaultPosZ(value);
    }


    public double getCameraPitch(){
        if(ogreView.getCamera()!=null) {
            return ogreView.getCamera().getPitch();
        }
        else {
            return ogreView.getCameraDefaultPitch();
        }
    }

    public double getCameraYaw(){
        if(ogreView.getCamera()!=null) {
            return ogreView.getCamera().getYaw();
        }
        else {
            return ogreView.getCameraDefaultYaw();
        }
    }

    public double getCameraRoll(){
        if(ogreView.getCamera()!=null) {
            return ogreView.getCamera().getRoll();
        }
        else {
            return ogreView.getCameraDefaultRoll();
        }
    }

    public void setCameraPitch(double value){
        ogreView.setCameraDefaultPitch(value);
    }
    public void setCameraYaw(double value){
        ogreView.setCameraDefaultYaw(value);
    }
    public void setCameraRoll(double value){
        ogreView.setCameraDefaultRoll(value);
    }

    @Override
    public void prepareCapture() {
        ogreView.prepareCapture();
    }

    @Override
    public int getCaptureWidth() {
        return ogreView.getCaptureWidth();
    }

    @Override
    public int getCaptureHeight() {
        return ogreView.getCaptureHeight();
    }

    @Override
    public boolean isSizeChanged() {
        return ogreView.isSizeChanged();
    }

    @Override
    public byte[] getCaptureData() {
        return ogreView.getCaptureData();
    }

    public OgreAwt getOgreView(){
        return ogreView;
    }
}
