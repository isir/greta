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
package greta.tools.editors.handshape;

import greta.core.animation.mpeg4.bap.JointType;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author Andre-Marie
 */
public class HandPanel extends JPanel {

    private Color skinColor = new Color(255,200,180);
    private Color strokeColor = Color.black;
    private Color unselectedColor = Color.GRAY;
    private Color selectedColor = Color.GREEN;
    private static final double RADIUS = 0.03;
    private double[][] hand = {
        {0.576, 1.000},//0
        {0.576, 0.932},
        {0.545, 0.886},
        {0.455, 0.841},
        {0.303, 0.705},
        {0.182, 0.636},//5
        {0.061, 0.591},
        {0.030, 0.545},
        {0.061, 0.523},
        {0.182, 0.523},
        {0.364, 0.568},//10
        {0.424, 0.545},
        {0.394, 0.159},
        {0.424, 0.091},
        {0.485, 0.091},
        {0.515, 0.159},//15
        {0.545, 0.409},
        {0.545, 0.091},
        {0.576, 0.023},
        {0.636, 0.023},
        {0.667, 0.091},//20
        {0.682, 0.386},
        {0.697, 0.114},
        {0.727, 0.045},
        {0.788, 0.045},
        {0.818, 0.114},//25
        {0.818, 0.409},
        {0.848, 0.182},
        {0.879, 0.136},
        {0.909, 0.136},
        {0.939, 0.182},//30
        {0.939, 0.455},
        {0.970, 0.659},
        {0.909, 0.841},
        {0.939, 1.000}
    };
    private double[] thumb_1 = {0.545, 0.773};
    private double[] thumb_2 = {0.394, 0.705};
    private double[] thumb_3 = {0.197, 0.602};
    private double[] thumb_tip = {0.091, 0.557};
    private double[] index_0 = {0.53, 0.682};
    private double[] index_1 = {0.5, 0.489};
    private double[] index_2 = {0.479, 0.318};
    private double[] index_3 = {0.464, 0.198};
    private double[] index_tip = {0.455, 0.114};
    private double[] middle_0 = {0.636, 0.67};
    private double[] middle_1 = {0.624, 0.468};
    private double[] middle_2 = {0.612, 0.261};
    private double[] middle_3 = {0.609, 0.136};
    private double[] middle_tip = {0.606, 0.045};
    private double[] ring_0 = {0.758, 0.693};
    private double[] ring_1 = {0.758, 0.484};
    private double[] ring_2 = {0.755, 0.273};
    private double[] ring_3 = {0.752, 0.155};
    private double[] ring_tip = {0.752, 0.075};
    private double[] pinky_0 = {0.864, 0.743};
    private double[] pinky_1 = {0.879, 0.5};
    private double[] pinky_2 = {0.882, 0.33};
    private double[] pinky_3 = {0.885, 0.227};
    private double[] pinky_tip = {0.888, 0.17};
//    private double[] wrist = {0.727, 0.886};
    private double[] selectedJoint = null;
    double handWidth = 1;
    double handHeight = 1;
    double widthOffset = 0;
    double heightOffset = 0;
    double radius = 1;

    public HandPanel() {
        super(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        handWidth = Math.min(getHeight() * 3.0 / 4.0, getWidth());
        handHeight = Math.min(getHeight(), getWidth() * 4.0 / 3.0);
        widthOffset = (getWidth() - handWidth) / 2.0;
        heightOffset = (getHeight() - handHeight) / 2.0;

        //draw hand
        drawHand(g);

        //draw joints
        radius = handWidth * RADIUS;
//        drawLine(g, wrist, thumb_1);
//        drawLine(g, wrist, index_0);
//        drawLine(g, wrist, middle_0);
//        drawLine(g, wrist, ring_0);
//        drawLine(g, wrist, pinky_0);
        drawFingerJoints(g, thumb_1, thumb_2, thumb_3, thumb_tip);
        drawFingerJoints(g, index_0, index_1, index_2, index_3, index_tip);
        drawFingerJoints(g, middle_0, middle_1, middle_2, middle_3, middle_tip);
        drawFingerJoints(g, ring_0, ring_1, ring_2, ring_3, ring_tip);
        drawFingerJoints(g, pinky_0, pinky_1, pinky_2, pinky_3, pinky_tip);
//        drawTip(g, wrist);
    }

    private void drawFingerJoints(Graphics g, double[] joint1, double[] joint2, double[] joint3, double[] tip) {
        drawLine(g, joint1, joint2);
        drawLine(g, joint2, joint3);
        drawLine(g, joint3, tip);
        drawJoint(g, joint1);
        drawJoint(g, joint2);
        drawJoint(g, joint3);
        drawTip(g, tip);
    }

    private void drawFingerJoints(Graphics g, double[] joint1, double[] joint2, double[] joint3, double[] joint4, double[] tip) {
        drawLine(g, joint1, joint2);
        drawLine(g, joint2, joint3);
        drawLine(g, joint3, joint4);
        drawLine(g, joint4, tip);
        drawJoint(g, joint1);
        drawJoint(g, joint2);
        drawJoint(g, joint3);
        drawJoint(g, joint4);
        drawTip(g, tip);

    }

    private void drawDisc(Graphics g, double[] center, double radius) {
        g.fillOval(
                (int) (widthOffset + center[0] * handWidth - radius + 0.5),
                (int) (heightOffset + center[1] * handHeight - radius + 0.5),
                (int) (radius * 2.0 + 0.5),
                (int) (radius * 2.0 + 0.5));
    }

    private void drawCircle(Graphics g, double[] center, double radius) {
        g.drawOval(
                (int) (widthOffset + center[0] * handWidth - radius + 0.5),
                (int) (heightOffset + center[1] * handHeight - radius + 0.5),
                (int) (radius * 2.0 + 0.5),
                (int) (radius * 2.0 + 0.5));
    }

    private void drawLine(Graphics g, double[] p1, double[] p2) {
        g.drawLine(
                (int) (widthOffset + p1[0] * handWidth + 0.5),
                (int) (heightOffset + p1[1] * handHeight + 0.5),
                (int) (widthOffset + p2[0] * handWidth + 0.5),
                (int) (heightOffset + p2[1] * handHeight + 0.5));
    }

    private void drawHand(Graphics g) {
        int[] xs = new int[hand.length];
        int[] ys = new int[hand.length];
        for (int i = 0; i < hand.length; ++i) {
            xs[i] = (int) (widthOffset + hand[i][0] * handWidth + 0.5);
            ys[i] = (int) (heightOffset + hand[i][1] * handHeight + 0.5);
        }
        g.setColor(skinColor);
        g.fillPolygon(xs, ys, hand.length);
        g.setColor(strokeColor);
        g.drawPolygon(xs, ys, hand.length);
    }

    private void drawTip(Graphics g, double[] joint) {
        g.setColor(skinColor);
        drawDisc(g, joint, radius / 2.0);
        g.setColor(strokeColor);
        drawCircle(g, joint, radius / 2.0);
    }

    private void drawJoint(Graphics g, double[] joint) {
        g.setColor(selectedJoint == joint ? selectedColor : unselectedColor);
        drawDisc(g, joint, radius);
        g.setColor(strokeColor);
        drawCircle(g, joint, radius);
    }

    public void selectAt(int posx, int posy) {
        double x = (posx - widthOffset) / handWidth;
        double y = (posy - heightOffset) / handHeight;
        if (selectedJoint == null || !isInside(x, y, selectedJoint)) {
            selectedJoint = null;
            if (!tryToSelectJoint(x, y, thumb_1)) {
                if (!tryToSelectJoint(x, y, thumb_2)) {
                    if (!tryToSelectJoint(x, y, thumb_3)) {
                        if (!tryToSelectJoint(x, y, index_0)) {
                            if (!tryToSelectJoint(x, y, index_1)) {
                                if (!tryToSelectJoint(x, y, index_2)) {
                                    if (!tryToSelectJoint(x, y, index_3)) {
                                        if (!tryToSelectJoint(x, y, middle_0)) {
                                            if (!tryToSelectJoint(x, y, middle_1)) {
                                                if (!tryToSelectJoint(x, y, middle_2)) {
                                                    if (!tryToSelectJoint(x, y, middle_3)) {
                                                        if (!tryToSelectJoint(x, y, ring_0)) {
                                                            if (!tryToSelectJoint(x, y, ring_1)) {
                                                                if (!tryToSelectJoint(x, y, ring_2)) {
                                                                    if (!tryToSelectJoint(x, y, ring_3)) {
                                                                        if (!tryToSelectJoint(x, y, pinky_0)) {
                                                                            if (!tryToSelectJoint(x, y, pinky_1)) {
                                                                                if (!tryToSelectJoint(x, y, pinky_2)) {
                                                                                    if (!tryToSelectJoint(x, y, pinky_3)) {
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            repaint();
        }
    }

    private boolean tryToSelectJoint(double x, double y, double[] joint) {
        if (isInside(x, y, joint)) {
            selectedJoint = joint;
            return true;
        }
        return false;
    }

    private boolean isInside(double x, double y, double[] center) {
        return Math.sqrt(Math.pow(x - center[0], 2) + Math.pow(y - center[1], 2)) < RADIUS;
    }

    public JointType[] getJointTypesOf(double[] joint) {
        if (joint == thumb_1) {
            return new JointType[]{JointType.l_thumb1, JointType.r_thumb1};
        }
        if (joint == thumb_2) {
            return new JointType[]{JointType.l_thumb2, JointType.r_thumb2};
        }
        if (joint == thumb_3) {
            return new JointType[]{JointType.l_thumb3, JointType.r_thumb3};
        }

        if (joint == index_0) {
            return new JointType[]{JointType.l_index0, JointType.r_index0};
        }
        if (joint == index_1) {
            return new JointType[]{JointType.l_index1, JointType.r_index1};
        }
        if (joint == index_2) {
            return new JointType[]{JointType.l_index2, JointType.r_index2};
        }
        if (joint == index_3) {
            return new JointType[]{JointType.l_index3, JointType.r_index3};
        }

        if (joint == middle_0) {
            return new JointType[]{JointType.l_middle0, JointType.r_middle0};
        }
        if (joint == middle_1) {
            return new JointType[]{JointType.l_middle1, JointType.r_middle1};
        }
        if (joint == middle_2) {
            return new JointType[]{JointType.l_middle2, JointType.r_middle2};
        }
        if (joint == middle_3) {
            return new JointType[]{JointType.l_middle3, JointType.r_middle3};
        }

        if (joint == ring_0) {
            return new JointType[]{JointType.l_ring0, JointType.r_ring0};
        }
        if (joint == ring_1) {
            return new JointType[]{JointType.l_ring1, JointType.r_ring1};
        }
        if (joint == ring_2) {
            return new JointType[]{JointType.l_ring2, JointType.r_ring2};
        }
        if (joint == ring_3) {
            return new JointType[]{JointType.l_ring3, JointType.r_ring3};
        }

        if (joint == pinky_0) {
            return new JointType[]{JointType.l_pinky0, JointType.r_pinky0};
        }
        if (joint == pinky_1) {
            return new JointType[]{JointType.l_pinky1, JointType.r_pinky1};
        }
        if (joint == pinky_2) {
            return new JointType[]{JointType.l_pinky2, JointType.r_pinky2};
        }
        if (joint == pinky_3) {
            return new JointType[]{JointType.l_pinky3, JointType.r_pinky3};
        }

        return new JointType[]{JointType.null_joint, JointType.null_joint};
    }

    public JointType[] getJointTypesOfSelected() {
        return getJointTypesOf(selectedJoint);
    }
}
