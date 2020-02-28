package org.joml.lwjgl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.camera.ArcBallCamera;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

/**
 * Showcases the use of
 * {@link Matrix4f#reflect(Quaternionf, Vector3f) Matrix4f.reflect()} with stencil reflections.
 * <p>
 * This demo also makes use of joml-camera with the {@link ArcBallCamera}.
 *
 * @author Kai Burjack
 */
public class ReflectDemo {
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
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);

        window = glfwCreateWindow(width, height, "Reflection Demo", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
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

    void renderMirror(boolean backside) {
        glBegin(GL_QUADS);
        glColor4f(1, 1, 1, 0.5f);
        glVertex3f(-0.5f, -0.5f, 0.0f);
        glVertex3f(0.5f, -0.5f, 0.0f);
        glVertex3f(0.5f, 0.5f, 0.0f);
        glVertex3f(-0.5f, 0.5f, 0.0f);
        glEnd();
        if (backside) {
            glBegin(GL_QUADS);
            glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
            glVertex3f(-0.5f, -0.5f, 0.0f);
            glVertex3f(-0.5f, 0.5f, 0.0f);
            glVertex3f(0.5f, 0.5f, 0.0f);
            glVertex3f(0.5f, -0.5f, 0.0f);
            glEnd();
        }
    }

    void renderCube() {
        glBegin(GL_QUADS);
        glColor3f(   0.0f,  0.0f,  0.2f );
        glVertex3f(  0.5f, -0.5f, -0.5f );
        glVertex3f( -0.5f, -0.5f, -0.5f );
        glVertex3f( -0.5f,  0.5f, -0.5f );
        glVertex3f(  0.5f,  0.5f, -0.5f );
        glColor3f(   0.0f,  0.0f,  1.0f );
        glVertex3f(  0.5f, -0.5f,  0.5f );
        glVertex3f(  0.5f,  0.5f,  0.5f );
        glVertex3f( -0.5f,  0.5f,  0.5f );
        glVertex3f( -0.5f, -0.5f,  0.5f );
        glColor3f(   1.0f,  0.0f,  0.0f );
        glVertex3f(  0.5f, -0.5f, -0.5f );
        glVertex3f(  0.5f,  0.5f, -0.5f );
        glVertex3f(  0.5f,  0.5f,  0.5f );
        glVertex3f(  0.5f, -0.5f,  0.5f );
        glColor3f(   0.2f,  0.0f,  0.0f );
        glVertex3f( -0.5f, -0.5f,  0.5f );
        glVertex3f( -0.5f,  0.5f,  0.5f );
        glVertex3f( -0.5f,  0.5f, -0.5f );
        glVertex3f( -0.5f, -0.5f, -0.5f );
        glColor3f(   0.0f,  1.0f,  0.0f );
        glVertex3f(  0.5f,  0.5f,  0.5f );
        glVertex3f(  0.5f,  0.5f, -0.5f );
        glVertex3f( -0.5f,  0.5f, -0.5f );
        glVertex3f( -0.5f,  0.5f,  0.5f );
        glColor3f(   0.0f,  0.2f,  0.0f );
        glVertex3f(  0.5f, -0.5f, -0.5f );
        glVertex3f(  0.5f, -0.5f,  0.5f );
        glVertex3f( -0.5f, -0.5f,  0.5f );
        glVertex3f( -0.5f, -0.5f, -0.5f );
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
        glEnable(GL_CULL_FACE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glLineWidth(1.4f);

        // Remember the current time.
        long lastTime = System.nanoTime();

        Matrix4f mat = new Matrix4f();
        // FloatBuffer for transferring matrices to OpenGL
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);

        cam.setAlpha((float) Math.toRadians(-20));
        cam.setBeta((float) Math.toRadians(20));

        Vector3f mirrorPosition = new Vector3f(0.0f, 3.0f, -5.0f);
        /* Build orientation quaternion of mirror. */
        Quaternionf mirrorOrientation = new Quaternionf();
        mirrorOrientation.rotateY((float) Math.toRadians(45))
                         .rotateX((float) Math.toRadians(45));

        /* Used to hold the mirror transformation matrix */
        Matrix4f mirrorMatrix = new Matrix4f();
        /* Used to hold the reflection matrix */
        Matrix4f reflectMatrix = new Matrix4f();

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
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

            mat.setPerspective((float) Math.atan((ViewSettings.screenHeight * height / ViewSettings.screenHeightPx) / ViewSettings.distanceToScreen),
                               (float) width / height, 0.01f, 100.0f);
            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(mat.get(fb));

            /*
             * Obtain the camera's view matrix and render grid.
             */
            glMatrixMode(GL_MODELVIEW);
            glLoadMatrixf(cam.viewMatrix(mat.identity()).get(fb));

            /* Stencil the mirror */
            mirrorMatrix.set(mat)
                        .translate(mirrorPosition)
                        .rotate(mirrorOrientation)
                        .scale(15.0f, 8.5f, 1.0f);
            glLoadMatrixf(mirrorMatrix.get(fb));
            glEnable(GL_STENCIL_TEST);
            glColorMask(false, false, false, false);
            glDisable(GL_DEPTH_TEST);
            glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
            glStencilFunc(GL_ALWAYS, 1, 1);
            renderMirror(false);
            glColorMask(true, true, true, true);
            glEnable(GL_DEPTH_TEST);
            glStencilFunc(GL_EQUAL, 1, 1);
            glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

            /* Render the reflected scene */
            reflectMatrix.set(mat)
                         .reflect(mirrorOrientation, mirrorPosition);
            glLoadMatrixf(reflectMatrix.get(fb));
            renderGrid();
            glFrontFace(GL_CW);
            renderCube();
            glFrontFace(GL_CCW);
            glDisable(GL_STENCIL_TEST);

            /* Render visible mirror geometry with blending */
            mirrorMatrix.get(fb);
            glLoadMatrixf(fb);
            glEnable(GL_BLEND);
            renderMirror(true);
            glDisable(GL_BLEND);

            /* Render scene normally */
            mat.get(fb);
            glLoadMatrixf(fb);
            renderGrid();
            renderCube();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new ReflectDemo().run();
    }
}
