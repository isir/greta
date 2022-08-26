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
