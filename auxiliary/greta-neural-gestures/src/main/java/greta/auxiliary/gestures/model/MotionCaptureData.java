package greta.auxiliary.gestures.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Motion capture data for gesture analysis and synthesis
 */
public class MotionCaptureData {
    
    private String id;
    private String sessionId;
    private List<GestureFrame> frames;
    private double frameRate;
    private double duration;
    private Map<String, String> markerLabels;
    private String captureSystem;
    private Instant captureTime;
    private Map<String, Object> metadata;
    private String subjectId;
    private String[] calibrationData;
    private boolean isProcessed;
    
    public MotionCaptureData() {
        this.captureTime = Instant.now();
        this.isProcessed = false;
        this.frameRate = 120.0; // Default 120 FPS
    }
    
    public MotionCaptureData(String id, String sessionId) {
        this();
        this.id = id;
        this.sessionId = sessionId;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public List<GestureFrame> getFrames() { return frames; }
    public void setFrames(List<GestureFrame> frames) { this.frames = frames; }
    
    public double getFrameRate() { return frameRate; }
    public void setFrameRate(double frameRate) { this.frameRate = frameRate; }
    
    public double getDuration() { return duration; }
    public void setDuration(double duration) { this.duration = duration; }
    
    public Map<String, String> getMarkerLabels() { return markerLabels; }
    public void setMarkerLabels(Map<String, String> markerLabels) { this.markerLabels = markerLabels; }
    
    public String getCaptureSystem() { return captureSystem; }
    public void setCaptureSystem(String captureSystem) { this.captureSystem = captureSystem; }
    
    public Instant getCaptureTime() { return captureTime; }
    public void setCaptureTime(Instant captureTime) { this.captureTime = captureTime; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    
    public String[] getCalibrationData() { return calibrationData; }
    public void setCalibrationData(String[] calibrationData) { this.calibrationData = calibrationData; }
    
    public boolean isProcessed() { return isProcessed; }
    public void setProcessed(boolean processed) { isProcessed = processed; }
}