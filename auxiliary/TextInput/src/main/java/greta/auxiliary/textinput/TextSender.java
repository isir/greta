/*
 * This file is part of Greta.
 * Licensed under the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package greta.auxiliary.textinput;

import java.util.Map;

/**
 * Interface for sending text from Text Input module to other Greta components
 */
public interface TextSender {
    
    /**
     * Send text with metadata to connected components
     * @param text The text content to send
     * @param metadata Additional metadata (content-id, timestamp, etc.)
     */
    void send(String text, Map<String, Object> metadata);
}