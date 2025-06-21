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
