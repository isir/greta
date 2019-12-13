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
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

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
        List<Intention> newIntentionsfromSpeech = processText(input, listPitchAccent);
          
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
    
    //Perform the simplified Lesk algorithm for Word disambiguation. 
    //It looks up in the WordNet dictionnary the glossary of each meaning for the word.
    //The meaning that has more word in common with the current context is the selected meaning
    public ISynset simplifiedLesk(IIndexWord idxWord, String context) {
        if (idxWord != null && idxWord.getWordIDs().size() > 0 && context != null) {
            ISynset bestSense = dict.getWord(idxWord.getWordIDs().get(0)).getSynset();
            int maxOverlap = 0;
            String[] contextArray = context.split(" ");
            for (IWordID otherSense : idxWord.getWordIDs()) {
                IWord word = dict.getWord(otherSense);
                String[] glossArray = word.getSynset().getGloss().split(" ");
                int overlap = 0;
                for (String cont : contextArray) {
                    if (cont.length() < 4) {
                        continue;
                    }
                    for (String glos : glossArray) {
                        if (glos.length() < 4) {
                            continue;
                        }
                        if (cont.toLowerCase().equals(glos.toLowerCase())) {
                            overlap++;
                        }
                    }
                }
                if (overlap > maxOverlap) {
                    maxOverlap = overlap;
                    bestSense = word.getSynset();
                }
            }
            return bestSense;
        }
        return null;
    }
    
    //Return the Image Schemas that can be found for this Synset (meaning).
    //This is a recursive function that starts at the synset of the word,
    //looks for Image Schemas and then continue up the tree by following the hypernyms (more global meaning) of the current set
    private Set<String> getImageSchemas(ISynset synset, int depth) {

        Set<String> toReturn = new HashSet<>();
        switch (synset.getPOS()) {
            case NOUN:
                if (dictSynsetToImageSchema.getImageSchemasForNoun(synset.getID().toString()) != null) {
                    toReturn.addAll(dictSynsetToImageSchema.getImageSchemasForNoun(synset.getID().toString()));
                }
                break;
            case VERB:
                if (dictSynsetToImageSchema.getImageSchemasForVerb(synset.getID().toString()) != null) {
                    toReturn.addAll(dictSynsetToImageSchema.getImageSchemasForVerb(synset.getID().toString()));
                }
                break;
            case ADJECTIVE:
                if (dictSynsetToImageSchema.getImageSchemasForAdjective(synset.getID().toString()) != null) {
                    toReturn.addAll(dictSynsetToImageSchema.getImageSchemasForAdjective(synset.getID().toString()));
                }
                break;
            case ADVERB:
                if (dictSynsetToImageSchema.getImageSchemasForAdverb(synset.getID().toString()) != null) {
                    toReturn.addAll(dictSynsetToImageSchema.getImageSchemasForAdverb(synset.getID().toString()));
                }
                break;
            default:
                break;
        }
        List<ISynsetID> relatedSynset = synset.getRelatedSynsets(Pointer.HYPERNYM);
        //FOR NOW WE STOP AS SOON AS WE FIND ONE IMAGE SCHEMA. If I remove the toReturn.size()>0, 
        //we will continue as long as there is an hypernym to this synset
        if (relatedSynset.isEmpty() || depth <= 0 || toReturn.size() > 0) {
            return toReturn;
        } else {
            ISynset next = dict.getSynset(relatedSynset.get(0));
            toReturn.addAll(getImageSchemas(next, depth - 1));
            return toReturn;
        }
        //return toReturn;
    }
    
    private boolean isWithinPitchAccent(List<int[]> listPitchAccent, int indexWord) {

        for (int[] pa : listPitchAccent) {
            if (indexWord >= pa[0] && indexWord <= pa[1]) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        performers.add(ip);
    }
    
    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }
    
    public List<Intention> processText(String input, List<int[]> listPitchAccent) {
        System.out.println(input);
        XMLParser xmlParser = XML.createParser();
        XMLTree inputXML = xmlParser.parseBuffer(input);
        //List<int[]> listPitchAccent = new ArrayList<>();
        //remove the description tag that creates trouble with the parser and 
        //retrieve the pitch accent for future access

        for (XMLTree xmltbml : inputXML.getChildren()) {
            for (XMLTree xmlt : xmltbml.getChildren()) {
                if (xmlt.isNamed("description")) {
                    xmltbml.removeChild(xmlt);
                }
                if (xmlt.isNamed("pitchaccent")) {
                    int start = Integer.parseInt(xmlt.getAttribute("start").replace("s1:tm", ""));
                    int end = Integer.parseInt(xmlt.getAttribute("end").replace("s1:tm", ""));
                    int[] pitchAccent = {start, end};
                    //listPitchAccent.add(pitchAccent);
                }
            }
        }
        //Get rid of the xml tags for text processing
        String tagFreeInput = inputXML.toString().replaceAll("<[^>]+>", " ");
        tagFreeInput = tagFreeInput.trim().replaceAll(" +", " ");
        List<XMLTree> imageSchemasGenerated = new ArrayList<>();

        int countTimeMarkers = 0;
        int countSentenceMarkers = 0;
        int countIdeationalUnit = 0;
        int countImageSchema = 0;

        //prepare the reader for our value
        StringReader sr = new StringReader(tagFreeInput);

        //*********   FIRST WE START BY AUGMENTING THE TEXT ***************
        //prepare the XML structure to store the speech in a FML-APML way
        XMLTree fmlApmlRoot = XML.createTree("fml-apml");
        XMLTree bmlRoot = fmlApmlRoot.addChild(inputXML);
        XMLTree fmlRoot = fmlApmlRoot.createChild("fml");

        //split by sentences, for each sentences:
        for (List<HasWord> sentence : new DocumentPreprocessor(sr)) {
            imageSchemasGenerated.clear();
            boolean hasVerb = false;
            boolean afterVerb = false;
            boolean negation = false;

            Set<String> imageSchemas = new HashSet<>();
            Tree parse = lp.apply(sentence);
            parse.pennPrint();

            System.out.println();
            List<TypedDependency> tdl = null;
            //retrieve the grammatical dependencies
            if (gsf != null) {
                GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
                tdl = gs.typedDependenciesCCprocessed();
                System.out.println(tdl);
                System.out.println();
            }

            //Prepare the ideational unit structure
            XMLTree ideationalUnit = fmlRoot.createChild("ideationalunit");
            ideationalUnit.setAttribute("id", "id_" + countIdeationalUnit++);
            ideationalUnit.setAttribute("importance", "1.0");
            ideationalUnit.setAttribute("start", "s1:tm" + countTimeMarkers + "-0.2");
            ideationalUnit.setAttribute("end", "s1:tm" + (countTimeMarkers + sentence.size() - 1) + "-0.2");
            String[] listToken = new String[sentence.size()];
            String[] listPos = new String[sentence.size()];
            //A first loop that checks if there is a verb in the sentence and prepare the sentence for chunking
            for (int i = 0; i < sentence.size(); i++) {
                //retrieve the word and its grammar posTag 
                CoreLabel cl = (CoreLabel) sentence.get(i);
                String type = cl.tag();

                listToken[i] = cl.originalText();
                listPos[i] = cl.tag();
                if (type != null && (type.equals("VB") || type.equals("VBD") || type.equals("VBG") || type.equals("VBN") || type.equals("VBZ") || type.equals("VBP"))) {
                    hasVerb = true;
                }

            }
            //retrieve the BIO (begin inside out) tags for the chunks
            String chunktag[] = chunker.chunk(listToken, listPos);
            XMLTree previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers);

            //XMLTree imageSchema = fmlRoot.createChild("imageschema");
            //imageSchema.setAttribute("id", "id_" + countImageSchema++);
            //The second loop
            for (int i = 0; i < sentence.size(); i++) {
                CoreLabel cl = (CoreLabel) sentence.get(i);
                imageSchemas.clear();

                String posTag = cl.tag();
                String value = cl.originalText();

                //mapping PENN from stanford to wordnet POS
                //this part can act as a filter
                POS pos = null;
                switch (posTag) {
                    //noun singular or mass
                    case "NN":
                        pos = POS.NOUN;
                        break;
                    //noun plural
                    case "NNS":
                        pos = POS.NOUN;
                        break;
                    //Verb
                    case "VB":
                        pos = POS.VERB;
                        break;
                    //Verb past tense
                    case "VBD":
                        pos = POS.VERB;
                        break;
                    //Verb gerondif
                    case "VBG":
                        pos = POS.VERB;
                        break;
                    //Verb past participle
                    case "VBN":
                        pos = POS.VERB;
                        break;
                    //Verb 3rd person singular present (s in english)
                    case "VBZ":
                        pos = POS.VERB;
                        break;
                    //Verb singular present other person (without the s)
                    case "VBP":
                        pos = POS.VERB;
                        break;
                    //Adjective
                    case "JJ":
                        pos = POS.ADJECTIVE;
                        break;
                    //Adjective Comparative    
                    case "JJR":
                        pos = POS.ADJECTIVE;
                        break;
                    //Adjective Superlative
                    case "JJS":
                        pos = POS.ADJECTIVE;
                        break;
                    //adverb
                    case "RB":
                        pos = POS.ADVERB;
                        break;
                    //adverb comparative
                    case "RBR":
                        pos = POS.ADVERB;
                        break;
                    //adverb superlative
                    case "RBS":
                        pos = POS.ADVERB;
                        break;
                    //particle
                    case "RP":
                        pos = POS.ADVERB;
                        break;
                    //DEBUG : this is unsafe, just to keep some particular words in check
                    case "IN":
                        pos = POS.ADVERB;
                        break;
                    default:
                        pos = null;
                        break;
                }

                //begin wordnet code
                List<String> stems = new ArrayList<>();

                //We retrieve the stems for the word.
                if (pos == POS.NOUN || pos == POS.ADJECTIVE || pos == POS.VERB || pos == POS.ADVERB) {
                    stems = wns.findStems(value, pos);
                }

                //check if we are at the verb
                if (pos == POS.VERB || chunktag[i].equals("B-VP")) {
                    afterVerb = true;
                }

                if (!hasVerb || (hasVerb && afterVerb)) {
                    //for each stem
                    for (String stem : stems) {
                        //we retrieve the word from wordnet
                        IIndexWord idxWord = dict.getIndexWord(stem, pos);
                        if (idxWord != null) {

                            //we retrieve the synset
                            ISynset synset = this.simplifiedLesk(idxWord, SentenceUtils.listToOriginalTextString(sentence));
                            //System.out.println("Stem : " + stem + " POS:" + pos);
                            /*for (IWordID idw : idxWord.getWordIDs()) {
                                System.out.println("ID : " + idw.getSynsetID().getOffset());
                            }*/
                            //System.out.println(stem + " id:" + synset.getOffset());
                            //THE IMPORTANT PART : we retrieve the image schemas for this synset
                            Set<String> imscSet = getImageSchemas(synset, 10);

                            imageSchemas.addAll(imscSet);

                        }
                    }

                    //Depending on the chunk, I will construct the image schema differently
                    //If we begin a new chunk (the B tag), I either delete an empty previous Image Schema (created as a placeholder) or
                    //if the previous is not empty, I close it with an end tag and I open a new one (with createXMLImageSchema).
                    if (chunktag[i].startsWith("B")) {

                        if (previousImageSchema != null) {
                            if (previousImageSchema.getAttribute("type") == "") {
                                fmlRoot.removeChild(previousImageSchema);
                                previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);

                            } else {
                                if (!imageSchemas.isEmpty()) {
                                    previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl.index() - 1) + "-0.2");
                                    imageSchemasGenerated.add(previousImageSchema);
                                    previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);

                                } else {
                                    //This is used so the Image Schema coming from a previous chunk can span unto the next chunk until there is a new Image Schema.
                                    previousImageSchema.setAttribute("previous", "true");
                                }
                            }
                        } else {
                            previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);

                        }
                        //If an Image Schema is identified for this word, I insert it
                        if (!imageSchemas.isEmpty()) {
                            String imageRef = imageSchemas.iterator().next().toLowerCase();
                            previousImageSchema.setAttribute("type", imageRef);
                            previousImageSchema.setAttribute("POSroot", pos.toString());
                        }

                    }
                    //If I am within a chunk, I should just update the existing previous Image Schema that
                    //has been created for the whole chunk during the B case, OR create a new one in case of multiple instance of a noun in a NP or verb in a VP
                    if (chunktag[i].startsWith("I")) {
                        if (previousImageSchema == null) {
                            previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);
                            //Check if the Image Schema was coming from a previous chunk
                        } else if (previousImageSchema.getAttribute("previous") == "true" && !imageSchemas.isEmpty()) {

                            previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl.index() - 1) + "-0.2");
                            imageSchemasGenerated.add(previousImageSchema);
                            previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);

                        }
                        if (!imageSchemas.isEmpty()) {
                            switch (chunktag[i]) {
                                case "I-NP": {
                                    if (previousImageSchema.getAttribute("POSroot") == "") {
                                        setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());
                                    } else {
                                        if (previousImageSchema.getAttribute("POSroot").equals(POS.NOUN.toString())) {
                                            setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());

                                        }
                                        if (pos.equals(POS.ADJECTIVE) || isWithinPitchAccent(listPitchAccent, cl.index()) || pos.equals(POS.NOUN) && !previousImageSchema.getAttribute("POSroot").equals(POS.ADJECTIVE.toString())) {
                                            if (previousImageSchema.getAttribute("type") == "") {
                                                fmlRoot.removeChild(previousImageSchema);
                                            } else {
                                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl.index() - 1) + "-0.2");
                                                imageSchemasGenerated.add(previousImageSchema);
                                            }
                                            XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);
                                            setImageSchemaType(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());
                                            previousImageSchema = imageschema;
                                        }
                                    }
                                    break;
                                }

                                case "I-VP": {
                                    if (previousImageSchema.getAttribute("POSroot") == "") {
                                        setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());
                                    } else {
                                        if (previousImageSchema.getAttribute("POSroot").equals(POS.VERB.toString())) {
                                            setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());
                                        }
                                        if (pos.equals(POS.ADVERB) || isWithinPitchAccent(listPitchAccent, cl.index()) || pos.equals(POS.VERB) && !previousImageSchema.getAttribute("POSroot").equals(POS.ADVERB.toString())) {
                                            if (previousImageSchema.getAttribute("type") == "") {
                                                fmlRoot.removeChild(previousImageSchema);
                                            } else {
                                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl.index() - 1) + "-0.2");
                                                imageSchemasGenerated.add(previousImageSchema);
                                            }
                                            XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);
                                            setImageSchemaType(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());
                                            previousImageSchema = imageschema;
                                        }
                                    }
                                    break;
                                }

                                default: { //I-PRT, I-INTJ, I-SBAR, I-ADJP, I-ADVP, I-PP
                                    if (previousImageSchema.getAttribute("POSroot") == "") {
                                        setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());

                                    } else {
                                        if (previousImageSchema.getAttribute("type") == "") {
                                            fmlRoot.removeChild(previousImageSchema);
                                        } else {
                                            previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl.index() - 1) + "-0.2");
                                            imageSchemasGenerated.add(previousImageSchema);
                                        }
                                        XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);
                                        setImageSchemaType(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());
                                        previousImageSchema = imageschema;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    if (chunktag[i].startsWith("O")) {
                        if (previousImageSchema != null) {
                            if (previousImageSchema.getAttribute("type") == "") {
                                fmlRoot.removeChild(previousImageSchema);
                            } else {
                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl.index() - 1) + "-0.2");
                                imageSchemasGenerated.add(previousImageSchema);
                            }

                        }
                        previousImageSchema = null;
                    }

                }

                negation = posTag.equalsIgnoreCase("DT") && value.equalsIgnoreCase("no") || posTag.equalsIgnoreCase("RB") && value.equalsIgnoreCase("not");
                //System.out.println(negation);

            }
            countSentenceMarkers += sentence.size();
            if (previousImageSchema != null) {
                if (previousImageSchema.getAttribute("type").equals("")) {
                    fmlRoot.removeChild(previousImageSchema);
                } else {
                    previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers - 1) + "-0.2");
                    imageSchemasGenerated.add(previousImageSchema);
                }

            }

            //If no root could be found, 
            if (!ideationalUnit.hasAttribute("main")) {
                //If no root could be found, delete the ideational unit.
                if (imageSchemasGenerated.size() > 0) {
                    ideationalUnit.setAttribute("main", imageSchemasGenerated.get(0).getAttribute("id"));
                } else {
                    fmlRoot.removeChild(ideationalUnit);
                }
            }
        }
        System.out.println(fmlApmlRoot.toString());

        //TO INTENTIONS
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fmlApmlRoot, this.cm);

        return intentions;

    }
}
