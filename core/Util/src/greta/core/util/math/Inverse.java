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
public class Inverse implements Function{

    public static Function of(){
        return of(X.x);
    }

    public static Function of(double d){
        return Constante.of(1/d);
    }

    public static Function of(Function f){
        if(f instanceof Constante){
            return Constante.of(1/((Constante)f).getValue());
        }
        return new Inverse(f);
    }

    private Function f;

    private Inverse(Function f){
        this.f = f;
    }

    protected Function getOperand(){
        return f;
    }

    @Override
    public double f(double x) {
        return 1 / f.f(x);
    }

    @Override
    public String getName() {
        return "Inverse";
    }

    @Override
    public String toString() {
        return "1/("+f+")";
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
            if(cst.equals(0)){
                //TODO NAN !
            }
        }
        return Product.of(Constante.of(-1), fprim, Inverse.of(Product.of(f,f)));
    }

    @Override
    public Function simplified() {
        return Inverse.of(f.simplified());
    }
}
