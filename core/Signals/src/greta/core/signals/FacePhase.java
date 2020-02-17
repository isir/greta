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
package greta.core.signals;

/**
 *
 * @author Quoc Anh Le
 */
public class FacePhase {
    double startTime;
    double endTime;
    String type; // start, attack, decay, sustain, end


    //NOT HERE: PHASE does not contains the action units!
    //action units are for a whole facial expressions!
    //i cannot have a action without start, apex or end - nonsense
    //private String visem;
    //private String actionUnit;

    public FacePhase(String type, double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
