package greta.auxiliary.speech2speech.model;

/**
 * Voice characteristics for detailed voice profiling
 */
public class VoiceCharacteristics {
    
    private double pitch; // Fundamental frequency in Hz
    private double pitchRange; // Range of pitch variation
    private double timbre; // Voice quality/color
    private double resonance; // Vocal tract resonance
    private double breathiness; // Amount of breath in voice
    private double roughness; // Voice roughness/hoarseness
    private double warmth; // Perceived warmth of voice
    private double energy; // Overall energy level
    private double articulation; // Clarity of articulation
    private double nasality; // Nasal quality
    
    // Prosodic characteristics
    private double rhythm; // Speaking rhythm regularity
    private double tempo; // Speaking speed
    private double stress; // Stress pattern strength
    private double intonation; // Intonation pattern variety
    
    // Emotional characteristics
    private double expressiveness; // Emotional expressiveness
    private double intensity; // Emotional intensity
    private double confidence; // Confidence level in voice
    
    // Additional characteristics for categorization
    private String gender; // "male", "female", "neutral"
    private String ageGroup; // "child", "young_adult", "adult", "elderly"
    
    public VoiceCharacteristics() {
        // Default neutral characteristics
        this.pitch = 150.0; // Hz
        this.pitchRange = 50.0;
        this.timbre = 0.5;
        this.resonance = 0.5;
        this.breathiness = 0.3;
        this.roughness = 0.2;
        this.warmth = 0.5;
        this.energy = 0.5;
        this.articulation = 0.7;
        this.nasality = 0.3;
        this.rhythm = 0.5;
        this.tempo = 0.5;
        this.stress = 0.5;
        this.intonation = 0.5;
        this.expressiveness = 0.5;
        this.intensity = 0.5;
        this.confidence = 0.7;
        this.gender = "neutral";
        this.ageGroup = "adult";
    }
    
    /**
     * Create a copy of these voice characteristics
     */
    public VoiceCharacteristics copy() {
        VoiceCharacteristics copy = new VoiceCharacteristics();
        copy.pitch = this.pitch;
        copy.pitchRange = this.pitchRange;
        copy.timbre = this.timbre;
        copy.resonance = this.resonance;
        copy.breathiness = this.breathiness;
        copy.roughness = this.roughness;
        copy.warmth = this.warmth;
        copy.energy = this.energy;
        copy.articulation = this.articulation;
        copy.nasality = this.nasality;
        copy.rhythm = this.rhythm;
        copy.tempo = this.tempo;
        copy.stress = this.stress;
        copy.intonation = this.intonation;
        copy.expressiveness = this.expressiveness;
        copy.intensity = this.intensity;
        copy.confidence = this.confidence;
        copy.gender = this.gender;
        copy.ageGroup = this.ageGroup;
        return copy;
    }
    
    /**
     * Calculate similarity with another voice characteristics profile
     */
    public double calculateSimilarity(VoiceCharacteristics other) {
        if (other == null) return 0.0;
        
        double similarity = 0.0;
        
        // Pitch similarity (weighted heavily)
        double pitchDiff = Math.abs(this.pitch - other.pitch);
        similarity += 0.2 * Math.max(0, 1.0 - (pitchDiff / 200.0)); // Normalize by 200 Hz
        
        // Timbre similarity
        similarity += 0.15 * (1.0 - Math.abs(this.timbre - other.timbre));
        
        // Warmth similarity
        similarity += 0.1 * (1.0 - Math.abs(this.warmth - other.warmth));
        
        // Energy similarity
        similarity += 0.1 * (1.0 - Math.abs(this.energy - other.energy));
        
        // Articulation similarity
        similarity += 0.1 * (1.0 - Math.abs(this.articulation - other.articulation));
        
        // Tempo similarity
        similarity += 0.1 * (1.0 - Math.abs(this.tempo - other.tempo));
        
        // Expressiveness similarity
        similarity += 0.1 * (1.0 - Math.abs(this.expressiveness - other.expressiveness));
        
        // Confidence similarity
        similarity += 0.05 * (1.0 - Math.abs(this.confidence - other.confidence));
        
        // Other characteristics (combined)
        double otherSim = 0.0;
        otherSim += 1.0 - Math.abs(this.breathiness - other.breathiness);
        otherSim += 1.0 - Math.abs(this.roughness - other.roughness);
        otherSim += 1.0 - Math.abs(this.resonance - other.resonance);
        otherSim += 1.0 - Math.abs(this.rhythm - other.rhythm);
        otherSim += 1.0 - Math.abs(this.stress - other.stress);
        otherSim += 1.0 - Math.abs(this.intonation - other.intonation);
        otherSim += 1.0 - Math.abs(this.intensity - other.intensity);
        otherSim += 1.0 - Math.abs(this.nasality - other.nasality);
        
        similarity += 0.1 * (otherSim / 8.0);
        
        return Math.min(1.0, Math.max(0.0, similarity));
    }
    
    // Getters and setters
    public double getPitch() { return pitch; }
    public void setPitch(double pitch) { this.pitch = pitch; }
    
    public double getPitchRange() { return pitchRange; }
    public void setPitchRange(double pitchRange) { this.pitchRange = pitchRange; }
    
    public double getTimbre() { return timbre; }
    public void setTimbre(double timbre) { this.timbre = timbre; }
    
    public double getResonance() { return resonance; }
    public void setResonance(double resonance) { this.resonance = resonance; }
    
    public double getBreathiness() { return breathiness; }
    public void setBreathiness(double breathiness) { this.breathiness = breathiness; }
    
    public double getRoughness() { return roughness; }
    public void setRoughness(double roughness) { this.roughness = roughness; }
    
    public double getWarmth() { return warmth; }
    public void setWarmth(double warmth) { this.warmth = warmth; }
    
    public double getEnergy() { return energy; }
    public void setEnergy(double energy) { this.energy = energy; }
    
    public double getArticulation() { return articulation; }
    public void setArticulation(double articulation) { this.articulation = articulation; }
    
    public double getNasality() { return nasality; }
    public void setNasality(double nasality) { this.nasality = nasality; }
    
    public double getRhythm() { return rhythm; }
    public void setRhythm(double rhythm) { this.rhythm = rhythm; }
    
    public double getTempo() { return tempo; }
    public void setTempo(double tempo) { this.tempo = tempo; }
    
    public double getStress() { return stress; }
    public void setStress(double stress) { this.stress = stress; }
    
    public double getIntonation() { return intonation; }
    public void setIntonation(double intonation) { this.intonation = intonation; }
    
    public double getExpressiveness() { return expressiveness; }
    public void setExpressiveness(double expressiveness) { this.expressiveness = expressiveness; }
    
    public double getIntensity() { return intensity; }
    public void setIntensity(double intensity) { this.intensity = intensity; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
}