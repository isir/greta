package greta.auxiliary.emotion.model;

import java.time.Instant;
import java.util.Map;

/**
 * Physiological input data for emotion analysis
 */
public class PhysiologicalInput {
    
    private double heartRate;
    private double heartRateVariability;
    private double galvanicSkinResponse;
    private double bodyTemperature;
    private double respirationRate;
    private Map<String, Double> additionalSensors;
    private Instant timestamp;
    private String sourceId;
    
    public PhysiologicalInput() {
        this.timestamp = Instant.now();
    }
    
    // Getters and setters
    public double getHeartRate() { return heartRate; }
    public void setHeartRate(double heartRate) { this.heartRate = heartRate; }
    
    public double getHeartRateVariability() { return heartRateVariability; }
    public void setHeartRateVariability(double heartRateVariability) { this.heartRateVariability = heartRateVariability; }
    
    public double getGalvanicSkinResponse() { return galvanicSkinResponse; }
    public void setGalvanicSkinResponse(double galvanicSkinResponse) { this.galvanicSkinResponse = galvanicSkinResponse; }
    
    public double getBodyTemperature() { return bodyTemperature; }
    public void setBodyTemperature(double bodyTemperature) { this.bodyTemperature = bodyTemperature; }
    
    public double getRespirationRate() { return respirationRate; }
    public void setRespirationRate(double respirationRate) { this.respirationRate = respirationRate; }
    
    public Map<String, Double> getAdditionalSensors() { return additionalSensors; }
    public void setAdditionalSensors(Map<String, Double> additionalSensors) { this.additionalSensors = additionalSensors; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
}