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

import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains informations about pitch accent.
 * @author Andre-Marie Pez
 * @author Angelo Cafaro
 */
public class PitchAccent implements Temporizable{

    public static final int LStar = 0;
    public static final int LStarPlusH = 1;
    public static final int LPlusHStar = 2;
    public static final int HStarPlusL = 3;
    public static final int HPlusLStar = 4;
    public static final int HStar = 5;

    public static final int NONE = 0;
    public static final int REDUCED = 1;
    public static final int MODERATE = 2;
    public static final int STRONG = 3;

    private static final double[] defaultDuration = {0.6 , 1, 1, 1, 1, 1};
    private static final String[] stringsOfTypes = {"Lstar", "LstarplusH", "LplusHstar", "HstarplusL", "HplusLstar", "Hstar"};
    private static final String[] stringsOfLevels = {"none"," reduced", "moderate", "strong"};

    private int type;
    private String id;
    private TimeMarker start;
    private TimeMarker end;
    private List<TimeMarker> markers;
    protected int level;
    protected double importance;

    public PitchAccent(String id, int type, int level, double importance, String startSynchPoint, String endSynchPoint){
        this.id = id;
        this.type = warpType(type);
        this.level = warpLevel(level);
        this.importance = importance;
        markers = new ArrayList<TimeMarker>();
        start = new TimeMarker("start");
        start.addReference(startSynchPoint);
        markers.add(start);
        end = new TimeMarker("end");
        end.addReference(endSynchPoint);
        markers.add(end);
    }

    public PitchAccent(String id, int type, int level, double importance, String startSynchPoint){
        this(id,type,level,importance,startSynchPoint,id+":start + "+defaultDuration[warpType(type)]);
    }

    public PitchAccent(PitchAccent p){
        this.type = p.type;
        this.id = p.id;
        this.level = p.level;
        this.importance = p.importance;
        this.markers = new ArrayList<TimeMarker>(p.markers);
        this.start = p.start;
        this.end = p.end;
    }

    /**
     * Returns the type of this pitch accent.
     * @return the type of this pitch accent
     */
    public int getPitchAccentType(){
        return type;
    }

    /**
     * Returns the level of this pitch accent.
     * @return the level of this pitch accent
     */
    public int getLevel(){
        return level;
    }

    /**
     * Returns the importance of this pitch accent.
     * @return the importance of this pitch accent
     */
    public double getImportance(){
        return importance;
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
                start.setValue(end.getValue()-defaultDuration[type]);
            }
            else {
                start.setValue(0);
            }
        }
        if(! end.isConcretized()) {
            end.setValue(start.getValue()+defaultDuration[type]);
        }
    }

    private static int warpType(int type){
        return type<LStar ? LStar : (type>HStar ? HStar : type);
    }

    private static int warpLevel(int level){
        return level<NONE ? NONE : (level>STRONG ? STRONG : level);
    }

    /**
     * Translates a type in string to a type value.
     * @param type the String of the type
     * @return the type value
     * @see #LStar
     * @see #LStarPlusH
     * @see #LPlusHStar
     * @see #HStarPlusL
     * @see #HPlusLStar
     * @see #HStar
     */
    public static int typeOf(String type){
        if(type.equalsIgnoreCase("LStar")||type.equalsIgnoreCase("L*")) {
            return LStar;
        }
        if(type.equalsIgnoreCase("LStarPlusH")||type.equalsIgnoreCase("L*+H")) {
            return LStarPlusH;
        }
        if(type.equalsIgnoreCase("LPlusHStar")||type.equalsIgnoreCase("L+H*")) {
            return LPlusHStar;
        }
        if(type.equalsIgnoreCase("HStarPlusL")||type.equalsIgnoreCase("H*+L")) {
            return HStarPlusL;
        }
        if(type.equalsIgnoreCase("HPlusLStar")||type.equalsIgnoreCase("H+L*")) {
            return HPlusLStar;
        }
        if(type.equalsIgnoreCase("HStar")||type.equalsIgnoreCase("H*")) {
            return HStar;
        }
        return HStar; //from BML dtd. Any idea to an other default return?
    }

    /**
     * Translates a level in string to a level value.
     * @param level the String of the level
     * @return the level value
     * @see #NONE
     * @see #REDUCED (supports "weak" for retro compatibility with old FML-APML definition)
     * @see #MODERATE (supports "medium" for retro compatibility with old FML-APML definition)
     * @see #STRONG
     */
    public static int levelOf(String level){

        if (level.equalsIgnoreCase("none")) {
            return NONE;
        }
        if (level.equalsIgnoreCase("weak") || level.equalsIgnoreCase("reduced")) {
            return REDUCED;
        }
        if(level.equalsIgnoreCase("medium") || level.equalsIgnoreCase("moderate")) {
            return MODERATE;
        }
        if(level.equalsIgnoreCase("strong")) {
            return STRONG;
        }
        // MODERATE is the default value in SSML for the emphasis element
        return MODERATE;
    }

    /**
     * Translates a level value to a corresponding String.<br/>
     * "weak" or "reduced" for REDUCED<br/>
     * "medium" or "moderate" for MODERATE<br/>
     * "strong" for STRONG
     * @param level type value
     * @return level in String
     * @see #REDUCED
     * @see #MODERATE
     * @see #STRONG
     */
    public static String stringOfLevel(int level){
        int index = warpLevel(level);
        return stringsOfLevels[index];
    }

    /**
     * Translates a type value to a corresponding String.<br/>
     * "Lstar" for LStar<br/>
     * "LstarplusH" for LStarPlusH<br/>
     * "LplusHstar" for LPlusHStar<br/>
     * "HstarplusL" for HStarPlusL<br/>
     * "HplusLstar" for HPlusLStar<br/>
     * "Hstar" for HStar
     * @param type type value
     * @return type in String
     * @see #LStar
     * @see #LStarPlusH
     * @see #LPlusHStar
     * @see #HStarPlusL
     * @see #HPlusLStar
     * @see #HStar
     */
    public static String stringOfType(int type){
        int index = warpType(type);
        return stringsOfTypes[index];
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
