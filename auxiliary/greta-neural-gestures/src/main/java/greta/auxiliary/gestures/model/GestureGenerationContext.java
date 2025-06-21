package greta.auxiliary.gestures.model;

import java.time.Instant;
import java.util.Map;

/**
 * Context information for gesture generation
 */
public class GestureGenerationContext {
    
    private String contextId;
    private String conversationalContext;
    private String emotionalContext;
    private String culturalContext;
    private Map<String, Object> environmentalFactors;
    private String[] previousGestures;
    private String currentSpeakerRole;
    private String audienceType;
    private Instant contextTimestamp;
    private double contextDuration;
    private String interactionMode;
    private Map<String, Float> socialFactors;
    
    public GestureGenerationContext() {
        this.contextTimestamp = Instant.now();
        this.interactionMode = "default";
    }
    
    public GestureGenerationContext(String contextId) {
        this();
        this.contextId = contextId;
    }
    
    // Getters and setters
    public String getContextId() { return contextId; }
    public void setContextId(String contextId) { this.contextId = contextId; }
    
    public String getConversationalContext() { return conversationalContext; }
    public void setConversationalContext(String conversationalContext) { this.conversationalContext = conversationalContext; }
    
    public String getEmotionalContext() { return emotionalContext; }
    public void setEmotionalContext(String emotionalContext) { this.emotionalContext = emotionalContext; }
    
    public String getCulturalContext() { return culturalContext; }
    public void setCulturalContext(String culturalContext) { this.culturalContext = culturalContext; }
    
    public Map<String, Object> getEnvironmentalFactors() { return environmentalFactors; }
    public void setEnvironmentalFactors(Map<String, Object> environmentalFactors) { this.environmentalFactors = environmentalFactors; }
    
    public String[] getPreviousGestures() { return previousGestures; }
    public void setPreviousGestures(String[] previousGestures) { this.previousGestures = previousGestures; }
    
    public String getCurrentSpeakerRole() { return currentSpeakerRole; }
    public void setCurrentSpeakerRole(String currentSpeakerRole) { this.currentSpeakerRole = currentSpeakerRole; }
    
    public String getAudienceType() { return audienceType; }
    public void setAudienceType(String audienceType) { this.audienceType = audienceType; }
    
    public Instant getContextTimestamp() { return contextTimestamp; }
    public void setContextTimestamp(Instant contextTimestamp) { this.contextTimestamp = contextTimestamp; }
    
    public double getContextDuration() { return contextDuration; }
    public void setContextDuration(double contextDuration) { this.contextDuration = contextDuration; }
    
    public String getInteractionMode() { return interactionMode; }
    public void setInteractionMode(String interactionMode) { this.interactionMode = interactionMode; }
    
    public Map<String, Float> getSocialFactors() { return socialFactors; }
    public void setSocialFactors(Map<String, Float> socialFactors) { this.socialFactors = socialFactors; }
}