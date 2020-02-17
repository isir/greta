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
