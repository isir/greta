/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package greta.application.ariavaluspa.core;

/**
 *
 * @author Angelo Cafaro
 */
public enum InteractionState {

    IDLE,
    ENGAGING,
    ENGAGED,
    DISENGAGING;
    
    public static final int NUM_INTERACTION_STATES = values().length;

    public static InteractionState interpret(String stateName, InteractionState defaultStateName) {
        try {
            return valueOf(stateName);
        } catch (Throwable t) {
            return defaultStateName;
        }
    }

    public static InteractionState interpret(String stateName) {
        return interpret(stateName, IDLE);
    }
}
