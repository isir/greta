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
package greta.core.interruptions.reactions;

/**
 *
 * @author Angelo Cafaro
 */
public enum BehaviorType {

    NONE,
    HEAD_TILT,
    HEAD_NOD_TOSS,
    EYES_LIDS_CLOSE,
    EYES_BROWS,
    EYES_SQUEEZE,
    SMILE,
    GESTURE_HOLD,
    GESTURE_RETRACT,
    SHOULDERS_UP_FORWARD;

    public static BehaviorType interpret(String behaviorType) {
        try {
            return valueOf(behaviorType.toUpperCase());
        } catch (Throwable t) {
            return NONE;
        }
    }

}
