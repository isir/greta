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

import greta.core.behaviorplanner.baseline.BaseLine;
import greta.core.behaviorplanner.baseline.BehaviorQualityComputer;
import greta.core.behaviorplanner.baseline.DynamicLine;
import greta.core.behaviorplanner.lexicon.BehaviorSet;
import greta.core.behaviorplanner.lexicon.Lexicon;
import greta.core.behaviorplanner.lexicon.SignalItem;
import greta.core.behaviorplanner.strokefillers.EmptyStrokeFiller;
import greta.core.behaviorplanner.strokefillers.StrokeFiller;
import greta.core.ideationalunits.IdeationalUnit;
import greta.core.ideationalunits.IdeationalUnitFactory;
import greta.core.intentions.IdeationalUnitIntention;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionPerformer;
import greta.core.intentions.IntentionTargetable;
import greta.core.signals.GazeSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.signals.SignalTargetable;
import greta.core.signals.gesture.GestureSignal;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.log.Logs;
import greta.core.util.time.Temporizer;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the Greta's behavior planner.<br/><br/> The
 * {@link #performIntentions(java.util.List, greta.core.util.id.ID, greta.core.util.Mode) performIntentions}
 * function (defined in {@link greta.core.intentions.IntentionPerformer}
 * interface) is its main function.<br/> it plans behaviors (as {@code Signals})
 * corresponding to the list of {@code Intention}, then sends all the resulted
 * {@code Signals} to all {@code SignalPerformer} added with
 * {@link #addSignalPerformer(greta.core.signals.SignalPerformer) addSignalPerformer(SignalPerformer)}
 * function (defined in {@link greta.core.signals.SignalEmitter} interface). The
 * attribute {@code requestId} is send without modification to the
 * {@code SignalPerformers} added.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because they are
 * UmlGraph tags, not javadoc tags.
 * @composed - - + greta.core.behaviorplanner.SignalSelector
 * @composed - - 1 greta.core.behaviorplanner.baseline.BaseLine
 * @composed - - 1 greta.core.behaviorplanner.lexicon.Lexicon
 * @composed - - 1 greta.core.behaviorplanner.baseline.BehaviorQualityComputer
 * @navassoc - - * greta.core.signals.Signal
 * @inavassoc - - * greta.core.intentions.Intention
 */
public class Planner extends CharacterDependentAdapter implements IntentionPerformer, SignalEmitter {

    //contains Behaviors relating to intentions
    private Lexicon lexicon;
    //contains base values of epressivity parameters
    private BaseLine baseline;
    //computes dynamicline from baseline
    private BehaviorQualityComputer bqc;
    //SignalSelectors
    private MSESelector mseSelector;
    private MultimodalSignalSelector defaultSelector;
    private List<SignalSelector> otherSelector;
    //list of selected signals
    private List<Signal> selectedSignals;
    //where send the resulted signals
    private List<SignalPerformer> signalPerformers;
    private StrokeFiller strokeFiller;

    public Planner(CharacterManager cm) {
        setCharacterManager(cm);
        lexicon = new Lexicon(cm);
        baseline = new BaseLine(cm);
        bqc = new BehaviorQualityComputer(cm);

        mseSelector = new MSESelector();
        defaultSelector = new MultimodalSignalSelector();
        otherSelector = new ArrayList<SignalSelector>();
        selectedSignals = new ArrayList<Signal>();
        signalPerformers = new ArrayList<SignalPerformer>();
        strokeFiller = new EmptyStrokeFiller();//the more simple case : add no strokes
    }

    /**
     * This function chooses the most apropriate {@code SignalSelector} to treat
     * a specific {@code Intention}.<br/> It use the function
     * {@code acceptIntention} defined in {@code SignalSelector} interface.
     *
     * @param intention the Intention to treat
     * @param context list of Intentions durring the Intention to treat
     * @return the choosen SignalSelector
     * @see
     * greta.core.behaviorplanner.SignalSelector#acceptIntention(greta.core.intentions.Intention,
     * java.util.List) {@code acceptIntention(Intention,List<Intention>)}
     */
    private SignalSelector chooseSelectorFor(Intention intention, List<Intention> context) {

        //needs to be addapted in terms of the futur works

        //first try MSE
        if (mseSelector.acceptIntention(intention, context)) {
            return mseSelector;
        }
        //try in added selector
        for (SignalSelector selector : otherSelector) {
            if (selector.acceptIntention(intention, context)) {
                return selector;
            }
        }
        //finaly use the default selector
        return defaultSelector;
    }

    /**
     * Sorts a list of intentions by importance, starting time, duration.
     *
     * @param intentions thie list to sort
     */
    protected void sortIntentions(List<Intention> intentions) {
        for (int i = 1; i < intentions.size(); ++i) {
            Intention toMove = intentions.get(i);
            for (int j = 0; j < i; ++j) {
                Intention toCompare = intentions.get(j);
                boolean before = toMove.getImportance() > toCompare.getImportance();
                if (!before && toMove.getImportance() == toCompare.getImportance()) {
                    double start = toMove.getStart().getValue();
                    double end = toMove.getEnd().getValue();
                    double c_start = toCompare.getStart().getValue();
                    double c_end = toCompare.getEnd().getValue();
                    before = end < c_start;
                    if (!before && start < c_end) {
                        before = end - start < c_end - c_start;
                        //in the case of an infinite end,
                        //it sets also priority to the last sarting (else, it may be skiped)
                    }
                }

                if (before) {
                    intentions.add(j, intentions.remove(i));
                    break;
                }
            }
        }
    }

    /**
     * This fuction receives a list of {@code Intention}.<br/> It plans
     * behaviors (as {@code Signals}) corresponding to the list of
     * {@code Intention}, then sends all the resulted {@code Signals} to all
     * {@code SignalPerformer} added with {@code add(SignalPerformer)} function.
     * The attribute {@code requestId} is send without modification to the
     * {@code SignalPerformers} added.
     *
     * @param intentions the list of {@code Intention}
     * @param requestId the identifier of the request
     */
    @Override
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode) {
        selectedSignals = new ArrayList<Signal>();

        //temporize every intentions
        Temporizer temporizer = new Temporizer();
        temporizer.add(intentions);
        temporizer.temporize();

        //sort intentions by importance, starting time, duration.
        sortIntentions(intentions);

        //for each intention :
        for (Intention intention : intentions) {

            //character playing
            if (intention.hasCharacter()) {
                baseline.set(getCharacterManager().getValueString(BaseLine.CHARACTER_PARAMETER_BASELINE, intention.getCharacter()));
                lexicon.setDefinition(getCharacterManager().getValueString(Lexicon.CHARACTER_PARAMETER_INTENTION_LEXICON, intention.getCharacter()));
                bqc.setDefinition(getCharacterManager().getValueString(BehaviorQualityComputer.CHARACTER_PARAMETER_QUALIFIERS, intention.getCharacter()));
            }

            //compute the dynimicline
            DynamicLine dynamicline = bqc.computeDynamicline(intention, baseline);

            //search the context (other intentions durring the current intention) :
            List<Intention> context = new ArrayList<Intention>();
            double start = intention.getStart().getValue();
            double end = intention.getEnd().getValue();
            for (Intention contextIntention : intentions) {
                if (contextIntention != intention) { //must be an other intentions
                    double otherStart = contextIntention.getStart().getValue();
                    double otherEnd = contextIntention.getEnd().getValue();
                    if (otherStart < end && start < otherEnd) {
                        //add in context
                        context.add(contextIntention);
                        //and update dynamicline
                        if (contextIntention.getName().equalsIgnoreCase("emotion")
                                || contextIntention.getName().equalsIgnoreCase("emphasis")
                                || contextIntention.getName().equalsIgnoreCase("rheme")
                                || contextIntention.getName().equalsIgnoreCase("ideationalunit")) {
                            dynamicline = bqc.computeDynamicline(contextIntention, dynamicline);
                        }
                    }
                }
            }

            //choose a signal selector
            SignalSelector selector = chooseSelectorFor(intention, context);

            //find the correponding BehaviorSet
            BehaviorSet set = lexicon.fromIntentionToBehaviorSet(intention, selector.getType());

            // in diectic if we have the target attribute the Agent have to use the gaze
            BehaviorSet deict_set = new BehaviorSet("deictic-gaze");
            // if target attribute is != null a new behaviorset for the gaze is crated
            if (intention.getName().equals("deictic") && intention.getTarget()!= null && intention.getTarget()!= ""){
                SignalItem gaz = new SignalItem("1", "gaze", null);
                deict_set.add(gaz);
            }

            //search existing signals durring the intention :
            List<Signal> existingSignals = new ArrayList<Signal>();
            int firstSignal = selectedSignals.size();
            for (int i = 0; i < selectedSignals.size(); ++i) {
                Signal contextSignal = selectedSignals.get(i);
                double otherStart = contextSignal.getStart().getValue();
                double otherEnd = contextSignal.getEnd().getValue();
                if (otherStart < end && start < otherEnd) {
                    existingSignals.add(contextSignal);
                    if (i < firstSignal) {
                        firstSignal = i;
                    }
                }
            }

            //let the selector choose the signals
            List<Signal> signalsReturned = selector.selectFrom(intention, set, dynamicline, existingSignals, getCharacterManager());
            //if signalsReturned is empty, it means that no Signal can be added.
            //  It's normal !
            //but if signalsReturned is null, it means that the selector cannot performe this kind of intention
            //  so there is a problem with the choice of the selector.

            // if the behaviorset for the gaze is != null it is created a GazeSignal with target like find in the fml, influence null and it is reported also the character_id
            if (deict_set.getBaseSignals().size() != 0){
                List<Signal> sign = selector.selectFrom(intention, deict_set, dynamicline, existingSignals, getCharacterManager());
                if (sign.size() > 0){
                    GazeSignal signa = (GazeSignal) sign.get(0);
                    signa.setInfluence(null);

                    String tg = intention.getTarget();
                    int underscoreIndex = tg.indexOf(":");
                    if (underscoreIndex != -1){
                        // name agent to gaze
                        String agent = tg.substring(underscoreIndex + 1).trim();
                        signa.setTarget(agent); // set agent name as target
                        //System.out.println(CharacterManager.getStaticInstance().currentPosition.keySet().size());
                        /*for ( String key : CharacterManager.getStaticInstance().currentPosition.keySet() ) {
                            // if equal to the character get as target, take the id
                            //System.out.println(key.get(1));
                            if(key.equals(agent)){ // check the name of the agent to gaze
                                signa.setOrigin(key); // take the character id
                            }
                        }*/
                    }else {
                        signa.setTarget(intention.getTarget());
                        signa.setCharacterManager(this.getCharacterManager());
                    }
                    signalsReturned.add(signa);
                }
            }

            if (signalsReturned != null) {
                for (Signal toAdd : signalsReturned) {
                    if (toAdd instanceof SignalTargetable && intention instanceof IntentionTargetable) {
                        ((SignalTargetable) toAdd).setTarget(((IntentionTargetable) intention).getTarget());
                    }
                    //add strokes in the signal
                    strokeFiller.fill(toAdd, set, dynamicline, existingSignals);
                    //add signal in result list in chronological order
                    TimeMarker startTime = toAdd.getStart();
                    boolean isAdded = false;
                    if (startTime.isConcretized()) {
                        //TODO check synchrony of TimeMarkers in signals (relative time)
                        for (int i = firstSignal; i < selectedSignals.size(); ++i) {
                            Signal added = selectedSignals.get(i);
                            if (toAdd.getStart().getValue() < added.getStart().getValue()) {
                                selectedSignals.add(i, toAdd);
                                isAdded = true;
                                break;
                            }
                        }
                    }
                    if (!isAdded) {
                        selectedSignals.add(toAdd);
                    }
                }
            }

            //reset character playing
            if (intention.hasCharacter()) {
                baseline.set(getCharacterManager().getValueString(BaseLine.CHARACTER_PARAMETER_BASELINE));
                lexicon.setDefinition(getCharacterManager().getValueString(Lexicon.CHARACTER_PARAMETER_INTENTION_LEXICON));
                bqc.setDefinition(getCharacterManager().getValueString(BehaviorQualityComputer.CHARACTER_PARAMETER_QUALIFIERS));
            }
        }

        // Ideational Units Creation and Pre-Processing
        IdeationalUnitFactory ideationalUnitFactory = new IdeationalUnitFactory();
        for (Intention intention : intentions) {
            if (intention instanceof IdeationalUnitIntention) {
                String ideationalUnitMainIntentionId = ((IdeationalUnitIntention) intention).getMainIntentionId();
                IdeationalUnit ideationalUnit = ideationalUnitFactory.newIdeationalUnit(intention.getId(), ideationalUnitMainIntentionId);
                for (Signal currentSignal : selectedSignals) {
                    if (currentSignal instanceof GestureSignal) {
                        if (currentSignal.getTimeMarker("ready").getValue() >= intention.getStart().getValue() && currentSignal.getTimeMarker("relax").getValue() <= intention.getEnd().getValue()) {
                            ideationalUnit.addSignal(currentSignal);
                            ((GestureSignal) currentSignal).setIdeationalUnit(ideationalUnit);
                        }
                    }
                }
            }
        }
        ideationalUnitFactory.preprocessIdeationalUnits();

        sendSignals(selectedSignals, requestId, mode);
    }

    @Override
    public void addSignalPerformer(SignalPerformer performer) {
        if (performer != null) {
            signalPerformers.add(performer);
        }
    }

    @Override
    public void removeSignalPerformer(SignalPerformer performer) {
        if (performer != null) {
            signalPerformers.remove(performer);
        }
    }

    /**
     * Adds a {@code SignalSelector}.<br> The selector add may be choose to
     * select signal.
     *
     * @param selector the selector to add
     */
    public void addSelector(SignalSelector selector) {
        if (selector != null) {
            otherSelector.add(selector);
        }
    }

    /**
     * Returns the {@code Lexicon} of this behavior planner.
     *
     * @return the {@code Lexicon}
     */
    public Lexicon getLexicon() {
        return lexicon;
    }

    /**
     * Sends a list of {@code Signal} to all known {@code SignalPerformer}.
     *
     * @param signals the list of {@code Signal} to emmit
     * @param id the identifier to send
     * @param mode : blend, replace, append
     */
    protected void sendSignals(List<Signal> signals, ID id, Mode mode) {
        if (signals != null) {
            for (SignalPerformer performer : signalPerformers) {
                performer.performSignals(signals, id, mode);
            }
        }
    }

    /**
     * Set a specific {@code StrokeFiller}.<br/> The default one used by the
     * {@code planner} is an {@code EmptyStrokeFiller} that never adds strokes.
     *
     * @param filler the {@code StrokeFiller} to set
     */
    public void setStrokeFiller(StrokeFiller filler) {
        if (filler != null) {
            strokeFiller = filler;
        } else {
            Logs.warning(this.getClass().getName() + " : in setStrokeFiller() the parameter is null. No changes are applied.");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println(this.getClass().getSimpleName() + " finalized!");
        getCharacterManager().remove(lexicon);
        lexicon = null;

        getCharacterManager().remove(baseline);
        baseline = null;

        getCharacterManager().remove(bqc);
        bqc = null;

        mseSelector = null;

        defaultSelector = null;

        otherSelector.clear();
        otherSelector = null;

        selectedSignals.clear();
        selectedSignals = null;

        signalPerformers.clear();
        signalPerformers = null;

        strokeFiller = null;

        super.finalize();
    }

    @Override
    public void onCharacterChanged() {
        Logs.info("Planner received onCharacterChanged, but does nothing itself. Should it ?");
    }

    public void UpdateLexicon(){
        //remove the old lexicon to be sure to not have two lexicons
        this.getCharacterManager().remove(lexicon);
        lexicon = new Lexicon(this.getCharacterManager());
    }
}
