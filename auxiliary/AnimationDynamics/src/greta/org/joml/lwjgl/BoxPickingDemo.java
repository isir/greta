package org.joml.lwjgl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class BoxPickingDemo {
    GLFWErrorCallback errorCallback;
    GLFWKeyCallback keyCallback;
    GLFWFramebufferSizeCallback fbCallback;
    GLFWCursorPosCallback cpCallback;
    GLFWMouseButtonCallback mbCallback;

    long window;
    int width = 1200;
    int height = 800;
    boolean windowed = true;

    float mouseX, mouseY;
    boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST + 1];
    float movementSpeed = 3.666f;
    int LEVEL_LENGTH = 64;
    int LEVEL_HEIGHT = 64;
    static float GHOST_CUBE_ALPHA = 0.4f;
    boolean displayListNeedsRecompile;
    int displayList = -1;
    int selectedCube = -1;
    int ghostCube = -1;
    Vector3f pos = new Vector3f(0, 2, 0);
    Vector3f selectedPos = new Vector3f();
    Vector3f tmp = new Vector3f();
    Matrix4f viewMatrix = new Matrix4f();
    boolean[] boxes = new boolean[LEVEL_LENGTH * LEVEL_LENGTH * LEVEL_HEIGHT];
    {
        /* Make a base */
        Arrays.fill(boxes, 0, LEVEL_LENGTH * LEVEL_LENGTH, true);
        displayListNeedsRecompile = true;
    }

    void run() {
        try {
            init();
            loop();

            glfwDestroyWindow(window);
            keyCallback.free();
            fbCallback.free();
            cpCallback.free();
        } finally {
            glfwTerminate();
            errorCallback.free();
        }
    }

    void init() {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);

        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);
        if (!windowed) {
            width = vidmode.width();
            height = vidmode.height();
        }
        window = glfwCreateWindow(width, height, "Hello picking!", !windowed ? monitor : NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        System.out.println("Press ESC to close the application.");
        System.out.println("Press W/S to move forward/backward.");
        System.out.println("Press A/D to strave left/right.");
        System.out.println("Press left shift to move faster.");
        System.out.println("Press left control/spacebar to move up/down.");
        System.out.println("Move the mouse to rotate.");
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_UNKNOWN)
                    return;
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
                if (action == GLFW_PRESS || action == GLFW_REPEAT)
                    keyDown[key] = true;
                else
                    keyDown[key] = false;
            }
        });
        glfwSetFramebufferSizeCallback(window, fbCallback = new GLFWFramebufferSizeCallback() {
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
            }
        });
        glfwSetCursorPosCallback(window, cpCallback = new GLFWCursorPosCallback() {
            public void invoke(long window, double xpos, double ypos) {
                mouseX = (float) xpos / width;
                mouseY = (float) ypos / height;
            }
        });
        glfwSetMouseButtonCallback(window, mbCallback = new GLFWMouseButtonCallback() {
            public void invoke(long window, int button, int action, int mods) {
                if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS)
                    clickSelected(true);
                else if (button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS)
                    clickSelected(false);
            }
        });

        if (windowed) {
            glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
        }

        IntBuffer framebufferSize = BufferUtils.createIntBuffer(2);
        nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
        width = framebufferSize.get(0);
        height = framebufferSize.get(1);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);
    }

    static void renderCube(int x, int y, int z, boolean selected, boolean ghost) {
        glBegin(GL_QUADS);
        glColor4f(selected ? 1.0f : 0.0f, 0.0f, 0.2f, ghost ? GHOST_CUBE_ALPHA : 1.0f);
        glVertex3f(0.49f + x, -0.49f + y, -0.49f + z);
        glVertex3f(-0.49f + x, -0.49f + y, -0.49f + z);
        glVertex3f(-0.49f + x, 0.49f + y, -0.49f + z);
        glVertex3f(0.49f + x, 0.49f + y, -0.49f + z);
        glColor4f(selected ? 1.0f : 0.0f, 0.0f, 1.0f, ghost ? GHOST_CUBE_ALPHA : 1.0f);
        glVertex3f(0.49f + x, -0.49f + y, 0.49f + z);
        glVertex3f(0.49f + x, 0.49f + y, 0.49f + z);
        glVertex3f(-0.49f + x, 0.49f + y, 0.49f + z);
        glVertex3f(-0.49f + x, -0.49f + y, 0.49f + z);
        glColor4f(1.0f, 0.0f, 0.0f, ghost ? 0.2f : 1.0f);
        glVertex3f(0.49f + x, -0.49f + y, -0.49f + z);
        glVertex3f(0.49f + x, 0.49f + y, -0.49f + z);
        glVertex3f(0.49f + x, 0.49f + y, 0.49f + z);
        glVertex3f(0.49f + x, -0.49f + y, 0.49f + z);
        glColor4f(selected ? 1.0f : 0.0f, 0.0f, 0.0f, ghost ? GHOST_CUBE_ALPHA : 1.0f);
        glVertex3f(-0.49f + x, -0.49f + y, 0.49f + z);
        glVertex3f(-0.49f + x, 0.49f + y, 0.49f + z);
        glVertex3f(-0.49f + x, 0.49f + y, -0.49f + z);
        glVertex3f(-0.49f + x, -0.49f + y, -0.49f + z);
        glColor4f(selected ? 1.0f : 0.0f, 1.0f, 0.0f, ghost ? GHOST_CUBE_ALPHA : 1.0f);
        glVertex3f(0.49f + x, 0.49f + y, 0.49f + z);
        glVertex3f(0.49f + x, 0.49f + y, -0.49f + z);
        glVertex3f(-0.49f + x, 0.49f + y, -0.49f + z);
        glVertex3f(-0.49f + x, 0.49f + y, 0.49f + z);
        glColor4f(selected ? 1.0f : 0.0f, 0.2f, 0.0f, ghost ? GHOST_CUBE_ALPHA : 1.0f);
        glVertex3f(0.49f + x, -0.49f + y, -0.49f + z);
        glVertex3f(0.49f + x, -0.49f + y, 0.49f + z);
        glVertex3f(-0.49f + x, -0.49f + y, 0.49f + z);
        glVertex3f(-0.49f + x, -0.49f + y, -0.49f + z);
        glEnd();
    }

    void computeBoxUnderCenter() {
        float closestDistance = Float.POSITIVE_INFINITY;
        selectedCube = -1;
        Vector3f dir = viewMatrix.positiveZ(tmp).negate();
        Vector2f nearFar = new Vector2f();
        for (int y = 0; y < LEVEL_HEIGHT; y++) {
            for (int z = 0; z < LEVEL_LENGTH; z++) {
                for (int x = 0; x < LEVEL_LENGTH; x++) {
                    int idx = y * LEVEL_LENGTH * LEVEL_LENGTH + z * LEVEL_LENGTH + x;
                    if (boxes[idx]) {
                        int px = (x - LEVEL_LENGTH / 2);
                        int py = y;
                        int pz = (z - LEVEL_LENGTH / 2);
                        if (Intersectionf.intersectRayAab(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, px - 0.5f, py - 0.5f, pz - 0.5f, px + 0.5f, py + 0.5f, pz + 0.5f, nearFar)) {
                            if (nearFar.x < closestDistance) {
                                closestDistance = nearFar.x;
                                selectedCube = idx;
                                selectedPos.set(dir).mul(closestDistance).add(pos);
                            }
                        }
                    }
                }
            }
        }
    }

    void compileDisplayList() {
        if (!displayListNeedsRecompile)
            return;
        if (displayList != -1) {
            glDeleteLists(displayList, 1);
        }
        displayList = glGenLists(1);
        glNewList(displayList, GL_COMPILE);
        for (int y = 0; y < LEVEL_HEIGHT; y++) {
            for (int z = 0; z < LEVEL_LENGTH; z++) {
                for (int x = 0; x < LEVEL_LENGTH; x++) {
                    if (boxes[y * LEVEL_LENGTH * LEVEL_LENGTH + z * LEVEL_LENGTH + x]) {
                        int px = (x - LEVEL_LENGTH / 2);
                        int py = y;
                        int pz = (z - LEVEL_LENGTH / 2);
                        renderCube(px, py, pz, false, false);
                    }
                }
            }
        }
        glEndList();
        displayListNeedsRecompile = false;
    }

    void renderSelectedCube() {
        glPushMatrix();
        int idx = selectedCube;
        int x = idx % LEVEL_LENGTH;
        idx /= LEVEL_LENGTH;
        int z = idx % LEVEL_LENGTH;
        idx /= LEVEL_LENGTH;
        int y = idx;
        renderCube(x - LEVEL_LENGTH / 2, y, z - LEVEL_LENGTH / 2, true, false);
        glPopMatrix();
    }

    boolean inRange(int x, int y, int z) {
        return x >= 0 && x < LEVEL_LENGTH && y >= 0 && y < LEVEL_HEIGHT && z >= 0 && z < LEVEL_LENGTH;
    }

    void renderGhostCube() {
        if (ghostCube == -1)
            return;
        glEnable(GL_BLEND);
        glPushMatrix();
        int idx = ghostCube;
        int x = idx % LEVEL_LENGTH;
        idx /= LEVEL_LENGTH;
        int z = idx % LEVEL_LENGTH;
        idx /= LEVEL_LENGTH;
        int y = idx;
        renderCube(x - LEVEL_LENGTH / 2, y, z - LEVEL_LENGTH / 2, true, true);
        glPopMatrix();
        glDisable(GL_BLEND);
    }

    void clickSelected(boolean add) {
        if (add && ghostCube != -1) {
            boxes[ghostCube] = true;
            displayListNeedsRecompile = true;
        } else if (selectedCube != -1) {
            boxes[selectedCube] = false;
            displayListNeedsRecompile = true;
        }
    }

    void computeGhostCube() {
        if (selectedCube == -1)
            return;
        int idx = selectedCube;
        int x = idx % LEVEL_LENGTH;
        idx /= LEVEL_LENGTH;
        int z = idx % LEVEL_LENGTH;
        idx /= LEVEL_LENGTH;
        int y = idx;
        float px = x - LEVEL_LENGTH / 2;
        float py = y;
        float pz = z - LEVEL_LENGTH / 2;
        Vector3f d = tmp.set(selectedPos).sub(px, py, pz);
        int maxComponent = d.maxComponent();
        int nx, ny, nz;
        int signX = d.x > 0.0f ? 1 : -1;
        int signY = d.y > 0.0f ? 1 : -1;
        int signZ = d.z > 0.0f ? 1 : -1;
        if (maxComponent == 0) {
            nx = x + signX;
            ny = y;
            nz = z;
        } else if (maxComponent == 1) {
            nx = x;
            ny = y + signY;
            nz = z;
        } else {
            nx = x;
            ny = y;
            nz = z + signZ;
        }
        if (inRange(nx, ny, nz)) {
            ghostCube = ny * LEVEL_LENGTH * LEVEL_LENGTH + nz * LEVEL_LENGTH + nx;
        }
    }

    void drawCrosshair() {
        glEnable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        glPushMatrix();
        glLoadIdentity();
        glBegin(GL_LINES);
        glColor4f(0.2f, 0.2f, 0.2f, 0.6f);
        glVertex3f(-0.01f, 0.0f, -1.0f);
        glVertex3f(+0.01f, 0.0f, -1.0f);
        glVertex3f(0.0f, -0.01f, -1.0f);
        glVertex3f(0.0f, +0.01f, -1.0f);
        glEnd();
        glPopMatrix();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    void loop() {
        GL.createCapabilities();
        glClearColor(0.97f, 0.97f, 0.97f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glDepthFunc(GL_LEQUAL);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        long lastTime = System.nanoTime();
        Vector3f dir = new Vector3f();
        Vector3f right = new Vector3f();
        Vector3f up = new Vector3f();
        Matrix4f mat = new Matrix4f();
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        float rotX = 0.0f;
        float rotY = 0.0f;

        while (!glfwWindowShouldClose(window)) {
            long thisTime = System.nanoTime();
            float diff = (float) ((thisTime - lastTime) / 1E9);
            lastTime = thisTime;
            float move = diff * movementSpeed;

            if (keyDown[GLFW_KEY_LEFT_SHIFT])
                move *= 2.0f;
            viewMatrix.positiveZ(dir).negate().mul(move);
            viewMatrix.positiveX(right).mul(move);
            viewMatrix.positiveY(up).mul(move);
            if (keyDown[GLFW_KEY_W])
                pos.add(dir);
            if (keyDown[GLFW_KEY_S])
                pos.sub(dir);
            if (keyDown[GLFW_KEY_A])
                pos.sub(right);
            if (keyDown[GLFW_KEY_D])
                pos.add(right);
            if (keyDown[GLFW_KEY_SPACE])
                pos.add(up);
            if (keyDown[GLFW_KEY_LEFT_CONTROL])
                pos.sub(up);
            rotX = mouseY;
            rotY = mouseX;

            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(mat.setPerspective((float) Math.toRadians(45), (float) width / height, 0.01f, 100.0f).get(fb));
            glMatrixMode(GL_MODELVIEW);
            glLoadMatrixf(viewMatrix.identity().rotateX(rotX).rotateY(rotY).translate(-pos.x, -pos.y, -pos.z).get(fb));
            glViewport(0, 0, width, height);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            compileDisplayList();
            if (displayList != -1)
                glCallList(displayList);
            computeBoxUnderCenter();
            if (selectedCube != -1) {
                renderSelectedCube();
            }
            computeGhostCube();
            renderGhostCube();
            drawCrosshair();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new BoxPickingDemo().run();
    }
}
