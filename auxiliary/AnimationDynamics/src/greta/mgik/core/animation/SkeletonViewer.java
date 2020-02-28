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

import greta.core.animation.math.Matrix3d;
import greta.core.animation.math.Vector3d;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix4f;
import org.joml.camera.ArcBallCamera;
import org.joml.lwjgl.ViewSettings;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class SkeletonViewer {

    GLFWErrorCallback errorCallback;
    GLFWKeyCallback keyCallback;
    GLFWFramebufferSizeCallback fbCallback;
    GLFWCursorPosCallback cpCallback;
    GLFWScrollCallback sCallback;
    GLFWMouseButtonCallback mbCallback;

    long window;
    int width = 800;
    int height = 600;
    int x, y;
    float zoom = 20;
    int mouseX, mouseY;
    boolean down;

    void run() {
        try {
            init();
            loop();

            glfwDestroyWindow(window);
            keyCallback.free();
            fbCallback.free();
            cpCallback.free();
            sCallback.free();
            mbCallback.free();
        } finally {
            glfwTerminate();
            errorCallback.free();
        }
    }

    ArcBallCamera cam = new ArcBallCamera();

    void init() {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure our window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);

        window = glfwCreateWindow(width, height, "Hello Skeleton!", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        System.out.println("Press ENTER to randomly reposition the cube on the grid.");
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }

                if (key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                    cam.center((float) Math.random() * 20.0f - 10.0f, 0.0f, (float) Math.random() * 20.0f - 10.0f);
                }
            }
        });
        glfwSetFramebufferSizeCallback(window, fbCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
            }
        });
        glfwSetCursorPosCallback(window, cpCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                x = (int) xpos - width / 2;
                y = height / 2 - (int) ypos;
            }
        });
        glfwSetMouseButtonCallback(window, mbCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (action == GLFW_PRESS) {
                    down = true;
                    mouseX = x;
                    mouseY = y;
                } else if (action == GLFW_RELEASE) {
                    down = false;
                }
            }
        });
        glfwSetScrollCallback(window, sCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                if (yoffset > 0) {
                    zoom /= 1.1f;
                } else {
                    zoom *= 1.1f;
                }
            }
        });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);

        IntBuffer framebufferSize = BufferUtils.createIntBuffer(2);
        nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
        width = framebufferSize.get(0);
        height = framebufferSize.get(1);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);
    }

    void renderCube() {
        glBegin(GL_QUADS);
        glColor3f(0.0f, 0.0f, 0.2f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glColor3f(0.0f, 0.0f, 1.0f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glColor3f(1.0f, 0.0f, 0.0f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glColor3f(0.2f, 0.0f, 0.0f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glColor3f(0.0f, 1.0f, 0.0f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glColor3f(0.0f, 0.2f, 0.0f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glEnd();
    }

    void renderGrid() {
        glBegin(GL_LINES);
        glColor3f(0.2f, 0.2f, 0.2f);
        for (int i = -20; i <= 20; i++) {
            glVertex3f(-20.0f, 0.0f, i);
            glVertex3f(20.0f, 0.0f, i);
            glVertex3f(i, 0.0f, -20.0f);
            glVertex3f(i, 0.0f, 20.0f);
        }
        glEnd();
    }

    void loop() {
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.9f, 0.9f, 0.9f, 1.0f);
        // Enable depth testing
        glEnable(GL_DEPTH_TEST);
        glLineWidth(1.4f);

        // Remember the current time.
        long lastTime = System.nanoTime();

        Matrix4f mat = new Matrix4f();
        // FloatBuffer for transferring matrices to OpenGL
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);

        cam.setAlpha((float) Math.toRadians(-20));
        cam.setBeta((float) Math.toRadians(20));

        while (!glfwWindowShouldClose(window)) {
            /* Set input values for the camera */
            if (down) {
                cam.setAlpha(cam.getAlpha() + Math.toRadians((x - mouseX) * 0.1f));
                cam.setBeta(cam.getBeta() + Math.toRadians((mouseY - y) * 0.1f));
                mouseX = x;
                mouseY = y;
            }
            cam.zoom(zoom);

            /* Compute delta time */
            long thisTime = System.nanoTime();
            float diff = (float) ((thisTime - lastTime) / 1E9);
            lastTime = thisTime;
            /* And let the camera make its update */
            cam.update(diff);

            glViewport(0, 0, width, height);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            mat.setPerspective((float) Math.atan((ViewSettings.screenHeight * height / ViewSettings.screenHeightPx) / ViewSettings.distanceToScreen),
                    (float) width / height, 0.01f, 100.0f)
                    .get(fb);
            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(fb);

            /*
             * Obtain the camera's view matrix and render grid.
             */
            cam.viewMatrix(mat.identity()).get(fb);
            glMatrixMode(GL_MODELVIEW);
            glLoadMatrixf(fb);
            renderGrid();

            /* Translate to cube position and render cube */
            mat.translate(cam.centerMover.target).get(fb);
            glLoadMatrixf(fb);
            //renderCube();
            renderSkeleton();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new SkeletonViewer().run();
    }

    void renderSkeleton() {
        //sk.m_dim_values.set(8, 1.5);
        //sk.m_dim_values.set(11, -1.5);
        //sk.update();
        glMatrixMode(GL_MODELVIEW);
        for (int i = 0; i < sk.m_joints.size(); ++i) {
            Matrix3d matrix = sk.m_joint_globalOrientations.get(i);
            Vector3d pos = sk.m_joint_globalPositions.get(i);
            float[] mat = new float[16];
            for (int w = 0; w < 3; ++w) {
                for (int h = 0; h < 3; ++h) {
                    mat[4 * w + h] = (float) matrix.getEntry(h, w);
                }
            }
            mat[12] = 0;
            mat[13] = 0;
            mat[14] = 0;
            mat[15] = 1;
            mat[3] = 0;
            mat[7] = 0;
            mat[11] = 0;

            glPushMatrix();
            //glLoadIdentity();
            glTranslatef((float) pos.getEntry(0), (float) pos.getEntry(1), (float) pos.getEntry(2));
            glPushMatrix();
            glMultMatrixf(mat);

            glPointSize(8);
            glBegin(GL_POINTS);
            glVertex3f(0, 0, 0);
            glEnd();
            glPopMatrix();
            glPopMatrix();

            glColor3f(0.1f, 0.4f, 0.4f);
            glBegin(GL_POINTS);
            glVertex3f((float) pos.getEntry(0), (float) pos.getEntry(1), (float) pos.getEntry(2));
            glEnd();
            int parent = sk.m_joints.get(i).m_index_parent;
            if (parent >= 0) {
                glColor3f(0.1f, 0.4f, 0.7f);
                glLineWidth(5);
                glBegin(GL_LINES);
                glVertex3f((float) pos.getEntry(0), (float) pos.getEntry(1), (float) pos.getEntry(2));
                glVertex3f((float) sk.m_joint_globalPositions.get(parent).getEntry(0), (float) sk.m_joint_globalPositions.get(parent).getEntry(1), (float) sk.m_joint_globalPositions.get(parent).getEntry(2));
                glEnd();
            }
        }
        glLineWidth(1);
        glPointSize(1);
    }

    public void applyIK(int indexEd, Vector3d position){
        sk.m_endeffectorsPositions.set(indexEd, position);

    }

    Skeleton sk = new Skeleton();
}
