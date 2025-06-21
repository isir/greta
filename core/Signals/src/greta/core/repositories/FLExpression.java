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
package greta.core.repositories;

import greta.core.animation.mpeg4.fap.FAPType;
import greta.core.util.parameter.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * This is definition of an action units in FAPs. See file aulibrary.xml
 *
 * @author Radoslaw Niewiadomski
 */
public class FLExpression implements Parameter<FLExpression> { //TODO extends FAPFrame instead of parameter

    //label = ie name!
    private String name;

    //fap list of this au
    private ArrayList<FAPItem> faps;

     /**
     * Constructor
     * @param name the name of the expression
     *
     */
     public FLExpression (String name){
        this.name = name;
        faps = new ArrayList<FAPItem>();
    }//end of method

    @Override
    public String getParamName() {
        return name;
    }//end of method


    @Override
    public void setParamName(String name) {
        this.name = name;
    }//end of method

    /**
     * Returns the list of all {@code AUItem} used in this {@code FLExpression}.
     * @return the list of all {@code AUItem} used
     */
    public List<FAPItem> getFAPs(){
        return faps;
    }//end of method


    /**
     * Adds a {@code AUItem} in the face library item.<br/>
     * @param type the target {@code FAPType}
     * @param value the value of the fap
     */
    public void add(FAPType type, int value){
        if(type == null)
            return; //never add a null object

        faps.add(new FAPItem(type, value));
    }//end of method

    @Override
    public boolean equals(FLExpression other) {

        if( ! name.equalsIgnoreCase(other.name)){
            return false;
        }
        if(faps.size() != other.faps.size()){
            return false;
        }

        for(FAPItem fap : faps){
            boolean found = false;
            for(FAPItem fap2 : other.faps){
                if(fap.equals(fap2)){
                    found = true;
                    break;
                }
            }
            if( ! found ){
                return false;
            }
        }
         return true;
    }


    public static class FAPItem{
        public final FAPType type;
        public final int value;
        FAPItem(FAPType type, int value){
            this.type = type;
            this.value = value;
        }
    }
}
