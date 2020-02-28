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
