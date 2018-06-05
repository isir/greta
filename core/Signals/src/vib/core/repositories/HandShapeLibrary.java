/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.repositories;

import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.IniManager;
import vib.core.util.math.Vec3d;
import vib.core.util.parameter.ParameterSet;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class HandShapeLibrary extends ParameterSet<HandShape> implements CharacterDependent{

    public static final String CHARACTER_PARAMETER_HAND_SHAPE_LIBRARY = "HANDSHAPE_REPOSITORY";

    public HandShapeLibrary() {
        //get the default library :
        super(CharacterManager.getDefaultValueString(CHARACTER_PARAMETER_HAND_SHAPE_LIBRARY));

        //load additionnal library :
        for(String filename : CharacterManager.getAllValuesString(CHARACTER_PARAMETER_HAND_SHAPE_LIBRARY)) {
            addDefinition(filename);
        }

        //set the current library to use :
        setDefinition(CharacterManager.getValueString(CHARACTER_PARAMETER_HAND_SHAPE_LIBRARY));
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
        setDefinition(CharacterManager.getValueString(CHARACTER_PARAMETER_HAND_SHAPE_LIBRARY));
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
