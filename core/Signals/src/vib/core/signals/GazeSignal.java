/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.signals;

import java.util.ArrayList;
import java.util.List;
import vib.core.repositories.AUItem;
import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.enums.GazeDirection;
import vib.core.util.enums.Influence;
import vib.core.util.enums.Side;
import vib.core.util.time.TimeMarker;
import vib.core.util.xml.XMLTree;

/**
 * This class contains informations about gaze Signals.
 * @author Andre-Marie Pez
 * @author Mathieu Chollet
 */

public class GazeSignal extends ParametricSignal implements SignalTargetable, CharacterDependent {

    private String id;
    private TimeMarker start;
    private TimeMarker ready;
    private TimeMarker relax;
    private TimeMarker end;
    private List<TimeMarker> timeMarkers;
    private ArrayList<AUItem> actionUnits;

    private boolean shift = false;

    private String origin;
    private String target;
    private Influence influence;
    private GazeDirection offsetDirection = null;
    private Double offsetAngle = null;
    private boolean isScheduled = false;
    
    private CharacterManager characterManager;
    
    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        if(characterManager==null)
            characterManager = CharacterManager.getStaticInstance();
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        this.characterManager = characterManager;
    }
    
    @Override
    public void onCharacterChanged() {
        //set the current library to use :
        setOrigin(getCharacterManager().currentCharacterId);
    }

    public GazeSignal(String id){
        this.actionUnits = new ArrayList<AUItem>();
        this.id = id;
        timeMarkers = new ArrayList<TimeMarker>(4);
        start = new TimeMarker("start");
        timeMarkers.add(start);
        ready = new TimeMarker("ready");
        timeMarkers.add(ready);
        relax = new TimeMarker("relax");
        timeMarkers.add(relax);
        end = new TimeMarker("end");
        timeMarkers.add(end);

        origin=getCharacterManager().currentCharacterId;
        target="";
        offsetDirection=GazeDirection.FRONT;
        offsetAngle=0.0;
    }

    public boolean isGazeShift() {
        return shift;
    }
    
    @Override
    public void setReference(String reference) {
        if(reference.startsWith("look@")){
            int sharpPos = reference.indexOf("#");
            if(sharpPos>5){
                setTarget(reference.substring(5, sharpPos));
                setInfluence(Influence.valueOf(reference.substring(sharpPos+1).toUpperCase()));
            }
            else{
                setTarget(reference.substring(5));
            }
        }
        super.setReference(reference);
    }

    public void setGazeShift(boolean isShift) {
        this.shift = isShift;
    }

    @Override
    public String getModality() {
        return "gaze";
    }

    @Override
    public List<TimeMarker> getTimeMarkers() {
        return timeMarkers;
    }

    @Override
    public TimeMarker getTimeMarker(String name) {
        if(name.equalsIgnoreCase("start"))
            return start;
        if(name.equalsIgnoreCase("ready"))
            return ready;
        if(name.equalsIgnoreCase("relax"))
            return relax;
        if(name.equalsIgnoreCase("end"))
            return end;
        return null;
    }

    public double getStartValue(){
        return this.start.getValue();
    }

    public double getEndVale(){
        return this.end.getValue();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void schedule() {
        //TODO this is temporary !!
        //if there are no time markers for ready and relax, should we compute
        // the time values based on influence ? (can be a rough approximation
        // of angle)
        if(!start.isConcretized())
        {
            start.setValue(0.0); //assume immediate gaze
        }
        if(!isGazeShift())
        {
            if(!end.isConcretized())
            {
                return ;
            }
            //TODO : change depending on influence ?
            /*if(influence==Influence.EYES || influence == Influence.HEAD || influence == Influence.SHOULDER || influence == Influence.TORSO ) {*/
                if(!ready.isConcretized() && !relax.isConcretized())
                {
                    double totalTime = this.end.getValue() - this.start.getValue();
                    ready.setValue(start.getValue()+totalTime/3);
                    relax.setValue(ready.getValue()+totalTime/3);
                }
                else if(ready.isConcretized() && !relax.isConcretized())
                {
                    relax.setValue(ready.getValue()+(end.getValue()-ready.getValue())/2);
                }
                else if(!ready.isConcretized() && relax.isConcretized())
                {
                    ready.setValue(start.getValue()+(relax.getValue()-start.getValue())/2);
                }
            isScheduled=true;
            /*}
            else
            {
                //TODO unimplemented yet
            }*/
        }
        else
        {
            if(!end.isConcretized())
            {
                end.setValue(Double.MAX_VALUE);
            }
            if(!relax.isConcretized())
            {
                relax.setValue(Double.MAX_VALUE);
            }
            if(!ready.isConcretized())
            {
                //TODO : change depending on influence ?
                if(influence==Influence.TORSO ) {
                    ready.setValue(start.getValue()+1.5);
                }
                else if( influence == Influence.HEAD) {
                    ready.setValue(start.getValue()+0.75);
                }
                else  if(influence==Influence.EYES) {
                    ready.setValue(start.getValue()+0.33);
                }
            }
            isScheduled=true;
        }
    }

    /**
     * @param target the target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

     /**
     * @return the target
     */
    public String getTarget()    {
        return target;
    }

    /**
     * @param origin the target to set
     */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

     /**
     * @return the target
     */
    public String getOrigin()    {
        return origin;
    }

    /**
     * @return the influence
     */
    public Influence getInfluence() {
        return influence;
    }

    /**
     * @param influence the influence to set
     */
    public void setInfluence(Influence influence) {
        this.influence = influence;
    }

    /**
     * @return the offsetDirection
     */
    public GazeDirection getOffsetDirection() {
        return offsetDirection;
    }

    /**
     * @param offsetDirection the offsetDirection to set
     */
    public void setOffsetDirection(GazeDirection offsetDirection) {
        this.offsetDirection = offsetDirection;
    }

    /**
     * @return the offsetAngle
     */
    public double getOffsetAngle() {
        return offsetAngle;
    }

    /**
     * @param offsetAngle the offsetAngle to set
     */
    public void setOffsetAngle(double offsetAngle) {
        this.offsetAngle = offsetAngle;
    }


    public void readFromXML(XMLTree tree, boolean endAsDuration) {
        //target
        if(tree.hasAttribute("target")) {
            setTarget(tree.getAttribute("target"));
        }
        //origin
        if(tree.hasAttribute("origin")) {
            setOrigin(tree.getAttribute("origin"));
        }
        //influence
        if(tree.hasAttribute("influence")) {
            String xmlinfluence= tree.getAttribute("influence");
            //maybe these checks should be in behavior realizer instead ?
            if(correctInfluence(xmlinfluence))
            {
                setInfluence(Influence.valueOf(xmlinfluence));
            }
            else
            {
                //unrecognized influence : should throw exception !
                // Default null. the influence will be calculated in gazekeyframeGenerator class automaticaly,
                // according to the rotation anlge to reaéch the target object
                setInfluence(null); //
                //default : eyes
                //setInfluence(Influence.EYES);
            }
        }
        //offset
        if(tree.hasAttribute("offsetDirection")) {
            String xmloffdirection = tree.getAttribute("offsetDirection");
            if(correctGazeDirection(xmloffdirection))
            {
                setOffsetDirection(GazeDirection.valueOf(xmloffdirection));
            }
            else
            {
                //unrecognized direction : should throw exception !
                //default : front
                setOffsetDirection(null);
            }
        }
        if(tree.hasAttribute("offsetAngle") && offsetDirection != null) {
            setOffsetAngle(Double.parseDouble(tree.getAttribute("offsetAngle")));
        }
        //sync attributes
        if (tree.hasAttribute("start")) {
            start.addReference(tree.getAttribute("start"));
        }
        if (tree.hasAttribute("ready")) {
            ready.addReference(tree.getAttribute("ready"));
        }
        if (tree.hasAttribute("relax")) {
            relax.addReference(tree.getAttribute("relax"));
        }
        if (tree.hasAttribute("end")) {
            end.addReference(tree.getAttribute("end"));
        }
    }

    public void toXML(XMLTree tree, boolean endAsDuration)
    {
        tree.setAttribute("id", id);
        if(target!=null) {
            tree.setAttribute("target", target);
        }
        if(origin!=null) {
            tree.setAttribute("origin", origin);
        }
        if(influence!=null) {
            tree.setAttribute("influence", influence.name());
        }
        if(offsetAngle!=null) {
            tree.setAttribute("offsetAngle", offsetAngle.toString());
        }
        if(offsetDirection!=null) {
            tree.setAttribute("offsetDirection", offsetDirection.name());
        }
        String stringofstart = TimeMarker.convertTimeMarkerToSynchPointString(start, "0", true);
        tree.setAttribute("start", stringofstart);
        
        
        String stringofend = TimeMarker.convertTimeMarkerToSynchPointString(end, "0", true);
                
        //backward compatibility
        if (endAsDuration) {
            if (start.isConcretized() && end.isConcretized()) {
                stringofend = TimeMarker.timeFormat.format(end.getValue() - start.getValue());
            }
        }
        //end backward compatibility
        if (stringofend != null) {
            tree.setAttribute("end", stringofend);
        }

        String stringofready = TimeMarker.convertTimeMarkerToSynchPointString(ready, null, true);
        String stringofrelax = TimeMarker.convertTimeMarkerToSynchPointString(relax, null, true);
        if (stringofready != null) {
            tree.setAttribute("ready", stringofready);
        }
        if (stringofrelax != null) {
            tree.setAttribute("relax", stringofrelax);
        }
    }

    private boolean correctInfluence(String xmlInfluence) {
        for (Influence i : Influence.values()) {
            if (i.name().equals(xmlInfluence)) {
                return true;
            }
        }
        return false;
    }

    private boolean correctGazeDirection(String xmlGazeDirection) {
        for (GazeDirection g : GazeDirection.values()) {
            if (g.name().equals(xmlGazeDirection)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the isScheduled
     */
    public boolean isScheduled() {
        return isScheduled;
    }


    public void addAU(String name){
            actionUnits.add(new AUItem(name));
    }

     /**
     *
     * @param side
     * @return the list of action units used in this facial expression
     */
    public ArrayList<AUItem> getActionUnits(Side side) {
        ArrayList<AUItem> onesideAUs = new ArrayList<AUItem>();
        for (AUItem auitem : actionUnits) {
            if ((auitem.getSide() == Side.BOTH)
                    || (auitem.getSide() == side)) {
                AUItem new_auitem = new AUItem(auitem.getAU(), auitem.getIntensity(), side);
                onesideAUs.add(new_auitem);
            }//end of if
        }//end of for
        return onesideAUs;
    }//end of method

     public ArrayList<AUItem> getActionUnits() {
        return actionUnits;
    }//end of method
    
    /**
     * Adds a {@code AUItem} in the face library item.<br/>
     *
     * @param item the {@code AUItem} to add in the set
     */
    public void add(AUItem item) {
        if (item == null) {
            return; //never add a null objec
        }
        actionUnits.add(item);
    }//end of method

    @Override
    public TimeMarker getStart() {
        return start;
    }

    @Override
    public TimeMarker getEnd() {
        return end;
    }
    
    public void setModality(String modality) {
    }
}
