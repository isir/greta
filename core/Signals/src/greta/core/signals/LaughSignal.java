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
package greta.core.signals;

import greta.core.util.IniManager;
import greta.core.util.laugh.Laugh;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;

/**
 *
 * @author Radoslaw Niewiadomski
 * @author Yu Ding
 */
public class LaughSignal extends Laugh implements Signal {

    private String file_reference;

    public LaughSignal() {
        super();
    }

    public LaughSignal(String id, TimeMarker start, TimeMarker end){
        super(id, start, end);
    }

    public LaughSignal(Laugh other){
        super(other);
    }

    /** checks if element has a reference for its start and end**/
    private boolean hasStartRef = false;
    private Signal startRef;
    private TimeMarker startMarker;
    private double startOffset = 0;
    private boolean hasEndRef = false;
    private Signal endRef;
    private TimeMarker endMarker;
    private double endOffset = 0;
    private ArrayList<String> linkedSignals = new ArrayList<String>();

    public boolean hasStartRef(){return hasStartRef;}
    public Signal getStartRef(){return startRef;}
    public TimeMarker getStartMarker(){return startMarker;}
    public double getStartOffset(){return startOffset;}

    public boolean hasEndRef(){return hasEndRef;}
    public Signal getEndRef(){return endRef;}
    public TimeMarker getEndMarker(){return endMarker;}
    public double getEndOffset(){return endOffset;}

    public void setHasStartRef(boolean newRef){this.hasStartRef = newRef;}
    public void setStartRef(Signal newT){this.startRef = newT;}
    public void setStartMarker(TimeMarker newTM){this.startMarker = newTM;}
    public void setStartOffset(double newO){this.startOffset = newO;}

    public void setHasEndRef(boolean newRef){this.hasEndRef = newRef;}
    public void setEndRef(Signal newT){this.endRef = newT;}
    public void setEndMarker(TimeMarker newTM){this.endMarker = newTM;}
    public void setEndOffset(double newO){this.endOffset = newO;}

    @Override
    public String getModality() {
        return "laugh";
    }

    @Deprecated
    public void setFileName(String fileName) {
        this.file_reference = IniManager.getProgramPath() + "./Examples/ilhaire/" + fileName;
    }

    @Deprecated
    public String getFileName(){
        return file_reference;
    }

    public ArrayList<String> getLinkedSignal(){
        return linkedSignals;
    }

    public void setLinkedSignal(String _signal){
        linkedSignals.add(_signal);
    }

    public void deleteLinkedSignal(String _signal){
        for(String tmp:linkedSignals){
            if(tmp.equals(_signal)){
                linkedSignals.remove(_signal);
            }
        }
    }

    public boolean isEmptyLinkedSignal(){
        return linkedSignals.isEmpty();
    }

}
