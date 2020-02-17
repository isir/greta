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
package greta.auxiliary.socialparameters.gui;

import greta.auxiliary.socialparameters.SocialDimension;
import greta.auxiliary.socialparameters.SocialParameterFrame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

/**
 *
 * @author Andre-Marie Pez
 */
public class InterpersonalCircumplexPanel extends JPanel {

    private static int margin = 10;
    private static BasicStroke big = new BasicStroke(2);
    private static BasicStroke lite = new BasicStroke(1);
    private static int pointRadius = 8;

    private SocialParameterFrame frame = null;
    private Color spaceColor = Color.lightGray;
    private Color circColor = Color.white;
    private Color lineColor = Color.black;
    private Color valLineColor = Color.red;
    private Color valPointColor = Color.black;

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        int h = this.getHeight();
        int w = this.getWidth();

        Graphics2D g2d = (Graphics2D)(g.create());
        double ovalx = Math.max( (w-h) / 2.0, 0) + margin;
        double ovaly = Math.max( (h-w) / 2.0, 0) + margin;
        double radius =  Math.min(w, h) / 2.0 - margin;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        Ellipse2D circumplex = new Ellipse2D.Double(ovalx, ovaly, radius * 2, radius * 2);
        Rectangle2D space = new Rectangle2D.Double(ovalx, ovaly, radius * 2, radius * 2);
        Shape oldClip = g2d.getClip();

        g2d.setColor(spaceColor);
        g2d.fill(space);
        g2d.setColor(lineColor);
        g2d.draw(space);

        g2d.setClip(space);


        g2d.setStroke(big);

        g2d.setColor(circColor);
        g2d.fill(circumplex);
        g2d.setColor(lineColor);
        g2d.draw(circumplex);
        g2d.setClip(circumplex);

        g2d.setStroke(lite);
        g2d.drawLine(0, h/2, w, h/2);
        g2d.drawLine(w/2, 0, w/2, h);

        if(frame == null){
            return ;
        }
        int dominance = (int)(h/2.0 - frame.getDoubleValue(SocialDimension.Dominance) * radius + 0.5);
        int liking = (int)(frame.getDoubleValue(SocialDimension.Liking) * radius + w/2.0 + 0.5);
        g2d.setStroke(big);
        g2d.setColor(valLineColor);
        if( ! frame.isInvalid(SocialDimension.Liking)){
            if( ! frame.isInvalid(SocialDimension.Dominance)){
                g2d.drawLine(w/2, h/2, liking, dominance);
                g2d.setClip(oldClip);
                g2d.setColor(valPointColor);
                g2d.fillOval(liking-pointRadius/2, dominance-pointRadius/2, pointRadius, pointRadius);
            }
            else{
                g2d.drawLine(liking, 0, liking, h);
            }
        }
        else{
            if( ! frame.isInvalid(SocialDimension.Dominance)){
                g2d.drawLine(0, dominance, w, dominance);
            }
        }

    }

    public double getLikingAt(int x){
        return (x - getWidth()/2.0)/ (Math.min(getWidth(), getHeight()) / 2.0 - margin);
    }

    public double getDominanceAt(int h){
        return (getHeight()/2.0 - h)/ (Math.min(getWidth(), getHeight()) / 2.0 - margin);
    }


    public void setSocialParameterFrame(SocialParameterFrame spf){
        frame = spf;
        repaint();
    }

    public static void main(String[] ss){
        InterpersonalCircumplexFrame.main(ss);
    }
}
