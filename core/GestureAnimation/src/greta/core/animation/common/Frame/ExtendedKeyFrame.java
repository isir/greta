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
package greta.core.animation.common.Frame;

import greta.core.util.math.Function;

/**
 *
 * @author Andre-Marie Pez
 */
public class ExtendedKeyFrame extends KeyFrame{

    private Function function = new greta.core.util.math.easefunctions.Linear();

    public ExtendedKeyFrame(double time){
        super(time);
    }

    public void setFunction(Function function){
        this.function = function;
    }

    public Function getFunction(){
        return function;
    }
}
