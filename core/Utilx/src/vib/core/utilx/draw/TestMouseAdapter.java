/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.utilx.draw;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public class TestMouseAdapter extends DrawingPanelMouseAdapter {

    protected IMovableDrawable focusedDrawable;
    protected Point mousePosOnDrawable;

    public TestMouseAdapter(DrawingPanel drawingPanel) {
        super(drawingPanel);
        focusedDrawable = null;
        mousePosOnDrawable = new Point();
    }

    @Override
    protected void rightClickAction(MouseEvent e) {
        System.out.println("right click");
        List selectedDrawables = drawingPanel.findDrawables(e.getPoint());
        if (selectedDrawables.isEmpty()) {
            System.out.println("no drawable");
            return;
        }
        System.out.println("on a drawable");
        IDrawable drawable = (IDrawable) selectedDrawables.get(0);
        drawingPanel.removeDrawable(drawable);
    }

    @Override
    protected void leftClickAction(MouseEvent e) {
        Point p = e.getPoint();
        List selectedDrawables = drawingPanel.findDrawables(e.getPoint());
        if (selectedDrawables.isEmpty()) {
            System.out.println("no drawable");

            IMovableDrawable circle = createCircleDrawable(e);
            drawingPanel.addDrawable(circle);
            focusedDrawable = circle;

            return;
        } else {
            for (Object drawable : selectedDrawables) {
                if (drawable instanceof CircleDrawable) {
                    if (focusedDrawable == null) {
                        IMovableDrawable line = createLineDrawable(e);
                        drawingPanel.addDrawable(line);
                        focusedDrawable = line;
                    } else {
                        focusedDrawable = null;
                    }
                } else if (focusedDrawable instanceof LineDrawable) {
                    drawingPanel.removeDrawable(focusedDrawable);
                }
            }
        }
    }

    private IMovableDrawable createCircleDrawable(MouseEvent e) {
        Point center = e.getPoint();
        return new CircleDrawable(Color.RED, center, 25);
    }

    private IMovableDrawable createLineDrawable(MouseEvent e) {
        Point p = e.getPoint();
        Dimension dim = new Dimension(20, 20);
        return new LineDrawable(Color.BLACK, p, p);
    }

    @Override
    public void doMouseDragged(MouseEvent e) {
        if (focusedDrawable != null) {
            focusedDrawable.setPosition(e.getPoint());
            drawingPanel.repaint();
        }
    }

    @Override
    public void doMousePressed(MouseEvent e) {
        List selectedDrawables = drawingPanel.findDrawables(e.getPoint());
        if (selectedDrawables.isEmpty()) {
            return;
        }
        focusedDrawable = (IMovableDrawable) selectedDrawables.get(0);
        focusedDrawable.setMousePressedPosition(e.getPoint());
    }

    @Override
    public void doMouseMoved(MouseEvent e) {

        if (focusedDrawable instanceof LineDrawable) {
            // System.out.println("mouse moved at pos " + e.getX() + " " + e.getY());
            focusedDrawable.setPosition(e.getPoint());
            drawingPanel.repaint();
        }
    }

    @Override
    public void doMouseReleased(MouseEvent e) {
        if (focusedDrawable instanceof CircleDrawable) {
            focusedDrawable = null;
        }
    }
}
