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
package greta.tools.editors;

import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import greta.tools.editors.bml.timelines.SpeechSignalTimeLine;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author Andre-Marie
 */
public abstract class TimeLine<T extends Temporizable>
extends JPanel
implements MouseListener, MouseMotionListener, FocusListener, KeyListener{

    //Number of digits for ID displayed in Editor
    protected static int NUM_DISPLAYIED_DIGITS_ID = 3;

    public static final int NOTHING_MODE = 0;
    public static final int ADD_MODE = 1;
    public static final int MOVE_MODE = 2;
    public static final int RESIZE_MODE = 3;
    private static final int alpha = 100;
    private static int pixSensibility = 2;

    private double totalDuration=10; //total duration of this timeLine in seconds
    private List<TemporizableContainer<T>> items = new ArrayList<TemporizableContainer<T>>();
    private TemporizableContainer<T> selected = null;
    private double startSelected = 0;
    private double endSelected = 0;
    private int mode = NOTHING_MODE;

    private Color strokeColor = new Color(0xcc,0xcc,0xff);
    private Color fillColor = new Color(0xcc,0xcc,0xff,alpha);
    private static Color xorColor = new Color(0xcc,0xcc,0xff);
    protected TimeLineManager<T> manager;
    protected MultiTimeLineEditors<? extends Temporizable> multiTimeLineEditor;

    public TimeLine(){
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addFocusListener(this);
        this.addKeyListener(this);
        this.setBackground(Color.white);
    }

    public TimeLine(MultiTimeLineEditors<? extends Temporizable> anEditor){
        this.multiTimeLineEditor = anEditor;
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addFocusListener(this);
        this.addKeyListener(this);
        this.setBackground(Color.white);
    }
    /**
     * Set the total duration of this {@code TimeLine} in seconds
     * @param totalDuration the total duration in seconds
     */
    public void setTotalDuration(double totalDuration) {
        this.totalDuration = totalDuration;
        repaint();
    }

    public void setManager(TimeLineManager<T> aManager) {
       this.manager = aManager;
    }

    public void setColor(Color c){
        strokeColor = c;
        fillColor = new Color(strokeColor.getRed(),strokeColor.getGreen(),strokeColor.getBlue(),alpha);
        repaint();
    }

    public void clear(){
        items.clear();
        mode = NOTHING_MODE;
        setSelected(null);
        this.setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    public Color getColor(){
        return strokeColor;
    }

    /**
     * Returns the total duration of this {@code TimeLine} in seconds
     * @return the total duration in seconds
     */
    public double getTotalDuration() {
        return totalDuration;
    }

    public List<TemporizableContainer<T>> getItems() {
        return items;
    }

    public List<String> getItemsNamesList(){
        List<String> output = new ArrayList<String>();
        for(TemporizableContainer<T> tmp : this.getItems()){
            output.add(tmp.getId());
        }
        return output;
    }

    private Comparator<TemporizableContainer<T>> comp = new Comparator<TemporizableContainer<T> >(){
        @Override
        public int compare(TemporizableContainer<T>  o1, TemporizableContainer<T>  o2) {
            double s1 = o1.getStart().getValue();
            double s2 = o2.getStart().getValue();
            if(s1>s2)return 1;
            if(s1<s2)return -1;
            return 0;
        }
    };
    private void sort(){
        Collections.sort(items,comp);
    }
    public void add(TemporizableContainer<T> temporizable){
        if(temporizable != null){
            items.add(temporizable);
            sort();
        }
    }

    protected double getTimeAt(int x){
        return totalDuration*x/this.getWidth();
    }

    protected int getPosOf(double time){
        return (int)(this.getWidth()/totalDuration*time+0.5);
    }

    protected TemporizableContainer<T> getItemAt(int x){
        double time = getTimeAt(x);
        TemporizableContainer<T> item = null;
        for(TemporizableContainer<T> temporizable : items){
            TimeMarker start = temporizable.getStart();
            TimeMarker end = temporizable.getEnd();
            if(start!=null && start.isConcretized() && start.getValue()<=time &&
               end!=null   && end.isConcretized()   && end.getValue()>=time)
                item = temporizable;
        }
        return item;
    }

    private void setSelected(TemporizableContainer<T> t){
        selected = t;
        if(selected!=null){
            startSelected = selected.getStart().getValue();
            endSelected = selected.getEnd().getValue();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int h = getHeight()-1;
        int y = h/2;
        g.setColor(Color.black);
        g.drawLine(0, 0, 0, h);
        for(TemporizableContainer<T> t : items){
            int x = getPosOf(t.getStart().getValue());
            int w = getPosOf(t.getEnd().getValue()) - x;
            if(t.isReferenceDeleted()){
                g.setColor(Color.RED);
                g.fillRect(x, 0, w, h);
            } else if (t.isReferenceModified()){
                g.setColor(Color.YELLOW);
                g.fillRect(x, 0, w, h);
            } else {
                g.setColor(fillColor);
                g.fillRect(x, 0, w, h);
            }
            g.setColor(strokeColor);
            g.drawRect(x, 0, w, h);
            for(TimeMarker tm : t.getTimeMarkers()){
                if(tm.isConcretized()){
                    int tmpos = getPosOf(tm.getValue());
                    g.setColor(strokeColor.darker().darker());
                    g.drawLine(tmpos, y, tmpos, h);
                    //Trying to have a bold font for TimeMarkers
                    g.setFont(new Font("default", Font.BOLD, (int) g.getFont().getSize2D()));
                    g.drawString(tm.getName(), tmpos+2, h-2);
                }
            }
            String description = getDescription(t,g.getFontMetrics(), w-10);
            g.setColor(this.getForeground());
            //Setting the font back to normal
            g.setFont(new Font("default", Font.PLAIN, (int) g.getFont().getSize2D()));
            if(description!=null && !(this instanceof SpeechSignalTimeLine)){
                g.drawString(description, x+5, y);
            }
            repaint();
        }
        if(selected!=null){
            int x = getPosOf(startSelected);
            int w = getPosOf(endSelected) - x;
            g.setColor(fillColor);
            g.fillRect(x, 0, w, h);
            g.fillRect(x, 0, w, h);
            g.setColor(this.getForeground());
            g.drawRect(x, 0, w, h);
            String description = getDescription(selected,g.getFontMetrics(), w-10);
            if(description!=null && !(this instanceof SpeechSignalTimeLine)){
                g.drawString(description, x+5, y);
            }
        }
    }
    protected boolean isGoodSize(String s, FontMetrics metrics, int limitSize){
        return metrics.stringWidth(s)<limitSize;
    }
    protected abstract TemporizableContainer<T> instanciateTemporizable(double startTime, double endTime);
    protected abstract String getDescription(TemporizableContainer<T> temporizable, FontMetrics metrics, int limitSize);
    protected TemporizableContainer<T> editTemporizable(TemporizableContainer<T> temporizableContainer){return temporizableContainer;}


    private int intitialX=0;
    private int intitialY=0;
    private int lastX=0;
    private int lastY=0;

    @Override
    public void mousePressed(MouseEvent e) {
        this.requestFocusInWindow();
        intitialX = e.getX();
        intitialY = e.getY();
        lastX = e.getX();
        lastY = e.getY();
        if(e.getButton() == MouseEvent.BUTTON1){
            if(e.getClickCount()==2){
                setSelected(getItemAt(intitialX));
                mode = MOVE_MODE;
                repaint();
                //drawXorRectSelectedMoves();
            }
            if(e.getClickCount()==1){
                if(mode==NOTHING_MODE){
                    mode = ADD_MODE;
                    drawXorRect();
                }
                if(mode == RESIZE_MODE){
                    drawXorRect();
                }
                if(mode == MOVE_MODE){
                    //drawXorRectSelectedMoves();
                }
                //Better experience : with 1 click an element can be selected, moved, deleted.
                if(getItemAt(intitialX) !=null){
                    setSelected(getItemAt(intitialX));
                    repaint();
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();
        if(mode == ADD_MODE){
            drawXorRect();
            if(Math.abs(lastX-intitialX)>pixSensibility){
                double initialtime = getTimeAt(intitialX);
                double lasttime = getTimeAt(lastX);
                TemporizableContainer<T> newT = instanciateTemporizable(Math.min(initialtime, lasttime), Math.max(initialtime, lasttime));
                add(newT);
                setSelected(newT);
            }
            mode = NOTHING_MODE;
        }

        drawXorRect();
        if(selected != null && Math.abs(lastX-intitialX)>pixSensibility){
            int cursorType = this.getCursor().getType();
            if(cursorType==Cursor.E_RESIZE_CURSOR){//start
                startSelected = getTimeAt(lastX);
                if(startSelected>endSelected)
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            }
            else{
                if(cursorType==Cursor.W_RESIZE_CURSOR){//end
                    endSelected = getTimeAt(lastX);
                    if(startSelected>endSelected)
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                }
            }
            changeTimeOfSelected(startSelected, endSelected);
        }

        if(mode == MOVE_MODE){

            if(Math.abs(lastX-intitialX)>pixSensibility){
                double timeOffset = getTimeAt(lastX-intitialX);
                changeTimeOfSelected(startSelected+timeOffset, endSelected+timeOffset);

            }
            else{
                TemporizableContainer<T> old = selected;
                if(old!=null){
                    TemporizableContainer<T> edited = editTemporizable(old);
                    setSelected(edited);
                    if(edited==null || edited!=old){
                        this.items.remove(old);
                        add(edited);
                    }
                }
            }
        }
        repaint();
    }

    private void changeTimeOfSelected(double start, double end){
        if(selected!=null){
            double min = Math.min(start, end);
            double max = Math.max(start, end);
            startSelected = selected.getStart().getValue();
            endSelected = selected.getEnd().getValue();
            for(TimeMarker tm : selected.getTimeMarkers()){
                if(tm.isConcretized()){
                    tm.setValue((tm.getValue()-startSelected)/(endSelected-startSelected)*(max-min)+min);
                }
            }
            startSelected = min;
            endSelected = max;
            sort();

            for(String linkedID : selected.getLinkedSignal()){
                if(this.multiTimeLineEditor != null){

                    for(TemporizableContainer tmp : this.multiTimeLineEditor.getAllTemporizableContainers()){
                        if(linkedID.equals(tmp.getId())){
                            tmp.setReferencesState(TemporizableContainer.ReferencesState.CHANGED);
                        }
                    }
                }
            }
        }
    }
    private void drawXorRect(){
        drawXorRect(Math.min(intitialX,lastX), 0, Math.abs(lastX-intitialX), getHeight());
    }
    private void drawXorRectSelectedMoves(){
        int xoffset = lastX-intitialX;
        int xs = getPosOf(startSelected);
        drawXorRect(xs+xoffset, 0, getPosOf(endSelected)-xs,getHeight());
    }
    private void drawXorRect(int x, int y, int w, int h){
        Graphics g = this.getGraphics();
        g.setXORMode(xorColor);
        g.fillRect(x, y, w, h);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(mode == ADD_MODE || mode == RESIZE_MODE){
            drawXorRect();
            lastX = e.getX();
            lastY = e.getY();
            drawXorRect();
        }
        if(mode==MOVE_MODE){
            drawXorRectSelectedMoves();
            lastX = e.getX();
            lastY = e.getY();
            drawXorRectSelectedMoves();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if(selected!=null){
            int xs = getPosOf(startSelected);
            int xe = getPosOf(endSelected);
            int xmin = e.getX()-pixSensibility;
            int xmax = e.getX()+pixSensibility;
            if(xs>xmin && xs<xmax){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                // Forbid Speech to be resizable
                if(! (this instanceof SpeechSignalTimeLine))
                    mode = RESIZE_MODE;
            }
            else{
                if(xe>xmin && xe<xmax){
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                    // Forbid Speech to be resizable
                    if(! (this instanceof SpeechSignalTimeLine))
                        mode = RESIZE_MODE;
                }
                else{
                    if(xs<xmin && xmax<xe){
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        mode = MOVE_MODE;
                    }
                    else{
                        this.setCursor(Cursor.getDefaultCursor());
                        mode = NOTHING_MODE;
                    }
                }
            }
        }
        else{
            mode = NOTHING_MODE;
        }
    }

    @Override
    public void focusLost(FocusEvent e){
        setSelected(null);
        this.setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void focusGained(FocusEvent e){}

    @Override
    public void keyPressed(KeyEvent e){
        if (e.getKeyCode() == KeyEvent.VK_DELETE ) {

            for(String signal : selected.getLinkedSignal()){
                if(this.multiTimeLineEditor != null){
                    for(TemporizableContainer tmp : this.multiTimeLineEditor.getAllTemporizableContainers()){
                        if(signal.equals(tmp.getId().toString())){
                            tmp.setReferencesState(TemporizableContainer.ReferencesState.REMOVED);
                        }
                    }
               }
           }
            this.items.remove(selected);
            mode = NOTHING_MODE;
            setSelected(null);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
