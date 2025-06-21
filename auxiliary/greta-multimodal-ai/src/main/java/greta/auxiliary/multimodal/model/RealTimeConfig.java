package greta.auxiliary.multimodal.model;

import java.util.Map;

/**
 * Configuration for real-time interaction
 */
public class RealTimeConfig {
    
    private boolean enableVideo;
    private boolean enableAudio;
    private boolean enableGestures;
    private int frameRate;
    private int audioSampleRate;
    private float latencyThreshold;
    private Map<String, Object> processingOptions;
    
    public RealTimeConfig() {
        this.enableVideo = true;
        this.enableAudio = true;
        this.enableGestures = true;
        this.frameRate = 30;
        this.audioSampleRate = 44100;
        this.latencyThreshold = 100.0f; // milliseconds
    }
    
    // Getters and setters
    public boolean isEnableVideo() { return enableVideo; }
    public void setEnableVideo(boolean enableVideo) { this.enableVideo = enableVideo; }
    
    public boolean isEnableAudio() { return enableAudio; }
    public void setEnableAudio(boolean enableAudio) { this.enableAudio = enableAudio; }
    
    public boolean isEnableGestures() { return enableGestures; }
    public void setEnableGestures(boolean enableGestures) { this.enableGestures = enableGestures; }
    
    public int getFrameRate() { return frameRate; }
    public void setFrameRate(int frameRate) { this.frameRate = frameRate; }
    
    public int getAudioSampleRate() { return audioSampleRate; }
    public void setAudioSampleRate(int audioSampleRate) { this.audioSampleRate = audioSampleRate; }
    
    public float getLatencyThreshold() { return latencyThreshold; }
    public void setLatencyThreshold(float latencyThreshold) { this.latencyThreshold = latencyThreshold; }
    
    public Map<String, Object> getProcessingOptions() { return processingOptions; }
    public void setProcessingOptions(Map<String, Object> processingOptions) { this.processingOptions = processingOptions; }
}