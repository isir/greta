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
package greta.core.behaviorplanner.mseconstraints;

import greta.core.util.IniManager;
import greta.core.util.log.Logs;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.HashMap;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class ConstraintSetsContainer {

    //typedef std::map<std::string,MultimodalEmotionConstraint> MultimodalEmotionConstraints;
    //private	MultimodalEmotionConstraintSet mecs;
    //OLD MultimodalEmotionConstraint
    //Hashtable<String, MultimodalEmotionConstraintSet> mecs = new Hashtable<String, MultimodalEmotionConstraintSet>();
    /**
     * contructor
     *
     */
    public ConstraintSetsContainer() {
    }

    /**
     *
     * translates a parses node to constraint node
     *
     * @return a tree of constraints
     * @param a node of parsing tree
     */
//private	ConsNode translate(XMLGenericTree con_iter){}
    /**
     *
     * parses behavior and constraint sets
     *
     * @return
     * @param file_name
     */
//private int init (String file_name){}
    /**
     *
     *
     *
     * @return
     */
//TO DO: change to HashMap
//Hashtable<String, MultimodalEmotionConstraintSet> getMultimodalEmotionConstraints() {return this.mecs;}
    /**
     *
     *
     *
     */
    void printAll() {
    }

    /**
     *
     *
     *
     * @return @param emotion
     * @param std::string signalname
     */
//float getRepetivity(String emotion, String signalname){}
    /**
     *
     *
     *
     * @return @param emotion
     * @param std::string signalname
     */
//float getProbability_start(String emotion, String signalname){}
    /**
     *
     *
     *
     * @return @param emotion
     * @param std::string signalname
     */
//float getProbability_end(String emotion, String signalname){}
    /**
     *
     *
     *
     * @return @param emotion
     * @param std::string signalname
     */
//float getOccurence(String emotion, String signalname){}
    /**
     *
     *
     *
     * @return @param emotion
     * @param std::string signalname
     */
//float getMax_duration(String emotion, String signalname){}
    /**
     *
     *
     *
     * @return @param emotion
     */

    /*
     *
     * WE ARE HERE
     *
     MultimodalEmotionConstraint getMultimodalEmotionConstraint(String emotion){

     if(mecs.isEmpty()==false)

     {

     for (MultimodalEmotionConstraintSet iter : mecs )
     //std::map<std::string,MultimodalEmotionConstraint>::const_iterator iter;

     //for(iter=mecs.begin();iter!=mecs.end();iter++)
     {
     String current_emotion_name = (*iter).first;

     MultimodalEmotionConstraint mec = &(mecs[iter->first]);

     if (current_emotion_name.equalsIgnoreCase(emotion)==0) return mec;

     }

     }

     return null;
     }
     *
     */
    public HashMap<String, MultimodalEmotionConstraintSet> initiation() {
        HashMap<String, MultimodalEmotionConstraintSet> map = new HashMap<String, MultimodalEmotionConstraintSet>();

        XMLParser parser = XML.createParser();
        String csFile = IniManager.getGlobals().getValueString("CONSTRAINT_SET");

        if (csFile.equals("")) {
            Logs.warning("ConstraintsContainer: no fileName given");
            return null;
            //throw Exception("ConstraintsContainer: no fileName given",1);
        }

        XMLTree constraintsets = parser.parseFile(csFile);

        if (constraintsets != null) {

            // child is "multimodal" with a name
            for (XMLTree cs_in_file : constraintsets.getChildrenElement()) {


                if (cs_in_file.getName().equalsIgnoreCase("multimodal")) {

                    String emotion_name = "";
                    //TO DO: change name of parameter "emotion" -> "name"
                    if (cs_in_file.hasAttribute("emotion") == false) {
                        Logs.warning("Multimodal noname ");
                        //TO DO: skip the rest of the loop
                    } else {
                        emotion_name = cs_in_file.getAttribute("emotion");
                    }

                    MultimodalEmotionConstraintSet cs = new MultimodalEmotionConstraintSet();
                    cs.setName(emotion_name);

                    for (XMLTree con : cs_in_file.getChildrenElement()) {

                        ConsNode constree = translate(con);
                        cs.addNewConstraint(constree);

                    }//end of for

                    //mecs.put(cs.getName(),cs);
                    map.put(cs.getName(), cs);

                }//END IF multimodal

            }//END FOR

        }//END IF
        else {
            //constraint set is empty - send a worning message


            return null;
        }

        return map;

    }//end of init

    private ConsNode translate(XMLTree con_iter) {

        //init constree
        ConsNode constree = new ConsNode();

        //if the node is arg
        if (con_iter.getName().equalsIgnoreCase("arg")) {

            //id and type
            int temp_id = -1; //default no id

            String temp = "";

            if (con_iter.hasAttribute("id")) {
                temp = con_iter.getAttribute("id");
            }

            if (!temp.equals("")) {
                //System.out.println(temp);
                Integer temp2 = new Integer(temp);
                //System.out.println(temp2);
                temp_id = temp2.intValue();
                // if "" means no id defined
            }

            temp = "";
            if (con_iter.hasAttribute("type")) {
                temp = con_iter.getAttribute("type");
            }


            if (!temp.equals("")) {
                constree.setArg(temp_id, temp);
            } else {
                constree.setArg(temp_id, "none");
            }

            //value
            //TO DO: and if there is no value?
            float temp_value = -1;
            if (con_iter.hasAttribute("value")) {
                temp_value = (float) con_iter.getAttributeNumber("value");
            }
            constree.setValue(temp_value);


            //operator
            constree.setConsNodeOperator("unkown");

        }//end if arg


        //if the node is con
        if (con_iter.getName().equalsIgnoreCase("con")) {

            //set operator
            String tymczas = "";

            if (con_iter.hasAttribute("type")) {
                tymczas = con_iter.getAttribute("type");
            }
            if (!(tymczas.equals(""))) {
                constree.setConsNodeOperator(tymczas);
            } else {
                constree.setConsNodeOperator("unkown");
            }

            //set label
            tymczas = "";
            if (con_iter.hasAttribute("label")) {
                tymczas = con_iter.getAttribute("label");
            }
            if (!(tymczas.equals(""))) {
                constree.id = tymczas;
            } else {
                constree.id = "unkown";
            }

            //id and type
            constree.setArg(-1, "none");  //default no id, no type

            //value
            constree.setValue(-1); //default no value

        }//end if con


        //check if the code is correct in both cases "arg" and "con"

        //par default no sons
        constree.setLeft(null);
        constree.setRight(null);

        // take children
//	std::list<XMLGenericTree*> child = (*con_iter).child;

        //if there are some
//	if(!child.empty())
//	{

        XMLTree chld;

        int counter = 0;

        for (XMLTree iter : con_iter.getChildrenElement()) {

            chld = iter;
            if (chld.isTextNode()) {
                continue;
            }

            //take a child..

            //is it first or second?
            if (counter == 0) {
                constree.setLeft(translate(chld));
            }
            if (counter == 1) {
                constree.setRight(translate(chld));
            }
            if (counter > 1)

                            //TO DO: warning: to much chld
				;

            counter++;

        }//end for

        //	}//empty child

        return constree;

    }//end of translate
}//end of class
