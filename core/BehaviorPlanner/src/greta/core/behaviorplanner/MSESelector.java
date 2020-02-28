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
package greta.core.behaviorplanner;

import greta.core.behaviorplanner.baseline.DynamicLine;
import greta.core.behaviorplanner.lexicon.BehaviorSet;
import greta.core.behaviorplanner.lexicon.SignalItem;
import greta.core.behaviorplanner.mseconstraints.ConsNode;
import greta.core.behaviorplanner.mseconstraints.ShortSignal;
import greta.core.intentions.Intention;
import greta.core.signals.FaceSignal;
import greta.core.signals.GazeSignal;
import greta.core.signals.HeadSignal;
import greta.core.signals.ParametricSignal;
import greta.core.signals.Signal;
import greta.core.signals.SpeechSignal;
import greta.core.signals.TorsoSignal;
import greta.core.signals.gesture.GestureSignal;
import greta.core.util.CharacterManager;
import greta.core.util.log.Logs;
import greta.core.util.speech.Speech;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

//TODO javadoc
/**
 * Radek's algorithm of Multimodal Sequential Expressions (needs to be
 * implemented)
 *
 * @author Radoslaw Niewiadomski
 */
public class MSESelector implements SignalSelector {

    @Override
    public String getType() {
        return "mse";
    }

    @Override
    public List<Signal> selectFrom(Intention intention, BehaviorSet behaviorSet, DynamicLine dynamicLine, List<Signal> existingSignals, CharacterManager cm) {
        List<Signal> toReturn = new ArrayList<Signal>();

        if (intention instanceof Speech) {
            toReturn.add(new SpeechSignal(cm,(Speech) intention));
            return toReturn;
        }

        if (behaviorSet == null) {
            return toReturn;
        }


        /*
         List<List<SignalItem>> candidates = new ArrayList<List<SignalItem>>();

         //mapSignalItem contains all the available free holes for each modality
         // Map<String,List<Hole>> mapSignalItem = new HashMap<String,List<Hole>>();

         int longest = 0;
         for(List<SignalItem> combination : behaviorSet.getCombinations()){
         //I-1)

         if (combination.size()> longest) {
         //clear the list
         candidates.clear();
         //add this combination
         candidates.add(combination);
         longest=combination.size();
         }
         else
         {
         if (combination.size() == longest){
         //add this combination to the list
         candidates.add(combination);
         }
         }//end of else

         }

         //no combination kept, return an empty selection...
         if(candidates.isEmpty())
         return toReturn;

         //II) select a combination :
         int num_of_longest_combinations = candidates.size();
         int which = (int)( Math.random() * ((num_of_longest_combinations) + 1) );

         */

        //List<SignalItem> longestCombination = behaviorSet.getBaseSignals();

        //for(SignalItem s : longestCombination)
        //{
        //    //System.out.println(s.getId());
        // }

        //another parameter
        toReturn = addtimeconstraints(intention, behaviorSet, dynamicLine, existingSignals);

        return toReturn;

    }

    private List<Signal> addtimeconstraints(Intention intention, BehaviorSet behaviorSet, DynamicLine dynamicLine, List<Signal> existingSignals) {

        //Start ADD TIME CONSTRAINTS

        List<Signal> toReturn = new ArrayList<Signal>();

        // BACK TRACKING IS NOT USED
        // enter is the number of trails
        /*
         if (enter>5) {
         toReturn.clear();
         System.out.println("Sorry, it was not possible to generate MS-expression");
         return toReturn;
         }
         */

        //type or name?


        String reference = behaviorSet.getParamName();

        //System.out.println("reference zzz : " + reference);

        String emotion = reference.substring(reference.indexOf("-") + 1);

        List<ConsNode> mec = behaviorSet.getMSEConstraints();


        //if there is no cons for this emotion
        //nothing to do MultimodalSignal is not changed

//*	if (mec==null) {
//*		Log.warning ("No constraints defined for: emotion");
//*		return null;
//*	}

        //else clone this
//*	MultimodalSignal clone = this->clone();


        //each signal should have time duration at the probability of the occurence, and of repetition
        //if the signal is not present in exe it should have additional constraints defined in te contraint file

//*	std::vector<Signal>::iterator iter;

        //decide the nunber of n-units on the base of baseline and time of commact

        //float start = 0;
        //float stop = 0;

        //what about the timemarkers????
        //find time constraints
//*	for(iter=clone.begin();iter!=clone.end();iter++)
//	{
//*		if (start==0) start =(*iter).start;
//*		else if (start> (*iter).start ) start =(*iter).start;
//*		if (stop < (*iter).start + (*iter).duration ) stop =(*iter).start + (*iter).duration;
//*	}


        double duration = (intention.getEnd().getValue() - intention.getStart().getValue());

//*	float time = stop - start;

        //at the moment we use n = 1 sec
        double n = java.lang.Math.floor(duration);

        //turn number
        double turn = 0.0;

        //use OAC but wedo not have at the moment
        double turn_increaser = (1.0 - 0.5) * 0.25;

        //time of one turn - at the moment it is about 1 sec
        double time_unit = duration / n;

        // cancel the signals that are shorter than one unit and create probability list

        //to do

        //create vectors of probabilities
        //+1 becaese of index from 1 to clone.size

        //how many signals we have (without alternatives)
        //int probabilities_size = (int) behaviorSet.getBaseSignals().size()+1;

        //Hashtable<String, Integer> mecs = new Hashtable<String, Integer>();
        HashMap<String, Double> probabilities_start = new HashMap<String, Double>();
        HashMap<String, Double> probabilities_end = new HashMap<String, Double>();
        HashMap<String, Double> current_probabilities = new HashMap<String, Double>();

        int index1 = 1;

        //to do: check if signals are defined, if not out from the list


        for (SignalItem signalitem : behaviorSet.getBaseSignals()) {
            //take a reference name - is it reference or signal name?
            String temp = signalitem.getMainShape().getName();

            //maximal duration of the signal
            double max_duration = signalitem.getMainShape().getMax();

            //check it
            if (max_duration < time_unit) {

                //temporal solution
                //put 0 for the probability

                //String temp_name= findName(temp);


                //probabilities_start[temp] = 0.0;
                probabilities_start.put(temp, 0.0);
                //probabilities_end[temp] = 0.0;
                probabilities_end.put(temp, 0.0);
                //current_probabilities[temp] =  0.0;
                current_probabilities.put(temp, 0.0);

            } else {

                //update the start and end probabilities

//*			std::string temp_name= findName(temp);

                probabilities_start.put(temp, signalitem.getMainShape().getProbability_Start());
                probabilities_end.put(temp, signalitem.getMainShape().getProbability_Stop());
                current_probabilities.put(temp, signalitem.getMainShape().getProbability_Start());

                //attention: some probabilities [x][y] are 0 = we cannot not use corresponding signal x
            } //end of else

        } //end of for


        //clone is a full list of signals at disposal
        //temporal is like clone but with some signals cancelled (=avoid infinite loop)

        //generate a  temporal list of signals
        List<SignalItem> temporal = behaviorSet.getBaseSignals();

        //start of the algortithm
        int singals_in_turn = 0;

        FaceSignal startsignal = new FaceSignal("neutral_start");
        //add reference
        startsignal.setReference("faceexp=neutral");
        //add start time

        //ATENTION SHOULD START OF THE SIGNAL i.e. ABSOLUTE!!!! + intention_start_time !!
        startsignal.getStart().setValue(0);
        //add end time

        //ATENTION SHOULD START OF THE SIGNAL i.e. ABSOLUTE!!!! + intention_start_time !!
        startsignal.getEnd().setValue(1);
        //add other markers??

        //TO DO: check if there are conflicts with existing signals???
        toReturn.add(startsignal);

        //turn=turn+turn_increaser;

        double last_added_time = 0;

        while (turn < n) {
            boolean success = false;

            //A. choose the signal (consider the probability of occurence)

            //choose from temporal

            if (!(temporal.isEmpty())) {

                Random r = new Random();
                double result = r.nextDouble();

                List<SignalItem> short_list = new ArrayList<SignalItem>();

                for (SignalItem iter : temporal) { //for all signals in temporal

                    //printf("temP %s \n", (*iter).reference.c_str() );
                    //printf("prob %f '\n", current_probabilities[findName(iter->reference)] );

                    //result is a random value
                    double current_value = ((Double) current_probabilities.get(iter.getMainShape().getName())).doubleValue();
                    if (current_value > result) {
                        ///printf(" added %s \n", (*iter) .reference.c_str());
                        short_list.add(iter);

                    }//end of if
                }//end of for all temporals

                if (short_list.isEmpty()) {
                    //if short list is empty so there is no signal to be shown;
                    // we incease the time, update probabilities and go to next turn

                    //all is possible in next turn
                    //??
                    temporal = behaviorSet.getBaseSignals();

                    //turn++;
                    turn = turn + turn_increaser;

                    //refresh probability current!!!

                    //update the probabilities next turn probabilities
                    double factor = (double) ((double) (turn + turn_increaser) / (double) n);

                    if ((factor < 1.0d) && (factor >= 0.0d)) {
                        //for all signals in behavior set - upadate corresponding probabilities
                        for (SignalItem signalitem : behaviorSet.getBaseSignals()) {

                            //get full name of the signal e.g. faceexp=anger_signal_1
                            String temp_name = signalitem.getMainShape().getName();

                            //three cases:
                            double start_value = ((Double) probabilities_start.get(temp_name)).doubleValue();
                            double stop_value = ((Double) probabilities_end.get(temp_name)).doubleValue();

                            //start=end
                            //if (probabilities_start[temp_name]==probabilities_end[temp_name]) current_probabilities[temp_name] = probabilities_start[temp_name] ;
                            if (start_value == stop_value) {
                                current_probabilities.put(temp_name, start_value);
                            }


                            //start < end
                            //if (probabilities_start[temp_name] < probabilities_end[temp_name])
                            //{
                            //	float distance = probabilities_end[temp_name]-probabilities_start[temp_name] ;
                            //	current_probabilities[temp_name] = probabilities_start[temp_name] + distance * factor ;
                            //}

                            if (start_value < stop_value) {
                                double distance = stop_value - start_value;
                                double temp_value = start_value + distance * factor;
                                current_probabilities.put(temp_name, temp_value);

                            }


                            //start > end
                            //if (probabilities_start[temp_name] > probabilities_end[temp_name])
                            //{
                            //	float distance = probabilities_start[temp_name] - probabilities_end[temp_name];
                            //	current_probabilities[temp_name] = probabilities_start[temp_name] - distance * factor ;
                            //}

                            if (start_value > stop_value) {
                                double distance = start_value - stop_value;
                                double temp_value = start_value - distance * factor;
                                current_probabilities.put(temp_name, temp_value);

                            }

                            //if (current_probabilities[temp_name]>1.0f) current_probabilities[temp_name]=1;
                            //if (current_probabilities[temp_name]<0.0f) current_probabilities[temp_name]=0;

                            double current_value = ((Double) current_probabilities.get(temp_name)).doubleValue();

                            if (current_value > 1.0d) {
                                current_probabilities.put(temp_name, 1.0d);
                            }
                            if (current_value < 0.0d) {
                                current_probabilities.put(temp_name, 0.0d);
                            }

                        }//end of for

                    }//end of if factor


                } else {

                    //Random r = new Random();
                    result = r.nextDouble();

                    SignalItem chosen = new SignalItem();

                    for (int i = 1; i <= (int) (short_list.size()); i++) {

                        double lower_interval = ((double) 1 / short_list.size()) * (double) (i - 1);
                        double upper_interval = ((double) 1 / short_list.size()) * (double) (i);

                        if ((lower_interval < result) && (result < upper_interval)) {
                            chosen = (SignalItem) short_list.get(i - 1);

                            i = (int) short_list.size() + 1; //exit from for
                        }
                    } //end of for

                    //Random r = new Random();
                    result = r.nextDouble();

                    //ATTENTION RELATIVE TIME
                    double begin_time_chosen_signal = (turn + result) * time_unit;

                    //check the contraints with "this" (does the signal can be the first one or it has to preceded by the other signal)

                    //if the signal starts before the other of the same id is finished then the second one is illegal
                    boolean goodornot = true;

                    for (Signal temp_signal : toReturn) {
                        if (temp_signal instanceof ParametricSignal) {

                            ParametricSignal param_temp_signal = (ParametricSignal) temp_signal;

                            //getreference of the signal
                            //getClass().getName()

                            String temp_ref = param_temp_signal.getReference();

                            //TimeMarker temp_start = temp_signal.getStart();
                            TimeMarker temp_end = temp_signal.getEnd();

                            if (temp_ref.equalsIgnoreCase(chosen.getMainShape().getName()) && (temp_end.getValue() > begin_time_chosen_signal)) {
                                goodornot = false;
                            }

                        }//end of if instanceof

                    }//end for

                    Logs.debug("Candidate's id: " + chosen.getMainShape().getName());


                    // no time back
                    // no continuation of existing signal
                    // cons are satisfied

                    if ((begin_time_chosen_signal >= last_added_time) && (goodornot == true) && (checkCons(chosen.getMainShape().getName(), begin_time_chosen_signal, 0, mec, behaviorSet, toReturn) == true)) //if (checkStartCons(chosen->reference, begin_time,  mec )==true)
                    {

                        Logs.debug("Proposed start time : " + begin_time_chosen_signal);

                        //choose the duration and check contraints again

                        double max_duration = chosen.getMainShape().getMax();
                        double min_duration = chosen.getMainShape().getMin();

                        double distance = max_duration - min_duration;

                        //r = new Random();
                        result = r.nextDouble();

                        result = java.lang.Math.sqrt(result);

                        double x1 = min_duration + (result * distance) / 2.0d;
                        double x2 = max_duration - (result * distance) / 2.0d;

                        //printf("min %f, max %f, x1, %f, x2 %f \n", min_duration, max_duration, begin_time + x1, begin_time + x2);

                        //duration
                        double duration_time_chosen_signal = 0.0d;

                        //r = new Random();
                        result = r.nextDouble();


                        if (result > 0.5d) {
                            duration_time_chosen_signal = x1;
                        } else {
                            duration_time_chosen_signal = x2;
                        }

                        //is it a good duration_time?
                        if ((duration_time_chosen_signal > 0) && (begin_time_chosen_signal + duration_time_chosen_signal <= duration) && (checkCons(chosen.getMainShape().getName(), begin_time_chosen_signal, duration_time_chosen_signal, mec, behaviorSet, toReturn) == true)) //if ( ( checkStopCons (chosen->reference, begin_time, duration_time, mec ) == true) && ( begin_time + duration_time <= time ) )
                        {

                            // duration is ok and endcons are true...
                            Logs.debug("Proposed duration: " + duration_time_chosen_signal);

                            //be sure that next signal starts non earlier than this one
                            last_added_time = begin_time_chosen_signal;

                            //set start and stop
//*						float_chosen->start=begin_time;
                            //chosen.start_sym

//*						chosen->duration = duration_time;
                            //chosen.duration_sym


                            //!!! IMPORTANT CREATE CORRECT SIGNAL
                            if (chosen.getModality().equalsIgnoreCase("face")) {
                                FaceSignal new_signal = new FaceSignal(chosen.getModality() + begin_time_chosen_signal);

                                //add reference
                                new_signal.setReference(chosen.getMainShape().getName());

                                //add start time
                                new_signal.getStart().setValue(begin_time_chosen_signal);

                                //add end time
                                new_signal.getEnd().setValue(begin_time_chosen_signal + duration_time_chosen_signal);

                                //add other markers??

                                toReturn.add(new_signal);
                                singals_in_turn++;

                            }


                            if (chosen.getModality().equalsIgnoreCase("gaze")) {
                                GazeSignal new_signal = new GazeSignal(chosen.getModality() + begin_time_chosen_signal);

                                //add reference
                                new_signal.setReference(chosen.getMainShape().getName());

                                //add start time
                                new_signal.getStart().setValue(begin_time_chosen_signal);

                                //add end time
                                new_signal.getEnd().setValue(begin_time_chosen_signal + duration_time_chosen_signal);


                                //add other markers??

                                toReturn.add(new_signal);
                                singals_in_turn++;

                            }

                            if (chosen.getModality().equalsIgnoreCase("gesture")) {
                                GestureSignal new_signal = new GestureSignal(chosen.getModality() + begin_time_chosen_signal);

                                //add reference
                                new_signal.setReference(chosen.getMainShape().getName());

                                //add id

                                //add start time
                                new_signal.getStart().setValue(begin_time_chosen_signal);

                                //add end time
                                new_signal.getEnd().setValue(begin_time_chosen_signal + duration_time_chosen_signal);


                                //add other markers??

                                //add strokes?

                                toReturn.add(new_signal);
                                singals_in_turn++;

                            }

                            if (chosen.getModality().equalsIgnoreCase("torso")) {
                                TorsoSignal new_signal = new TorsoSignal(chosen.getModality() + begin_time_chosen_signal);

                                //add reference
                                new_signal.setReference(chosen.getMainShape().getName());

                                //add start time
                                new_signal.getStart().setValue(begin_time_chosen_signal);

                                //add end time
                                new_signal.getEnd().setValue(begin_time_chosen_signal + duration_time_chosen_signal);


                                //add other markers??

                                //add strokes?


                                toReturn.add(new_signal);
                                singals_in_turn++;

                            }

                            if (chosen.getModality().equalsIgnoreCase("head")) {
                                HeadSignal new_signal = new HeadSignal(chosen.getModality() + begin_time_chosen_signal);

                                //add reference
                                new_signal.setReference(chosen.getMainShape().getName());


                                //add start time
                                new_signal.getStart().setValue(begin_time_chosen_signal);

                                //add end time
                                new_signal.getEnd().setValue(begin_time_chosen_signal + duration_time_chosen_signal);

                                //add other markers??

                                //add strokes?

                                toReturn.add(new_signal);
                                singals_in_turn++;

                            }


//*                                                this->push_back(*chosen);

                            //we fill both sides
                            //the last signal of the id=id is in a tree
                            //the alg is ok if there are no signals of the same id that overlap

                            //check it!
                            //fill constreee with if the signal added to list

                            fill(mec, behaviorSet.getIdofSignal(chosen.getMainShape().getName()), begin_time_chosen_signal, duration_time_chosen_signal);

                            //UPADATE the probabilities : (consider the possibility of repetition, (???of the continuation!!!!,) change the probabilities of occurence)

                            double factor = (double) ((double) (turn + turn_increaser) / (double) n);

                            if ((factor < 1.0d) && (factor >= 0.0d)) {

                                //for all signals in clone
                                for (SignalItem signalitem : behaviorSet.getBaseSignals()) {

                                    String temp_name = signalitem.getMainShape().getName();
                                    String chosen_name = chosen.getMainShape().getName();

                                    float current_repetivity = chosen.getMainShape().getRepetivity();

                                    //if it is an emement that can not be repeated - I put 0 as a probability
                                    if (temp_name.equalsIgnoreCase(chosen_name)) {
                                        if (current_repetivity == 0.0d) {
                                            Logs.debug("no reperetition possible:" + temp_name + " removed form  a list");
                                            probabilities_start.put(temp_name, 0.0d);
                                            probabilities_end.put(temp_name, 0.0d);
                                        }
                                    }

                                    //eliminate ifnotthenlist
                                    String excludelist = chosen.getMainShape().getExcludeList();

                                    java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(excludelist, ",");
                                    while (tokenizer.hasMoreTokens()) {

                                        String temp_name1 = signalitem.getMainShape().getName();
                                        if (temp_name1.equalsIgnoreCase(tokenizer.nextToken())) {
                                            //OUT
                                            Logs.debug("Incopatible Signal" + temp_name + " removed form  a list");
                                            probabilities_start.put(temp_name1, 0.0d);
                                            probabilities_end.put(temp_name1, 0.0d);
                                        }
                                    }//end of while

                                    //update probabilities

                                    //three cases:
                                    double start_value = ((Double) probabilities_start.get(temp_name)).doubleValue();
                                    double stop_value = ((Double) probabilities_end.get(temp_name)).doubleValue();

                                    //start=end
                                    //if (probabilities_start[temp_name]==probabilities_end[temp_name]) current_probabilities[temp_name] = probabilities_start[temp_name] ;
                                    if (start_value == stop_value) {
                                        current_probabilities.put(temp_name, start_value);
                                    }


                                    //start < end
                                    //if (probabilities_start[temp_name] < probabilities_end[temp_name])
                                    //{
                                    //	float distance = probabilities_end[temp_name]-probabilities_start[temp_name] ;
                                    //	current_probabilities[temp_name] = probabilities_start[temp_name] + distance * factor ;
                                    //}

                                    if (start_value < stop_value) {
                                        distance = stop_value - start_value;
                                        double temp_value = start_value + distance * factor;
                                        current_probabilities.put(temp_name, temp_value);

                                    }


                                    //start > end
                                    //if (probabilities_start[temp_name] > probabilities_end[temp_name])
                                    //{
                                    //	float distance = probabilities_start[temp_name] - probabilities_end[temp_name];
                                    //	current_probabilities[temp_name] = probabilities_start[temp_name] - distance * factor ;
                                    //}

                                    if (start_value > stop_value) {
                                        distance = start_value - stop_value;
                                        double temp_value = start_value - distance * factor;
                                        current_probabilities.put(temp_name, temp_value);

                                    }

                                    //if (current_probabilities[temp_name]>1.0f) current_probabilities[temp_name]=1;
                                    //if (current_probabilities[temp_name]<0.0f) current_probabilities[temp_name]=0;

                                    double current_value = ((Double) current_probabilities.get(temp_name)).doubleValue();

                                    if (current_value > 1.0d) {
                                        current_probabilities.put(temp_name, 1.0d);
                                    }
                                    if (current_value < 0.0d) {
                                        current_probabilities.put(temp_name, 0.0d);
                                    }


                                }//end of for

                            }//end of if factor

                            //after adding a signal and updating the prob
                            //do we want to add a new signal in this turn

                            //which parameters should be used chosen.getMainShape().   .parameters["OAC.value"].GetValue();

                            double overall = dynamicLine.getParameter(chosen.getModality(), "OAC").getValue();

                            //3 - becouse max 3 signals at the moment possible - true ?
                            //supposing that the OAC is from the interval -1,1

                            //ATTENTION : what is that??
                            if (((double) (overall + 1) * (double) (3 - singals_in_turn)) > 2.0d) {
                                //DEBUG IT

                                //do not use it again in this turn
                                String temp_name = chosen.getMainShape().getName();
                                current_probabilities.put(temp_name, 0.0d);

                                success = true;
                                //continue; // I DO NOT EXIT to while....
                            } else {

                                //all is possible in next turn

                                //??
                                temporal = behaviorSet.getBaseSignals();


                                //turn++; //or more if the expressivity is low
                                turn = turn + turn_increaser;

                                //refresh probability current!!!

                                //update the probabilities next turn probabilities
                                factor = (double) ((double) (turn + turn_increaser) / (double) n);

                                if ((factor < 1.0d) && (factor >= 0.0d)) {
                                    //for all signals in clone
                                    for (SignalItem signalitem : behaviorSet.getBaseSignals()) {

                                        String temp_name = signalitem.getMainShape().getName();


                                        //three cases:
                                        double start_value = ((Double) probabilities_start.get(temp_name)).doubleValue();
                                        double stop_value = ((Double) probabilities_end.get(temp_name)).doubleValue();

                                        //start=end
                                        //if (probabilities_start[temp_name]==probabilities_end[temp_name]) current_probabilities[temp_name] = probabilities_start[temp_name] ;
                                        if (start_value == stop_value) {
                                            current_probabilities.put(temp_name, start_value);
                                        }


                                        //start < end
                                        //if (probabilities_start[temp_name] < probabilities_end[temp_name])
                                        //{
                                        //	float distance = probabilities_end[temp_name]-probabilities_start[temp_name] ;
                                        //	current_probabilities[temp_name] = probabilities_start[temp_name] + distance * factor ;
                                        //}

                                        if (start_value < stop_value) {
                                            distance = stop_value - start_value;
                                            double temp_value = start_value + distance * factor;
                                            current_probabilities.put(temp_name, temp_value);

                                        }


                                        //start > end
                                        //if (probabilities_start[temp_name] > probabilities_end[temp_name])
                                        //{
                                        //	float distance = probabilities_start[temp_name] - probabilities_end[temp_name];
                                        //	current_probabilities[temp_name] = probabilities_start[temp_name] - distance * factor ;
                                        //}

                                        if (start_value > stop_value) {
                                            distance = start_value - stop_value;
                                            double temp_value = start_value - distance * factor;
                                            current_probabilities.put(temp_name, temp_value);

                                        }

                                        //if (current_probabilities[temp_name]>1.0f) current_probabilities[temp_name]=1;
                                        //if (current_probabilities[temp_name]<0.0f) current_probabilities[temp_name]=0;

                                        double current_value = ((Double) current_probabilities.get(temp_name)).doubleValue();

                                        if (current_value > 1.0d) {
                                            current_probabilities.put(temp_name, 1.0d);
                                        }
                                        if (current_value < 0.0d) {
                                            current_probabilities.put(temp_name, 0.0d);
                                        }

                                    }//end of for

                                }//end of if factor
                                success = true;
                            }//end of else

                        }//end of if checkstop is true
                        else {
                            //if we are here it means that chosen is not a good signal becouse it cannot be finshed in time
                            //the signal duration is too long or
                            //the endcons are not satisfied
                            //CHOOSE DIFFERENT duration_time
                            //refaire checkend...
                            //do
                            //divide duration_time by two till is > min.duration
                            //while {endtime < min.duration or checkend==ok)
                            //REFAIRE TOUT???
                        }//end of else checkstop true

                    } // end if checkstart == true
                    else //check start is false....
                    {

                        //if we are here it means that chosen is not a good signal becouse it cannot be started

                        //cons for start can not be satisfied!

                        //so: cancel a from the proposal list and choose a signal again

                        //do not use it again in this turn
                        String temp_name = chosen.getMainShape().getName();
                        current_probabilities.put(temp_name, 0.0d);

                    }//end of else

                }//end of else if short list is empty

            }// end of	(!(temporal.empty()))
            else {

                //IF TEMPORAL IS EMPTY:

                //all is possible in next turn
                temporal = behaviorSet.getBaseSignals();

                //turn++;
                turn = turn + turn_increaser;

                //refresh probability current!!!

                //update the probabilities next turn probabilities
                double factor = (double) ((double) (turn + turn_increaser) / (double) n);

                if ((factor < 1.0d) && (factor >= 0.0d)) {
                    for (SignalItem signalitem : behaviorSet.getBaseSignals()) {

                        String temp_name = signalitem.getMainShape().getName();


                        //three cases:
                        double start_value = ((Double) probabilities_start.get(temp_name)).doubleValue();
                        double stop_value = ((Double) probabilities_end.get(temp_name)).doubleValue();

                        //start=end
                        //if (probabilities_start[temp_name]==probabilities_end[temp_name]) current_probabilities[temp_name] = probabilities_start[temp_name] ;
                        if (start_value == stop_value) {
                            current_probabilities.put(temp_name, start_value);
                        }


                        //start < end
                        //if (probabilities_start[temp_name] < probabilities_end[temp_name])
                        //{
                        //	float distance = probabilities_end[temp_name]-probabilities_start[temp_name] ;
                        //	current_probabilities[temp_name] = probabilities_start[temp_name] + distance * factor ;
                        //}

                        if (start_value < stop_value) {
                            double distance = stop_value - start_value;
                            double temp_value = start_value + distance * factor;
                            current_probabilities.put(temp_name, temp_value);

                        }


                        //start > end
                        //if (probabilities_start[temp_name] > probabilities_end[temp_name])
                        //{
                        //	float distance = probabilities_start[temp_name] - probabilities_end[temp_name];
                        //	current_probabilities[temp_name] = probabilities_start[temp_name] - distance * factor ;
                        //}

                        if (start_value > stop_value) {
                            double distance = start_value - stop_value;
                            double temp_value = start_value - distance * factor;
                            current_probabilities.put(temp_name, temp_value);

                        }

                        //if (current_probabilities[temp_name]>1.0f) current_probabilities[temp_name]=1;
                        //if (current_probabilities[temp_name]<0.0f) current_probabilities[temp_name]=0;

                        double current_value = ((Double) current_probabilities.get(temp_name)).doubleValue();

                        if (current_value > 1.0d) {
                            current_probabilities.put(temp_name, 1.0d);
                        }
                        if (current_value < 0.0d) {
                            current_probabilities.put(temp_name, 0.0d);
                        }

                    }//end of for

                }//end of if factor


            }

        }// End of while


        //add the empty singal at the end if THERE IS A LOt OF EMPTY SPACE at end
        if (turn * time_unit < duration - 0.1) {

            FaceSignal stopsignal = new FaceSignal("neutral_end");
            //add reference
            stopsignal.setReference("faceexp=neutral");
            //add start time
            stopsignal.getStart().setValue(turn * time_unit);
            //add end time
            stopsignal.getEnd().setValue(duration);
            //add other markers??

            //TO DO: check if there are conflicts with existing signals???
            toReturn.add(stopsignal);

        }//end of if

        //to do: fill gaps

        //refill constrees
        clean(mec);

        //check how many signals if the resultat is poor think about recurency
        //recurency if the result is poor
        if (toReturn.size() < duration * 0.3) {


            Logs.warning("MSE The resultat is poor ");

            /*emergency.addTimeConstraints(emotion,++enter);
             this->clear();

             std::vector<Signal>::iterator iterxxx4;

             for(iterxxx4=emergency.begin();iterxxx4!=emergency.end();iterxxx4++)
             {
             Signal s(*iterxxx4);
             this->push_back(s);
             }*/

        }

        //clean procedures:

        //clean emergency

        return toReturn;

        //STOP ADD TIME CONSTRAINTS
    }//end of add time constraints

    private void fill(List<ConsNode> constree, int id, double start_time, double stop_time) {

        //for every constree...
        for (ConsNode itr : constree) {
            itr.fill(id, start_time, stop_time);
        }
    }

    private void clean(List<ConsNode> constree) {
        //for every constree...
        for (ConsNode itr : constree) {
            itr.clean();
        }
    }

    boolean checkCons(String reference, double begin_time, double duration_time, List<ConsNode> good_cons, BehaviorSet set, List<Signal> toReturn) {


        //System.out.println("reference" + reference);


        //start or end cons
        boolean end_cons = false;

        if (duration_time == 0) {
            end_cons = false;
        } else {
            end_cons = true;
        }

        //get all relevant cons
        //std::vector<ConsNode*>* good_cons = mec->getNewConstraintsOfOneSignal(reference);

        //NEW VERSION MAY 2011
        //get all  cons	- important if we want to force existence of some signals before others!!!
        //List<ConsNode> good_cons = set.getMSEConstraints();

        int id = set.getIdofSignal(reference);

        //clone good_cons
        //for security we do not want to have left part
        //even if on the left part it should not be nothing dangerous
        // the rules A or (B and C) are not approved

        List<ConsNode> cloned_good_cons = new ArrayList<ConsNode>();

        //clone good_cons
        for (ConsNode itr : good_cons) {
            cloned_good_cons.add(itr.copy());
        }

        //result
        // 0 - false, 1 - true

        int satisfied = 1;

        //for all relevant cons

        //cons_iter is one constreee

        // redefinition : std::vector<ConsNode*>::iterator cons_iter;

        //find the current list of signals with constraints and put in to vector of the type ConstraintSignal
        List<ShortSignal> temp_animation = new ArrayList<ShortSignal>();

        for (Signal signal : toReturn) {

            double current_signal_start = signal.getStart().getValue();
            double current_signal_end = signal.getEnd().getValue();

            //? is instantiated
            if ((current_signal_start >= 0) && (current_signal_start < current_signal_end)) {
                if (signal instanceof ParametricSignal) {
                    ParametricSignal param_temp_signal = (ParametricSignal) signal;

                    //create temp_cs
                    ShortSignal temp = new ShortSignal();

                    temp.setStart(param_temp_signal.getStart().getValue());
                    temp.setEnd(param_temp_signal.getEnd().getValue());

                    //check it
                    temp.setLabel(param_temp_signal.getReference());
                    temp.setId(set.getIdofSignal(param_temp_signal.getId()));

                    temp_animation.add(temp);
                }
            }

        }

        int help = 1;

        for (ConsNode itr : cloned_good_cons) {

            //we fill both sides
            //the last signal of the id=id is in a tree
            //the alg is ok if there are no signals of the same id that overlap
            //check it!

            itr.fill(id, begin_time, duration_time);
            boolean wynik = itr.evaluate(id, temp_animation);
            if (wynik == false) {
                satisfied = 0;
            }

            //#ifdef RDEBUG
            //if(satisfied==0) help--;
            //if(help==0)	printf(" rule not safisfied is %s \n", ((*cons_iter)->id).c_str());
            //#endif


        }//end for

        //cloned_good_cons empty

        //check it;
//*	cloned_good_cons->clear();

        Logs.debug("is satisfied: " + satisfied + "begin_time: " + begin_time + "duration_time: " + duration_time);


        if (satisfied == 1) {

            //we fill both sides
            //the last signal of the id=id is in a tree
            //the alg is ok if there are no signals of the same id that overlap
            //check it!
            //fill constree if added to list

            //if only at the end of testing no!!!
            //if (duration_time>0) mec->fill(id, begin_time,duration_time);

            return true;

        }

        //else
        return false;

    }//end of checkcons

    public boolean acceptIntention(Intention intention, List<Intention> context) {
        //there is no synchronisation with speech in the algorith
        //so when speech is context, we refuse.
        //it may be change later (i.e. return false if non constraints found for the Intention)

        return false;

        /*

         for(Intention contextIntention : context) {
         if(contextIntention instanceof Speech) {
         return false;
         }
         }

         System.out.println("intention: " + intention.getName() + " END: "+intention.getEnd().getValue());
         System.out.println("START: "+intention.getStart().getValue());

         if ( (intention.getEnd().getValue() - intention.getStart().getValue()) < 5.0 ) {
         return false;
         }

         if ( (intention.getEnd().getValue() - intention.getStart().getValue()) < 5.0 ) {
         return false;
         }

         return true;

         */


    }
}
