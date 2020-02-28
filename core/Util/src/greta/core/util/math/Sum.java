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
public class Sum implements Function{

    /**
     * Returns the {@code Function} representing the sum of all {@code Function}s passed in parameter.<br/>
     * It may not be a {@code Sum} object. for exemple: <code>Sum.of(aFunction, new Constante(0))</code> will return {@code aFunction}.
     * @param toSum the {@code Function}s to sum
     * @return the {@code Function} representing the sum of {@code Function}s
     */
    public static Function of(Function... toSum){
        return of(false, toSum);
    }

    public static Function of(boolean siplify, Function... toSum){
        return of(siplify, Arrays.asList(toSum));
    }

    /**
     * Returns the {@code Function} representing the sum of all {@code Function}s passed in parameter.<br/>
     * It may not be a {@code Sum} object. for exemple: <code>Sum.of(aFunction, new Constante(0))</code> will return {@code aFunction}.
     * @param toSum the {@code Function}s to sum
     * @return the {@code Function} representing the sum of {@code Function}s
     */
    public static Function of(List<Function> toSum){
        return of(false, toSum);
    }

    public static Function of(boolean siplify, List<Function> toSum){
        if(toSum.isEmpty()){
            return Constante.ZERO;
        }
        Sum sum = new Sum(toSum);
        if(sum.operands.length==1){
            return siplify ? sum.operands[0].simplified() : sum.operands[0];
        }
        return siplify ? sum.simplified() : sum;
    }

    private Function[] operands;

    private Sum(Function... toSum){
        this(Arrays.asList(toSum));
    }

    private Sum(List<Function> toSum){
        ArrayList<Function> temp = new ArrayList<Function>(toSum.size());
        for(Function f : toSum) {
            findAssociative(temp, f);
        }
        if(temp.isEmpty()){
            operands = new Function[1];
            operands[0] = Constante.ZERO;
        }
        else{
            operands = new Function[temp.size()];
            for(int i=0; i<operands.length; ++i){
                operands[i] = temp.get(i);
            }
        }
        //a better thing to do may be to merge all Constante in one.
        //but there is a problem: if at the end there is only one Function, this
        //will be the only Function and this as no sens.
    }


    private void findAssociative(List<Function> noAssociatives, Function toUnassociate){
        if(toUnassociate == null){
            return;
        }
        if(toUnassociate instanceof Sum){
            for(Function f : ((Sum)toUnassociate).operands){
                findAssociative(noAssociatives, f);
            }
        }
        else{
            if((!(toUnassociate instanceof Constante)) || ((Constante)toUnassociate).getValue()!=0) {
                noAssociatives.add(toUnassociate);
            }
        }
    }

    @Override
    public double f(double x) {
        double result = 0;
        for(Function funct : operands){
            result += funct.f(x);
        }
        return result;
    }

    @Override
    public String getName() {
        return "Sum";
    }


    @Override
    public String toString() {
        String formula = "";
        boolean first = true;
        for(Function funct : operands){
            if(!first){
                formula += " + ";
            }
            else{
                first = false;
            }
            formula += funct;
        }
        return formula;
    }

    @Override
    public Function getDerivative() {
        Function[] derivatives = new Function[operands.length];
        for(int i=0; i<operands.length; ++i){
            derivatives[i] = operands[i].getDerivative();
            if(derivatives[i]==null){
                return null;
            }
        }
        return Sum.of(derivatives);
    }

    @Override
    public Function simplified(){
        ArrayList<Function> nonConstant = new ArrayList<Function>(operands.length);
        double constantes = 0;
        for(int i=0; i< operands.length; ++i){
            Function simplified = operands[i].simplified();
            if(simplified instanceof Constante){
                constantes += ((Constante)simplified).getValue();
            }
            else{
                nonConstant.add(simplified);
            }
        }

        if(constantes!=0){
            nonConstant.add(Constante.of(constantes));
        }

        return Sum.of(nonConstant);
    }
}
