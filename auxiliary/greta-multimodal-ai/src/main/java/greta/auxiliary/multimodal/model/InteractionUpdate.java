package greta.auxiliary.multimodal.model;

import java.time.Instant;
import java.util.Map;

/**
 * Real-time update during interaction
 */
public class InteractionUpdate {
    
    private String updateId;
    private String updateType;
    private Object data;
    private float progress;
    private Instant timestamp;
    private Map<String, Object> metadata;
    
    public InteractionUpdate() {
        this.timestamp = Instant.now();
    }
    
    // Getters and setters
    public String getUpdateId() { return updateId; }
    public void setUpdateId(String updateId) { this.updateId = updateId; }
    
    public String getUpdateType() { return updateType; }
    public void setUpdateType(String updateType) { this.updateType = updateType; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    public float getProgress() { return progress; }
    public void setProgress(float progress) { this.progress = progress; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}