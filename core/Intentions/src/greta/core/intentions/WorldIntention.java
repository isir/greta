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
