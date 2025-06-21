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
