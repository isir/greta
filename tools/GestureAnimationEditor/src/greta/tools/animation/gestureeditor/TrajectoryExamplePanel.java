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
package greta.tools.animation.gestureeditor;

import greta.core.util.math.Vec3d;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.ArrayList;

/**
 *
 * @author HUANG
 */
public class TrajectoryExamplePanel extends javax.swing.JPanel {

    int scale = 10;
    int originalX = 100;
    int originalY = 100;
    int axisLengthY = 100;
    int axisLengthX = 100;

    int axisLengthZ = 100;
    int originalZ = 300;
    ArrayList<Vec3d> pointlist = new ArrayList<Vec3d>();

    /**
     * Creates new form TrajectoryExamplePanel
     */
    public TrajectoryExamplePanel() {
        initComponents();
    }

    public void updatePointlist(ArrayList<Vec3d> points) {
        pointlist.clear();
        for (int i = 0 ; i < points.size(); ++i) {
            pointlist.add(
                    new Vec3d(
                    (points.get(i).x() * scale), (points.get(i).y() * scale) , (points.get(i).z() * scale)
                    )
                    );
            /*System.out.println(new Vec3d(
                    (points.get(i).x()), (points.get(i).y()) , (points.get(i).z())
                    ));*/
        }
        this.updateUI();
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        // Draw ordinate.
        drawAxis(g2);
        // Draw abcissa.

        // Mark data points.
        for (int i = 1; i < pointlist.size(); i++) {
            //float c = (float)(pointlist.get(i - 1).z() / scale + 20.0f)/ 40.0f;
            //System.out.println(c);
            if (i == 1) {
                g2.setPaint(Color.red);
                g2.setStroke(new BasicStroke(5F));
            } else {
                g2.setPaint(new Color(0, 1, 0));
                g2.setStroke(new BasicStroke(2F));
            }

            g2.draw(new Line2D.Double(
                    new Point( (int)(originalX + pointlist.get(i - 1).x()), (int)(originalY - pointlist.get(i - 1).y()))   ,
                    new Point( (int)(originalX + pointlist.get(i).x()), (int)(originalY - pointlist.get(i).y()))
                    ));

            g2.draw(new Line2D.Double(
                    new Point( (int)(originalZ + pointlist.get(i - 1).z()), (int)(originalY - pointlist.get(i - 1).y()))   ,
                    new Point( (int)(originalZ + pointlist.get(i).z()), (int)(originalY - pointlist.get(i).y()))
                    ));
        }
    }

    void drawAxis(Graphics2D g2) {
        g2.draw(new Line2D.Double(new Point(originalX, originalY), new Point(axisLengthX + originalX, originalY)));
        g2.drawString("x",axisLengthX + originalX, originalY + 8);
        for (int i = 1 ; i < 10; ++i) {
            g2.draw(new Line2D.Double(new Point(originalX + scale * i, originalY), new Point(originalX + scale * i, originalY - 5)));
        }
        g2.draw(new Line2D.Double(new Point(originalX, originalY), new Point(originalX, originalY - axisLengthY)));
        g2.drawString("y",originalX - 8, originalY - axisLengthY + 8);
        for (int i = 1 ; i < 10; ++i) {
            g2.draw(new Line2D.Double(new Point(originalX, originalY  - scale * i), new Point(originalX + 5, originalY  - scale * i)));
        }

        g2.draw(new Line2D.Double(new Point(originalZ, originalY), new Point(axisLengthZ + originalZ, originalY)));
        g2.drawString("z",axisLengthZ + originalZ, originalY + 8);
        for (int i = 1 ; i < 10; ++i) {
            g2.draw(new Line2D.Double(new Point(originalZ + scale * i, originalY), new Point(originalZ + scale * i, originalY - 5)));
        }
        g2.draw(new Line2D.Double(new Point(originalZ, originalY), new Point(originalZ, originalY - axisLengthY)));
        g2.drawString("y",originalZ - 8, originalY - axisLengthY + 8);
        for (int i = 1 ; i < 10; ++i) {
            g2.draw(new Line2D.Double(new Point(originalZ, originalY  - scale * i), new Point(originalZ + 5, originalY  - scale * i)));
        }

    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
