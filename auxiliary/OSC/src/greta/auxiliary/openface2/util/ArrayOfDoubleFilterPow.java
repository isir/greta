/*
 * This file is part of the auxiliaries of Greta.
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
*/
package greta.auxiliary.openface2.util;

import java.util.LinkedList;

/**
 * The idea is to use the pow function between 0-1 to get differents weights 
 * along the queue size of the filter.
 * The most recent value will have more weight than the previous one
 * 
 * @author Philippe Gauthier ISIR2020
 */


public class ArrayOfDoubleFilterPow extends ArrayOfDoubleFilter {
    private double pow;
    
    public ArrayOfDoubleFilterPow(int size, int maxSizePerQueue, double pow) {
        super(size, maxSizePerQueue);
        this.pow = pow;
        
    }
    
    @Override
    public double getFiltered(int idx){
        if(idx>=0 && idx<size){            
            LinkedList<Double> q = buffer[idx];            
            double sum = 0.;
            double sumWeight = 0.;
            for (int i=0;i<q.size();i++) {
                double w = Math.pow(((double)i+1.)/q.size(), getPow());
                sumWeight += w;
                sum += q.get(i)*w;
            }
            return sum/sumWeight;
        }
        return -1;
    }

    /**
     * @return the pow
     */
    public double getPow() {
        return pow;
    }

    /**
     * @param pow the pow to set
     */
    public void setPow(double pow) {
        this.pow = pow;
    }
}
