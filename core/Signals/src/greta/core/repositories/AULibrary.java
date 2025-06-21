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
package greta.core.repositories;

import greta.core.animation.mpeg4.fap.FAPType;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.parameter.ParameterSet;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class AULibrary extends ParameterSet<FLExpression> implements CharacterDependent {

    public static final String CHARACTER_PARAMETER_AULIBRARY;
    private static final String xsdFile;
    public static AULibrary global_aulibrary;

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
        //this.characterManager = characterManager;
    }

    static {
        CHARACTER_PARAMETER_AULIBRARY = "AULIBRARY";
        xsdFile = IniManager.getGlobals().getValueString("XSD_AULIBRARY");
        global_aulibrary = new AULibrary(CharacterManager.getStaticInstance());
    }

    public AULibrary(CharacterManager cm) {
        //get the default Lexicon :
        super();
        setCharacterManager(cm);
        setDefaultDefinition(cm.getDefaultValueString(CHARACTER_PARAMETER_AULIBRARY));
        //load additionnal Lexicon :
        for (String fileName : cm.getAllValuesString(CHARACTER_PARAMETER_AULIBRARY)) {
            addDefinition(fileName);
        }
        //set the current Lexicon to use :
        setDefinition(cm.getValueString(CHARACTER_PARAMETER_AULIBRARY));

        //to be notify when the character change : //To-do to remove depend on the tree
        //cm.add(this);
    }

    @Override
    protected List<FLExpression> load(String definition) {
        ArrayList<FLExpression> aulibrary = new ArrayList<FLExpression>();

        XMLParser parser = XML.createParser();
        //XMLTree facialexpressions = parser.parseFileWithXSD(definition,xsdFile);
        XMLTree facialexpressions = parser.parseFile(definition);

        if (facialexpressions != null) {
            XMLTree root = facialexpressions.getRootNode();
            for (XMLTree facialexpression : root.getChildrenElement()) {
                if (facialexpression.isNamed("actiondeclaration")) {
                    //create FLE
                    FLExpression flexpression = new FLExpression(facialexpression.getAttribute("name"));
                    //load
                    for (XMLTree signal : facialexpression.getChildrenElement()) {
                        if (signal.isNamed("fap")) {
                            int num = Integer.valueOf(signal.getAttribute("num"));
                            int value = Integer.valueOf(signal.getAttribute("value"));
                            flexpression.add(FAPType.get(num), value); //adds the signal and constructs combinations
                        }//end if
                    }//end for
                    aulibrary.add(flexpression);
                }//end if expression
                else { //not expression so what else??
                    continue;
                }
            }
        }//empty

        return aulibrary;
    }

    public FLExpression findExpression(String name) {
        FLExpression target = this.get(name);
//        if (target == null) {
//            Logs.info(this.getClass().getName()+ " : not found any expression for "+name);
//        }
        return target;
    }

    @Override
    public void onCharacterChanged() {
        setDefinition(getCharacterManager().getValueString(CHARACTER_PARAMETER_AULIBRARY));
    }

    @Override
    protected void save(String string, List<FLExpression> list) {
        XMLTree auLibrary = XML.createTree("facelibrary");
        Collections.sort(list, new Comparator<FLExpression>() {

            @Override
            public int compare(FLExpression o1, FLExpression o2) {
                try{
                    if(o1.getParamName().toUpperCase().startsWith("AU") && o2.getParamName().toUpperCase().startsWith("AU")){
                        int auNum1 = Integer.parseInt(o1.getParamName().substring(2));
                        int auNum2 = Integer.parseInt(o2.getParamName().substring(2));
                        return auNum1-auNum2;
                    }
                }
                catch(Throwable t){}
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getParamName(), o2.getParamName());
            }
        });
        for(FLExpression au : list){
            XMLTree declaration = auLibrary.createChild("actiondeclaration");
            declaration.setAttribute("name", au.getParamName());
            List<FLExpression.FAPItem> faps = au.getFAPs();
            Collections.sort(faps, new Comparator<FLExpression.FAPItem>() {

                @Override
                public int compare(FLExpression.FAPItem o1, FLExpression.FAPItem o2) {
                    return o1.type.ordinal() - o2.type.ordinal();
                }
            });
            for(FLExpression.FAPItem fap : au.getFAPs()){
                XMLTree fapTree = declaration.createChild("fap");
                fapTree.setAttribute("num", Integer.toString(fap.type.ordinal()));
                fapTree.setAttribute("value", Integer.toString(fap.value));
            }
        }
        auLibrary.save(string);
    }
}
