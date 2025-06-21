package greta.auxiliary.llm.model;

import java.util.List;
import java.util.Map;

/**
 * Request object for LLM interaction
 */
public class LLMRequest {
    
    private String prompt;
    private ConversationContext context;
    private LLMParameters parameters;
    private Map<String, Object> metadata;
    
    public LLMRequest() {}
    
    public LLMRequest(String prompt, ConversationContext context) {
        this.prompt = prompt;
        this.context = context;
        this.parameters = new LLMParameters();
    }
    
    // Getters and setters
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    
    public ConversationContext getContext() { return context; }
    public void setContext(ConversationContext context) { this.context = context; }
    
    public LLMParameters getParameters() { return parameters; }
    public void setParameters(LLMParameters parameters) { this.parameters = parameters; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    /**
     * LLM generation parameters
     */
    public static class LLMParameters {
        private double temperature = 0.7;
        private int maxTokens = 150;
        private double topP = 0.9;
        private double frequencyPenalty = 0.0;
        private double presencePenalty = 0.0;
        private List<String> stopSequences;
        private String model = "gpt-4";
        private boolean stream = false;
        
        // Getters and setters
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
        
        public double getTopP() { return topP; }
        public void setTopP(double topP) { this.topP = topP; }
        
        public double getFrequencyPenalty() { return frequencyPenalty; }
        public void setFrequencyPenalty(double frequencyPenalty) { this.frequencyPenalty = frequencyPenalty; }
        
        public double getPresencePenalty() { return presencePenalty; }
        public void setPresencePenalty(double presencePenalty) { this.presencePenalty = presencePenalty; }
        
        public List<String> getStopSequences() { return stopSequences; }
        public void setStopSequences(List<String> stopSequences) { this.stopSequences = stopSequences; }
        
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        
        public boolean isStream() { return stream; }
        public void setStream(boolean stream) { this.stream = stream; }
    }
}