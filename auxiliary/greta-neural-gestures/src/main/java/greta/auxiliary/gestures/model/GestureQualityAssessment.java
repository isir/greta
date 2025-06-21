package greta.auxiliary.gestures.model;

import java.time.Instant;
import java.util.Map;

/**
 * Assessment of gesture quality and characteristics
 */
public class GestureQualityAssessment {
    
    private String assessmentId;
    private String gestureId;
    private float overallQuality;
    private float naturalness;
    private float expressiveness;
    private float temporalConsistency;
    private float spatialConsistency;
    private float semanticAlignment;
    private Map<String, Float> dimensionalScores;
    private String[] detectedIssues;
    private String[] recommendations;
    private Instant assessmentTime;
    private String assessmentMethod;
    
    public GestureQualityAssessment() {
        this.assessmentTime = Instant.now();
        this.assessmentMethod = "automatic";
    }
    
    public GestureQualityAssessment(String gestureId) {
        this();
        this.gestureId = gestureId;
    }
    
    // Getters and setters
    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }
    
    public String getGestureId() { return gestureId; }
    public void setGestureId(String gestureId) { this.gestureId = gestureId; }
    
    public float getOverallQuality() { return overallQuality; }
    public void setOverallQuality(float overallQuality) { this.overallQuality = overallQuality; }
    
    public float getNaturalness() { return naturalness; }
    public void setNaturalness(float naturalness) { this.naturalness = naturalness; }
    
    public float getExpressiveness() { return expressiveness; }
    public void setExpressiveness(float expressiveness) { this.expressiveness = expressiveness; }
    
    public float getTemporalConsistency() { return temporalConsistency; }
    public void setTemporalConsistency(float temporalConsistency) { this.temporalConsistency = temporalConsistency; }
    
    public float getSpatialConsistency() { return spatialConsistency; }
    public void setSpatialConsistency(float spatialConsistency) { this.spatialConsistency = spatialConsistency; }
    
    public float getSemanticAlignment() { return semanticAlignment; }
    public void setSemanticAlignment(float semanticAlignment) { this.semanticAlignment = semanticAlignment; }
    
    public Map<String, Float> getDimensionalScores() { return dimensionalScores; }
    public void setDimensionalScores(Map<String, Float> dimensionalScores) { this.dimensionalScores = dimensionalScores; }
    
    public String[] getDetectedIssues() { return detectedIssues; }
    public void setDetectedIssues(String[] detectedIssues) { this.detectedIssues = detectedIssues; }
    
    public String[] getRecommendations() { return recommendations; }
    public void setRecommendations(String[] recommendations) { this.recommendations = recommendations; }
    
    public Instant getAssessmentTime() { return assessmentTime; }
    public void setAssessmentTime(Instant assessmentTime) { this.assessmentTime = assessmentTime; }
    
    public String getAssessmentMethod() { return assessmentMethod; }
    public void setAssessmentMethod(String assessmentMethod) { this.assessmentMethod = assessmentMethod; }
}