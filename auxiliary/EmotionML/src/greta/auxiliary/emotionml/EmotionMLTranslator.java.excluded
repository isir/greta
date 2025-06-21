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
package greta.auxiliary.emotionml;

import greta.auxiliary.socialparameters.SocialDimension;
import greta.auxiliary.socialparameters.SocialParameterFrame;
import greta.core.intentions.EmotionIntention;
import greta.core.intentions.Intention;
import greta.core.util.IniManager;
import greta.core.util.time.TimeMarker;
import greta.core.util.time.Timer;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mathieu Chollet
 */
public class EmotionMLTranslator {

    /**
     * Don't let anyone instantiate this class.
     */
    private EmotionMLTranslator(){};

    static int numberOfEmotionsSent = 0;
    static int numberOfAttitudesSent = 0;

    /** To ensure backward compatibility with the tag end used as a duration.<br/>
     * {@code true} to use the old wrong format, {@code false} otherwise.<br/>
     * must be remove as soon as possible
     */
    private static final boolean endAsDuration = IniManager.getGlobals().getValueBoolean("FML_END_TAG_AS_DURATION");


    public static synchronized XMLTree SocialParametersToEmotionML(List<SocialParameterFrame> socialFrames) {
        XMLTree xmltree = XML.createTree("emotionML");
        for(SocialParameterFrame spf : socialFrames)
        {
            XMLTree spftree = xmltree.createChild("emotion");
            for(SocialDimension sd : SocialDimension.values())
            {
                XMLTree cat = spftree.createChild("category");
                cat.setAttribute("confidence", "1");
                cat.setAttribute("type", "Attitude");
                cat.setAttribute("name", sd.name());
                cat.setAttribute("value", String.valueOf(spf.getDoubleValue(sd)));
            }
        }
        return xmltree;
    }

    /**
     * Translates an XMLTree in EML format to a List of SocialParameterFrames (attitudes).
     * @param emotionml the XMLTree in EML
     * @return the List of SocialParameterFrames
     */
    public static synchronized List<SocialParameterFrame> EmotionMLToSocialParameters(XMLTree emotionml) {
        List<SocialParameterFrame> socialFrames = new ArrayList<SocialParameterFrame>();
         for(XMLTree emotionmlchild : emotionml.getChildrenElement()){
            if(emotionmlchild.getName().equalsIgnoreCase("emotion")){ //root tag
                SocialParameterFrame spf = new SocialParameterFrame(Timer.getCurrentFrameNumber());
                for(XMLTree category : emotionmlchild.getChildrenElement()){
                    String a_type = category.getAttribute("type");
                    if (a_type.equals("Attitude"))
                    {
                        numberOfAttitudesSent++;
                        String a_id = "eml-attitude"+String.valueOf(numberOfAttitudesSent);
                        double a_value = Double.valueOf(category.getAttribute("value"));
                        double a_confidence = Double.valueOf(category.getAttribute("confidence"));
                        String a_name = category.getAttribute("name");

                        if(a_name.equalsIgnoreCase("Friendliness")
                                || a_name.equalsIgnoreCase("Liking"))
                        {
                            spf.setDoubleValue(SocialDimension.Liking, a_value);
                        }
                        else if(a_name.equalsIgnoreCase("Dominance"))
                        {
                            spf.setDoubleValue(SocialDimension.Dominance, a_value);
                        }
                    }
                }
                socialFrames.add(spf);
            }
            //else{
                //person
            //}
        }
        return socialFrames;
    }


    /**
     * Translates an XMLTree in EML format to a List of Intentions (emotions).
     * @param emotionml the XMLTree in EML
     * @return the List of Intentions
     */
    public static synchronized List<Intention> EmotionMLToIntentions(XMLTree emotionml) {

        double maxEmotionValue = Double.MIN_VALUE;
        List<Intention> intentions = new ArrayList<Intention>();
         for(XMLTree emotionmlchild : emotionml.getChildrenElement()){
            //load the person
            if(emotionmlchild.getName().equalsIgnoreCase("emotion")){
                //emotions
                for(XMLTree category : emotionmlchild.getChildrenElement()){
                    if(!category.getAttribute("value").equals("0")) //we don't send emotions with 0 intensity
                    {
                        String e_type = category.getAttribute("type");
                        if(e_type.equals("Emotion"))
                        {
                            numberOfEmotionsSent++;
                            String e_id = "eml-emotion"+String.valueOf(numberOfEmotionsSent);
                            double e_value = Double.valueOf(category.getAttribute("value"));
                            if(e_value>maxEmotionValue) {
                                maxEmotionValue = e_value;
                            }
                            double e_confidence = Double.valueOf(category.getAttribute("confidence"));
                            String e_name = category.getAttribute("name");
                            TimeMarker e_start = new TimeMarker("start",0);
                            TimeMarker e_end = new TimeMarker("end",4.0);

                            Intention intention;
                            intention = new EmotionIntention(e_id,e_name.toLowerCase(),
                                    e_start,e_end, e_confidence, 1,e_value); // 1 is a default value for regulation (felt)

                            intentions.add(intention);
                        }
                    }

                }
            }
            //else{
                //person
            //}
        }

         ArrayList<Intention> returnedIntention = new ArrayList<Intention>();
         for (int i=0;i<intentions.size();i++)
         {
             if(((EmotionIntention) intentions.get(i)).getIntensity()==maxEmotionValue)
             {
                 returnedIntention.add(intentions.get(i));
                 break;
             }
         }
        return returnedIntention;
    }

}
