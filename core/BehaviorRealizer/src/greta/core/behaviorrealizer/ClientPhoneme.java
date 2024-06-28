package greta.core.behaviorrealizer;
/**
 *
 * @author NEZIH YOUNSI
 */

import greta.core.util.speech.Phoneme;
import greta.core.util.speech.Phoneme.PhonemeType;
import greta.core.keyframes.PhonemSequence;
import greta.core.util.id.ID;
import greta.core.util.Mode;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import java.text.Normalizer;


public class ClientPhoneme {
    private final List<Phoneme> phonemeList = new ArrayList<>();
    private static final Map<String, PhonemeType> phonemeMapping = new HashMap<>();
    private PhonemSequence lastReceivedSequence = null; // Store the last received PhonemeSequence


    static {
        InitPhonemes();
    }

    private static void InitPhonemes() {
        // Example mappings, add as needed
    
        
        phonemeMapping.put("ɡ", PhonemeType.g); // Assuming 'ɡ' corresponds to the Greta phoneme type 'g'
        phonemeMapping.put("o", PhonemeType.o); // Assuming 'o' corresponds to the Greta phoneme type 'o'
        phonemeMapping.put("i", PhonemeType.i);
        phonemeMapping.put("ɾ", PhonemeType.r);
        phonemeMapping.put("sil", PhonemeType.pause);
        phonemeMapping.put("ɚ", PhonemeType.E1);
        phonemeMapping.put("ʊ", PhonemeType.u);
        phonemeMapping.put("ɪ", PhonemeType.i1);
        phonemeMapping.put("ŋ", PhonemeType.g);
        phonemeMapping.put("ɐ", PhonemeType.a); // You might need to adjust this based on the correct mapping
        phonemeMapping.put("ɔ", PhonemeType.o); // Adjust if 'ɔ' corresponds to a different Greta phoneme type
        phonemeMapping.put("ɹ", PhonemeType.r); // Assuming 'ɹ' corresponds to the Greta phoneme type 'r'
        phonemeMapping.put("ɑ", PhonemeType.a);
        phonemeMapping.put("iː", PhonemeType.i); // "sheep"
        phonemeMapping.put("ɪ", PhonemeType.i1); // "ship"
        phonemeMapping.put("ʊ", PhonemeType.u); // "good"
        phonemeMapping.put("uː", PhonemeType.u1); // "shoot"
        phonemeMapping.put("e", PhonemeType.e); // "bed"
        phonemeMapping.put("ə", PhonemeType.e1); // "teacher" (schwa sound, may need adjustment)
        phonemeMapping.put("ɜː", PhonemeType.E1);
        phonemeMapping.put("ɜ", PhonemeType.E1);// "bird" (may need adjustment)
        phonemeMapping.put("ɔː", PhonemeType.o); // "door"
        phonemeMapping.put("æ", PhonemeType.a1); // "cat"
        phonemeMapping.put("ʌ", PhonemeType.a); // "up"
        phonemeMapping.put("ɑː", PhonemeType.a); // "far"
        phonemeMapping.put("ɒ", PhonemeType.o1); // "on"
        phonemeMapping.put("ɪə", PhonemeType.e); // "here"
        phonemeMapping.put("eɪ", PhonemeType.e); // "wait"
        phonemeMapping.put("aɪ", PhonemeType.a); // "my"
        phonemeMapping.put("ɔɪ", PhonemeType.o); // "boy"
        phonemeMapping.put("aʊ", PhonemeType.a); // "cow"
        phonemeMapping.put("əʊ", PhonemeType.o); // "tourist" (may need adjustment)
        phonemeMapping.put("p", PhonemeType.p); // "pea"
        phonemeMapping.put("b", PhonemeType.b); // "boat"
        phonemeMapping.put("t", PhonemeType.t); // "tea"
        phonemeMapping.put("d", PhonemeType.d); // "dog"
        phonemeMapping.put("tʃ", PhonemeType.tS); // "cheese"
        phonemeMapping.put("k", PhonemeType.k); // "car"
        phonemeMapping.put("g", PhonemeType.g); // "go"
        phonemeMapping.put("f", PhonemeType.f); // "fly"
        phonemeMapping.put("v", PhonemeType.v); // "video"
        phonemeMapping.put("θ", PhonemeType.th); // "think"
        phonemeMapping.put("ð", PhonemeType.th); // Assuming Greta has one 'th' for both voiced/unvoiced
        phonemeMapping.put("s", PhonemeType.s); // "see"
        phonemeMapping.put("z", PhonemeType.z); // "zoo"
        phonemeMapping.put("ʃ", PhonemeType.SS); // "shall"
        phonemeMapping.put("ʒ", PhonemeType.z); // Assuming no direct match for 'ʒ', using 'z'
        phonemeMapping.put("m", PhonemeType.m); // "man"
        phonemeMapping.put("n", PhonemeType.n); // "now"
        phonemeMapping.put("ŋ", PhonemeType.g); // Assuming no direct match for 'ŋ', using 'g'
        phonemeMapping.put("h", PhonemeType.h); // Assuming Greta has a 'h' phoneme type
        phonemeMapping.put("l", PhonemeType.l); // "love"
        phonemeMapping.put("r", PhonemeType.r); // "red"
        phonemeMapping.put("w", PhonemeType.w); // "wet"
        phonemeMapping.put("j", PhonemeType.y); // "yes"
       
    }
    
 
    private String convertUnicodeEscape(String str) {
    if (str.startsWith("\\u")) {
        // Convert from Unicode escape format to the actual character
        int code = Integer.parseInt(str.substring(2), 16); // Base 16 for hexadecimal
        return Character.toString((char) code);
    }
    return str;
    }
    public void startClient() {
        String host = "localhost";
        int port = 60000;

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            StringBuilder data = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                data.append(line);
            }

            parseAndConvertPhonemes(data.toString());

            // Here, create and use your PhonemeSequence
            lastReceivedSequence = createPhonemeSequence("UniqueId", 0.0);
            System.out.println("PhonemeSequence created: " + lastReceivedSequence);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseAndConvertPhonemes(String jsonString) {
        jsonString = jsonString.substring(1, jsonString.length() - 1); // Remove square brackets
        String[] phonemesData = jsonString.split("\\},\\{");

        for (String phonemeData : phonemesData) {
            phonemeData = phonemeData.replace("{", "").replace("}", "");
            phonemeData = phonemeData.replace("[", "");
            
            String[] keyValuePairs = phonemeData.split("],");
            
            
            String phonemeKey = null;
            String duration = null;
            
            for (String pair : keyValuePairs) {
                
                String[] entry = pair.split(",");
                
                if (entry.length > 1) { // Check that both key and value exist
                    
                    String key = entry[0].replace("]", "").trim();
                    String value = entry[1].replace("]", "").trim();
                    
                    
                    phonemeKey = key;
                    
                    phonemeKey = StringEscapeUtils.unescapeJava(key);
                    phonemeKey = phonemeKey.replace("\"", "");
                    
                    // Normalize phoneme key and lookup in the map
                    phonemeKey = Normalizer.normalize(phonemeKey, Normalizer.Form.NFC);
                    //System.out.println("Phoneme key is : " + phonemeKey);
                    duration = value;
                }
               
                InitPhonemes();
                PhonemeType convertedType = phonemeMapping.get(phonemeKey);
                if (convertedType == null){
                    convertedType = PhonemeType.a;
                }
                if (convertedType == PhonemeType.h){
                    convertedType = PhonemeType.a;
                }
                //System.out.println("Phoneme Type is : " + convertedType);
                phonemeList.add(new Phoneme(convertedType, Double.parseDouble(duration)));
                
            }

        System.out.println("Received and converted phoneme data: " + phonemeList);
        
    }
    }
    private PhonemSequence createPhonemeSequence(String sequenceId, double startTime) {
        if (phonemeList.isEmpty()) {
            System.err.println("Phoneme list is empty. No PhonemeSequence created.");
            return null;
        }

        return new PhonemSequence(sequenceId, phonemeList, startTime);
    }
    
     public void clearLastReceivedSequence() {
        lastReceivedSequence = null;
    }

    public PhonemSequence getLastReceivedSequence() {
        return lastReceivedSequence;
    }

    public static void main(String[] args) {
        ClientPhoneme client = new ClientPhoneme();
        client.startClient();
    }
}


