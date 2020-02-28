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

import greta.core.util.enums.Side;
import greta.core.util.log.Logs;
import greta.core.util.math.Functions;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains shoulder movement description
 *
 * @author Radoslaw Niewiadomski
 */
public class ShoulderSignal extends ParametricSignal {

    //from ParametricSignal
    //private String category - type: shoulder
    //private String reference - experession name up/front/back/shake
    //private double intensity - amount
    //face tag identificator

    private String id;
    private double repetition;
    private Side side;

    private double front;
    private double up;

    //real or normalized values?
    private int normalized; // 0 or 1
    //torso included or not // 0 or 1
    private int torso;

    private TimeMarker start;
    private TimeMarker attack;
    private TimeMarker decay;
    private TimeMarker sustain;
    private TimeMarker end;
    //list of 5 time markers
    private List<TimeMarker> timeMarkers;
    //private ArrayList<ShoulderPhase> phases; // start, attack, sustain, end
    //some signals cant be scheduled
    private boolean is_scheduled = false;

    public boolean isScheduled() {
        return is_scheduled;
    }

    public ShoulderSignal(String id) {
        this.id = id;
        timeMarkers = new ArrayList<TimeMarker>(5);
        start = new TimeMarker("start");
        timeMarkers.add(start);
        attack = new TimeMarker("attack");
        timeMarkers.add(attack);
        decay = new TimeMarker("decay");
        timeMarkers.add(decay);
        sustain = new TimeMarker("sustain");
        timeMarkers.add(sustain);
        end = new TimeMarker("end");
        timeMarkers.add(end);
        side = Side.BOTH;

        front = 0.0;
        up = 0.0d;

        //phases = new ArrayList<SpinePhase>();
    }

    @Override
    public String getModality() {
        return "shoulder";
    }

    @Override
    public List<TimeMarker> getTimeMarkers() {
        return timeMarkers;
    }

    @Override
    public TimeMarker getTimeMarker(String name) {

        if (name.equalsIgnoreCase("start")) {
            return start;
        }
        if ((name.equalsIgnoreCase("attack")) || (name.equalsIgnoreCase("ready"))) {
            return attack;
        }
        if (name.equalsIgnoreCase("decay")) {
            return decay;
        }
        if ((name.equalsIgnoreCase("sustain")) || (name.equalsIgnoreCase("relax"))) {
            return sustain;
        }
        if (name.equalsIgnoreCase("end")) {
            return end;
        }

        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    public double getStartValue() {
        return this.start.getValue();
    }

    public double getEndValue() {
        return this.end.getValue();
    }

   public int getMode() {
        return normalized;
    }


    public int getTorsoIn() {
        return torso;
    }

    public void updateFromLibrary() {
        if (getReference().equalsIgnoreCase("up")) {
            up = getIntensity();
            front = 0.0;
        }
        if (getReference().equalsIgnoreCase("back")) {
            front = (getIntensity() * (-1));
            up = 0.0;
        }
        if (getReference().equalsIgnoreCase("front")) {
            front = getIntensity();
            up = 0.0;
        }
    }

    private double checkTimming(TimeMarker first, TimeMarker second) throws Exception {
        double duration = first.isConcretized() && second.isConcretized() ? second.getValue() - first.getValue() : Double.NEGATIVE_INFINITY;
        if (Double.NEGATIVE_INFINITY < duration && duration < 0) {
            throw new Exception("Timming error in face " + getId() + ": " + second.getName() + " is before " + first.getName());
        }
        return duration;
    }

    @Override
    public void schedule() {

        is_scheduled = false;

        //<editor-fold defaultstate="collapsed" desc="AM code">
        //*
        try {
            double st2at = checkTimming(start, attack);
            double at2de = checkTimming(attack, decay);
            double de2su = checkTimming(decay, sustain);//we have a high flexibility on this value
            double su2en = checkTimming(sustain, end);

            double st2de = checkTimming(start, decay);
            double st2su = checkTimming(start, sustain);
            double st2en = checkTimming(start, end);
            double at2su = checkTimming(attack, sustain);
            double at2en = checkTimming(attack, end);
            double de2en = checkTimming(decay, end);

            //TODO compute the 4 first durations
            double st2atExpected = 0.2 * warpIntensity() * warpTMP(); //this 0.2 means 0.2 seconds for the higher intensity and tmp
            double at2deExpected = 0.1 * warpIntensity() * warpTMP(); //this 0.1 means 0.1 seconds for the higher intensity and tmp
            double de2suExpected = 0.5 * warpIntensity() * warpTMP(); //this 0.5 means 0.5 seconds for the higher intensity and tmp
            double su2enExpected = 0.2 * warpIntensity() * warpTMP(); //this 0.2 means 0.2 seconds for the higher intensity and tmp

            if (st2at < 0) {
                if (!start.isConcretized()) {
                    st2at = st2atExpected;
                } else {
                    //we know that start is concrete and attack is not concrete, at2... are not set and some st2... are may be set
                    if(st2de>=0){
                        if(st2atExpected+at2deExpected > st2de){
                            st2at = 0.7*st2de;
                            at2de = 0.3*st2de;
                        } else {
                            st2at = st2atExpected;
                            at2de = st2de-st2at;
                        }
                    } else {
                        //here decay is not concrete
                        if(st2su>=0){
                            if(st2atExpected+at2deExpected > st2su){
                                st2at = 0.7*st2su;
                                at2de = 0.3*st2su;
                                de2su = 0;
                            } else {
                                st2at = st2atExpected;
                                at2de = at2deExpected;
                                de2su = st2su - (st2at+at2de);
                            }
                        } else {
                            //here sustain is not concrete
                            if(st2en>=0){
                                if(st2atExpected+at2deExpected+su2enExpected > st2en){
                                    st2at = 0.4*st2en;
                                    at2de = 0.2*st2en;
                                    de2su = 0;
                                    su2en = 0.4*st2en;
                                } else {
                                    st2at = st2atExpected;
                                    at2de = at2deExpected;
                                    su2en = su2enExpected;
                                    de2su = st2en - (st2at+at2de+su2en);
                                }
                            } else {
                                st2at = st2atExpected;
                                at2de = at2deExpected;
                                de2su = de2suExpected;
                                su2en = su2enExpected;
                            }
                        }
                    }
                }
            }
            //here st2at is known
            if (su2en < 0) {
                if( ! end.isConcretized()){
                    su2en = su2enExpected;
                } else{
                    //we know that end is concrete and sustain is not concrete
                    if(de2en>=0){
                        if(su2enExpected > de2en){
                            su2en = de2en;
                            de2su = 0;
                        } else {
                            su2en = su2enExpected;
                            de2su = de2en-su2en;
                        }
                    } else {
                        //decay in not concrete
                        //start or attack may be concrete and we know st2at
                        //if both are not concrete, we have all the time we want.
                        //else at2en = at2en>=0 ? at2en : st2en - st2at
                        if(start.isConcretized() || attack.isConcretized()){
                            at2en = at2en>=0 ? at2en : st2en - st2at;
                            if(at2deExpected+su2enExpected > at2en){
                                at2de = 0.3*at2en;
                                su2en = 0.7*at2en;
                                de2su = 0;
                            } else {
                                at2de = at2deExpected;
                                su2en = su2enExpected;
                                de2su = at2en - (at2de+su2en);
                            }
                        } else {
                            at2de = at2deExpected;
                            de2su = de2suExpected;
                            su2en = su2enExpected;
                        }
                    }
                }
            }
            //here st2at and su2en are known
            if(at2de<0){
                if(decay.isConcretized()){
                    //here start and attack are not concrete
                    at2de = at2deExpected;
                } else {
                    //here decay and end are not concrete
                    if(!sustain.isConcretized()){
                        //we have all the time
                        at2de = at2deExpected;
                        de2su = de2suExpected;
                    } else {
                        if(attack.isConcretized()){
                            //at2su is known
                            if(at2su<at2deExpected){
                                at2de = at2su;
                                de2su = 0;
                            } else {
                                at2de = at2deExpected;
                                de2su = at2su - at2de;
                            }
                        } else {
                            //here start attack and decay are not concrete
                            at2de = at2deExpected;
                            de2su = de2suExpected;
                        }
                    }
                }
            }
            if(de2su<0){
                //here sustain and end are not concrete
                de2su = de2suExpected;
            }

            start.addReference(attack, -st2at);
            start.addReference(decay, -st2at - at2de);
            start.addReference(sustain, -st2at - at2de - de2su);
            start.addReference(end, -st2at - at2de - de2su - su2en);
            if (!start.concretizeByReferences()) {
                //no TimeMarkers are concrete:
                //we put a default start value
                start.setValue(0);
            }

            attack.addReference(start, st2at);
            attack.concretizeByReferences();

            decay.addReference(start, st2at + at2de);
            decay.concretizeByReferences();

            sustain.addReference(start, st2at + at2de + de2su);
            sustain.concretizeByReferences();

            end.addReference(start, st2at + at2de + de2su + su2en);
            end.concretizeByReferences();
        } catch (Exception e) {
            Logs.error(e.getMessage());
            return;
        }
        //*/
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Radek code">
        /*
        int any_problem = 0;

        if (start.isConcretized() && attack.isConcretized() && start.getValue() >= attack.getValue()) {
            any_problem = 1;
        }
        if (start.isConcretized() && decay.isConcretized() && start.getValue() >= decay.getValue()) {
            any_problem = 1;
        }
        if (start.isConcretized() && sustain.isConcretized() && start.getValue() >= sustain.getValue()) {
            any_problem = 1;
        }
        if (start.isConcretized() && end.isConcretized() && start.getValue() >= end.getValue()) {
            any_problem = 1;
        }
        if (attack.isConcretized() && decay.isConcretized() && attack.getValue() >= decay.getValue()) {
            any_problem = 1;
        }
        if (attack.isConcretized() && sustain.isConcretized() && attack.getValue() >= sustain.getValue()) {
            any_problem = 1;
        }
        if (attack.isConcretized() && end.isConcretized() && attack.getValue() >= end.getValue()) {
            any_problem = 1;
        }
        if (decay.isConcretized() && sustain.isConcretized() && decay.getValue() >= sustain.getValue()) {
            any_problem = 1;
        }
        if (decay.isConcretized() && end.isConcretized() && decay.getValue() >= end.getValue()) {
            any_problem = 1;
        }
        if (sustain.isConcretized() && end.isConcretized() && sustain.getValue() >= end.getValue()) {
            any_problem = 1;
        }
        if (any_problem == 1) {
            //warning
            Logs.warning("I cannot schedule the expression: id " + id + " wrong time markers");

            is_scheduled = false;
            //give up, destroy itself
            return; //enough? check
        }


        if (!(start.isConcretized())) {
            //1. there is no timemarkers at all

            //not concetized == not exists??
            if ((!(start.isConcretized())) && (!(end.isConcretized())) && (!(decay.isConcretized())) && (!(sustain.isConcretized())) && (!(attack.isConcretized()))) {
                //warning
                Logs.warning("I cannot schedule the expression: id " + id);

                //give up, destroy itself
                is_scheduled = false;

                return; //enough? check

            } else {

                if ((start.isConcretized() ? 1 : 0) + (end.isConcretized() ? 1 : 0) + (decay.isConcretized() ? 1 : 0) + (sustain.isConcretized() ? 1 : 0) + (attack.isConcretized() ? 1 : 0) >= 2) {


                    if (attack.isConcretized() && decay.isConcretized()) {
                        double duration = decay.getValue() - attack.getValue();
                        start.setValue(attack.getValue() - duration * (2.0d / 1.0d));
                    }

                    if (attack.isConcretized() && (!decay.isConcretized()) && sustain.isConcretized()) {
                        double duration = sustain.getValue() - attack.getValue();
                        start.setValue(attack.getValue() - duration * (2.0d / 6.0d));
                    }

                    if (attack.isConcretized() && (!decay.isConcretized()) && (!sustain.isConcretized()) && end.isConcretized()) {
                        double duration = end.getValue() - attack.getValue();
                        start.setValue(attack.getValue() - duration * (2.0d / 8.0d));
                    }

                    if ((!attack.isConcretized()) && (decay.isConcretized()) && sustain.isConcretized()) {
                        double duration = sustain.getValue() - decay.getValue();
                        start.setValue(decay.getValue() - duration * (3.0d / 5.0d));
                    }

                    if ((!attack.isConcretized()) && (!decay.isConcretized()) && sustain.isConcretized() && end.isConcretized()) {
                        double duration = end.getValue() - sustain.getValue();
                        start.setValue(sustain.getValue() - duration * (2.0d / 2.0d));
                    }
                    if ((!attack.isConcretized()) && (decay.isConcretized()) && (!sustain.isConcretized()) && end.isConcretized()) {
                        double duration = end.getValue() - decay.getValue();
                        start.setValue(decay.getValue() - duration * (3.0d / 7.0d));
                    }

                    //todo add the condition  - at least two are defined
                    //if any two are defined we can use the correct procedure
                    //add the procedures 2 out of 4 - all the combinations
                } else {

                    //else - only one is defined
                    if ((start.isConcretized() ? 1 : 0) + (end.isConcretized() ? 1 : 0) + (decay.isConcretized() ? 1 : 0) + (sustain.isConcretized() ? 1 : 0) + (attack.isConcretized() ? 1 : 0) == 1) {

                        //if only one is defined then any stupid approximation
                        if (attack.isConcretized()) {
                            start.setValue(attack.getValue() / 2.0);
                        }

                        if ((!attack.isConcretized()) && decay.isConcretized()) {
                            start.setValue(decay.getValue() / 2.0);
                            attack.setValue(decay.getValue() - decay.getValue() / 3.0);
                        }

                        if ((!attack.isConcretized()) && (!decay.isConcretized()) && sustain.isConcretized()) {
                            start.setValue(sustain.getValue() / 2.0);
                            attack.setValue(sustain.getValue() - sustain.getValue() / 3.0);
                            decay.setValue(sustain.getValue() - sustain.getValue() / 4.0);
                        }

                        if ((!attack.isConcretized()) && (!sustain.isConcretized()) && (!decay.isConcretized()) && end.isConcretized()) {
                            start.setValue(end.getValue() / 2.0);
                            attack.setValue(end.getValue() - end.getValue() / 3.0);
                            decay.setValue(end.getValue() - end.getValue() / 4.0);
                            sustain.setValue(end.getValue() - end.getValue() / 5.0);
                        }
                    }//end of if
                }//end of else
            }//end of else
        }//end of start


        if (!(end.isConcretized())) {

            //1. there is no timemarkers at all
            //not concetized == not exists??
            if ((!(start.isConcretized())) && (!(end.isConcretized())) && (!(decay.isConcretized())) && (!(sustain.isConcretized())) && (!(attack.isConcretized()))) {
                //warning
                Logs.warning("I cannot schedule the expression: id " + id);
                //give up, destroy itself
            } else {

                if ((start.isConcretized() ? 1 : 0) + (end.isConcretized() ? 1 : 0) + (decay.isConcretized() ? 1 : 0) + (sustain.isConcretized() ? 1 : 0) + (attack.isConcretized() ? 1 : 0) >= 2) {

                    if (start.isConcretized() && attack.isConcretized()) {
                        double duration = attack.getValue() - start.getValue();
                        end.setValue(attack.getValue() + duration * (8.0d / 2.0d));
                    }

                    if (start.isConcretized() && (!attack.isConcretized()) && decay.isConcretized()) {
                        double duration = decay.getValue() - start.getValue();
                        end.setValue(decay.getValue() + duration * (7.0d / 2.0d));
                    }

                    if (start.isConcretized() && (!decay.isConcretized()) && (!attack.isConcretized()) && sustain.isConcretized()) {
                        double duration = sustain.getValue() - start.getValue();
                        start.setValue(start.getValue() + duration * (10.0d / 8.0d));
                    }

                    if ((!start.isConcretized()) && (decay.isConcretized()) && attack.isConcretized()) {
                        double duration = decay.getValue() - attack.getValue();
                        start.setValue(decay.getValue() + duration * (7.0d / 1.0d));
                    }

                    if ((!start.isConcretized()) && (!attack.isConcretized()) && sustain.isConcretized() && decay.isConcretized()) {
                        double duration = sustain.getValue() - decay.getValue();
                        start.setValue(sustain.getValue() + duration * (2.0d / 5.0d));
                    }
                    if ((!start.isConcretized()) && (attack.isConcretized()) && (!decay.isConcretized()) && sustain.isConcretized()) {
                        double duration = sustain.getValue() - attack.getValue();
                        start.setValue(attack.getValue() + duration * (8.0d / 6.0d));
                    }

                } else {

                    //else - only one is defined
                    if ((start.isConcretized() ? 1 : 0) + (end.isConcretized() ? 1 : 0) + (decay.isConcretized() ? 1 : 0) + (sustain.isConcretized() ? 1 : 0) + (attack.isConcretized() ? 1 : 0) == 1) {

                        //if only one is defined then any stupid approximation

                        if (sustain.isConcretized()) {
                            end.setValue(sustain.getValue() + sustain.getValue() / 2.0);
                        }

                        if ((!sustain.isConcretized()) && decay.isConcretized()) {
                            end.setValue(decay.getValue() + decay.getValue() / 2.0);
                            sustain.setValue(decay.getValue() + decay.getValue() / 3.0);
                        }

                        if ((!decay.isConcretized()) && (!sustain.isConcretized()) && attack.isConcretized()) {
                            end.setValue(attack.getValue() + end.getValue() / 2.0);
                            sustain.setValue(attack.getValue() + end.getValue() / 3.0);
                            decay.setValue(attack.getValue() + end.getValue() / 4.0);
                        }


                        if ((!decay.isConcretized()) && (!sustain.isConcretized()) && (!attack.isConcretized()) && start.isConcretized()) {
                            end.setValue(start.getValue() + start.getValue() / 2.0);
                            sustain.setValue(start.getValue() + start.getValue() / 3.0);
                            decay.setValue(start.getValue() + start.getValue() / 4.0);
                            attack.setValue(start.getValue() + start.getValue() / 5.0);
                        }

                    }//end of if
                }//end of else
            }//end of else
        }//end of end


        //for the additional markers between attack, decay, sustain - we assume that at least to markers are known (see start and end above)

        if (!(attack.isConcretized())) {

            //not concetized == not exists??
            if ((!(start.isConcretized())) && (!(end.isConcretized())) && (!(decay.isConcretized())) && (!(sustain.isConcretized())) && (!(attack.isConcretized()))) {
                //warning
                Logs.warning("I cannot schedule the expression: id " + id);
                //give up, destroy itself
            } else {
                //any stupid approximation
                if (start.isConcretized() && decay.isConcretized()) {
                    double duration = decay.getValue() - start.getValue();
                    attack.setValue(start.getValue() + duration * (2.0d / 3.0d));
                }
                //important condition :do we permit the decay to be otpional or not

                if (start.isConcretized() && (!decay.isConcretized()) && sustain.isConcretized()) {
                    double duration = sustain.getValue() - start.getValue();
                    attack.setValue(start.getValue() + duration * 0.25d);
                    //decay.setValue(start.getValue() + duration * (3.0d / 8.0d));
                }

                // case that is most often used
                //it is the same procedure
                if ((start.isConcretized()) && (!sustain.isConcretized()) && (!decay.isConcretized()) && end.isConcretized()) {
                    double duration = end.getValue() - start.getValue();
                    attack.setValue(start.getValue() + duration * 0.2d);
                    // decay.setValue(start.getValue() + duration * 0.3d);
                    // sustain.setValue(start.getValue() + duration * 0.8d);
                }
            }//end of else
        }//end of attack

        if (!(decay.isConcretized())) {

            //not concetized == not exists??
            if ((!(start.isConcretized())) && (!(end.isConcretized())) && (!(decay.isConcretized())) && (!(sustain.isConcretized())) && (!(attack.isConcretized()))) {
                //warning
                Logs.warning("I cannot schedule the expression: id " + id);
                //give up, destroy itself
            } else {
                //any stupid approximation
                if (attack.isConcretized() && sustain.isConcretized()) {
                    double duration = sustain.getValue() - attack.getValue();
                    decay.setValue(attack.getValue() + duration * (1.0d / 6.0d));
                }
                //important condition do we permit the decay to be otpional or not

                if (start.isConcretized() && (!attack.isConcretized()) && sustain.isConcretized()) {
                    double duration = sustain.getValue() - start.getValue();

                    decay.setValue(start.getValue() + duration * (3.0d / 8.0d));
                    // attack.setValue(start.getValue() + duration * (2.0d / 8.0d));
                }

                if (attack.isConcretized() && (!sustain.isConcretized()) && end.isConcretized()) {
                    double duration = end.getValue() - attack.getValue();

                    decay.setValue(attack.getValue() + duration * (1.0d / 8.0d));
                    // sustain.setValue(attack.getValue() + duration * (6.0d / 8.0d));
                }

                if ((start.isConcretized()) && (!sustain.isConcretized()) && (!attack.isConcretized()) && end.isConcretized()) {
                    double duration = end.getValue() - start.getValue();

                    //attack.setValue(start.getValue() + duration * 0.2d);
                    decay.setValue(start.getValue() + duration * 0.3d);
                    //sustain.setValue(start.getValue() + duration * 0.8d);
                }

            }//end of else

        }//end of decay

        if (!(sustain.isConcretized())) {
            //not concetized == not exists??

            if ((!(start.isConcretized())) && (!(end.isConcretized())) && (!(decay.isConcretized())) && (!(sustain.isConcretized())) && (!(attack.isConcretized()))) {
                //warning
                Logs.warning("I cannot schedule the expression: id " + id);
                //give up, destroy itself
            } else {
                //any stupid approximation
                if (decay.isConcretized() && end.isConcretized()) {
                    double duration = end.getValue() - decay.getValue();

                    sustain.setValue(decay.getValue() + duration * (5.0d / 7.0d));
                }
                //important condition do we permit the decay to be otpional or not

                if (attack.isConcretized() && (!decay.isConcretized()) && end.isConcretized()) {
                    double duration = end.getValue() - attack.getValue();

                    // decay.setValue(start.getValue() + duration * (1.0d / 8.0d));
                    sustain.setValue(start.getValue() + duration * (6.0d / 8.0d));
                }

                if ((start.isConcretized()) && (!decay.isConcretized()) && (!attack.isConcretized()) && end.isConcretized()) {
                    double duration = end.getValue() - start.getValue();

                    // attack.setValue(start.getValue() + duration * 0.2d);
                    // decay.setValue(start.getValue() + duration * 0.3d);
                    sustain.setValue(start.getValue() + duration * 0.8d);
                }

            }//end of else
        }//end of sustain

        //Logs.info("signal :" + this.getId() +  " start: " + start.getValue() +  " attack: " +  attack.getValue() +  " decay: "  + decay.getValue() +  " sustain: " + sustain.getValue() +  " end: " + end.getValue() );
        //*/
        //</editor-fold>

        is_scheduled = true;

    }

    public void setTimeMarker(String value, String name) {

        if (name.equalsIgnoreCase("start")) {
            start.addReference(value);
        }

        if ((name.equalsIgnoreCase("attack")) || (name.equalsIgnoreCase("ready"))) {
            attack.addReference(value);
        }

        if (name.equalsIgnoreCase("decay")) {
            decay.addReference(value);
        }

        if ((name.equalsIgnoreCase("sustain")) || (name.equalsIgnoreCase("relax"))) {
            sustain.addReference(value);
        }

        if (name.equalsIgnoreCase("end")) {
            end.addReference(value);
        }

    }//end of method


    public void setMode(int mode) {
        this.normalized = mode;
    }

   public void setSide(Side side) {
        this.side = side;
    }

    public void setTorso(int torso) {
        this.torso = torso;
    }

    public void setRepetition(double number) {
        this.repetition = number;
    }

    public Side getSide() {
        return side;
    }

    public double getRepetition() {
        return repetition;
    }

    @Override
    public TimeMarker getStart() {
        return start;
    }

    @Override
    public TimeMarker getEnd() {
        return end;
    }

    private double warpTMP() {
        return Functions.changeInterval(Math.max(0, Math.min(getTMP(), 1)), 0, 1, 3, 1);
    }

    private double warpIntensity() {
        return Math.max(0, Math.min(getIntensity(), 1));
    }

    public double getFront() {
        return front;
    }

    public void setFront(double front) {
        this.front = front;
    }

    public double getUp() {
        return up;
    }

    public void setUp(double up) {
        this.up = up;
    }
}
