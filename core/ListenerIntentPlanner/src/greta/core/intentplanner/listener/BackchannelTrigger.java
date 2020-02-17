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

import greta.core.intentions.BasicIntention;
import greta.core.intentions.EmotionIntention;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.intentplanner.AgentState;
import greta.core.signals.ParametricSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.signals.SignalProvider;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.parameter.EngineParameter;
import greta.core.util.time.TimeMarker;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains all the rules to trigger a backchannel and check the
 * user's visual and acoustic signals in input to find the rules to trigger
 *
 * @author Elisabetta Bevacqua
 */
public class BackchannelTrigger extends CharacterDependentAdapter implements IntentionEmitter, SignalEmitter, CharacterDependent {

    // This perfomers are used to send the backchannels
    private ArrayList<IntentionPerformer> intentionPerformers = new ArrayList<IntentionPerformer>();
    private ArrayList<SignalPerformer> signalPerformers = new ArrayList<SignalPerformer>();

    //this index is used to create unique id for each communicative intention
    private static int index = 0;

    // This list contains all the backchannel trigger rules
    private ArrayList<BackchannelRule> backchannelRulesList = new ArrayList<BackchannelRule>();
    private String currentRulesFile;
    /** This variable represents the type of agent's intention when it expresses an appreciation.*/
    private  String emotionIntentionOfAgentAppreciation;

    //public methods :
    /**
     * Construct, it upload all the trigger rules
     */
    public BackchannelTrigger(){
        loadBackchannelTriggerRules();
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        if (ip != null) {
            intentionPerformers.add(ip);
        }
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer ip) {
        if (ip != null && intentionPerformers.contains(ip)) {
            intentionPerformers.remove(ip);
        }
    }

    @Override
    public void addSignalPerformer(SignalPerformer sp) {
        if (sp != null && sp != this) {
            signalPerformers.add(sp);
        }
    }

    @Override
    public void removeSignalPerformer(SignalPerformer sp) {
        if (sp != null && signalPerformers.contains(sp)) {
            signalPerformers.remove(sp);
        }
    }

    /**
     * This function loads backchannel trigger rules
     */
    private void loadBackchannelTriggerRules() {
        String rulesFile = getCharacterManager().getValueString("BACKCHANNELTRIGGERRULES");
        if (currentRulesFile == null || !currentRulesFile.equalsIgnoreCase(rulesFile)) {
            currentRulesFile = rulesFile;

            backchannelRulesList = new ArrayList<BackchannelRule>();
            XMLTree tree = XML.createParser().parseFile(currentRulesFile);
            if (tree != null) {
                for (XMLTree row : tree.getChildrenElement()) {
                    if (row.isNamed("rule")) {
                        BackchannelRule newRule = new BackchannelRule(row.getAttribute("name"));
                        for (XMLTree child : row.getChildrenElement()) {
                            if (child.isNamed("usersignals")) {
                                for (XMLTree us : child.getChildrenElement()) {
                                    newRule.addInputSignal(us.getAttribute("modality"), us.getAttribute("name"));
                                }
                            } else if (child.isNamed("backchannels")) {
                                newRule.setProbability(child.getAttributeNumber("probability"));
                                for (XMLTree bc : child.getChildrenElement()) {
                                    if (bc.isNamed("mimicry")) {
                                        newRule.setMimicryProbability(bc.getAttributeNumber("probability"));
                                        for (XMLTree as : bc.getChildrenElement()) {
                                            newRule.addMimicrySignal(as.getAttribute("modality"), as.getAttribute("name"));
                                        }
                                    } else if (bc.isNamed("response_reactive")) {
                                        newRule.setResponseProbability(bc.getAttributeNumber("probability"));
                                    }
                                }
                                System.out.println(newRule.getName()+" "+newRule.getProbability()+" "+newRule.getResponseProbability());
                            }
                        }
                        backchannelRulesList.add(newRule);
                        //  Logs.debug(signalId);
                    }
                }
            }

            // Print all rules for debug
            for (BackchannelRule r : backchannelRulesList) {
                Logs.debug("name:" + r.getName() + " prob:" + r.getProbability());
                for (ProtoSignal s : r.getInputSignals()) {
                    Logs.debug(s.getModality() + " " + s.getReference());
                }
                for (ProtoSignal s : r.getMimicrySignals()) {
                    Logs.debug(s.getModality() + " " + s.getReference());
                }
            }
        }
    }

    /**
     * This function finds the bc rules that can be triggered from the input
     * list of user's behaviours More rules can be triggered at the same time
     * (for example a rule is triggered by a head nod and another by a smile) a
     * response backchannel is generated if at least one of the triggered rules
     * can generate a rensponse backchannel a mimicry backchannel is generated
     * and it contains all the visual signals that have triggered a bc according
     * to thier probability for example a head nod and a smile are in the list
     * of input signals, they trigger thier corresponding rules a bml is
     * generated and it can contain the head nod and the smile according to
     * their probability
     *
     * @param inputSignalsList the list of user's signals
     */
    public void findAndTrigger(List<? extends ProtoSignal> inputSignalsList) {
        // List of the signals to mimic according to the rules triggered
        ArrayList<Signal> mimicrySignals = new ArrayList<Signal>();
        boolean doResponse = false;
        int mimicryCount = 0;

       // System.out.println("inputsignal0 "+inputSignalsList.get(0).getModality()+" "
       //         +inputSignalsList.get(0).getReference());

        BackchannelRule selectedRule = null;

        for (BackchannelRule rule : backchannelRulesList) {
            boolean allFound = true;
            for (ProtoSignal us : rule.getInputSignals()) {
                boolean found = false;
                for (ProtoSignal is : inputSignalsList) {
                    if (us.equals(is)) {
                        found = true;
                        break; //do not search for redondant signals
                    }
                }
                if(!found){
                    allFound = false;
                    break; //it's useless to continue
                }
            }
            if (allFound && Math.random() < rule.getProbability()) {
                // a rule is triggered when all its user's acoustic and visual signals are found in input
                Logs.debug("rule found and enclenched: "+ rule.getName());
                selectedRule = rule;
                //try to trigger mimicry
                if (!rule.getMimicrySignals().isEmpty() && Math.random() < rule.getMimicryProbability()) {
                    //if mimicry backchannels are triggered a bml that contains all the visual signals is generated and sent
                    for (ProtoSignal as : rule.getMimicrySignals()) {
                        Signal s = SignalProvider.create(as.getModality(), "mimicry" + (mimicryCount++));
                        if (s instanceof ParametricSignal) {
                            ParametricSignal sp = (ParametricSignal) s;
                            sp.setReference(as.getReference());
                            sp.setTMP(0.5);// TODO: baseline?
                            sp.setSPC(0.5);// TODO: baseline?
                        }
                        s.getStart().setValue(0);
                        s.getEnd().setValue(1.8);
                        mimicrySignals.add(s);
                    }
                }

                if (!doResponse && rule.getResponseProbability() > 0) {
                    //try to trigger a rensponse backchannel
                    doResponse = Math.random() < rule.getResponseProbability();
                }
            }
        }

        //if a rensponse backchannel is triggered a fml that contains all the agent's communicative intentions is generated and sent
        if (doResponse) {
            Logs.debug("LIP: do response");

            List<Intention> intentList;

            if (!isAppreciationRule(selectedRule)) {
                intentList = getIntentions();
            } else {
                intentList = getAppreciationIntentions();
            }

            if (!intentList.isEmpty()) {
                Logs.debug("LIP: Send response");
                sendResponse(intentList);
            }
        }

        if (!mimicrySignals.isEmpty()) {
            for (Signal as : mimicrySignals) {
                if (as instanceof ParametricSignal) {
                    ParametricSignal sp = (ParametricSignal) as;
                    Logs.debug("to mimic: " + sp.getModality() + " " + sp.getReference() + " " + sp.getStart().getValue() + " " + sp.getEnd().getValue());
                }
            }
            sendMimicry(mimicrySignals);
        }
    }

    public List<Intention> getIntentions(){
        List<EngineParameter> intentionValues = AgentState.getGlobalState().find(ListenerIntentPlanner.BACKCHANNEL_SET_NAME).getAll();
        ArrayList<Intention> intentionsList = new ArrayList<Intention>(intentionValues.size());
        for(EngineParameter ep : intentionValues) {
            if (ep.getValue() > 0) {
                BasicIntention intent = new BasicIntention("backchannel", "bc" + (index++), ep.getParamName(), new TimeMarker("start", 0), new TimeMarker("end", 2));
                intent.setImportance(ep.getValue());
                intentionsList.add(intent);
            }
        }
        return intentionsList;
    }

    /**
     * @author: Sabrina Campano.
     * This method get agent's intentions expressing an appreciation.
     * It is called only for appreciation rules, which were not originally present in Elisabetta's Listener Intent Planner.
     * @return a list of intentions.
     */
    public List<Intention> getAppreciationIntentions(){

         ArrayList<Intention> intentionsList = new ArrayList<Intention>();

         if (emotionIntentionOfAgentAppreciation == null)
            return intentionsList;

        EmotionIntention intent = new EmotionIntention("e" + (index++), emotionIntentionOfAgentAppreciation, new TimeMarker("start", 0), new TimeMarker("end", 2), 1, 1);
        intent.setImportance(1);
        intentionsList.add(intent);

        return intentionsList;
    }

    private void sendResponse(List<Intention> response) {
        for (IntentionPerformer performer : intentionPerformers) {
            performer.performIntentions(response, IDProvider.createID("ListenerIntentPlanner_Response"), new Mode(CompositionType.blend));
        }
    }

    private void sendMimicry(List<Signal> mimicry) {
        for (SignalPerformer performer : signalPerformers) {
            performer.performSignals(mimicry, IDProvider.createID("ListenerIntentPlanner_Mimicry"), new Mode(CompositionType.blend));
        }
    }

    @Override
    public void onCharacterChanged() {
        this.loadBackchannelTriggerRules();
    }

    private boolean isAppreciationRule(BackchannelRule rule){
        return rule.getName().equalsIgnoreCase("trigger-appreciation-positive") || rule.getName().equalsIgnoreCase("trigger-appreciation-negative");
    }

    public void setEmotionIntentionOfAgentAppreciation(String emotionIntentionOfAgentAppreciation){
        this.emotionIntentionOfAgentAppreciation = emotionIntentionOfAgentAppreciation;
    }
}
