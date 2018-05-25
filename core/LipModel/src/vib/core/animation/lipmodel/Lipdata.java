/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.lipmodel;

import vib.core.util.speech.Phoneme.PhonemeType;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;
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
