package greta.auxiliary.speech2speech.model;

import java.time.Instant;
import java.util.Map;

/**
 * Request for speech-to-speech conversion
 */
public class Speech2SpeechRequest {
    
    private AudioSegment inputAudio;
    private String text; // Optional text override or prompt
    private VoiceProfile targetVoice;
    private Speech2SpeechParameters parameters;
    private Map<String, Object> metadata;
    private Instant timestamp;
    private String requestId;
    
    public Speech2SpeechRequest() {
        this.timestamp = Instant.now();
    }
    
    public Speech2SpeechRequest(AudioSegment inputAudio, VoiceProfile targetVoice) {
        this();
        this.inputAudio = inputAudio;
        this.targetVoice = targetVoice;
    }
    
    /**
     * Parameters for speech-to-speech conversion
     */
    public static class Speech2SpeechParameters {
        private double temperature = 0.7;
        private double voiceStability = 0.5;
        private double clarity = 0.75;
        private boolean preserveEmotion = true;
        private boolean preserveProsody = true;
        private String language = "en";
        private double speedFactor = 1.0;
        private double pitchShift = 0.0;
        
        // Getters and setters
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        
        public double getVoiceStability() { return voiceStability; }
        public void setVoiceStability(double voiceStability) { this.voiceStability = voiceStability; }
        
        public double getClarity() { return clarity; }
        public void setClarity(double clarity) { this.clarity = clarity; }
        
        public boolean isPreserveEmotion() { return preserveEmotion; }
        public void setPreserveEmotion(boolean preserveEmotion) { this.preserveEmotion = preserveEmotion; }
        
        public boolean isPreserveProsody() { return preserveProsody; }
        public void setPreserveProsody(boolean preserveProsody) { this.preserveProsody = preserveProsody; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public double getSpeedFactor() { return speedFactor; }
        public void setSpeedFactor(double speedFactor) { this.speedFactor = speedFactor; }
        
        public double getPitchShift() { return pitchShift; }
        public void setPitchShift(double pitchShift) { this.pitchShift = pitchShift; }
    }
    
    // Getters and setters
    public AudioSegment getInputAudio() { return inputAudio; }
    public void setInputAudio(AudioSegment inputAudio) { this.inputAudio = inputAudio; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public VoiceProfile getTargetVoice() { return targetVoice; }
    public void setTargetVoice(VoiceProfile targetVoice) { this.targetVoice = targetVoice; }
    
    public Speech2SpeechParameters getParameters() { return parameters; }
    public void setParameters(Speech2SpeechParameters parameters) { this.parameters = parameters; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}