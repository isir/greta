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
package greta.tools.editors.aulibrary;

import greta.core.animation.mpeg4.fap.FAPType;
import greta.core.util.CharacterManager;
import greta.tools.editors.SliderAndText;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Andre-Marie Pez
 */
public class FacePanel extends JPanel{
    private static double radius = 0.01;
    private static double radius_sensibility = 0.005;
    private Face face;
    AffineTransform transform;

    private FapComponents current = null;
    private Color unselected = new Color(100,100,100, 200);
    private Color selected = Color.green;
    private Color innerMouthColor = new Color(150,50,50);
    private Color outerMouthColor = new Color(250,150,150);
    private Color eyesColor = new Color(250,250,250);
    private Color eyebrowsColor = new Color(120,75,0);
    private Color skinColor = new Color(255,200,180);

    private AULibraryEditor auEditor;

    public FacePanel(AULibraryEditor auEditor){
        face = new Face(this);
        transform = new AffineTransform();
        this.auEditor = auEditor;

        MouseAdapter mouseListener = new MouseAdapter() {

            int oldx = 0;
            int oldy = 0;
            @Override
            public void mousePressed(MouseEvent e) {

                int size = getDrawingSize();
                int xOffset = getXOffset(size);
                int yOffset = getYOffset(size);
                for(FapComponents fapComp : face.allCotrolPoints){
                    if(isInside(size, xOffset, yOffset, fapComp, e.getX(), e.getY())){
                        current = fapComp;
                        FacePanel.this.auEditor.setRightPanel(current.panel);
                        repaint();
                        putOld(e);
                        return ;
                    }
                }
                current = null;
                FacePanel.this.auEditor.setRightPanel(null);
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(current!=null){
                    double size = getDrawingSize();
                    if(current.horizontal!=null){
                        double dx = (e.getX()-oldx) / size / current.horizontal.unit * current.horizontal.axeSignum;
                        current.horizontal.value.slider.setValue(current.horizontal.value.slider.getValue()+(int)(dx+0.5));
                    }
                    if(current.vertical!=null){
                        double dy = (e.getY()-oldy) / size / current.vertical.unit * current.vertical.axeSignum;
                        current.vertical.value.slider.setValue(current.vertical.value.slider.getValue()+(int)(dy+0.5));
                    }
                    putOld(e);
                }
            }

            private void putOld(MouseEvent e){
                oldx = e.getX();
                oldy = e.getY();
            }
        };

        this.addMouseListener(mouseListener);
        this.addMouseMotionListener(mouseListener);
        this.addMouseWheelListener(mouseListener);
    }

    private int getDrawingSize(){
        return Math.min(getHeight(), getWidth());
    }

    private int getXOffset(int size){
        return (getWidth()-size)/2;
    }

    private int getYOffset(int size){
        return (getHeight()-size)/2;
    }

    @Override
    protected void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        int size = getDrawingSize();
        int xOffset = getXOffset(size);
        int yOffset = getYOffset(size);
        transform.setTransform(size, 0, 0, size, xOffset, yOffset);
        Graphics2D g = (Graphics2D)gr;
        drawHead(g);
        drawEyes(g);
        drawMouth(g);
        drawEyebrows(g);
        drawNose(g);

        for(FapComponents fapComp : face.allCotrolPoints){
            drawPoint(g, size, xOffset, yOffset, fapComp);
        }
        if(current!=null){
            drawSelectedPoint(g, size, xOffset, yOffset, current);
        }
    }

    private void drawHead(Graphics2D g){
        Shape neck = transform.createTransformedShape(face.neck.getShape());
        Shape head = transform.createTransformedShape(face.head.getShape());
        g.setColor(skinColor);
        g.fill(neck);
        g.setColor(Color.black);
        g.draw(neck);
        g.setColor(skinColor);
        g.fill(head);
        g.setColor(Color.black);
        g.draw(head);
    }

    private void drawEyes(Graphics2D g){
        Shape leftEye = transform.createTransformedShape(face.leftEye.getShape());
        Shape rightEye = transform.createTransformedShape(face.rightEye.getShape());
        g.setColor(eyesColor);
        g.fill(leftEye);
        g.fill(rightEye);
        g.setColor(Color.black);
        g.draw(leftEye);
        g.draw(rightEye);
    }
    private void drawMouth(Graphics2D g){
        Shape innerMouth = transform.createTransformedShape(face.innerMouth.getShape());
        Shape outerMouth = transform.createTransformedShape(face.outerMouth.getShape());
        g.setColor(outerMouthColor);
        g.fill(outerMouth);
        g.setColor(Color.black);
        g.draw(outerMouth);
        g.setColor(innerMouthColor);
        g.fill(innerMouth);
        g.setColor(Color.black);
        g.draw(innerMouth);
    }
    private void drawEyebrows(Graphics2D g){
        Shape brow = transform.createTransformedShape(face.brow.getShape());
        Shape leftEyebrow = transform.createTransformedShape(face.leftEyebrow.getShape());
        Shape rightEyebrow = transform.createTransformedShape(face.rightEyebrow.getShape());
        g.setColor(skinColor);
        g.fill(brow);
        g.setColor(eyebrowsColor);
        g.fill(leftEyebrow);
        g.fill(rightEyebrow);
        g.setColor(Color.black);
        g.draw(leftEyebrow);
        g.draw(rightEyebrow);
    }
    private void drawNose(Graphics2D g){
        Shape noseTrills = transform.createTransformedShape(face.nosetrils.getShape());
        Shape nose = transform.createTransformedShape(face.nose.getShape());
        g.setColor(Color.black);
        g.draw(noseTrills);
        g.setColor(skinColor);
        g.fill(nose);
        g.setColor(Color.black);
        g.draw(nose);
    }

    private void drawPoint(Graphics g, int size, int xOffset, int yOffset, FapComponents p){
        drawPoint(g, size, xOffset, yOffset, p, unselected);
    }

    private void drawSelectedPoint(Graphics g, int size, int xOffset, int yOffset, FapComponents p){
        drawPoint(g, size, xOffset, yOffset, p, selected);
    }

    private void drawPoint(Graphics g, int size, int xOffset, int yOffset, FapComponents p, Color c){
        g.setColor(c);
        g.fillOval(xOffset+(int)(size*(p.getX()-radius) + 0.5), yOffset+(int)(size*(p.getY()-radius) + 0.5), (int)(size*radius*2+0.5), (int)(size*radius*2+0.5));
        g.drawOval(xOffset+(int)(size*(p.getX()-radius) + 0.5), yOffset+(int)(size*(p.getY()-radius) + 0.5), (int)(size*radius*2+0.5), (int)(size*radius*2+0.5));
        g.setColor(Color.black);
        g.drawOval(xOffset+(int)(size*(p.getX()-radius) + 0.5), yOffset+(int)(size*(p.getY()-radius) + 0.5), (int)(size*radius*2+0.5), (int)(size*radius*2+0.5));
    }

    private boolean isInside(double size, double xOffset, double yOffset, FapComponents p, double posx, double posy){
        double dx = (posx-xOffset) - p.x*size;
        double dy = (posy-yOffset) - p.y*size;
        return Math.sqrt(dx*dx + dy*dy) <= (radius+radius_sensibility)*size;
    }

    public void updateFrame(FAPType fapType, int value){
        repaint();
        auEditor.updateFrame(fapType, value);
    }

    public SliderAndText[] getSliders(){
        return face.fapMapping;
    }

    public static void main(String [] ss){
        JFrame jf = new AULibraryEditor(CharacterManager.getStaticInstance());
        jf.setSize(1000, 1000);
        jf.setVisible(true);
    }

    public Face getFace() {
        return face;
    }
}
