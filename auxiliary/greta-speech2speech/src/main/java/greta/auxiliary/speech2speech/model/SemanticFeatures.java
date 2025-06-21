package greta.auxiliary.speech2speech.model;

import java.util.List;
import java.util.Map;

/**
 * Semantic features extracted from speech for enhanced conversion
 */
public class SemanticFeatures {
    
    private String transcribedText;
    private String detectedLanguage;
    private double confidence;
    private List<String> keywords;
    private String sentiment; // positive, negative, neutral
    private double sentimentScore;
    private String emotion; // happy, sad, angry, neutral, etc.
    private double emotionConfidence;
    private String intent; // question, statement, command, etc.
    private double urgency;
    private double formality;
    private Map<String, Double> topicProbabilities;
    private List<SemanticEntity> entities;
    
    // Prosodic semantic features
    private double emphasis;
    private double pausePattern;
    private double breathPattern;
    private List<Double> stressTiming;
    private double pitch; // Average pitch
    private String emotionalContext; // Emotional context description
    private double energy; // Energy level
    private String phoneticContent; // Phonetic representation
    
    public SemanticFeatures() {}
    
    public SemanticFeatures(String transcribedText, String detectedLanguage) {
        this.transcribedText = transcribedText;
        this.detectedLanguage = detectedLanguage;
    }
    
    /**
     * Semantic entity (named entity recognition)
     */
    public static class SemanticEntity {
        private String text;
        private String type; // PERSON, LOCATION, ORGANIZATION, etc.
        private double confidence;
        private int startIndex;
        private int endIndex;
        
        public SemanticEntity() {}
        
        public SemanticEntity(String text, String type, double confidence) {
            this.text = text;
            this.type = type;
            this.confidence = confidence;
        }
        
        // Getters and setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public int getStartIndex() { return startIndex; }
        public void setStartIndex(int startIndex) { this.startIndex = startIndex; }
        
        public int getEndIndex() { return endIndex; }
        public void setEndIndex(int endIndex) { this.endIndex = endIndex; }
    }
    
    /**
     * Calculate semantic similarity with another features object
     */
    public double calculateSimilarity(SemanticFeatures other) {
        if (other == null) return 0.0;
        
        double similarity = 0.0;
        int factors = 0;
        
        // Language similarity
        if (this.detectedLanguage != null && other.detectedLanguage != null) {
            similarity += this.detectedLanguage.equals(other.detectedLanguage) ? 1.0 : 0.0;
            factors++;
        }
        
        // Sentiment similarity
        if (this.sentiment != null && other.sentiment != null) {
            similarity += this.sentiment.equals(other.sentiment) ? 1.0 : 0.0;
            factors++;
        }
        
        // Emotion similarity
        if (this.emotion != null && other.emotion != null) {
            similarity += this.emotion.equals(other.emotion) ? 1.0 : 0.0;
            factors++;
        }
        
        // Intent similarity
        if (this.intent != null && other.intent != null) {
            similarity += this.intent.equals(other.intent) ? 1.0 : 0.0;
            factors++;
        }
        
        // Numerical feature similarities
        similarity += 1.0 - Math.abs(this.urgency - other.urgency);
        factors++;
        
        similarity += 1.0 - Math.abs(this.formality - other.formality);
        factors++;
        
        similarity += 1.0 - Math.abs(this.emphasis - other.emphasis);
        factors++;
        
        return factors > 0 ? similarity / factors : 0.0;
    }
    
    // Getters and setters
    public String getTranscribedText() { return transcribedText; }
    public void setTranscribedText(String transcribedText) { this.transcribedText = transcribedText; }
    
    public String getDetectedLanguage() { return detectedLanguage; }
    public void setDetectedLanguage(String detectedLanguage) { this.detectedLanguage = detectedLanguage; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    
    public double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(double sentimentScore) { this.sentimentScore = sentimentScore; }
    
    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }
    
    public double getEmotionConfidence() { return emotionConfidence; }
    public void setEmotionConfidence(double emotionConfidence) { this.emotionConfidence = emotionConfidence; }
    
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    
    public double getUrgency() { return urgency; }
    public void setUrgency(double urgency) { this.urgency = urgency; }
    
    public double getFormality() { return formality; }
    public void setFormality(double formality) { this.formality = formality; }
    
    public Map<String, Double> getTopicProbabilities() { return topicProbabilities; }
    public void setTopicProbabilities(Map<String, Double> topicProbabilities) { this.topicProbabilities = topicProbabilities; }
    
    public List<SemanticEntity> getEntities() { return entities; }
    public void setEntities(List<SemanticEntity> entities) { this.entities = entities; }
    
    public double getEmphasis() { return emphasis; }
    public void setEmphasis(double emphasis) { this.emphasis = emphasis; }
    
    public double getPausePattern() { return pausePattern; }
    public void setPausePattern(double pausePattern) { this.pausePattern = pausePattern; }
    
    public double getBreathPattern() { return breathPattern; }
    public void setBreathPattern(double breathPattern) { this.breathPattern = breathPattern; }
    
    public List<Double> getStressTiming() { return stressTiming; }
    public void setStressTiming(List<Double> stressTiming) { this.stressTiming = stressTiming; }
    
    public double getPitch() { return pitch; }
    public void setPitch(double pitch) { this.pitch = pitch; }
    
    public String getEmotionalContext() { return emotionalContext; }
    public void setEmotionalContext(String emotionalContext) { this.emotionalContext = emotionalContext; }
    
    public double getEnergy() { return energy; }
    public void setEnergy(double energy) { this.energy = energy; }
    
    public String getPhoneticContent() { return phoneticContent; }
    public void setPhoneticContent(String phoneticContent) { this.phoneticContent = phoneticContent; }
}