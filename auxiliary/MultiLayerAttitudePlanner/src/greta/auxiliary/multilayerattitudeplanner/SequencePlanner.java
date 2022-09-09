/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.multilayerattitudeplanner;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.BufferedWriter;
import java.io.File;
import greta.auxiliary.multilayerattitudeplanner.structures.SequenceProbabilityTuple;
import greta.auxiliary.multilayerattitudeplanner.structures.ValuedAttitudeVariation;
import greta.auxiliary.multilayerattitudeplanner.structures.NVBEventType;
import greta.auxiliary.multilayerattitudeplanner.structures.AttitudeDimension;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import greta.auxiliary.multilayerattitudeplanner.structures.AttitudeCluster;
import greta.auxiliary.socialparameters.SocialDimension;
import greta.auxiliary.socialparameters.SocialParameterFrame;
import greta.auxiliary.socialparameters.SocialParameterPerformer;
import greta.core.behaviorplanner.MultimodalSignalSelector;
import greta.core.behaviorplanner.Planner;
import greta.core.behaviorplanner.SignalSelector;
import greta.core.behaviorplanner.baseline.BaseLine;
import greta.core.behaviorplanner.baseline.BehaviorQualityComputer;
import greta.core.behaviorplanner.baseline.DynamicLine;
import greta.core.behaviorplanner.lexicon.BehaviorSet;
import greta.core.behaviorplanner.lexicon.Lexicon;
import greta.core.behaviorplanner.lexicon.SignalItem;
import greta.core.behaviorplanner.strokefillers.SmoothStrokeFiller;
import greta.core.behaviorplanner.strokefillers.StrokeFiller;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionPerformer;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalProvider;
import greta.core.signals.TorsoSignal;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.animationparameters.APFrameList;
import greta.core.util.id.ID;
import greta.core.util.time.Temporizer;
import greta.core.util.time.TimeMarker;
import greta.core.util.time.Timer;

/**
 * MultiLayer Attitude Expression main class : it computes the best sequence of 
 * signals for expressing a set of intentions wrt an attitude variation
 *
 * @author Mathieu
 */
public class SequencePlanner extends Planner implements IntentionPerformer, SignalEmitter, SocialParameterPerformer//, AttitudePerformer 
{

    //lexicon adapted for the model : we don't consider all kinds of intentions
    //and we require that every intention can be instantiated with a reasonable set of signals
    private Lexicon lexicon; 
    public final String LEXICON_FILE = ".\\Common\\Data\\MultiLayerAttitude\\lexicon.xml";
    
    //neutral baseline, because we do not consider other sources of expressivity variability
    private BaseLine baseline;
    public final String BASELINE_FILE = ".\\Common\\Data\\MultiLayerAttitude\\Baseline.xml";
    
    //behavior qualifier adapted for attitudes (unused for now, will be in near future)
    private BehaviorQualityComputer bqc;
    public Map<String, String> behaviorQualifiersMap;
    public final String BQC_DATA_DIR = ".\\Common\\Data\\MultiLayerAttitude\\BQC\\";
    
    //Expressive parameters computers
    private Map<String, ExpressiveParameterComputer> expressiveParamCompMap;
    private ExpressiveParameterComputer currentEPC;
    
    //Maurizio's model of signal selection (we do not use MSE here)
    private MultimodalSignalSelector mss;
    
    //regular stroker filler
    private StrokeFiller strokeFiller;
    
    //the attitude variations we received last that have to be performed 
    private ValuedAttitudeVariation nextFrdVariation;
    private ValuedAttitudeVariation nextDomVariation;
    
    //class used to query the frequent sequences database for support/confidence
    private MinedSequenceQuerier msq;
    
    //bayesian networks trained from weka BIF files to generate new sequences
    private BayesNet bnfrd;
    private BayesNet bndom;
    public final String BAYES_NET_DOM_FILE = ".\\Common\\Data\\MultiLayerAttitude\\DomOnlyBif.xml";
    public final String BAYES_NET_FRD_FILE = ".\\Common\\Data\\MultiLayerAttitude\\FrdOnlyBif.xml";
    
    //logs
    public final String COMPUTED_SEQUENCES_FILE = ".\\Common\\Data\\MultiLayerAttitude\\BML\\ComputedSequences.csv";
    public CSVWriter computedSequencesWriter;
    
    //List of social parameter to use in order to compute the attitude
    APFrameList<SocialParameterFrame> socialParameterFrames;
    SocialParameterFrame lastSocialParameterFrame;

    public SequencePlanner(CharacterManager cm) throws IOException, Exception {
        super(cm);
        
        lexicon = new Lexicon(super.getCharacterManager());
        lexicon.setDefinition(LEXICON_FILE);
        
        baseline = new BaseLine(super.getCharacterManager());
        baseline.set(BASELINE_FILE);
        
        behaviorQualifiersMap = new HashMap<String, String>();
        behaviorQualifiersMap.put("DomBigDecr", BQC_DATA_DIR+"BQCDomBigDecr.xml");
        behaviorQualifiersMap.put("DomSmallDecr", BQC_DATA_DIR+"BQCDomSmallDecr.xml");
        behaviorQualifiersMap.put("DomSmallIncr", BQC_DATA_DIR+"BQCDomSmallIncr.xml");
        behaviorQualifiersMap.put("DomBigIncr", BQC_DATA_DIR+"BQCDomBigIncr.xml");
        behaviorQualifiersMap.put("FrdBigDecr", BQC_DATA_DIR+"BQCFrdBigDecr.xml");
        behaviorQualifiersMap.put("FrdSmallDecr", BQC_DATA_DIR+"BQCFrdSmallDecr.xml");
        behaviorQualifiersMap.put("FrdSmallIncr", BQC_DATA_DIR+"BQCFrdSmallIncr.xml");
        behaviorQualifiersMap.put("FrdBigIncr", BQC_DATA_DIR+"BQCFrdBigIncr.xml");
        bqc = new BehaviorQualityComputer(super.getCharacterManager());
        for (String s : behaviorQualifiersMap.values())
        {
            bqc.addDefinition(s);
        }
        
        expressiveParamCompMap = new HashMap<String, ExpressiveParameterComputer>();
        expressiveParamCompMap.put("DomBigDecr", new ExpressiveParameterComputer("DomBigDecr"));
        expressiveParamCompMap.put("DomSmallDecr",  new ExpressiveParameterComputer("DomSmallDecr"));
        expressiveParamCompMap.put("DomSmallIncr",  new ExpressiveParameterComputer("DomSmallIncr"));
        expressiveParamCompMap.put("DomBigIncr",  new ExpressiveParameterComputer("DomBigIncr"));
        expressiveParamCompMap.put("FrdBigDecr",  new ExpressiveParameterComputer("FrdBigDecr"));
        expressiveParamCompMap.put("FrdSmallDecr",  new ExpressiveParameterComputer("FrdSmallDecr"));
        expressiveParamCompMap.put("FrdSmallIncr",  new ExpressiveParameterComputer("FrdSmallIncr"));
        expressiveParamCompMap.put("FrdBigIncr",  new ExpressiveParameterComputer("FrdBigIncr"));
        expressiveParamCompMap.put("Neutral",  new ExpressiveParameterComputer("Neutral"));
        currentEPC=expressiveParamCompMap.get("Neutral");
        
        mss = new MultimodalSignalSelector();
        strokeFiller = new SmoothStrokeFiller();

        nextFrdVariation = new ValuedAttitudeVariation(AttitudeDimension.Friendliness);
        nextDomVariation = new ValuedAttitudeVariation(AttitudeDimension.Dominance);
        //String[] att = {"BigIncr", "SmallIncr", "SmallDecr", "BigDecr"};
        
        msq = new MinedSequenceQuerier();
        
        bndom = new BayesNet(BAYES_NET_DOM_FILE, true);        
        bnfrd = new BayesNet(BAYES_NET_FRD_FILE, false);
        computedSequencesWriter=new CSVWriter(new BufferedWriter(new FileWriter(new File(COMPUTED_SEQUENCES_FILE))));
        
        SocialParameterFrame defaultFrame = new SocialParameterFrame(Timer.getCurrentFrameNumber());
        defaultFrame.applyValue(SocialDimension.Liking, 0);
        defaultFrame.applyValue(SocialDimension.Dominance, 0);
        defaultFrame.applyValue(SocialDimension.Familiarity, 0);
        socialParameterFrames = new APFrameList<SocialParameterFrame>(defaultFrame);
        lastSocialParameterFrame = new SocialParameterFrame(Timer.getCurrentFrameNumber()+1);
        lastSocialParameterFrame.applyValue(SocialDimension.Liking, 0);
        lastSocialParameterFrame.applyValue(SocialDimension.Dominance, 0);
        lastSocialParameterFrame.applyValue(SocialDimension.Familiarity, 0);
    }

    //OLD
    /*@Override
    public void performAttitude(ValuedAttitudeVariation av) {
        if (av.getAttitudeVariation().getDimension().equals(AttitudeDimension.Dominance)) {
            nextDomVariation = av;
        } else if (av.getAttitudeVariation().getDimension().equals(AttitudeDimension.Friendliness)) {
            nextFrdVariation = av;
        }
    }*/

    /*
     * Sequence planning :
     * 1/First we compute dynamiclines/lexicon with the current attitude, for adapting
     * global behavior tendencies (INTERACTION LAYER : FOURTH LAYER OF THE MODEL)
     * 
     * 2/Then we let maurizio's regular MultimodalSignalSelector compute appropriate signals
     * for the intentions (INTENTIONS LAYER : FIRST LAYER OF THE MODEL)
     * TODO : change with with only querying all the possible sequences for the intentions
     * (using the candidate sets of signals for every intention), then choose the best that contains
     * any one of these possible sequences
     * 
     * 3/Then we compute the most appopriate sequence with the chosen attitude variation
     * and containing the signals computed from the MSS (SEQUENCE LAYER : SECOND LAYER OF THE MODEL)
     * 
     * 4/Finally we temporize the sequence
     * 
     */
    @Override
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode) {
        List<Signal> selectedSignals = new ArrayList<Signal>();

        //temporize every intentions
        Temporizer temporizer = new Temporizer();
        temporizer.add(intentions);
        temporizer.temporize();

        //sort intentions by importance, starting time, duration.
        sortIntentions(intentions);
        
        //attitude change computation
        SocialParameterFrame socialAttitude = SocialParameterFrame.SubstractFrames(socialParameterFrames.getCurrentFrame(), 
                lastSocialParameterFrame);
        double domVal = socialAttitude.getDoubleValue(SocialDimension.Dominance);
        double frdVal = socialAttitude.getDoubleValue(SocialDimension.Liking);
        
        nextDomVariation = new ValuedAttitudeVariation(AttitudeDimension.Dominance, domVal);
        nextFrdVariation = new ValuedAttitudeVariation(AttitudeDimension.Friendliness, frdVal);

        BayesNet bn = bndom;
        ValuedAttitudeVariation nextVariation = nextDomVariation;

        //For now we only express one attitude at a time : here we choose which one
        //if one is null : the other. else, the maximum intensity
        if(nextDomVariation.getAttitudeVariation().getCluster().equals(AttitudeCluster.Null)
                && nextFrdVariation.getAttitudeVariation().getCluster().equals(AttitudeCluster.Null))
        {
            bn=bnfrd;
            nextVariation = new ValuedAttitudeVariation(AttitudeDimension.Friendliness, 1-(Math.random()*2));
            lastSocialParameterFrame.setDoubleValue(SocialDimension.Liking, 
                    socialParameterFrames.getCurrentFrame().getDoubleValue(SocialDimension.Liking));
        }
        else if(nextFrdVariation.getAttitudeVariation().getCluster().equals(AttitudeCluster.Null))   
        {
            bn=bndom; 
            nextVariation = nextDomVariation; 
            lastSocialParameterFrame.setDoubleValue(SocialDimension.Dominance, 
                    socialParameterFrames.getCurrentFrame().getDoubleValue(SocialDimension.Dominance));
        }
        else if(nextDomVariation.getAttitudeVariation().getCluster().equals(AttitudeCluster.Null))
        {
            bn=bnfrd;
            nextVariation = nextFrdVariation;
            lastSocialParameterFrame.setDoubleValue(SocialDimension.Liking, 
                    socialParameterFrames.getCurrentFrame().getDoubleValue(SocialDimension.Liking));
        }
        else 
        {
            if(Math.abs(domVal)>Math.abs(frdVal))
            {
                bn=bndom; 
                nextVariation = nextDomVariation; 
                lastSocialParameterFrame.setDoubleValue(SocialDimension.Dominance, 
                        socialParameterFrames.getCurrentFrame().getDoubleValue(SocialDimension.Dominance));
            }
            else
            {
                bn=bnfrd;
                nextVariation = nextFrdVariation;
                lastSocialParameterFrame.setDoubleValue(SocialDimension.Liking, socialParameterFrames.getCurrentFrame().getDoubleValue(SocialDimension.Liking));
            }
        }
        setBehaviorQualifier(nextVariation);
        
        Map<Intention, List<List<SignalItem>>> candidateSets = new HashMap<Intention, List<List<SignalItem>>>();
        for (Intention intention : intentions) {
            //baseline.set(CharacterManager.getValueString(BaseLine.CHARACTER_PARAMETER_BASELINE, intention.getCharacter()));
            //lexicon.setDefinition(CharacterManager.getValueString(Lexicon.CHARACTER_PARAMETER_INTENTION_LEXICON, intention.getCharacter()));
            //##########################
            //1/ ADAPT DYNAMICLINES WITH ATTITUDES
            // (change the Behavior Quality Computer with corresponding attitude)
            // TODO : pour l'instant on a que un BQC et dynamicline "neutres"
            // on verra quand on aura extrait les données "quanti"
            //##########################
            //compute the dynamicline 
            //bqc.setDefinition(CharacterManager.getValueString(BehaviorQualityComputer.CHARACTER_PARAMETER_QUALIFIERS, 
            //        behaviorQualifiersMap.get("Dom"+nextDomVariation.toString())));
            
            
            DynamicLine dynamicline = bqc.computeDynamicline(intention, baseline);
            //bqc.setDefinition(CharacterManager.getValueString(BehaviorQualityComputer.CHARACTER_PARAMETER_QUALIFIERS, 
            //        behaviorQualifiersMap.get("Frd"+nextFrdVariation.toString())));
            dynamicline = bqc.computeDynamicline(intention, dynamicline);

            
            //##########################
            //2/ GET MINIMAL SEQUENCES OF SIGNALS
            //pour l'instant on utiliser le planner classique pour nous choisir des signaux "de base"...
            //TODO : recuperer tous les CandidateSet, et calculer les "minimal sequences"
            //##########################
            double start = intention.getTimeMarker("start").getValue();
            double end = intention.getTimeMarker("end").getValue();
            
            //find the correponding BehaviorSet
            BehaviorSet set = lexicon.fromIntentionToBehaviorSet(intention, mss.getType());

            //search existing signals durring the intention :
            List<Signal> existingSignals = new ArrayList<Signal>();
            int firstSignal = selectedSignals.size();
            for (int i = 0; i < selectedSignals.size(); ++i) {
                Signal contextSignal = selectedSignals.get(i);
                double otherStart = contextSignal.getTimeMarker("start").getValue();
                double otherEnd = contextSignal.getTimeMarker("end").getValue();
                if (otherStart < end && start < otherEnd) {
                    existingSignals.add(contextSignal);
                    if (i < firstSignal) {
                        firstSignal = i;
                    }
                }
            }

            //let the selector choose the signals
            List<Signal> signalsReturned = mss.selectFrom(intention, set, dynamicline, existingSignals,super.getCharacterManager());
            //if signalsReturned is empty, it means that no Signal can be added.
            //  It's normal !
            //but if signalsReturned is null, it means that the selector cannot performe this kind of intention
            //  so there is a problem with the choice of the selector.
            
            List<List<SignalItem>> candidates = mss.findCandidates(intention, set, dynamicline, existingSignals);
            List<List<SignalItem>> finalCandidates = new ArrayList<List<SignalItem>>();
            for(List<SignalItem> lsi : candidates)
            {
                if(lsi.size()==1)
                {
                    finalCandidates.add(lsi);
                }
            }
            candidateSets.put(intention, finalCandidates);

            if (signalsReturned != null) {
                for (Signal toAdd : signalsReturned) {
                    //add signal in result list in chronological order
                    TimeMarker startTime = toAdd.getTimeMarker("start");
                    boolean isAdded = false;
                    if (startTime.isConcretized()) {
                        //TODO check synchrony of TimeMarkers in signals (relative time)
                        for (int i = firstSignal; i < selectedSignals.size(); ++i) {
                            Signal added = selectedSignals.get(i);
                            if (toAdd.getTimeMarker("start").getValue() < added.getTimeMarker("start").getValue()) {
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
        }
        
        //get signals that can be considered as part of the Sequence planning mechanism
        List<NVBEventType> signalsStrings = SequenceSignalProvider.listSignalAsNVBEvents(selectedSignals);
        //get all timemarkers...
        List<TimeMarker> allTimeMarkers = SequenceTemporizer.getTimeMarkersInIntentionsList(intentions);
        //... and the amount of "holes" remaining (the amount of places where there is not already a signal)
        allTimeMarkers = SequenceTemporizer.orderTimeMarkerList(allTimeMarkers);
        int maxNumberOfSignals = Math.min(allTimeMarkers.size()-1,3);
        
        List<NVBEventType> timings = new ArrayList<NVBEventType>();
        for(int i=0;i<allTimeMarkers.size()-1;i++)
        {
            boolean taken = false;
            for(Signal s : SequenceSignalProvider.cleanSignals(selectedSignals))
            {
                if(SignalProvider.getBegining(s).getValue()==allTimeMarkers.get(i).getValue())
                {
                    timings.add(SequenceSignalProvider.signalAsNVBEvent(s));
                    taken=true;
                }
            }
            if(!taken)
                timings.add(NVBEventType.Any);
        }

        boolean speaking = true;
        if (speaking) {
            List<SequenceProbabilityTuple> sequence = new ArrayList<SequenceProbabilityTuple>();
            //##########################
            //3/COMPUTE SEQUENCE OF NON-VERBAL SIGNALS
            //##########################
            
        
            
            try {
                //compute candidate sequences
                sequence =bn.getOrderedTuplesContainingSignals(maxNumberOfSignals, 
                        nextVariation.getAttitudeVariation(), signalsStrings,msq,timings);
            } catch (Exception ex) {
                Logger.getLogger(SequencePlanner.class.getName()).log(Level.SEVERE, null, ex);
            }

            //get the sequence with the best score over all sequences (with varying lengths)
            SequenceProbabilityTuple chosenSpt = new SequenceProbabilityTuple();
            for(SequenceProbabilityTuple spt : sequence)
            {
                if(spt!=null)
                {
                    if(spt.probability>chosenSpt.probability)
                    {
                        chosenSpt = spt;
                    }
                }
            }
            chosenSpt.printSequence();
            
            List<NVBEventType> mappedSequence = mapSequenceToTiming(chosenSpt.signals,timings);
            
            //##########################
            //4/TEMPORIZE SEQUENCE 
            //##########################
           List<Signal> temporizedSequence = new ArrayList<Signal>();
            if(mappedSequence.size()>0)
                temporizedSequence = SequenceTemporizer.temporizeSequence(mappedSequence, selectedSignals, intentions);
            
            //little check to make sure we are not adding an already temporized signal twice
            //trick because torso is bugged!?§
            //have to copy list because of concurrent modifications...
            List<Signal> selectedSignalsCopy = new ArrayList<Signal>(selectedSignals);
            for(Signal s : selectedSignals)
            {
                if(s instanceof TorsoSignal)
                    selectedSignalsCopy.remove(s);
            }
            selectedSignals=selectedSignalsCopy;
            for(Signal s : temporizedSequence)
            {
                if(!selectedSignals.contains(s))
                    selectedSignals.add(s);
            }
            
            //##########################
            //new and experimental : adapt expressive parameters
            //##########################
            setExpressiveParameterComputer(nextVariation);
            currentEPC.adaptSequence(selectedSignals);
            
            
            //write sequences file
            String[] toWrite = new String[5+timings.size()+mappedSequence.size()];
            toWrite[0] = requestId.toString();
            toWrite[1] = nextVariation.getAttitudeVariation().getDimension().toString();
            toWrite[2] = nextVariation.getAttitudeVariation().getCluster().toString();
            toWrite[3] = "SEQUENCE";
            for(int i=0;i<mappedSequence.size();i++)
            {
                toWrite[i+4]=mappedSequence.get(i).toString();
            }
            toWrite[4+mappedSequence.size()] = "TIMINGS";
            for(int i=0;i<timings.size();i++)
            {
                toWrite[i+5+mappedSequence.size()]=timings.get(i).toString();
            }
            computedSequencesWriter.writeNext(toWrite);
            try {
                computedSequencesWriter.flush();
            } catch (IOException ex) {
                Logger.getLogger(SequencePlanner.class.getName()).log(Level.SEVERE, null, ex);
            }            
        } else {
            //##########################
            //IF LISTENING, THEN USE REGULAR PLANNER MECHANISM 
            //##########################
        }
        sendSignals(selectedSignals, requestId, mode);
    }

    private List<NVBEventType> mapSequenceToTiming(List<NVBEventType> chosenSequence, List<NVBEventType> timings) {
        
        List<NVBEventType> mappedSequence = new ArrayList<NVBEventType>();
        int i=0;
        for(int j=0;j<timings.size();j++)
        {
            if(i>=chosenSequence.size())
            {
                if(!timings.get(j).equals(NVBEventType.Any))
                {
                    mappedSequence.add(timings.get(j));
                }
                continue;
            }
            if(timings.get(j).equals(chosenSequence.get(i))
                    || timings.get(j).equals(NVBEventType.Any))
            {
                mappedSequence.add(chosenSequence.get(i));
                i++;
            }
            else
            {
                mappedSequence.add(timings.get(j));
            }
        }
        return mappedSequence;
    }
    
    @Override
    public void performSocialParameter(List<SocialParameterFrame> frames, ID requestId) {
        this.socialParameterFrames.addFrames(frames);        
    }
    
    private void setBehaviorQualifier(ValuedAttitudeVariation variation)
    {
        String bqcdef = variation.getAttitudeVariation().getDimension().equals(AttitudeDimension.Dominance)? "Dom" : "Frd";
        if(variation.getAttitudeVariation().getCluster().equals(AttitudeCluster.Null))
        {
            bqc.setDefinition(bqc.getDefaultDefinition().getName());
            return;
        }
        else
        {
            bqcdef+=variation.getAttitudeVariation().getCluster().toString();
        }
        bqc.setDefinition(behaviorQualifiersMap.get(bqcdef));

    }
    
    private void setExpressiveParameterComputer(ValuedAttitudeVariation variation)
    {
        String str="";
        if(variation.getAttitudeVariation().getDimension().equals(AttitudeDimension.Dominance))
        {
            str+="Dom";
        }
        else
        {
            str+="Frd";
        }
        
        if(variation.getAttitudeVariation().getCluster().equals(AttitudeCluster.BigDecr)
                || variation.getAttitudeVariation().getCluster().equals(AttitudeCluster.SmallDecr)
                || variation.getAttitudeVariation().getCluster().equals(AttitudeCluster.SmallIncr)
                || variation.getAttitudeVariation().getCluster().equals(AttitudeCluster.BigIncr))
        {
            str+=variation.getAttitudeVariation().getCluster().toString();
        }
        else
        {
            str="Neutral";
        }
        currentEPC=expressiveParamCompMap.get(str);
    }
}
