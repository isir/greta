package org.joml.lwjgl;

public interface ViewSettings {

    /**
     * The distance between the viewer's eyes and the screen in some distance
     * measure (such as centimeters).
     */
    double distanceToScreen = 60.0;
    /**
     * The height of the screen area in the same distance measure (such as
     * centimeters).
     */
    double screenHeight = 32.5;
    /**
     * The vertical resolution of the screen in pixels.
     */
    int screenHeightPx = 1200;

}
