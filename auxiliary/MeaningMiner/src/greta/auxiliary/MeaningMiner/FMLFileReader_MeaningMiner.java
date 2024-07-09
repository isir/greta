
package greta.auxiliary.MeaningMiner;

/**
 *
 * @author takes
 */

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
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FMLFileReader_MeaningMiner implements IntentionEmitter, SignalEmitter {

    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    private ArrayList<SignalPerformer> signal_performers = new ArrayList<SignalPerformer>();
    private XMLParser fmlparser = XML.createParser();
    private XMLParser bmlparser = XML.createParser();
    private static String markup = "fml-apml";
    private CharacterManager cm;
    
    private boolean MM_parse_server_activated = false;
    private String MM_python_env_checker_path = "Common\\Data\\MeaningMiner\\python\\check_env.py";
    private String MM_python_env_installer_path = "Common\\Data\\MeaningMiner\\python\\init_env.bat";
    private String MM_parse_server_path         = "Common\\Data\\MeaningMiner\\python\\activate_server.bat";
    private String MM_parse_server_killer_path  = "Common\\Data\\MeaningMiner\\python\\kill_server.bat";
    private Process server_process;
    private Thread server_shutdownHook;
    
    public FMLFileReader_MeaningMiner(CharacterManager cm) throws InterruptedException{
        
        this.cm = cm;
        this.cm.setTouch_computed(false);
        
        // TODO: add environment check script to run env installer at the very first time
        
        System.out.println("greta.core.intentions.FMLFileReader: MeaningMiner, checking python environment...");
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
        System.out.println("greta.core.intentions.FMLFileReader: MeaningMiner, python env exist: " + result);
        
        if(result.equals("0")){
            System.out.println("greta.core.intentions.FMLFileReader: MeaningMiner, installing python environment...");
            try{
                server_process = new ProcessBuilder(MM_python_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            } catch (IOException ex2){
                Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex2);
            }
            server_process.waitFor();
        }        

        System.out.println("greta.core.intentions.FMLFileReader: initializing MeaningMiner python env");
        try {
            server_process = new ProcessBuilder(MM_parse_server_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            //client_process = new ProcessBuilder("python", "-c", "print('hello')").redirectErrorStream(true).start();
            server_shutdownHook = new shutdownHook(server_process, MM_parse_server_killer_path);
            Runtime.getRuntime().addShutdownHook(server_shutdownHook);
        } catch (IOException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("greta.core.intentions.FMLFileReader: MeaningMiner python env initialization signal sent");
        
        /**
        InputStream inputStream = server_process.getInputStream();
        String result = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n")
                );
        System.out.println("MM SERVER OUTPUT");
        System.out.println(result);
        boolean errorFound = checkKeywords(new String[] {"Error", "ERROR", "ERR"}, result);
        if(errorFound){
            if(checkKeywords(new String[] {"Only one usage of each socket address"}, result)){
                System.out.println("greta.core.intentions.FMLFileReader: server is already running.");
            }
            else{
                System.out.println("greta.core.intentions.FMLFileReader: failed to launch MeaningMiner server.");
            }
        }
        else{
            System.out.println("greta.core.intentions.FMLFileReader: MeaningMiner server is now ready.");
        }
        * **/
        
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        performers.add(performer);
    }

    /**
     * Loads an FML file.<br> The communicative intentions in the specified
     * file will be send to all {@code IntentionPerformer} added with the
     * {@link #addIntentionPerformer(greta.core.intentions.IntentionPerformer) add}
     * function.<br> The base file name of the FML file is used as
     * {@code requestId} parameter when calling the
     * {@link greta.core.intentions.IntentionPerformer#performIntentions(java.util.List, greta.core.util.id.ID, greta.core.util.Mode) performIntentions}
     * function.
     *
     * @param fmlFileName the name of the file to load
     * @return The ID of the generated event
     */
    public ID load(String fmlFileName) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {
        //get the base file name to use it as requestId
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
        FileWriter writer = new FileWriter(new File(System.getProperty("user.dir")+"\\fml_text_brut.xml"));
        StreamResult result = new StreamResult(writer);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
        return System.getProperty("user.dir")+"\\fml_text_brut.xml";
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

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
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
                    }
                }
                return false;
            }
        };
    }

    @Override
    public void addSignalPerformer(SignalPerformer sp) {
        signal_performers.add(sp); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeSignalPerformer(SignalPerformer sp) {
        signal_performers.remove(sp); //To change body of generated methods, choose Tools | Templates.
    }

    public static boolean checkKeywords(String[] words, String str)
    {
        return (Arrays.asList(words).contains(str));
    }
    
}