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
package greta.core.signals;

/**
 * This abstract Signal contains informations about expressivity.<br/>
 * It also contains the reference and submodality of the Signal.<br/>
 * The reference should be used to know which is the shape to use (i.e. for the animation).<br/>
 * The submodality may distinguish different parts of a same modality.
 * For example, in head modality, submodality can be movement or direction. And both can be displayed simultaneously.<br/>
 * It is supposed that an empty submodality means that the modality is fully used.
 * @author Andre-Marie Pez
 * @author Quoc Anh Le
 */
public abstract class ParametricSignal implements Signal{

    /** the category of this signal (gesture: Iconic, Beat,.., face: sadness, joy,.. */
    private String category;
    /** the reference of this signal */
    private String reference = "";
    /** the intensity value of this signal */
    private double intensity = 1;
    /** the spacial parameter value of this signal */
    private double spc = 0;
    /** the temporal parameter value of this signal */
    private double tmp = 0;
    /** the fluidity parameter value of this signal */
    private double fld = 0;
    /** the power parameter value of this signal */
    private double pwr = 0;
    /** the repetition parameter value of this signal
     *
     */
    private double rep = 0;
    /** the submodality of this signal */
    private String submodality = "";

    /** the openness parameter value of this signal */
    private double openness = 0;

    /** the tension parameter value of this signal */
    private double tension = 0;

    private boolean filled = false;

    public void setOpenness(double openness) {
        this.openness = openness;
    }

    public void setTension(double tension) {
        this.tension = tension;
    }

    /**
     * Sets the category of this signal.
     * The category can be sadness or joy to a face signal.
     * or can be beat, iconic to a gesture signal
     * @param category the category to set
     */
    public void setCategory(String category){
        this.category = category;
    }

    /**
     * Sets the reference of this signal.
     * @param reference the reference to set
     */
    public void setReference(String reference){
        this.reference = reference;
    }

    /**
     * Sets the intensity value of this signal.
     * @param intensity the intensity value to set
     */
    public void setIntensity(double intensity){
        this.intensity = intensity;
    }

    /**
     * Sets the spacial parameter value of this signal.
     * @param spc the spacial parameter value to set
     */
    public void setSPC(double spc){
        this.spc = spc;
    }

    /**
     * Sets the temporal parameter value of this signal.
     * @param tmp the temporal parameter value to set
     */
    public void setTMP(double tmp){
        this.tmp = tmp;
    }

    /**
     * Sets the fluidity parameter value of this signal.
     * @param fld the fluidity parameter value to set
     */
    public void setFLD(double fld){
        this.fld = fld;
    }

    /**
     * Sets the power parameter value of this signal.
     * @param pwr the power parameter value to set
     */
    public void setPWR(double pwr){
        this.pwr = pwr;
    }

    /**
     * Sets the repetition parameter value of this signal.
     * @param rep the repetition parameter value to set
     * this parameter is used in the planner => no, load signal repositories too
     */
    public void setREP(double rep){
        this.rep = rep;
    }

    /**
     * Sets the submodality of this signal.<br/>
     * The submodality can be mouth or eyebrows to a face signal.
     * Or, movement or direction to a head signal, etc.
     * @param submodality the submodality to set
     */
    public void setSubmodality(String submodality){
        this.submodality = submodality;
    }

    /**
     * Returns the category of this signal.
     * @return the category of this signal
     */
    public String getCategory(){
        return category;
    }

    /**
     * Returns the reference of this signal.
     * @return the reference of this signal
     */
    public String getReference(){
        return reference;
    }

    /**
     * Returns the intensity value of this signal.
     * @return the intensity value of this signal
     */
    public double getIntensity(){
        return intensity;
    }

    /**
     * Returns the spacial parameter value of this signal.
     * @return the spacial parameter value of this signal
     */
    public double getSPC(){
        return spc;
    }

    /**
     * Returns the temporal parameter value of this signal.
     * @return the temporal parameter value of this signal
     */
    public double getTMP(){
        return tmp;
    }

    /**
     * Returns the fluidity parameter value of this signal.
     * @return the fluidity parameter value of this signal
     */
    public double getFLD(){
        return fld;
    }

    /**
     * Returns the power parameter value of this signal.
     * @return the power parameter value of this signal
     */
    public double getPWR(){
        return pwr;
    }

    /**
     * Returns the repetition parameter value of this signal.
     * @return the repetition parameter value of this signal
     * @deprecated this parameter is used in the planner
     */
    public double getREP(){
        return rep;
    }

    /**
     * Returns the submodality of this signal.<br/>
     * The submodality can be mouth or eyebrows to a face signal.
     * Or, movement or direction to a head signal, etc.
     * @return the submodality of this signal
     */
    public String getSubmodality(){
        return submodality;
    }

    public double getOpenness() {
        return openness;
    }

    public double getTension() {
        return tension;
    }

    public void setFilled(boolean b) {
        filled = b;
    }

    public boolean isFilled() {
        return filled;
    }
}
