/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.enums;

/**
 * This is an enumeration of animation composition types.
 *
 * @author Andre-Marie Pez
 */
public enum CompositionType {

    /**
     * The associated animation must replace the current animation
     */
    replace,
    /**
     * The associated animation must be blened with the current animation
     */
    blend,
    /**
     * The associated animation must be added after the current animation
     */
    append;

    public static CompositionType interpret(String name, CompositionType defaultCompositionType) {
        try {
            return valueOf(name);
        } catch (Throwable t) {
            return defaultCompositionType;
        }
    }

    public static CompositionType interpret(String name) {
        return interpret(name, blend);
    }
}
