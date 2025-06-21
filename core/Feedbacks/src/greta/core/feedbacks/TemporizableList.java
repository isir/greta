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
package greta.core.feedbacks;

import greta.core.signals.SpeechSignal;
import greta.core.util.id.ID;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public class TemporizableList {

    /**
     * @return the pendingList
     */
    public List<Temporizable> getPendingList() {
        return pendingList;
    }

    /**
     * @param pendingList the pendingList to set
     */
    public void setPendingList(List<Temporizable> pendingList) {
        this.pendingList = pendingList;
    }

    /**
     * @return the startTime
     */
    public Double getStartTime() {
        return startTime;
    }
    private ID id;
    private Double deadTime;
    private Double startTime;
    private Double stopTime;
    private Double endTime;
    private List<Temporizable> pendingList;
    private List<Temporizable> startedList;
    private ArrayList<Temporizable> lastStartedList;
    private ArrayList<Temporizable> finishedList;
    private boolean allTmpFinished;
    private ArrayList<TimeMarker> speech_timeMrk; /******************/
    private TimeMarker currentTimeMarker_ended;

    public TemporizableList(ID Id){
        id=Id;
        deadTime=-1.;
        startTime=-1.;
        stopTime=-1.;
        endTime=-1.;
        pendingList = new ArrayList<Temporizable>();
        startedList = new ArrayList<Temporizable>();
        lastStartedList = new ArrayList<Temporizable>();
        finishedList = new ArrayList<Temporizable>();
        //speech_timeMrk = new ArrayList<TimeMarker>();
        currentTimeMarker_ended = new TimeMarker("");
        allTmpFinished = false;
    }
    public TemporizableList(ID Id, List<? extends Temporizable> PendingList){
        id=Id;
        startTime=0.;
        pendingList = (List<Temporizable>) PendingList;
        startedList = new ArrayList<Temporizable>();
        lastStartedList = new ArrayList<Temporizable>();
        finishedList = new ArrayList<Temporizable>();
        //speech_timeMrk = new ArrayList<TimeMarker>();
        currentTimeMarker_ended = new TimeMarker("");
        allTmpFinished = false;
   }
    public void addTemporizable(Temporizable NewTmp){
        getPendingList().add(NewTmp);
    }
    public void setDeadTime(Double DeadTime){deadTime=DeadTime;}
    public void setStartTime(Double StartTime){startTime=StartTime;}
    public void setStoppedTime(Double StoppedTime){stopTime=StoppedTime;}
    public void setEndTime(Double EndTime){endTime=EndTime;}
    public ID getID(){
        return id;
    }
    public List<Temporizable> listStarted(){
        return startedList;
    }
    public List<Temporizable> listLastStarted(){
        List<Temporizable> lastStartedTmp=(ArrayList<Temporizable>)lastStartedList.clone();
        lastStartedList.clear();
        return lastStartedTmp;
    }
    public List<Temporizable> listFinished(){
        List<Temporizable> finishedListTmp=(ArrayList<Temporizable>)finishedList.clone();
        finishedList.clear();
        return finishedListTmp;
    }
    public boolean isFinished(){
        return allTmpFinished;
    }

    public TimeMarker updateTimeMarker(SpeechSignal speechSign){
         if (getStartTime()!=0){
            Double currentTime = Timer.getTime() - getStartTime();
            speech_timeMrk = (ArrayList) speechSign.getTimeMarkers();
            int j = 1;
            if (j+1 < speech_timeMrk.size()){
                while( speech_timeMrk.get(j).getValue() < currentTime){ // check the time progressing and the timeMarker values
                    setCurrentTimeMarker_ended(speech_timeMrk.get(j));
                    // remove the timemarker from the list so I don't have to update j
                    speech_timeMrk.remove(j);
                    if (j+1 >= speech_timeMrk.size()){
                        speechSign.setId("");
                        break;
                    }
                }
            }
        }
        return getCurrentTimeMarker_ended();
    }

    public TimeMarker update(){

        if (getStartTime()!=0){
            Double currentTime = Timer.getTime() - getStartTime();
            for(int i=0; i<startedList.size();i++){

                if (startedList.get(i).getEnd().getValue() < currentTime){
                    finishedList.add(startedList.get(i));
                    startedList.remove(i);
                    i--;
                }

            }
            for(int i=0; i<getPendingList().size();i++){
                if (getPendingList().get(i).getStart().getValue()<currentTime){
                    startedList.add(getPendingList().get(i));
                    lastStartedList.add(getPendingList().get(i));
                    getPendingList().remove(i);
                    i--;
                }
            }
            if(getPendingList().isEmpty()&&startedList.isEmpty()){
                allTmpFinished = true;
            }
        }
        return getCurrentTimeMarker_ended();
    }

    /**
     * @return the currentTimeMarker_ended
     */
    public TimeMarker getCurrentTimeMarker_ended() {
        return currentTimeMarker_ended;
    }

    /**
     * @param currentTimeMarker_ended the currentTimeMarker_ended to set
     */
    public void setCurrentTimeMarker_ended(TimeMarker currentTimeMarker_ended) {
        this.currentTimeMarker_ended = currentTimeMarker_ended;
    }
}
