package greta.auxiliary.multimodal.model;

import java.time.Instant;
import java.util.Map;

/**
 * Response from multimodal interaction processing
 */
public class InteractionResponse {
    
    private String responseId;
    private String requestId;
    private String responseType;
    private String textResponse;
    private byte[] audioResponse;
    private Map<String, Object> gestureData;
    private Map<String, Object> emotionalState;
    private Instant timestamp;
    private boolean success;
    private String error;
    private Map<String, Object> metadata;
    
    public InteractionResponse() {
        this.timestamp = Instant.now();
        this.success = true;
    }
    
    // Getters and setters
    public String getResponseId() { return responseId; }
    public void setResponseId(String responseId) { this.responseId = responseId; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public String getResponseType() { return responseType; }
    public void setResponseType(String responseType) { this.responseType = responseType; }
    
    public String getTextResponse() { return textResponse; }
    public void setTextResponse(String textResponse) { this.textResponse = textResponse; }
    
    public byte[] getAudioResponse() { return audioResponse; }
    public void setAudioResponse(byte[] audioResponse) { this.audioResponse = audioResponse; }
    
    public Map<String, Object> getGestureData() { return gestureData; }
    public void setGestureData(Map<String, Object> gestureData) { this.gestureData = gestureData; }
    
    public Map<String, Object> getEmotionalState() { return emotionalState; }
    public void setEmotionalState(Map<String, Object> emotionalState) { this.emotionalState = emotionalState; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}