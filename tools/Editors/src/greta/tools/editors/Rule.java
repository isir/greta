/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.tools.editors;

import greta.core.util.time.Temporizable;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

/**
 *
 * @author Andre-Marie
 */
public class Rule<T extends Temporizable> extends TimeLine<T>{

    private static final int MIN_GRAD_LENGTH = 15;
    private static final int SHORT_GRAD_SIZE = 5;
    private static final int MIDDLE_GRAD_SIZE = 10;
    private static final int LONG_GRAD_SIZE = 15;

    private static final double[] levels = {
        0.01, 0.05,
        0.1, 0.5, 1, 5, 10, 30, 60, 300, 600, 1800, 3600};

    public Rule(){
        this.removeMouseListener(this);
        this.removeMouseMotionListener(this);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);

        int maxGradNumber = getWidth()/MIN_GRAD_LENGTH;
        int level = 0;
        while(level<levels.length-2 && getTotalDuration()/levels[level]>maxGradNumber)level++;
        for(double time=0; time<getTotalDuration(); time+=levels[level])drawShortGrad(g, time);
        for(double time=0; time<getTotalDuration(); time+=levels[level+1])drawMiddleGrad(g, time);
        for(double time=0; time<getTotalDuration(); time+=levels[level+2])drawLongGrad(g, time);

    }

    private void drawShortGrad(Graphics g, double time){
        drawGrad(g, time, SHORT_GRAD_SIZE,false);
    }
    private void drawMiddleGrad(Graphics g, double time){
        drawGrad(g, time, MIDDLE_GRAD_SIZE, false);
    }
    private void drawLongGrad(Graphics g, double time){
        drawGrad(g, time, LONG_GRAD_SIZE, true);
    }

    private void drawGrad(Graphics g, double time, int size, boolean labeled){
        int x = this.getPosOf(time);
        g.drawLine(x, getHeight()-size, x, getHeight());
        if(labeled)
            g.drawString(""+time, x+1, getHeight()-size);
    }

    @Override
    protected TemporizableContainer<T> instanciateTemporizable(double startTime, double endTime) {return null;}

    @Override
    protected String getDescription(TemporizableContainer<T> temporizableContainer, FontMetrics metrics, int limitSize){return null;}

    @Override
    protected TemporizableContainer<T> editTemporizable(TemporizableContainer<T> temporizableContainer) {return null;}

    @Override
    public void keyTyped(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void keyReleased(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
