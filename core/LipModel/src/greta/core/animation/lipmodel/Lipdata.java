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
package greta.core.animation.lipmodel;

import greta.core.util.speech.Phoneme.PhonemeType;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class Lipdata {

    Map<PhonemeType, Map<PhonemeType, CoartParameter>> datas;
    String fileName;

    public Lipdata(String fileName) {
        load(fileName);
        this.fileName = fileName;
    }

    private void load(String string) {
        XMLParser parser = XML.createParser();
        parser.setValidating(false);
        datas = new EnumMap<PhonemeType, Map<PhonemeType, CoartParameter>>(PhonemeType.class);
        XMLTree lipdataxml = parser.parseFile(string);
        if (lipdataxml.isNamed("lipdata")) {

            for (XMLTree firstPhoneme : lipdataxml.getChildrenElement()) {
                Map<PhonemeType, CoartParameter> phodata = new EnumMap<PhonemeType, CoartParameter>(PhonemeType.class);
                datas.put(PhonemeType.valueOf(firstPhoneme.getAttribute("name")), phodata);
                for (XMLTree secondPhoneme : firstPhoneme.getChildrenElement()) {
                    CoartParameter c = new CoartParameter();

                    for (XMLTree target : secondPhoneme.getChildrenElement()) {
                        if (target.getAttribute("name").equalsIgnoreCase("ULO")) {
                            c.setULO(target.getAttributeNumber("apex"), target.getAttributeNumber("apextime"), target.hasAttribute("middle") ? target.getAttributeNumber("middle") : 0);
                        }
                        if (target.getAttribute("name").equalsIgnoreCase("LLO")) {
                            c.setLLO(target.getAttributeNumber("apex"), target.getAttributeNumber("apextime"), target.hasAttribute("middle") ? target.getAttributeNumber("middle") : 0);
                        }
                        if (target.getAttribute("name").equalsIgnoreCase("JAW")) {
                            c.setJAW(target.getAttributeNumber("apex"), target.getAttributeNumber("apextime"), target.hasAttribute("middle") ? target.getAttributeNumber("middle") : 0);
                        }
                        if (target.getAttribute("name").equalsIgnoreCase("LW")) {
                            c.setLW(target.getAttributeNumber("apex"), target.getAttributeNumber("apextime"), target.hasAttribute("middle") ? target.getAttributeNumber("middle") : 0);
                        }
                        if (target.getAttribute("name").equalsIgnoreCase("ULP")) {
                            c.setULP(target.getAttributeNumber("apex"), target.getAttributeNumber("apextime"), target.hasAttribute("middle") ? target.getAttributeNumber("middle") : 0);
                        }
                        if (target.getAttribute("name").equalsIgnoreCase("LLP")) {
                            c.setLLP(target.getAttributeNumber("apex"), target.getAttributeNumber("apextime"), target.hasAttribute("middle") ? target.getAttributeNumber("middle") : 0);
                        }
                        if (target.getAttribute("name").equalsIgnoreCase("CR")) {
                            c.setCR(target.getAttributeNumber("apex"), target.getAttributeNumber("apextime"), target.hasAttribute("middle") ? target.getAttributeNumber("middle") : 0);
                        }
                    }

                    phodata.put(PhonemeType.valueOf(secondPhoneme.getAttribute("name")), c);
                }
            }
        }
    }
}
