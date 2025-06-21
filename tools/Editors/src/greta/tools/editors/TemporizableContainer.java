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
package greta.tools.editors;

import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Angelo Cafaro
 */
public class TemporizableContainer<T extends Temporizable> {

    public enum ReferencesState {
        UNCHANGED,
        CHANGED,
        REMOVED
    }

    private T temporizible;

    private String temporizableContainerType;

    private TemporizableContainer<? extends Temporizable> startRef;
    private TimeMarker startMarker;
    private double startOffset;

    private TemporizableContainer<? extends Temporizable> endRef;
    private TimeMarker endMarker;
    private double endOffset;

    private ArrayList<String> linkedSignals;
    private ReferencesState refsState;

    public TemporizableContainer(T aTemporizable, String aType) {

        temporizible = aTemporizable;

        startRef = null;
        startOffset = 0;

        endRef = null;
        endOffset = 0;

        linkedSignals = new ArrayList<String>();
        refsState = ReferencesState.UNCHANGED;

        temporizableContainerType = aType;
    }

    public T getTemporizable() {
        return this.temporizible;
    }

    public void setTemporizable(T newTemporizable) {
        this.temporizible = newTemporizable;
    }

    public String getId() {
        return this.temporizible.getId();
    }

    public String getTemporizableType() {
        return this.temporizableContainerType;
    }

    public TimeMarker getStart() {
        return this.temporizible.getStart();
    }

    public TimeMarker getEnd() {
        return this.temporizible.getEnd();
    }

    public TimeMarker getTimeMarker(String aTMName) {
        return this.temporizible.getTimeMarker(aTMName);
    }

     public List<TimeMarker> getTimeMarkers() {
        return this.temporizible.getTimeMarkers();
    }

    public boolean hasStartRef() {
        return (startRef != null);
    }

    public TemporizableContainer<? extends Temporizable> getStartRef() {
        return startRef;
    }

    public TimeMarker getStartMarker() {
        return startMarker;
    }

    public double getStartOffset() {
        return startOffset;
    }

    public boolean hasEndRef() {
        return (endRef != null);
    }

    public TemporizableContainer<? extends Temporizable> getEndRef()
    {
        return endRef;
    }

    public TimeMarker getEndMarker() {
        return endMarker;
    }

    public double getEndOffset() {
        return endOffset;
    }

    public void setStartRef(TemporizableContainer<? extends Temporizable> newT) {
        this.startRef = newT;
    }

    public void setStartMarker(TimeMarker newTM) {
        this.startMarker = newTM;
    }

    public void setStartOffset(double newO) {
        this.startOffset = newO;
    }

    public void setEndRef(TemporizableContainer<? extends Temporizable> newT) {
        this.endRef = newT;
    }

    public void setEndMarker(TimeMarker newTM) {
        this.endMarker = newTM;
    }

    public void setEndOffset(double newO) {
        this.endOffset = newO;
    }


    public ArrayList<String> getLinkedSignal(){
        return linkedSignals;
    }

    public void setLinkedSignal(String id){
        linkedSignals.add(id);
    }

    public void deleteLinkedSignal(String id){
        linkedSignals.remove(id);
    }

    public void clearLinkedSignals(){
        linkedSignals.clear();
    }

    public boolean isEmptyLinkedSignal(){
        return linkedSignals.isEmpty();
    }

    public boolean isReferenceModified(){
        return (refsState == ReferencesState.CHANGED);
    }

    public boolean isReferenceDeleted(){
        return (refsState == ReferencesState.REMOVED);
    }

    public void setReferencesState(ReferencesState state){
        refsState = state;
    }
}
