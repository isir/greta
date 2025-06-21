package greta.auxiliary.gestures.model;

import java.time.Instant;
import java.util.Map;

/**
 * Represents the communicative intention behind a gesture
 */
public class CommunicativeIntention {
    
    private String intentionId;
    private String primaryIntention;
    private String[] secondaryIntentions;
    private float intensity;
    private String modalityPreference;
    private Map<String, Float> intentionWeights;
    private String communicativeGoal;
    private Instant timestamp;
    private String urgency;
    private Map<String, Object> contextualFactors;
    
    public CommunicativeIntention() {
        this.timestamp = Instant.now();
        this.intensity = 1.0f;
        this.urgency = "normal";
    }
    
    public CommunicativeIntention(String primaryIntention) {
        this();
        this.primaryIntention = primaryIntention;
    }
    
    // Getters and setters
    public String getIntentionId() { return intentionId; }
    public void setIntentionId(String intentionId) { this.intentionId = intentionId; }
    
    public String getPrimaryIntention() { return primaryIntention; }
    public void setPrimaryIntention(String primaryIntention) { this.primaryIntention = primaryIntention; }
    
    public String[] getSecondaryIntentions() { return secondaryIntentions; }
    public void setSecondaryIntentions(String[] secondaryIntentions) { this.secondaryIntentions = secondaryIntentions; }
    
    public float getIntensity() { return intensity; }
    public void setIntensity(float intensity) { this.intensity = intensity; }
    
    public String getModalityPreference() { return modalityPreference; }
    public void setModalityPreference(String modalityPreference) { this.modalityPreference = modalityPreference; }
    
    public Map<String, Float> getIntentionWeights() { return intentionWeights; }
    public void setIntentionWeights(Map<String, Float> intentionWeights) { this.intentionWeights = intentionWeights; }
    
    public String getCommunicativeGoal() { return communicativeGoal; }
    public void setCommunicativeGoal(String communicativeGoal) { this.communicativeGoal = communicativeGoal; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    
    public Map<String, Object> getContextualFactors() { return contextualFactors; }
    public void setContextualFactors(Map<String, Object> contextualFactors) { this.contextualFactors = contextualFactors; }
}