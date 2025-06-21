package greta.auxiliary.multimodal.model;

/**
 * Capabilities of the multimodal AI system
 */
public class MultimodalCapabilities {
    
    private boolean supportsVision;
    private boolean supportsSpeech;
    private boolean supportsGestures;
    private boolean supportsEmotions;
    private boolean supportsRealTime;
    private String[] supportedLanguages;
    private String[] supportedModels;
    
    public MultimodalCapabilities() {
        this.supportsVision = false; // Limited due to missing dependencies
        this.supportsSpeech = true;
        this.supportsGestures = false; // Limited due to missing MediaPipe
        this.supportsEmotions = true;
        this.supportsRealTime = false;
        this.supportedLanguages = new String[]{"en", "fr", "de", "es", "it"};
        this.supportedModels = new String[]{"gpt-4", "claude"};
    }
    
    // Getters and setters
    public boolean isSupportsVision() { return supportsVision; }
    public void setSupportsVision(boolean supportsVision) { this.supportsVision = supportsVision; }
    
    public boolean isSupportsSpeech() { return supportsSpeech; }
    public void setSupportsSpeech(boolean supportsSpeech) { this.supportsSpeech = supportsSpeech; }
    
    public boolean isSupportsGestures() { return supportsGestures; }
    public void setSupportsGestures(boolean supportsGestures) { this.supportsGestures = supportsGestures; }
    
    public boolean isSupportsEmotions() { return supportsEmotions; }
    public void setSupportsEmotions(boolean supportsEmotions) { this.supportsEmotions = supportsEmotions; }
    
    public boolean isSupportsRealTime() { return supportsRealTime; }
    public void setSupportsRealTime(boolean supportsRealTime) { this.supportsRealTime = supportsRealTime; }
    
    public String[] getSupportedLanguages() { return supportedLanguages; }
    public void setSupportedLanguages(String[] supportedLanguages) { this.supportedLanguages = supportedLanguages; }
    
    public String[] getSupportedModels() { return supportedModels; }
    public void setSupportedModels(String[] supportedModels) { this.supportedModels = supportedModels; }
}