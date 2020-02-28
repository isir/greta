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
package greta.core.signals;

import greta.core.repositories.AUItem;
import greta.core.repositories.HeadLibrary;
import greta.core.signals.gesture.GestureSignal;
import greta.core.signals.gesture.PointingSignal;
import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.enums.Side;
import greta.core.util.log.Logs;
import greta.core.util.speech.Speech;
import greta.core.util.time.TimeMarker;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * It contains methods to translate a list of Signals into an XMLTree in BML
 * format, or translate an XMLTree in BML format into a list of Signals.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because they are
 * UmlGraph tags, not javadoc tags.
 * @depend * "generates\nfrom Signals" 1 greta.core.util.xml.XMLTree
 * @depend 1 "generates\nfrom XMLTree" * greta.core.signals.Signal
 */
public class BMLTranslator {

    /**
     *
     */
    public static String bmlNameSpace = "http://www.mindmakers.org/projects/BML";
    /**
     *
     */
    public static String gretaNameSpace = "GretaNameSpace";

    public static Mode getDefaultBMLMode() {
        return new Mode(IniManager.getGlobals().getValueString("DEFAULT_BML_MODE"), CompositionType.blend);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private BMLTranslator() {
    }
    /**
     * To ensure backward compatibility with the tag end used as a
     * duration.<br/> {@code true} to use the old wrong format, {@code false}
     * otherwise.<br/> must be remove as soon as possible
     */
    private static final boolean endAsDuration = IniManager.getGlobals().getValueBoolean("BML_END_TAG_AS_DURATION");
    /**
     * defines the name of the descriptions specific to Greta
     */
    private static String greta_s_description_type;
    /**
     * To define the String format of numbers
     */
    private static NumberFormat numberFormat;

    /**
     * Static constructor
     */
    static {
        numberFormat = IniManager.getNumberFormat();
        greta_s_description_type = "gretabml";
    }

    /**
     * Translates an XMLTree in BML format to a List of Signals.
     *
     * @param bml the XMLTree in BML
     * @return the List of Signals
     */
    public static synchronized List<Signal> BMLToSignals(XMLTree bml, CharacterManager cm) {
        List<Signal> signals = new ArrayList<Signal>();

        XMLTree root = bml.getRootNode();

        for (XMLTree bmlchild : root.getChildrenElement()) {

            //<editor-fold defaultstate="collapsed" desc="laugh">

            // <greta:laugh id="t1" greta:lexeme="fileName" start="2">
            // <description priority="1" type="Greta">
            //     <greta:lexeme reference=fileName/>
            // </description>
            // </greta:laugh>

            if ((bmlchild.getName().equalsIgnoreCase("greta:laugh")) || (bmlchild.getName().equalsIgnoreCase("laugh"))) {

                LaughSignal laugh = new LaughSignal();

                laugh.setId(bmlchild.getAttribute("id"));

                if(bmlchild.hasAttribute("intensity")){
                    laugh.setIntensity(bmlchild.getAttributeNumber("intensity"));
                }

                if (bmlchild.hasAttribute("start")) {
                    laugh.setTimeMarker(bmlchild.getAttribute("start"), "start");
                }

                if (bmlchild.hasAttribute("end")) {
                    laugh.setTimeMarker(bmlchild.getAttribute("end"), "end");
                }

                for (XMLTree laughchild : bmlchild.getChildrenElement()) {
                    //if defined by descrption level
                    if (laughchild.getName().equalsIgnoreCase("description")) {

                        if (laughchild.hasAttribute("type") && laughchild.getAttribute("type").equalsIgnoreCase("Greta")) {

                            for (XMLTree laughgrandchild : laughchild.getChildrenElement()) {
                                if ((laughgrandchild.getName().equalsIgnoreCase("lexeme")) || (laughgrandchild.getName().equalsIgnoreCase("greta:lexeme"))) {
                                    if (laughgrandchild.hasAttribute("reference")) {
                                        laugh.setFileName(laughgrandchild.getAttribute("reference"));
                                    }
                                }
                            }//end of for
                        }//end fordescription
                    }//end of for
                }
                signals.add(laugh);
            }//end of greta laugth

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="speech">
            if (bmlchild.getName().equalsIgnoreCase("speech")) {
                SpeechSignal speech = new SpeechSignal(cm);
                speech.readFromXML(bmlchild, endAsDuration);
                signals.add(speech);
            }

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="shoulder">

            /* SHOULDER tag
             *
             *   sync points = like facial exp - start and end obligatory
             *   lexemes - four posible front, back, shake, up - hardcoded
             *   amount [0 - 1] - optional default 1
             *   side l/r/both - optional default both
             *   repapetition optional default is 1
             *
             *
             *
             *
             */

            //add the namespace greta
            if (bmlchild.getName().equalsIgnoreCase("greta:shoulder") || bmlchild.getName().equalsIgnoreCase("shoulder")) {
                if (!bmlchild.hasAttribute("id") || !bmlchild.hasAttribute("lexeme")) {
                    Logs.warning("Shoulder tag syntax is not valid!");
                    continue;
                }

                //obligatory  parameters
                ShoulderSignal shoulderSignal = new ShoulderSignal(bmlchild.getAttribute("id"));
                String reference = bmlchild.getAttribute("lexeme");
                shoulderSignal.setReference(reference);

                //optional parameters
                if (bmlchild.hasAttribute("amount")) {
                    shoulderSignal.setIntensity(bmlchild.getAttributeNumber("amount"));
                } else {
                    shoulderSignal.setIntensity(1.0d);
                }

                Side side = Side.BOTH;
                if (bmlchild.hasAttribute("side")) {
                    String sideAttr = bmlchild.getAttribute("side");
                    if (sideAttr.equalsIgnoreCase("left")) {
                        side = Side.LEFT;
                    }
                    else if (sideAttr.equalsIgnoreCase("right")) {
                        side = Side.RIGHT;
                    }
                    else {
                        side = Side.BOTH;
                    }
                }
                shoulderSignal.setSide(side);

                if (bmlchild.hasAttribute("repetition")) {
                    shoulderSignal.setRepetition(bmlchild.getAttributeNumber("repetition"));
                } else {
                    shoulderSignal.setRepetition(1.0d);
                }

                if (bmlchild.hasAttribute("normalized")) {
                    shoulderSignal.setMode((int) (bmlchild.getAttributeNumber("normalized")));
                } else {
                    shoulderSignal.setMode(1);
                }

               if (bmlchild.hasAttribute("torso")) {
                    shoulderSignal.setTorso((int) (bmlchild.getAttributeNumber("torso")));
                } else {
                    shoulderSignal.setTorso(0);
                }

                //sync points: start, attackPeak, relax, end
                //attribute start
                if (bmlchild.hasAttribute("start")) {
                    shoulderSignal.setTimeMarker(bmlchild.getAttribute("start"), "start");
                }

                //attribute attackPeak
                if (bmlchild.hasAttribute("attackPeak")) {
                    shoulderSignal.setTimeMarker(bmlchild.getAttribute("attackPeak"), "attack");
                }

                //attribute relax
                if (bmlchild.hasAttribute("relax")) {
                    shoulderSignal.setTimeMarker(bmlchild.getAttribute("relax"), "sustain");
                }

                //attrbiute end
                if (bmlchild.hasAttribute("end")) {
                    shoulderSignal.setTimeMarker(bmlchild.getAttribute("end"), "end");
                }

                //core extensions?
                //attrbiute overshoot
                if (bmlchild.hasAttribute("overshoot")) {
                    shoulderSignal.setTimeMarker(bmlchild.getAttribute("overshoot"), "decay");
                }

                signals.add(shoulderSignal);

            }

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="face">
            //NEW BML PARSER for facial expressions

            //face - ONLY COMPOUND EXPRESSIONS

            if (bmlchild.getName().equalsIgnoreCase("face")) {

// XML LESSON :
//the syntax greta:xxx is valid only if there is somewhere xmlns:greta="yyyy" specified (in the xxx node or its parents, not in its children)
//"greta" it self has no sens. only "yyyy" makes sens.
//it is only a shortcut for the namespace, it is NOT a part of the markup name
//the folowing exemples are stricly the same :
//<greta:lexeme xmlns:greta="the.greta.s.namespace" >
//<toto:lexeme xmlns:toto="the.greta.s.namespace" >
//<lexeme xmlns="the.greta.s.namespace" >
//
//so you can use :
//bmlchild.findNodeCalled("lexeme");//if the resulted xmlTree "lexeme" is non null, it will have the same namaspace than bmlchild (the bml namespace)
//bmlchild.findNodeCalled("lexeme", bmlNameSpace);//idem
//bmlchild.findNodeCalled("lexeme", gretaNameSpace);//if the resulted xmlTree "lexeme" is non null, it will have the greta namespace.


//                SYNTAX AVAILABLE at the moment:
//
//                1. all agents do the first line
//
//                <face id="face1" start="" end="" amount="" other sync points possible...>
//                        <lexeme lexeme="RAISE_MOUTH_CORNER"/>
//                </face>
//
//                2. only greta does this facial expressions
//
//                <face id="face1" start="" end="" amount="" other sync points possible...>
//                        <greta:lexeme lexeme="RAISE_MOUTH_CORNER"/>
//                </face>
//
//                3. both agents do the first line, gretA additionally does the second line
//
//                <face id="face1" start="" end="" amount="" other sync points possible...>
//                        <lexeme lexeme="RAISE_MOUTH_CORNER"/>
//                        <greta:lexeme lexeme="RAISE_MOUTH_CORNER"/>
//                </face>
//
//                 4. greta does slight_polite zhile the others do raise_,outh_corner
//
//                 WRONG: NOT A STANDARD - it should be by level description 1
//
//                <face id="face1" start="" end="" amount="" other sync points possible...>
//                        <lexeme lexeme="RAISE_MOUTH_CORNER" greta:faceexp="slight_polite"/>
//                </face>
//
//                CORRECT SYNTAX (NOT IMPLEMENTED YET) IS:
//
//                  <face id="face1" start="" end="" amount="" other sync points possible...>
//                        <lexeme lexeme="RAISE_MOUTH_CORNER"
//                           <description priority="1" type="greta">
//                                     <greta:lexeme lexeme="slight_polite"/>
//                          </descrition>
//                </face>
//
//                 5. Greta does slight_polite AND AU1 While the others do raise_Mouth_corner AND AU1
//
//                 WRONG: NOT A STANDARD - it should be by level description 1
//
//                <face id="face1" start="" end="" amount="" other sync points possible...>
//                        <lexeme lexeme="RAISE_MOUTH_CORNER" greta:faceexp="slight_polite"/>
//                        <ext:facs au="1"/>
//                </face>
//
//
//                CORRECT SYNTAX (NOT IMPLEMENTED YET) IS:
//
//                  <face id="face1" start="" end="" amount="" other sync points possible...>
//                        <lexeme lexeme="RAISE_MOUTH_CORNER"
//                        <ext:facs au="1"/>
//                           <description priority="1" type="greta">
//                                     <ext:facs au="1"/>
//                                     <greta:lexeme lexeme="slight_polite"/>
//                          </descrition>
//                </face>
//
//               6. both do AU=1
//
//                <face id="face1" start="" end="" amount="" other sync points possible...>
//                        <ext:facs au="1"/>
//                </face>
//
//                both do AU=1 and AU=2
//
//                <face id="face1" start="" end="" amount="" other sync points possible...>
//                        <ext:facs au="1"/>
//                        <ext:facs au="2"/>
//               </face>
//


                //if nochildren = no expression
                for (XMLTree facechild : bmlchild.getChildrenElement()) {

                    //if defined by descrption level

                    //does it contain descrption level 1?
                    if (facechild.getName().equalsIgnoreCase("description")) {
                        //do all the same but ignore everything that is outside the descrption tag
                        // recursive?
                        //
                        //TODO sytax not not suported
                        //thrown BML not supported exeception
                    }

                    //if defined by lexemes


                    //TODO: add namespaces
                    if ((facechild.getName().equalsIgnoreCase("lexeme")) || (facechild.getName().equalsIgnoreCase("greta:lexeme"))) {

                        int number_of_expressions_for_this_tag = 0;
                        //atribute id
                        FaceSignal faceexpression = new FaceSignal(bmlchild.getAttribute("id") + facechild.getAttribute("lexeme"));

                        //THIS SHOULD NEVERHAPPEN - USE DESCRIPTION INSTEAD
                        if (facechild.hasAttribute("greta:faceexp")) {
                            faceexpression.setReference("faceexp=" + facechild.getAttribute("greta:faceexp"));
                            faceexpression.setCategory("faceexp");
                            number_of_expressions_for_this_tag++;

                        } else {

                            //if it is standard lexeme
                            if ((facechild.hasAttribute("lexeme")) && (facechild.getName().equalsIgnoreCase("lexeme"))) {
                                faceexpression.setReference("faceexp=" + facechild.getAttribute("lexeme"));
                                faceexpression.setCategory("faceexp");
                                number_of_expressions_for_this_tag++;

                            }//endif lexeme

                            //if it is greta:lexeme
                            if ((facechild.hasAttribute("lexeme")) && (facechild.getName().equalsIgnoreCase("greta:lexeme"))) {
                                faceexpression.setReference("faceexp=" + facechild.getAttribute("greta:lexeme"));
                                faceexpression.setCategory("faceexp");
                                number_of_expressions_for_this_tag++;

                            }//endif greta:lexeme


                        }//end of else

                        //attribute amount
                        faceexpression.setIntensity(bmlchild.getAttributeNumber("amount"));

                        //sync points: start, attackPeak, relax, end
                        //attribute start
                        if (bmlchild.hasAttribute("start")) {
                            faceexpression.setTimeMarker(bmlchild.getAttribute("start"), "start");
                        }

                        //attribute attackPeak
                        if (bmlchild.hasAttribute("attackPeak")) {
                            faceexpression.setTimeMarker(bmlchild.getAttribute("attackPeak"), "attack");
                        }

                        //attribute relax
                        if (bmlchild.hasAttribute("relax")) {
                            faceexpression.setTimeMarker(bmlchild.getAttribute("relax"), "sustain");
                        }

                        //attrbiute end
                        if (bmlchild.hasAttribute("end")) {
                            faceexpression.setTimeMarker(bmlchild.getAttribute("end"), "end");
                        }

                        //core extensions?
                        //attrbiute overshoot
                        if (bmlchild.hasAttribute("overshoot")) {
                            faceexpression.setTimeMarker(bmlchild.getAttribute("overshoot"), "decay");
                        }

                        if (number_of_expressions_for_this_tag > 0) {
                            signals.add(faceexpression);
                        }

                    }//end of lexeme


                    //if defined by aus

                    //check if
                    if ((facechild.getName().equalsIgnoreCase("ext:facs")) || (facechild.getName().equalsIgnoreCase("facs"))) {
                        //if yes split it into additional facial signals

                        //atribute id
                        FaceSignal faceexpression = new FaceSignal(bmlchild.getAttribute("id") + " " + facechild.getAttribute("au"));
                        int number_of_expressions_for_this_tag = 0;

                        //attribute amount
                        faceexpression.setIntensity(bmlchild.getAttributeNumber("amount"));

                        //sync points: start, attackPeak, relax, end

                        //attribute start
                        if (bmlchild.hasAttribute("start")) {
                            faceexpression.setTimeMarker(bmlchild.getAttribute("start"), "start");
                        }


                        //attribute attackPeak
                        if (bmlchild.hasAttribute("attackPeak")) {
                            faceexpression.setTimeMarker(bmlchild.getAttribute("attackPeak"), "attack");
                        }


                        //attribute relax
                        if (bmlchild.hasAttribute("relax")) {
                            faceexpression.setTimeMarker(bmlchild.getAttribute("relax"), "sustain");
                        }


                        //attrbiute end
                        if (bmlchild.hasAttribute("end")) {
                            faceexpression.setTimeMarker(bmlchild.getAttribute("end"), "end");
                        }

                        //core extensions?
                        //attrbiute overshoot
                        if (bmlchild.hasAttribute("overshoot")) {
                            faceexpression.setTimeMarker(bmlchild.getAttribute("overshoot"), "decay");
                        }


                        //add this expression
                        if (facechild.hasAttribute("au")) {
                            faceexpression.setReference("au=AU" + facechild.getAttribute("au"));

                            double amount = 1.0;
                            if (facechild.hasAttribute("amount")) {
                                amount = (new Double(facechild.getAttribute("amount"))).doubleValue();
                            }

                            Side side = Side.BOTH;
                            if (facechild.hasAttribute("side")) {
                                side = Side.valueOf(facechild.getAttribute("side").toUpperCase());
                            }

                            AUItem au1 = new AUItem("AU" + facechild.getAttribute("au"), amount, side);
                            faceexpression.add(au1);

                            //useless?
                            faceexpression.setCategory("au");
                            number_of_expressions_for_this_tag++;
                        }

                        //WRONG NOT HERE
                        //if (facechild.hasAttribute("side")) {
                        //    //TODO
                        //    //not suported at the moment
                        //}

                        if (number_of_expressions_for_this_tag > 0) {
                            signals.add(faceexpression);
                        }

                    }//end of au

                }//end of for

            }//end of face


            //faceLexeme: To do
            if (bmlchild.getName().equalsIgnoreCase("facelexeme")) {

                int number_of_expressions_for_this_tag = 0;

//                SYNTAX AVAILABLE at the moment:
//
//                 1.
//
//                <facelexeme id="face1" start="" end="" amount="" other sync points possible... lexeme="RAISE_MOUTH_CORNER"/>
//
//                2. WRONG not a standard!!!
//
//                <facelexeme id="face1" start="" end="" amount="" other sync points possible... lexeme="RAISE_MOUTH_CORNER" greta:faceexp="slight_polite"/>
//
//                it should be (not implemented yet):
//
//                <facelexeme id="face1" start="" end="" amount="" other sync points possible... lexeme="RAISE_MOUTH_CORNER">
//                 <description priority="1" type="greta">
//                          <greta:lexeme lexeme="slight_polite"/>
//                 </description>
//                 </facelexeme>

                //atribute id
                FaceSignal faceexpression = new FaceSignal(bmlchild.getAttribute("id"));

                //if nochildren = standard lexeme

                int bml_number_of_children = 0;

                for (XMLTree facechild : bml.getChildrenElement()) {

                    //if defined by descrption level

                    //does it contain descrption level 1?
                    if (facechild.getName().equalsIgnoreCase("description")) {
                        //do all the same but ignore everything that is outside the descrption tag
                        // recursive?
                        //
                        //TODO sytax not not suported
                        //thrown BML not supported exeception
                        bml_number_of_children++;
                    }

                }

                if (bml_number_of_children == 0) {// = no descrption level only main tag

                    //THIS SHOULD NEVERHAPPEN - USE DESCRIPTION INSTEAD
                    if (bmlchild.hasAttribute("greta:lexeme")) {

                        faceexpression.setReference("faceexp=" + bmlchild.getAttribute("greta:lexeme"));
                        faceexpression.setCategory("faceexp");
                        number_of_expressions_for_this_tag++;

                    } else {
                        if (bmlchild.hasAttribute("lexeme")) {
                            faceexpression.setReference("faceexp=" + bmlchild.getAttribute("lexeme"));
                            faceexpression.setCategory("faceexp");
                            number_of_expressions_for_this_tag++;

                        }//endif lexeme
                    }//end of else

                    //attribute amount
                    if (bmlchild.hasAttribute("amount")) {
                        faceexpression.setIntensity(bmlchild.getAttributeNumber("amount"));
                    } else {
                        faceexpression.setIntensity(1);
                    }

                    //sync points: start, attackPeak, relax, end
                    //attribute start
                    if (bmlchild.hasAttribute("start")) {
                        faceexpression.setTimeMarker(bmlchild.getAttribute("start"), "start");
                    }

                    //attribute attackPeak
                    if (bmlchild.hasAttribute("attackPeak")) {
                        faceexpression.setTimeMarker(bmlchild.getAttribute("attackPeak"), "attack");
                    }

                    //attribute relax
                    if (bmlchild.hasAttribute("relax")) {
                        faceexpression.setTimeMarker(bmlchild.getAttribute("relax"), "sustain");
                    }

                    //attrbiute end
                    if (bmlchild.hasAttribute("end")) {
                        faceexpression.setTimeMarker(bmlchild.getAttribute("end"), "end");
                    }

                    //core extensions?
                    //attrbiute overshoot
                    if (bmlchild.hasAttribute("overshoot")) {
                        faceexpression.setTimeMarker(bmlchild.getAttribute("overshoot"), "decay");
                    }

                    if (number_of_expressions_for_this_tag > 0) {
                        signals.add(faceexpression);
                    }

                }//ens of else

            }//end of faceleceme

            //facefacs: To do
            if ((bmlchild.getName().equalsIgnoreCase("ext:facefacs")) || (bmlchild.getName().equalsIgnoreCase("facefacs"))) {
                //String st = bmlchild.getAttribute("start");
                //String au = bmlchild.getAttribute("au");

//                SYNTAX AVAILABLE at the moment:
//
//                <ext:facefacs id="face1" start="" end="" amount="" other sync points possible... au="1"/>
//

                //atribute id
                FaceSignal faceexpression = new FaceSignal(bmlchild.getAttribute("id"));
                int number_of_expressions_for_this_tag = 0;

                //attribute amount
                double amount = 1;
                if (bmlchild.hasAttribute("amount")) {
                    amount = bmlchild.getAttributeNumber("amount");
                }

                Side side = Side.BOTH;
                if (bmlchild.hasAttribute("side")) {
                    side = Side.valueOf(bmlchild.getAttribute("side").toUpperCase());
                }

                //intensity is defined locally in AUItem,
                //global intensity should not influence it, thus it is =1
                // AUintenisty * global intensity = Auintensity * 1 =  AuIntensity
                faceexpression.setIntensity(1);

                //sync points: start, attackPeak, relax, end

                //attribute start
                if (bmlchild.hasAttribute("start")) {
                    faceexpression.setTimeMarker(bmlchild.getAttribute("start"), "start");
                }


                //attribute attackPeak
                if (bmlchild.hasAttribute("attackPeak")) {
                    faceexpression.setTimeMarker(bmlchild.getAttribute("attackPeak"), "attack");
                }


                //attribute relax
                if (bmlchild.hasAttribute("relax")) {
                    faceexpression.setTimeMarker(bmlchild.getAttribute("relax"), "sustain");
                }


                //attrbiute end
                if (bmlchild.hasAttribute("end")) {
                    faceexpression.setTimeMarker(bmlchild.getAttribute("end"), "end");
                }

                //core extensions?
                //attrbiute overshoot
                if (bmlchild.hasAttribute("overshoot")) {
                    faceexpression.setTimeMarker(bmlchild.getAttribute("overshoot"), "decay");
                }


                //add this expression
                if (bmlchild.hasAttribute("au")) {

                    //zle dodaj AU
                    faceexpression.setReference("au=" + bmlchild.getAttribute("au"));

                    AUItem au1 = new AUItem("AU" + bmlchild.getAttribute("au"), amount, side);
                    faceexpression.add(au1);
                    //useless?
                    faceexpression.setCategory("au");
                    number_of_expressions_for_this_tag++;
                }


                if (number_of_expressions_for_this_tag > 0) {
                    signals.add(faceexpression);
                }

            }//end of facefacs

            //facefacs: To do
            if (bmlchild.getName().equalsIgnoreCase("faceshifts")) {
                //to do not implemented
                //throws BMLException()
            }//end of faceshifts
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="gesture">
            // gestures
        /*
             * <gesture id="behavior1" lexeme="beat=beat_right" start="2"
             * mode="BOTH_HANDS"> <description priority="1" type="greta">
             * <reference>beat=beat_right</reference>
             * <intensity>1.00</intensity> <SPC.value>0.00</SPC.value>
             * <TMP.value>0.00</TMP.value> <FLD.value>0.00</FLD.value>
             * <PWR.value>0.00</PWR.value> <REP.value>-1.00</REP.value>
             * </description> </gesture>
             */
            if (bmlchild.getName().equalsIgnoreCase("gesture")) {
                if (!bmlchild.hasAttribute("id") || !bmlchild.hasAttribute("lexeme")) {
                    Logs.warning("Gesture syntaxes are not valided with BML Standard!");
                    continue;
                }
                GestureSignal gestureSignal = new GestureSignal(bmlchild.getAttribute("id"));
                String reference = "gesture=".concat(bmlchild.getAttribute("lexeme"));

                gestureSignal.setReference(reference);

                if (bmlchild.hasAttribute("start")) {
                    gestureSignal.getStart().addReference(bmlchild.getAttribute("start"));
                }
                if (bmlchild.hasAttribute("ready")) {
                    gestureSignal.getTimeMarker("ready").addReference(bmlchild.getAttribute("ready"));
                }
                if (bmlchild.hasAttribute("strokeStart")) {
                    if(gestureSignal.getTimeMarker("stroke-start")==null)
                    {
                        gestureSignal.getTimeMarkers().add(new TimeMarker("stroke-start"));
                    }
                    gestureSignal.getTimeMarker("stroke-start").addReference(bmlchild.getAttribute("strokeStart"));
                }
                if (bmlchild.hasAttribute("stroke")) {
                    if(gestureSignal.getTimeMarker("stroke")==null)
                    {
                        gestureSignal.getTimeMarkers().add(new TimeMarker("stroke"));
                    }
                    gestureSignal.getTimeMarker("stroke").addReference(bmlchild.getAttribute("stroke"));
                }
                if (bmlchild.hasAttribute("strokeEnd")) {
                    if(gestureSignal.getTimeMarker("stroke-end")==null)
                    {
                        gestureSignal.getTimeMarkers().add(new TimeMarker("stroke-end"));
                    }
                    gestureSignal.getTimeMarker("stroke-end").addReference(bmlchild.getAttribute("strokeEnd"));
                }
                if (bmlchild.hasAttribute("relax")) {
                    gestureSignal.getTimeMarker("relax").addReference(bmlchild.getAttribute("relax"));
                }
                if (bmlchild.hasAttribute("end")) {
                    gestureSignal.getEnd().addReference(bmlchild.getAttribute("end"));
                }

                readDescriptionOfParametricSignals(bmlchild.findNodeCalled("description"), gestureSignal);

                signals.add(gestureSignal);
            }

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="head">
            // Read head
            if (bmlchild.getName().equalsIgnoreCase("head")) {
                if (!bmlchild.hasAttribute("id") || !bmlchild.hasAttribute("lexeme")) {
                    Logs.warning("Head syntaxes are not valided with BML Standard!");
                    continue;
                }
                HeadSignal headSignal = new HeadSignal(bmlchild.getAttribute("id"));
                String lexeme = bmlchild.getAttribute("lexeme");
                headSignal.setLexeme(lexeme);

                if (bmlchild.hasAttribute("repetition")) {
                    headSignal.setRepetitions((int) bmlchild.getAttributeNumber("repetition"));
                }

                if (bmlchild.hasAttribute("start")) {
                    headSignal.getStart().addReference(bmlchild.getAttribute("start"));
                }
                if (bmlchild.hasAttribute("ready")) {
                    headSignal.getTimeMarker("ready").addReference(bmlchild.getAttribute("ready"));
                }
                if (bmlchild.hasAttribute("strokeStart")) {
                    headSignal.getTimeMarker("strokeStart").addReference(bmlchild.getAttribute("strokeStart"));
                }
                if (bmlchild.hasAttribute("stroke")) {
                    headSignal.getTimeMarker("stroke").addReference(bmlchild.getAttribute("stroke"));
                }
                if (bmlchild.hasAttribute("strokeEnd")) {
                    headSignal.getTimeMarker("strokeEnd").addReference(bmlchild.getAttribute("strokeEnd"));
                }
                if (bmlchild.hasAttribute("relax")) {
                    headSignal.getTimeMarker("relax").addReference(bmlchild.getAttribute("relax"));
                }
                if (bmlchild.hasAttribute("end")) {
                    headSignal.getEnd().addReference(bmlchild.getAttribute("end"));
                }

                readDescriptionOfParametricSignals(bmlchild.findNodeCalled("description"), headSignal);

                //the amount attribute override the spc parameter
                if (bmlchild.hasAttribute("amount")) {
                    headSignal.setSPC(bmlchild.getAttributeNumber("amount"));
                }

                signals.add(headSignal);
            }


            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="headDirectionShift">
            if (bmlchild.getName().equalsIgnoreCase("headDirectionShift")) {
                if (!bmlchild.hasAttribute("id")) {
                    Logs.warning("Head syntaxes are not valided with BML Standard!");
                    continue;
                }
                HeadSignal headSignal = new HeadSignal(bmlchild.getAttribute("id"));
                headSignal.setDirectionShift(true);
                headSignal.setLexeme(bmlchild.getAttribute("target"));// ??? how to use the target
                if (bmlchild.hasAttribute("start")) {
                    headSignal.getStart().addReference(bmlchild.getAttribute("start"));
                }
                if (bmlchild.hasAttribute("end")) {
                    headSignal.getEnd().addReference(bmlchild.getAttribute("end"));
                }
                readDescriptionOfParametricSignals(bmlchild.findNodeCalled("description"), headSignal);
                signals.add(headSignal);
            }

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="torso">
            //Read torso
            //System.out.println("signal name="+bmlchild.getName());
            if (bmlchild.getName().equalsIgnoreCase("greta:torso") || bmlchild.getName().equalsIgnoreCase("torso")) {
                if (!bmlchild.hasAttribute("id") || !bmlchild.hasAttribute("lexeme")) {
                    Logs.warning("Torso syntaxes are not valided as an extension of BML Standard!");
                    continue;
                }
                TorsoSignal torsoSignal = new TorsoSignal(bmlchild.getAttribute("id"));
                String reference = "torso=".concat(bmlchild.getAttribute("lexeme"));
                torsoSignal.setReference(reference);

                if (bmlchild.hasAttribute("start")) {
                    torsoSignal.getStart().addReference(bmlchild.getAttribute("start"));
                }
                if (bmlchild.hasAttribute("ready")) {
                    torsoSignal.getTimeMarker("ready").addReference(bmlchild.getAttribute("ready"));
                }
                if (bmlchild.hasAttribute("strokeStart")) {
                    torsoSignal.getTimeMarker("strokeStart").addReference(bmlchild.getAttribute("strokeStart"));
                }
                if (bmlchild.hasAttribute("stroke")) {
                    torsoSignal.getTimeMarker("stroke").addReference(bmlchild.getAttribute("stroke"));
                }
                if (bmlchild.hasAttribute("strokeEnd")) {
                    torsoSignal.getTimeMarker("strokeEnd").addReference(bmlchild.getAttribute("strokeEnd"));
                }
                if (bmlchild.hasAttribute("relax")) {
                    torsoSignal.getTimeMarker("relax").addReference(bmlchild.getAttribute("relax"));
                }
                if (bmlchild.hasAttribute("end")) {
                    torsoSignal.getEnd().addReference(bmlchild.getAttribute("end"));
                }

                readDescriptionOfParametricSignals(bmlchild.findNodeCalled("description"), torsoSignal);

                signals.add(torsoSignal);
            }

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="gaze">
            //Read gaze
            if (bmlchild.getName().equalsIgnoreCase("greta:gaze") || bmlchild.getName().equalsIgnoreCase("gaze")) {
                GazeSignal gazeSignal = new GazeSignal(bmlchild.getAttribute("id"));
                gazeSignal.readFromXML(bmlchild, endAsDuration);
                gazeSignal.setCharacterManager(cm);
                signals.add(gazeSignal);
            }

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="gazeShift">
            if (bmlchild.getName().equalsIgnoreCase("gazeShift")) {
                GazeSignal gazeSignal = new GazeSignal(bmlchild.getAttribute("id"));
                gazeSignal.setGazeShift(true);
                gazeSignal.readFromXML(bmlchild, endAsDuration);
                gazeSignal.setCharacterManager(cm);
                signals.add(gazeSignal);
            }
            //</editor-fold>


            if (bmlchild.getName().equalsIgnoreCase("pointing")) {
                if (!bmlchild.hasAttribute("id") || !bmlchild.hasAttribute("target")) {
                    Logs.warning("Pointing syntaxes are not valided with BML Standard!");
                    continue;
                }
                PointingSignal pointingSignal = new PointingSignal(bmlchild.getAttribute("id"));
                String reference = "target=".concat(bmlchild.getAttribute("target"));

                pointingSignal.setReference(reference);

                if (bmlchild.hasAttribute("target")) {
                    pointingSignal.setTarget(bmlchild.getAttribute("target"));
                }

                if (bmlchild.hasAttribute("mode")) {
                    String mode = bmlchild.getAttribute("mode");
                    if(mode.equals("RIGHT_HAND"))
                        pointingSignal.setMode(Side.RIGHT);
                    else if(mode.equals("LEFT_HAND"))
                        pointingSignal.setMode(Side.LEFT);
                    else if(mode.equals("BOTH_HANDS"))
                        pointingSignal.setMode(Side.BOTH);
                }

                if (bmlchild.hasAttribute("start")) {
                    pointingSignal.getStart().addReference(bmlchild.getAttribute("start"));
                }
                if (bmlchild.hasAttribute("ready")) {
                    pointingSignal.getTimeMarker("ready").addReference(bmlchild.getAttribute("ready"));
                }
                if (bmlchild.hasAttribute("strokeStart")) {
                    if(pointingSignal.getTimeMarker("stroke-start")==null)
                    {
                        pointingSignal.getTimeMarkers().add(new TimeMarker("stroke-start"));
                    }
                    pointingSignal.getTimeMarker("stroke-start").addReference(bmlchild.getAttribute("strokeStart"));
                }
                if (bmlchild.hasAttribute("stroke")) {
                    if(pointingSignal.getTimeMarker("stroke")==null)
                    {
                        pointingSignal.getTimeMarkers().add(new TimeMarker("stroke"));
                    }
                    pointingSignal.getTimeMarker("stroke").addReference(bmlchild.getAttribute("stroke"));
                }
                if (bmlchild.hasAttribute("strokeEnd")) {
                    if(pointingSignal.getTimeMarker("stroke-end")==null)
                    {
                        pointingSignal.getTimeMarkers().add(new TimeMarker("stroke-end"));
                    }
                    pointingSignal.getTimeMarker("stroke-end").addReference(bmlchild.getAttribute("strokeEnd"));
                }
                if (bmlchild.hasAttribute("relax")) {
                    pointingSignal.getTimeMarker("relax").addReference(bmlchild.getAttribute("relax"));
                }
                if (bmlchild.hasAttribute("end")) {
                    pointingSignal.getEnd().addReference(bmlchild.getAttribute("end"));
                }

                readDescriptionOfParametricSignals(bmlchild.findNodeCalled("description"), pointingSignal);

                signals.add(pointingSignal);
            }
            //TODO complete other signals

        }
        return signals;
    }

    private static void readDescriptionOfParametricSignals(XMLTree description, ParametricSignal signal) {
        if (description != null
                && description.getName().equalsIgnoreCase("description")
                && description.hasAttribute("type")
                && description.getAttribute("type").equalsIgnoreCase(greta_s_description_type)) {

            for (XMLTree parameter : description.getChildrenElement()) {
                if (parameter.getName().equalsIgnoreCase("reference")) {
                    signal.setReference(parameter.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                }
                if (parameter.getName().equalsIgnoreCase("intensity")) {
                    signal.setIntensity(Double.parseDouble(parameter.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue()));
                }
                if (parameter.getName().equalsIgnoreCase("SPC.value")) {
                    signal.setSPC(Double.parseDouble(parameter.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue()));
                }
                if (parameter.getName().equalsIgnoreCase("TMP.value")) {
                    signal.setTMP(Double.parseDouble(parameter.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue()));
                }
                if (parameter.getName().equalsIgnoreCase("FLD.value")) {
                    signal.setFLD(Double.parseDouble(parameter.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue()));
                }
                if (parameter.getName().equalsIgnoreCase("PWR.value")) {
                    signal.setPWR(Double.parseDouble(parameter.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue()));
                }
                if (parameter.getName().equalsIgnoreCase("REP.value")) {
                    signal.setREP(Double.parseDouble(parameter.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue()));
                }
                if (parameter.getName().equalsIgnoreCase("OPN.value")) {
                    signal.setOpenness(Double.parseDouble(parameter.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue()));
                }
                if (parameter.getName().equalsIgnoreCase("TEN.value")) {
                    signal.setTension(Double.parseDouble(parameter.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue()));
                }
            }
        }
    }

    /**
     * Translates a List of Signals to an XMLTree in BML format.
     *
     * @param signals the List of Signals
     * @param mode how the list is added to previous list : blend, replace,
     * append
     * @return the XMLTree in BML
     */
    public static synchronized XMLTree SignalsToBML(List<Signal> signals, Mode mode) {
        XMLTree bml = XML.createTree("bml", bmlNameSpace);
        // if the bml id is written in the fml file we put the same id in the bml
        if (mode.getBml_id() != "" || mode.getBml_id() != null){
            bml.setAttribute("id", mode.getBml_id());
        }else {// otherwise we put a stadanrd id in the bml file
            bml.setAttribute("id", "bml1");
        }
        bml.setAttribute("character", "Greta");
        bml.setAttribute("composition", mode.getCompositionType().toString());
        bml.setAttribute("reaction_type", mode.getReactionType().toString());
        bml.setAttribute("reaction_duration", mode.getReactionDuration().toString());
        bml.setAttribute("social_attitude", mode.getSocialAttitude().toString());

        for (Signal signal : signals) {
            //<editor-fold defaultstate="collapsed" desc="speech">
            //SPEECH
            if (signal instanceof Speech) {
                bml.addChild(((Speech) signal).toXML(endAsDuration));
                continue;
            }

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="face">

            if (signal instanceof FaceSignal) {
                FaceSignal facesignal = (FaceSignal) signal;

                XMLTree signalxml = bml.createChild(facesignal.getModality());
                signalxml.setAttribute("id", facesignal.getId());


                TimeMarker start = facesignal.getStart();
                String stringofstart = TimeMarker.convertTimeMarkerToSynchPointString(start, "0", true);
                signalxml.setAttribute("start", stringofstart);


                TimeMarker end = facesignal.getEnd();
                String stringofend = TimeMarker.convertTimeMarkerToSynchPointString(end, "0", true);
                signalxml.setAttribute("end", stringofend);

                signalxml.setAttribute("amount", numberFormat.format(facesignal.getIntensity()));

                //descrition
                XMLTree description = signalxml.createChild("lexeme");
                String gestureID = facesignal.getReference().substring(facesignal.getReference().indexOf("=") + 1).trim();
                description.setAttribute("lexeme", gestureID);

            }//end of facz

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="gesture">
            //GESTURE
            if (signal instanceof GestureSignal) {
                GestureSignal parametric = (GestureSignal) signal;
                XMLTree signalxml = bml.createChild(parametric.getModality());
                signalxml.setAttribute("id", parametric.getId());

                String gestureID = parametric.getReference().substring(parametric.getReference().indexOf("=") + 1).trim();
                signalxml.setAttribute("lexeme", gestureID);

                TimeMarker start = parametric.getStart();
                String stringofstart = TimeMarker.convertTimeMarkerToSynchPointString(start, "0", true);

                TimeMarker ready = parametric.getTimeMarker("ready");
                String stringofready = TimeMarker.convertTimeMarkerToSynchPointString(ready, "0", true);
                if (start.isConcretized()) {
                    signalxml.setAttribute("start", stringofstart);
                } else {
                    signalxml.setAttribute("ready", stringofready);
                }


                TimeMarker end = parametric.getEnd();
                String stringofend = TimeMarker.convertTimeMarkerToSynchPointString(end, "0", true);

                TimeMarker relax = parametric.getTimeMarker("relax");
                String stringofrelax = TimeMarker.convertTimeMarkerToSynchPointString(relax, null, true);

                //backward compatibility
                if (endAsDuration) {
                    if (start.isConcretized() && end.isConcretized()) {
                        stringofend = TimeMarker.timeFormat.format(end.getValue() - start.getValue());
                    }
                }
                //end backward compatibility
                if (end.isConcretized()) {
                    if (stringofend != null) {
                        signalxml.setAttribute("end", stringofend);
                    }
                } else {
                    signalxml.setAttribute("relax", stringofrelax);
                }

                //descrition
                XMLTree description = signalxml.createChild("description");
                description.setAttribute("priority", "1");
                description.setAttribute("type", greta_s_description_type);
                //reference
                description.createChild("reference").addText(parametric.getReference());
                //intensity
                description.createChild("intensity").addText(numberFormat.format(parametric.getIntensity()));
                //expressivity parameters
                description.createChild("SPC.value").addText(numberFormat.format(parametric.getSPC()));
                description.createChild("TMP.value").addText(numberFormat.format(parametric.getTMP()));
                description.createChild("FLD.value").addText(numberFormat.format(parametric.getFLD()));
                description.createChild("PWR.value").addText(numberFormat.format(parametric.getPWR()));
                description.createChild("REP.value").addText(numberFormat.format(parametric.getREP()));

                description.createChild("OPN.value").addText(numberFormat.format(0.0));
                description.createChild("TEN.value").addText(numberFormat.format(0.0));
            }

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="head and headDirectionShift">
            //HEAD
            if (signal instanceof HeadSignal) {
                HeadSignal headSignal = (HeadSignal) signal;
                XMLTree signalxml = bml.createChild(headSignal.isDirectionShift() ? "headDirectionShift" : "head");
                signalxml.setAttribute("id", headSignal.getId());

                String lexeme = headSignal.isFilled() ? headSignal.getLexeme() : HeadLibrary.getGlobalLibrary().getLexemeOf(headSignal.getReference());
                signalxml.setAttribute(headSignal.isDirectionShift() ? "target" : "lexeme", lexeme == null ? headSignal.getReference() : lexeme);

                TimeMarker start = headSignal.getStart();
                if (start.isConcretized()) {
                    String stringofstart = TimeMarker.convertTimeMarkerToSynchPointString(start, "0", true);
                    signalxml.setAttribute("start", stringofstart);
                }

                TimeMarker end = headSignal.getEnd();
                if (end.isConcretized()) {
                    String stringofend = TimeMarker.convertTimeMarkerToSynchPointString(end, "0", true);

                    //backward compatibility
                    if (endAsDuration) {
                        if (start.isConcretized() && end.isConcretized()) {
                            stringofend = TimeMarker.timeFormat.format(end.getValue() - start.getValue());
                        }
                    }
                    //end backward compatibility

                    if (stringofend != null) {
                        signalxml.setAttribute("end", stringofend);
                    }
                }

                //descrition
                XMLTree description = signalxml.createChild("description");
                description.setAttribute("priority", "1");
                description.setAttribute("type", greta_s_description_type);
                //reference
                description.createChild("reference").addText(headSignal.getReference());
                //intensity
                description.createChild("intensity").addText(numberFormat.format(headSignal.getIntensity()));
                //expressivity parameters
                description.createChild("SPC.value").addText(numberFormat.format(headSignal.getSPC()));
                description.createChild("TMP.value").addText(numberFormat.format(headSignal.getTMP()));
                description.createChild("FLD.value").addText(numberFormat.format(headSignal.getFLD()));
                description.createChild("PWR.value").addText(numberFormat.format(headSignal.getPWR()));
                description.createChild("REP.value").addText(numberFormat.format(headSignal.getREP()));

                description.createChild("OPN.value").addText(numberFormat.format(0.0));
                description.createChild("TEN.value").addText(numberFormat.format(0.0));

            }

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="torso">
            //TORSO (exceptionel)
            if (signal instanceof TorsoSignal) {
                TorsoSignal parametric = (TorsoSignal) signal;
                XMLTree signalxml = bml.createChild("greta:torso", gretaNameSpace);

                signalxml.setAttribute("id", parametric.getId());

                String gestureID = parametric.getReference().substring(parametric.getReference().indexOf("=") + 1).trim();
                signalxml.setAttribute("lexeme", gestureID);

                TimeMarker start = parametric.getStart();
                String stringofstart = TimeMarker.convertTimeMarkerToSynchPointString(start, "0", true);
                signalxml.setAttribute("start", stringofstart);

                TimeMarker end = parametric.getEnd();
                String stringofend = TimeMarker.convertTimeMarkerToSynchPointString(end, "0", true);

                //backward compatibility
                if (endAsDuration) {
                    if (start.isConcretized() && end.isConcretized()) {
                        stringofend = TimeMarker.timeFormat.format(end.getValue() - start.getValue());
                    }
                }
                //end backward compatibility

                if (stringofend != null) {
                    signalxml.setAttribute("end", stringofend);
                }

                //descrition
                XMLTree description = signalxml.createChild("description");
                description.setAttribute("priority", "1");
                description.setAttribute("type", greta_s_description_type);
                //reference
                description.createChild("reference").addText(parametric.getReference());
                //intensity
                description.createChild("intensity").addText(numberFormat.format(parametric.getIntensity()));
                //expressivity parameters
                description.createChild("SPC.value").addText(numberFormat.format(parametric.getSPC()));
                description.createChild("TMP.value").addText(numberFormat.format(parametric.getTMP()));
                description.createChild("FLD.value").addText(numberFormat.format(parametric.getFLD()));
                description.createChild("PWR.value").addText(numberFormat.format(parametric.getPWR()));
                description.createChild("REP.value").addText(numberFormat.format(parametric.getREP()));

                description.createChild("OPN.value").addText(numberFormat.format(0.0));
                description.createChild("TEN.value").addText(numberFormat.format(0.0));

            }

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="gaze and gazeShift">
            //gaze
            if (signal instanceof GazeSignal) {
                GazeSignal gs = (GazeSignal) signal;
                XMLTree signalxml = bml.createChild(gs.isGazeShift() ? "gazeShift" : "gaze");
                gs.toXML(signalxml, endAsDuration);
            }

            //</editor-fold>
        }
        return bml;
    }
}
