package greta.auxiliary.gestures.model;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a single frame of gesture data in a motion sequence
 */
public class GestureFrame {
    
    private double timestamp;
    private Map<String, Float> jointPositions; // Joint name to position mapping
    private Map<String, Float> jointRotations; // Joint name to rotation mapping
    private float confidence;
    private String gestureType;
    private Map<String, Object> metadata;
    private Instant frameTime;
    
    public GestureFrame() {
        this.frameTime = Instant.now();
        this.confidence = 1.0f;
    }
    
    public GestureFrame(double timestamp) {
        this();
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public double getTimestamp() { return timestamp; }
    public void setTimestamp(double timestamp) { this.timestamp = timestamp; }
    
    public Map<String, Float> getJointPositions() { return jointPositions; }
    public void setJointPositions(Map<String, Float> jointPositions) { this.jointPositions = jointPositions; }
    
    public Map<String, Float> getJointRotations() { return jointRotations; }
    public void setJointRotations(Map<String, Float> jointRotations) { this.jointRotations = jointRotations; }
    
    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { this.confidence = confidence; }
    
    public String getGestureType() { return gestureType; }
    public void setGestureType(String gestureType) { this.gestureType = gestureType; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public Instant getFrameTime() { return frameTime; }
    public void setFrameTime(Instant frameTime) { this.frameTime = frameTime; }
}