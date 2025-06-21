package greta.auxiliary.multimodal.model;

import java.time.Instant;
import java.util.Map;

/**
 * Input data for multimodal processing
 */
public class MultimodalInput {
    
    private String inputId;
    private byte[] visualData;
    private byte[] audioData;
    private String textData;
    private Map<String, Object> sensorData;
    private Instant timestamp;
    private Map<String, Object> metadata;
    
    public MultimodalInput() {
        this.timestamp = Instant.now();
    }
    
    // Getters and setters
    public String getInputId() { return inputId; }
    public void setInputId(String inputId) { this.inputId = inputId; }
    
    public byte[] getVisualData() { return visualData; }
    public void setVisualData(byte[] visualData) { this.visualData = visualData; }
    
    public byte[] getAudioData() { return audioData; }
    public void setAudioData(byte[] audioData) { this.audioData = audioData; }
    
    public String getTextData() { return textData; }
    public void setTextData(String textData) { this.textData = textData; }
    
    public Map<String, Object> getSensorData() { return sensorData; }
    public void setSensorData(Map<String, Object> sensorData) { this.sensorData = sensorData; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}