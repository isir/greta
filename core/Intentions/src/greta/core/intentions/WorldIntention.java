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

import greta.core.util.time.TimeMarker;

/**
 * This {@code BasicIntention} contains informations about what to describe in world.
 * @author Andre-Marie Pez
 */
public class WorldIntention extends BasicIntention implements IntentionTargetable{

    private String refType;
    private String refId;
    private String propType;
    private String propValue;
    private String target;

    public WorldIntention(String id, TimeMarker start, TimeMarker end, double importance){
        super("world", id, null, start, end, importance);
    }

    @Override
    public String getType(){
        return refType
            + (refId==null || refId.isEmpty() ? "" : "-"+refId)
            + (propType==null || propType.isEmpty() ? "" : "-"+propType)
            + (propValue==null || propValue.isEmpty() ? "" : "-"+propValue);
    }

    /**
     * Sets the reference type.
     * @param refType the reference type to set
     */
    public void setRefType(String refType){
        this.refType = refType;
    }

    /**
     * Sets the reference id.
     * @param refId the reference id to set
     */
    public void setRefId(String refId){
        this.refId = refId;
    }

    /**
     * Sets the property type.
     * @param propType the property type to set
     */
    public void setPropType(String propType){
        this.propType = propType;
    }

    /**
     * Sets the property value.
     * @param propValue the property value to set
     */
    public void setPropValue(String propValue){
        this.propValue = propValue;
    }

    /**
     * Returns the reference type.
     * @return the reference type
     */
    public String getRefType(){
        return refType;
    }

    /**
     * Returns the reference id.
     * @return the reference id
     */
    public String getRefId(){
        return refId;
    }

    /**
     * Returns the property type.
     * @return the property type
     */
    public String getPropType(){
        return propType;
    }

    /**
     * Returns the property value.
     * @return the property value
     */
    public String getPropValue(){
        return propValue;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public void setTarget(String target) {
        this.target = target;
    }

}
