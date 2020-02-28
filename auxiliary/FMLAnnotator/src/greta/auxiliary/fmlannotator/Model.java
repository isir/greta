/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.fmlannotator;

import greta.auxiliary.fmlannotator.util.Counter;
import greta.auxiliary.fmlannotator.util.CounterFactory;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Marie-Laure Guénot
 * @author David Panou
 */
public class Model implements IntentionEmitter {

    public static enum Language {
        en_UK,
        fr_FR;
    };

    // Configuration Directories
    public static final String FMLANNOTATOR_BASEDIR = "./Common/Data/FMLAnnotator/";
    public static final String CONFIG_FILES_DIR = FMLANNOTATOR_BASEDIR + "./Configs/";
    public static final String OUTPUT_DIR = FMLANNOTATOR_BASEDIR + "./Output/";

    // Configuration Files
    public static final String CONFIG_FILE_CONJUNCTIONS_FILENAME = "conjunctions";
    public static final String CONFIG_FILE_PREPOSITIONS_FILENAME = "prepositions";
    public static final String CONFIG_FILE_LEXICON_DA_FILENAME = "lexicon-dialogueacts";
    public static final String CONFIG_FILE_LEXICON_DA_COMPLEX_FILENAME = "lexicon-dialogueacts-complex";
    public static final String CONFIG_FILE_LEXICON_WORLD_FILENAME = "lexicon-world";
    public static final String CONFIG_FILE_EXTENSION = ".txt";

    // Output File
    public static final String OUTPUT_FILE = "output.xml";

    private Document doc;
    private HashMap<String, Counter> Compteurs;
    private boolean SHOW;
    private Element speechElement;
    private Element descriptionElement;
    private boolean tokPrecEstUnMot = false;
    private boolean tokEstUnMot = false;
    private Element rootElement;
    private Element bmlElement;
    private Element referenceElement;
    private HashMap<String, Integer> dicoMot;
    private HashMap<Integer, String> idLexMap;
    private int idLex;
    private Element pitchAccentElement;
    private Element fmlElement;
    private String output_dir;

    private Language modelLanguage;

    // MEHODE DE L'INTERFACE
    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    private XMLParser fmlparser = XML.createParser();
    private String markup = "fml-apml";

    @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        performers.add(performer);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }

    /**
     * Loads an FML file.<br> The communicative intentions in the specified file
     * will be send to all {@code IntentionPerformer} added with the
     * {@link #addIntentionPerformer(greta.core.intentions.IntentionPerformer) add}
     * function.<br> The base file name of the FML file is used as
     * {@code requestId} parameter when calling the
     * {@link greta.core.intentions.IntentionPerformer#performIntentions(java.util.List, greta.core.util.id.ID, greta.core.util.Mode) performIntentions}
     * function.
     *
     * @return The ID of the generated event
     */

    // String NL désigne le texte en langage naturel
    public ID load(String textNL) {

        try {
            String fml = Treat(textNL);
        } catch (IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        //get the base file name to use it as requestId
        String base = (new File(output_dir)).getName().replaceAll("\\.fml$", "");
        //get the intentions of the FML file
        fmlparser.setValidating(true);
        XMLTree fml = fmlparser.parseFile(output_dir);
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml, CharacterManager.getStaticInstance());
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
        ID id = IDProvider.createID(base);
        //send to all SignalPerformer added
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, mode);
        }
        return id;
    }

    /**
     * Returns a {@code java.io.FileFilter} corresponding to FML Files.
     *
     * @return a {@code java.io.FileFilter} corresponding to FML Files
     */
    public java.io.FileFilter getFileFilter() {
        return new java.io.FileFilter() {
            @Override
            public boolean accept(File pathName) {
                String fileName = pathName.getName().toLowerCase();
                if (fileName.endsWith(".xml") || fileName.endsWith(".fml")) {
                    try {
                        fmlparser.setValidating(false);
                        return fmlparser.parseFile(pathName.getAbsolutePath()).getName().equalsIgnoreCase(markup);
                    } catch (Exception e) {
                        // TODO
                        e.printStackTrace();
                    }
                }
                return false;
            }
        };
    }

    public Model(Language aModelLanguage) {
        this.modelLanguage = aModelLanguage;
        initDocument();
    }

    public void initLex() {
        idLexMap = new HashMap<Integer, String>();
        Integer[] lexTemp = {1, 2, 3, 4, 5};
        for (Integer i : lexTemp) {
            idLexMap.put(i, "");
        }
    }

    public String Treat(String entryFile) throws IOException, TransformerException, ParserConfigurationException {

        //Valeurs textuelles
        //private static String tok;
        //private static String tokPrec;
        StringBuffer tok = new StringBuffer();
        String tokPrec;
        // Valeurs numériques
        int valTokPrec = 0;
        int valTok = 0;

        HashMap<Character, Integer> dicoCar = new HashMap<Character, Integer>();

        //valeurs booléenne

        // TODO : ADD BALISE BOUNDARIES
        // LISTE DE CARACTERES
        Pattern pattern;
        //pattern = Pattern.compile("[a-zA-Z0-9\\-\\/]",Pattern.CANON_EQ);
        pattern = Pattern.compile("[a-zA-Z0-9\\-\\/\\p{L}]", Pattern.CANON_EQ);
        //pattern = Pattern.compile("[^\\x00-\\x7F]+",Pattern.CANON_EQ);
        // CONSTRUCTION DU DICO DE CARACTERE

        dicoCar.put('\n', 6);
        dicoCar.put('.', 4);
        dicoCar.put(';', 4);
        dicoCar.put('?', 4);
        dicoCar.put('!', 4);
        dicoCar.put(',', 2);
        dicoCar.put(':', 2);
        dicoCar.put(')', 2);
        dicoCar.put('(', 2);
        dicoCar.put('[', 2);
        dicoCar.put(']', 2);
        dicoCar.put('\"', 2);

        // Traitement et manipulation des fichiers des lexiques

        File input_lex = new File(IniManager.getProgramPath() + CONFIG_FILES_DIR + modelLanguage.toString() + "/");

        File[] lex_files = input_lex.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equals(CONFIG_FILE_CONJUNCTIONS_FILENAME + CONFIG_FILE_EXTENSION) || name.equals(CONFIG_FILE_PREPOSITIONS_FILENAME + CONFIG_FILE_EXTENSION);
            }
        });

        // word lexique building
        initDicoMot(lex_files);

        // TODO créer un objet compteurFactory
        HashMap<String, Counter> test = new HashMap<String, Counter>();
        CounterFactory C1 = new CounterFactory(test);

        String[] compteurs = {"tm", "pause", "boundaries", "speech", "social", "turntaking", "backchannel", "performative", "functionalRelation", "emotion", "emphasis", "certainty", "other", "compteCar", "world"};
        Compteurs = C1.createCompteur(compteurs, 1);

        initLex();
        idLex = 5;

        // Text file processing
        int r;
        int temp = 0;
        StringReader sr = new StringReader(entryFile);
        while ((r = sr.read()) != -1) {
            Compteurs.get("compteCar").increase();
            char c = (char) r;
            String stringValueOf = String.valueOf(c);
            Matcher matcher = pattern.matcher(stringValueOf);
            if (tokEstUnMot) {
                if (!matcher.matches()) {

                    valTok = affectValMO(tok.toString(), dicoMot);
                    temp = valTok + valTokPrec;

                    if (temp > 0) {

                        insereTM(temp, speechElement);
                        initLex();

                        if (temp > 1) {

                            inserePause(temp, speechElement);
                            Compteurs.get("compteCar").set(0);

                        } else if (Compteurs.get("compteCar").getValue() > 15) {

                            inserePause(temp, speechElement);
                            Compteurs.get("compteCar").set(0);

                        }
                    }

                    tokPrec = tok.toString();
                    tokPrecEstUnMot = true;
                    valTokPrec = valTok;
                    tok = new StringBuffer();
                    tokEstUnMot = false;
                    //System.out.println(tokPrec.toString());
                    chercheDAsimple(tokPrec);
                    //chercheWorldSimple(tokPrec);
                    while (idLex > 0) {
                        String var;
                        if ((var = idLexMap.get(idLex)) != "") {
                            //chercheDAcomplexe(tokPrec,var);
                        }
                        idLex--;
                    }
                    //chercheDAcomplexe(tokPrec);
                    idLex = 5;
                    valTok = affecteValCar(c);
                    speechElement.appendChild(doc.createTextNode(tokPrec));
                }
            } else {
                if (matcher.matches()) {

                    tokPrec = tok.toString();
                    valTokPrec = valTok;
                    tokPrecEstUnMot = false;
                    tok = new StringBuffer();
                    tokEstUnMot = true;
                    valTok = 0;
                    speechElement.appendChild(doc.createTextNode(tokPrec));
                } else {
                    valTok = valTok + affecteValCar(c);
                }
            }
            tok.append(c);
        }
        insereTM(0, speechElement);
        String result;
        result = WriteDomElement("file");
        return result;
    }

    private void chercheDAcomplexe(String tokPrec) {
        // TODO
    }

    private void chercheDAcomplexe(String tokPrec, String var) {
        // TODO
    }

    public void initDicoMot(File[] file) {
        dicoMot = new HashMap<String, Integer>();
        for (File f : file) {
            Pattern pattern = Pattern.compile("^\\..*");
            Matcher m = pattern.matcher(f.toString());
            if (f.isFile() && !m.matches()) {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(f));
                    String line = null;

                    while ((line = br.readLine()) != null) {

                        if (!line.trim().isEmpty()) {
                            if (dicoMot.containsKey(line.trim())) {
                                Logs.warning("Adding to dictionary a word [" + line.trim() + "] from file [" + f.getName() + "] for language [" + this.modelLanguage.toString()  + "] that has already been inserted.");
                            }
                            dicoMot.put(line.trim(), 1);
                        }
                    }
                    br.close();
                } catch (FileNotFoundException e) {
                    // TODO
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            // TODO
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void initDocument() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
            //FML-APML element
            rootElement = doc.createElement("fml-apml");
            doc.appendChild(rootElement);
            //BML element
            bmlElement = doc.createElement("bml");
            rootElement.appendChild(bmlElement);

            //SPEECH element
            speechElement = doc.createElement("speech");

            bmlElement.appendChild(speechElement);
            Attr attr = doc.createAttribute("id");
            //Text text = doc.createTextNode(tok);
            attr.setValue("s1");
            speechElement.setAttributeNode(attr);

            Attr lang = doc.createAttribute("language");
            lang.setValue("english");
            speechElement.setAttributeNode(lang);

            Attr voice = doc.createAttribute("voice");
            voice.setValue("marytts");
            speechElement.setAttributeNode(voice);

            Attr type = doc.createAttribute("type");
            type.setValue("SAPI4");
            speechElement.setAttributeNode(type);

            Attr text = doc.createAttribute("text");
            text.setValue("");
            speechElement.setAttributeNode(text);

            // DESCRIPTION element
            descriptionElement = doc.createElement("description");

            speechElement.appendChild(descriptionElement);

            Attr level = doc.createAttribute("level");
            level.setValue("1");

            descriptionElement.setAttributeNode(level);

            Attr typeDesc = doc.createAttribute("type");
            typeDesc.setValue("gretabml");
            descriptionElement.setAttributeNode(typeDesc);

            //REFERENCE element
            referenceElement = doc.createElement("reference");

            descriptionElement.appendChild(referenceElement);

            referenceElement.setTextContent("tmp/from-fml-apml.pho");

            // pitchAccentElement
            //pitchAccentElement = doc.createElement("pitchaccent");
            //speechElement.appendChild(pitchAccentElement);

            //fml Element
            fmlElement = doc.createElement("fml");
            rootElement.appendChild(fmlElement);

        } catch (ParserConfigurationException e) {
            // TODO
            e.printStackTrace();
        }

    }

    private void chercheWorldSimple(String entry) throws FileNotFoundException {
        String mot;
        String line;
        mot = entry.toLowerCase();
        Pattern pattern = Pattern.compile("^" + mot + "$");

        // Load World Lexicon
        FileReader fileReader = new FileReader(IniManager.getProgramPath() + CONFIG_FILES_DIR + modelLanguage.toString() + "/" + CONFIG_FILE_LEXICON_WORLD_FILENAME + CONFIG_FILE_EXTENSION);

        BufferedReader bufferedReader = new BufferedReader(fileReader);

        try {
            while ((line = bufferedReader.readLine()) != null) {

                String[] tab;
                // On reconstruit notre tableau
                tab = line.split("\t");
                Matcher m = pattern.matcher(tab[0]);
                // Si le mot appartient à la première colonne de notre système
                if (m.matches()) {
                    //System.out.println("On a detecté le mot \"" + tab[0] + "\"");
                    String refType = tab[1];
                    String refID = tab[2];
                    String propType = tab[3];

                    insereWorld(refType, refID, propType, Compteurs.get("world"));
                }
            }
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }

    public void insereWorld(String refType, String refID, String propType, Counter count) {

        Element temp = doc.createElement("world");

        Attr attr = doc.createAttribute("id");
        attr.setValue("w" + count.toString());
        temp.setAttributeNode(attr);

        Attr attr2 = doc.createAttribute("ref_type");
        attr2.setValue(refType);
        temp.setAttributeNode(attr2);

        Attr attr3 = doc.createAttribute("ref_ID");
        attr3.setValue(refID);
        temp.setAttributeNode(attr3);

        Attr attr4 = doc.createAttribute("propType");
        attr4.setValue(propType);
        temp.setAttributeNode(attr4);

        Attr attr5 = doc.createAttribute("start");
        attr5.setValue("s1:tm" + (Compteurs.get("tm").getValue() - 1));
        temp.setAttributeNode(attr5);

        Attr attr6 = doc.createAttribute("end");
        attr6.setValue("s1:tm" + Compteurs.get("tm").getValue());
        temp.setAttributeNode(attr6);

        //System.out.println(temp.toString());

        // On lui affecte des attributs
        fmlElement.appendChild(temp);
        //StreamResult result = new StreamResult(System.out);
        count.increase();
    }

    public void chercheDAsimple(String tokPre) throws FileNotFoundException {
        String line;
        String mot = tokPre.toLowerCase();
        Pattern pattern = Pattern.compile("^" + mot + "$");

        // Load Simple Lexicon
        FileReader fileReader = new FileReader(IniManager.getProgramPath() + CONFIG_FILES_DIR + modelLanguage.toString() + "/" + CONFIG_FILE_LEXICON_DA_FILENAME + CONFIG_FILE_EXTENSION);

        BufferedReader bufferedReader = new BufferedReader(fileReader);

        try {
            while ((line = bufferedReader.readLine()) != null) {

                String[] tab;
                // On reconstruit notre tableau
                tab = line.split("\t");
                Matcher m = pattern.matcher(tab[0]);

                // Si le mot appartient à la première colonne de notre système
                if (m.matches()) {
                    //System.out.println("On a detecté le mot \"" + tab[0] + "\"");
                    int compteur = 0;
                    for (String s : tab) {
                        //System.out.println(s);
                        if (!s.equals("-")) {
                            compteur++;
                            if (compteur == 1) {
                                insereBaliseFML("social", "s", Compteurs.get("social"), s);
                            } else if (compteur == 2) {
                                insereBaliseFML("turntaking", "tt", Compteurs.get("turntaking"), s);
                            } else if (compteur == 3) {
                                insereBaliseFML("backchannel", "bk", Compteurs.get("backchannel"), s);
                            } else if (compteur == 4) {
                                insereBaliseFML("performative", "p", Compteurs.get("performative"), s);
                            } else if (compteur == 5) {

                                insereBaliseFML("functional-relation", "fr", Compteurs.get("functionalRelation"), s);
                            } else if (compteur == 6) {
                                insereBaliseFML("emotion", "emo", Compteurs.get("emotion"), s);
                            } else if (compteur == 7) {
                                insereBaliseFML("emphasis", "emp", Compteurs.get("emphasis"), s);
                            } else if (compteur == 8) {
                                insereBaliseFML("certainty", "c", Compteurs.get("certainty"), s);
                            } else if (compteur == 9) {
                                insereBaliseFML("other", "o", Compteurs.get("other"), s);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }

    public void insereBaliseFML(String dimension, String abr, Counter count, String type) {

        Element temp = doc.createElement(dimension);

        Attr attr = doc.createAttribute("id");
        attr.setValue(abr + count.toString());
        temp.setAttributeNode(attr);

        Attr attr2 = doc.createAttribute("type");
        attr2.setValue(type);
        temp.setAttributeNode(attr2);

        Attr attr3 = doc.createAttribute("start");
        attr3.setValue("s1:tm" + (Compteurs.get("tm").getValue() - 1));
        temp.setAttributeNode(attr3);

        Attr attr4 = doc.createAttribute("end");
        attr4.setValue("s1:tm" + Compteurs.get("tm").getValue());
        temp.setAttributeNode(attr4);

        //System.out.println(temp.toString());

        // On lui affecte des attributs
        fmlElement.appendChild(temp);
        //StreamResult result = new StreamResult(System.out);
        count.increase();

    }

    public int affecteValCar(Character car) {

        if (car == '.' || car == ';') {
            insereBoundary("LL");
            return 4;
        } else if (car == '!' || car == '?') {
            insereBoundary("HH");
            return 4;
        } else if (car == ',' || car == ':' || car == ')') {
            insereBoundary("LH");
            return 2;
        } else if (car == '(' || car == '[' || car == ']') {
            return 2;
        } else if (car == '"' || car == '«' || car == '»') {
            return 1;
        } else {
            return 0;
        }

    }

    public void insereBoundary(String name) {
        Element temp = doc.createElement("boundary");
        // ensemble d'attributs de l'élément

        Attr attr = doc.createAttribute("id");
        attr.setValue("b" + Compteurs.get("boundaries").toString());
        temp.setAttributeNode(attr);

        Attr attr2 = doc.createAttribute("type");
        attr2.setValue(name);
        temp.setAttributeNode(attr2);

        Attr attrStart = doc.createAttribute("start");
        attrStart.setValue("s1:tm" + (Compteurs.get("tm").getValue() - 1));
        temp.setAttributeNode(attrStart);

        Attr attrEnd = doc.createAttribute("end");
        attrEnd.setValue("s1:tm" + Compteurs.get("tm").getValue());
        temp.setAttributeNode(attrEnd);

        speechElement.appendChild(temp);
        //StreamResult result = new StreamResult(System.out);
        Compteurs.get("boundaries").increase();
    }

    public void inserePause(int valeur, Element descriptionElement) {
        Element temp = doc.createElement("pause");
        // ensemble d'attributs de l'élément

        Attr attr = doc.createAttribute("id");
        attr.setValue("p" + Compteurs.get("pause").toString());
        temp.setAttributeNode(attr);

        Attr attr2 = doc.createAttribute("duration_sym");
        String desc;
        if (valeur == 2) {
            desc = "short";
        } else if (valeur == 3) {
            desc = "medium";
        } else {
            desc = "long";
        }
        attr2.setValue(desc);
        temp.setAttributeNode(attr2);

        Attr attrRef = doc.createAttribute("ref");
        attrRef.setValue(Compteurs.get("tm").toString());
        temp.setAttributeNode(attrRef);

        // On lui affecte des attributs
        descriptionElement.appendChild(temp);
        StreamResult result = new StreamResult(System.out);
        Compteurs.get("pause").increase();
    }

    public void insereTM(int valeur, Element descriptionElement) {
        Element temp = doc.createElement("tm");
        // ensemble d'attributs de l'élément
        Attr attr = doc.createAttribute("id");
        attr.setValue("tm" + Compteurs.get("tm").toString());
        temp.setAttributeNode(attr);
        Attr attr2 = doc.createAttribute("value");
        Integer val = (Integer) valeur;
        attr2.setValue(val.toString());
        temp.setAttributeNode(attr2);
        // On lui affecte des attributs
        descriptionElement.appendChild(temp);
        StreamResult result = new StreamResult(System.out);
        Compteurs.get("tm").increase();
    }

    public String WriteDomElement(String param) throws TransformerException, ParserConfigurationException {
        // Writing the content into an xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        if (param == "print") {
            StreamResult result = new StreamResult(System.out);
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, result);
            String temp = "";
            return temp;
            //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        } else if (param == "file") {
            File out = new File(IniManager.getProgramPath() + OUTPUT_DIR + OUTPUT_FILE);
            StreamResult result = new StreamResult(out);
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, result);
            // Triche - On enregistre un fichier puis on l'affiche
            String temp = new String();
            try (BufferedReader reader = Files.newBufferedReader(out.toPath())) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    temp = temp + line + "\n";
                }
            } catch (IOException x) {
                System.err.format("IOException : %s%n", x);
            }
            System.out.print(temp);
            return temp;
        }
        return "";
    }

    public static int affectValMO(String tok, HashMap<String, Integer> dico) {
        if (dico.containsKey(tok)) {
            //System.out.println("On a trouvé le mot-outil \t" + tok);
            return dico.get(tok);
        } else {
            return 0;
        }
    }

    public boolean isSHOW() {
        return SHOW;
    }

    public void setSHOW(boolean sHOW) {
        SHOW = sHOW;
    }

}
