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
package greta.core.signals.gesture;

import java.util.HashMap;
import java.util.Map;

/**
 * The symbolic position describe position for the wrist.
 * @author Brian Ravenet
 * @author Quoc Anh Le
 */
public class SymbolicPosition extends UniformPosition {

    public static final Map<String, Double> horizontalPositions;
    public static final Map<String, Double> verticalPositions;
    public static final Map<String, Double> frontalPositions;

    static {

        horizontalPositions = new HashMap<String, Double>();
        horizontalPositions.put("XEP", 0.8);
        horizontalPositions.put("XP", 0.5);
        horizontalPositions.put("XC", 0.25);
        horizontalPositions.put("XCC", 0.0);
        horizontalPositions.put("XOppC", -0.25);

        verticalPositions = new HashMap<String, Double>();
        verticalPositions.put("YUpperEP", 1.2);
        verticalPositions.put("YUpperP", 0.9);
        verticalPositions.put("YUpperC", 0.6);
        verticalPositions.put("YCC", 0.25);
        verticalPositions.put("YLowerC", 0.0);
        verticalPositions.put("YLowerP", -0.3);
        verticalPositions.put("YLowerEP", -0.5);

        frontalPositions = new HashMap<String, Double>();
        frontalPositions.put("ZNear", 0.4);
        frontalPositions.put("ZMiddle", 0.65);
        frontalPositions.put("ZFar", 0.95);

    }

    private String horizontalLocation;
    private String verticalLocation;
    private String frontalLocation;

    @Override
    public String getStringPosition() {
        String returnString = "       <horizontalLocation>" + getHorizontalLocation() + "</horizontalLocation>\n";
        returnString += "       <verticalLocation>" + getVerticalLocation() + "</verticalLocation>\n";
        returnString += "       <frontalLocation>" + getFrontalLocation() + "</frontalLocation>\n";
        return returnString;
    }

    @Override
    public Position getCopy() {
        SymbolicPosition pos = new SymbolicPosition();
        pos.setX(getX());
        pos.setY(getY());
        pos.setZ(getZ());
        pos.setHorizontalLocation(horizontalLocation);
        pos.setVerticalLocation(verticalLocation);
        pos.setFrontalLocation(frontalLocation);
        pos.setXFixed(isXFixed());
        pos.setYFixed(isYFixed());
        pos.setZFixed(isZFixed());
        return pos;
    }

    // x
    public String getHorizontalLocation() {
        return horizontalLocation;
    }

    public void setHorizontalLocation(String horizontalLocation) {
        if(horizontalLocation!=null && horizontalPositions.containsKey(horizontalLocation)){
            this.horizontalLocation = horizontalLocation;
            setX(horizontalPositions.get(horizontalLocation));
        }
    }

    // y
    public String getVerticalLocation() {
        return verticalLocation;
    }

    public void setVerticalLocation(String verticalLocation) {
        if(verticalLocation!=null && verticalPositions.containsKey(verticalLocation)){
            this.verticalLocation = verticalLocation;
            setY(verticalPositions.get(verticalLocation));
        }
    }

    // z
    public String getFrontalLocation() {
        return frontalLocation;
    }

    public void setFrontalLocation(String frontalLocation) {
        if(frontalLocation!=null && frontalPositions.containsKey(frontalLocation)){
            this.frontalLocation = frontalLocation;
            setZ(frontalPositions.get(frontalLocation));
        }
    }

}
