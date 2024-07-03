/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.MeaningMiner;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import greta.auxiliary.activemq.TextReceiver;
import greta.auxiliary.activemq.WhiteBoard;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.intentions.PseudoIntentionPitchAccent;
import greta.core.intentions.PseudoIntentionSpeech;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.time.SynchPoint;
import greta.core.util.time.TimeMarker;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import org.xml.sax.SAXException;

/**
 *
 * @author Donatella Simonetti
 */
public class FMLReceiver_MeaningMiner extends TextReceiver implements IntentionEmitter{

    private ArrayList<IntentionPerformer> performers;
    private XMLParser fmlParser;
    private CharacterManager cm;

    private ImageSchemaExtractor imgSchmext;

    private ArrayList<IntentionPerformer> intentionsPerformers = new ArrayList<>();
    private DictionnarySynsetImageSchema dictSynsetToImageSchema = new DictionnarySynsetImageSchema();
    private DictionnaryImageSchemaGesture dictImageSchemaToGesture = new DictionnaryImageSchemaGesture();
    private String WORDNETDICTPATH = "./Common/Data/wordnet30/dict";
    private String STANFORDPARSER = "./Common/Data/stanfordparser/englishPCFG.ser.gz";
    private String OPENNLPCHUNKER = "./Common/Data/opennlp/en-chunker.bin";
    private ChunkerME chunker = null;
    private WordnetStemmer wns;
    private LexicalizedParser lp;
    private TreebankLanguagePack tlp;
    private GrammaticalStructureFactory gsf;
    private IDictionary dict;
    
    private ImageSchemaExtractor im;

    public FMLReceiver_MeaningMiner(CharacterManager cm) {

        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "greta.FML",cm);

        this.cm = cm;

        //this.imgSchmext = new ImageSchemaExtractor(this.cm);
        //load the JWI wordnet classes
        URL url = null;
        try {
            url = new URL("file", null, WORDNETDICTPATH);
        } catch (MalformedURLException e) {
            System.out.println(e.toString());
        }
        if (url == null) {
            return;
        }

        // construct the dictionary object and open it
        dict = new Dictionary(url);
        try {
            dict.open();
        } catch (IOException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Prepare the stemmer
        wns = new WordnetStemmer(dict);

        //Load the parser chunkerModel from the stanford parser
        lp = LexicalizedParser.loadModel(STANFORDPARSER);

        //Load the Tree Bank for english
        tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
        if (tlp.supportsGrammaticalStructures()) {
            gsf = tlp.grammaticalStructureFactory();
        }

        //Load the OpenNLP Chunker
        InputStream modelIn = null;
        ChunkerModel chunkerModel = null;
        try {
            modelIn = new FileInputStream(OPENNLPCHUNKER);
            chunkerModel = new ChunkerModel(modelIn);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        chunker = new ChunkerME(chunkerModel);
        
        im = new ImageSchemaExtractor(cm);

    }

    public FMLReceiver_MeaningMiner(String host, String port, String topic, CharacterManager cm) {
        super(host, port, topic);
        this.cm = cm;
        performers = new ArrayList<IntentionPerformer>();
        fmlParser = XML.createParser();
        fmlParser.setValidating(false);
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {
        // parse the fml message
        XMLTree fml = fmlParser.parseBuffer(content.toString());
        //XMLTree bmlxml = fmlParser.parseBuffer(content.toString());
        XMLTree pitchxml = XML.createTree("speech");

        String fml_id = "";
        List<int[]> listPitchAccent = new ArrayList<>();

        if (fml == null) {
            return;
        }

        Mode mode = FMLTranslator.getDefaultFMLMode();
        if (fml.hasAttribute("composition")) {
            mode.setCompositionType(fml.getAttribute("composition"));
        }
        if (fml.hasAttribute("reaction_type")) {
            mode.setReactionType(fml.getAttribute("reaction_type"));
        }
        if (fml.hasAttribute("reaction_duration")) {
            mode.setReactionDuration(fml.getAttribute("reaction_duration"));
        }
        if (fml.hasAttribute("social_attitude")) {
            mode.setSocialAttitude(fml.getAttribute("social_attitude"));
        }
        if(fml.hasAttribute("id")){
            fml_id = fml.getAttribute("id");
        }else{
            fml_id = "fml_1";
        }

        for (XMLTree fmlchild : fml.getChildrenElement()) {
            // store the bml id in the Mode class
            if (fmlchild.isNamed("bml")) {
                if(fmlchild.hasAttribute("id")){
                    mode.setBml_id(fmlchild.getAttribute("id"));
                }
                for (XMLTree bmlchild : fmlchild.getChildrenElement()){
                    if (bmlchild.isNamed("speech")){
                        for (XMLTree child : bmlchild.getChildrenElement()){
                            if (child.isNamed("description")) {
                                bmlchild.removeChild(child);
                            }else if(child.isNamed("pitchaccent")){
                                pitchxml.addChild(child); // add the pitchaccent in a XML tree specific
                            }
                        }
                    }
                }
            }
        }

        String plaintext = "";
        // take the speech elements end translate them in a simple text
        /*plaintext = bmlxml.toString().replaceAll("<[^>]+>", "");
        plaintext = plaintext.trim().replaceAll(" +", " "); // delete multiple spaces*/

        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml, cm);

        PseudoIntentionSpeech speech = new PseudoIntentionSpeech(this.cm);

        // set if the timemearker name start from tmO or tm1
        int startfrom = 0;
        List<TimeMarker> justcheck = speech.getTimeMarkers();
        String st = justcheck.get(1).getName();
        if (st.lastIndexOf('0') != -1){
            startfrom = 0;
        }else if(st.lastIndexOf('1') != -1){
            startfrom = 1;
        }

        // take the PseudoIntentionSpeech in order to put a timemarker for each word
        for (Intention intent : intentions){
            if (intent instanceof PseudoIntentionSpeech){
                speech.addSpeechElement(((PseudoIntentionSpeech) intent).getSpeechElements()); // take the utterances
                //speech.setLanguage(((PseudoIntentionSpeech) intent).getLanguage()); // the language
                speech.setId(((PseudoIntentionSpeech) intent).getId());
                //speech.setReference(((PseudoIntentionSpeech) intent).getReference());
                //speech.addPhonems(((PseudoIntentionSpeech) intent).getPhonems());
                intentions.remove(intent);
                break;
            }
        }

        HashMap<String, String> wordandTimeMarker = new HashMap<String, String>();

        int numWords = 0;
        if (speech != null){
            for (Object word : speech.getSpeechElements()){
                if (word instanceof ArrayList){
                    for(Object array_obj : (ArrayList) word){
                        if(array_obj instanceof String){
                            String w =(String) array_obj;
                            if (!w.equals(" ")){
                                plaintext += (String) array_obj;

                                String utterence = (String) array_obj;
                                utterence.trim();
                                //utterence.replace(" +", " ");
                                List<String> words = Arrays.asList(utterence.split(" "));
                                numWords += words.size() - 1;

                            }
                        }else if (array_obj instanceof TimeMarker){
                            String actualTM = ((TimeMarker) array_obj).getName();
                            wordandTimeMarker.put(actualTM, "tm"+numWords);

                        }
                    }
                    break;
                }
            }
        }

        // create the intention for the Newspeech
        PseudoIntentionSpeech newSpeech = createSpeechIntention(plaintext, wordandTimeMarker);
        newSpeech.setLanguage(speech.getLanguage());
        newSpeech.setId(speech.getId());
        newSpeech.setReference(speech.getReference());
        newSpeech.getStart().addReference("start");

        //add the markers in a list
        List<TimeMarker> tm_list = new ArrayList<TimeMarker>();
        for (Object obj: newSpeech.getSpeechElements()){
            if(obj instanceof ArrayList){
                String last_tm_name = "";
                for (Object array_obj : (ArrayList) obj){
                    if(array_obj instanceof TimeMarker){
                        if(!((TimeMarker) array_obj).getName().equals(last_tm_name)){
                            tm_list.add((TimeMarker) array_obj);
                            last_tm_name = ((TimeMarker) array_obj).getName();
                        }
                    }
                }
            }
        }

        // create a bml xmltree with the new speech and timemarker to put as input for the meaningMiner computation
        XMLTree treebml = toXML(newSpeech); // speech child

        //newSpeech set timemarkers list
        newSpeech.setMarkers(tm_list);

        // update the timemarker for each intention according the newSpeech markers
        for(Intention intens : intentions){

            TimeMarker end = intens.getEnd();
            TimeMarker start = intens.getStart();
            int[] strt_end = new int[2];
            int counter = 0;

            List<TimeMarker> list_tm = new ArrayList<TimeMarker>();
            list_tm.add(end);
            list_tm.add(start);
            for (TimeMarker m : list_tm){
                List<SynchPoint> list_sypoint = m.getReferences();

                double offset_synchpnt = m.getReferences().get(0).getOffset();

                String targetname = list_sypoint.get(0).getTargetName();
                // index of number in the string
                int column = targetname.indexOf(":");// example s1:tm2
                String nametm = targetname.substring(column + 1, targetname.length());
                // TODO intentions have correct timemarkers
                String new_nametm = wordandTimeMarker.get(nametm);
                m.removeReferences();
                String newtm = targetname.substring(0,targetname.indexOf(":") + 1) + new_nametm;
                m.addReference(newtm);
                m.getReferences().get(0).setOffset(offset_synchpnt);

                int nmb = Integer.parseInt(new_nametm.substring(2, new_nametm.length()));

                strt_end[counter] = nmb;
                counter++;
            }

            if (intens instanceof PseudoIntentionPitchAccent){
                Arrays.sort(strt_end);
                int[] pitchAccent = {strt_end[0], strt_end[1]};
                listPitchAccent.add(pitchAccent);
            }
        }

        // create the input for the MeaningMiner computation
        XMLTree bmlRoot = XML.createTree("bml");
        bmlRoot.addChild(treebml); // bml part where after each word is set a timemarker

        //MeaningMiner
        String input = bmlRoot.toString();
        List<Intention> newIntentionsfromSpeech;
        try {
            newIntentionsfromSpeech = im.processText_3(input, listPitchAccent);
            // add the intentions found with the mining miner to the others
            if (newIntentionsfromSpeech.size() > 0){
                for (int i = 0; i < newIntentionsfromSpeech.size(); i++)
                if (newIntentionsfromSpeech.get(i) instanceof PseudoIntentionSpeech){
                    PseudoIntentionSpeech spc = (PseudoIntentionSpeech) newIntentionsfromSpeech.get(i);
                    spc.setId(speech.getId());
                    intentions.add(spc);
                }else{
                    intentions.add(newIntentionsfromSpeech.get(i));
                }
            }
        } catch (TransformerException ex) {
            Logger.getLogger(FMLReceiver_MeaningMiner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FMLReceiver_MeaningMiner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(FMLReceiver_MeaningMiner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FMLReceiver_MeaningMiner.class.getName()).log(Level.SEVERE, null, ex);
        }



        Object contentId = null;
        if (fml.hasAttribute("id")) {
            contentId = fml.getAttribute("id");
        }
        else {
            contentId = properties.get("content-id");
        }

        ID id = IDProvider.createID(contentId == null ? "FMLReceiver" : contentId.toString());
        id.setFmlID(fml_id); // add the fml id
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, mode);
        }
    }

    public PseudoIntentionSpeech createSpeechIntention(String plaintext, HashMap<String, String> wordandTimeMarker){
        PseudoIntentionSpeech pis = new PseudoIntentionSpeech(this.cm);

        ArrayList<String> listofSpeechElement = new ArrayList<String>();
        int lastcharposition = 0;
        boolean reading_word = false;
        // read each caracter
        for (int i=0; i < plaintext.length(); i++){
            if (plaintext.charAt(i) == ' ' || plaintext.charAt(i) == '.' || plaintext.charAt(i) == ';' || plaintext.charAt(i) == ',' || plaintext.charAt(i) == ':' || plaintext.charAt(i) == '!' || plaintext.charAt(i) == '?' ||
                               plaintext.charAt(i) == '\n' || plaintext.charAt(i) == '\"' || plaintext.charAt(i) == '(' || plaintext.charAt(i) == ')' || plaintext.charAt(i) == '/'){

                if (i!=0 && reading_word){
                    listofSpeechElement.add(plaintext.substring(lastcharposition, i)); // add the special caracter
                }
                //char c = plaintext.charAt(i);
                if (plaintext.charAt(i) != ' ' && plaintext.charAt(i) != '\n'){
                    listofSpeechElement.add(String.valueOf(plaintext.charAt(i))); // add the special caracter
                }
                lastcharposition = i+1;
                reading_word = false;
            }else{
                reading_word = true;
            }
        }

        // create a PseudoIntentionSpeech with the new speechElement and timemarker
        ArrayList<Object> speechelement = new ArrayList<Object>();
        int counter = 0;
        for (String str : listofSpeechElement){
            speechelement.add(new TimeMarker("tm"+counter)); //add the TimeMarker
            speechelement.add(str);// add the word
            counter++;
        }
        speechelement.add(new TimeMarker("tm"+counter)); //add last TimeMarker
        pis.addSpeechElement(speechelement);
        return pis;
    }

    //Insert a new xml imageschema in the XML tree. This object will be completed later or even deleted if no Image Schema could be put there.
    private XMLTree createXMLImageSchema(XMLTree fmlRoot, int countImageSchema, int countTimeMarker) {
        XMLTree imageschema = fmlRoot.createChild("imageschema");
        imageschema.setAttribute("importance", "1.0");
        imageschema.setAttribute("id", "im_" + countImageSchema);
        imageschema.setAttribute("start", "s1:tm" + (countTimeMarker) + "-0.2");

        return imageschema;
    }

    //Set the Image Schema to this xml imageschema. It checks if this was the root of the sentence to decide if it should be the main of the current ideational unit.
    private void setImageSchemaType(XMLTree imageschema, String imageRef, String pos, XMLTree ideationalUnit, List<TypedDependency> tdl, Integer indexWord) {

        imageschema.setAttribute("type", imageRef);
        imageschema.setAttribute("indexword", indexWord.toString());
        imageschema.setAttribute("POSroot", pos);

        for (TypedDependency td : tdl) {
            if (td.dep().index() == indexWord) {
                if (td.reln().toString().equals("root")) {
                    ideationalUnit.setAttribute("main", imageschema.getAttribute("id"));
                }
                break;
            }
        }
    }

    public XMLTree toXML( PseudoIntentionSpeech newSpeech){
        XMLTree toReturn;

            toReturn = XML.createTree("speech");
            toReturn.setAttribute("id", newSpeech.getId());
            toReturn.setAttribute("language", newSpeech.getLanguage());
            toReturn.setAttribute("voice","marytts");
            toReturn.setAttribute("type", "SAPI4");
            TimeMarker start = newSpeech.getMarkers().get(0);
            SynchPoint synchRef = start.getFirstSynchPointWithTarget();
            if(synchRef != null) {
                toReturn.setAttribute("start", Double.toString(synchRef.getValue()));
            }
            else{
                if(start.concretizeByReferences()) {
                    toReturn.setAttribute("start", ""+start.getValue());
                }
            }
            String ref = newSpeech.getReference();
            if(ref != null) {
                toReturn.setAttribute("ref", ref);
            }
            for(int i=1; i< newSpeech.getSpeechElements().size()-1; ++i){ //skip start and end
                if (newSpeech.getSpeechElements().get(i) instanceof ArrayList){
                    ArrayList arraylist = (ArrayList) newSpeech.getSpeechElements().get(i);
                    for (Object obj : arraylist){
                        if(obj instanceof String) {
                            toReturn.addText((String)obj);
                        }
                        else{
                            XMLTree tm = toReturn.createChild("tm");
                            tm.setAttribute("id", ((TimeMarker)obj).getName());
                                // time ?
                        }
                    }

                }

            }

        return toReturn;
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        performers.add(ip);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }
    
}
