/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.enums;

/**
 * This is an enumeration of social attitude descriptors.
 *
 * @author Angelo Cafaro
 */
public enum SocialAttitude {

    /**
     * Describes a neutral social attitude
     */
    neutral,
    /**
     * Describe a submissive social attitude (or dominance decrease)
     */
    submissive,
    /**
     * Describe a dominant social attitude (or dominance increase)
     */
    dominant,
    /**
     * Describe an hostile social attitude (or friendliness decrease)
     */
    hostile,
    /**
     * Describe a friendly social attitude (or friendliness increase)
     */
    friendly;

    public static SocialAttitude interpret(String aSocialAttitude, SocialAttitude defaultSocialAttitude) {
        try {
            return valueOf(aSocialAttitude);
        } catch (Throwable t) {
            return defaultSocialAttitude;
        }
    }

    public static SocialAttitude interpret(String aSocialAttitude) {
        return interpret(aSocialAttitude, neutral);
    }
}
