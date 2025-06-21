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
import java.util.logging.Logger;
/**
 *
 * @author Philippe Gauthier ISIR2020
 */

public class ArrayOfDoubleFilter {
    protected static final Logger LOGGER = Logger.getLogger(ArrayOfDoubleFilter.class.getName());

    protected LinkedList<Double>[] buffer; 
    protected int size, maxSizePerQueue;
    
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
    
    public double pushAndGetFiltered(int idx, double value){
        if(idx>=0 && idx<size){
            if(buffer[idx].size()>= maxSizePerQueue)
                buffer[idx].poll();
             buffer[idx].add(value);
        }
        return getFiltered(idx);
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

    public void clear() {
        for (int idx = 0; idx<size;idx++) {
            buffer[idx].clear();
        }
    }
}
