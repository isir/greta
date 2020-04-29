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
 *
 */
package greta.auxiliary.openface2.util;

import java.util.LinkedList;
import java.util.logging.Logger;
/**
 *
 * @author Philippe Gauthier ISIR2020
 */

public class ArrayOfDoubleFilter {
    protected static final Logger LOGGER = Logger.getLogger(ArrayOfDoubleFilter.class.getName());

    private LinkedList<Double>[] buffer; 
    private int size, maxSizePerQueue;
    
    public ArrayOfDoubleFilter(int size, int maxSizePerQueue){
        this.size = size;
        this.maxSizePerQueue = maxSizePerQueue;
        buffer = new LinkedList[size]; 
        for (int idx = 0; idx<size;idx++) {
            buffer[idx] = new LinkedList<>();
        }
    }
    
    public int getSize(){
        return size;
    }
    
    public int getMaxSizePerQueue(){
        return maxSizePerQueue;
    }
    
    public void setMaxSizePerQueue(int m){
        maxSizePerQueue = m;
        for (int idx = 0; idx<size;idx++) {
            buffer[idx].clear();
        }
        LOGGER.info(String.format("MaxSizePerQueue changed to: %d", m));
    }
    
    public void push(int idx, double value){
        if(idx>=0 && idx<size){
            if(buffer[idx].size()>= maxSizePerQueue)
                buffer[idx].poll();
             buffer[idx].add(value);
        }
    }
    
    public double getValueAt(int idx, int col){
        if(idx>=0 && idx<size){
            if(col>=0 && col< buffer[idx].size())
                return buffer[idx].get(col);
        }
        return 0.;
    }
    
    public int getBuffLengthAt(int idx){
        if(idx>=0 && idx<size){
            return buffer[idx].size();
        }
        return 0;
    }
    
    public double getFiltered(int idx){
        if(idx>=0 && idx<size){            
            LinkedList<Double> q = buffer[idx];
            double w = 1./q.size();
            double sum = 0.;
            
            for (int i=0;i<q.size();i++) {
                sum += q.get(i)*w;
            }
            return sum;
        }
        return -1;
    }
}
