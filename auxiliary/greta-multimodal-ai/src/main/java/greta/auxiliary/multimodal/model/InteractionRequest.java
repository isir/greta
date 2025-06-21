package greta.auxiliary.multimodal.model;

import java.time.Instant;
import java.util.Map;

/**
 * Request for multimodal interaction processing
 */
public class InteractionRequest {
    
    private String requestId;
    private String interactionType;
    private MultimodalInput input;
    private String intent;
    private Map<String, Object> context;
    private Instant timestamp;
    private Map<String, Object> parameters;
    
    public InteractionRequest() {
        this.timestamp = Instant.now();
    }
    
    // Getters and setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public String getInteractionType() { return interactionType; }
    public void setInteractionType(String interactionType) { this.interactionType = interactionType; }
    
    public MultimodalInput getInput() { return input; }
    public void setInput(MultimodalInput input) { this.input = input; }
    
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    
    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}