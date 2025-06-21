package greta.auxiliary.emotion.model;

import java.time.Instant;
import java.util.Map;

/**
 * Represents the current emotion state of a user
 */
public class EmotionState {
    
    // Primary emotion dimensions
    private String primaryEmotion;      // dominant emotion (happy, sad, angry, etc.)
    private double confidence;          // confidence in the primary emotion [0.0, 1.0]
    private double arousal;            // activation level [0.0, 1.0] (calm to excited)
    private double valence;            // pleasantness [-1.0, 1.0] (negative to positive)
    private double dominance;          // control/power [0.0, 1.0] (submissive to dominant)
    
    // Secondary emotions with probabilities
    private Map<String, Double> emotionProbabilities;
    
    // Temporal information
    private Instant timestamp;
    private long durationMs;           // how long this state has been maintained
    
    // Source information
    private String[] detectedModalities; // which modalities contributed to this detection
    private double voiceConfidence;
    private double facialConfidence;
    private double physiologicalConfidence;
    private double fusedConfidence;
    
    // Context information
    private EmotionContext context;
    
    public EmotionState() {
        this.timestamp = Instant.now();
    }
    
    public EmotionState(String primaryEmotion, double confidence) {
        this();
        this.primaryEmotion = primaryEmotion;
        this.confidence = confidence;
    }
    
    /**
     * Get the emotion intensity on a scale of 0.0 to 1.0
     */
    public double getIntensity() {
        return Math.sqrt(arousal * arousal + Math.abs(valence));
    }
    
    /**
     * Get the emotion as a categorical label with intensity
     */
    public String getEmotionWithIntensity() {
        String intensityLabel;
        double intensity = getIntensity();
        
        if (intensity < 0.3) {
            intensityLabel = "low";
        } else if (intensity < 0.7) {
            intensityLabel = "medium";
        } else {
            intensityLabel = "high";
        }
        
        return intensityLabel + "_" + primaryEmotion;
    }
    
    /**
     * Check if this emotion state is significantly different from another
     */
    public boolean isSignificantlyDifferentFrom(EmotionState other, double threshold) {
        if (other == null) return true;
        
        if (!this.primaryEmotion.equals(other.primaryEmotion)) {
            return true;
        }
        
        double arousalDiff = Math.abs(this.arousal - other.arousal);
        double valenceDiff = Math.abs(this.valence - other.valence);
        double confidenceDiff = Math.abs(this.confidence - other.confidence);
        
        return arousalDiff > threshold || valenceDiff > threshold || confidenceDiff > threshold;
    }
    
    /**
     * Get a human-readable description of the emotion state
     */
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        
        desc.append("Emotion: ").append(primaryEmotion);
        desc.append(" (").append(String.format("%.1f%%", confidence * 100)).append(" confidence)");
        
        if (arousal > 0.7) {
            desc.append(", highly aroused");
        } else if (arousal < 0.3) {
            desc.append(", calm");
        }
        
        if (valence > 0.5) {
            desc.append(", positive");
        } else if (valence < -0.5) {
            desc.append(", negative");
        } else {
            desc.append(", neutral");
        }
        
        if (dominance > 0.7) {
            desc.append(", assertive");
        } else if (dominance < 0.3) {
            desc.append(", submissive");
        }
        
        return desc.toString();
    }
    
    // Getters and setters
    public String getPrimaryEmotion() { return primaryEmotion; }
    public void setPrimaryEmotion(String primaryEmotion) { this.primaryEmotion = primaryEmotion; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public double getArousal() { return arousal; }
    public void setArousal(double arousal) { this.arousal = arousal; }
    
    public double getValence() { return valence; }
    public void setValence(double valence) { this.valence = valence; }
    
    public double getDominance() { return dominance; }
    public void setDominance(double dominance) { this.dominance = dominance; }
    
    public Map<String, Double> getEmotionProbabilities() { return emotionProbabilities; }
    public void setEmotionProbabilities(Map<String, Double> emotionProbabilities) { this.emotionProbabilities = emotionProbabilities; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    
    public String[] getDetectedModalities() { return detectedModalities; }
    public void setDetectedModalities(String[] detectedModalities) { this.detectedModalities = detectedModalities; }
    
    public double getVoiceConfidence() { return voiceConfidence; }
    public void setVoiceConfidence(double voiceConfidence) { this.voiceConfidence = voiceConfidence; }
    
    public double getFacialConfidence() { return facialConfidence; }
    public void setFacialConfidence(double facialConfidence) { this.facialConfidence = facialConfidence; }
    
    public double getPhysiologicalConfidence() { return physiologicalConfidence; }
    public void setPhysiologicalConfidence(double physiologicalConfidence) { this.physiologicalConfidence = physiologicalConfidence; }
    
    public double getFusedConfidence() { return fusedConfidence; }
    public void setFusedConfidence(double fusedConfidence) { this.fusedConfidence = fusedConfidence; }
    
    public EmotionContext getContext() { return context; }
    public void setContext(EmotionContext context) { this.context = context; }
    
    /**
     * Context information for emotion recognition
     */
    public static class EmotionContext {
        private String conversationTopic;
        private String socialSetting;      // formal, informal, educational, therapeutic
        private String culturalContext;    // cultural background considerations
        private String environmentalContext; // noisy, quiet, indoor, outdoor
        private String timeOfDay;         // morning, afternoon, evening, night
        private String userMood;          // overall user mood trend
        private Map<String, Object> customContext;
        
        // Getters and setters
        public String getConversationTopic() { return conversationTopic; }
        public void setConversationTopic(String conversationTopic) { this.conversationTopic = conversationTopic; }
        
        public String getSocialSetting() { return socialSetting; }
        public void setSocialSetting(String socialSetting) { this.socialSetting = socialSetting; }
        
        public String getCulturalContext() { return culturalContext; }
        public void setCulturalContext(String culturalContext) { this.culturalContext = culturalContext; }
        
        public String getEnvironmentalContext() { return environmentalContext; }
        public void setEnvironmentalContext(String environmentalContext) { this.environmentalContext = environmentalContext; }
        
        public String getTimeOfDay() { return timeOfDay; }
        public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }
        
        public String getUserMood() { return userMood; }
        public void setUserMood(String userMood) { this.userMood = userMood; }
        
        public Map<String, Object> getCustomContext() { return customContext; }
        public void setCustomContext(Map<String, Object> customContext) { this.customContext = customContext; }
    }
}