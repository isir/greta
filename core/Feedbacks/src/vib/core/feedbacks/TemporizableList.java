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
package vib.core.feedbacks;

import java.util.ArrayList;
import java.util.List;
import vib.core.util.id.ID;
import vib.core.util.time.Temporizable;
import vib.core.util.time.Timer;

/**
 *
 * @author Ken Prepin
 */
public class TemporizableList {
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
        allTmpFinished = false;
    }
    public TemporizableList(ID Id, List<? extends Temporizable> PendingList){
        id=Id;
        startTime=0.;
        pendingList = (List<Temporizable>) PendingList;
        startedList = new ArrayList<Temporizable>();
        lastStartedList = new ArrayList<Temporizable>();
        finishedList = new ArrayList<Temporizable>();
        allTmpFinished = false;
   }
    public void addTemporizable(Temporizable NewTmp){
        pendingList.add(NewTmp);
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
    public void update(){
        if (startTime!=0){
            Double currentTime = Timer.getTime() - startTime;
            for(int i=0; i<startedList.size();i++){
                if (startedList.get(i).getEnd().getValue()<currentTime){
                    finishedList.add(startedList.get(i));
                    startedList.remove(i);
                    i--;
                }
            }
            for(int i=0; i<pendingList.size();i++){
                if (pendingList.get(i).getStart().getValue()<currentTime){
                    startedList.add(pendingList.get(i));
                    lastStartedList.add(pendingList.get(i));
                    pendingList.remove(i);
                    i--;
                }
            }
            if(pendingList.isEmpty()&&startedList.isEmpty()){
                allTmpFinished = true;
            }
        }
    }
}
