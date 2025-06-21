package greta.auxiliary.gestures.model;

import java.util.Map;

/**
 * Criteria for refining and improving generated gestures
 */
public class RefinementCriteria {
    
    private float smoothnessWeight;
    private float naturalnessWeight;
    private float expressivenessWeight;
    private float temporalConsistencyWeight;
    private float spatialConsistencyWeight;
    private Map<String, Float> jointConstraints;
    private Map<String, Object> stylePreferences;
    private boolean preserveKeyframes;
    private float noiseReduction;
    private String optimizationTarget;
    
    public RefinementCriteria() {
        // Default weights
        this.smoothnessWeight = 0.3f;
        this.naturalnessWeight = 0.3f;
        this.expressivenessWeight = 0.2f;
        this.temporalConsistencyWeight = 0.1f;
        this.spatialConsistencyWeight = 0.1f;
        this.preserveKeyframes = true;
        this.noiseReduction = 0.1f;
        this.optimizationTarget = "balanced";
    }
    
    // Getters and setters
    public float getSmoothnessWeight() { return smoothnessWeight; }
    public void setSmoothnessWeight(float smoothnessWeight) { this.smoothnessWeight = smoothnessWeight; }
    
    public float getNaturalnessWeight() { return naturalnessWeight; }
    public void setNaturalnessWeight(float naturalnessWeight) { this.naturalnessWeight = naturalnessWeight; }
    
    public float getExpressivenessWeight() { return expressivenessWeight; }
    public void setExpressivenessWeight(float expressivenessWeight) { this.expressivenessWeight = expressivenessWeight; }
    
    public float getTemporalConsistencyWeight() { return temporalConsistencyWeight; }
    public void setTemporalConsistencyWeight(float temporalConsistencyWeight) { this.temporalConsistencyWeight = temporalConsistencyWeight; }
    
    public float getSpatialConsistencyWeight() { return spatialConsistencyWeight; }
    public void setSpatialConsistencyWeight(float spatialConsistencyWeight) { this.spatialConsistencyWeight = spatialConsistencyWeight; }
    
    public Map<String, Float> getJointConstraints() { return jointConstraints; }
    public void setJointConstraints(Map<String, Float> jointConstraints) { this.jointConstraints = jointConstraints; }
    
    public Map<String, Object> getStylePreferences() { return stylePreferences; }
    public void setStylePreferences(Map<String, Object> stylePreferences) { this.stylePreferences = stylePreferences; }
    
    public boolean isPreserveKeyframes() { return preserveKeyframes; }
    public void setPreserveKeyframes(boolean preserveKeyframes) { this.preserveKeyframes = preserveKeyframes; }
    
    public float getNoiseReduction() { return noiseReduction; }
    public void setNoiseReduction(float noiseReduction) { this.noiseReduction = noiseReduction; }
    
    public String getOptimizationTarget() { return optimizationTarget; }
    public void setOptimizationTarget(String optimizationTarget) { this.optimizationTarget = optimizationTarget; }
}