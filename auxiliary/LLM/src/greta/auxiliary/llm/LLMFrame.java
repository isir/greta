/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.llm;

import greta.auxiliary.MeaningMiner.ImageSchemaExtractor;
import greta.auxiliary.MeaningMiner.shutdownHook;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.signals.BMLTranslator;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import java.nio.charset.StandardCharsets;
import static greta.core.util.audio.Audio.ANSI_RESET;
import static greta.core.util.audio.Audio.ANSI_YELLOW;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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

import greta.core.util.enums.CompositionType;

/**
 *
 * @author miche
 */
public class LLMFrame extends javax.swing.JFrame implements IntentionEmitter{

    /**
     * Creates new form LLMFrame
     */
    
    private Server server;
    public Socket soc;
    public String answ;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public Boolean IsStreaming = Boolean.FALSE;
    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    private ArrayList<SignalPerformer> signal_performers = new ArrayList<SignalPerformer>();
    private XMLParser fmlparser = XML.createParser();
    private XMLParser bmlparser = XML.createParser();
    private static String markup = "fml-apml";

    private boolean MM_parse_server_activated = false;
    private String MM_python_env_checker_path = "Common\\Data\\MeaningMiner\\python\\check_env.py";
    private String MM_python_env_installer_path = "Common\\Data\\MeaningMiner\\python\\init_env.bat";
    private String MM_parse_server_path         = "Common\\Data\\MeaningMiner\\python\\activate_server.bat";
    private String MM_parse_server_killer_path  = "Common\\Data\\MeaningMiner\\python\\kill_server.bat";
    private String LLM_python_env_checker_path = "Common\\Data\\LLM\\Mistral\\check_env.py";
    private String LLM_python_env_installer_path = "Common\\Data\\LLM\\Mistral\\init_env.bat";
    private String python_path_llm="\\Common\\Data\\LLM\\Mistral\\Mistral.py ";
    private Process server_process;
    private Thread server_shutdownHook;
    private Process server_process_mistral;
    private Thread server_shutdownHook_mistral;

    public String getAnswer() {
        return answ;
    }

    public void setAnswer(String answer) {
        this.answ = answer;
    }
    
    
    
    
    public CharacterManager cm;
    
    public LLMFrame(CharacterManager cm) throws InterruptedException {
       
        server = new Server();
        this.cm=cm;
        
        
   
        
    }
    public String TextToFML(String text) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException{
        String fml_name = TextToFML(text, true);
        return fml_name;
    }
    
    public String TextToFML(String text, boolean renew) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException{
        
        String file_path = "";        
        
        System.out.println("TEXT TO TRANSFORM:"+text); 
        if(text.length()>1){
            String construction="<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                                "<fml-apml>\n<bml>"+
                                "\n<speech id=\"s1\" language=\"english\" start=\"0.0\" text=\"\" type=\"SAPI4\" voice=\"marytts\" xmlns=\"\">"+
                                "\n<description level=\"1\" type=\"gretabml\"><reference>tmp/from-fml-apml.pho</reference></description>";
            System.out.println("greta.core.intentions.FMLFileReader.TextToFML()");
            String[] sp=text.split(" ");
            int i=1;
            System.out.println("greta.auxiliary.llm.LLMFrame.TextToFML() "+sp.length);
            for(int j=0;j<sp.length;j++){
                construction=construction+"\n<tm id=\"tm"+i+"\"/>"+sp[j];
                i++;
            }
            construction=construction+"\n<tm id=\"tm"+i+"\"/>";

            construction=construction+"\n<boundary id=\"b1\" type=\"LL\" start=\"s1:tm1\" end=\"s1:tm"+i+"\"/>";

            construction=construction+"\n</speech>\n</bml>\n<fml>\n";
            construction=construction+ "</fml>\n</fml-apml>";
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(construction)));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);

            if(renew){
                file_path = System.getProperty("user.dir")+"\\fml_mistral_renew.xml";
            }else{
                file_path = System.getProperty("user.dir")+"\\fml_mistral.xml";
            }
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(file_path)),"ISO-8859-1");                        
            StreamResult result = new StreamResult(writer);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        }
        
        return file_path;
        
    }
    
    public String FMLToBML(String filename) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException{
        
        System.out.println("greta.core.intentions.FMLFileReader.FMLToBML()");
        BufferedReader br = null;
        PrintWriter pw = null; 
        try {
            br = new BufferedReader(new FileReader(filename));
            pw =  new PrintWriter(new FileWriter(System.getProperty("user.dir")+"\\fml_to_bml.xml"));
            String line;
            while ((line = br.readLine()) != null) {

                   if(!line.contains("fml") && !line.contains("<?xml") && !line.contains("<rest") && !line.contains("<certainty") && !line.contains("<emotion")&& !line.contains("<performative") && !line.contains("<iconic") && !line.contains("<deictic") && !line.contains("<beat")){
                       if(line.contains("<speech")){
                            pw.println(line);
                           pw.println("<description level=\"1\" type=\"gretabml\"><reference>tmp/from-fml-apml.pho</reference></description>");
                       }else{
                       pw.println(line);
                       }
                   }
            }
            br.close();
            pw.close();
        }catch (Exception e) {
             e.printStackTrace();
        }
        return System.getProperty("user.dir")+"\\fml_to_bml.xml";
    }

    public ID load(String fmlFileName) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {
        
        ID id = load(fmlFileName, CompositionType.replace);
        return id;
    }
    
    public ID load(String fmlFileName, CompositionType compositionType) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {
   
        System.out.println("CompositionType: " + compositionType);
        
        String base = (new File(fmlFileName)).getName().replaceAll("\\.xml$", "");
        String bml_file=FMLToBML(fmlFileName);
        String base_bml= (new File(bml_file)).getName().replaceAll("\\.xml$", "");

        String fml_id = "";
        boolean text_brut=false;
        //get the intentions of the FML file
        fmlparser.setValidating(true);
        bmlparser.setValidating(true);
        BufferedReader reader;
        String text="";
        
        
        
        boolean flag=false;
        try {
            File myObj = new File(fmlFileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                //System.out.println(data);
                if(!data.contains("<?xml")){
                    System.out.println(data);
                    if(!flag)
                        text_brut=true;
                    text+=data;

                }
                else{
                    flag=true;
                    text+=data;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
                        
			
                
	if(text_brut){
            System.out.println(text);
            fmlFileName=TextToFML(text);
            bml_file=FMLToBML(fmlFileName);
            System.out.println("Nome nuovo file "+fmlFileName);
       }
        
        XMLTree fml = fmlparser.parseFile(fmlFileName);
        if(!text_brut){
             bml_file=FMLToBML(fmlFileName);
             System.out.println("Nome nuovo file "+bml_file);
        }
        XMLTree bml = bmlparser.parseFile(bml_file);
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml,cm);
        List<Signal> signals = BMLTranslator.BMLToSignals(bml,cm);
        System.out.println("greta.core.intentions.FMLFileReader.load()");
        for (int i =0;i>signals.size();i++){
                   System.out.println("greta.core.intentions.FMLFileReader.load()"+signals.get(i).toString());
        }
        Mode mode = FMLTranslator.getDefaultFMLMode();
        Mode mode_bml = BMLTranslator.getDefaultBMLMode();
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
        
//        if (fml.hasAttribute("composition")) {
//            mode.setCompositionType(fml.getAttribute("composition"));
//        }
        mode.setCompositionType(compositionType);
        
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
        id.setFmlID(fml_id);
        
        if(this.cm.use_MM()){
            ImageSchemaExtractor im = new ImageSchemaExtractor(this.cm);
             //MEANING MINER TREATMENT START
            List<Intention> intention_list;
            System.out.println("File Name "+fml.toString());
            intention_list = im.processText_2(fml.toString());
            intentions.addAll(intention_list);
            //MEANING MINER TREATMENT END
        }
        
        for(int i=0; i<intentions.size();i++){
            System.out.println("[INFO]: Intention_type:"+intentions.get(i).getType()+"   "+intentions.get(i).getName());
            if(this.cm.getGesture_map().containsKey(intentions.get(i).getType())){
                this.cm.setTouch_computed(true);
                this.cm.setTouch_gesture_computed(this.cm.getGesture_map().get(intentions.get(i).getType()));
                //REMOVE TOUCH GESTURE AND DO IT AFTER (IF NEAR OTHER CHARACTER/HUMAN
                intentions.remove(i);
            }
        }
        
       
        //send to all SignalPerformer added
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, mode);
        }
        for (SignalPerformer performer : signal_performers) {
            performer.performSignals(signals, id, mode);
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

    
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
   
    public void setRequestTextandSend(String content){
        
    }
   
   

    private void portActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_portActionPerformed

    /**
     * @param args the command line arguments
     */



    public void init_MeaningMiner_server(String this_file_path) throws InterruptedException
    {
        System.out.println(this_file_path + ".init_MeaningMiner_server(): MeaningMiner, checking python environment...");
        try{
            server_process = new ProcessBuilder("python", MM_python_env_checker_path).redirectErrorStream(true).start();
        } catch (IOException ex2){
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex2);
        }
        server_process.waitFor();

        InputStream inputStream = server_process.getInputStream();
        String result = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n")
                );
        System.out.println(this_file_path + ".init_MeaningMiner_server(): MeaningMiner, python env exist: " + result);
        
        if(result.equals("0")){
            System.out.println(this_file_path + ".init_MeaningMiner_server(): MeaningMiner, installing python environment...");
            try{
                server_process = new ProcessBuilder(MM_python_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            } catch (IOException ex2){
                Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex2);
            }
            server_process.waitFor();
        }        

        System.out.println(this_file_path + ".init_MeaningMiner_server(): initializing MeaningMiner python env");
        try {
            server_process = new ProcessBuilder(MM_parse_server_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            //client_process = new ProcessBuilder("python", "-c", "print('hello')").redirectErrorStream(true).start();
            server_shutdownHook = new shutdownHook(server_process, MM_parse_server_killer_path);
            Runtime.getRuntime().addShutdownHook(server_shutdownHook);
        } catch (IOException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(this_file_path + ".init_MeaningMiner_server(): MeaningMiner python env initialization signal sent");
        
    }
}
