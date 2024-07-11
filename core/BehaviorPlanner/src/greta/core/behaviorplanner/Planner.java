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
import greta.core.feedbacks.FeedbackEmitter;
import greta.core.feedbacks.FeedbackPerformer;
import greta.core.ideationalunits.IdeationalUnit;
import greta.core.ideationalunits.IdeationalUnitFactory;
import greta.core.intentions.IdeationalUnitIntention;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionPerformer;
import greta.core.intentions.IntentionTargetable;
import greta.core.signals.BMLTranslator;
import greta.core.signals.GazeSignal;
import greta.core.signals.MessageSender;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.signals.SignalTargetable;
import greta.core.signals.TorsoSignal;
import greta.core.signals.gesture.GestureSignal;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.log.Logs;
import greta.core.util.speech.Speech;
import greta.core.util.time.Temporizer;
import greta.core.util.time.TimeMarker;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
public class Planner extends CharacterDependentAdapter implements IntentionPerformer, SignalEmitter{

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
     * @param mode
     * @throws java.io.FileNotFoundException
     * @throws javax.xml.transform.TransformerException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode) {
        
        System.out.println("PERFORM INTENTIOS");
        selectedSignals = new ArrayList<Signal>();
        /**
        GazeSignal gaze = new GazeSignal("1");
        gaze.setStartValue("1");
        gaze.setTarget("Andre_chair0");
        selectedSignals.add(gaze);
        **/
        //temporize every intentions
        Temporizer temporizer = new Temporizer();
        temporizer.add(intentions);
        temporizer.temporize();
        String fml_construction="";
        int flag=0;
        
        boolean MeaningMiner_treatement=false;

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
            
            //Start NVBG TREATMENT
            if(this.getCharacterManager().get_use_NVBG()){
                
                String phrase="";
                List<Signal> signals=new ArrayList<Signal>();
                boolean NVBG_worked=false;
                List<String> gestures=null;
                String fml_gestures_tag="";
                int max_index=0;
                for(Signal sig: signalsReturned){
                    try {
                        System.out.println("GRETA Returned:"+sig.getClass());
                        if (sig.getModality()=="speech" && !NVBG_worked){
                            NVBG_worked=true;
                            Speech m = (Speech) sig;
                            m.getOriginalText();
                            List<Object> f=m.getSpeechElements();
                            for(Object ob:f){
                                if (ob.getClass()==String.class){
                                    phrase = phrase+ob;

                                }
                            }
                            XMLParser bmlparser = XML.createParser();
                            MessageSender msg_send = new MessageSender();
                            System.out.println("INFO: "+phrase);
                            phrase=phrase.replaceAll("  ", " ");
                            if(phrase.startsWith(" ")){
                                phrase=phrase.substring(1);
                            }
                            if(flag==0)
                            fml_construction="<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n<fml-apml>\n";
                            String construction="<bml>"+
                                    "\n<speech id=\"s1\" language=\"english\" start=\"0.0\" text=\"\" type=\"SAPI4\" voice=\"marytts\" xmlns=\"\">"+
                                    "\n<description level=\"1\" type=\"gretabml\"><reference>tmp/from-fml-apml.pho</reference></description>";
                            fml_construction=fml_construction+construction;
                            //System.out.println(phrase.replaceAll("  ", " ").substring(1));
                            String[] sp=phrase.split(" ");
                            int i=1;
                            for(int j=0;j<sp.length;j++){
                                if(flag==0)
                                    fml_construction=fml_construction+"\n<tm id=\"tm"+i+"\"/>"+sp[j];
                                construction=construction+"\n<tm id=\"tm"+i+"\"/>"+sp[j];
                                i++;
                                max_index=i;
                            }
                            if(flag==0)
                                fml_construction=fml_construction+"\n</speech>\n</bml>\n<fml>";
                            construction=construction+"\n</speech>\n</bml>";
                            try {
                                    try {
                                        gestures = msg_send.traitement_NVBG(phrase,this.getCharacterManager().getEnvironment().getNVBG_Open());
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                this.getCharacterManager().getEnvironment().setNVBG_Open(true);
                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (JMSException ex) {
                                Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            //System.out.println("Out " + gestures);
                            if(gestures!=null){
                                for(String y : gestures){
                                    String[] k=y.split("importance");
                                    y=k[0];
                                    String bml_modif=construction.toString();
                                    String[] g=y.split("lexeme=");
                                    String b= g[1].substring(1,g[1].indexOf(" ")-1);
                                    String[] c=y.replace("<","").split(" ");

                                    String addend="<description priority=\"1\" type=\"gretabml\">"+
                                            "\n<reference>"+c[0]+"="+b+"</reference>"+
                                            "\n<intensity>1.000</intensity>"+
                                            "`\n<SPC.value>0.646</SPC.value>"+
                                            "\n<TMP.value>-0.400</TMP.value>"+
                                            "\n<FLD.value>0.000</FLD.value>"+
                                            "\n<PWR.value>0.000</PWR.value>"+
                                            "\n<REP.value>0.000</REP.value>"+
                                            "\n<OPN.value>0.000</OPN.value>"+
                                            "\n<TEN.value>0.000</TEN.value>"+
                                            "\n</description>";
                                    bml_modif=construction.replaceAll("</bml>",y.replace(c[0],"gesture")+">"+"\n"+addend+"\n</gesture>\n</bml>");
                                    flag=1;
                                    if(flag==1){
                                    String ends[] = y.split("end=");
                                    String ends2[]=ends[1].split(":");
                                    String ends3[]=ends2[1].split(" ");
                                    //System.out.println("INFO ENDS[]:"+ends2[0]+"    "+ends3[0]+"   "+max_index);
                                    if(Integer.parseInt(ends3[0].replace("\"","").replace("tm",""))>max_index){
                                        System.out.println("INFO ENDS[]:"+ends2[0].replace("\"","")+"    "+ends3[0].replace("\"","").replace("tm","")+"   "+max_index);
                                        y=y.replace("end="+ends2[0].replace("\"","")+":"+ends3[0].replace("\"","").replace("tm",""),"end="+ends2[0]+":"+Integer.toString(max_index));
                                    }
                                    fml_construction=fml_construction+"\n"+y.replace("lexeme","type")+"importance=\"1.0\"/>";
                                    // fml_construction=fml_construction+"\n"+y.replace("lexeme","type");
                                    }
                                    //System.out.println("FML FILE\n:"+fml_construction);
                                    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                                    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                                    Document document = docBuilder.parse(new InputSource(new StringReader(bml_modif)));
                                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                                    Transformer transformer = transformerFactory.newTransformer();
                                    DOMSource source = new DOMSource(document);
                                    FileWriter writer = new FileWriter(new File(System.getProperty("user.dir")+"\\test_fml.xml"));
                                    StreamResult result = new StreamResult(writer);
                                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                                    transformer.transform(source, result);
                                    XMLTree bml_mod = bmlparser.parseFile(System.getProperty("user.dir")+"\\test_fml.xml");
                                    signals.addAll(BMLTranslator.BMLToSignals(bml_mod, this.getCharacterManager()));
                                    //System.out.println("SIGNALS:"+signals);
                                }
                            }

                            // Fin traitment NVBG
                        }
                    }
                    //if signalsReturned is empty, it means that no Signal can be added.
                    //  It's normal !
                    //but if signalsReturned is null, it means that the selector cannot performe this kind of intention
                    //  so there is a problem with the choice of the selector.
                    catch (SAXException ex) {
                        Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (TransformerConfigurationException ex) {
                        Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ParserConfigurationException ex) {
                        Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (TransformerException ex) {
                        Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                    }



                }
                // If NVBG found animations we will add the animations translated in signals to the signals
                if(gestures!=null){
                    signalsReturned.addAll(signals);
                }
            }

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
        
        
        if(flag==1){
                fml_construction=fml_construction+"\n</fml>\n</fml-apml>";
                System.out.println("greta.core.behaviorplanner.Planner.performIntentions(): fml start");
                System.out.println(fml_construction);
                System.out.println("greta.core.behaviorplanner.Planner.performIntentions(): fml end");                
                try{
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document document = docBuilder.parse(new InputSource(new StringReader(fml_construction)));
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(document);
                FileWriter writer = new FileWriter(new File(System.getProperty("user.dir")+"\\fml_output.xml"));
                StreamResult result = new StreamResult(writer);
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(source, result);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformerConfigurationException ex) {
                    Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformerException ex) {
                    Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        

        
        /*System.out.println("greta.core.behaviorplanner.Planner.performIntentions()"+ selectedSignals);
        for(Signal p : selectedSignals){
            if(p.getModality()=="torso"){
                TorsoSignal ts=(TorsoSignal)p;
                System.out.println("INFO "+ts.getLexeme());
            }
        }
        */
        // Ideational Units Creation and Pre-Processing
        IdeationalUnitFactory ideationalUnitFactory = new IdeationalUnitFactory();
        for (Intention intention : intentions) {
            Logs.debug("[INFO IDEATIONAL_UNIT]:"+intention.getClass());
            if (intention instanceof IdeationalUnitIntention) {
                String ideationalUnitMainIntentionId = ((IdeationalUnitIntention) intention).getMainIntentionId();
                IdeationalUnit ideationalUnit = ideationalUnitFactory.newIdeationalUnit(intention.getId(), ideationalUnitMainIntentionId);
                for (Signal currentSignal : selectedSignals) {
                    if (currentSignal instanceof GestureSignal) {
                        Logs.debug("[INFO IDEATIONAL_UNIT]:"+currentSignal + "  " + selectedSignals);
                        if (currentSignal.getTimeMarker("ready").getValue() >= intention.getStart().getValue() && currentSignal.getTimeMarker("relax").getValue() <= intention.getEnd().getValue()) {
                            ideationalUnit.addSignal(currentSignal);
                            Logs.debug("[INFO IDEATIONAL_UNIT]:"+currentSignal);
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
            // System.out.println("greta.core.behaviorplanner.Planner.sendSignals()");
            for(Signal s:signals){
                System.out.println("greta.core.behaviorplanner.Planner.sendSignals(): "+s.getClass());
            }
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
