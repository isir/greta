/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.auxiliary.MeaningMiner;

import greta.core.util.xml.DefaultXMLParser;
import greta.core.util.xml.XMLTree;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
