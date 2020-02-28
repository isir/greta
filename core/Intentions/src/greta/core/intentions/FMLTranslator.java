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
package greta.core.intentions;

import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.laugh.Laugh;
import greta.core.util.log.Logs;
import greta.core.util.speech.Boundary;
import greta.core.util.speech.PitchAccent;
import greta.core.util.speech.Speech;
import greta.core.util.time.TimeMarker;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;

/**
 * It contains methods to translate a list of Intentions into an XMLTree in FML
 * format, or translate an XMLTree in FML format into a list of Intentions.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because they are
 * UmlGraph tags, not javadoc tags.
 * @depend * "generates\nfrom Intentions" 1 greta.core.util.xml.XMLTree
 * @depend 1 "generates\nfrom XMLTree" * greta.core.intentions.Intention
 */
public class FMLTranslator {

    /**
     * Don't let anyone instantiate this class.
     */
    private FMLTranslator() {
    }

    public static Mode getDefaultFMLMode() {
        return new Mode(IniManager.getGlobals().getValueString("DEFAULT_FML_MODE"), CompositionType.blend);
    }

    /**
     * To ensure backward compatibility with the tag end used as a
     * duration.<br/> {@code true} to use the old wrong format, {@code false}
     * otherwise.<br/> must be remove as soon as possible
     */
    private static final boolean endAsDuration = IniManager.getGlobals().getValueBoolean("FML_END_TAG_AS_DURATION");

    /**
     * Translates an XMLTree in FML-APML format to a List of Intentions.
     *
     * @param fml the XMLTree in FML-APML
     * @return the List of Intentions
     */
    public static synchronized List<Intention> FMLToIntentions(XMLTree fml, CharacterManager cm) {
        List<Intention> intentions = new ArrayList<Intention>();
        for (XMLTree fmlchild : fml.getChildrenElement()) {
            //load the speeches, boundaries and pitch accents :
            if (fmlchild.isNamed("bml")) {
                for (XMLTree bmlchild : fmlchild.getChildrenElement()) {
                    if (bmlchild.getName().equalsIgnoreCase("speech")) {
                        //add speech :
                        PseudoIntentionSpeech speech = new PseudoIntentionSpeech(cm);
                        speech.readFromXML(bmlchild, endAsDuration);
                        intentions.add(speech);
                        //add boundaries :
                        for (Boundary b : speech.getBoundaries()) {
                            intentions.add(new PseudoIntentionBoundary(b));
                        }
                        //add pitch accents :
                        for (PitchAccent p : speech.getPitchAccent()) {
                            intentions.add(new PseudoIntentionPitchAccent(p));
                        }
                    }
                }
            } else if (fmlchild.isNamed("fml")) {
                for (XMLTree function : fmlchild.getChildrenElement()) {
                    if (!function.hasAttribute("id")) {
                        Logs.warning("FMLTranslator : the function " + function.getName() + " has not an id, it will be ignored.");
                        continue;
                    }
                    String f_id = function.getAttribute("id");
                    String f_type = function.getAttribute("type");
                    TimeMarker f_start = new TimeMarker("start");
                    if (function.hasAttribute("start")) {
                        f_start.addReference(function.getAttribute("start"));
                    }
                    TimeMarker f_end = new TimeMarker("end");
                    if (function.hasAttribute("end")) {

                        //backward compatibility
                        if (endAsDuration) {
                            try {
                                //we test if it's a number
                                double duration = Double.parseDouble(function.getAttribute("end"));//send an exception if it's not a number
                                //if we are here, no exception was send
                                f_end.addReference(f_id + ":start+" + duration);
                            } catch (Exception e) {
                                // it's not a number so it may be a sychpoint
                                f_end.addReference(function.getAttribute("end"));
                            }
                        } else //end backward compatibility
                        {
                            f_end.addReference(function.getAttribute("end"));
                        }
                    }
                    double f_importance = 0.5; //default value ?
                    if (function.hasAttribute("importance")) {
                        f_importance = function.getAttributeNumber("importance");
                    }

                    Intention intention;

                    //emotion and world functions have more information than other functions
                    if (function.isNamed("emotion")) {
                        //get specificities of emotion functions
                        int e_regulation = EmotionIntention.stringToRegulation(function.getAttribute("regulation"));
                        double e_intensity = 1;
                        if (function.hasAttribute("intensity")) {
                            e_intensity = function.getAttributeNumber("intensity");
                        }
                        intention = new EmotionIntention(f_id, f_type, f_start, f_end, f_importance, e_regulation, e_intensity);
                    } else if (function.isNamed("certainty")) {
                        double c_intensity = 1;
                        if (function.hasAttribute("intensity")) {
                            c_intensity = function.getAttributeNumber("intensity");
                        }
                        intention = new CertaintyIntention(f_id, f_type, f_start, f_end, f_importance, c_intensity);
                    } else if (function.isNamed("world")) {
                        //get specificities of world functions
                        WorldIntention world = new WorldIntention(f_id, f_start, f_end, f_importance);
                        world.setRefType(function.getAttribute("ref_type"));
                        world.setRefId(function.getAttribute("ref_id"));
                        world.setPropType(function.getAttribute("prop_type"));
                        world.setPropValue(function.getAttribute("prop_value"));
                        world.setTarget(function.getAttribute("target"));
                        intention = world;
                    } else if (function.isNamed("laugh")) {
                        LaughIntention laugh = new LaughIntention(f_id, f_start, f_end);
                        if (function.hasAttribute("intensity")) {
                            laugh.setIntensity(function.getAttributeNumber("intensity"));
                        }
                        intention = laugh;
                    } else if (function.isNamed("ideationalunit")) {
                        //get specificities of ideational units
                        IdeationalUnitIntention ideationalUnit = new IdeationalUnitIntention(f_id, f_start, f_end);
                        ideationalUnit.setMainIntentionId(function.getAttribute("main"));
                        intention = ideationalUnit;
                    } else {
                        intention = null;
                        // check the type of the function (f_type)
                        // if the f_tyoe is empty that could be a deictic intention with only the target to gaze so we
                        // don't need to create an intention because we have just a geze signal created after
                        if (f_type != ""){
                            //juste instanciate other functions
                            intention = new BasicIntention(function.getName(), f_id, f_type, f_start, f_end, f_importance);
                        }
                        if (function.getName().equals("deictic")){
                            String targ = function.getAttribute("target");
                            if (targ != null || targ != ""){
                                BasicIntention intent = new BasicIntention (function.getName(), f_id, f_type, f_start, f_end, f_importance);
                                intent.setTarget(targ);
                                intention = intent;
                            }
                        }
                    }

                    if (function.hasAttribute("character") && intention instanceof BasicIntention) {
                        ((BasicIntention) intention).setCharacter(function.getAttribute("character"));
                    }
                    intentions.add(intention);
                }
            }
        }
        return intentions;
    }

    /**
     * Translates a List of Intentions to an XMLTree in FML-APML format.
     *
     * @param intentions the List of Intentions
     * @param mode blend, replace, append
     * @return the XMLTree in FML-APML
     */
    public static synchronized XMLTree IntentionsToFML(List<Intention> intentions, Mode mode) {
        XMLTree fmlapml = XML.createTree("fml-apml");
        fmlapml.setAttribute("composition", mode.getCompositionType().toString());
        fmlapml.setAttribute("reaction_type", mode.getReactionType().toString());
        fmlapml.setAttribute("reaction_duration", mode.getReactionDuration().toString());
        fmlapml.setAttribute("social_attitude", mode.getSocialAttitude().toString());
        XMLTree bml = fmlapml.createChild("bml"); //contains speech
        XMLTree fml = fmlapml.createChild("fml"); //contains fml functions
        for (Intention intention : intentions) {

            if (intention instanceof Speech) {
                bml.addChild(((Speech) intention).toXML(endAsDuration));
            }

            if (intention instanceof Laugh) {
                XMLTree function = fml.createChild(intention.getName());
                function.setAttribute("id", intention.getId());

                TimeMarker start = intention.getStart();
                String stringofstart = TimeMarker.convertTimeMarkerToSynchPointString(start, "0", true);
                function.setAttribute("start", stringofstart);

                TimeMarker end = intention.getEnd();
                String stringofend = TimeMarker.convertTimeMarkerToSynchPointString(end, null, true);

                if (stringofend != null) {
                    function.setAttribute("end", stringofend);
                }

                function.setAttribute("intensity", "" +((Laugh)intention).getIntensity());
            }

            //must skip boundaries and pitch accents : they are in speech

            if (intention instanceof BasicIntention) {
                XMLTree function = fml.createChild(intention.getName());
                function.setAttribute("id", intention.getId());

                TimeMarker start = intention.getStart();
                String stringofstart = TimeMarker.convertTimeMarkerToSynchPointString(start, "0", true);
                function.setAttribute("start", stringofstart);

                TimeMarker end = intention.getEnd();
                String stringofend = TimeMarker.convertTimeMarkerToSynchPointString(end, null, true);

                //backward compatibility
                if (endAsDuration) {
                    if (start.isConcretized() && end.isConcretized()) {
                        stringofend = TimeMarker.timeFormat.format(end.getValue() - start.getValue());
                    }
                }
                //end backward compatibility

                if (stringofend != null) {
                    function.setAttribute("end", stringofend);
                }

                if (intention instanceof EmotionIntention) {
                    //add specific informations about emotions
                    function.setAttribute("intensity", "" + ((EmotionIntention) intention).getIntensity());
                    function.setAttribute("regulation", EmotionIntention.regulationToString(((EmotionIntention) intention).getRegulation()));
                }
                if (intention instanceof WorldIntention) {
                    //add specific informations about world
                    WorldIntention world = (WorldIntention) intention;
                    function.setAttribute("ref_type", world.getRefType());//#REQUIRED
                    function.setAttribute("ref_id", world.getRefId());//#REQUIRED
                    if (!world.getPropType().isEmpty()) {
                        function.setAttribute("prop_type", world.getPropType());//#IMPLIED
                    }
                    if (!world.getPropValue().isEmpty()) {
                        function.setAttribute("prop_value", world.getPropValue());//#IMPLIED
                    }
                }
                if (intention instanceof IdeationalUnitIntention) {
                    //add specific informations about ideational units
                    IdeationalUnitIntention ideationalUnit = (IdeationalUnitIntention) intention;
                    function.setAttribute("main", ideationalUnit.getMainIntentionId());//#REQUIRED
                }

                //emphasis has not importance in fml-apml.dtd
                //all other functions have it
                if (!intention.getName().equalsIgnoreCase("emphasis")) {
                    function.setAttribute("importance", "" + intention.getImportance());
                }

                //theme, rheme and world have not type in fml-apml.dtd
                //all other functions have it
                if (!(intention.getName().equalsIgnoreCase("theme")
                        || intention.getName().equalsIgnoreCase("rheme")
                        || intention.getName().equalsIgnoreCase("world"))) {
                    function.setAttribute("type", intention.getType());
                }

                if (intention.hasCharacter()) {
                    function.setAttribute("character", intention.getCharacter());
                }
            }
        }
        return fmlapml;
    }
}
