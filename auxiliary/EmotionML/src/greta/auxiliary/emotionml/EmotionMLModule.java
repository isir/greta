/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.auxiliary.emotionml;

/**
 * EmotionML module placeholder.
 * 
 * This module provides emotion markup language support for emotional behavior modeling.
 * Main functionality has been temporarily excluded due to dependency issues with
 * SocialParameters module which is not part of the current Maven build.
 * 
 * @author Greta Modernization Project
 */
public class EmotionMLModule {
    
    /**
     * Module information
     */
    public static final String MODULE_NAME = "EmotionML";
    public static final String VERSION = "1.0.0-SNAPSHOT";
    public static final String DESCRIPTION = "Emotion markup language support for emotional behavior modeling";
    
    /**
     * Private constructor to prevent instantiation
     */
    private EmotionMLModule() {
        // Utility class
    }
    
    /**
     * Get module information
     * @return module name and version
     */
    public static String getModuleInfo() {
        return MODULE_NAME + " v" + VERSION + " - " + DESCRIPTION;
    }
}