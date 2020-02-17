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
package greta.core.behaviorplanner.baseline;

/**
 *  Contains informations on a Modulation.</br>
 *  A Modulation is a mathemetical function applied on some numerical values and/or
 *  attributes of the communicative modalities and its result is stored in an attribute
 *  of a communicative modality. The terms of the Modulation (called operands) can
 *  be 1, 2 or 3 depending on the {@code operator}.
 *  @author Andre-Marie Pez
 */
public class Modulation {
    /**
     * Class contructor.
     *
     */
    public Modulation(){
        destModality = "";
        destAttribute = "";
        operator = "";
        operand1_modality = "";
        operand1_attribute = "";
        operand1_isNumber = false;
        operand1_value = 0;
        operand2_modality = "";
        operand2_attribute = "";
        operand2_isNumber = false;
        operand2_value = 0;
        operand3_modality = "";
        operand3_attribute = "";
        operand3_isNumber = false;
        operand3_value = 0;
    }

// Destination :
    private String destModality;
    private String destAttribute;

    /**
     * Sets the attribute in which the result of the Modulation is stored.
     * The format of the string is modality.attribute. For example
     * to refer to the Spatial value of the face modality: face.SPC.
     * Allowed modalities and attributes are listed in the file global.xsd.
     * @param destination the {@code String} of the destination
     */
    public void setDestination(String destination){
        int dotPos = destination.indexOf(".");
        if(dotPos>-1){
            destModality = destination.substring(0, dotPos);
            destAttribute = destination.substring(dotPos+1);
        }
    }

    /**
     * Returns the name of the target modality.
     * @return the name of the target modality
     */
    public String getDestinationModality(){
        return destModality;
    }

    /**
     * Returns the name of the target attribute.
     * @return the name of the target attribute
     */
    public String getDestinationAttribute(){
        return destAttribute;
    }

    /**
     * Sets the name of the target modality.
     * @param modality the name of the target modality
     */
    public void setDestinationModality(String modality){
        destModality = modality;
    }

    /**
     * Sets the name of the target attribute.
     * @param attribute the name of the target attribute
     */
    public void setDestinationAttribute(String attribute){
        destAttribute = attribute;
    }

// Operator :
    /**
     * The operator that will be used in the Modulation.
     * Allowed operators are listed in the file global.xsd.
     */
    public String operator;

// Operands :
// 1st
    private String  operand1_modality;
    private String  operand1_attribute;
    private boolean operand1_isNumber;
    private double  operand1_value;
    /**
     * Sets the first operand of this Modulation.<br/>
     * The format of the string is modality.attribute or a float value.
     * @param operand the {@code String} of the operand
     */
    public void setOperand1(String operand){
        try{
            operand1_value = Double.parseDouble(operand);
            operand1_isNumber = true;
        }
        catch(Exception e){
            int dotPos = operand.indexOf(".");
            if(dotPos>-1){
                operand1_modality = operand.substring(0, dotPos);
                operand1_attribute = operand.substring(dotPos+1);
            }
            operand1_isNumber = false;
        }
    }
    /**
     * Returns the float value of the first operand.
     * @return the float value of the first operand
     */
    public double getValueOfOperand1(){
        return operand1_value;
    }
    /**
     * Tells if the fist operand is a flaot value or describes a target parameter.
     * @return {@code true} if the first operand is a float value
     */
    public boolean operand1IsNumber(){
        return operand1_isNumber;
    }
    /**
     * Returns the name of the modality of the first operand.
     * @return the name of the modality of the first operand
     */
    public String getOperand1Modality(){
        return operand1_modality;
    }
    /**
     * Returns the name of the attribute of the first operand.
     * @return the name of the attribute of the first operand
     */
    public String getOperand1Attribute(){
        return operand1_attribute;
    }


// 2nd
    private String  operand2_modality;
    private String  operand2_attribute;
    private boolean operand2_isNumber;
    private double  operand2_value;
    /**
     * Sets the second operand of this Modulation.<br/>
     * The format of the string is modality.attribute or a float value.
     * @param operand the {@code String} of the operand
     */
    public void setOperand2(String operand){
        try{
            operand2_value = Double.parseDouble(operand);
            operand2_isNumber = true;
        }
        catch(Exception e){
            int dotPos = operand.indexOf(".");
            if(dotPos>-1){
                operand2_modality = operand.substring(0, dotPos);
                operand2_attribute = operand.substring(dotPos+1);
            }
            operand2_isNumber = false;
        }
    }
    /**
     * Returns the float value of the second operand.
     * @return the float value of the second operand
     */
    public double getValueOfOperand2(){
        return operand2_value;
    }
    /**
     * Tells if the second operand is a flaot value or describes a target parameter.
     * @return {@code true} if the second operand is a float value
     */
    public boolean operand2IsNumber(){
        return operand2_isNumber;
    }
    /**
     * Returns the name of the modality of the second operand.
     * @return the name of the modality of the second operand
     */
    public String getOperand2Modality(){
        return operand2_modality;
    }
    /**
     * Returns the name of the attribute of the second operand.
     * @return the name of the attribute of the second operand
     */
    public String getOperand2Attribute(){
        return operand2_attribute;
    }


// 3rd
    private String  operand3_modality;
    private String  operand3_attribute;
    private boolean operand3_isNumber;
    private double  operand3_value;
    /**
     * Sets the third operand of this Modulation.<br/>
     * The format of the string is modality.attribute or a float value.
     * @param operand the {@code String} of the operand
     */
    public void setOperand3(String operand){
        try{
            operand3_value = Double.parseDouble(operand);
            operand3_isNumber = true;
        }
        catch(Exception e){
            int dotPos = operand.indexOf(".");
            if(dotPos>-1){
                operand3_modality = operand.substring(0, dotPos);
                operand3_attribute = operand.substring(dotPos+1);
            }
            operand3_isNumber = false;
        }
    }
    /**
     * Returns the float value of the third operand.
     * @return the float value of the third operand
     */
    public double getValueOfOperand3(){
        return operand3_value;
    }
    /**
     * Tells if the third operand is a flaot value or describes a target parameter.
     * @return {@code true} if the third operand is a float value
     */
    public boolean operand3IsNumber(){
        return operand3_isNumber;
    }
    /**
     * Returns the name of the modality of the third operand.
     * @return the name of the modality of the third operand
     */
    public String getOperand3Modality(){
        return operand3_modality;
    }
    /**
     * Returns the name of the attribute of the third operand.
     * @return the name of the attribute of the third operand
     */
    public String getOperand3Attribute(){
        return operand3_attribute;
    }
}
