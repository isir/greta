package greta.auxiliary.multimodal.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents spatial understanding of the environment
 */
public class SpatialUnderstanding {
    
    private String environmentType;
    private List<SpatialObject> objects;
    private Map<String, Float> spatialRelations;
    private float[] roomDimensions;
    private float confidence;
    private Instant timestamp;
    private Map<String, Object> metadata;
    
    public SpatialUnderstanding() {
        this.timestamp = Instant.now();
        this.confidence = 0.0f;
    }
    
    /**
     * Represents an object in 3D space
     */
    public static class SpatialObject {
        private String id;
        private String type;
        private float[] position; // x, y, z
        private float[] rotation; // roll, pitch, yaw
        private float[] dimensions; // width, height, depth
        private Map<String, Object> properties;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public float[] getPosition() { return position; }
        public void setPosition(float[] position) { this.position = position; }
        
        public float[] getRotation() { return rotation; }
        public void setRotation(float[] rotation) { this.rotation = rotation; }
        
        public float[] getDimensions() { return dimensions; }
        public void setDimensions(float[] dimensions) { this.dimensions = dimensions; }
        
        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    }
    
    // Getters and setters
    public String getEnvironmentType() { return environmentType; }
    public void setEnvironmentType(String environmentType) { this.environmentType = environmentType; }
    
    public List<SpatialObject> getObjects() { return objects; }
    public void setObjects(List<SpatialObject> objects) { this.objects = objects; }
    
    public Map<String, Float> getSpatialRelations() { return spatialRelations; }
    public void setSpatialRelations(Map<String, Float> spatialRelations) { this.spatialRelations = spatialRelations; }
    
    public float[] getRoomDimensions() { return roomDimensions; }
    public void setRoomDimensions(float[] roomDimensions) { this.roomDimensions = roomDimensions; }
    
    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { this.confidence = confidence; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}