package greta.auxiliary.gestures.model;

import java.time.Instant;
import java.util.Map;

/**
 * Request for generating new gestures using neural networks
 */
public class GestureGenerationRequest {
    
    private String requestId;
    private String textInput;
    private String emotionalContext;
    private String gestureStyle;
    private float intensity;
    private double duration;
    private Map<String, Object> parameters;
    private Instant timestamp;
    private String characterId;
    private String language;
    private boolean realTimeMode;
    private String[] constraints;
    
    public GestureGenerationRequest() {
        this.timestamp = Instant.now();
        this.intensity = 1.0f;
        this.realTimeMode = false;
    }
    
    public GestureGenerationRequest(String textInput) {
        this();
        this.textInput = textInput;
    }
    
    // Getters and setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public String getTextInput() { return textInput; }
    public void setTextInput(String textInput) { this.textInput = textInput; }
    
    public String getEmotionalContext() { return emotionalContext; }
    public void setEmotionalContext(String emotionalContext) { this.emotionalContext = emotionalContext; }
    
    public String getGestureStyle() { return gestureStyle; }
    public void setGestureStyle(String gestureStyle) { this.gestureStyle = gestureStyle; }
    
    public float getIntensity() { return intensity; }
    public void setIntensity(float intensity) { this.intensity = intensity; }
    
    public double getDuration() { return duration; }
    public void setDuration(double duration) { this.duration = duration; }
    
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getCharacterId() { return characterId; }
    public void setCharacterId(String characterId) { this.characterId = characterId; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public boolean isRealTimeMode() { return realTimeMode; }
    public void setRealTimeMode(boolean realTimeMode) { this.realTimeMode = realTimeMode; }
    
    public String[] getConstraints() { return constraints; }
    public void setConstraints(String[] constraints) { this.constraints = constraints; }
}