/*
 * This file is part of Greta.
 * Licensed under the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package greta.auxiliary.textinput;

import java.util.Map;

/**
 * Simple implementation of TextSender for direct text output
 */
public class SimpleTextSender implements TextSender {
    
    @Override
    public void send(String text, Map<String, Object> metadata) {
        // For now, just log the text
        // In a full implementation, this would connect to TTS or other components
        System.out.println("TextSender: " + text);
        System.out.println("Metadata: " + metadata);
        
        // TODO: Connect to Greta TTS/Avatar system
        // This could send to:
        // - TTS modules (AzureTTS, MaryTTS, CereProc)
        // - WebAvatar Player for lip sync
        // - Animation system for gestures
    }
}