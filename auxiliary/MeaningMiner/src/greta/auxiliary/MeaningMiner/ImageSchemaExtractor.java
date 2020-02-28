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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

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
     * @param input the input string to be processed
     */
    @Override
    public void processText(String input) {
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
        //Get rid of the xml tags for text processing
        String tagFreeInput = inputXML.toString().replaceAll("<[^>]+>", "");
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
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fmlApmlRoot, charactermanager);

        for (IntentionPerformer ip : intentionsPerformers) {
            ip.performIntentions(intentions, IDProvider.createID("MeaningMiner"), new Mode(CompositionType.blend));
        }

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
}
