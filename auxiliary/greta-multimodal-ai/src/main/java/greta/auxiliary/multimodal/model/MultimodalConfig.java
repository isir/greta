package greta.auxiliary.multimodal.model;

import java.util.Map;

/**
 * Configuration for multimodal AI engine
 */
public class MultimodalConfig {
    
    private boolean visionEnabled;
    private boolean speechEnabled;
    private boolean gestureEnabled;
    private boolean emotionEnabled;
    private String visionModel;
    private String languageModel;
    private double confidenceThreshold;
    private Map<String, Object> customSettings;
    
    public MultimodalConfig() {
        this.visionEnabled = true;
        this.speechEnabled = true;
        this.gestureEnabled = true;
        this.emotionEnabled = true;
        this.confidenceThreshold = 0.7;
    }
    
    // Getters and setters
    public boolean isVisionEnabled() { return visionEnabled; }
    public void setVisionEnabled(boolean visionEnabled) { this.visionEnabled = visionEnabled; }
    
    public boolean isSpeechEnabled() { return speechEnabled; }
    public void setSpeechEnabled(boolean speechEnabled) { this.speechEnabled = speechEnabled; }
    
    public boolean isGestureEnabled() { return gestureEnabled; }
    public void setGestureEnabled(boolean gestureEnabled) { this.gestureEnabled = gestureEnabled; }
    
    public boolean isEmotionEnabled() { return emotionEnabled; }
    public void setEmotionEnabled(boolean emotionEnabled) { this.emotionEnabled = emotionEnabled; }
    
    public String getVisionModel() { return visionModel; }
    public void setVisionModel(String visionModel) { this.visionModel = visionModel; }
    
    public String getLanguageModel() { return languageModel; }
    public void setLanguageModel(String languageModel) { this.languageModel = languageModel; }
    
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public void setConfidenceThreshold(double confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }
    
    public Map<String, Object> getCustomSettings() { return customSettings; }
    public void setCustomSettings(Map<String, Object> customSettings) { this.customSettings = customSettings; }
}