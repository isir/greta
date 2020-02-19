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
