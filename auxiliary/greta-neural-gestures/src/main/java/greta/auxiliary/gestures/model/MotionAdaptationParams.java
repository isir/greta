package greta.auxiliary.gestures.model;

import java.util.Map;

/**
 * Parameters for adapting motion capture data to different characters or styles
 */
public class MotionAdaptationParams {
    
    private String targetCharacterType;
    private Map<String, Float> bodyProportions;
    private Map<String, Float> jointLimits;
    private float adaptationStrength;
    private String adaptationMethod;
    private boolean preserveStyle;
    private boolean scaleMotion;
    private Map<String, Object> customParameters;
    private String[] adaptationConstraints;
    
    public MotionAdaptationParams() {
        this.adaptationStrength = 1.0f;
        this.adaptationMethod = "proportional";
        this.preserveStyle = true;
        this.scaleMotion = true;
    }
    
    public MotionAdaptationParams(String targetCharacterType) {
        this();
        this.targetCharacterType = targetCharacterType;
    }
    
    // Getters and setters
    public String getTargetCharacterType() { return targetCharacterType; }
    public void setTargetCharacterType(String targetCharacterType) { this.targetCharacterType = targetCharacterType; }
    
    public Map<String, Float> getBodyProportions() { return bodyProportions; }
    public void setBodyProportions(Map<String, Float> bodyProportions) { this.bodyProportions = bodyProportions; }
    
    public Map<String, Float> getJointLimits() { return jointLimits; }
    public void setJointLimits(Map<String, Float> jointLimits) { this.jointLimits = jointLimits; }
    
    public float getAdaptationStrength() { return adaptationStrength; }
    public void setAdaptationStrength(float adaptationStrength) { this.adaptationStrength = adaptationStrength; }
    
    public String getAdaptationMethod() { return adaptationMethod; }
    public void setAdaptationMethod(String adaptationMethod) { this.adaptationMethod = adaptationMethod; }
    
    public boolean isPreserveStyle() { return preserveStyle; }
    public void setPreserveStyle(boolean preserveStyle) { this.preserveStyle = preserveStyle; }
    
    public boolean isScaleMotion() { return scaleMotion; }
    public void setScaleMotion(boolean scaleMotion) { this.scaleMotion = scaleMotion; }
    
    public Map<String, Object> getCustomParameters() { return customParameters; }
    public void setCustomParameters(Map<String, Object> customParameters) { this.customParameters = customParameters; }
    
    public String[] getAdaptationConstraints() { return adaptationConstraints; }
    public void setAdaptationConstraints(String[] adaptationConstraints) { this.adaptationConstraints = adaptationConstraints; }
}