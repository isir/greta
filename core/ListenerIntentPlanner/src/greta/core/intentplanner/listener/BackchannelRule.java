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
package greta.core.intentplanner.listener;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represent a trigger rule in the Listener Intent Planner
 *
 * @author Elisabetta Bevacqua
 */
public class BackchannelRule {

    // Name of the rule
    private String name;

    // Probability to trigger the rule
    private double probability;

    // Probability to generate a "response" backchannel
    private double responseProbability;

    // Probability to generate mimicry
    private double mimicryProbability;

    // List of the user's visual and acoustic signals in input
    private ArrayList<ProtoSignal> inputSignals = new ArrayList<ProtoSignal>();

    // List of the agent signals to mimic
    private ArrayList<ProtoSignal> mimicrySignals = new ArrayList<ProtoSignal>();

    //public methods :
    /**
     * Construct a rule with the basic information.
     */
    public BackchannelRule(String ruleName) {
        this.name = ruleName;
        this.probability = 0;
        this.responseProbability = 0;
        this.mimicryProbability = 0;
    }

    public String getName(){
        return name;
    }

    public void setProbability(double ruleProbability) {
        this.probability = ruleProbability;
    }

    public double getProbability() {
        return this.probability;
    }

    public void setResponseProbability(double responseProbability) {
        this.responseProbability = responseProbability;
    }

    public double getResponseProbability() {
        return this.responseProbability;
    }

    public void setMimicryProbability(double mimicryProbability) {
        this.mimicryProbability = mimicryProbability;
    }

    public double getMimicryProbability() {
        return this.mimicryProbability;
    }

    public List<ProtoSignal> getInputSignals(){
        return inputSignals;
    }

    public List<ProtoSignal> getMimicrySignals(){
        return mimicrySignals;
    }

    /**
     * This function add the user's acoustic and visual signals that the rule needs to be triggered
     * @param modality
     * @param name of the signal
     */
    public void addInputSignal(String modality, String name) {
        if(modality != null && name != null) {
            inputSignals.add(new ProtoSignal(modality, name));
        }
    }

    /**
     * This function add the visual signals of mimicry that this rule can generate when triggered
     * @param modality
     * @param name of the signal
     */
    public void addMimicrySignal(String modality, String name) {
        if(modality != null && name != null) {
            mimicrySignals.add(new ProtoSignal(modality, name));
        }
    }

}
