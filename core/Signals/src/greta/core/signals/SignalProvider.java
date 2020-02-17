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

import greta.core.signals.gesture.GestureSignal;
import greta.core.signals.gesture.PointingSignal;
import greta.core.util.time.TimeMarker;

/**
 * This class is a way to create or compare {@code Signals} without knowing the implementations of {@code Signals}.<br/>
 * It contains only static functions.
 * @author Andre-Marie Pez
 * @navassoc - instanciate - greta.core.signals.Signal
 */
public class SignalProvider {

    /**
     * Don't let anyone instantiate this class.
     */
    private SignalProvider(){}

    /**
     * Creates a {@code Signal} according to a specific modality.
     * @param modality the modality of the {@code Signal}
     * @param id the id of the {@code Signal} to create
     * @return the created {@code Signal} or {@code null} if it can not be created.
     */
    public static Signal create(String modality, String id){
        String[] modalities = getModalityAndSubmodality(modality);
        if(modality.equalsIgnoreCase("face")){
            FaceSignal fs = new FaceSignal(id);
            fs.setSubmodality(modalities[1]);
            return fs;
        }
        if(modality.equalsIgnoreCase("head")){
            HeadSignal hs = new HeadSignal(id);
            hs.setSubmodality(modalities[1]);
            return hs;
        }
        if(modality.equalsIgnoreCase("gaze")){
            GazeSignal gs = new GazeSignal(id);
            gs.setSubmodality(modalities[1]);
            return gs;
        }
        if(modalities[0].equalsIgnoreCase("gesture")){
            GestureSignal gs = new GestureSignal(id);
            gs.setSubmodality(modalities[1]);
            return gs;
        }
        if(modalities[0].equalsIgnoreCase("torso")){
            TorsoSignal ts = new TorsoSignal(id);
            ts.setSubmodality(modalities[1]);
            return ts;
        }
        if(modalities[0].equalsIgnoreCase("pointing")){
            PointingSignal ps = new PointingSignal(id);
            ps.setSubmodality(modalities[1]);
            return ps;
        }
        if(modalities[0].equalsIgnoreCase("shoulder")){
            ShoulderSignal ss = new ShoulderSignal(id);
            ss.setSubmodality(modalities[1]);
            return ss;
        }
        return null;
    }

    /**
     * Compares to {@code Signals} and check if they use the same modality or the same submodality.
     * @param s1 first {@code Signal} to compare
     * @param s2 second {@code Signal} to compare
     * @return {@code true} if the {@code Signals} use the same modality and submodality, {@code false} otherwise
     */
    public static boolean hasSameModality(Signal s1, Signal s2){
        if(s1==null || s2==null)
            return false;
        String[] modalities1 = getModalityAndSubmodality(s1);
        String[] modalities2 = getModalityAndSubmodality(s2);

        if(modalities1[0].equalsIgnoreCase("face")) return false;
        return modalities1[0].equalsIgnoreCase(modalities2[0]) //same modality
            && (modalities1[1].isEmpty() || modalities2[1].isEmpty() //modality fully used
               || modalities1[1].equalsIgnoreCase(modalities2[1])); //same submodality
    }

    private static String[] getModalityAndSubmodality(Signal s){
        String[] toReturn;
        if(s instanceof ParametricSignal){
            toReturn = new String[2];
            toReturn[0] = s.getModality();
            toReturn[1] = ((ParametricSignal)s).getSubmodality();
        }
        else{
            toReturn = getModalityAndSubmodality(s.getModality());
        }
        return toReturn;
    }

    private static String[] getModalityAndSubmodality(String modality){
        String[] splited = modality.split("\\.");
        if(splited.length==2)
            return  splited;

        String[] toReturn = new String[2];
        toReturn[0] = modality;
        toReturn[1] = "";
        return toReturn;
    }

    public static TimeMarker getBegining(Signal signal){
        String timeMarkerName = "start";
        //String category = signa this.getReference().substring(0, this.getReference().indexOf("=")).trim();
        if(signal instanceof GestureSignal ){
            GestureSignal gesture = (GestureSignal)signal;
            int equalPos = gesture.getReference().indexOf("=");
            if(equalPos>=0){
                String category = gesture.getReference().substring(0, equalPos).trim();
                if(!category.equalsIgnoreCase("ADJECTIVALS")){
                    timeMarkerName = "ready";
                }
            }
        }
        return signal.getTimeMarker(timeMarkerName);
    }

    public static void setBegining(Signal signal, double value){
        getBegining(signal).setValue(value);
    }


    public static TimeMarker getEnding(Signal signal){
        String timeMarkerName = "end";
        if(signal instanceof GestureSignal){
            GestureSignal gesture = (GestureSignal)signal;
            int equalPos = gesture.getReference().indexOf("=");
            if(equalPos>=0){
                String category = gesture.getReference().substring(0, equalPos).trim();
                if(!category.equalsIgnoreCase("ADJECTIVALS")){
                    timeMarkerName = "relax";
                }
            }
        }
        return signal.getTimeMarker(timeMarkerName);
    }

    public static void setEnding(Signal signal, double value){
        getEnding(signal).setValue(value);
    }
}
