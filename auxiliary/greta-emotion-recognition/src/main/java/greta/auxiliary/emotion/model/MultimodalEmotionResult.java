package greta.auxiliary.emotion.model;

import java.time.Instant;
import java.util.Map;

/**
 * Result of multimodal emotion analysis
 */
public class MultimodalEmotionResult {
    
    private String primaryEmotion;
    private double confidence;
    private Map<String, Double> emotionProbabilities;
    private double arousal;
    private double valence;
    private double dominance;
    private Instant timestamp;
    private long processingTimeMs;
    
    // Individual modality results
    private VoiceEmotionResult voiceResult;
    private FacialEmotionResult facialResult;
    private PhysiologicalEmotionResult physiologicalResult;
    
    // Fusion information
    private String fusionMethod;
    private double[] modalityWeights;
    
    public MultimodalEmotionResult() {
        this.timestamp = Instant.now();
    }
    
    public MultimodalEmotionResult(String primaryEmotion, double confidence) {
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
    
    public double getDominance() { return dominance; }
    public void setDominance(double dominance) { this.dominance = dominance; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    
    public VoiceEmotionResult getVoiceResult() { return voiceResult; }
    public void setVoiceResult(VoiceEmotionResult voiceResult) { this.voiceResult = voiceResult; }
    
    public FacialEmotionResult getFacialResult() { return facialResult; }
    public void setFacialResult(FacialEmotionResult facialResult) { this.facialResult = facialResult; }
    
    public PhysiologicalEmotionResult getPhysiologicalResult() { return physiologicalResult; }
    public void setPhysiologicalResult(PhysiologicalEmotionResult physiologicalResult) { this.physiologicalResult = physiologicalResult; }
    
    public String getFusionMethod() { return fusionMethod; }
    public void setFusionMethod(String fusionMethod) { this.fusionMethod = fusionMethod; }
    
    public double[] getModalityWeights() { return modalityWeights; }
    public void setModalityWeights(double[] modalityWeights) { this.modalityWeights = modalityWeights; }
}