package greta.auxiliary.llm.model;

import java.util.Map;

/**
 * Personality profile for consistent character behavior in conversations
 */
public class PersonalityProfile {
    
    // Core personality traits (Big Five model)
    private double openness = 0.5;          // 0.0 to 1.0
    private double conscientiousness = 0.5;  // 0.0 to 1.0
    private double extraversion = 0.5;       // 0.0 to 1.0
    private double agreeableness = 0.5;      // 0.0 to 1.0
    private double neuroticism = 0.5;        // 0.0 to 1.0
    
    // Communication style
    private String communicationStyle = "balanced"; // formal, casual, balanced, enthusiastic
    private String languageRegister = "neutral";    // formal, informal, neutral, academic
    private double verbosity = 0.5;                // 0.0 (concise) to 1.0 (verbose)
    private double empathy = 0.7;                  // 0.0 to 1.0
    
    // Domain expertise
    private String primaryDomain = "general";       // education, research, therapy, entertainment
    private double expertise = 0.5;                // 0.0 (novice) to 1.0 (expert)
    
    // Cultural and demographic
    private String culturalBackground = "neutral";
    private String ageGroup = "adult";             // child, teen, adult, elderly
    private String gender = "neutral";             // male, female, neutral
    
    // Behavioral preferences
    private boolean usesHumor = true;
    private boolean asksClarifyingQuestions = true;
    private boolean providesExamples = true;
    private boolean expressesUncertainty = true;
    
    // Custom attributes
    private Map<String, Object> customAttributes;
    
    public PersonalityProfile() {}
    
    /**
     * Create a predefined personality profile
     */
    public static PersonalityProfile createProfile(String profileType) {
        PersonalityProfile profile = new PersonalityProfile();
        
        switch (profileType.toLowerCase()) {
            case "teacher":
                profile.conscientiousness = 0.8;
                profile.agreeableness = 0.8;
                profile.empathy = 0.9;
                profile.communicationStyle = "encouraging";
                profile.primaryDomain = "education";
                profile.expertise = 0.8;
                profile.providesExamples = true;
                profile.asksClarifyingQuestions = true;
                break;
                
            case "researcher":
                profile.openness = 0.9;
                profile.conscientiousness = 0.8;
                profile.languageRegister = "academic";
                profile.primaryDomain = "research";
                profile.expertise = 0.9;
                profile.expressesUncertainty = true;
                profile.usesHumor = false;
                break;
                
            case "therapist":
                profile.agreeableness = 0.9;
                profile.empathy = 0.95;
                profile.neuroticism = 0.2;
                profile.communicationStyle = "supportive";
                profile.primaryDomain = "therapy";
                profile.asksClarifyingQuestions = true;
                profile.verbosity = 0.3; // More listening than talking
                break;
                
            case "entertainer":
                profile.extraversion = 0.9;
                profile.openness = 0.8;
                profile.communicationStyle = "enthusiastic";
                profile.primaryDomain = "entertainment";
                profile.usesHumor = true;
                profile.verbosity = 0.7;
                break;
                
            case "assistant":
            default:
                profile.conscientiousness = 0.8;
                profile.agreeableness = 0.7;
                profile.empathy = 0.7;
                profile.communicationStyle = "helpful";
                profile.asksClarifyingQuestions = true;
                profile.providesExamples = true;
                break;
        }
        
        return profile;
    }
    
    /**
     * Generate system prompt based on personality profile
     */
    public String generateSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an embodied conversational agent with the following personality:\n\n");
        
        // Personality traits
        prompt.append("Personality Traits:\n");
        prompt.append("- Openness: ").append(formatTrait(openness)).append("\n");
        prompt.append("- Conscientiousness: ").append(formatTrait(conscientiousness)).append("\n");
        prompt.append("- Extraversion: ").append(formatTrait(extraversion)).append("\n");
        prompt.append("- Agreeableness: ").append(formatTrait(agreeableness)).append("\n");
        prompt.append("- Emotional Stability: ").append(formatTrait(1.0 - neuroticism)).append("\n\n");
        
        // Communication style
        prompt.append("Communication Style:\n");
        prompt.append("- Style: ").append(communicationStyle).append("\n");
        prompt.append("- Language register: ").append(languageRegister).append("\n");
        prompt.append("- Verbosity: ").append(formatTrait(verbosity)).append("\n");
        prompt.append("- Empathy: ").append(formatTrait(empathy)).append("\n\n");
        
        // Domain expertise
        prompt.append("Expertise: ").append(primaryDomain)
               .append(" (").append(formatTrait(expertise)).append(")\n\n");
        
        // Behavioral preferences
        prompt.append("Behavioral Guidelines:\n");
        if (usesHumor) prompt.append("- Use appropriate humor when suitable\n");
        if (asksClarifyingQuestions) prompt.append("- Ask clarifying questions when needed\n");
        if (providesExamples) prompt.append("- Provide examples to illustrate points\n");
        if (expressesUncertainty) prompt.append("- Express uncertainty when appropriate\n");
        
        prompt.append("\nAlways maintain consistency with this personality profile in your responses.");
        
        return prompt.toString();
    }
    
    private String formatTrait(double value) {
        if (value < 0.3) return "low";
        else if (value < 0.7) return "moderate";
        else return "high";
    }
    
    // Getters and setters for all fields
    public double getOpenness() { return openness; }
    public void setOpenness(double openness) { this.openness = openness; }
    
    public double getConscientiousness() { return conscientiousness; }
    public void setConscientiousness(double conscientiousness) { this.conscientiousness = conscientiousness; }
    
    public double getExtraversion() { return extraversion; }
    public void setExtraversion(double extraversion) { this.extraversion = extraversion; }
    
    public double getAgreeableness() { return agreeableness; }
    public void setAgreeableness(double agreeableness) { this.agreeableness = agreeableness; }
    
    public double getNeuroticism() { return neuroticism; }
    public void setNeuroticism(double neuroticism) { this.neuroticism = neuroticism; }
    
    public String getCommunicationStyle() { return communicationStyle; }
    public void setCommunicationStyle(String communicationStyle) { this.communicationStyle = communicationStyle; }
    
    public String getLanguageRegister() { return languageRegister; }
    public void setLanguageRegister(String languageRegister) { this.languageRegister = languageRegister; }
    
    public double getVerbosity() { return verbosity; }
    public void setVerbosity(double verbosity) { this.verbosity = verbosity; }
    
    public double getEmpathy() { return empathy; }
    public void setEmpathy(double empathy) { this.empathy = empathy; }
    
    public String getPrimaryDomain() { return primaryDomain; }
    public void setPrimaryDomain(String primaryDomain) { this.primaryDomain = primaryDomain; }
    
    public double getExpertise() { return expertise; }
    public void setExpertise(double expertise) { this.expertise = expertise; }
    
    public String getCulturalBackground() { return culturalBackground; }
    public void setCulturalBackground(String culturalBackground) { this.culturalBackground = culturalBackground; }
    
    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public boolean isUsesHumor() { return usesHumor; }
    public void setUsesHumor(boolean usesHumor) { this.usesHumor = usesHumor; }
    
    public boolean isAsksClarifyingQuestions() { return asksClarifyingQuestions; }
    public void setAsksClarifyingQuestions(boolean asksClarifyingQuestions) { this.asksClarifyingQuestions = asksClarifyingQuestions; }
    
    public boolean isProvidesExamples() { return providesExamples; }
    public void setProvidesExamples(boolean providesExamples) { this.providesExamples = providesExamples; }
    
    public boolean isExpressesUncertainty() { return expressesUncertainty; }
    public void setExpressesUncertainty(boolean expressesUncertainty) { this.expressesUncertainty = expressesUncertainty; }
    
    public Map<String, Object> getCustomAttributes() { return customAttributes; }
    public void setCustomAttributes(Map<String, Object> customAttributes) { this.customAttributes = customAttributes; }
}