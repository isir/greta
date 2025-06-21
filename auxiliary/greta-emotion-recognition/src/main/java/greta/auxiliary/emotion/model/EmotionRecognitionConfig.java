package greta.auxiliary.emotion.model;

import java.util.Map;

/**
 * Configuration for emotion recognition engine
 */
public class EmotionRecognitionConfig {
    
    private boolean voiceEnabled = true;
    private boolean facialEnabled = true;
    private boolean physiologicalEnabled = false;
    private boolean multimodalFusion = true;
    
    private double confidenceThreshold = 0.5;
    private int processingIntervalMs = 1000;
    private int historyLength = 10;
    
    private String voiceModelPath;
    private String facialModelPath;
    private String physiologicalModelPath;
    private String fusionModelPath;
    
    private Map<String, Object> customParameters;
    
    public EmotionRecognitionConfig() {}
    
    // Getters and setters
    public boolean isVoiceEnabled() { return voiceEnabled; }
    public void setVoiceEnabled(boolean voiceEnabled) { this.voiceEnabled = voiceEnabled; }
    
    public boolean isFacialEnabled() { return facialEnabled; }
    public void setFacialEnabled(boolean facialEnabled) { this.facialEnabled = facialEnabled; }
    
    public boolean isPhysiologicalEnabled() { return physiologicalEnabled; }
    public void setPhysiologicalEnabled(boolean physiologicalEnabled) { this.physiologicalEnabled = physiologicalEnabled; }
    
    public boolean isMultimodalFusion() { return multimodalFusion; }
    public void setMultimodalFusion(boolean multimodalFusion) { this.multimodalFusion = multimodalFusion; }
    
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public void setConfidenceThreshold(double confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }
    
    public int getProcessingIntervalMs() { return processingIntervalMs; }
    public void setProcessingIntervalMs(int processingIntervalMs) { this.processingIntervalMs = processingIntervalMs; }
    
    public int getHistoryLength() { return historyLength; }
    public void setHistoryLength(int historyLength) { this.historyLength = historyLength; }
    
    public String getVoiceModelPath() { return voiceModelPath; }
    public void setVoiceModelPath(String voiceModelPath) { this.voiceModelPath = voiceModelPath; }
    
    public String getFacialModelPath() { return facialModelPath; }
    public void setFacialModelPath(String facialModelPath) { this.facialModelPath = facialModelPath; }
    
    public String getPhysiologicalModelPath() { return physiologicalModelPath; }
    public void setPhysiologicalModelPath(String physiologicalModelPath) { this.physiologicalModelPath = physiologicalModelPath; }
    
    public String getFusionModelPath() { return fusionModelPath; }
    public void setFusionModelPath(String fusionModelPath) { this.fusionModelPath = fusionModelPath; }
    
    public Map<String, Object> getCustomParameters() { return customParameters; }
    public void setCustomParameters(Map<String, Object> customParameters) { this.customParameters = customParameters; }
}