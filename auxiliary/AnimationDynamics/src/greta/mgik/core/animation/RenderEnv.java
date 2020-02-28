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
package greta.mgik.core.animation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class RenderEnv {
    Skeleton sk;
    private int w, h;
    private Camera camera = new Camera();
    private boolean g_MouseMovePressed = false;
    private boolean g_MouseRotatePressed = false;
    private boolean g_MouseZoomPressed = false;
    double unit = 0.05;
    public boolean _useIK = false;

    public void draw(){
        drawPlane();
    }

    public void drawPlane(){
        glLineWidth((float) 0.5);
        glColor3f(0.0f,0.8f,0.8f);
	glPushMatrix();

        glColor3f(0.1f,0.1f,0.1f);
	glRotated(90,1,0,0);

        int nbSubdivisions = 40;
        int size = 10;
	//glDisable(GL_LIGHTING);
	glBegin(GL_LINES);
	for (int i=0; i<=nbSubdivisions; ++i)
	{
		float pos = (float) (size*(2.0*i/nbSubdivisions-1.0));
		glVertex2f(pos, -size);
		glVertex2f(pos, +size);
		glVertex2f(-size, pos);
		glVertex2f( size, pos);
	}
	glEnd();
	glPopMatrix();
    }

    public void drawSk(){


    }

     public void mouseExited(MouseEvent me) {
    }
    int ox = 0, oy = 0;

    public void mousePressed(MouseEvent me) {
        int x = me.getX();
        int y = h - me.getY();
        if (me.getButton() == me.BUTTON1) {
            camera.beginRotate(me.getX(), me.getY());
            g_MouseMovePressed = false;
            g_MouseRotatePressed = true;

        } else if (me.getButton() == me.BUTTON2) {

            g_MouseMovePressed = false;
            g_MouseRotatePressed = false;
            g_MouseZoomPressed = true;

        } else if (me.getButton() == me.BUTTON3 && !me.isShiftDown()) {

            g_MouseMovePressed = true;
            g_MouseRotatePressed = false;
            g_MouseZoomPressed = false;
            ox = me.getX();
            oy = h - me.getY();

        }else if (me.getButton() == me.BUTTON3 && me.isShiftDown()){
            g_MouseMovePressed = false;
            g_MouseRotatePressed = false;
            g_MouseZoomPressed = true;
            camera.zoom(-5.0f * 1);
        }
    }

    public void mouseReleased(MouseEvent me) {

        if (g_MouseRotatePressed == true) {
            camera.endRotate();
        } else if (g_MouseMovePressed == true) {
            if (_useIK) {
            } else {
                int x = me.getX();
                int y = h - me.getY();
                camera.move((x - ox) * 0.3f, (y - oy) * 0.3f, 0);
            }
        }

        g_MouseMovePressed = false;
        g_MouseRotatePressed = false;
        g_MouseZoomPressed = false;

    }


    public void mouseMoved(MouseEvent me) {
    }


    public void mouseDragged(MouseEvent me) {

        if (g_MouseRotatePressed == true) {
            {
                camera.rotate(me.getX(), me.getY());
            }

        } else if (g_MouseMovePressed == true) {

            if (_useIK ) {
//                Joint cj = getSelectedJoint();
//                if (cj != null) {
//                    int x = me.getX();
//                    int y = h - me.getY();
//                    Vec4d pos = camera.get3DTrackPositionOfScreenCoordinateOf(new Vec4d(_target, 1), x, y);
//                    setTarget(new Vec3d(pos.x(), pos.y(), pos.z()));
//                    _container.applyIK();
                    //System.out.println(pos);
               // }
            } else {
                int x = me.getX();
                int y = h - me.getY();
                camera.move((x - ox) * 0.3f * unit, (y - oy) * 0.3f * unit, 0);
                ox = x;
                oy = y;
            }
        } else if (g_MouseZoomPressed == true) {
            //camera.zoom(fZoom);
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int rotation = e.getWheelRotation();
        camera.zoom(rotation * 8.0f * unit);
    }
}
