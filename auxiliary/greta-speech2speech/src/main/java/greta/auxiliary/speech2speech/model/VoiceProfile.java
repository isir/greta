package greta.auxiliary.speech2speech.model;

import java.time.Instant;
import java.util.Map;

/**
 * Voice profile for speech synthesis and conversion
 */
public class VoiceProfile {
    
    private String voiceId;
    private String name;
    private String description;
    private VoiceCharacteristics characteristics;
    private String language;
    private String accent;
    private String gender;
    private int age;
    private String emotion;
    private double stability;
    private double clarity;
    private Map<String, Object> customParameters;
    private Instant createdAt;
    private String provider; // "bark", "elevenlabs", "custom"
    private String speakerName;
    private String barkSpeakerCode; // Bark-specific speaker code
    
    public VoiceProfile() {
        this.createdAt = Instant.now();
    }
    
    public VoiceProfile(String voiceId, String name) {
        this();
        this.voiceId = voiceId;
        this.name = name;
    }
    
    /**
     * Create a copy of this voice profile
     */
    public VoiceProfile copy() {
        VoiceProfile copy = new VoiceProfile();
        copy.voiceId = this.voiceId;
        copy.name = this.name;
        copy.description = this.description;
        copy.characteristics = this.characteristics != null ? this.characteristics.copy() : null;
        copy.language = this.language;
        copy.accent = this.accent;
        copy.gender = this.gender;
        copy.age = this.age;
        copy.emotion = this.emotion;
        copy.stability = this.stability;
        copy.clarity = this.clarity;
        copy.customParameters = this.customParameters;
        copy.createdAt = this.createdAt;
        copy.provider = this.provider;
        return copy;
    }
    
    /**
     * Check if this voice profile is compatible with another
     */
    public boolean isCompatibleWith(VoiceProfile other) {
        if (other == null) return false;
        
        // Check language compatibility
        if (this.language != null && other.language != null) {
            if (!this.language.equals(other.language)) {
                return false;
            }
        }
        
        // Check provider compatibility
        if (this.provider != null && other.provider != null) {
            if (!this.provider.equals(other.provider)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Calculate similarity score with another voice profile
     */
    public double calculateSimilarity(VoiceProfile other) {
        if (other == null || !isCompatibleWith(other)) {
            return 0.0;
        }
        
        double similarity = 0.0;
        int factors = 0;
        
        // Age similarity
        if (this.age > 0 && other.age > 0) {
            double ageDiff = Math.abs(this.age - other.age);
            similarity += Math.max(0, 1.0 - (ageDiff / 50.0)); // Normalize by 50 years
            factors++;
        }
        
        // Gender similarity
        if (this.gender != null && other.gender != null) {
            similarity += this.gender.equals(other.gender) ? 1.0 : 0.0;
            factors++;
        }
        
        // Characteristics similarity
        if (this.characteristics != null && other.characteristics != null) {
            similarity += this.characteristics.calculateSimilarity(other.characteristics);
            factors++;
        }
        
        return factors > 0 ? similarity / factors : 0.0;
    }
    
    // Getters and setters
    public String getVoiceId() { return voiceId; }
    public void setVoiceId(String voiceId) { this.voiceId = voiceId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public VoiceCharacteristics getCharacteristics() { return characteristics; }
    public void setCharacteristics(VoiceCharacteristics characteristics) { this.characteristics = characteristics; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getAccent() { return accent; }
    public void setAccent(String accent) { this.accent = accent; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }
    
    public double getStability() { return stability; }
    public void setStability(double stability) { this.stability = stability; }
    
    public double getClarity() { return clarity; }
    public void setClarity(double clarity) { this.clarity = clarity; }
    
    public Map<String, Object> getCustomParameters() { return customParameters; }
    public void setCustomParameters(Map<String, Object> customParameters) { this.customParameters = customParameters; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getSpeakerName() { return speakerName; }
    public void setSpeakerName(String speakerName) { this.speakerName = speakerName; }
    
    public String getBarkSpeakerCode() { return barkSpeakerCode; }
    public void setBarkSpeakerCode(String barkSpeakerCode) { this.barkSpeakerCode = barkSpeakerCode; }
}