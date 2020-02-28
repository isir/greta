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
package greta.core.behaviorplanner.lexicon;

import greta.core.behaviorplanner.mseconstraints.MultimodalEmotionConstraintSet;
import greta.core.intentions.Intention;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.log.Logs;
import greta.core.util.parameter.ParameterSet;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class maps {@code BehaviorSets} with intentions.<br/>
 * It can contains more definitions of the behavior sets depending to the character.
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @has - - * greta.core.behaviorplanner.lexicon.BehaviorSet
 */
public class Lexicon extends ParameterSet<BehaviorSet> implements CharacterDependent{
    public static final String CHARACTER_PARAMETER_INTENTION_LEXICON = "LEXICON";
    private static final String xsdFile = IniManager.getGlobals().getValueString("XSD_BEHAVIORSETS");

    public Lexicon(CharacterManager cm){
        //get the default Lexicon :
        super();
        setCharacterManager(cm);
        setDefaultDefinition(cm.getDefaultValueString(CHARACTER_PARAMETER_INTENTION_LEXICON));

        //load additionnal Lexicon :
        for(String fileName : cm.getAllValuesString(CHARACTER_PARAMETER_INTENTION_LEXICON)) {
            addDefinition(fileName);
        }

        //set the current Lexicon to use :
        setDefinition(getCharacterManager().getValueString(CHARACTER_PARAMETER_INTENTION_LEXICON));
    }

    @Override
    protected List<BehaviorSet> load(String definition) {
        ArrayList<BehaviorSet> lexicon = new ArrayList<BehaviorSet>();

        XMLParser parser = XML.createParser();
        XMLTree behaviorsets = parser.parseFileWithXSD(definition,xsdFile);

        if(behaviorsets != null){
            for(XMLTree behaviorset : behaviorsets.getChildrenElement()){
                if(behaviorset.isNamed("behaviorset")){
                    //create BS with name and eventuallt type if it has one
                    BehaviorSet set = new BehaviorSet(behaviorset.getAttribute("name"), behaviorset.hasAttribute("type")? behaviorset.getAttribute("type"):"simple");
                    //load signals and theire alternative
                    XMLTree signals = behaviorset.findNodeCalled("signals");
                    if(signals!=null){
                        for(XMLTree signal : signals.getChildrenElement()){
                            if(signal.isNamed("signal")){

                                Shape shape = new Shape(signal.getAttribute("name"),
                                        signal.hasAttribute("min")? signal.getAttributeNumber("min"):0,
                                        signal.hasAttribute("max")? signal.getAttributeNumber("max"):Double.POSITIVE_INFINITY,
                                        //Info speech modality
                                        signal.hasAttribute("content")? signal.getAttribute("content"):"",
                                        signal.hasAttribute("intonation")? signal.getAttribute("intonation"):"",
                                        signal.hasAttribute("voicequality")? signal.getAttribute("voicequality"):"",
                                        signal.hasAttribute("meaning")? signal.getAttribute("meaning"):""
                                        );

                                //add some additional fields for mse algoritm:
                                if (set.getType().equalsIgnoreCase("mse")) {
                                    //if does not have a value defined take 1
                                    //send warning - no values defined?
                                    if(signal.hasAttribute("probability_start")) {
                                        shape.setProbability_Start(signal.getAttributeNumber("probability_start"));
                                    }
                                    else{
                                        shape.setProbability_Start(1);
                                        Logs.warning("Some parameters in behavior set:" + set.getParamName() + " do not have values:" + "probability_start");
                                    }

                                    if(signal.hasAttribute("probability_end")) {
                                        shape.setProbability_Stop(signal.getAttributeNumber("probability_end"));
                                    }
                                    else{
                                        shape.setProbability_Stop(1);
                                        Logs.warning("Some parameters in behavior set:" + set.getParamName() + " do not have values:" + "probability_end");
                                    }

                                    if(signal.hasAttribute("repetitivity")) {
                                        shape.setRepetivity((int)signal.getAttributeNumber("repetitivity"));
                                    }
                                    else{
                                        shape.setRepetivity(0);
                                        Logs.warning("Some parameters in behavior set:" + set.getParamName() + " do not have values:" + "repetitivity");
                                    }
                                }

                                SignalItem s = new SignalItem(signal.getAttribute("id"),signal.getAttribute("modality"),shape);
                                for(XMLTree alternative : signal.getChildrenElement()){
                                    if(alternative.isNamed("alternative")){
                                        Shape alternateShape = new Shape(alternative.getAttribute("name"),
                                        alternative.hasAttribute("min")? alternative.getAttributeNumber("min"):0,
                                        alternative.hasAttribute("max")? alternative.getAttributeNumber("max"):Double.POSITIVE_INFINITY,
                                        //Info speech modality
                                        alternative.hasAttribute("content")? alternative.getAttribute("content"):"",
                                        alternative.hasAttribute("intonation")? alternative.getAttribute("intonation"):"",
                                        alternative.hasAttribute("voicequality")? alternative.getAttribute("voicequality"):"",
                                        alternative.hasAttribute("meaning")? alternative.getAttribute("meaning"):""
                                        );
                                        s.addAlternative(alternateShape, alternative.getAttributeNumber("probability"));
                                    }
                                }
                                set.add(s); //adds the signal and constructs combinations
                            }
                        }
                    }
                    else{
                        continue;
                    }

                    //load contraints
                    boolean constraintApplied = false;
                    XMLTree constraints = behaviorset.findNodeCalled("constraints");
                    if(constraints!=null){
                        //load core constaints
                        XMLTree core = constraints.findNodeCalled("core");
                        if(core!=null){
                            for(XMLTree item : core.getChildrenElement()){
                                if(item.isNamed("item")){
                                    constraintApplied = set.mustPresent(item.getAttribute("id")) || constraintApplied;
                                }
                            }
                        }
                        //load implications
                        XMLTree rules = constraints.findNodeCalled("rules");
                        if(rules!=null){
                            for(XMLTree implication : rules.getChildrenElement()){
                                if(implication.isNamed("implication")){
                                    XMLTree ifpresent = implication.findNodeCalled("ifpresent");
                                    if(ifpresent!=null){
                                        String idPresent = ifpresent.getAttribute("id");
                                        for(XMLTree then : implication.getChildrenElement()){
                                            if(then.isNamed("thenpresent")){
                                                constraintApplied = set.ifPresentThenPresent(idPresent, then.getAttribute("id")) || constraintApplied;
                                            }
                                            else{
                                                if(then.isNamed("thennotpresent")) {
                                                    constraintApplied = set.ifPresentThenNotPresent(idPresent, then.getAttribute("id")) || constraintApplied;
                                                }
                                            }
                                        }
                                    }
                                    else{
                                        continue;
                                    }
                                }
                            }

                           //TODO: find msecons - change XSD before do it
                        }
                    }
                    if(constraintApplied) {
                        set.cleanNullCombination();
                    }
                    lexicon.add(set);
                }
            }
        }

        // Parse MSE contraints and add to BS
        // To DO: 1. copy constraints to one file! 2. change XSD 3. cancel this part
        greta.core.behaviorplanner.mseconstraints.ConstraintSetsContainer csc = new greta.core.behaviorplanner.mseconstraints.ConstraintSetsContainer();
        HashMap<String,MultimodalEmotionConstraintSet> hashmap1 = csc.initiation();

        //"Enumerate the HashMap"
        for (Map.Entry<String,MultimodalEmotionConstraintSet> entry : hashmap1.entrySet()){
            MultimodalEmotionConstraintSet mec = entry.getValue();
            String lookfor = "emotion-"+mec.getName();

            //find corresponding behavior set
            for (BehaviorSet set : lexicon){
                if ((set.getParamName().equalsIgnoreCase("mse:"+lookfor))&&(set.getType().equalsIgnoreCase("mse"))){
                    set.setMSEConstraints (mec.getNewConstraints());
                }
            }
        }

        return lexicon;
    }

    /**
     * It returns the behavior set corresponding to the given communicative intention.
     * @param intention the {@code Intention} to refer
     * @return the corresponding {@code BehaviorSet}
     */
    public BehaviorSet fromIntentionToBehaviorSet(Intention intention, String type){

        String lookfor;

        lookfor=intention.getName()+"-"+intention.getType();

        BehaviorSet targetBehaviorSet = this.get(type+":"+lookfor);

        if(targetBehaviorSet == null){
            //can not be found, we search other :
            lookfor=intention.getName()+"-*";
            targetBehaviorSet = this.get(type+":"+lookfor);
            if(targetBehaviorSet == null){
                Logs.warning(this.getClass().getName()+ " : not found any behavior set for "+intention.getName()+"-"+intention.getType());
            }
        }
	return targetBehaviorSet;
    }


    @Override
    public void onCharacterChanged() {
        setDefinition(getCharacterManager().getValueString(CHARACTER_PARAMETER_INTENTION_LEXICON));
    }

    @Override
    protected void save(String string, List<BehaviorSet> list) {
        //TODO but how ?
        Logs.warning("Impossible to save the Lexicon. \"core\" and \"implications\" are lost, they were only used during initialization.");
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
