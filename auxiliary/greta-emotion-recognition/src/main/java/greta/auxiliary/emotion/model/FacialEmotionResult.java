package greta.auxiliary.emotion.model;

import java.time.Instant;
import java.util.Map;

/**
 * Result of facial emotion analysis
 */
public class FacialEmotionResult {
    
    private String primaryEmotion;
    private double confidence;
    private Map<String, Double> emotionProbabilities;
    private double arousal;
    private double valence;
    private Instant timestamp;
    private long processingTimeMs;
    
    // Facial-specific metrics
    private FacialFeatures facialFeatures;
    private FaceRegion detectedFaceRegion;
    
    public FacialEmotionResult() {
        this.timestamp = Instant.now();
    }
    
    public FacialEmotionResult(String primaryEmotion, double confidence) {
        this();
        this.primaryEmotion = primaryEmotion;
        this.confidence = confidence;
    }
    
    /**
     * Facial features extracted from the face
     */
    public static class FacialFeatures {
        private double[] actionUnits;  // facial action units
        private double eyebrowRaise;
        private double eyeWiden;
        private double mouthOpen;
        private double smileIntensity;
        private double frownIntensity;
        
        // Getters and setters
        public double[] getActionUnits() { return actionUnits; }
        public void setActionUnits(double[] actionUnits) { this.actionUnits = actionUnits; }
        
        public double getEyebrowRaise() { return eyebrowRaise; }
        public void setEyebrowRaise(double eyebrowRaise) { this.eyebrowRaise = eyebrowRaise; }
        
        public double getEyeWiden() { return eyeWiden; }
        public void setEyeWiden(double eyeWiden) { this.eyeWiden = eyeWiden; }
        
        public double getMouthOpen() { return mouthOpen; }
        public void setMouthOpen(double mouthOpen) { this.mouthOpen = mouthOpen; }
        
        public double getSmileIntensity() { return smileIntensity; }
        public void setSmileIntensity(double smileIntensity) { this.smileIntensity = smileIntensity; }
        
        public double getFrownIntensity() { return frownIntensity; }
        public void setFrownIntensity(double frownIntensity) { this.frownIntensity = frownIntensity; }
    }
    
    /**
     * Face region in the analyzed image
     */
    public static class FaceRegion {
        private int x, y, width, height;
        private double confidence;
        
        public FaceRegion(int x, int y, int width, int height) {
            this.x = x; this.y = y; this.width = width; this.height = height;
        }
        
        // Getters and setters
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
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
    
    public FacialFeatures getFacialFeatures() { return facialFeatures; }
    public void setFacialFeatures(FacialFeatures facialFeatures) { this.facialFeatures = facialFeatures; }
    
    public FaceRegion getDetectedFaceRegion() { return detectedFaceRegion; }
    public void setDetectedFaceRegion(FaceRegion detectedFaceRegion) { this.detectedFaceRegion = detectedFaceRegion; }
}