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
