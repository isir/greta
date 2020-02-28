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
package greta.core.util;

/**
 * This class contains several constants
 * @author Andre-Marie Pez
 */
public final class Constants {
    private Constants(){
        //can not be instanciate
    }

    public static final int FRAME_PER_SECOND = 25;
    public static final int FRAME_DURATION_MILLIS = 1000/FRAME_PER_SECOND;
    public static final double FRAME_DURATION_SECONDS = 1.0/FRAME_PER_SECOND;

    // This time marker ID has an absolute value that refers to the Intention (i.e. Speech) currently played in Greta when the interruption occurs
    public static final String _TIME_MARKER_INTERRUPTION_DETECTED_ID = "_interruption_detected";

    // This time marker ID has an absolute value that refers to the Intention (i.e. Speech) currently played in Greta when the interruption occurs
    public static final String _TIME_MARKER_INTERRUPTION_REACTION_STARTED_ID = "_reaction_started";

    // This time marker ID has an absolute value that refers to Reaction that replaces the Intention (i.e. Speech) currently played in Greta when the interruption occurs
    public static final String TIME_MARKER_INTERRUPTION_REACTION_END_ID = "reaction_end";

    //TODO: move enums from greta.core.util.enums to here and perhaps simplify these enums

}
