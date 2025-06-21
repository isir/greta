package greta.auxiliary.emotion.model;

import java.time.Instant;

/**
 * Audio input data for voice emotion analysis
 */
public class AudioInput {
    
    private byte[] audioData;
    private int sampleRate;
    private int channels;
    private int bitsPerSample;
    private AudioFormat format;
    private Instant timestamp;
    private long durationMs;
    private String sourceId;
    
    public AudioInput() {
        this.timestamp = Instant.now();
    }
    
    public AudioInput(byte[] audioData, int sampleRate, int channels) {
        this();
        this.audioData = audioData;
        this.sampleRate = sampleRate;
        this.channels = channels;
    }
    
    public enum AudioFormat {
        PCM_16BIT,
        PCM_24BIT,
        PCM_32BIT,
        MP3,
        WAV,
        FLAC,
        AAC
    }
    
    // Getters and setters
    public byte[] getAudioData() { return audioData; }
    public void setAudioData(byte[] audioData) { this.audioData = audioData; }
    
    public int getSampleRate() { return sampleRate; }
    public void setSampleRate(int sampleRate) { this.sampleRate = sampleRate; }
    
    public int getChannels() { return channels; }
    public void setChannels(int channels) { this.channels = channels; }
    
    public int getBitsPerSample() { return bitsPerSample; }
    public void setBitsPerSample(int bitsPerSample) { this.bitsPerSample = bitsPerSample; }
    
    public AudioFormat getFormat() { return format; }
    public void setFormat(AudioFormat format) { this.format = format; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
}