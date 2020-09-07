/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.FlipperDemo.dm.managers;



import greta.FlipperDemo.dm.managers.FMLManager;

import greta.FlipperDemo.dm.managers.SimpleManager;
import greta.FlipperDemo.dm.managers.Say;
import org.slf4j.LoggerFactory;

import javax.json.*;
import javax.json.stream.JsonParser;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by WaterschootJB on 6-6-2017.
 */
public class FMLGenerator {

    FMLManager manager;
    private String agentName = "Agent";
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(FMLGenerator.class.getName());

    public FMLGenerator(FMLManager fm) {
        System.out.println("FMLGenerator started");
        this.manager = fm;
    }

    public FMLGenerator() {
        System.out.println("FMLGenerator started");
        // this.manager = new FMLManager();
    }
    /**
     * Creates the strategy history
     * @return filename
     */
     /*
    public String createFile(){
        String header = "STRATEGY;RANDOM;Q_INGRATIATION;Q_SUPPLICATION;Q_SELF_PROMOTION;Q_INTIMIDATION\n";
        Charset charset = Charset.forName("UTF-8");
        byte data[] = header.getBytes(charset);
        DateTimeFormatter formatToday = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        LocalDateTime today = LocalDateTime.now();
        logfile = "./log/" + today.format(formatToday) + "-agent.csv";
        Path file = Paths.get(logfile);
        File f = new File("./log");
        f.mkdirs();
        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(file, CREATE, APPEND))) {
            out.write(data, 0, data.length);
        } catch (IOException e) {
            logger.error("IOException: %s%n",e);
        }
        return logfile;
    }*/

    /**
     * Takes the FML json string as input and executes the behaviour with the parameters
     * @param json, json containing the template name and required and optional parameters
     */
    public void executeTemplate(String json) {

        Map<String,String> fml = new HashMap<>();
        JsonReader jr = Json.createReader(new StringReader(json));
        JsonObject jo = jr.readObject();
        jr.close();
        fml.put("template",jo.getString("template"));
        JsonObject parameters = jo.getJsonObject("parameters");
        Iterator it = parameters.keySet().iterator();
        while(it.hasNext()){
            String key = String.valueOf(it.next());
            String value = parameters.getString(key);
            fml.put(key,value);
        }
        manager.queue(new ArrayList<>(fml.keySet()),new ArrayList<>(fml.values()));
        manager.process();
        System.out.println("Template is executed");
    }
    /**
     * DEPRECATED: FLIPPER 1
     * Old class for executing the behaviours
     * @param argNames
     * @param argValues
     */
    public void execute(ArrayList<String> argNames, ArrayList<String> argValues) {
        if (argNames.contains("template")) {
            manager.queue(argNames, argValues);
        } else {
            System.out.println("Current behaviour does not contain template argument! \n ");
        }
        int i = argNames.indexOf("fallback");
        Say newSay;
        if (i >= 0) {
            newSay = new Say(argValues.get(i), agentName, true);
        } else {
            newSay = new Say("No fallback found for response!", agentName, true);
        }


    }

    public void prepare(ArrayList<String> argNames, ArrayList<String> argValues) {

    }

    public void setLogfile(String s) {
        this.manager.setLogfile(s);
    }

    public boolean isBMLReceiverConnected() {
        return this.manager.isBMLReceiverConnected();
    }

    public boolean hasReceivedBML() {
        return this.manager.hasReceivedBML();
    }

    public boolean isConnected() {
        return this.manager.isConnected();
    }

    public boolean canTalk(Boolean interruptSelf) {
        if (interruptSelf) {
            return true;
        } else {
            return !this.manager.getAgentIsTalking();
        }
    }

    public boolean isTalking() {
        return this.manager.getAgentIsTalking();
    }

    public SimpleManager getManager() {
        return this.manager;
    }

    public void setManager(SimpleManager manager) {
        if (manager instanceof FMLManager) {
            this.manager = (FMLManager) manager;
        } else {
            System.err.println("The FMLGenerator behaviour requires a FMLManager as its manager!");
        }
    }

    public boolean stoppedTalking() {
        return this.manager.stoppedTalking;
    }

    public boolean isPlanning() {
        return this.manager.isPlanning();
    }

    public void setPlanning(Boolean b) {
        this.manager.setPlanning(b);
    }

    public String introduceNewTopic(String possible, String previous) {
        JsonReader possibleRead = Json.createReader(new StringReader(possible));
        JsonReader previousRead = Json.createReader(new StringReader(previous));
        JsonArray previousMoves = previousRead.readArray();
        JsonArray possibleMoves = possibleRead.readArray();

        JsonArrayBuilder jab = Json.createArrayBuilder();
        for (int i = 0; i < possibleMoves.size(); i++) {
            JsonObject jo = possibleMoves.getJsonObject(i);
            String id = jo.getString("id");
            if (previousMoves.contains(id)) {
                System.out.println("Exists: " + id);
            }
            if (!previousMoves.contains(id)) {
                if (id.startsWith("qa")) {
                    jab.add(jo);
                }
            }
        }
        JsonArray introductionMoves = jab.build();
        System.out.println("possibleMoves left: " + introductionMoves.size());
        int randomNum = ThreadLocalRandom.current().nextInt(0, introductionMoves.size());
        String newMove = introductionMoves.get(randomNum).toString();
        return newMove;
    }

    public String getLastText() {
        return this.manager.getLastText();
    }
    public boolean init(){
        System.out.println("initializing FMLGenerator");
        return true;
    }
    public boolean initFmlSender(String host, String port, String senderFmlTopic){
        this.manager = new FMLManager(host, port, senderFmlTopic);
        System.out.println("initializing FMLGenerator");
        return true;
    }
}
