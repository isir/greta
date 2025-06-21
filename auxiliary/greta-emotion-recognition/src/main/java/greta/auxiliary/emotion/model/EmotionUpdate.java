package greta.auxiliary.emotion.model;

import java.time.Instant;

/**
 * Real-time emotion update for streaming emotion recognition
 */
public class EmotionUpdate {
    
    private EmotionState currentState;
    private EmotionState previousState;
    private String updateType;  // "new", "changed", "stable"
    private Instant timestamp;
    private String[] triggeredModalities;
    private double changeSignificance;
    
    public EmotionUpdate() {
        this.timestamp = Instant.now();
    }
    
    public EmotionUpdate(EmotionState currentState, String updateType) {
        this();
        this.currentState = currentState;
        this.updateType = updateType;
    }
    
    // Getters and setters
    public EmotionState getCurrentState() { return currentState; }
    public void setCurrentState(EmotionState currentState) { this.currentState = currentState; }
    
    public EmotionState getPreviousState() { return previousState; }
    public void setPreviousState(EmotionState previousState) { this.previousState = previousState; }
    
    public String getUpdateType() { return updateType; }
    public void setUpdateType(String updateType) { this.updateType = updateType; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String[] getTriggeredModalities() { return triggeredModalities; }
    public void setTriggeredModalities(String[] triggeredModalities) { this.triggeredModalities = triggeredModalities; }
    
    public double getChangeSignificance() { return changeSignificance; }
    public void setChangeSignificance(double changeSignificance) { this.changeSignificance = changeSignificance; }
}