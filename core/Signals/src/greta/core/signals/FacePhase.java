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
