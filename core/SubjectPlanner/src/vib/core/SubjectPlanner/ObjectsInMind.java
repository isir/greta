/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package vib.core.SubjectPlanner;


import java.util.ArrayList;
import java.util.List;
import vib.core.util.log.Logs;
import vib.core.util.parameter.ParameterSet;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nadine
 */

public class ObjectsInMind extends ParameterSet<MuseumObject> {

    public static final String OBJECTS_IN_MIND = "SubjectPlanner/Data/ObjectsInMind.xml";
    public static ObjectsInMind global_objects_in_mind;

    public ObjectsInMind(String filename) {
        //get the default Lexicon :
        super(filename);;
        Logs.debug("ObjectsInMind FileName " + filename);

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
