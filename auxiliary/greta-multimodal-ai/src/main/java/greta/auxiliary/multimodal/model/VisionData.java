package greta.auxiliary.multimodal.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Contains processed vision data from images or video frames
 */
public class VisionData {
    
    private String dataId;
    private byte[] rawData;
    private List<DetectedObject> detectedObjects;
    private Map<String, Float> sceneAttributes;
    private String sceneDescription;
    private float[] imageEmbedding;
    private Instant timestamp;
    private boolean processed;
    private String error;
    private Map<String, Object> metadata;
    
    public VisionData() {
        this.timestamp = Instant.now();
        this.processed = true;
    }
    
    /**
     * Represents a detected object in the visual scene
     */
    public static class DetectedObject {
        private String label;
        private float confidence;
        private BoundingBox boundingBox;
        private Map<String, Object> attributes;
        
        // Getters and setters
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        
        public float getConfidence() { return confidence; }
        public void setConfidence(float confidence) { this.confidence = confidence; }
        
        public BoundingBox getBoundingBox() { return boundingBox; }
        public void setBoundingBox(BoundingBox boundingBox) { this.boundingBox = boundingBox; }
        
        public Map<String, Object> getAttributes() { return attributes; }
        public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
    }
    
    /**
     * Bounding box for object detection
     */
    public static class BoundingBox {
        private float x, y, width, height;
        
        public BoundingBox() {}
        
        public BoundingBox(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        // Getters and setters
        public float getX() { return x; }
        public void setX(float x) { this.x = x; }
        
        public float getY() { return y; }
        public void setY(float y) { this.y = y; }
        
        public float getWidth() { return width; }
        public void setWidth(float width) { this.width = width; }
        
        public float getHeight() { return height; }
        public void setHeight(float height) { this.height = height; }
    }
    
    // Getters and setters
    public String getDataId() { return dataId; }
    public void setDataId(String dataId) { this.dataId = dataId; }
    
    public byte[] getRawData() { return rawData; }
    public void setRawData(byte[] rawData) { this.rawData = rawData; }
    
    public List<DetectedObject> getDetectedObjects() { return detectedObjects; }
    public void setDetectedObjects(List<DetectedObject> detectedObjects) { this.detectedObjects = detectedObjects; }
    
    public Map<String, Float> getSceneAttributes() { return sceneAttributes; }
    public void setSceneAttributes(Map<String, Float> sceneAttributes) { this.sceneAttributes = sceneAttributes; }
    
    public String getSceneDescription() { return sceneDescription; }
    public void setSceneDescription(String sceneDescription) { this.sceneDescription = sceneDescription; }
    
    public float[] getImageEmbedding() { return imageEmbedding; }
    public void setImageEmbedding(float[] imageEmbedding) { this.imageEmbedding = imageEmbedding; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}