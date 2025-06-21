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
package greta.core.feedbacks;

import greta.core.util.id.ID;

/**
 *
 * @author Ken Prepin
 */
public class Callback {

    private String type;
    private double time;
    private ID animId;

    public Callback(Callback callback){
        this(callback.type(),callback.time(), callback.animId());
    }

    public Callback(String type, double time, ID animId) {
        this.type = type;
        this.time = time;
        this.animId = animId;
    }

    public String type() {
        return type;
    }

    public double time() {
        return time;
    }

    public ID animId() {
        return animId;
    }
    public void setType(String type){
        this.type = type;
    }
     public void setTime(double time){
        this.time = time;
    }
    public void setAnimId(ID animId){
        this.animId = animId;
    }
}
