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
package greta.core.util.laugh;

import greta.core.util.audio.Audio;
import greta.core.util.log.Logs;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 * @author Yu Ding
 */
public class Laugh implements Temporizable{

    private String id;
    private double intensity;
    private TimeMarker start;
    private TimeMarker end;
    private List<TimeMarker> tms;
    private List<LaughPhoneme> phonems;
    private Audio audio;
    private ArrayList<String> linkedSignals;
    private int refModified = 0;


    public Laugh(){
        this(null, new TimeMarker("start"), new TimeMarker("end"));
    }

    public Laugh(String id, TimeMarker start, TimeMarker end){
        this.id = id;
        this.start = start;
        this.end = end;
        tms =new ArrayList<TimeMarker>(2);
        tms.add(start);
        tms.add(end);
        intensity = 0.5;
    }

    public Laugh(Laugh other){
        id = other.id;
        intensity = other.intensity;
        start = other.start;
        end = other.end;
        tms = other.tms;
        phonems = other.phonems;
        audio = other.audio;
    }

    @Override
    public List<TimeMarker> getTimeMarkers() {
        return tms;
    }

    @Override
    public TimeMarker getTimeMarker(String name) {
        if(name.equalsIgnoreCase("start")){
            return start;
        }
        if(name.equalsIgnoreCase("end")){
            return end;
        }
        return null;
    }

    public void setTimeMarker(String value, String name) {
        if (name.equalsIgnoreCase("start")) {
            start.addReference(value);
        }

        if (name.equalsIgnoreCase("end")) {
            end.addReference(value);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIntensity(double intensity){
        this.intensity = intensity;
    }

    public double getIntensity(){
        return intensity;
    }

    @Override
    public void schedule() {
        compute(true, synthetizerDoAudio, synthetizerDoPhonemes);
    }

    private void compute(boolean doTemporize, boolean doAudio, boolean doPhonemes){
         synchronized (lock){
            if(synthetizerToUse==null){
                Logs.error(this.getClass().getName() + " : no TTS found.");
            }
            else{
                synthetizerToUse.setLaugh(this);
                synthetizerToUse.compute(doTemporize, doAudio, doPhonemes);

                //Copy result phonems :
                if(doPhonemes) {
                    phonems = new ArrayList<LaughPhoneme>(synthetizerToUse.getPhonemes());
                }

                //Copy result audio
                if(doAudio){
                    audio = synthetizerToUse.getAudio();
                }
            }
        }
    }

    public List<LaughPhoneme> getLaughPhonemes(){
        if(phonems == null) {
            compute(synthetizerDoTemporize, synthetizerDoAudio, true);
        }
        return phonems;
    }

    public void setLaughPhonemes(List<LaughPhoneme> laughPhonemes){
        this.phonems = laughPhonemes;
    }

    public Audio getAudio(){
        if(audio==null) {
            compute(synthetizerDoTemporize, true, synthetizerDoPhonemes);
        }
        return audio;
    }

    public void setAudio(Audio audio){
        this.audio = audio;
    }

//Static fields :
    private static LaughSynthetizer synthetizerToUse;
    private static boolean synthetizerDoTemporize = true;
    private static boolean synthetizerDoAudio = true;
    private static boolean synthetizerDoPhonemes = true;
    private static final Object lock = new Object(); //used to synchronize threads on synthetizerToUse

    public static void setLaughSynthetizer(LaughSynthetizer toUse){
        synchronized (lock){
            synthetizerToUse = toUse;
        }
    }

    public static LaughSynthetizer getLaughSynthetizer(){
        return synthetizerToUse;
    }

    public static void setLaughSynthetizerOptions(boolean doTemporize, boolean doAudio, boolean doPhonemes){
        synchronized (lock){
            synthetizerDoTemporize = doTemporize;
            synthetizerDoAudio = doAudio;
            synthetizerDoPhonemes = doPhonemes;
        }
    }

    @Override
    public TimeMarker getStart() {
        return start;
    }

    @Override
    public TimeMarker getEnd() {
        return end;
    }

    public ArrayList<String> getLinkedSignal(){
        return linkedSignals;
    }

    public void setLinkedSignal(String _signal){
        linkedSignals.add(_signal);
    }

    public void deleteLinkedSignal(String _signal){
        linkedSignals.remove(_signal);
    }

    public boolean isEmptyLinkedSignal(){
        return linkedSignals.isEmpty();
    }

    public boolean isRefModified(){
        if(refModified==1) return true;
        return false;
    }

    public boolean isRefDeleted(){
        if(refModified==2) return true;
        return false;
    }

    public void setRefModified(int i){
        refModified = i;
    }
}
