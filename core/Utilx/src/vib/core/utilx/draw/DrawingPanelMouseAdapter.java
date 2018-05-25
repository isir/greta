/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.utilx.draw;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import javax.swing.SwingUtilities;

/**
 *
 * @author Ken Prepin
 */
public class DrawingPanelMouseAdapter extends MouseAdapter {

    protected DrawingPanel drawingPanel;

    public DrawingPanelMouseAdapter(DrawingPanel drawingPanel) {
        this.drawingPanel = drawingPanel;
        drawingPanel.addMouseListener(this);
        drawingPanel.addMouseMotionListener(this);
        drawingPanel.addMouseWheelListener(this);
    }

    public DrawingPanel getPanel() {
        return drawingPanel;
    }


    public MouseEvent transformEvent(MouseEvent e){
        Point2D point;
        try {
            point = drawingPanel.transform.inverseTransform(e.getPoint(), null);
        } catch (NoninvertibleTransformException ex) {
           return e;
        }
        MouseEvent newEvent = new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (int)point.getX(), (int)point.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
        return newEvent;
    }


    public MouseWheelEvent transformWheelEvent(MouseWheelEvent e){
        Point2D point;
        try {
            point = drawingPanel.transform.inverseTransform(e.getPoint(), null);
        } catch (NoninvertibleTransformException ex) {
           return e;
        }
        MouseWheelEvent newEvent = new MouseWheelEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), (int)point.getX(), (int)point.getY(), e.getClickCount(), e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(), e.getWheelRotation());
        return newEvent;
    }

    @Override
    public final void mouseClicked(MouseEvent e) {
        // System.out.println("mouseClicked");

        if (SwingUtilities.isLeftMouseButton(e)) {
            leftClickAction(transformEvent(e));
        } else {
            rightClickAction(transformEvent(e));
        }
    }

    @Override
    public final void mouseDragged(MouseEvent e) {
        doMouseDragged(transformEvent(e));
    }

    @Override
    public final void mouseEntered(MouseEvent e) {
        doMouseEntered(transformEvent(e));
    }

    @Override
    public final void mouseExited(MouseEvent e) {
        doMouseExited(transformEvent(e));
    }

    @Override
    public final void mouseMoved(MouseEvent e) {
        doMouseMoved(transformEvent(e));
    }

    @Override
    public final void mousePressed(MouseEvent e) {
        doMousePressed(transformEvent(e));
    }

    @Override
    public final void mouseReleased(MouseEvent e) {
        doMouseReleased(transformEvent(e));
    }

    @Override
    public final void mouseWheelMoved(MouseWheelEvent e) {
        doMouseWheelMoved(transformWheelEvent(e));
    }




    protected void doMouseWheelMoved(MouseWheelEvent e){
    }


    protected void doMouseReleased(MouseEvent e){
    }

    protected void doMousePressed(MouseEvent e){
    }

    protected void doMouseMoved(MouseEvent e){
    }

    protected void doMouseExited(MouseEvent e){
    }

    protected void doMouseEntered(MouseEvent e){
    }

    protected void doMouseDragged(MouseEvent e){
    }

    protected void leftClickAction(MouseEvent e) {
    }

    protected void rightClickAction(MouseEvent e) {
    }
}
