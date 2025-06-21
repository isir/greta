package greta.auxiliary.gestures.model;

import java.time.Instant;
import java.util.Map;

/**
 * Real-time input for gesture generation and modification
 */
public class RealTimeGestureInput {
    
    private String inputId;
    private String textFragment;
    private String emotionalState;
    private float arousal;
    private float valence;
    private Map<String, Float> biometricData;
    private Instant timestamp;
    private boolean isIncremental;
    private String contextWindow;
    private Map<String, Object> environmentalFactors;
    private String[] activeConstraints;
    
    public RealTimeGestureInput() {
        this.timestamp = Instant.now();
        this.isIncremental = true;
        this.arousal = 0.5f; // Neutral arousal
        this.valence = 0.5f; // Neutral valence
    }
    
    public RealTimeGestureInput(String textFragment) {
        this();
        this.textFragment = textFragment;
    }
    
    // Getters and setters
    public String getInputId() { return inputId; }
    public void setInputId(String inputId) { this.inputId = inputId; }
    
    public String getTextFragment() { return textFragment; }
    public void setTextFragment(String textFragment) { this.textFragment = textFragment; }
    
    public String getEmotionalState() { return emotionalState; }
    public void setEmotionalState(String emotionalState) { this.emotionalState = emotionalState; }
    
    public float getArousal() { return arousal; }
    public void setArousal(float arousal) { this.arousal = arousal; }
    
    public float getValence() { return valence; }
    public void setValence(float valence) { this.valence = valence; }
    
    public Map<String, Float> getBiometricData() { return biometricData; }
    public void setBiometricData(Map<String, Float> biometricData) { this.biometricData = biometricData; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public boolean isIncremental() { return isIncremental; }
    public void setIncremental(boolean incremental) { isIncremental = incremental; }
    
    public String getContextWindow() { return contextWindow; }
    public void setContextWindow(String contextWindow) { this.contextWindow = contextWindow; }
    
    public Map<String, Object> getEnvironmentalFactors() { return environmentalFactors; }
    public void setEnvironmentalFactors(Map<String, Object> environmentalFactors) { this.environmentalFactors = environmentalFactors; }
    
    public String[] getActiveConstraints() { return activeConstraints; }
    public void setActiveConstraints(String[] activeConstraints) { this.activeConstraints = activeConstraints; }
}