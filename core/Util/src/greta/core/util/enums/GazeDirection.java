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
 * Closed set item for BML gaze signals directions. Defined in the BML standard.
 * @author Mathieu Chollet
 */
public enum GazeDirection {
    LEFT,
    RIGHT,
    UP,
    DOWN,
    FRONT,
    //BACK,
    UPRIGHT,
    UPLEFT,
    DOWNLEFT,
    DOWNRIGHT;

    public GazeDirection opposite(){
        if(this==LEFT)
            return RIGHT;
        if(this==RIGHT)
            return LEFT;
        if(this==UPRIGHT)
            return DOWNLEFT;
        if(this==DOWNLEFT)
            return UPRIGHT;
        if(this==DOWNRIGHT)
            return UPLEFT;
        if(this==UPLEFT)
            return DOWNRIGHT;
        if(this==UP)
            return DOWN;
        if(this==DOWN)
            return UP;

        return FRONT;
    }

}
