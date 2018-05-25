/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.enums;

/**
 * Closed set item for BML gaze signals directions. Defined in the BML standard.
 * @author Mathieu Chollet
 */

public enum GazeDirection {
    LEFT,
    RIGHT,
    UP,
    DOWN,
    FRONT,
    //BACK,
    UPRIGHT,
    UPLEFT,
    DOWNLEFT,
    DOWNRIGHT;

    public GazeDirection opposite(){
        if(this==LEFT)
            return RIGHT;
        if(this==RIGHT)
            return LEFT;
        if(this==UPRIGHT)
            return DOWNLEFT;
        if(this==DOWNLEFT)
            return UPRIGHT;
        if(this==DOWNRIGHT)
            return UPLEFT;
        if(this==UPLEFT)
            return DOWNRIGHT;
        if(this==UP)
            return DOWN;
        if(this==DOWN)
            return UP;

        return FRONT;
    }

}