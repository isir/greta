package greta.auxiliary.llm.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Context for maintaining conversation state and history
 */
public class ConversationContext {
    
    private String conversationId;
    private List<ConversationTurn> history;
    private Map<String, Object> userProfile;
    private String currentEmotion;
    private String currentTopic;
    private Instant lastInteraction;
    private Map<String, Object> sessionMetadata;
    
    public ConversationContext() {
        this.history = new ArrayList<>();
        this.lastInteraction = Instant.now();
    }
    
    public ConversationContext(String conversationId) {
        this();
        this.conversationId = conversationId;
    }
    
    /**
     * Add a turn to the conversation history
     */
    public void addTurn(String role, String content) {
        history.add(new ConversationTurn(role, content));
        this.lastInteraction = Instant.now();
    }
    
    /**
     * Get recent conversation history for context
     */
    public List<ConversationTurn> getRecentHistory(int maxTurns) {
        int start = Math.max(0, history.size() - maxTurns);
        return history.subList(start, history.size());
    }
    
    /**
     * Clear conversation history
     */
    public void clearHistory() {
        history.clear();
    }
    
    // Getters and setters
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    
    public List<ConversationTurn> getHistory() { return history; }
    public void setHistory(List<ConversationTurn> history) { this.history = history; }
    
    public Map<String, Object> getUserProfile() { return userProfile; }
    public void setUserProfile(Map<String, Object> userProfile) { this.userProfile = userProfile; }
    
    public String getCurrentEmotion() { return currentEmotion; }
    public void setCurrentEmotion(String currentEmotion) { this.currentEmotion = currentEmotion; }
    
    public String getCurrentTopic() { return currentTopic; }
    public void setCurrentTopic(String currentTopic) { this.currentTopic = currentTopic; }
    
    public Instant getLastInteraction() { return lastInteraction; }
    public void setLastInteraction(Instant lastInteraction) { this.lastInteraction = lastInteraction; }
    
    public Map<String, Object> getSessionMetadata() { return sessionMetadata; }
    public void setSessionMetadata(Map<String, Object> sessionMetadata) { this.sessionMetadata = sessionMetadata; }
    
    /**
     * Individual turn in a conversation
     */
    public static class ConversationTurn {
        private String role; // "user", "assistant", "system"
        private String content;
        private Instant timestamp;
        private Map<String, Object> metadata;
        
        public ConversationTurn() {
            this.timestamp = Instant.now();
        }
        
        public ConversationTurn(String role, String content) {
            this();
            this.role = role;
            this.content = content;
        }
        
        // Getters and setters
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}