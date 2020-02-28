package org.joml.lwjgl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Simple demo showcasing the use of simple VBO rendering and setting
 * projection and view matrices using JOML.
 *
 * @author Kai Burjack
 *
 */
public class VboDemo {
    GLFWErrorCallback errorCallback;
    GLFWKeyCallback   keyCallback;
    GLFWFramebufferSizeCallback fbCallback;

    long window;
    int width = 300;
    int height = 300;

    // JOML matrices
    Matrix4f projMatrix = new Matrix4f();
    Matrix4f viewMatrix = new Matrix4f();

    // FloatBuffer for transferring matrices to OpenGL
    FloatBuffer fb = BufferUtils.createFloatBuffer(16);

    void run() {
        try {
            init();
            loop();

            glfwDestroyWindow(window);
            keyCallback.free();
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
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 1);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Hello VBO!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key,
                    int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
            }
        });
        glfwSetFramebufferSizeCallback(window,
                fbCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
            }
        });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);
    }

    void buildCube() {
        // create buffer to hold the vertex colors
        FloatBuffer cb = BufferUtils.createFloatBuffer(3 * 4 * 6);
        // create buffer to hold the vertex positions
        FloatBuffer pb = BufferUtils.createFloatBuffer(3 * 4 * 6);
        // define all faces of a cube as quads.
        // this has redundant vertices in it, but it's easy to create
        // an element buffer for it.
        for (int i = 0; i < 4; i++)
            cb.put(0.0f).put(0.0f).put(0.2f);
        pb.put( 0.5f).put(-0.5f).put(-0.5f);
        pb.put(-0.5f).put(-0.5f).put(-0.5f);
        pb.put(-0.5f).put( 0.5f).put(-0.5f);
        pb.put( 0.5f).put( 0.5f).put(-0.5f);
        for (int i = 0; i < 4; i++)
            cb.put(0.0f).put(0.0f).put(1.0f);
        pb.put( 0.5f).put(-0.5f).put( 0.5f);
        pb.put( 0.5f).put( 0.5f).put( 0.5f);
        pb.put(-0.5f).put( 0.5f).put( 0.5f);
        pb.put(-0.5f).put(-0.5f).put( 0.5f);
        for (int i = 0; i < 4; i++)
            cb.put(1.0f).put(0.0f).put(0.0f);
        pb.put( 0.5f).put(-0.5f).put(-0.5f);
        pb.put( 0.5f).put( 0.5f).put(-0.5f);
        pb.put( 0.5f).put( 0.5f).put( 0.5f);
        pb.put( 0.5f).put(-0.5f).put( 0.5f);
        for (int i = 0; i < 4; i++)
            cb.put(0.2f).put(0.0f).put(0.0f);
        pb.put(-0.5f).put(-0.5f).put( 0.5f);
        pb.put(-0.5f).put( 0.5f).put( 0.5f);
        pb.put(-0.5f).put( 0.5f).put(-0.5f);
        pb.put(-0.5f).put(-0.5f).put(-0.5f);
        for (int i = 0; i < 4; i++)
            cb.put(0.0f).put(1.0f).put(0.0f);
        pb.put( 0.5f).put( 0.5f).put( 0.5f);
        pb.put( 0.5f).put( 0.5f).put(-0.5f);
        pb.put(-0.5f).put( 0.5f).put(-0.5f);
        pb.put(-0.5f).put( 0.5f).put( 0.5f);
        for (int i = 0; i < 4; i++)
            cb.put(0.0f).put(0.2f).put(0.0f);
        pb.put( 0.5f).put(-0.5f).put(-0.5f);
        pb.put( 0.5f).put(-0.5f).put( 0.5f);
        pb.put(-0.5f).put(-0.5f).put( 0.5f);
        pb.put(-0.5f).put(-0.5f).put(-0.5f);
        pb.flip();
        cb.flip();
        // build element buffer
        IntBuffer eb = BufferUtils.createIntBuffer(6 * 6);
        for (int i = 0; i < 4 * 6; i += 4)
            eb.put(i).put(i+1).put(i+2).put(i+2).put(i+3).put(i+0);
        eb.flip();
        // setup vertex positions buffer
        int cubeVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cubeVbo);
        glBufferData(GL_ARRAY_BUFFER, pb, GL_STATIC_DRAW);
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(3, GL_FLOAT, 0, 0);
        // setup vertex color buffer
        int cubeCb = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cubeCb);
        glBufferData(GL_ARRAY_BUFFER, cb, GL_STATIC_DRAW);
        glEnableClientState(GL_COLOR_ARRAY);
        glColorPointer(3, GL_FLOAT, 0, 0);
        // setup element buffer
        int cubeEbo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, cubeEbo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, eb, GL_STATIC_DRAW);
    }

    void loop() {
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.6f, 0.7f, 0.8f, 1.0f);
        // Enable depth testing
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        // Remember the current time.
        long firstTime = System.nanoTime();

        buildCube();

        while ( !glfwWindowShouldClose(window) ) {
            // Build time difference between this and first time.
            long thisTime = System.nanoTime();
            float diff = (thisTime - firstTime) / 1E9f;
            // Compute some rotation angle.
            float angle = diff;

            // Make the viewport always fill the whole window.
            glViewport(0, 0, width, height);

            // Build the projection matrix in JOML by using some fixed vertical
            // field-of-view and compute the correct aspect ratio based
            // on window width and height. Make sure to cast them to float
            // before dividing, or else we would do an integer division!
            projMatrix.setPerspective((float) Math.toRadians(30.0f),
                                      (float)width/height, 0.01f, 100.0f);
            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(projMatrix.get(fb));

            // Build a model-view matrix which first rotates the cube
            // about the Y-axis and then lets a "camera" look at that
            // cube from a certain distance.
            viewMatrix.setLookAt(0.0f, 2.0f, 5.0f,
                                 0.0f, 0.0f, 0.0f,
                                 0.0f, 1.0f, 0.0f)
                      // rotate 90 degrees per second
                      .rotateY(angle * (float) Math.toRadians(90));
            glMatrixMode(GL_MODELVIEW);
            glLoadMatrixf(viewMatrix.get(fb));

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Render a simple cube
            glDrawElements(GL_TRIANGLES, 6 * 6, GL_UNSIGNED_INT, 0L);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new VboDemo().run();
    }
}
