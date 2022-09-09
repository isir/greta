/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.multilayerattitudeplanner.structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import greta.core.signals.Signal;


/**
 *
 * @author Mathieu
 */
public enum NVBEventType {
    HeadAt(),
    HeadTilt("HeadTiltL"),
    HeadDown("HeadDownL"),
    HeadUp("HeadUpH"),
    HeadSide("HeadSideH"),
    HeadNod("HeadNodLN"),
    HeadShake("HeadShakeNN"),
    RestArmsCrossed(),
    RestHandsTogether(),
    RestOver(),
    RestUnder(),
    EyebrowUp("EyebrowUpL"),
    EyebrowDown("EyebrowDownL"),
    GestComm("GestCommLL"),
    GestAdaptorArms(),
    GestObjectManipulation(),
    Smile("SmileL"),
    BodyStraight(),
    BodyLean("BodyLeanL"),
    BodyRecline("BodyReclineL"),
    Any;
    
    private String alias;
    
    private NVBEventType()
    {
        this.alias = "noalias!!!!!!§";
    }
    
    private NVBEventType(String alias)
    {
        this.alias = alias;
    }
    
    public static NVBEventType getNVBEventTypeByString(String str)
    {
        for(NVBEventType nvbet : values())
        {
            if(nvbet.equals(str))
                return nvbet;
        }
        return null;
    }
    
    public boolean equals(String str)
    {
        if(str !=null)
        {
            if(str.equalsIgnoreCase(alias) ||str.equalsIgnoreCase(this.toString()))
                return true;
        }
        return false;
    }
        
    public static List<NVBEventType> readListString(List<String> liststr)
    {
        List<NVBEventType> returnedList = new ArrayList<NVBEventType>();
        for(String s : liststr)
        {   
            NVBEventType nvbev = getNVBEventTypeByString(s);
            if (nvbev!=null)
                returnedList.add(nvbev);
        }
        return returnedList;
    }
    
    public static List<NVBEventType> readArrayString(String[] liststr)
    {
        return readListString(Arrays.asList(liststr));
    }
    
    public static boolean isSubsequence(List<NVBEventType> evaluatedsequence ,List<NVBEventType> possibletiming)
    {
        
        
       if(evaluatedsequence.size()>possibletiming.size())
        {
            return false;
        }
       else 
        {
            int j=0;//possible timing index
            for(int i=0;i<evaluatedsequence.size();i++)
            {
                boolean found=false;
                while(!found)
                {
                    if(j>=possibletiming.size())
                    {
                        return false;
                    }
                    if(evaluatedsequence.get(i).equals(possibletiming.get(j))
                            || possibletiming.get(j).equals(NVBEventType.Any))
                    {
                        j++;
                        found=true;
                    }
                    else
                    {
                        j++;
                    }
                }
            }
            //System.out.println(evaluatedsequence.toString());
            return true;
        }
           /* int i=0;
            int numberOfAnys=0;
            for(int j=0;j<possibletiming.size();j++)
            {
                    if(possibletiming.get(j).equals(NVBEventType.Any))
                    {
                        numberOfAnys++;
                        continue;
                    }
                    if(evaluatedsequence.get(i).equals(possibletiming.get(j)))
                    {
                        i++;
                    }
                    else 
                    {
                        while(numberOfAnys>0)
                        {
                            numberOfAnys--;
                            i++;
                            if(i>=evaluatedsequence.size())
                            {
                                //verifier !
                                return true;
                            }
                            if(evaluatedsequence.get(i).equals(possibletiming.get(j)))
                            {
                                continue;
                            }
                        }
                        return false;
                    }
                    if(i>=evaluatedsequence.size())
                    {
                        //verifier !
                        return true;
                    }
            }
            return true;
            
        
        }*/
       
    }
    
    public static boolean isSameModality(NVBEventType nvbev, Signal s)
    {
        if(s.getModality().equalsIgnoreCase("face")
                && (nvbev.equals(EyebrowDown) || nvbev.equals(EyebrowUp) || nvbev.equals(Smile)))
        {
            return true;
        }
        else if(s.getModality().equalsIgnoreCase("head")
                && (nvbev.equals(HeadAt) || nvbev.equals(HeadDown) || nvbev.equals(HeadNod)
                || nvbev.equals(HeadShake) || nvbev.equals(HeadSide) || nvbev.equals(HeadTilt) 
                || nvbev.equals(HeadUp)))
        {
            return true;
        }
        else if(s.getModality().equalsIgnoreCase("gesture")
                && (nvbev.equals(GestAdaptorArms) || nvbev.equals(GestComm) || nvbev.equals(GestObjectManipulation)
                 || nvbev.equals(RestArmsCrossed) || nvbev.equals(RestHandsTogether) 
                || nvbev.equals(RestOver) || nvbev.equals(RestUnder)))
        {
            return true;
        }
        else if(s.getModality().equalsIgnoreCase("torso")
                && (nvbev.equals(BodyLean) || nvbev.equals(BodyRecline) || nvbev.equals(BodyStraight)))
        {
            return true;
        }
        return false;
    }
    
}
