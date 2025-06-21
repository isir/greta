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
package greta.core.util.enums;

/**
 * This is an enumeration of social attitude descriptors.
 *
 * @author Angelo Cafaro
 */
public enum SocialAttitude {

    /**
     * Describes a neutral social attitude
     */
    neutral,
    /**
     * Describe a submissive social attitude (or dominance decrease)
     */
    submissive,
    /**
     * Describe a dominant social attitude (or dominance increase)
     */
    dominant,
    /**
     * Describe an hostile social attitude (or friendliness decrease)
     */
    hostile,
    /**
     * Describe a friendly social attitude (or friendliness increase)
     */
    friendly;

    public static SocialAttitude interpret(String aSocialAttitude, SocialAttitude defaultSocialAttitude) {
        try {
            return valueOf(aSocialAttitude);
        } catch (Throwable t) {
            return defaultSocialAttitude;
        }
    }

    public static SocialAttitude interpret(String aSocialAttitude) {
        return interpret(aSocialAttitude, neutral);
    }
}
