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
package greta.core.util.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class Product implements Function {

    public static Function of(Function... factors){
        return of(false, factors);
    }
    public static Function of(boolean siplify, Function... factors){
        return of(siplify, Arrays.asList(factors));
    }

    /**
     * Returns the {@code Function} representing the product of all {@code Function}s passed in parameter.<br/>
     * It may not be a {@code Product} object. for exemple: <code>Sum.of(aFunction, new Constante(1))</code> will return {@code aFunction}.
     * @param factors the {@code Function}s to multiply
     * @return the {@code Function} representing the product of {@code Function}s
     */
    public static Function of(List<Function> factors){
        return of(false, factors);
    }

    public static Function of(boolean siplify, List<Function> factors){
        if(factors.isEmpty()){
            return Constante.ZERO;
        }
        Product product = new Product(factors);
        if(product.factors.length==1){
            return siplify ? product.factors[0].simplified() : product.factors[0];
        }
        return siplify ? product.simplified() : product;
    }

    Function[] factors;
    private Product(Function... factors) {
        this(Arrays.asList(factors));
    }

    private Product(List<Function> factors){
        ArrayList<Function> temp = new ArrayList<Function>(factors.size());
        boolean zeroFound = false;
        for(Function f : factors) {
            zeroFound |= findAssociative(temp, f);
        }
        if(temp.isEmpty()||zeroFound){
            this.factors = new Function[1];
            this.factors[0] = Constante.ZERO;
        }
        else{
            boolean negative = false; // -1^n = n%2==0?1:-1
            for(int i=temp.size()-1; i>=0; --i){
                Function f = temp.get(i);
                if(f instanceof Constante && ((Constante)f).equals(-1)){ // -1^n = n%2==0?1:-1
                    temp.remove(i);
                    negative = !negative;
                }
            }
            if(negative){ // -1^n = n%2==0?1:-1
                temp.add(Constante.of(-1));
            }
            this.factors = new Function[temp.size()];
            for(int i=0; i<this.factors.length; ++i){
                this.factors[i] = temp.get(i);
            }
        }
        //a better thing to do may be to merge all Constante in one.
        //but there is a problem: if at the end there is only one Function, this
        //will be the only Function and this as no sens.
    }


    /**
     *
     * @param noAssociatives
     * @param toUnassociate
     * @return true if a zero is found
     */
    private boolean findAssociative(List<Function> noAssociatives, Function toUnassociate){
        if(toUnassociate == null){ //skip this
            return false;
        }
        if(toUnassociate instanceof Product){
            for(Function f : ((Product)toUnassociate).factors){
                if(findAssociative(noAssociatives, f)){
                    return true;
                }
            }
        }
        else{
            if(toUnassociate instanceof Constante){
                if(((Constante)toUnassociate).getValue()==0){
                    return true;
                }
                if(((Constante)toUnassociate).getValue()!=1){
                    noAssociatives.add(toUnassociate);
                }
            }
            else{
                noAssociatives.add(toUnassociate);
            }
        }
        return false;
    }

    @Override
    public double f(double x) {
        double result = 1;
        for(Function funct : factors){
            result *= funct.f(x);
        }
        return result;
    }

    @Override
    public String getName() {
        return "Product";
    }

    @Override
    public String toString() {
        String formula = "";
        boolean first = true;
        for(Function funct : factors){
            if(!first){
                formula += " * ";
            }
            else{
                first = false;
            }
            if(funct instanceof Sum){
                formula += "("+funct+")";
            }
            else{
                formula += funct;
            }
        }
        return formula;
    }

    @Override
    public Function getDerivative() {
        Function[] toSum = new Function[factors.length];
        for(int i=0; i<factors.length; ++i){
            Function derivative = factors[i].getDerivative();
            if(derivative==null){
                return null;
            }
            Function[] otherFactors = new Function[factors.length];
            for(int j=0; j<factors.length; ++j){
                otherFactors[j] = i==j ? derivative : factors[j];
            }
            toSum[i] = Product.of(otherFactors);
        }

        return Sum.of(toSum);
    }

    @Override
    public Function simplified(){

        ArrayList<Function> nonConstant = new ArrayList<Function>(factors.length);
        ArrayList<Function> inverses = new ArrayList<Function>(factors.length); // 1/a * 1/b = 1/(a*b)
        double constantes = 1;
        for(int i=0; i< factors.length; ++i){
            Function simplified = factors[i].simplified();
            if(simplified instanceof Constante){
                constantes *= ((Constante)simplified).getValue();
            }
            else{
                if(simplified instanceof Inverse){
                    inverses.add(((Inverse)simplified).getOperand());
                }
                else{
                    nonConstant.add(simplified);
                }
            }
        }


        if(constantes == 0){
            return Constante.ZERO;
        }
        if(!inverses.isEmpty()) {
            nonConstant.add(Inverse.of(Product.of(inverses)));
        }

        if(constantes != 1) {
            nonConstant.add(Constante.of(constantes));
        }

        return Product.of(nonConstant);
    }
}
