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
public class Ln implements Function{

    public static Function of(){
        return of(X.x);
    }

    public static Function of(double d){
        return Constante.of(Math.log(d));
    }

    public static Function of(Function f){
        if(f instanceof Constante){
            return Constante.of(Math.log(((Constante)f).getValue()));
        }
        if(f instanceof Exp){
            return ((Exp)f).getOperand();
        }
        return new Ln(f);
    }

    private Function f;

    private Ln(Function f){
        this.f = f;
    }

    protected Function getOperand(){
        return f;
    }

    @Override
    public double f(double x) {
        return Math.log(f.f(x));
    }

    @Override
    public String getName() {
        return "Ln";
    }

    @Override
    public String toString() {
        return "ln("+f+")";
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
                return Constante.ZERO;
            }
            if(cst.getValue()==1){
                return Inverse.of(f);
            }
        }
        return Product.of(fprim, Inverse.of(f));
    }

    @Override
    public Function simplified() {
        return Ln.of(f.simplified());
    }
}
