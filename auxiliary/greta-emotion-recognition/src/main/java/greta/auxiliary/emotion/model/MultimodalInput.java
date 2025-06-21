package greta.auxiliary.emotion.model;

import java.time.Instant;

/**
 * Combined multimodal input for emotion analysis
 */
public class MultimodalInput {
    
    private AudioInput audioInput;
    private FaceInput faceInput;
    private PhysiologicalInput physiologicalInput;
    private Instant timestamp;
    private String sessionId;
    
    public MultimodalInput() {
        this.timestamp = Instant.now();
    }
    
    public MultimodalInput(AudioInput audioInput, FaceInput faceInput, PhysiologicalInput physiologicalInput) {
        this();
        this.audioInput = audioInput;
        this.faceInput = faceInput;
        this.physiologicalInput = physiologicalInput;
    }
    
    // Getters and setters
    public AudioInput getAudioInput() { return audioInput; }
    public void setAudioInput(AudioInput audioInput) { this.audioInput = audioInput; }
    
    public FaceInput getFaceInput() { return faceInput; }
    public void setFaceInput(FaceInput faceInput) { this.faceInput = faceInput; }
    
    public PhysiologicalInput getPhysiologicalInput() { return physiologicalInput; }
    public void setPhysiologicalInput(PhysiologicalInput physiologicalInput) { this.physiologicalInput = physiologicalInput; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}