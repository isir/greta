package greta.furhat.activemq;


import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.*;
import javax.jms.MessageProducer;
import org.apache.activemq.command.ActiveMQMessage;


import greta.core.util.audio.Audio;
import greta.core.util.speech.Phoneme;
import greta.core.util.time.TimeMarker;
import greta.core.util.speech.Speech;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;



/**
 *
 * @author Fousseyni Sangaré 04/2024-09/2024
 */

import java.util.HashMap;
import java.util.Map;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import org.apache.activemq.command.ActiveMQMessage;

/**
 *
 * @author Andre-Marie Pez
 */

public class GretaFurhatAudioSender extends WhiteBoard{

    protected MessageProducer producer;
    private ScheduledExecutorService executorService;

    public GretaFurhatAudioSender(){
        super();
    }
    public GretaFurhatAudioSender(String host, String port, String topic){
        super(host, port, topic);
        executorService = Executors.newScheduledThreadPool(1);
        startConnectionAttempt();
    }

    @Override
    protected void onConnectionStarted() {
        createProducer();
        super.onConnectionStarted();
    }

    private void createProducer(){
        try {
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch (Exception ex) {
            //Logs.error("producer not created");
        }
    } 

    @Override
    protected void onReboot() {
        super.onReboot();
        closeProducer();
    }

    private void closeProducer(){
        try { producer.close(); } catch (Exception ex) {//Logs.error("at producer closing : "+ex.getLocalizedMessage());

        }
        producer = null;
    }

    @Override
    protected void onDestinationChanged() {
        super.onDestinationChanged();
        closeProducer();
        createProducer();
    }
 

    public void send(byte[] audioBuffer, List<Phoneme> phonemeList, List<Object> speechElements, Audio audio){
        
        try {
           
         System.out.println("Sending audio over topic");
            
        // Parse phonemes and sentence
        Transcription transcription = parsePhonemesAndSentence(phonemeList, speechElements);

        // Serialize to JSON
        String Phonemejson = serializeTranscription(transcription);
        
        // Create a TextMessage for the phoneme data
        TextMessage phonemeMessage = session.createTextMessage(Phonemejson);

        // Set properties for the phoneme message
        phonemeMessage.setStringProperty("Type", "PhonemeData");
        
        // Send the phoneme message
        producer.send(phonemeMessage);
        
           // Create a BytesMessage for the audio buffer
        BytesMessage audioMessage = session.createBytesMessage();
        audioMessage.writeBytes(audioBuffer);

        // Set properties for the WAV file
        audioMessage.setFloatProperty("SampleRate", audio.getFormat().getSampleRate());
        audioMessage.setIntProperty("BitsPerSample", audio.getFormat().getSampleSizeInBits());
        audioMessage.setIntProperty("Channels", audio.getFormat().getChannels());
        
        // Send the audio message
        producer.send(audioMessage);

        // Phoneme data and speech text
        //phonemeJson = "{ \"class\": \"furhatos.records.Transcription\", \"phones\": [ { \"name\": \"_s\", \"prominent\": false, \"start\": 0.0, \"end\": 0.2 }, { \"name\": \"AI\", \"prominent\": true, \"start\": 0.2, \"word\": \"kids\", \"end\": 1.2 }, { \"name\": \"_s\", \"prominent\": false, \"start\": 1.2, \"end\": 1.7 }, { \"name\": \"AE\", \"prominent\": true, \"start\": 1.7, \"word\": \"are\", \"end\": 2.2 }, { \"name\": \"M\", \"prominent\": false, \"start\": 2.22, \"end\": 2.33 }, { \"name\": \"AH\", \"prominent\": false, \"start\": 2.33, \"word\": \"talking\", \"end\": 2.67 }, { \"name\": \"_s\", \"prominent\": false, \"start\": 2.67, \"end\": 2.7 }, { \"name\": \"R\", \"prominent\": false, \"start\": 2.7, \"word\": \"by\", \"end\": 2.81 }, { \"name\": \"OWX\", \"prominent\": false, \"start\": 2.81, \"end\": 2.86 }, { \"name\": \"B\", \"prominent\": false, \"start\": 2.86, \"end\": 2.92 }, { \"name\": \"AH\", \"prominent\": false, \"start\": 2.92, \"word\": \"the\", \"end\": 3.02 }, { \"name\": \"T\", \"prominent\": false, \"start\": 3.02, \"end\": 3.2 }, { \"name\": \"_s\", \"prominent\": false, \"start\": 3.2, \"word\": \"door\", \"end\": 3.8 }, { \"name\": \"\", \"prominent\": false, \"start\": 3.8, \"end\": 4.0 } ] }";

        // Create a TextMessage for the speech text
        /*TextMessage speechMessage = session.createTextMessage(speechElements);

        // Set properties for the speech message
        speechMessage.setStringProperty("Type", "SpeechText");
        
        // Send the speech message
        producer.send(speechMessage);*/
        
        
            
        } catch (Exception ex) {
            //Logs.error("could not send message");
            System.out.println("greta.furhat.activemq.GretaFurhatAudioSender: could not send message: "+ex.toString());
        }
    }
    
    public static class Transcription {
        @JsonProperty("class")
        private String className;
        private List<Phone> phones;

        public Transcription(String className, List<Phone> phones) {
            this.className = className;
            this.phones = phones;
        }

        public static class Phone {
            private String name;
            private boolean prominent;
            private double start;
            private double end;
            private String word;

            public Phone(String name, boolean prominent, double start, double end, String word) {
                this.name = name;
                this.prominent = prominent;
                this.start = start;
                this.end = end;
                this.word = word;
            }
        }
    }
    
    // Phoneme mapping from the first agent's list to the second agent's list
    private static final Map<String, String> phonemeMapping = new HashMap<>();

    static {
        phonemeMapping.put("pause", "_s");
        phonemeMapping.put("a1", "AA");
        phonemeMapping.put("a", "A");
        phonemeMapping.put("e1", "EHX");
        phonemeMapping.put("e", "E");
        phonemeMapping.put("E1", "EH");
        phonemeMapping.put("i1", "IH");
        phonemeMapping.put("i", "I");
        phonemeMapping.put("o1", "AO");
        phonemeMapping.put("o", "O");
        phonemeMapping.put("O1", "OWX");
        phonemeMapping.put("u1", "UH");
        phonemeMapping.put("u", "UU");
        phonemeMapping.put("y", "Y");
        phonemeMapping.put("b", "B");
        phonemeMapping.put("c", "C");
        phonemeMapping.put("d", "D");
        phonemeMapping.put("f", "F");
        phonemeMapping.put("g", "G");
        phonemeMapping.put("h", "H");
        phonemeMapping.put("k", "K");
        phonemeMapping.put("l", "L");
        phonemeMapping.put("m", "M");
        phonemeMapping.put("n", "N");
        phonemeMapping.put("p", "P");
        phonemeMapping.put("q", "Q");
        phonemeMapping.put("r", "R");
        phonemeMapping.put("s", "S");
        phonemeMapping.put("t", "T");
        phonemeMapping.put("v", "V");
        phonemeMapping.put("w", "W");
        phonemeMapping.put("z", "Z");
        phonemeMapping.put("SS", "SH");
        phonemeMapping.put("tS", "CH");
        phonemeMapping.put("th", "TH");
    }

     public static Transcription parsePhonemesAndSentence(List<Phoneme> phonemeList, List<Object> sentence) {
        List<Transcription.Phone> phones = new ArrayList<>();
        double currentTime = 0.0;
        String currentWord = null;

        for (Phoneme phoneme : phonemeList) {
            // Check for the next word in the sentence array
            while (!sentence.isEmpty()) {
                Object element = sentence.get(0);
                if (element instanceof TimeMarker && ((TimeMarker) element).getValue()<= currentTime) {
                    sentence.remove(0); // Remove the TimeMarker
                    if (!sentence.isEmpty() && sentence.get(0) instanceof String) {
                        currentWord = (String) sentence.remove(0); // Remove and get the word
                    }
                } else {
                    break;
                }
            }

            // Create Phone object
            Transcription.Phone phone = new Transcription.Phone(
                    phoneme.getPhonemeType().toString(),
                    phoneme.getPhonemeType().toString().matches("[AEIOUaeiou]"),  // simplistic prominent detection
                    currentTime,
                    currentTime + phoneme.getDuration(),
                    null  // Initialize word attribute as null
            );

            // If currentWord is not null, assign it to the first phone in the word
            if (currentWord != null) {
                phone.word = currentWord;
                currentWord = null;  // Reset currentWord so it's only assigned once per word
            }

            phones.add(phone);
            currentTime += phoneme.getDuration();
        }

        return new Transcription("furhatos.records.Transcription", phones);
    }

    public static String serializeTranscription(Transcription transcription) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(transcription);
    }

    private void startConnectionAttempt() {
        executorService.scheduleWithFixedDelay(this::attemptConnection, 0, 1, TimeUnit.SECONDS);
        attemptConnection();
        
    }

    private void attemptConnection() {
        
        
        if (!this.isConnected()){
            
            try{
                //System.out.println("Attempting to connect audioserver to broker at: "+this.getURL());
                startConnection();
            } catch(Exception e){
            System.err.println("greta.furhat.activemq.GretaFurhatAUSender: audio server connection attempt failed: " + e.getMessage());
            }
        }
        else{
            System.out.println("greta.furhat.activemq.GretaFurhatAUSender: audio connected to broker at: "+this.getURL() );
            executorService.shutdown(); // Stop retrying after a successful connection
         }   
    }
}
