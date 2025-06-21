package greta.auxiliary.emotion.model;

import java.time.Instant;
import java.util.Map;

/**
 * Result of physiological emotion analysis
 */
public class PhysiologicalEmotionResult {
    
    private String primaryEmotion;
    private double confidence;
    private Map<String, Double> emotionProbabilities;
    private double arousal;
    private double valence;
    private Instant timestamp;
    private long processingTimeMs;
    
    public PhysiologicalEmotionResult() {
        this.timestamp = Instant.now();
    }
    
    public PhysiologicalEmotionResult(String primaryEmotion, double confidence) {
        this();
        this.primaryEmotion = primaryEmotion;
        this.confidence = confidence;
    }
    
    // Getters and setters
    public String getPrimaryEmotion() { return primaryEmotion; }
    public void setPrimaryEmotion(String primaryEmotion) { this.primaryEmotion = primaryEmotion; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public Map<String, Double> getEmotionProbabilities() { return emotionProbabilities; }
    public void setEmotionProbabilities(Map<String, Double> emotionProbabilities) { this.emotionProbabilities = emotionProbabilities; }
    
    public double getArousal() { return arousal; }
    public void setArousal(double arousal) { this.arousal = arousal; }
    
    public double getValence() { return valence; }
    public void setValence(double valence) { this.valence = valence; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
}