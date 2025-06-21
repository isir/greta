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

import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.math.Vec3d;
import greta.core.util.parameter.ParameterSet;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class HandShapeLibrary extends ParameterSet<HandShape> implements CharacterDependent{

    public static final String CHARACTER_PARAMETER_HAND_SHAPE_LIBRARY = "HANDSHAPE_REPOSITORY";
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

    public HandShapeLibrary() {
        //get the default library :
        super();
        setDefaultDefinition(getCharacterManager().getDefaultValueString(CHARACTER_PARAMETER_HAND_SHAPE_LIBRARY));

        //load additionnal library :
        for(String fileName : getCharacterManager().getAllValuesString(CHARACTER_PARAMETER_HAND_SHAPE_LIBRARY)) {
            addDefinition(fileName);
        }

        //set the current library to use :
        setDefinition(getCharacterManager().getValueString(CHARACTER_PARAMETER_HAND_SHAPE_LIBRARY));
    }


    @Override
    protected List<HandShape> load(String string) {
        ArrayList<HandShape> shapes = new ArrayList<HandShape>(0);
        XMLParser parser = XML.createParser();
        parser.setValidating(false);
        XMLTree handShapes = parser.parseFile(string);
        if(handShapes!=null && handShapes.isNamed("HandShapes")){
            List<XMLTree> handShapeList = handShapes.getChildrenElement();
            shapes.ensureCapacity(handShapeList.size());
            for(XMLTree handShape : handShapeList){
                if(handShape.isNamed("HandShape")){
                    HandShape shape = new HandShape(handShape.getAttribute("id"));
                    for(XMLTree joint : handShape.getChildrenElement()){
                        shape.setJoint(
                                joint.getName(),
                                joint.getAttributeNumber("x"),
                                joint.getAttributeNumber("y"),
                                joint.getAttributeNumber("z"));
                    }
                    shapes.add(shape);
                }
            }
        }
        return shapes;
    }

    @Override
    public void onCharacterChanged() {
        setDefinition(getCharacterManager().getValueString(CHARACTER_PARAMETER_HAND_SHAPE_LIBRARY));
    }

    private XMLTree toXML(Collection<HandShape> handShapes){
        XMLTree handShapesXML = XML.createTree("HandShapes");
        for(HandShape handShape : handShapes){
            XMLTree handShapeXML = handShapesXML.createChild("HandShape");
            handShapeXML.setAttribute("id", handShape.getParamName());
            for(String jointName : handShape.getJointNames()){
                XMLTree jointXML = handShapeXML.createChild(jointName);
                Vec3d jointValues = handShape.getJoint(jointName);
                jointXML.setAttribute("x", IniManager.getNumberFormat().format(jointValues.x()));
                jointXML.setAttribute("y", IniManager.getNumberFormat().format(jointValues.y()));
                jointXML.setAttribute("z", IniManager.getNumberFormat().format(jointValues.z()));
            }
        }
        return handShapesXML;
    }

    @Override
    protected void save(String string, List<HandShape> list) {
        XMLTree xml = toXML(list);
        xml.save(string);
    }
}
