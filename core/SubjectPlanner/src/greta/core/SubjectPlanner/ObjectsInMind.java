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
