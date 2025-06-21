package greta.auxiliary.emotion.model;

import java.time.Instant;
import java.util.Map;

/**
 * Result of voice emotion analysis
 */
public class VoiceEmotionResult {
    
    private String primaryEmotion;
    private double confidence;
    private Map<String, Double> emotionProbabilities;
    private double arousal;
    private double valence;
    private Instant timestamp;
    private long processingTimeMs;
    
    // Voice-specific metrics
    private double pitch;
    private double pitchVariation;
    private double energy;
    private double speakingRate;
    private double spectralCentroid;
    private double[] mfccFeatures;
    
    public VoiceEmotionResult() {
        this.timestamp = Instant.now();
    }
    
    public VoiceEmotionResult(String primaryEmotion, double confidence) {
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
    
    public double getPitch() { return pitch; }
    public void setPitch(double pitch) { this.pitch = pitch; }
    
    public double getPitchVariation() { return pitchVariation; }
    public void setPitchVariation(double pitchVariation) { this.pitchVariation = pitchVariation; }
    
    public double getEnergy() { return energy; }
    public void setEnergy(double energy) { this.energy = energy; }
    
    public double getSpeakingRate() { return speakingRate; }
    public void setSpeakingRate(double speakingRate) { this.speakingRate = speakingRate; }
    
    public double getSpectralCentroid() { return spectralCentroid; }
    public void setSpectralCentroid(double spectralCentroid) { this.spectralCentroid = spectralCentroid; }
    
    public double[] getMfccFeatures() { return mfccFeatures; }
    public void setMfccFeatures(double[] mfccFeatures) { this.mfccFeatures = mfccFeatures; }
}