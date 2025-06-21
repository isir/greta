package greta.auxiliary.multimodal.model;

import java.time.Instant;
import java.util.Map;

/**
 * Contextual understanding of the current interaction
 */
public class ContextualUnderstanding {
    
    private String contextId;
    private SceneUnderstanding currentScene;
    private Map<String, Object> conversationHistory;
    private Map<String, Object> userState;
    private Map<String, Object> environmentState;
    private Instant lastUpdate;
    private Map<String, Object> metadata;
    
    public ContextualUnderstanding() {
        this.lastUpdate = Instant.now();
    }
    
    // Getters and setters
    public String getContextId() { return contextId; }
    public void setContextId(String contextId) { this.contextId = contextId; }
    
    public SceneUnderstanding getCurrentScene() { return currentScene; }
    public void setCurrentScene(SceneUnderstanding currentScene) { this.currentScene = currentScene; }
    
    public Map<String, Object> getConversationHistory() { return conversationHistory; }
    public void setConversationHistory(Map<String, Object> conversationHistory) { this.conversationHistory = conversationHistory; }
    
    public Map<String, Object> getUserState() { return userState; }
    public void setUserState(Map<String, Object> userState) { this.userState = userState; }
    
    public Map<String, Object> getEnvironmentState() { return environmentState; }
    public void setEnvironmentState(Map<String, Object> environmentState) { this.environmentState = environmentState; }
    
    public Instant getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(Instant lastUpdate) { this.lastUpdate = lastUpdate; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}