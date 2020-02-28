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
