package greta.auxiliary.emotion.model;

import java.time.Instant;
import java.util.Map;

/**
 * User-specific calibration data for emotion recognition
 */
public class UserCalibrationData {
    
    private String userId;
    private Map<String, Double> emotionBaselines;
    private Map<String, Double> voiceBaselines;
    private Map<String, Double> facialBaselines;
    private Map<String, Double> physiologicalBaselines;
    private Instant calibrationTimestamp;
    private String culturalBackground;
    private int age;
    private String gender;
    private String[] preferredEmotionLabels;
    
    public UserCalibrationData() {
        this.calibrationTimestamp = Instant.now();
    }
    
    public UserCalibrationData(String userId) {
        this();
        this.userId = userId;
    }
    
    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Map<String, Double> getEmotionBaselines() { return emotionBaselines; }
    public void setEmotionBaselines(Map<String, Double> emotionBaselines) { this.emotionBaselines = emotionBaselines; }
    
    public Map<String, Double> getVoiceBaselines() { return voiceBaselines; }
    public void setVoiceBaselines(Map<String, Double> voiceBaselines) { this.voiceBaselines = voiceBaselines; }
    
    public Map<String, Double> getFacialBaselines() { return facialBaselines; }
    public void setFacialBaselines(Map<String, Double> facialBaselines) { this.facialBaselines = facialBaselines; }
    
    public Map<String, Double> getPhysiologicalBaselines() { return physiologicalBaselines; }
    public void setPhysiologicalBaselines(Map<String, Double> physiologicalBaselines) { this.physiologicalBaselines = physiologicalBaselines; }
    
    public Instant getCalibrationTimestamp() { return calibrationTimestamp; }
    public void setCalibrationTimestamp(Instant calibrationTimestamp) { this.calibrationTimestamp = calibrationTimestamp; }
    
    public String getCulturalBackground() { return culturalBackground; }
    public void setCulturalBackground(String culturalBackground) { this.culturalBackground = culturalBackground; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String[] getPreferredEmotionLabels() { return preferredEmotionLabels; }
    public void setPreferredEmotionLabels(String[] preferredEmotionLabels) { this.preferredEmotionLabels = preferredEmotionLabels; }
}