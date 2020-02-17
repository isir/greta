/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
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
