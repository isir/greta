package greta.auxiliary.multimodal.model;

import java.time.Instant;
import java.util.Map;

/**
 * Represents the AI's understanding of a scene
 */
public class SceneUnderstanding {
    
    private String sceneId;
    private String sceneType;
    private String description;
    private Map<String, Object> entities;
    private Map<String, Float> attributes;
    private float confidence;
    private Instant timestamp;
    private Map<String, Object> metadata;
    
    public SceneUnderstanding() {
        this.timestamp = Instant.now();
        this.confidence = 0.0f;
    }
    
    // Getters and setters
    public String getSceneId() { return sceneId; }
    public void setSceneId(String sceneId) { this.sceneId = sceneId; }
    
    public String getSceneType() { return sceneType; }
    public void setSceneType(String sceneType) { this.sceneType = sceneType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Map<String, Object> getEntities() { return entities; }
    public void setEntities(Map<String, Object> entities) { this.entities = entities; }
    
    public Map<String, Float> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Float> attributes) { this.attributes = attributes; }
    
    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { this.confidence = confidence; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}