/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.MeaningMiner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import vib.core.util.xml.DefaultXMLParser;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLTree;

/**
 *
 * @author Brian
 */
public class DictionnarySynsetImageSchema {

    private String MODELPATH = "./Common/Data/MeaningMiner/";
    private String MODELNAME = "imageschemaSynset";
    private HashMap<String, Set<String>> verbs;
    private HashMap<String, Set<String>> nouns;
    private HashMap<String, Set<String>> adjectives;
    private HashMap<String, Set<String>> adverbs;

    public DictionnarySynsetImageSchema() {
        verbs = new HashMap<>();
        nouns = new HashMap<>();
        adjectives = new HashMap<>();
        adverbs = new HashMap<>();
        DefaultXMLParser parser = new DefaultXMLParser();
        XMLTree tree = parser.parseFile(MODELPATH + MODELNAME + ".xml");
        for (XMLTree imageschema : tree.getChildrenElement()) {
            String imScName = imageschema.getAttribute("type");
            for (XMLTree synset : imageschema.getChildrenElement()) {

                switch (synset.getAttribute("type")) {
                    case "VERB":
                        insertImageSchemasForVerb(imScName, synset.getAttribute("id"));
                        break;
                    case "NOUN":
                        insertImageSchemasForNoun(imScName, synset.getAttribute("id"));
                        break;
                    case "ADJ":
                         insertImageSchemasForAdjective(imScName, synset.getAttribute("id"));
                        break;
                    case "ADV":
                         insertImageSchemasForAdverb(imScName, synset.getAttribute("id"));
                        break;
                    default:
                        break;
                }
            }

        }
    }

    public Set<String> getImageSchemasForVerb(String synsetID) {
        return verbs.get(synsetID);
    }

    public Set<String> getImageSchemasForNoun(String synsetID) {
        return nouns.get(synsetID);
    }

    public Set<String> getImageSchemasForAdjective(String synsetID) {
        return adjectives.get(synsetID);
    }

    public Set<String> getImageSchemasForAdverb(String synsetID) {
        return adverbs.get(synsetID);
    }

    private void insertImageSchemasForVerb(String imageSchema, String synsetID) {
        if (verbs.containsKey(synsetID)) {
            verbs.get(synsetID).add(imageSchema);
        } else {
            HashSet<String> hashSetImageSchema = new HashSet<>();
            hashSetImageSchema.add(imageSchema);
            verbs.put(synsetID, hashSetImageSchema);
        }
    }

    private void insertImageSchemasForNoun(String imageSchema, String synsetID) {
        if (nouns.containsKey(synsetID)) {
            nouns.get(synsetID).add(imageSchema);
        } else {
            HashSet<String> hashSetImageSchema = new HashSet<>();
            hashSetImageSchema.add(imageSchema);
            nouns.put(synsetID, hashSetImageSchema);
        }
    }

    private void insertImageSchemasForAdjective(String imageSchema, String synsetID) {
        if (adjectives.containsKey(synsetID)) {
            adjectives.get(synsetID).add(imageSchema);
        } else {
            HashSet<String> hashSetImageSchema = new HashSet<>();
            hashSetImageSchema.add(imageSchema);
            adjectives.put(synsetID, hashSetImageSchema);
        }
    }

    private void insertImageSchemasForAdverb(String imageSchema, String synsetID) {
        if (adverbs.containsKey(synsetID)) {
            adverbs.get(synsetID).add(imageSchema);
        } else {
            HashSet<String> hashSetImageSchema = new HashSet<>();
            hashSetImageSchema.add(imageSchema);
            adverbs.put(synsetID, hashSetImageSchema);
        }
    }

}
