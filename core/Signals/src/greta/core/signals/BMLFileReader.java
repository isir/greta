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
package greta.core.signals;

import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.speech.Speech;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class is an implementation of {@code SignalEmitter} interface.<br/> When
 * calling the {@code load} function, It sends the {@code Signals} contained in
 * a specified BML file to all {@code SignalPerformers} added with the
 * {@code addSignalPerformer} function.
 *
 * @author Andre-Marie Pez
 */
public class BMLFileReader implements SignalEmitter {

    private ArrayList<SignalPerformer> signalPerformers = new ArrayList<SignalPerformer>();
    private XMLParser bmlparser = XML.createParser();
    private CharacterManager cm;

    public BMLFileReader(CharacterManager cm){
        this.cm = cm;
    }

    @Override
    public void addSignalPerformer(SignalPerformer performer) {
        if (performer != null) {
            signalPerformers.add(performer);
        }
    }

    @Override
    public void removeSignalPerformer(SignalPerformer performer) {
        signalPerformers.remove(performer);
    }

    /**
     * Loads a BML file.<br/> The behavior signals in the specified file will be
     * send to all {@code SignalPerformer} added with the
     * {@link #addSignalPerformer(greta.core.signals.SignalPerformer) addSignalPerformer} function.<br/> The
     * base file name of the BML file is used as {@code requestId} parameter
     * when calling the
     * {@link greta.core.signals.SignalPerformer#performSignals(java.util.List, greta.core.util.id.ID, greta.core.util.Mode) performSignals}
     * function.
     *
     * @param bmlFileName the name of the file to load
     * @return The ID of the generated event
     */
    public ID load(String bmlFileName) throws JMSException, FileNotFoundException, TransformerException, ParserConfigurationException, SAXException, IOException, InterruptedException {
        //get the base file name to use it as requestId
        String base = (new File(bmlFileName)).getName().replaceAll("\\.xml$", "");
        Logs.debug(base);
        //get the signals of the BML file
        bmlparser.setValidating(true);
        XMLTree bml = bmlparser.parseFile(bmlFileName);
        Mode mode = BMLTranslator.getDefaultBMLMode();
        //System.out.println(bml);
        List<Signal> signals=new ArrayList<Signal>();
        XMLTree bml_mod = null;
        if(this.cm.use_NVBG){
        //Start traitement NVBG
        String input1=bml.toString();
        
	Scanner myReader = new Scanner(input1);
        String phrase="";
        String bml_gaze="";
        boolean target=true;
	while (myReader.hasNextLine()) {
	String data = myReader.nextLine();
	data=data.trim();
        if(data.contains("<gaze ") || data.contains("<gazeShift ")){
             bml_gaze+=data;
             if(!data.contains("gaze_target")&&!data.contains("target")){
                 target=false;
             }
        }
	if (!data.endsWith(">")) {
		phrase+=data.replaceAll("<.*?>","")+" ";
                Logs.debug(phrase);
           }
	}

        List<String> gestures=null;
        signals=new ArrayList<Signal>();
	myReader.close();
        phrase=phrase.substring(0, phrase.length()-1);
                    XMLParser bmlparser = XML.createParser();
                    MessageSender msg_send = new MessageSender();
                    Logs.debug("INFO: "+phrase);
                    phrase=phrase.replaceAll("  ", " ");
                    if(phrase.startsWith(" ")){
                        phrase=phrase.substring(1);
                    }
                    String construction="<bml>"+
                            "\n<speech id=\"s1\" language=\"english\" start=\"0.0\" text=\"\" type=\"SAPI4\" voice=\"marytts\" xmlns=\"\">"+
                            "\n<description level=\"1\" type=\"gretabml\"><reference>tmp/from-fml-apml.pho</reference></description>";
                    Logs.debug(phrase.replaceAll("  ", " ").substring(1));
                    String[] sp=phrase.split(" ");
                    int i=0;
                    for(int j=0;j<sp.length;j++){
                        construction=construction+"\n<tm id=\""+i+"\"/>"+sp[j];
                        i++;
                    }
                    //this.getCharacterManager().getEn
                    Logs.debug("[BML GAZE] "+bml_gaze);
                    if(target==false){
                        bml_gaze=bml_gaze.replace("/>", " target=\"gaze_target\"/>");
                    }
                    construction=construction+"\n</speech>\n"+bml_gaze+"\n</bml>";
                    Logs.debug("[BML GAZE] "+bml_gaze);
                    System.out.println("USE NVBG:"+this.cm.use_NVBG);
                    if(this.cm.use_NVBG){
                    gestures = msg_send.traitement_NVBG(phrase,this.cm.getEnvironment().getNVBG_Open());
                    this.cm.getEnvironment().setNVBG_Open(true);
                    Logs.debug("Out " + gestures);
                    if(gestures!=null){
                    for(String y : gestures){
                    //System.out.println("what happens"+bml);
                    String[] k=y.split("importance");
                    y=k[0];
                    Logs.debug("what happens"+y);
                    String bml_modif=construction.toString();
                    String[] g=y.split("lexeme=");
                    String b= g[1].substring(1,g[1].indexOf(" ")-1);
                    String[] c=y.replace("<","").split(" ");
                    Logs.debug("TYPE:"+c[0]);
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
                    bml_modif=construction.replaceAll("</bml>",y.replace(c[0],"gesture")+">\n"+addend+"\n</gesture>\n</bml>");
                    Logs.debug(bml_modif);
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
                    bml_mod = bmlparser.parseFile(System.getProperty("user.dir")+"\\test_fml.xml");
                    
                    }
             }
        // Fin traitment NVBG
        
        
        if (bml.hasAttribute("composition")) {
            mode.setCompositionType(bml.getAttribute("composition"));
        }
        if (bml.hasAttribute("reaction_type")) {
            mode.setReactionType(bml.getAttribute("reaction_type"));
        }
        if (bml.hasAttribute("reaction_duration")) {
            mode.setReactionDuration(bml.getAttribute("reaction_duration"));
        }
        if (bml.hasAttribute("social_attitude")) {
            mode.setSocialAttitude(bml.getAttribute("social_attitude"));
        }
        
        
         
        }
            
            signals.addAll(BMLTranslator.BMLToSignals(bml_mod, this.cm));
       }
        
        signals.addAll(BMLTranslator.BMLToSignals(bml, this.cm));
        

        ID id = IDProvider.createID(base);
        //send to all SignalPerformer added
        for (SignalPerformer performer : signalPerformers) {
            performer.performSignals(signals, id, mode);
        }
        
        return id;
    }
    

    /**
     * Returns a {@code java.io.FileFilter} corresponding to BML Files.
     *
     * @return a {@code java.io.FileFilter} corresponding to BML Files
     */
    public java.io.FileFilter getFileFilter() {
        return new java.io.FileFilter() {
            @Override
            public boolean accept(File pathName) {
                String fileName = pathName.getName().toLowerCase();
                if (fileName.endsWith(".xml") || fileName.endsWith(".bml")) {
                    try {
                        bmlparser.setValidating(false);
                        return bmlparser.parseFile(pathName.getAbsolutePath()).getName().equalsIgnoreCase("bml");
                    } catch (Exception e) {
                    }
                }
                return false;
            }
        };
    }
}
