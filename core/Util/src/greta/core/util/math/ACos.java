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
package greta.core.util.math;

/**
 *
 * @author André-Marie
 */
public class ACos implements Function{

    public static Function of(Function f){
        if(f instanceof Constante){
            return Constante.of(Math.acos(((Constante)f).getValue()));
        }
        return new ACos(f);
    }

    private Function f;

    public ACos(){
        this(X.x);
    }

    public ACos(Function f){
        this.f = f;
    }


    @Override
    public double f(double x) {
        return Math.acos(f.f(x));
    }

    @Override
    public String getName() {
        return "Arc Cosinus";
    }

    @Override
    public String toString() {
        return "acos( "+f+" )";
    }

    @Override
    public Function getDerivative() {
        return null; //TODO
    }

    @Override
    public Function simplified() {
        return ACos.of(f.simplified());
    }

}
