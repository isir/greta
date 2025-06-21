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
 * contains some usfull functions
 * @author Andre-Marie Pez
 */
public class Functions {

    /**
     * Convert x from the interval [a b] to x' in the interval [c d]
     * @param x original value
     * @param a inferior bound of the original interval
     * @param b supperior bound of the original interval
     * @param c inferior bound of the new interval
     * @param d supperior bound of the new interval
     * @return the new value x'
     */
    public static double changeInterval(double x, double a, double b, double c, double d){
        return a==b ? c : (x-a)/(b-a) * (d-c) + c;
    }
}
