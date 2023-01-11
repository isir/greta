/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.chatgpt;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.transport.udp.OSCPortOut;
import greta.auxiliary.MeaningMiner.ImageSchemaExtractor;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.signals.SignalPerformer;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
 *
 * @author Michele
 */
public class ChatGPT implements IntentionEmitter{
    
    protected ChatGPTFrame loader;
    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    private ArrayList<SignalPerformer> signal_performers = new ArrayList<SignalPerformer>();
    private XMLParser fmlparser = XML.createParser();
    private XMLParser bmlparser = XML.createParser();
    private static String markup = "fml-apml";
    
    public CharacterManager cm;
    
    public String TextToFML(String text) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException{
        String construction="<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                            "<fml-apml>\n<bml>"+
                            "\n<speech id=\"s1\" language=\"english\" start=\"0.0\" text=\"\" type=\"SAPI4\" voice=\"marytts\" xmlns=\"\">"+
                            "\n<description level=\"1\" type=\"gretabml\"><reference>tmp/from-fml-apml.pho</reference></description>";
        System.out.println(text.replaceAll("  ", " "));
        System.out.println("greta.core.intentions.FMLFileReader.TextToFML()");
        String[] sp=text.split(" ");
        int i=1;
        for(int j=0;j<sp.length;j++){
            construction=construction+"\n<tm id=\"tm"+i+"\"/>"+sp[j];
                        i++;
        }
        i=i-1;
        construction=construction+"\n</speech>\n</bml>\n<fml>\n";
        construction=construction+ "</fml>\n</fml-apml>";
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse(new InputSource(new StringReader(construction)));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        FileWriter writer = new FileWriter(new File(System.getProperty("user.dir")+"\\fml_chatgpt.xml"));
        StreamResult result = new StreamResult(writer);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
        return System.getProperty("user.dir")+"\\fml_chatgpt.xml";
    }
    
    
    public ID load(String fmlFileName) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {

        //get the intentions of the FML file
        fmlparser.setValidating(true);
        bmlparser.setValidating(true);
        String fml_id = "";
        BufferedReader reader;
        String text="";
        fmlFileName=TextToFML(text);
        System.out.println("Name new fml file "+fmlFileName);
       
        
        XMLTree fml = fmlparser.parseFile(fmlFileName);
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml,cm);
        System.out.println("greta.core.intentions.FMLFileReader.load()");
        Mode mode = FMLTranslator.getDefaultFMLMode();
        for (XMLTree fmlchild : fml.getChildrenElement()) {
            // store the bml id in the mode class in order
            if (fmlchild.isNamed("bml")) {
                //System.out.println(fmlchild.getName());
                if(fmlchild.hasAttribute("id")){
                    mode.setBml_id(fmlchild.getAttribute("id"));
                }
            }
        }
        if(fml.hasAttribute("id")){
            fml_id = fml.getAttribute("id");
        }else{
            fml_id = "fml_1";
        }
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

        ID id = IDProvider.createID(fmlFileName);
        id.setFmlID(fml_id);
        if(this.cm.use_MM()){
        ImageSchemaExtractor im = new ImageSchemaExtractor(this.cm);
         //MEANING MINER TREATMENT START
        List<Intention> intention_list;
        //System.out.println("File Name "+fml.toString());
        intention_list = im.processText_2(fml.toString());
        intentions.addAll(intention_list);
        //MEANING MINER TREATMENT END
        }
        
        
       
        //send to all SignalPerformer added
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, mode);
        }
        return id;
    }
    
         @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        performers.add(performer);
    }
    
        @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }
    
        public ChatGPT(CharacterManager cm){
            this.cm=cm;
            java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ChatGPTFrame(cm).setVisible(true);
 
    }
            });
}
}
    

