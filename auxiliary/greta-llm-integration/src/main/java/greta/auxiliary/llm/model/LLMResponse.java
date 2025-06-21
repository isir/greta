package greta.auxiliary.llm.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response object from LLM interaction
 */
public class LLMResponse {
    
    private String text;
    private String requestId;
    private String model;
    private UsageInfo usage;
    private List<String> alternatives;
    private double confidence;
    private Map<String, Object> metadata;
    private Instant timestamp;
    private long responseTimeMs;
    
    public LLMResponse() {
        this.timestamp = Instant.now();
    }
    
    public LLMResponse(String text) {
        this();
        this.text = text;
    }
    
    // Getters and setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public UsageInfo getUsage() { return usage; }
    public void setUsage(UsageInfo usage) { this.usage = usage; }
    
    public List<String> getAlternatives() { return alternatives; }
    public void setAlternatives(List<String> alternatives) { this.alternatives = alternatives; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    
    /**
     * Token usage information
     */
    public static class UsageInfo {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
        private double cost;
        
        public UsageInfo() {}
        
        public UsageInfo(int promptTokens, int completionTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = promptTokens + completionTokens;
        }
        
        // Getters and setters
        public int getPromptTokens() { return promptTokens; }
        public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
        
        public int getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }
        
        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
        
        public double getCost() { return cost; }
        public void setCost(double cost) { this.cost = cost; }
    }
}