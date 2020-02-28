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
package greta.core.util.speech;

import greta.core.util.time.SynchPoint;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains informations about boundary.
 * @author Andre-Marie Pez
 */
public class Boundary implements Temporizable{

    public static final int L = 0;
    public static final int H = 1;
    public static final int LL = 2;
    public static final int LH = 3;
    public static final int HL = 4;
    public static final int HH = 5;

    private static final double[] defaultDuration = {0.3, 0.5, 0.5, 0.5, 0.5, 0.5};
    private static final String[] stringsOfTypes = {"L", "H", "LL", "LH", "HL", "HH"};

    private int type;
    private String id;
    private TimeMarker start;
    private TimeMarker end;
    private List<TimeMarker> markers;

    protected double duration; //in seconds

    public Boundary(String id, int type, String startSynchPoint, String endSynchPoint){
        this.id = id;
        this.type = warpType(type);
        duration = -1;
        markers = new ArrayList<TimeMarker>();
        start = new TimeMarker("start");
        start.addReference(startSynchPoint);
        markers.add(start);
        end = new TimeMarker("end");
        end.addReference(endSynchPoint);
        markers.add(end);
    }

    public Boundary(String id, int type, String startSynchPoint, double duration){
        this.id = id;
        this.type = warpType(type);
        this.duration = duration;
        markers = new ArrayList<TimeMarker>();
        start = new TimeMarker("start");
        start.addReference(startSynchPoint);
        markers.add(start);
        end = new TimeMarker("end");
        end.addReference(id+":start + "+duration);
        markers.add(end);
    }

    public Boundary(String id, int type, String startSynchPoint){
        this(id,type,startSynchPoint,defaultDuration[warpType(type)]);
    }

    public Boundary(Boundary b){
        this.id = b.id;
        this.type = b.type;
        this.duration = b.duration;
        this.markers = new ArrayList<TimeMarker>(b.markers);
        this.start = b.start;
        this.end = b.end;
    }

    /**
     * Returns the type of this boundary.
     * @return the type of this boundary
     */
    public int getBoundaryType(){
        return type;
    }

    /**
     * Returns the duration in seconds of this boundary.<br/>
     * It returns a negative value if unknown.
     * @return the duration in seconds of this boundary
     */
    public double getDuration(){
        if(duration<0){
            if(markers.get(0).isConcretized() && markers.get(1).isConcretized()) {
                duration = markers.get(1).getValue() - markers.get(0).getValue();
            }
            else{
                SynchPoint targetedSynch = markers.get(1).getFirstSynchPointWithTarget();
                if(targetedSynch!=null){
                  if(targetedSynch.getTargetName().equalsIgnoreCase(id+":start")) {
                        duration = targetedSynch.getOffset();
                    }
                  else{
                      SynchPoint startTarget = markers.get(0).getFirstSynchPointWithTarget();
                      if(startTarget!=null && targetedSynch.getTargetName().equalsIgnoreCase(startTarget.getTargetName())) {
                          duration = targetedSynch.getOffset()-startTarget.getOffset();
                      }
                  }
                }
            }
            //finaly get the default duration if it is always negative
            if(duration<0) {
                duration = defaultDuration[type];
            }
        }
        return duration;
    }


    public List<TimeMarker> getTimeMarkers() {
        return markers;
    }

    public TimeMarker getTimeMarker(String name) {
        if(name.equalsIgnoreCase("start")) {
            return markers.get(0);
        }
        if(name.equalsIgnoreCase("end")) {
            return markers.get(1);
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public void schedule() {
        TimeMarker start = markers.get(0);
        TimeMarker end = markers.get(1);
        if(! start.isConcretized()){
            if(end.isConcretized()) {
                start.setValue(end.getValue()-( duration<0? defaultDuration[type] : duration));
            }
            else {
                start.setValue(0);
            }
        }
        if(! end.isConcretized()) {
            end.setValue(start.getValue()+( duration<0? defaultDuration[type] : duration));
        }
        //then update duration if start and end was already concretized before calling this function
        duration = end.getValue() - start.getValue();
    }

    private static int warpType(int type){
        return type<L ? L : (type>HH ? HH : type);
    }

    /**
     * Translates a type in string to a type value.
     * @param type the String of the type
     * @return the type value
     * @see #L
     * @see #H
     * @see #LL
     * @see #LH
     * @see #HL
     * @see #HH
     */
    public static int typeOf(String type){
        if(type.equalsIgnoreCase("L")) {
            return L;
        }
        if(type.equalsIgnoreCase("H")) {
            return H;
        }
        if(type.equalsIgnoreCase("LL")) {
            return LL;
        }
        if(type.equalsIgnoreCase("LH")) {
            return LH;
        }
        if(type.equalsIgnoreCase("HL")) {
            return HL;
        }
        if(type.equalsIgnoreCase("HH")) {
            return HH;
        }
        return LL; //from BML dtd. Any idea to an other default return?
    }

    /**
     * Translates a type value to a corresponding String.<br/>
     * "L" for L<br/>
     * "H" for H<br/>
     * "LL" for LL<br/>
     * "LH" for LH<br/>
     * "HL" for HL<br/>
     * "HH" for HH
     * @param type type value
     * @return type in String
     * @see #L
     * @see #H
     * @see #LL
     * @see #LH
     * @see #HL
     * @see #HH
     */
    public static String stringOfType(int type){
        int index = warpType(type);
        return stringsOfTypes[index];
    }

    /**
     * Returns a default duration for a specific {@code Boundary}.
     * @param b the boundary
     * @return the default duration
     */
    public static double defaultDurationFor(Boundary b){
        return defaultDuration[b.type];
    }

    @Override
    public TimeMarker getStart() {
        return start;
    }

    @Override
    public TimeMarker getEnd() {
        return end;
    }

}
