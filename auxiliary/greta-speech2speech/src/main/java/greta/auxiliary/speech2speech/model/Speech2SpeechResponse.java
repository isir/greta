package greta.auxiliary.speech2speech.model;

import java.time.Instant;
import java.util.Map;

/**
 * Response from speech-to-speech conversion
 */
public class Speech2SpeechResponse {
    
    private AudioSegment outputAudio;
    private String convertedText; // Transcribed or generated text
    private VoiceProfile actualVoice; // Voice profile that was actually used
    private ConversionMetrics metrics;
    private Map<String, Object> metadata;
    private Instant timestamp;
    private String requestId;
    private boolean success;
    private String errorMessage;
    private long processingTimeMs;
    private String model; // Model used for conversion
    private double qualityScore; // Quality score of conversion
    private String[] emotionalTags; // Emotional tags for the conversion
    
    public Speech2SpeechResponse() {
        this.timestamp = Instant.now();
        this.success = true;
    }
    
    public Speech2SpeechResponse(AudioSegment outputAudio) {
        this();
        this.outputAudio = outputAudio;
    }
    
    /**
     * Metrics about the conversion process
     */
    public static class ConversionMetrics {
        private double voiceSimilarity;
        private double emotionPreservation;
        private double prosodyPreservation;
        private double audioQuality;
        private double latency;
        private int inputSampleRate;
        private int outputSampleRate;
        private double compressionRatio;
        
        // Getters and setters
        public double getVoiceSimilarity() { return voiceSimilarity; }
        public void setVoiceSimilarity(double voiceSimilarity) { this.voiceSimilarity = voiceSimilarity; }
        
        public double getEmotionPreservation() { return emotionPreservation; }
        public void setEmotionPreservation(double emotionPreservation) { this.emotionPreservation = emotionPreservation; }
        
        public double getProsodyPreservation() { return prosodyPreservation; }
        public void setProsodyPreservation(double prosodyPreservation) { this.prosodyPreservation = prosodyPreservation; }
        
        public double getAudioQuality() { return audioQuality; }
        public void setAudioQuality(double audioQuality) { this.audioQuality = audioQuality; }
        
        public double getLatency() { return latency; }
        public void setLatency(double latency) { this.latency = latency; }
        
        public int getInputSampleRate() { return inputSampleRate; }
        public void setInputSampleRate(int inputSampleRate) { this.inputSampleRate = inputSampleRate; }
        
        public int getOutputSampleRate() { return outputSampleRate; }
        public void setOutputSampleRate(int outputSampleRate) { this.outputSampleRate = outputSampleRate; }
        
        public double getCompressionRatio() { return compressionRatio; }
        public void setCompressionRatio(double compressionRatio) { this.compressionRatio = compressionRatio; }
    }
    
    // Getters and setters
    public AudioSegment getOutputAudio() { return outputAudio; }
    public void setOutputAudio(AudioSegment outputAudio) { this.outputAudio = outputAudio; }
    
    public String getConvertedText() { return convertedText; }
    public void setConvertedText(String convertedText) { this.convertedText = convertedText; }
    
    public VoiceProfile getActualVoice() { return actualVoice; }
    public void setActualVoice(VoiceProfile actualVoice) { this.actualVoice = actualVoice; }
    
    public ConversionMetrics getMetrics() { return metrics; }
    public void setMetrics(ConversionMetrics metrics) { this.metrics = metrics; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public double getQualityScore() { return qualityScore; }
    public void setQualityScore(double qualityScore) { this.qualityScore = qualityScore; }
    
    public String[] getEmotionalTags() { return emotionalTags; }
    public void setEmotionalTags(String[] emotionalTags) { this.emotionalTags = emotionalTags; }
}