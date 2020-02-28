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
package greta.core.utilx.gui;

import greta.core.util.math.Function;
import greta.core.util.math.Functions;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 *
 * @author Andre-Marie Pez
 */
public class FunctionGraph extends javax.swing.JPanel {

    //formating constantes
    public static final int LEGEND_NONE = 0;
    public static final int LEGEND_TOP_LEFT = 1;
    public static final int LEGEND_TOP_RIGHT = 2;
    public static final int LEGEND_BOTTOM_LEFT = 3;
    public static final int LEGEND_BOTTOM_RIGHT = 4;


    //current space
    private double scaleX = 1;
    private double scaleY = 1;
    private double minX = -1;
    private double minY = -1;
    private double maxX = 1;
    private double maxY = 1;

    //formating
    private int textMargin = 8;
    private int legendMargin = 10;
    private int legendPosition = LEGEND_BOTTOM_RIGHT; //default value

    //for x axis
    private DecimalFormat xFormat = new DecimalFormat();
    private double xInterval;
    //for y axis
    private DecimalFormat yFormat = new DecimalFormat();
    private double yInterval;

    private ArrayList<Plot> plots = new ArrayList<Plot>();

    int step = 1;//px

    /**
     *
     * @param position one of LEGEND_NONE, LEGEND_TOP_LEFT, LEGEND_TOP_RIGHT, LEGEND_BOTTOM_LEFT and LEGEND_BOTTOM_RIGHT.
     */
    public void setLegendPosition(int position){
        legendPosition = position;
        repaint();
    }

    public void setRange(double xmin, double ymin, double xmax, double ymax) {
        _setRange(xmin, ymin, xmax, ymax);
        scaleChanged();
        repaint();
    }
    private void _setRange(double xmin, double ymin, double xmax, double ymax) {
        minX=xmin;
        minY=ymin;
        maxX=xmax;
        maxY=ymax;
    }

    private void scaleChanged(){
        int log = (int)(Math.log10((maxX-minX)/20));
        xInterval = Math.pow(10, log);
        if(log<0) {
            xFormat.setMaximumFractionDigits(Math.abs(log));
        }
        else{
            xFormat.setMaximumFractionDigits(0);
        }

        log = (int)(Math.log10((maxY-minY)/20));
        yInterval = Math.pow(10, log);
        if(log<0) {
            yFormat.setMaximumFractionDigits(Math.abs(log));
        }
        else{
            yFormat.setMaximumFractionDigits(0);
        }
    }
    private class Plot{
        private int sampleLength = 20;
        public Plot(Color color, Function function, String title){
            this.color = color;
            this.function = function;
            this.title = title;
        }
        public Plot(Color color, Function function){
            this(color,function,""+function);
        }
        Color color;
        Function function;
        String title;
        public void draw(Graphics g){
            g.setColor(color);
            double lastValue = function.f(getValueWidth(0));
            for(int w=step; w<getWidth(); w+=step){
                double nextValue = function.f(getValueWidth(w));
                if(!Double.isNaN(lastValue) && !Double.isNaN(nextValue)) {
                    g.drawLine(w-step, getPxHeight(lastValue), w, getPxHeight(nextValue));
                }
                lastValue = nextValue;
            }
        }
        public void title(Graphics g, int posX, int posY){
            //draw sample
            g.setColor(color);
            g.drawLine(posX, posY-g.getFontMetrics().getHeight()/3, posX+sampleLength, posY-g.getFontMetrics().getHeight()/3);
            //draw title
            g.setColor(Color.black);
            g.drawString(title, posX+sampleLength+textMargin, posY);
        }

        public int getTitleWidth(Graphics g){
            return g.getFontMetrics().stringWidth(title)+sampleLength+textMargin;
        }
    }


    public void plot(Function f, Color c){
        if(f!=null) {
            plots.add(new Plot(c, f));
        }
    }
    public void plot(Function f, Color c, String title){
        if(f!=null) {
            plots.add(new Plot(c, f, title));
        }
    }

    /**
     * Creates new form FunctionGraph
     */
    public FunctionGraph() {

        initComponents();
        this.setDoubleBuffered(true);
        MouseAdapter mouse = new MouseAdapter() {

            Point currentPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                currentPoint = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point move = e.getPoint();
                double deltaX = getValueWidth(currentPoint.x)-getValueWidth(move.x);
                double deltaY = getValueHeight(currentPoint.y)-getValueHeight(move.y);
                currentPoint = move;
                _setRange(minX + deltaX, minY + deltaY, maxX + deltaX, maxY + deltaY);
                repaint();
            }


            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double scale = 1.0+e.getPreciseWheelRotation()/10.0;
                double xCenter = getValueWidth(e.getPoint().x);
                double yCenter = getValueHeight(e.getPoint().y);

                _setRange((minX-xCenter)*scale + xCenter,
                        (minY-yCenter)*scale + yCenter,
                        (maxX-xCenter)*scale + xCenter,
                        (maxY-yCenter)*scale + yCenter);
                scaleChanged();
                repaint();
            }
        };
        this.addMouseListener(mouse);
        this.addMouseMotionListener(mouse);
        this.addMouseWheelListener(mouse);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
            }
        });
    }

    private void drawXaxis(Graphics g){
        int fontHeight = g.getFontMetrics().getHeight();
        int posX0 = getPxHeight(0);
        if(0<=posX0 && posX0<=getHeight()-fontHeight-textMargin){
            g.setColor(Color.BLACK);
        }
        else{
            g.setColor(Color.lightGray);
            posX0 = getHeight()-fontHeight-textMargin;
        }
        g.drawLine(0, posX0, this.getWidth(), posX0);


        double first = minX-minX%xInterval;
        int written = -1;
        for(double label = first; label< maxX; label+=xInterval){
            int px = getPxWidth(label);
            if(px>written){
                g.drawLine(px, posX0, px, posX0+4);
                String stringLabel = xFormat.format(label);
                g.drawString(stringLabel, px, posX0+fontHeight);
                written = px+g.getFontMetrics().stringWidth(stringLabel)+textMargin;
            }
            else{
                g.drawLine(px, posX0, px, posX0+2);
            }
        }
    }

    private void drawYaxis(Graphics g){

        int fontWidth = 0;
        double first = maxY-maxY%yInterval;
        for(double label = first; label>= minY; label-=yInterval){
            fontWidth = Math.max(fontWidth, g.getFontMetrics().stringWidth(yFormat.format(label)));
        }


        int posY0 = getPxWidth(0);
        if(fontWidth+textMargin<=posY0 && posY0<=getWidth()){
            g.setColor(Color.BLACK);
        }
        else{
            g.setColor(Color.lightGray);
            posY0 = fontWidth+textMargin;
        }
        g.drawLine(posY0, 0, posY0, getHeight());

        int written = -1;
        int fontHeight = g.getFontMetrics().getHeight();
        for(double label = first; label>= minY; label-=yInterval){
            int px = getPxHeight(label);
            if(px>written){
                g.drawLine(posY0, px, posY0-4, px);
                String stringLabel = yFormat.format(label);
                g.drawString(stringLabel, posY0-g.getFontMetrics().stringWidth(stringLabel)-4, px+fontHeight/3);
                written = px+g.getFontMetrics().getHeight()+textMargin;
            }
            else{
                g.drawLine(posY0, px, posY0-2, px);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawXaxis(g);
        drawYaxis(g);

        int maxTitleWidth = 0;
        int count = 0;
        for(Plot pl : plots){
            if(pl!=null){
                pl.draw(g);
                maxTitleWidth = Math.max(maxTitleWidth, pl.getTitleWidth(g));
                count++;
            }
        }

        if(legendPosition!=0){
            if(maxTitleWidth>0){
                int legendWidth = maxTitleWidth+textMargin*2;
                int legendHeight = count*(g.getFontMetrics().getHeight()+textMargin)+textMargin;

                int xLegend = legendMargin; //Left
                int yLegend = legendMargin; //Top
                if(legendPosition==LEGEND_BOTTOM_LEFT || legendPosition==LEGEND_BOTTOM_RIGHT){
                    yLegend = Math.max(legendMargin, getHeight()-legendHeight-legendMargin);
                }
                if(legendPosition==LEGEND_TOP_RIGHT || legendPosition==LEGEND_BOTTOM_RIGHT){
                    xLegend = Math.max(legendMargin, getWidth()-legendWidth-legendMargin);
                }

                g.setColor(new Color(255,255,255,150));
                g.fillRect(xLegend, yLegend, legendWidth, legendHeight);
                g.setColor(Color.black);
                g.drawRect(xLegend, yLegend, legendWidth, legendHeight);

                count = 1;
                for(Plot pl : plots){
                    if(pl!=null){
                        int posx = xLegend+textMargin;
                        int posY = yLegend+(count++)*(textMargin+g.getFontMetrics().getHeight());
                        pl.title(g, posx, posY);
                    }
                }
            }

        }
    }

    private double getValueHeight(int pxH){
        return Functions.changeInterval(pxH, 0, getHeight(), maxY, minY);
    }

    private double getValueWidth(int pxW){
        return Functions.changeInterval(pxW, 0, getWidth(), minX, maxX);
    }

    private int getPxHeight(double val){
        return (int)Functions.changeInterval(val, minY, maxY, getHeight(), 0);
    }

    private int getPxWidth(double val){
        return (int)Functions.changeInterval(val, minX, maxX, 0, getWidth());
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
