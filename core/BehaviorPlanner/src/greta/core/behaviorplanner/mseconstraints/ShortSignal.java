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
package greta.core.behaviorplanner.mseconstraints;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class ShortSignal {
    double start;
    double end;
    String label;
    int id;

    public ShortSignal(){
    start = -1;
    end =-1;
    label="";
    id=-1;
    }

    public void setStart(double start){
        this.start = start;
    }

    public void setEnd(double end){
        this.end = end;
    }

    public void setLabel(String label){
        this.label = label;
    }

    public void setId(int id){
        this.id = id;
    }

    public double getStart(){
        return this.start;
    }

    public double getEnd(){
        return this.end;
    }

    public String getLabel(){
        return this.label;
    }

    public int getId(){
        return this.id;
    }

}
