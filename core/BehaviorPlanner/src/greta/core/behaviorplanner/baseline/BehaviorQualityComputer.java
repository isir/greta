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

import greta.core.intentions.Intention;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.parameter.EngineParameter;
import greta.core.util.parameter.ParameterSet;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the object that is charged of performing the transformation of the
 * Baseline into the Dynamicline. The object is initialized by giving the path
 * of an xml file containing a set of qualifiers that act on the Baselines
 * depending of the different communicative acts. Once the object is
 * initialized, it is ready to perform the transformation of the Baseline into
 * the Dynamicline by calling the corresponding method.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because they are
 * UmlGraph tags, not javadoc tags.
 * @has - - * greta.core.behaviorplanner.baseline.Qualifier
 * @navassoc - "computes" - greta.core.behaviorplanner.baseline.DynamicLine
 */
public class BehaviorQualityComputer extends ParameterSet<Qualifier> implements CharacterDependent {

    public static final String CHARACTER_PARAMETER_QUALIFIERS = "QUALIFIERS";
    /**
     * the XML schema file to validate XML files
     */
    public static final String XSDFile = IniManager.getGlobals().getValueString("XSD_QUALIFIERS");

//constructors :
    /**
     * Construct a {@code BehaviorQualityComputer}.
     */
    public BehaviorQualityComputer(CharacterManager cm) {
        //get the default qualifiers :
        super();
        setCharacterManager(cm);
        setDefaultDefinition(cm.getDefaultValueString(CHARACTER_PARAMETER_QUALIFIERS));

        //load additionnal qualifiers :
        for (String fileName : cm.getAllValuesString(CHARACTER_PARAMETER_QUALIFIERS)) {
            addDefinition(fileName);
        }

        //set the current qulifiers to use :
        setDefinition(cm.getValueString(CHARACTER_PARAMETER_QUALIFIERS));
    }

    /**
     * Loads in memory a specific file containig qualifiers</br> This function
     * is call by the super class {@code ParameterSet}. You must never call it.
     * Use {@code addDefinition(String)} to only add in memory, or
     * {@code setDefinition(String)} to use this file (it will be added
     * automaticly if needed).
     *
     * @param definition the name of file to load
     * @return the list of the contained {@code Qualifiers}
     * @see
     * greta.core.util.parameter.ParameterSet#addDefinition(java.lang.String)
     * addDefinition(String)
     * @see
     * greta.core.util.parameter.ParameterSet#setDefinition(java.lang.String)
     * setDefinition(String)
     */
    @Override
    protected List<Qualifier> load(String definition) {
        ArrayList<Qualifier> qualifiers = new ArrayList<Qualifier>();

        XMLTree t = XML.createParser().parseFileWithXSD(definition, XSDFile);
        if (t == null) {
            return qualifiers;
        }

        //every child of t is a set of rules for an Intention.
        //in the xml file all the rules for an Intention are in the
        //same tag called "rule"
        //std::list<XMLGenericTree*>::iterator rule;
        //for(rule=(t->child).begin();rule!=(t->child).end();rule++)
        //{
        //(*rule) --> rule

        for (XMLTree rule : t.getChildrenElement()) {
            Qualifier qualifier = new Qualifier(rule.getAttribute("name"));

            //every child of the rule is a modulation on an attribute of the expressivity or
            //on the preference of a modality

            for (XMLTree op : rule.getChildrenElement()) {
                Modulation modulation = new Modulation();

                //the destination of the modulation must always be there
                XMLTree dest = op.findNodeCalled("destination");
                if (dest == null) {
                    continue;
                }
                dest = dest.findNodeCalled(XML.TEXT_NODE_NAME);
                modulation.setDestinationModality(removeSpacesAndNewlines(dest.getTextValue()));

                //and the attribute of the destination too
                XMLTree attr = op.findNodeCalled("parameter");
                attr = attr.findNodeCalled(XML.TEXT_NODE_NAME);
                modulation.setDestinationAttribute(removeSpacesAndNewlines(attr.getTextValue()));

                //the operator also must always be there
                XMLTree operat = op.findNodeCalled("operator");
                operat = operat.findNodeCalled(XML.TEXT_NODE_NAME);
                modulation.operator = removeSpacesAndNewlines(operat.getTextValue());

                //operand1 must to be there but can have different forms
                XMLTree oper1 = op.findNodeCalled("op1_name");
                //the attribute of operand1 can be there
                XMLTree oper1_attr = op.findNodeCalled("op1_attribute");
                if (oper1 != null && oper1_attr != null) {
                    oper1 = oper1.findNodeCalled(XML.TEXT_NODE_NAME);
                    oper1_attr = oper1_attr.findNodeCalled(XML.TEXT_NODE_NAME);
                    modulation.setOperand1(removeSpacesAndNewlines(oper1.getTextValue() + "." + oper1_attr.getTextValue()));
                } else {
                    oper1 = op.findNodeCalled("op1_value");
                    if (oper1 == null) {
                        continue;
                    }
                    oper1 = oper1.findNodeCalled(XML.TEXT_NODE_NAME);
                    modulation.setOperand1(removeSpacesAndNewlines(oper1.getTextValue()));
                }

                //operand2 is optional
                XMLTree oper2 = op.findNodeCalled("op2_name");
                XMLTree oper2_attr = op.findNodeCalled("op2_attribute");
                if (oper2 != null && oper2_attr != null) {
                    oper2 = oper2.findNodeCalled(XML.TEXT_NODE_NAME);
                    oper2_attr = oper2_attr.findNodeCalled(XML.TEXT_NODE_NAME);
                    modulation.setOperand2(removeSpacesAndNewlines(oper2.getTextValue() + "." + oper2_attr.getTextValue()));
                } else {
                    oper2 = op.findNodeCalled("op2_value");
                    if (oper2 != null) {
                        oper2 = oper2.findNodeCalled(XML.TEXT_NODE_NAME);
                        modulation.setOperand2(removeSpacesAndNewlines(oper2.getTextValue()));
                    }
                }

                //operand3 is optional and anyway it can be only a value
                XMLTree oper3 = op.findNodeCalled("op3_value");
                if (oper3 != null) {
                    oper3 = oper3.findNodeCalled(XML.TEXT_NODE_NAME);
                    modulation.setOperand3(removeSpacesAndNewlines(oper3.getTextValue()));
                }

                qualifier.addModulation(modulation);
            }
            qualifiers.add(qualifier);
        }
        return qualifiers;
    }

//public methods :
    /**
     * Computes the Dynamicline.<br/> The {@code DynamicLine} will be obtained
     * by applying the qualifiers corresponding to the actual {@code Intention}
     * to the {@code BaseLine}.
     *
     * @param ca the {@code Intention} that is used to alter the base line and
     * obtain the dynamic line
     * @param bl the input base line
     */
    public DynamicLine computeDynamicline(Intention ca, BaseLine bl) {
        DynamicLine dl = new DynamicLine(bl);
        return computeDynamicline(ca, dl);
    }

    /**
     * Computes the Dynamicline.<br/> The {@code DynamicLine} will be obtained
     * by applying the qualifiers corresponding to the actual {@code Intention}
     * to the {@code DynamicLine}.
     *
     * @param ca the {@code Intention} that is used to alter the dynamic line
     * @param dl the input dynamic line
     */
    public DynamicLine computeDynamicline(Intention ca, DynamicLine dl) {

        Qualifier qual = fromIntentionToQualifier(ca);

        if (qual != null) {
            // qual.getModulations().keySet()
            for (String modality : qual.getModulations().keySet()) {
                for (Modulation modulation : qual.getModulation(modality)) {
                    EngineParameterAdaptor destination = getAdaptor(dl, modality, modulation.getDestinationAttribute());
                    if (destination != null) {
                        //Operand 1 :
                        double op1 = 0;
                        if (modulation.operand1IsNumber()) {
                            op1 = modulation.getValueOfOperand1();
                        } else {
                            EngineParameterAdaptor targetOp1 = getAdaptor(dl, modulation.getOperand1Modality(), modulation.getOperand1Attribute());
                            if (targetOp1 != null) {
                                op1 = targetOp1.getValue();
                            }
                        }

                        //Operand 2 :
                        double op2;
                        if (modulation.operand2IsNumber()) {
                            op2 = modulation.getValueOfOperand2();
                        } else {
                            EngineParameterAdaptor targetOp2 = getAdaptor(dl, modulation.getOperand2Modality(), modulation.getOperand2Attribute());
                            if (targetOp2 != null) {
                                op2 = targetOp2.getValue();
                            } else {
                                op2 = ca.getImportance();
                            }
                        }

                        //Operand 3 :
                        double op3 = 0;
                        if (modulation.operand3IsNumber()) {
                            op3 = modulation.getValueOfOperand3();
                        } else {
                            EngineParameterAdaptor targetOp3 = getAdaptor(dl, modulation.getOperand3Modality(), modulation.getOperand3Attribute());
                            if (targetOp3 != null) {
                                op3 = targetOp3.getValue();
                            }
                        }

                        destination.setValue(computeOperation(destination.getValue(), modulation.operator, op1, op2, op3));
                    }
                }
            }
        }
        return dl;
    }

//private methods :
    /**
     * A function for eliminating spaces and newlines from a string.
     *
     * @param s the String to alter
     * @return the altered String
     */
    private static String removeSpacesAndNewlines(String s) {
        s = s.replaceAll("\n", "");
        s = s.replaceAll("\r", "");
        s = s.replaceAll("\t", "");
        s = s.replaceAll(" ", "");
        return s;
    }

    /**
     * It performs a mathematical operation on the given operands and returns
     * the result of the base parameter.
     *
     * @param base The result of the operation will be stored here
     * @param operat It is the name of the operation to be performed
     * @param op1 the value of the first operand involved in the operation
     * @param op1 the value of the second operand involved in the operation
     * @param op3 the value of the third operand involved in the operation
     */
    private double computeOperation(double base, String operat, double op1, double op2, double op3) {
        if (operat.equalsIgnoreCase("VAL")) {
            return op1;
        }
        if (operat.equalsIgnoreCase("ADD")) {
            return op1 + op2;
        }
        if (operat.equalsIgnoreCase("SUB")) {
            return op1 - op2;
        }
        if (operat.equalsIgnoreCase("MUL")) {
            return op1 * op2;
        }
        if (operat.equalsIgnoreCase("DIV")) {
            return op1 / op2;
        }
        if (operat.equalsIgnoreCase("REL")) {
            return op1 + (op2 - op1) * op3;
        }
        if (operat.equalsIgnoreCase("LIM")) {
            return limit(base, op1, op2);
        }
        if (operat.equalsIgnoreCase("INTENSIFY")) {
            return intensity(op1, op2);
        }
        return base;
    }

    /**
     * Function that limits the value of the first parameter by the values of
     * the other two.
     *
     * @param val The value to be limited
     * @param min,max the lower and upper limits to apply to the first parameter
     */
    private double limit(double val, double min, double max) {
        if (val > max) {
            return max;
        }
        if (val < min) {
            return min;
        }
        return val;
    }

    /**
     * Function that modify the intensity of the parameter.
     *
     * @param par parameter to intensify
     * @param intensity the intensity to apply to the parameter
     */
    private double intensity(double par, double intensity) {
        return par * (intensity * 2.0);
    }

    /**
     * It returns the behavior qualifier corresponding to the given
     * communicative intention.
     *
     * @param intention the {@code Intention} to refer
     * @return the corresponding {@code Qalifier}
     */
    private Qualifier fromIntentionToQualifier(Intention intention) {
        String lookfor;

        lookfor = intention.getName() + "-" + intention.getType();

        Qualifier targetQualifier = this.get(lookfor);

        if (targetQualifier == null) {
            //can not be found, we search other :
            lookfor = intention.getName() + "-*";
            targetQualifier = this.get(lookfor);
            // if(targetQualifier == null){
            //    Logs.debug(this.getClass().getName()+ " : not found any qualifier for "+intention.getName()+"-"+intention.getType());
            // }
        }
        return targetQualifier;
    }

    @Override
    public void onCharacterChanged() {
        setDefinition(getCharacterManager().getValueString(CHARACTER_PARAMETER_QUALIFIERS));
    }

    private static EngineParameterAdaptor getAdaptor(DynamicLine dl, String modality, String paramNameExtended) {
        String[] paramNameExtendedSplited = paramNameExtended.split("\\.");
        String paramName = paramNameExtendedSplited[0];
        EngineParameterAdaptor epa = null;
        if (paramNameExtendedSplited.length > 1) {
            if (paramNameExtendedSplited[1].equalsIgnoreCase("value")) {
                epa = new EngineParameterAdaptorValue();
            } else {
                if (paramNameExtendedSplited[1].equalsIgnoreCase("max")) {
                    epa = new EngineParameterAdaptorMax();
                } else {
                    if (paramNameExtendedSplited[1].equalsIgnoreCase("min")) {
                        epa = new EngineParameterAdaptorMin();
                    }
                }
            }
        }
        if (epa == null) {
            return null;
        }
        epa.ep = dl.getParameter(modality, paramName);
        return epa.ep == null ? null : epa;
    }

    @Override
    protected void save(String definition, List<Qualifier> list) {
        XMLTree qualifiersTree = XML.createTree("qualifiers");
        qualifiersTree.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        qualifiersTree.setAttribute("xsi:noNamespaceSchemaLocation", "../../Common/Data/xml/qualifiers.xsd");//TODO: put the relatve path between XSDFile and taget file

        for(Qualifier qualifier : list){
            XMLTree qualifierTree = qualifiersTree.createChild("qualifier");
            qualifierTree.setAttribute("name", qualifier.getParamName());
            for(List<Modulation> modulations : qualifier.getModulations().values()){
                for(Modulation modulation : modulations){
                    XMLTree modulationTree = qualifierTree.createChild("modulation");
                    modulationTree.createChild("destination").addText(modulation.getDestinationModality());
                    modulationTree.createChild("parameter").addText(modulation.getDestinationAttribute());
                    modulationTree.createChild("operator").addText(modulation.operator);
                    if(modulation.operand1IsNumber()){
                        modulationTree.createChild("op1_value").addText(Double.toString(modulation.getValueOfOperand1()));
                    }
                    else{
                        modulationTree.createChild("op1_name").addText(modulation.getOperand1Modality());
                        modulationTree.createChild("op1_attribute").addText(modulation.getOperand1Attribute());
                    }
                    if(modulation.operand2IsNumber()){
                        modulationTree.createChild("op2_value").addText(Double.toString(modulation.getValueOfOperand2()));
                    }
                    else{
                        if( ! modulation.getOperand2Modality().isEmpty()){
                            modulationTree.createChild("op2_name").addText(modulation.getOperand2Modality());
                            modulationTree.createChild("op2_attribute").addText(modulation.getOperand2Attribute());
                        }
                    }
                    if(modulation.operand3IsNumber()){
                        modulationTree.createChild("op3_value").addText(Double.toString(modulation.getValueOfOperand3()));
                    }
                }
            }
        }
        qualifiersTree.save(definition);
    }

    private static abstract class EngineParameterAdaptor {

        protected EngineParameter ep;

        public abstract double getValue();

        public abstract void setValue(double value);
    }

    private static class EngineParameterAdaptorValue extends EngineParameterAdaptor {

        @Override
        public double getValue() {
            return ep.getValue();
        }

        @Override
        public void setValue(double value) {
            ep.setValue(value);
        }
    }

    private static class EngineParameterAdaptorMax extends EngineParameterAdaptor {

        @Override
        public double getValue() {
            return ep.getMax();
        }

        @Override
        public void setValue(double value) {
            ep.setMax(value);
        }
    }

    private static class EngineParameterAdaptorMin extends EngineParameterAdaptor {

        @Override
        public double getValue() {
            return ep.getMin();
        }

        @Override
        public void setValue(double value) {
            ep.setMin(value);
        }
    }

    private CharacterManager characterManager;

    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        if(characterManager==null)
            characterManager = CharacterManager.getStaticInstance();
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        if(this.characterManager!=null)
            this.characterManager.remove(this);
        this.characterManager = characterManager;
        characterManager.add(this);
    }
}
