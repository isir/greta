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
 * @author Andre-Marie Pez
 */
public class Cos implements Function{

    public static Function of(Function f){
        if(f instanceof Constante){
            return Constante.of(Math.cos(((Constante)f).getValue()));
        }
        return new Cos(f);
    }

    private Function f;

    public Cos(){
        this(X.x);
    }

    public Cos(Function f){
        this.f = f;
    }


    @Override
    public double f(double x) {
        return Math.cos(f.f(x));
    }

    @Override
    public String getName() {
        return "Cos";
    }

    @Override
    public String toString() {
        return "cos( "+f+" )";
    }


    @Override
    public Function getDerivative() {
        if(f instanceof Constante){
            return Constante.ZERO;
        }
        Function fprim = f.getDerivative();
        if(fprim==null){
            return null;
        }
        if(fprim instanceof Constante){
            Constante cst = (Constante)fprim;
            if(cst.getValue()==0){
                return Constante.ZERO;
            }
            if(cst.getValue()==1){
                return Product.of(Constante.of(-1), new Sin(f));
            }
        }
        return Product.of(Constante.of(-1), new Sin(f), fprim);
    }

    @Override
    public Function simplified() {
        return Cos.of(f.simplified());
    }

}
