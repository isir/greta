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
