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
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.mit.jwi.item.Synset;
import edu.mit.jwi.item.SynsetID;

/**
 *
 * This class is the core of the Meaning Miner project. It acts as a text
 * analyzer that extracts Image Schemas from the String input and build an XML
 * FML file out of it with Image Schemas type of intention
 *
 * @author Brian Ravenet
 */
public class ImageSchemaExtractor implements MeaningMinerModule, IntentionEmitter, CharacterDependent {

    private CharacterManager charactermanager;

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
    
    private Process server_process = null;
    private Process client_process = null;
    private String python_init_env_path = "./Common/Data/MeaningMiner/python/init_env.bat";
    private String python_server_path = "./Common/Data/MeaningMiner/python/activate_server.bat";
    private String python_client_path = "./Common/Data/MeaningMiner/python/meaning_miner_adapter_client.py";
    private String lang = "fr";
    
    public ImageSchemaExtractor(CharacterManager cm) {

        setCharacterManager(cm);
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

//            try {
//                server_process = new ProcessBuilder(python_init_env_path).redirectErrorStream(true).start();
//                //client_process = new ProcessBuilder("python", "-c", "print('hello')").redirectErrorStream(true).start();
//            } catch (IOException ex) {
//                Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            int exitStatus = server_process.waitFor();
//            System.out.println("Env init status:" + exitStatus);
//            
//            try {
//                server_process = new ProcessBuilder(python_server_path).redirectErrorStream(true).start();
//                //client_process = new ProcessBuilder("python", "-c", "print('hello')").redirectErrorStream(true).start();
//            } catch (IOException ex) {
//                Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
//            }

    }

    /**
     * process an input string and split it by sentences first, then, for each
     * sentence, it will use the stanford lexical parser to identify the
     * grammatical role of each word, the OpenNLP for the chunks, and will use
     * WordNet to retrieve a semantic context for these words to be used to
     * extract Image Schemas. Using all these informations, it will build an FML
     * document containing Ideational Units and Image Schema type of gestures
     * where invariant are defined.
     *
     * Call from ImageSchemaExtractor in modular.jar (implemented in this file: greta.auxiliary.MeaningMiner.ImageSchemaExtractor)
     * 
     * @param input the input string to be processed
     * @throws javax.xml.transform.TransformerConfigurationException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    
    @Override
    public void processText(String input) throws TransformerConfigurationException, TransformerException, ParserConfigurationException, SAXException, IOException {
        
        List<Intention> intentions = _processText(input);

    }
    
    public List<Intention> processText_2(String input) throws TransformerConfigurationException, TransformerException, ParserConfigurationException, SAXException, IOException {
        
        List<Intention> intentions = _processText(input);
        return intentions;
        
    }
    
    public List<Intention> processText_3(String input, List<int[]> listPitchAccent) throws TransformerConfigurationException, TransformerException, ParserConfigurationException, SAXException, IOException {
        
        List<Intention> intentions = _processText_withPitchAccent(input, listPitchAccent);
        return intentions;
        
    }
        
    public List<Intention> _processText(String input) throws TransformerConfigurationException, TransformerException, ParserConfigurationException, SAXException, IOException {
        
        System.out.println("ImageSchema: processText2: start");
        
        //System.out.println(input);
        XMLParser xmlParser = XML.createParser();
        XMLTree inputXML = xmlParser.parseBuffer(input);
        List<int[]> listPitchAccent = new ArrayList<>();
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
                    listPitchAccent.add(pitchAccent);
                }
            }
        }

        List<Intention> intentions = _processTextCore(input, xmlParser, inputXML, listPitchAccent);
        
        return intentions;
    }

    public List<Intention> _processText_withPitchAccent(String input, List<int[]> listPitchAccent) throws TransformerConfigurationException, TransformerException, ParserConfigurationException, SAXException, IOException {
        
        System.out.println("ImageSchema: processText2: start");
        
        //System.out.println(input);
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

        List<Intention> intentions = _processTextCore(input, xmlParser, inputXML, listPitchAccent);
        
        return intentions;
    }
    
    public List<Intention> _processTextCore(String input, XMLParser xmlParser, XMLTree inputXML, List<int[]> listPitchAccent) throws IOException{
        
        // TODO: add function to make sure time markers are in word-level, not sentence level.
        
        //Get rid of the xml tags for text processing
        String tagFreeInput = inputXML.toString().replaceAll("<[^>]+>", "");
        List<XMLTree> imageSchemasGenerated = new ArrayList<>();

        int countTimeMarkers = 0;
        int countSentenceMarkers = 1;
        int countIdeationalUnit = 0;
        int countImageSchema = 0;
        
        //System.out.println("CHECK TEXT");
        String fix= new String();
        BufferedReader rea = new BufferedReader(new StringReader(tagFreeInput));
        String line1;
        while ((line1 = rea.readLine()) != null) {
            if(!line1.contains("tmp") && line1.trim().length()>0)
                fix = fix + " " + line1.trim() + "\n";
        }
        fix=fix.substring(0, fix.length()-1);
        System.out.println("[TAG FREE TEXT]:");
        System.out.println(fix);
        
        StringReader sr = new StringReader(fix);

        //*********   FIRST WE START BY AUGMENTING THE TEXT ***************
        //prepare the XML structure to store the speech in a FML-APML way
        XMLTree fmlApmlRoot = XML.createTree("fml-apml");
        XMLTree bmlRoot = fmlApmlRoot.addChild(inputXML);
        XMLTree fmlRoot = fmlApmlRoot.createChild("fml");

        //split by sentences, for each sentences:
        for (List<HasWord> sentence : new DocumentPreprocessor(sr)) {
            
            String[] token_array = new String[sentence.size()];
            String[] lemma_array = new String[sentence.size()];
            String[] pos_array = new String[sentence.size()];
            String[] chunk_tag_array = new String[sentence.size()];
            String[] gesture_ID_array = new String[sentence.size()];
            String[] gesture_lemma_array = new String[sentence.size()];

            if(lang == "fr"){
                String sentenceString = SentenceUtils.listToOriginalTextString(sentence);
                System.out.println("InputSentence: " + sentenceString);
                try {
                    client_process = new ProcessBuilder("python", python_client_path, sentenceString).redirectErrorStream(true).start();
                    //client_process = new ProcessBuilder("python", "-c", "print('hello')").redirectErrorStream(true).start();
                } catch (IOException ex) {
                    Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }
                InputStream inputStream = client_process.getInputStream();
                String result = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n")
                        );
                System.out.println("CLIENT OUTPUT");
                System.out.println(result);

                String[] split_result = result.split("@");
                token_array = split_result[0].split(" ");
                lemma_array = split_result[1].split(" ");
                pos_array = split_result[2].split(" ");
                chunk_tag_array = split_result[3].split(" ");
                gesture_ID_array = split_result[4].split(" ");
                gesture_lemma_array = split_result[5].split(" ");
            }


            System.out.println("HAS WORD:"+sentence.toString());
            imageSchemasGenerated.clear();
            boolean hasVerb = false;
            boolean afterVerb = false;
            boolean negation = false;

            Set<String> imageSchemas = new HashSet<>();
            Tree parse = lp.apply(sentence);
            parse.pennPrint();

            //System.out.println();
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
            System.out.println("[COUNT TIME MARKERS]:"+countTimeMarkers+"   "+sentence.size());
            ideationalUnit.setAttribute("end", "s1:tm" + (countTimeMarkers + sentence.size() - 1) + "-0.2");
            String[] listToken = new String[sentence.size()];
            String[] listPos = new String[sentence.size()];
            
            //A first loop that checks if there is a verb in the sentence and prepare the sentence for chunking
            for (int i = 0; i < sentence.size(); i++) {
                
                String type;
                if(lang == "fr"){
                    
                    type = pos_array[i];
                    
                    listToken[i] = token_array[i];
                    listPos[i] = pos_array[i];
                    
                    if(type != null && (type.equals("VERB"))){
                        hasVerb = true;
                    }
                    
                }
                else{

                    //retrieve the word and its grammar posTag
                    CoreLabel cl = (CoreLabel) sentence.get(i);
                    type = cl.tag();

                    listToken[i] = cl.originalText();
                    listPos[i] = cl.tag();

                    if (type != null && (type.equals("VB") || type.equals("VBD") || type.equals("VBG") || type.equals("VBN") || type.equals("VBZ") || type.equals("VBP"))) {
                        hasVerb = true;
                    }                    
                    
                }
                

            }
            System.out.println("hasVerb: " + true);
            
            //retrieve the BIO (begin inside out) tags for the chunks
            String chunktag[] = chunker.chunk(listToken, listPos);
            XMLTree previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countTimeMarkers);

            System.out.println("countSentenceMarkers: " + countSentenceMarkers);
            System.out.println("Sentence:" + sentence.toString());
                        
            //XMLTree imageSchema = fmlRoot.createChild("imageschema");
            //imageSchema.setAttribute("id", "id_" + countImageSchema++);
            //The second loop
            for (int i = 0; i < sentence.size(); i++) {
                
                CoreLabel cl = (CoreLabel) sentence.get(i);
                imageSchemas.clear();
                
                String posTag = new String();
                String value = new String();
                POS pos = null;
                                    
                if(lang == "fr"){
                    
                    posTag = pos_array[i];
                    value = token_array[i];

                    //mapping PENN from stanford to wordnet POS
                    //this part can act as a filter 
                    switch (posTag) {
                        //noun singular or mass
                        case "NOUN":
                            pos = POS.NOUN;
                            break;
                        //Verb
                        case "VERB":
                            pos = POS.VERB;
                            break;
                        //Adjective
                        case "ADJ":
                            pos = POS.ADJECTIVE;
                            break;
                        //adverb
                        case "ADV":
                            pos = POS.ADVERB;
                            break;
                        //particle
                        case "PART":
                            pos = POS.ADVERB;
                            break;
                        default:
                            pos = null;
                            break;
                    }
                    
                }
                else{
                    
                    posTag = cl.tag();
                    value = cl.originalText();

                    //mapping PENN from stanford to wordnet POS
                    //this part can act as a filter 
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

                }



                //begin wordnet code
                List<String> stems = new ArrayList<>();
                    
                if(lang == "fr"){

                    //We retrieve the stems for the word.
                    if (pos == POS.NOUN || pos == POS.ADJECTIVE || pos == POS.VERB || pos == POS.ADVERB) {
                        stems.add(lemma_array[i]);
                    }

                    //check if we are at the verb
                    if (pos == POS.VERB || chunk_tag_array[i].equals("B-VP")) {
                        afterVerb = true;
                    }

                }
                else{

                    //We retrieve the stems for the word.
                    if (pos == POS.NOUN || pos == POS.ADJECTIVE || pos == POS.VERB || pos == POS.ADVERB) {
                        stems = wns.findStems(value, pos);
                    }

                    //check if we are at the verb
                    if (pos == POS.VERB || chunktag[i].equals("B-VP")) {
                        afterVerb = true;
                    }

                }
                System.out.println("afterVerb: " + afterVerb);

                if (!hasVerb || (hasVerb && afterVerb)) {
                    //for each stem
                    for (String stem : stems) {
                        
                        if(lang == "fr"){
                            System.out.println(0);
                            if(!gesture_ID_array[i].equals("_")){
                                
                                //System.out.println(1);
                                ISynset synset = dict.getSynset(new SynsetID(Integer.parseInt(gesture_ID_array[i]), pos));
                                //System.out.println(2);
                                //System.out.println(3);
                                //IIndexWord indexWord = dict.getIndexWord(gesture_ID_array[i], pos);
                                //IWordID wordID = indexWord.getWordIDs().get(0);
                                //ISynset synset = dict.getWord(wordID).getSynset();

                                //THE IMPORTANT PART : we retrieve the image schemas for this synset
                                Set<String> imscSet = getImageSchemas(synset, 10);
                                //System.out.println(4);
                                imageSchemas.addAll(imscSet);

                                System.out.println("Synset: " + synset.toString() + " " + imscSet.toString());
                                System.out.println("countSentenceMarkers: " + countSentenceMarkers);
                                
                            }
                            
                        }
                        else{
                            
                            //we retrieve the word from wordnet
                            IIndexWord idxWord = dict.getIndexWord(stem, pos);
                            if (idxWord != null) {

                                //we retrieve the synset
                                ISynset synset = this.simplifiedLesk(idxWord, SentenceUtils.listToOriginalTextString(sentence));
                                /*System.out.println("Stem : " + stem + " POS:" + pos);
                                for (IWordID idw : idxWord.getWordIDs()) {
                                    System.out.println("ID : " + idw.getSynsetID().getOffset());
                                }
                                System.out.println(stem + " id:" + synset.getOffset());*/
                                //THE IMPORTANT PART : we retrieve the image schemas for this synset
                                Set<String> imscSet = getImageSchemas(synset, 10);
                                imageSchemas.addAll(imscSet);
                            }
                        }

                    }
                    
                    //Depending on the chunk, I will construct the image schema differently
                    //If we begin a new chunk (the B tag), I either delete an empty previous Image Schema (created as a placeholder) or
                    //if the previous is not empty, I close it with an end tag and I open a new one (with createXMLImageSchema).
                    
                    String chunk_tag_i;
                    Integer cl_index;
                    if(lang == "fr"){
                        
                        chunk_tag_i = chunk_tag_array[i];
                        cl_index = i;
                        
                    }
                    else{
                        
                        chunk_tag_i = chunktag[i];
                        cl_index = cl.index();

                    }
                    
                    if (chunk_tag_i.startsWith("B")) {

                        if (previousImageSchema != null) {
                            if (previousImageSchema.getAttribute("type") == "") {
                                fmlRoot.removeChild(previousImageSchema);
                                previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countTimeMarkers + cl_index);

                            } else {
                                if (!imageSchemas.isEmpty()) {
                                    previousImageSchema.setAttribute("end", "s1:tm" + (countTimeMarkers + cl_index) + "-0.2");
                                    imageSchemasGenerated.add(previousImageSchema);
                                    previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countTimeMarkers + cl_index);

                                } else {
                                    //This is used so the Image Schema coming from a previous chunk can span unto the next chunk until there is a new Image Schema.
                                    previousImageSchema.setAttribute("previous", "true");
                                }
                            }
                        } else {
                            previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countTimeMarkers + cl_index);

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
                    if (chunk_tag_i.startsWith("I")) {
                        if (previousImageSchema == null) {
                            previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countTimeMarkers + cl_index);
                            //Check if the Image Schema was coming from a previous chunk
                        } else if (previousImageSchema.getAttribute("previous") == "true" && !imageSchemas.isEmpty()) {

                            previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl_index) + "-0.2");
                            imageSchemasGenerated.add(previousImageSchema);
                            previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countTimeMarkers + cl_index);

                        }
                        if (!imageSchemas.isEmpty()) {
                            switch (chunktag[i]) {
                                case "I-NP": {
                                    if (previousImageSchema.getAttribute("POSroot") == "") {
                                        setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl_index);
                                    } else {
                                        if (previousImageSchema.getAttribute("POSroot").equals(POS.NOUN.toString())) {
                                            setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl_index);

                                        }
                                        if (pos.equals(POS.ADJECTIVE) || isWithinPitchAccent(listPitchAccent, cl_index) || pos.equals(POS.NOUN) && !previousImageSchema.getAttribute("POSroot").equals(POS.ADJECTIVE.toString())) {
                                            if (previousImageSchema.getAttribute("type") == "") {
                                                fmlRoot.removeChild(previousImageSchema);
                                            } else {
                                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl_index + 1) + "-0.2");
                                                imageSchemasGenerated.add(previousImageSchema);
                                            }
                                            XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countTimeMarkers + cl_index);
                                            setImageSchemaType(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl_index);
                                            previousImageSchema = imageschema;
                                        }
                                    }
                                    break;
                                }

                                case "I-VP": {
                                    if (previousImageSchema.getAttribute("POSroot") == "") {
                                        setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl_index);
                                    } else {
                                        if (previousImageSchema.getAttribute("POSroot").equals(POS.VERB.toString())) {
                                            setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl_index);
                                        }
                                        if (pos.equals(POS.ADVERB) || isWithinPitchAccent(listPitchAccent, cl_index) || pos.equals(POS.VERB) && !previousImageSchema.getAttribute("POSroot").equals(POS.ADVERB.toString())) {
                                            if (previousImageSchema.getAttribute("type") == "") {
                                                fmlRoot.removeChild(previousImageSchema);
                                            } else {
                                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl_index + 1) + "-0.2");
                                                imageSchemasGenerated.add(previousImageSchema);
                                            }
                                            XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countTimeMarkers + cl_index);
                                            setImageSchemaType(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl_index);
                                            previousImageSchema = imageschema;
                                        }
                                    }
                                    break;
                                }

                                default: { //I-PRT, I-INTJ, I-SBAR, I-ADJP, I-ADVP, I-PP
                                    if (previousImageSchema.getAttribute("POSroot") == "") {
                                        setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl_index);

                                    } else {
                                        if (previousImageSchema.getAttribute("type") == "") {
                                            fmlRoot.removeChild(previousImageSchema);
                                        } else {
                                            previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl_index + 1) + "-0.2");
                                            imageSchemasGenerated.add(previousImageSchema);
                                        }
                                        XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countTimeMarkers + cl_index);
                                        setImageSchemaType(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl_index);
                                        previousImageSchema = imageschema;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    if (chunk_tag_i.startsWith("O")) {
                        if (previousImageSchema != null) {
                            if (previousImageSchema.getAttribute("type") == "") {
                                fmlRoot.removeChild(previousImageSchema);
                            } else {
                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl_index + 1) + "-0.2");
                                imageSchemasGenerated.add(previousImageSchema);
                            }

                        }
                        previousImageSchema = null;
                    }

                }

                negation = posTag.equalsIgnoreCase("DT") && value.equalsIgnoreCase("no") || posTag.equalsIgnoreCase("RB") && value.equalsIgnoreCase("not");
                //System.out.println(negation);

            }

            //countSentenceMarkers += sentence.size();
            countSentenceMarkers++;

            if (previousImageSchema != null) {
                if (previousImageSchema.getAttribute("type").equals("")) {
                    fmlRoot.removeChild(previousImageSchema);
                } else {
                    previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + 1) + "-0.2");
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

        System.out.println("CHECK TEXT");
        printXML(fmlApmlRoot.toString());

        //TO INTENTIONS
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fmlApmlRoot, charactermanager);

        for (IntentionPerformer ip : intentionsPerformers) {
            ip.performIntentions(intentions, IDProvider.createID("MeaningMiner"), new Mode(CompositionType.blend));
        }
        return intentions;
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
        intentionsPerformers.add(ip);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer ip) {
        intentionsPerformers.remove(ip);
    }

    @Override
    public void onCharacterChanged() {

    }

    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        if(charactermanager==null)
            charactermanager = CharacterManager.getStaticInstance();
        return charactermanager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        if(this.charactermanager!=null)
            this.charactermanager.remove(this);
        this.charactermanager = characterManager;
        //characterManager.add(this);
    }
    
    public void printXML(String fmlApmlRoot){
        
        try{
            String file= new String();
            BufferedReader reader = new BufferedReader(new StringReader(fmlApmlRoot.toString()));

            String line;

            int counter=0;
            int counter1=1;
            while ((line = reader.readLine()) != null) {
                line=line.trim();
                if(line.length()==3 || line.contains("importance") || line.contains("imageschema") || line.contains("speech")){
                    file=file+(line+" "+reader.readLine().trim())+"\n";
                    //System.out.println("LINE:"+line+" "+reader.readLine().trim());
                }else{
                    if(line.trim().contains("<fml-apml>")){
                        if(counter==0){
                            file=file+line+"\n";
                            counter=counter+1;
                        }
                    }
                    else{
                     if(!line.trim().contains("</fml-apml>") && !line.contains("<fml/>"))
                        file=file+line+"\n";

                    }
                }
            }

            System.out.println("STRING:\n"+file );

            String fmlApmlRoot_v1=file+"</fml-apml>".replace("s1:tm0","s1:tm1");
            //fmlApmlRoot_v1= fmlApmlRoot_v1.replace("</bml>","\n</bml>\n<fml>").replace("?>", "?>\n<fml-apml>").replace(":tm0",":tm1")+"\n</fml-apml>";
            //System.out.println("CHECK STRING 2");
            //System.out.println(fmlApmlRoot_v1);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(fmlApmlRoot_v1)));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            FileWriter writer = new FileWriter(new File(System.getProperty("user.dir")+"\\fml_output_mm.xml"));
            StreamResult result = new StreamResult(writer);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
