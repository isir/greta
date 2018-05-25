/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.enums.interruptions;

/**
 * This is an enumeration of interruption reaction durations.
 *
 * @author Brice Donval
 * @author Angelo Cafaro
 */
public enum ReactionDuration {

    NONE,
    EXTRA_SHORT,
    SHORT,
    MEDIUM,
    LONG;

    public static ReactionDuration interpret(String reactionDuration) {
        try {
            return valueOf(reactionDuration.toUpperCase());
        } catch (Throwable t) {
            return NONE;
        }
    }
}
