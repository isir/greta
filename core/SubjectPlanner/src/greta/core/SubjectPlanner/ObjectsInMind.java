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
package greta.core.SubjectPlanner;

import greta.core.util.log.Logs;
import greta.core.util.parameter.ParameterSet;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nadine
 */
public class ObjectsInMind extends ParameterSet<MuseumObject> {

    public static final String OBJECTS_IN_MIND = "SubjectPlanner/Data/ObjectsInMind.xml";
    public static ObjectsInMind global_objects_in_mind;

    public ObjectsInMind(String fileName) {
        //get the default Lexicon :
        super(fileName);
        Logs.debug("ObjectsInMind FileName " + fileName);

    }

    /**
     *
     * @param definition
     * @return
     */
    @Override
    protected ArrayList<MuseumObject> load(String definition) {
        ArrayList<MuseumObject> objList = new ArrayList<MuseumObject>();
        Logs.debug("Load FileName " + definition);
        System.out.println("Load FileName " + definition);
        XMLParser parser = XML.createParser();

        //XMLTree facialexpressions = parser.parseFileWithXSD(definition,xsdFile);
        XMLTree Objects = parser.parseFile(definition);
        if (Objects != null) {
            Logs.debug("Load Object not null ");
 //           System.out.println("Load Object not null ");
            XMLTree root = Objects.getRootNode();
            for (XMLTree objects : root.getChildrenElement()) {
                Logs.debug("Objects child " + objects.getName());

      //          System.out.println("Objects child " + objects.getName());
                if (objects.getName().equalsIgnoreCase("object")) {

                    MuseumObject Obj = new MuseumObject(objects.getAttribute("name"), objects.getAttribute("period"), objects.getAttribute("type"), objects.getAttribute("artist"));
                    Logs.debug("Obj " + Obj.name + " " + Obj.period + " " + Obj.type + " " + Obj.artist);

         //           System.out.println("Obj " + Obj.name + " " + Obj.type + " " + Obj.period);
                    objList.add(Obj);

                }
            }
        } else {
            Logs.error("ObjectsInMind not loaded, parse error.");
            System.out.println("ObjectsInMind not loaded, parse error.");
        }
        return objList;
    }

    /**
     *
     * @param string
     * @param list
     */
    @Override
    protected void save(String string, List<MuseumObject> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
