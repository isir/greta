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

import greta.core.util.parameter.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * This is definition of an expression by AUs. See file facelibrary.xml
 *
 * @author Radoslaw Niewiadomski
 */
public class AUExpression implements Parameter<AUExpression> {

    public static int NUMBER_OF_AUS = 64;
    //label
    private String paramName;
    //au list
    private ArrayList<AUItem> actionUnits;
    //class
    private String type;
    private String instanceName;

    /**
     * Constructor
     *
     * @param name the name of the expression
     */
    public AUExpression(String name) {
        this(name, "faceexp");
    }

    /**
     * Constructor
     *
     * @param name the name of the expression
     * @param type the type of the expression
     */
    public AUExpression(String name, String type) {
        this.type = type;
        this.instanceName = name;
        this.paramName = type + "=" + name;
        actionUnits = new ArrayList<AUItem>();
    }

    @Override
    public String getParamName() {
        return paramName;
    }


    @Override
    public void setParamName(String name) {
        this.paramName = name;
    }

    public String getType() {
        return type;
    }

    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Returns the list of all {@code AUItem} used in this {@code AUExpression}.
     *
     * @return the list of all {@code AUItem} used
     */
    public List<AUItem> getActionUnits() {
        return actionUnits;
    }

    /**
     * Adds a {@code AUItem} in the face library item.<br/>
     *
     * @param item the {@code AUItem} to add in the set
     */
    public void add(AUItem item) {
        if (item != null) {
            actionUnits.add(item);
        }
        //never add a null object
    }

    public boolean equals(AUExpression aue) {
        if(this==aue){
            return true;
        }
        if( ! this.type.equalsIgnoreCase(aue.type)){
            return false;
        }
        if( ! this.instanceName.equalsIgnoreCase(aue.instanceName)){
            return false;
        }

        if(this.actionUnits.size() != aue.actionUnits.size()){
            return false;
        }

        //we suppose that their is not duplicate in the actionUnits List
        for(AUItem au : actionUnits){
            boolean found = false;
            for(AUItem au2 : aue.actionUnits){
                if(au.equals(au2)){
                    found = true;
                    break;
                }
            }
            if( ! found ){
                return false;
            }
        }

        //normally, paramName = type+"="+instanceName
        //but, setParamName method may changes it
//        if( ! this.paramName.equalsIgnoreCase(aue.paramName)){
//            return false;
//        }

        return true;
    }
}
