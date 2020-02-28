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
package greta.core.util.time;

/**
 * This class is use by the TimeMarker class to make reference to an other TimeMarkers.<br/>
 * The reference can be made with an instance of TimeMarker or a synch point (String).<br/>
 * You need to use a Temporizer to convert String refernce to TimeMarker reference.
 * @see greta.core.util.time.Temporizer Temporizer
 * @see greta.core.util.time.TimeMarker TimeMarker
 * @author Andre-Marie Pez
 */
public class SynchPoint {

    /**
     * @param offset the offset to set
     */
    public void setOffset(double offset) {
        this.offset = offset;
    }

    /**
     * Time constraint : is around a time but not after
     */
    public static final byte BEFORE = -1;

    /**
     * Time constraint : is at a time, not before neither after
     */
    public static final byte AT = 0;

    /**
     * Time constraint : is around a time but not before
     */
    public static final byte AFTER = 1;

    /**
     * Time constraint : is around a time
     */
     public static final byte AROUND = 2;


    private TimeMarker target;
    /** This is the information to find target.<br/>
     * Its format must be : Temporizable's id + ":" + TimeMarker's name.<br/>
     * i.e. "face1:start", "gesture14:stroke" etc.
     */
    private String targetName;
    private double offset;
    private byte constraint; //BEFORE, AT, AFTER or AROUND

    /**
     * Construct a reference to a TimeMarker without offset.
     * @param timeMarker the TimeMarker to refer
     */
    public SynchPoint(TimeMarker timeMarker){
        this(timeMarker, 0);
    }

    /**
     * Construct a reference to a TimeMarker with an offset from it.
     * @param timeMarker the TimeMarker to refer
     * @param offset the offset
     */
    public SynchPoint(TimeMarker timeMarker, double offset){
        setTimeMarker(timeMarker,offset);
        constraint = AT;
    }

    /**
     * Construct a synch point with a string.<br/>
     * The string must describes a reference to an other TimeMarker.<br/>
     * four forms are aviable:<br/>
     * an absolute time : {@code 5.2}<br/>
     * only the name of the target TimeMarker : {@code "timeMarkerName"}<br/>
     * the name of the target TimeMarker plus or minus an offset : {@code "timeMarkerName + 0.53"}<br/>
     * a synch point with a time constraint :
     * {@code "after(aSynchPoint)"} (means that add an unknown offset upper or equals 0),
     * or {@code "before(aSynchPoint)"} (means that add an unknown offset lower or equals 0)<br/>
     * @param synchPoint the synch point
     */
    public SynchPoint(String synchPoint){
        this(null,0);
        parse(synchPoint);
    }

    /**
     * Returns {@code true} if the TimeMarker to refer is concretized.<br/>
     * Note : a refer to a string may not be concretized. It must be convert by a Temporizer before.
     * @return if the TimeMarker to refer is concretized
     * @see greta.core.util.time.TimeMarker#isConcretized() TimeMarker.isConcretized()
     * @see greta.core.util.time.Temporizer Temporizer
     */
    public boolean isConcretized(){
        return (target!=null && target.isConcretized()) || (target==null && targetName==null);
    }

    /**
     * Returns the offset from the TimeMarker to refer.
     * @return the offset from the TimeMarker to refer
     */
    public double getOffset(){
        return offset;
    }

    /**
     * Returns the time value of the TimeMarker to refer plus the offset.
     * @return the time value of the TimeMarker to refer plus the offset
     */
    public double getValue(){
        return (target != null ? (target.equals(TimeMarker.INFINITY) ? TimeMarker.INFINITY.getValue() : target.getValue()+offset) : offset);
    }

    /**
     * Returns the name of the target {@code TimeMarker}.<br/>
     * If this does not refer to a TimeMarker, it returns {@code null}.
     * @return the synch point reference
     */
    public String getTargetName(){
        return targetName;
    }

    public void setTargetName(String targetName){
        this.targetName = targetName;
    }

     /**
     * Returns the target {@code TimeMarker} of this {@code SynchPoint}.
     * @return the synch point reference
     */
    public TimeMarker getTarget(){
        return target;
    }

    /**
     * Returns {@code true} if this refer to a TimeMarker.<br/>
     * {@code false} otherwise.
     * @return if this refer to a synch point
     */
    public boolean hasTargetTimeMarker(){
        return targetName!=null || target!=null;
    }

    /**
     * Sets the TimeMarker to refer with an offset from it.<br/>
     * @param timeMarker the TimeMarker to refer
     * @param offset the offset
     */
    public void setTimeMarker(TimeMarker timeMarker, double offset){
        this.target = timeMarker;
        this.setOffset(offset);
    }

    /**
     * Sets the TimeMarker to refer.<br/>
     * @param timeMarker the TimeMarker to refer
     */
    public void setTimeMarker(TimeMarker timeMarker){
        this.target = timeMarker;
    }

    @Override
    public String toString(){
        //TODO this.target ?
        String toReturn = targetName;
        if(toReturn==null) {
            toReturn = "" + Double.toString(offset);
        }
        else{
            if(offset>=0) {
                toReturn += " + " + Double.toString(offset);
            }
            if(offset<0) {
                toReturn += Double.toString(offset);
            }
        }

        if(constraint==BEFORE) {
            toReturn = "before( "+toReturn+" )";
        }
        if(constraint==AFTER) {
            toReturn = "after( "+toReturn+" )";
        }
        //complete with around (not in BML norm) ?
        /*
        if(constraint==AROUND)
            toReturn = "around( "+toReturn+" )";
        //*/
        return toReturn;
    }

    private void parse(String synchPoint){
        //check time constraint :
        constraint = AT;
        if(synchPoint.contains("around(")) {
            constraint = AROUND;
        }
        else{
            if(synchPoint.contains("before(")){
                if(synchPoint.contains("after(")){
                    constraint = AROUND; // or AT ?
                }
                else {
                    constraint = BEFORE;
                }
            }
            else{
                if(synchPoint.contains("after(")) {
                    constraint = AFTER;
                }
            }
        }
        synchPoint = synchPoint.replaceAll("before\\(", "(");
        synchPoint = synchPoint.replaceAll("after\\(",  "(");
        synchPoint = synchPoint.replaceAll("at\\(",     "(");
        synchPoint = synchPoint.replaceAll("around\\(", "(");

        //now, we must have ((+*-*)*offset)*+tmname((+or-)(+*-*)*offset)*
        //tmname may contains + or - and numbers !

        //we suppose that yhe name never starts or ends with '+' '-' '(space)' '(' and ')'
        int first_index = 0;
        char charat = 0;
        while(first_index < synchPoint.length()
          && ((charat = synchPoint.charAt(first_index)) == ' '
          || charat == '0' || charat == '1' || charat == '2'
          || charat == '3' || charat == '4' || charat == '5'
          || charat == '6' || charat == '7' || charat == '8'
          || charat == '9' || charat == '.' || charat == '+'
          || charat == '-' || charat == '(' || charat == ')'
          )){++first_index;}
        if(first_index < synchPoint.length()){
            --first_index;
            while(first_index >= 0
              && ((charat = synchPoint.charAt(first_index)) == '0'
              || charat == '1' || charat == '2' || charat == '3'
              || charat == '4' || charat == '5' || charat == '6'
              || charat == '7' || charat == '8' || charat == '9'
              || charat == '.'
              )){--first_index;}
            ++first_index;
            int last_index = synchPoint.length()-1;
            while(last_index > first_index
              && ((charat = synchPoint.charAt(last_index)) == ' '
              || charat == '0' || charat == '1' || charat == '2'
              || charat == '3' || charat == '4' || charat == '5'
              || charat == '6' || charat == '7' || charat == '8'
              || charat == '9' || charat == '.' || charat == '+'
              || charat == '-' || charat == '(' || charat == ')'
              )){--last_index;}
            last_index++;
            while(last_index < synchPoint.length()
              && ((charat = synchPoint.charAt(last_index)) == '0'
              || charat == '1' || charat == '2' || charat == '3'
              || charat == '4' || charat == '5' || charat == '6'
              || charat == '7' || charat == '8' || charat == '9'
              || charat == '.'
              )){++last_index;}
            targetName = synchPoint.substring(first_index, last_index);
            synchPoint = synchPoint.substring(0, first_index)+"+0+"+synchPoint.substring(last_index);
        }
        synchPoint = synchPoint.replaceAll(" ", "");
        setOffset(valueOf(synchPoint));
    }

    /**
     * @param formule string of a sum
     * @return the value of the sum
     */
    private static double valueOf(String formule){
        double result = 0;
        double signum = 1;
        char charat = 0;
        int first_index = 0;
        int last_index = 0;
        while(last_index < formule.length()){
            while(last_index < formule.length()
              && ((charat = formule.charAt(last_index)) == '0'
              || charat == '1' || charat == '2' || charat == '3'
              || charat == '4' || charat == '5' || charat == '6'
              || charat == '7' || charat == '8' || charat == '9'
              || charat == '.' )){
                last_index++;
            }
            if(last_index>first_index){
                result +=  signum*Double.parseDouble(formule.substring(first_index,last_index));
            }
            if(charat=='+') {
                signum = 1;
            }
            else{
                if(charat=='-') {
                    signum = -1;
                }
                else{
                    if(charat=='('){
                        first_index = ++last_index;
                        int parenthesis = 1;
                        while(last_index < formule.length()
                          && parenthesis>0){
                            charat = formule.charAt(last_index++);
                            if(charat=='(') {
                                  ++parenthesis;
                              }
                            if(charat==')') {
                                  --parenthesis;
                              }
                        }
                        --last_index;
                        result +=  signum*valueOf(formule.substring(first_index,last_index));
                    }
                }
            }
            ++last_index;
            first_index = last_index;
        }
        return result;
    }

}
