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
package greta.core.util.parameter;

/**
 *
 * @author Andre-Marie Pez
 */
public class EngineParameter implements Parameter<EngineParameter>{

    /** The name of this parameter */
    private String name;
    /** The lower bound of the interval that contains the value of this parameter */
    private double min = -1;
    /** The upper bound of the interval that contains the value of this parameter */
    private double max = 1;
    /** The value of this parameter */
    private double value = 0;

    /**
     * Constructs an {@code EngineParameter}.<br/>
     * The value is set to 0, in the interval [-1, 1].
     * @param name the name of the parameter
     */
    public EngineParameter(String name){
        this(name, 0, -1, 1);
    }

    /**
     * Constructs an {@code EngineParameter}.<br/>
     * The interval is set to [-1, 1].
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public EngineParameter(String name, double value){
        this(name, value, -1, 1);
    }

    /**
     * Constructs an {@code EngineParameter}.<br/>
     * @param name the name of the parameter
     * @param value the value of the parameter
     * @param min the min bound of the interval that contains the value
     * @param max the max bound of the interval that contains the value
     */
    public EngineParameter(String name, double value, double min, double max){
        this.name = name;
        this.value = value;
        this.min = min;
        this.max = max;
    }

    @Override
    public String getParamName(){
        return name;
    }

    @Override
    public void setParamName(String name){
        this.name = name;
    }

    /**
     * Returns the lower bound of the interval that contains the value.
     * @return the value of the lower bound
     */
    public double getMin(){
        return min;
    }

    /**
     * Returns the upper bound of the interval that contains the value.
     * @return the value of the upper bound
     */
    public double getMax(){
        return max;
    }

    /**
     * Returns the value of this {@code EngineParameter}
     * @return the value of this {@code EngineParameter}
     */
    public double getValue(){
        return value;
    }

    /**
     * Computes and returns the value of this parameter in a specific interval.
     * @param min min bounds of the target interval
     * @param max max bounds of the target interval
     * @return the value scaled in the interval [min, max]
     */
    public double getValueIn(double min, double max){
        return greta.core.util.math.Functions.changeInterval(value, this.min, this.max, min, max);
    }

    /**
     * Set a new interval to this {@code EngineParameter} and rescales its value.
     * @param min
     * @param max
     */
    public void scaleIn(double min, double max){
        value = getValueIn(min,max);
        this.min = min;
        this.max = max;
    }

    /**
     * Sets the lower bound of the interval that contains the value.<br/>
     * The value will not be scaled (use {@code scaleIn(min,max)} instead).
     * @param min the value of the lower bound
     * @see #scaleIn(double, double) scaleIn(double, double)
     */
    public void setMin(double min){
        this.min = min;
    }

    /**
     * Sets the upper bound of the interval that contains the value.<br/>
     * The value will not be scaled (use {@code scaleIn(min,max)} instead.
     * @param max the value of the upper bound
     * @see #scaleIn(double, double) scaleIn(double, double)
     */
    public void setMax(double max){
        this.max = max;
    }

    /**
     * Sets the value of this {@code EngineParameter}.
     * @param value the value to set
     */
    public void setValue(double value){
        this.value = value;
    }

    @Override
    public EngineParameter clone(){
        return new EngineParameter( name, value, min, max);
    }

    @Override
    public boolean equals(EngineParameter other) {
        return  name.equalsIgnoreCase(other.name) &&
                other.value == value && //perhaps compare only normalized value and not the mins and the maxs
                other.min == min &&
                other.max == max;
    }
}
