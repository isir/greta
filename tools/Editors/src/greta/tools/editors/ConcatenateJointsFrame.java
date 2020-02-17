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

import greta.core.animation.mpeg4.bap.JointType;
import greta.core.animation.mpeg4.bap.filters.ConcatenateJoints;
import greta.core.utilx.draw.CircleDrawable;
import greta.core.utilx.draw.DiscDrawable;
import greta.core.utilx.draw.DrawingPanel;
import greta.core.utilx.draw.DrawingPanelMouseAdapter;
import greta.core.utilx.draw.IDrawable;
import greta.core.utilx.draw.LineArrow;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFrame;

/**
 *
 * @author Andre-Marie Pez
 */
public class ConcatenateJointsFrame extends JFrame implements ComponentListener {

    public static void main(String[] aaa) {
        ConcatenateJointsFrame frame = new ConcatenateJointsFrame();
        frame.setVisible(true);
    }

    private static Color selectedColor = new Color(0.6f, 0.4f, 1f, 1f);
    private static Color unselectedColor = new Color(0.7f, 0.7f, 0.8f, 0.7f);
    private static Color outlineColor = Color.darkGray;
    private static double radius = 20;
    private static float strokeSize = (float) (radius / 8);

    private MouseListener listener;
    private DrawingPanel panel;
    private HashMap<JointType, JointButton> buttons = new HashMap<JointType, JointButton>(JointType.NUMJOINTS);
    private ConcatenateJoints concat;

    public ConcatenateJointsFrame() {
        panel = new DrawingPanel();

        panel.addDrawable(new JointButton(new Point.Double(0, 0), JointType.HumanoidRoot));
        double step = -3 * radius;
        double left = -1;
        double right = 1;
        //spine :
        double i = 0.5;
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vl5));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vl4));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vl3));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vl2));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vl1));
        i += 0.5;
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vt12));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vt11));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vt10));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vt9));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vt8));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vt7));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vt6));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vt5));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vt4));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vt3));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vt2));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vt1));
        i += 0.5;
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vc7));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vc6));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vc5));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vc4));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vc3));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vc2));
        panel.addDrawable(new JointButton(new Point.Double(0, step * (++i)), JointType.vc1));
        i += 3;
        panel.addDrawable(new JointButton(new Point.Double(0, step * i), JointType.skullbase, radius * 6));

        //arms
        Point2D vt1 = buttons.get(JointType.vt1).getCenter();
        panel.addDrawable(new JointButton(new Point.Double(left * step * 1.3, vt1.getY() + step * 0.2), JointType.l_sternoclavicular));
        panel.addDrawable(new JointButton(new Point.Double(left * step * 3, vt1.getY() + step * 1.2), JointType.l_acromioclavicular));
        panel.addDrawable(new JointButton(new Point.Double(left * step * 4, vt1.getY() + step * 1.3), JointType.l_shoulder));
        panel.addDrawable(new JointButton(new Point.Double(left * step * 13, vt1.getY() + step * 1.3), JointType.l_elbow));
        panel.addDrawable(new JointButton(new Point.Double(left * step * 23, vt1.getY() + step * 1.3), JointType.l_wrist));

        panel.addDrawable(new JointButton(new Point.Double(right * step * 1.3, vt1.getY() + step * 0.2), JointType.r_sternoclavicular));
        panel.addDrawable(new JointButton(new Point.Double(right * step * 3, vt1.getY() + step * 1.2), JointType.r_acromioclavicular));
        panel.addDrawable(new JointButton(new Point.Double(right * step * 4, vt1.getY() + step * 1.3), JointType.r_shoulder));
        panel.addDrawable(new JointButton(new Point.Double(right * step * 13, vt1.getY() + step * 1.3), JointType.r_elbow));
        panel.addDrawable(new JointButton(new Point.Double(right * step * 23, vt1.getY() + step * 1.3), JointType.r_wrist));

        //hand
        Point2D wrist = buttons.get(JointType.l_wrist).getCenter();
        double palm = 1.5;
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm - 0.1), wrist.getY() + step * 2), JointType.l_thumb1));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 0.1), wrist.getY() + step * 3), JointType.l_thumb2));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 0.3), wrist.getY() + step * 4), JointType.l_thumb3));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * palm, wrist.getY() + step), JointType.l_index0));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 1.2), wrist.getY() + step * 1.1), JointType.l_index1));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 2.4), wrist.getY() + step * 1.2), JointType.l_index2));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 3.6), wrist.getY() + step * 1.3), JointType.l_index3));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * palm, wrist.getY()), JointType.l_middle0));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 1.5), wrist.getY()), JointType.l_middle1));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 3), wrist.getY()), JointType.l_middle2));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 4.5), wrist.getY()), JointType.l_middle3));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * palm, wrist.getY() - step), JointType.l_ring0));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 1.2), wrist.getY() - step * 1.1), JointType.l_ring1));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 2.4), wrist.getY() - step * 1.2), JointType.l_ring2));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 3.6), wrist.getY() - step * 1.3), JointType.l_ring3));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * palm, wrist.getY() - step * 2), JointType.l_pinky0));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 1), wrist.getY() - step * 2.2), JointType.l_pinky1));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 2), wrist.getY() - step * 2.4), JointType.l_pinky2));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + left * step * (palm + 3), wrist.getY() - step * 2.6), JointType.l_pinky3));

        wrist = buttons.get(JointType.r_wrist).getCenter();
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm - 0.1), wrist.getY() + step * 2), JointType.r_thumb1));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 0.1), wrist.getY() + step * 3), JointType.r_thumb2));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 0.3), wrist.getY() + step * 4), JointType.r_thumb3));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * palm, wrist.getY() + step), JointType.r_index0));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 1.2), wrist.getY() + step * 1.1), JointType.r_index1));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 2.4), wrist.getY() + step * 1.2), JointType.r_index2));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 3.6), wrist.getY() + step * 1.3), JointType.r_index3));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * palm, wrist.getY()), JointType.r_middle0));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 1.5), wrist.getY()), JointType.r_middle1));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 3), wrist.getY()), JointType.r_middle2));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 4.5), wrist.getY()), JointType.r_middle3));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * palm, wrist.getY() - step), JointType.r_ring0));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 1.2), wrist.getY() - step * 1.1), JointType.r_ring1));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 2.4), wrist.getY() - step * 1.2), JointType.r_ring2));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 3.6), wrist.getY() - step * 1.3), JointType.r_ring3));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * palm, wrist.getY() - step * 2), JointType.r_pinky0));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 1), wrist.getY() - step * 2.2), JointType.r_pinky1));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 2), wrist.getY() - step * 2.4), JointType.r_pinky2));
        panel.addDrawable(new JointButton(new Point.Double(wrist.getX() + right * step * (palm + 3), wrist.getY() - step * 2.6), JointType.r_pinky3));

        //legs
        panel.addDrawable(new JointButton(new Point.Double(0, -step * 1.5), JointType.sacroiliac));
        panel.addDrawable(new JointButton(new Point.Double(left * step * 3, -step * 2.3), JointType.l_hip));
        panel.addDrawable(new JointButton(new Point.Double(left * step * 3, -step * 15), JointType.l_knee));
        panel.addDrawable(new JointButton(new Point.Double(left * step * 3, -step * 26), JointType.l_ankle));
        panel.addDrawable(new JointButton(new Point.Double(left * step * 2.8, -step * 27.5), JointType.l_subtalar));
        panel.addDrawable(new JointButton(new Point.Double(left * step * 5, -step * 27.3), JointType.l_midtarsal));
        panel.addDrawable(new JointButton(new Point.Double(left * step * 7, -step * 27.5), JointType.l_metatarsal));

        panel.addDrawable(new JointButton(new Point.Double(right * step * 3, -step * 2.3), JointType.r_hip));
        panel.addDrawable(new JointButton(new Point.Double(right * step * 3, -step * 15), JointType.r_knee));
        panel.addDrawable(new JointButton(new Point.Double(right * step * 3, -step * 26), JointType.r_ankle));
        panel.addDrawable(new JointButton(new Point.Double(right * step * 2.8, -step * 27.5), JointType.r_subtalar));
        panel.addDrawable(new JointButton(new Point.Double(right * step * 5, -step * 27.3), JointType.r_midtarsal));
        panel.addDrawable(new JointButton(new Point.Double(right * step * 7, -step * 27.5), JointType.r_metatarsal));

        //create links
        for (JointType t : buttons.keySet()) {
            JointType parent = t.parent;
            if (parent != null) {
                JointButton parentButton = buttons.get(parent);
                if (parentButton != null) {
                    JointButton button = buttons.get(t);
                    panel.addDrawable(new LineArrow(outlineColor, parentButton.getLimitToward(button.getCenter()), button.getLimitToward(parentButton.getCenter()), strokeSize));
                }
            }
        }

        //add joins outlines
        for (JointButton button : buttons.values()) {
            panel.addDrawable(button.outline);
        }

        listener = new MouseListener(panel);
        this.add(panel);
        panel.addComponentListener(this);

        Dimension dim = new Dimension(400, 400);
        panel.setSize(dim);
        panel.setPreferredSize(dim);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();
    }

    public void setConcatenateJoints(ConcatenateJoints concat) {
        this.concat = concat;
        updateFromConcat();
    }

    private void concatUpdate() {
        if (concat != null) {
            ArrayList<JointType> toUse = new ArrayList<JointType>();
            for (JointType type : JointType.values()) {
                JointButton button = buttons.get(type);
                if (button != null && button.isSelected) {
                    toUse.add(type);
                }
            }
            concat.setJointToUse(toUse);
        }
    }

    private void updateFromConcat() {
        if (concat != null) {
            List<JointType> used = concat.getJointToUse();
            for (JointType type : JointType.values()) {
                JointButton button = buttons.get(type);
                if (button != null) {
                    button.setSelected(used.contains(type));
                }
            }
        }
    }

    public String getJointToUseString() {
        return concat == null ? "" : concat.getJointToUseString();
    }

    public void parseJointToUseString(String status) {
        if (concat != null) {
            concat.parseJointToUseString(status);
            updateFromConcat();
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        panel.centerView(100);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    private class MouseListener extends DrawingPanelMouseAdapter {

        JointButton current = null;
        Point mousePos;

        public MouseListener(DrawingPanel drawingPanel) {
            super(drawingPanel);
        }

        @Override
        protected void doMousePressed(MouseEvent e) {
            List<IDrawable> drawables = this.drawingPanel.findDrawables(e.getPoint());
            if ((!drawables.isEmpty()) && drawables.get(0) instanceof JointButton) {
                current = (JointButton) drawables.get(0);
            } else {
                mousePos = e.getPoint();
            }
        }

        @Override
        protected void doMouseReleased(MouseEvent e) {
            if (current != null) {
                List<IDrawable> drawables = this.drawingPanel.findDrawables(e.getPoint());
                if (drawables.contains(current)) {
                    current.press();
                    this.drawingPanel.repaint();
                }
            }
            current = null;
            mousePos = null;
        }

        @Override
        protected void doMouseWheelMoved(MouseWheelEvent e) {
            if (e.getWheelRotation() == 0) {
                return;
            }
            double panelMinScale = Math.min(this.drawingPanel.getScaleX(), this.drawingPanel.getScaleY());
            double panelMaxScale = Math.max(this.drawingPanel.getScaleX(), this.drawingPanel.getScaleY());
            double scaleStep = Math.abs(e.getWheelRotation()) * 1.2;
            double addedScale = e.getWheelRotation() > 0 ? 1.0 / scaleStep : scaleStep;
            if (addedScale * panelMinScale < 0.001) {
                addedScale = 0.001 / panelMinScale;
            }
            if (addedScale * panelMaxScale > 1000) {
                addedScale = 1000 / panelMaxScale;
            }
            this.drawingPanel.scaleFrom(addedScale, e.getPoint());
            this.drawingPanel.repaint();
        }

        @Override
        protected void doMouseDragged(MouseEvent e) {
            if (mousePos != null) {
                this.drawingPanel.translate(e.getPoint().getX() - mousePos.getX(), e.getPoint().getY() - mousePos.getY());
                this.drawingPanel.repaint();
            }
        }
    }

    private class JointButton extends DiscDrawable {

        private CircleDrawable outline;
        private boolean isSelected;
        private Paint paint;

        public JointButton(Point2D center, JointType type) {
            this(center, type, radius);
        }

        public JointButton(Point2D center, JointType type, double radius) {
            super(selectedColor, center, radius);
            outline = new CircleDrawable(outlineColor, center, radius, strokeSize);
            outline.setName(type.name());
            isSelected = true;
            buttons.put(type, this);
            updateColor();
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            updateColor();
        }

        public void press() {
            setSelected(!isSelected);
            concatUpdate();
        }

        private void updateColor() {
            this.setColor(isSelected ? selectedColor : unselectedColor);
            paint = new RadialGradientPaint(
                    new Point.Double(getCenter().getX() - this.getRadius() * 0.2, getCenter().getY() - this.getRadius() * 0.2),
                    (float) this.getRadius(),
                    new float[]{0, 1},
                    new Color[]{this.getColor().brighter(), this.getColor().darker()});
        }

        @Override
        public void draw(Graphics g) {
            Paint p = ((Graphics2D) g).getPaint();
            ((Graphics2D) g).setPaint(paint);
            ((Graphics2D) g).fill(shape);
            ((Graphics2D) g).setPaint(p);
        }
    }
}
