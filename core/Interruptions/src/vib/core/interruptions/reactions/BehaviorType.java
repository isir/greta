/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.interruptions.reactions;

/**
 *
 * @author Angelo Cafaro
 */
public enum BehaviorType {

    NONE,
    HEAD_TILT,
    HEAD_NOD_TOSS,
    EYES_LIDS_CLOSE,
    EYES_BROWS,
    EYES_SQUEEZE,
    SMILE,
    GESTURE_HOLD,
    GESTURE_RETRACT,
    SHOULDERS_UP_FORWARD;

    public static BehaviorType interpret(String behaviorType) {
        try {
            return valueOf(behaviorType.toUpperCase());
        } catch (Throwable t) {
            return NONE;
        }
    }

}
