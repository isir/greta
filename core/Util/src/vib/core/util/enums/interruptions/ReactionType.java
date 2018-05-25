/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.enums.interruptions;

/**
 * This is an enumeration of interruption reactions.
 *
 * @author Brice Donval
 * @author Angelo Cafaro
 */
public enum ReactionType {

    NONE,
    HALT,
    OVERLAP,
    REPLAN;

    public static ReactionType interpret(String reactionType) {
        try {
            return valueOf(reactionType.toUpperCase());
        } catch (Throwable t) {
            return NONE;
        }
    }
}
