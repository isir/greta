package greta.auxiliary.speech2speech.model;

import java.time.Duration;
import java.util.Map;

/**
 * Represents an audio segment with metadata
 */
public class AudioSegment {
    
    private byte[] audioData;
    private int sampleRate;
    private int channels;
    private int bitsPerSample;
    private Duration duration;
    private AudioFormat format;
    private Map<String, Object> metadata;
    
    // Derived properties
    private double[] samples; // Normalized audio samples [-1.0, 1.0]
    private SpectralFeatures spectralFeatures;
    
    public AudioSegment() {}
    
    public AudioSegment(byte[] audioData, int sampleRate) {
        this.audioData = audioData;
        this.sampleRate = sampleRate;
        this.channels = 1; // Default to mono
        this.bitsPerSample = 16; // Default to 16-bit
        this.format = AudioFormat.WAV;
        calculateDuration();
    }
    
    public AudioSegment(byte[] audioData, int sampleRate, int channels, int bitsPerSample) {
        this.audioData = audioData;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.bitsPerSample = bitsPerSample;
        this.format = AudioFormat.WAV;
        calculateDuration();
    }
    
    /**
     * Calculate duration from audio data
     */
    private void calculateDuration() {
        if (audioData != null && sampleRate > 0) {
            int bytesPerSample = (bitsPerSample / 8) * channels;
            long totalSamples = audioData.length / bytesPerSample;
            double durationSeconds = (double) totalSamples / sampleRate;
            this.duration = Duration.ofMillis((long) (durationSeconds * 1000));
        }
    }
    
    /**
     * Convert raw audio bytes to normalized samples
     */
    public double[] getNormalizedSamples() {
        if (samples == null) {
            samples = convertToNormalizedSamples();
        }
        return samples;
    }
    
    private double[] convertToNormalizedSamples() {
        if (audioData == null || bitsPerSample != 16) {
            return new double[0];
        }
        
        int bytesPerSample = 2; // 16-bit = 2 bytes
        int totalSamples = audioData.length / (bytesPerSample * channels);
        double[] normalized = new double[totalSamples];
        
        for (int i = 0; i < totalSamples; i++) {
            int byteIndex = i * bytesPerSample * channels;
            
            // Read 16-bit sample (little-endian)
            int sample = (audioData[byteIndex + 1] << 8) | (audioData[byteIndex] & 0xFF);
            
            // Convert to signed
            if (sample > 32767) {
                sample -= 65536;
            }
            
            // Normalize to [-1.0, 1.0]
            normalized[i] = sample / 32768.0;
        }
        
        return normalized;
    }
    
    /**
     * Extract spectral features for analysis
     */
    public SpectralFeatures getSpectralFeatures() {
        if (spectralFeatures == null) {
            spectralFeatures = extractSpectralFeatures();
        }
        return spectralFeatures;
    }
    
    private SpectralFeatures extractSpectralFeatures() {
        double[] samples = getNormalizedSamples();
        if (samples.length == 0) {
            return new SpectralFeatures();
        }
        
        // Simple spectral feature extraction
        SpectralFeatures features = new SpectralFeatures();
        
        // Calculate energy
        double energy = 0.0;
        for (double sample : samples) {
            energy += sample * sample;
        }
        features.setEnergy(energy / samples.length);
        
        // Calculate zero crossing rate
        int zeroCrossings = 0;
        for (int i = 1; i < samples.length; i++) {
            if ((samples[i] >= 0) != (samples[i-1] >= 0)) {
                zeroCrossings++;
            }
        }
        features.setZeroCrossingRate((double) zeroCrossings / samples.length);
        
        // Estimate fundamental frequency (simple autocorrelation)
        features.setFundamentalFrequency(estimateF0(samples));
        
        return features;
    }
    
    private double estimateF0(double[] samples) {
        // Simplified F0 estimation using autocorrelation
        int minPeriod = sampleRate / 800; // 800 Hz max
        int maxPeriod = sampleRate / 80;  // 80 Hz min
        
        double maxCorrelation = 0.0;
        int bestPeriod = minPeriod;
        
        for (int period = minPeriod; period <= maxPeriod && period < samples.length / 2; period++) {
            double correlation = 0.0;
            int count = 0;
            
            for (int i = 0; i < samples.length - period; i++) {
                correlation += samples[i] * samples[i + period];
                count++;
            }
            
            correlation /= count;
            
            if (correlation > maxCorrelation) {
                maxCorrelation = correlation;
                bestPeriod = period;
            }
        }
        
        return (double) sampleRate / bestPeriod;
    }
    
    /**
     * Create a copy of this audio segment
     */
    public AudioSegment copy() {
        AudioSegment copy = new AudioSegment();
        copy.audioData = this.audioData != null ? this.audioData.clone() : null;
        copy.sampleRate = this.sampleRate;
        copy.channels = this.channels;
        copy.bitsPerSample = this.bitsPerSample;
        copy.duration = this.duration;
        copy.format = this.format;
        copy.metadata = this.metadata;
        return copy;
    }
    
    // Getters and setters
    public byte[] getAudioData() { return audioData; }
    public void setAudioData(byte[] audioData) { 
        this.audioData = audioData;
        this.samples = null; // Reset cached samples
        calculateDuration();
    }
    
    public int getSampleRate() { return sampleRate; }
    public void setSampleRate(int sampleRate) { this.sampleRate = sampleRate; }
    
    public int getChannels() { return channels; }
    public void setChannels(int channels) { this.channels = channels; }
    
    public int getBitsPerSample() { return bitsPerSample; }
    public void setBitsPerSample(int bitsPerSample) { this.bitsPerSample = bitsPerSample; }
    
    public Duration getDuration() { return duration; }
    public void setDuration(Duration duration) { this.duration = duration; }
    
    public AudioFormat getFormat() { return format; }
    public void setFormat(AudioFormat format) { this.format = format; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    /**
     * Audio format enumeration
     */
    public enum AudioFormat {
        WAV, MP3, FLAC, OGG, M4A, AAC
    }
    
    /**
     * Spectral features for audio analysis
     */
    public static class SpectralFeatures {
        private double energy;
        private double zeroCrossingRate;
        private double fundamentalFrequency;
        private double[] mfcc; // Mel-frequency cepstral coefficients
        private double spectralCentroid;
        private double spectralRolloff;
        
        public SpectralFeatures() {}
        
        // Getters and setters
        public double getEnergy() { return energy; }
        public void setEnergy(double energy) { this.energy = energy; }
        
        public double getZeroCrossingRate() { return zeroCrossingRate; }
        public void setZeroCrossingRate(double zeroCrossingRate) { this.zeroCrossingRate = zeroCrossingRate; }
        
        public double getFundamentalFrequency() { return fundamentalFrequency; }
        public void setFundamentalFrequency(double fundamentalFrequency) { this.fundamentalFrequency = fundamentalFrequency; }
        
        public double[] getMfcc() { return mfcc; }
        public void setMfcc(double[] mfcc) { this.mfcc = mfcc; }
        
        public double getSpectralCentroid() { return spectralCentroid; }
        public void setSpectralCentroid(double spectralCentroid) { this.spectralCentroid = spectralCentroid; }
        
        public double getSpectralRolloff() { return spectralRolloff; }
        public void setSpectralRolloff(double spectralRolloff) { this.spectralRolloff = spectralRolloff; }
    }
}