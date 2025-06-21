package greta.auxiliary.gestures.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents a sequence of gesture frames forming a complete gesture
 */
public class GestureSequence {
    
    private String id;
    private String name;
    private List<GestureFrame> frames;
    private double duration;
    private String gestureType;
    private float confidence;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private String source; // e.g., "neural_synthesis", "motion_capture", "manual"
    private boolean isLooping;
    private String[] tags;
    
    public GestureSequence() {
        this.createdAt = Instant.now();
        this.confidence = 1.0f;
        this.isLooping = false;
    }
    
    public GestureSequence(String id, String name) {
        this();
        this.id = id;
        this.name = name;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public List<GestureFrame> getFrames() { return frames; }
    public void setFrames(List<GestureFrame> frames) { this.frames = frames; }
    
    public double getDuration() { return duration; }
    public void setDuration(double duration) { this.duration = duration; }
    
    public String getGestureType() { return gestureType; }
    public void setGestureType(String gestureType) { this.gestureType = gestureType; }
    
    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { this.confidence = confidence; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public boolean isLooping() { return isLooping; }
    public void setLooping(boolean looping) { isLooping = looping; }
    
    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }
}