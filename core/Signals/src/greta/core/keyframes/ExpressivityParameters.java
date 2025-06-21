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
package greta.core.keyframes;

/**
 *
 * @author Quoc Anh Le
 */
public class ExpressivityParameters {

    public ExpressivityParameters(){}

    public ExpressivityParameters(ExpressivityParameters p){
        intensity = p.intensity;
        spc = p.spc;
        tmp = p.tmp;
        fld = p.fld;
        pwr = p.pwr;
        tension = p.tension;
        stf = p.stf;
    }
    /**
     * the intensity value of this signal
     */
    protected double intensity = 1;
    /**
     * the spacial parameter value of this signal
     */
    public double spc = 0;
    /**
     * the temporal parameter value of this signal
     */
    public double tmp = 0;
    /**
     * the fluidity parameter value of this signal
     */
    public double fld = 0;
    /**
     * the power parameter value of this signal
     */
    public double pwr = 0;
    /**
     * the stiffness parameter value of this signal
     */
    public double stf = 0;
    /**
     * the tension parameter value of this signal
     */
    public double tension = 0; // Cath: this is used as Stiffness

    @Override
    public String toString() {
        return new String(
                " intensity:" + intensity
                + " spc:" + spc
                + " tmp:" + tmp
                + " fld:" + fld
                + " pwr:" + pwr
                + " tension:" + tension);
    }
}
