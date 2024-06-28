package org.joml.lwjgl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.camera.ArcBallCamera;
import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

public class BillboardDemo {
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
    int numBoxes = 40;
    Vector3f[] boxes = new Vector3f[numBoxes];
    Matrix4f[] modelMatrices = new Matrix4f[numBoxes];
    int billboardMode = 1; // <- start with cylindrical billboards
    boolean wireframe;
    boolean freeze;

    void resetBoxes() {
        for (int i = 0; i < boxes.length; i++) {
            boxes[i].set((float) Math.random() *  40.0f - 20.0f, 0.0f, (float) Math.random() * 40.0f - 20.0f);
            modelMatrices[i].translation(boxes[i]);
        }
    }

    void init() {
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = new Vector3f();
            modelMatrices[i] = new Matrix4f();
        }
        resetBoxes();

        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);

        window = glfwCreateWindow(width, height, "Hello Billboard!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        System.out.println("Press 'R' to randomly reposition the boxes.");
        System.out.println("Press 'B' to toggle between no, spherical, cylindrical and spherical shortest arc billboards.");
        System.out.println("Press 'F' to freeze current box rotations.");
        System.out.println("Press 'W' to toggle between wireframe and filled.");
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);

                if (key == GLFW_KEY_R && action == GLFW_PRESS) {
                    resetBoxes();
                } else if (key == GLFW_KEY_B && action == GLFW_PRESS) {
                    billboardMode = (billboardMode + 1) % 4;
                    if (billboardMode == 0)
                        System.out.println("Not using billboards");
                    else if (billboardMode == 1)
                        System.out.println("Using cylindrical billboards");
                    else if (billboardMode == 2)
                        System.out.println("Using spherical billboards with up = +Y");
                    else if (billboardMode == 3)
                        System.out.println("Using spherical shortest arc billboards");
                } else if (key == GLFW_KEY_W && action == GLFW_PRESS) {
                    wireframe = !wireframe;
                    if (wireframe)
                        System.out.println("Using wireframe rendering");
                    else
                        System.out.println("Using filled rendering");
                } else if (key == GLFW_KEY_F && action == GLFW_PRESS) {
                    freeze = !freeze;
                    if (freeze)
                        System.out.println("Froze updating box model matrices");
                    else
                        System.out.println("Resumed updating box model matrices");
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
        glColor3f(   0.0f,  0.0f,  0.2f );
        glVertex3f(  0.5f, -0.5f, -0.5f );
        glVertex3f(  0.5f,  0.5f, -0.5f );
        glVertex3f( -0.5f,  0.5f, -0.5f );
        glVertex3f( -0.5f, -0.5f, -0.5f );
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
        glLineWidth(1.4f);

        // Remember the current time.
        long lastTime = System.nanoTime();

        Matrix4f mat = new Matrix4f();
        // FloatBuffer for transferring matrices to OpenGL
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);

        // Objects for building the billboard matrix
        Vector3f origin = new Vector3f();
        Vector3f up = new Vector3f(0, 1, 0);
        Matrix4f modelViewProj = new Matrix4f();

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

            mat.setPerspective((float) Math.toRadians(60),
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

            /* Determine camera origin */
            mat.origin(origin);

            int mode;
            if (wireframe) {
                mode = GL_LINE;
            } else {
                mode = GL_FILL;
            }
            glPolygonMode(GL_FRONT_AND_BACK, mode);

            /* Render each cube */
            for (int i = 0; i < boxes.length; i++) {
                /* Build box model matrix */
                if (!freeze) {
                    if (billboardMode == 0) // not using billboards
                        modelMatrices[i].translation(boxes[i]);
                    else if (billboardMode == 1) // cylindrical
                        modelMatrices[i].billboardCylindrical(boxes[i], origin, up);
                    else if (billboardMode == 2) // spherical
                        modelMatrices[i].billboardSpherical(boxes[i], origin, up);
                    else if (billboardMode == 3) // shortest arc spherical
                        modelMatrices[i].billboardSpherical(boxes[i], origin);
                }

                /* Multiply with view-projection matrix */
                mat.mulAffine(modelMatrices[i], modelViewProj);
                glLoadMatrixf(modelViewProj.get(fb));
                renderCube();
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new BillboardDemo().run();
    }
}
