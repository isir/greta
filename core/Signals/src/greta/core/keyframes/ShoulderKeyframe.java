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
package greta.core.keyframes;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class ShoulderKeyframe extends ParametersKeyframe {

    //two directions are possible
    //eqch direction takes a value
    //up ... 0...1
    //back front ... -1...1
    /**
     * relative value of displacement on Y
     */
    public double front;

    /**
     * relative value of displacement on X
     */
    public double up;

    /**
     * Symmetric, Assymetric, Left, Right
     */
    public String side;
    
    private String parentId;

    public ShoulderKeyframe(String id, String category, String phase, double time, String side, double front, double up) {

        this.id = id;
        this.modality = "shoulder";
        this.category = category;
        this.phaseType = phase;
        this.onset = 0;
        this.offset = time;

        this.side = side;

        this.front = front;
        this.up = up;

    }

    public ShoulderKeyframe(String id, String category, String phase, double time) {

        this.id = id;
        this.modality = "shoulder";
        this.category = category;
        this.phaseType = phase;
        this.onset = 0;
        this.offset = time;

        this.side = "both";

        this.front = 0;
        this.up = 0;
    }

    public ShoulderKeyframe(String id, String category, String phase, double time, String side) {

        this.id = id;
        this.modality = "shoulder";
        this.category = category;
        this.phaseType = phase;
        this.onset = 0;
        this.offset = time;

        this.side = side;

        this.front = 0;
        this.up = 0;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public void setFront(double front) {
        this.front = front;
    }

    public void setUp(double up) {
        this.up = up;
    }

    public double getFront() {
        return front;
    }

    public double getUp() {
        return up;
    }

    public String getSide() {
        return side;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parParentId) {
        this.parentId = parParentId;
    }
}//end of class