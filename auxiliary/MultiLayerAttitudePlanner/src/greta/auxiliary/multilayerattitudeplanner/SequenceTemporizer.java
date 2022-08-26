/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.multilayerattitudeplanner;

import greta.auxiliary.multilayerattitudeplanner.structures.SequenceProbabilityTuple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import greta.auxiliary.multilayerattitudeplanner.structures.NVBEventType;
import greta.core.intentions.Intention;
import greta.core.signals.Signal;
import greta.core.signals.SignalProvider;
import greta.core.util.time.TimeMarker;

/**
 *
 * @author Mathieu
 */
public class SequenceTemporizer {
    
    /*
     * 
     * Get all the TimeMarkers defined in a list of intentions (no duplicates, if any)
     */
    public static List<TimeMarker> getTimeMarkersInIntentionsList(List<Intention> intentions)
    {
        List<TimeMarker> tms = new ArrayList<TimeMarker>();
        for(Intention intent : intentions)
        {
            tms.addAll(intent.getTimeMarkers());
        }
        List<TimeMarker> noDuplicateTmList = new ArrayList<TimeMarker>();
        for(TimeMarker tm : tms)
        {
            if(!timeMarkerInList(tm, noDuplicateTmList))
                noDuplicateTmList.add(tm);
        }
        return noDuplicateTmList;
    }

    /*
     * 
     * Given a sequence, existing signals, and intentions, temporize a sequence
     */
    public static List<Signal> temporizeSequence(List<NVBEventType> chosenSequence, 
            List<Signal> selectedSignals, List<Intention> intentions) {
        List<Signal> signals = new ArrayList<Signal>();
        List<TimeMarker> tms = getTimeMarkersInIntentionsList(intentions);
        tms = orderTimeMarkerList(tms);
        
        List<Signal> cleanedSignals = SequenceSignalProvider.cleanSignals(selectedSignals);
        
        List<NVBEventType> selectedNVBEvents = SequenceSignalProvider.listSignalAsNVBEvents(cleanedSignals);

        Map<Integer, Signal> alreadyTimedSignals = new HashMap<Integer, Signal>();
        List<Integer> indexesAlreadyTimed = new ArrayList<Integer>();
        //indexesAlreadyTimed.add(-1); //trick for B)
        //already timed signals
        for(int i=0; i<chosenSequence.size();i++)
        {
            for(int j=0;j<selectedNVBEvents.size();j++)
            {
                if(chosenSequence.get(i).equals(selectedNVBEvents.get(j)))
                {
                    alreadyTimedSignals.put(i,cleanedSignals.get(j));
                    indexesAlreadyTimed.add(i);
                    selectedNVBEvents.remove(j);
                    break;
                }
            }
        }
        
        //B)we time contiguous signals between already timed signals
        //indexesAlreadyTimed.add(chosenSpt.signals.size()); //trick for B)
        
        for(int i=0;i<chosenSequence.size();i++)
        {
            List<Integer> contiguousSignalsToTemporize = new ArrayList<Integer>();
            if(indexesAlreadyTimed.contains(i))
            {
                //signals.add(alreadyTimedSignals.get(i));
            }
            else
            {
                contiguousSignalsToTemporize.add(i);
                /*for(int j=i+1;j<chosenSpt.signals.size();j++)
                {
                    if(!indexesAlreadyTimed.contains(j))
                    {
                        contiguousSignalsToTemporize.add(j);
                    }
                    else
                    {
                        break;
                    }
                }*/
                
                //here we have a set of contiguous symoblic signals to temporize : create and temporize them
                List<Signal> timedSignals = temporizeSignals(contiguousSignalsToTemporize, 
                        tms, chosenSequence,selectedSignals, alreadyTimedSignals );
                        
                
                //loop to check if some signals were not created for some reason
                int k=0;
                for(Integer in : contiguousSignalsToTemporize)
                {
                    indexesAlreadyTimed.add(in);
                    if(timedSignals.size()<=k)
                    {
                        alreadyTimedSignals.put(in, null);
                    }
                    else
                    {
                        alreadyTimedSignals.put(in, timedSignals.get(k));
                        signals.add(timedSignals.get(k));
                    }
                    k++;
                }
                
            }
        }
        
        return signals;
    }
    
    private static List<TimeMarker> getTimeMarkersToTemporizeSignals(List<Integer> contiguousSignalsToTemporize,
                        Map<Integer,Signal> alreadyTimedSignals, List<TimeMarker> allAvailableTimeMarkers,
                        List<NVBEventType> chosenSequence)
    {
        List<TimeMarker> timemarkersToTemporize = new ArrayList<TimeMarker>();
        TimeMarker lowerBound = null;
        TimeMarker upperBound = null;
        //if first signal to temporize is not the first in sequence then lower bound is start timemarker of previous signal
        if(contiguousSignalsToTemporize.get(0)>0)
        {
            if(alreadyTimedSignals.get(contiguousSignalsToTemporize.get(0)-1)!=null)
                lowerBound = SignalProvider.getBegining(alreadyTimedSignals.get(contiguousSignalsToTemporize.get(0)-1));
                    //alreadyTimedSignals.get(contiguousSignalsToTemporize.get(0)-1).getTimeMarker("start"); 
        }
        /*else
        {
            //les intentions sont a priori dans l'ordre...
            lowerBound=intentions.get(0).getTimeMarker("start");
        }*/

        //if last signal to temporize is not the last in sequence then upper bound is start timemarker of next signal
        if(contiguousSignalsToTemporize.get(contiguousSignalsToTemporize.size()-1)<chosenSequence.size()-1)
        {
            if(alreadyTimedSignals.get(contiguousSignalsToTemporize.get(contiguousSignalsToTemporize.size()-1)+1)!=null)
                upperBound = SignalProvider.getBegining(alreadyTimedSignals.get(contiguousSignalsToTemporize.get(contiguousSignalsToTemporize.size()-1)+1));
                    //alreadyTimedSignals.get(contiguousSignalsToTemporize.get(contiguousSignalsToTemporize.size()-1)+1).getTimeMarker("start");
        }
        /*else
        {
            //les intentions sont a priori dans l'ordre...
            upperBound=intentions.get(intentions.size()-1).getTimeMarker("end");
        }*/

        allAvailableTimeMarkers = orderTimeMarkerList(allAvailableTimeMarkers);
        for(int i=0;i<allAvailableTimeMarkers.size();i++)
        {
            TimeMarker tm = allAvailableTimeMarkers.get(i);
            if(!timeMarkerInList(tm,timemarkersToTemporize))
            {
                if(lowerBound!=null && upperBound==null)
                {
                    if(lowerBound.getValue()<tm.getValue())
                    {
                        timemarkersToTemporize.add(tm);
                    }
                }
                else if(lowerBound==null && upperBound!=null)
                {
                    if(upperBound.getValue()>tm.getValue())
                    {
                        timemarkersToTemporize.add(tm);
                    }
                }
                else if(lowerBound!=null && upperBound!=null)
                {
                    if(upperBound.getValue()>tm.getValue() 
                            && lowerBound.getValue()<tm.getValue())
                    {
                        timemarkersToTemporize.add(tm);
                    }
                }
                else 
                {
                    timemarkersToTemporize.add(tm);
                }
            }
        }
        return timemarkersToTemporize;
    }

    private static List<Signal> temporizeSignals(List<Integer> contiguousSignalsToTemporize, 
            List<TimeMarker> allTimemarkers, 
            List<NVBEventType> chosenSequence, 
            List<Signal> selectedSignals,
            Map<Integer, Signal> alreadyTimedSignals ) 
    {
        List<Signal> timedSignals = new ArrayList<Signal>();
        
        //first get the timemarkers where the signals can start
        //->heuristic : just get the first available
        List<TimeMarker> timemarkersToStart = getTimeMarkersToTemporizeSignals(contiguousSignalsToTemporize,
                alreadyTimedSignals, allTimemarkers, chosenSequence);
        timemarkersToStart = orderTimeMarkerList(timemarkersToStart);
        
        //map an available timemarker to every contiguous signals (referenced by their indexes)
        Map<Integer, TimeMarker> signalsStartTMs = new HashMap<Integer, TimeMarker>();
        for(Integer i : contiguousSignalsToTemporize)
        {
            for(TimeMarker tm : timemarkersToStart)
            {
                boolean alreadyUsedTM = false;
                for(Integer j : signalsStartTMs.keySet())
                {
                    if(tm.getValue()<=signalsStartTMs.get(j).getValue())
                    {
                        alreadyUsedTM = true;
                    }
                }
                if(!alreadyUsedTM)
                {                    
                    signalsStartTMs.put(i, tm);
                    break;
                }
                else
                {
                    continue;
                }
            }
        }
        
        
        // find an ending timemarker for every signal, making sure we do not time two signals
        // of the same modality at the same time
        for(Integer i : contiguousSignalsToTemporize)
        {
            if(signalsStartTMs.get(i)==null)
            {
                continue;
            }
            
            TimeMarker endTimeMarker = allTimemarkers.get(0);
            for(int j=1;j<allTimemarkers.size();j++)
            {
                boolean foundATimeMarker=false;
                TimeMarker tm = allTimemarkers.get(j);
                if(tm.getValue()>signalsStartTMs.get(i).getValue())
                {
                    boolean foundSameModality=false;
                    for(Signal s : selectedSignals)
                    {
                        if(NVBEventType.isSameModality(chosenSequence.get(i), s))
                        {
                            foundSameModality=true;
                            if(signalsStartTMs.get(i).getValue()>SignalProvider.getEnding(s).getValue())
                            {
                                foundATimeMarker=true;
                                endTimeMarker = tm;
                            }
                            else if (signalsStartTMs.get(i).getValue()<SignalProvider.getEnding(s).getValue()
                                        && tm.getValue()<SignalProvider.getBegining(s).getValue())
                            {
                                foundATimeMarker=true;
                                endTimeMarker = tm;
                            }
                            else if (signalsStartTMs.get(i).getValue()<SignalProvider.getEnding(s).getValue()
                                        && tm.getValue()==SignalProvider.getBegining(s).getValue())
                            {
                                foundATimeMarker=true;
                                endTimeMarker = tm;
                                break;
                            }
                        }
                    }
                    if(!foundSameModality)
                    {
                        foundATimeMarker=true;
                        endTimeMarker = tm;
                    }
                    //endTimeMarker = tm;
                    //break;
                }
                if(foundATimeMarker)
                    break;
            }
            
            //if there was already a signal here (one not considered in the annotation scheme)
            //we don't add
            if(endTimeMarker.getValue()<signalsStartTMs.get(i).getValue())
            {
                return timedSignals;
            }
            //create the signal with the computed start and end timemarkers
            List<Signal> s = SequenceSignalProvider.createSignal(signalsStartTMs.get(i), endTimeMarker, chosenSequence.get(i));
            if(s!=null)
                timedSignals.addAll(s);
        }
        
        return timedSignals;
    }

    private static boolean timeMarkerInList(TimeMarker tm, List<TimeMarker> timemarkersToTemporize) {
        for(TimeMarker tminlist : timemarkersToTemporize)
        {if(tm.getValue()==tminlist.getValue())
            return true;
        }
        return false;
    }

    public static List<TimeMarker> orderTimeMarkerList(List<TimeMarker> timeMarkersToOrder) {
        if(timeMarkersToOrder.isEmpty())
            return timeMarkersToOrder;
        
        List<TimeMarker> orderedList = new ArrayList<TimeMarker>();
        
        for(TimeMarker tm : timeMarkersToOrder)
        {
            boolean added=false;
            for(int i=0;i<orderedList.size();i++)
            {
                if(tm.getValue()<=orderedList.get(i).getValue())
                {
                    orderedList.add(i, tm);
                    added=true;
                    break;
                }
            }
            if(!added)
            {
                orderedList.add(tm);
            }
        }
        
        return orderedList;
    }
}
