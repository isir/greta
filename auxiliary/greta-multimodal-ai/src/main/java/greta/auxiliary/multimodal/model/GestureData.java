package greta.auxiliary.multimodal.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Contains processed gesture recognition data
 */
public class GestureData {
    
    private String dataId;
    private List<HandPose> handPoses;
    private BodyPose bodyPose;
    private String recognizedGesture;
    private float confidence;
    private Instant timestamp;
    private boolean processed;
    private String error;
    private Map<String, Object> metadata;
    
    public GestureData() {
        this.timestamp = Instant.now();
        this.processed = true;
        this.confidence = 0.0f;
    }
    
    /**
     * Represents hand pose data
     */
    public static class HandPose {
        private String handType; // "left" or "right"
        private List<Landmark> landmarks;
        private float confidence;
        
        // Getters and setters
        public String getHandType() { return handType; }
        public void setHandType(String handType) { this.handType = handType; }
        
        public List<Landmark> getLandmarks() { return landmarks; }
        public void setLandmarks(List<Landmark> landmarks) { this.landmarks = landmarks; }
        
        public float getConfidence() { return confidence; }
        public void setConfidence(float confidence) { this.confidence = confidence; }
    }
    
    /**
     * Represents body pose data
     */
    public static class BodyPose {
        private List<Landmark> landmarks;
        private float confidence;
        
        // Getters and setters
        public List<Landmark> getLandmarks() { return landmarks; }
        public void setLandmarks(List<Landmark> landmarks) { this.landmarks = landmarks; }
        
        public float getConfidence() { return confidence; }
        public void setConfidence(float confidence) { this.confidence = confidence; }
    }
    
    /**
     * Represents a pose landmark
     */
    public static class Landmark {
        private int id;
        private float x, y, z;
        private float visibility;
        
        public Landmark() {}
        
        public Landmark(int id, float x, float y, float z, float visibility) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.visibility = visibility;
        }
        
        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public float getX() { return x; }
        public void setX(float x) { this.x = x; }
        
        public float getY() { return y; }
        public void setY(float y) { this.y = y; }
        
        public float getZ() { return z; }
        public void setZ(float z) { this.z = z; }
        
        public float getVisibility() { return visibility; }
        public void setVisibility(float visibility) { this.visibility = visibility; }
    }
    
    // Getters and setters
    public String getDataId() { return dataId; }
    public void setDataId(String dataId) { this.dataId = dataId; }
    
    public List<HandPose> getHandPoses() { return handPoses; }
    public void setHandPoses(List<HandPose> handPoses) { this.handPoses = handPoses; }
    
    public BodyPose getBodyPose() { return bodyPose; }
    public void setBodyPose(BodyPose bodyPose) { this.bodyPose = bodyPose; }
    
    public String getRecognizedGesture() { return recognizedGesture; }
    public void setRecognizedGesture(String recognizedGesture) { this.recognizedGesture = recognizedGesture; }
    
    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { this.confidence = confidence; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}