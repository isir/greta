/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package greta.application.ariavaluspa.core;

/**
 *
 * @author Angelo Cafaro
 */
public enum ARIALanguage {

    english,
    french,
    german;
    
    public static final int NUM_LANGUAGES = values().length;

    public static ARIALanguage interpret(String languageName, ARIALanguage defaultLanguageName) {
        try {
            return valueOf(languageName);
        } catch (Throwable t) {
            return defaultLanguageName;
        }
    }

    public static ARIALanguage interpret(String languageName) {
        return interpret(languageName, english);
    }
}
