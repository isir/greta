package greta.auxiliary.tts.azuretts;

import greta.core.behaviorrealizer.ClientPhoneme;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.CharacterManager;
import greta.core.util.audio.Audio;
import greta.core.util.log.Logs;
import greta.core.util.speech.Phoneme;
import greta.core.util.speech.Speech;
import greta.core.util.speech.TTS;
import greta.core.util.time.TimeMarker;
import greta.core.util.xml.XMLTree;
import greta.furhat.activemq.GretaFurhatAudioSender;
import greta.furhat.activemq.GretaFurhatSpeechTextSender;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.sound.sampled.AudioFormat;
import javax.swing.JComboBox;

/**
 *
 * @author takes
 */


public class AzureTTSFrame extends javax.swing.JFrame implements TTS {

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jComboBox1 = new javax.swing.JComboBox<>();

        jMenuItem1.setText("jMenuItem1");

        jMenu1.setText("jMenu1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(357, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(265, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
        
        selectedVoice = jComboBox1.getSelectedItem().toString();
        selectedVoice = selectedVoice.split(",")[1];
        if (selectedVoice.contains(" ")) {
            selectedVoice = selectedVoice.split(" ")[0];        
        }
        
    }//GEN-LAST:event_jComboBox1ActionPerformed
    
    
    private CharacterManager characterManager;

    private boolean initialized = false;
    private boolean functional = false;
    private String LOAD_ABSOLUTE_PATH = null;
    private String VOICES_ABSOLUTE_PATH = null;

    private boolean interreuptionReactionSupported;
    private Audio audio; //audio buffer
    private byte[] rawAudioBuffer;
    private static AudioFormat audioFormatCereProc;
    private List<Phoneme> phonemes;//phoneme list computed by the native cereproc library
    private Speech speech; //speech object, input to the TTS engine
    private List<TimeMarker> tmOfSpeechList; //time markers list outputed by the native cereproc library

    int tmnumber; //time marker number

    private String languageID; // Character's language specified using <LANGUAGE_CODE_ISO>-<COUNTRY_CODE_ISO> in character's .ini file (e.g. en-GB)
    private String voiceName; // Character's voice specified in character's .ini file (e.g. en-GB)
    
    private GretaFurhatSpeechTextSender speechtextserver;
    private GretaFurhatAudioSender audioserver;
    
    private String AzureTTS_env_checker_path = "Common\\Data\\AzureTTS\\check_env.py";
    private String AzureTTS_env_installer_path = "Common\\Data\\AzureTTS\\init_env.bat";

    private String getAvailableVoices_bat_path = "Common\\Data\\AzureTTS\\get_available_voices.bat";
    
    private String[] voiceNames = {
        // Check here to add others: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/language-support?tabs=tts
        "en-GB-AdaMultilingualNeural", //female 
        "en-GB-OllieMultilingualNeural", //male
        "ja-JP-ShioriNeural",
        "ja-JP-NaokiNeural",
        "fr-FR-LucienMultilingualNeural",
        "fr-FR-VivienneMultilingualNeural"
    };
    private String selectedVoice = "en-GB-AdaMultilingualNeural";
    
    private Process main_process;
        
    private AzureTTS azureTTS;
    

    /**
     * Creates new form AzureTTSFrame
     */
    public AzureTTSFrame(CharacterManager cm) {
        initComponents();

        interreuptionReactionSupported = true;
        setupCharacterLanguageVoiceParameters();

        azureTTS = new AzureTTS(cm);
        init();
        cm.setTTS(this);
        setCharacterManager(cm);

        clean();
        tmnumber = 0;

        try{
            main_process = new ProcessBuilder("python", AzureTTS_env_checker_path).redirectErrorStream(true).start();
            main_process.waitFor();
        } catch (Exception e){
           e.printStackTrace();
        }        

        InputStream inputStream = main_process.getInputStream();
        String result = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n")
                );
        System.out.println("greta.auxiliary.tts.azuretts.AzureTTSFrame.AzureTTSFrame(): python env exist: " + result);
        
        if(result.equals("0")){
            
            System.out.println("greta.auxiliary.tts.azuretts.AzureTTSFrame.AzureTTSFrame(): installing python environment...");
            try{
                main_process = new ProcessBuilder(AzureTTS_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                main_process.waitFor();
            } catch (Exception e){
                e.printStackTrace();
            }
            
        }        
        
        try {
            main_process = new ProcessBuilder(getAvailableVoices_bat_path).redirectErrorStream(true).start();                    
            //main_process.waitFor();
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(main_process.getInputStream()));
            String line = null;
            int index = 0;
            while ( (line = reader.readLine()) != null) {
                // System.out.println("greta.auxiliary.tts.azuretts.AzureTTSFrame.AzureTTSFrame(): output from python: " + line);
                if (!line.contains("INFO")) {
                    String[] data = line.split(",");
                    String gender = "";
                    if (data[1].contains("Male")) {
                        gender = "Male";
                    }
                    else {
                        gender = "Female";
                    }
                    String name = Integer.toString(index) + "," + data[0] + " (" + gender + ")";
                    jComboBox1.addItem(name);
                    index += 1;
                }
            }
            
        } catch (IOException ex) {
            Logger.getLogger(AzureTTSFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
//        catch (Exception e) {
//            
//            System.out.println("greta.auxiliary.tts.azuretts.AzureTTSFrame.AzureTTSFrame(): exception - " + e);
//            
//            for (String voiceName: voiceNames) {
//                jComboBox1.addItem(voiceName);
//            }
//        }
        
//        for (String voiceName: voiceNames) {
//            jComboBox1.addItem(voiceName);
//        }
        
                
        /*====================================================================================================*/
        /*                     This block is part of the Greta Furhat Interface       
                                   Author: Fousseyni Sangaré 04/2024-09/2024                                  */
        /*====================================================================================================*/
        
        speechtextserver = new GretaFurhatSpeechTextSender("localhost", "61616", "greta.furhat.SpeechText");
        audioserver = new GretaFurhatAudioSender("localhost", "61616", "greta.furhat.Audio");
        
        /*====================================================================================================*/
        /*====================================================================================================*/    
    
    }
    
    static{
        // Init constants and make phonemes mappings
        AzureTTSConstants.init();
    }

    private static boolean checkFolder(String path) {
        return (path != null) && (!path.isEmpty()) && (Files.isDirectory(Paths.get(path)));
    }

    /*
     * Initialization function, CereProcConstants.init() reads parameters from {Greta}/bin/Greta.ini file
     */
    private void init() {
        this.azureTTS.init();
    }

    /**
     * Set new speech object to be computed. </br>
     * Also sets a new temp file path to avoid the speech duplication bug.
     * @param speech Speech object to compute
     */
    @Override
    public void setSpeech(Speech speech) {
        this.azureTTS.setSpeech(speech);
    }

    // Relative changes in pitch and rate (used in Mathieu'sampleAudio experiment)
    private double rate =0.0;
    private double pitch =0.0;
    public void setRateAndPitch(double rate, double pitch)
    {
        this.azureTTS.setRateAndPitch(rate, pitch);
    }

    @Override
    public boolean isInterruptionReactionSupported() {
        return this.azureTTS.isInterruptionReactionSupported();
    }

    /**
     * Computes the latest speech object. </br>
     * The three boolean should all be true: CereProc only does all at once.
     * @param doTemporize Temporize or not
     * @param doAudio Compute audio or not
     * @param doPhonems Compute phonems or not
     */
    @Override
    public void compute(boolean doTemporize, boolean doAudio, boolean doPhonems) {
        this.azureTTS.compute(doTemporize, doAudio, doPhonems, selectedVoice);
        audio = this.azureTTS.getAudio();
        phonemes = this.azureTTS.getPhonemes();
    }
    
    @Override
    public List<Phoneme> getPhonemes() {
        phonemes = this.azureTTS.getPhonemes();
        return phonemes;
    }

    @Override
    public Audio getAudio() {
        audio = this.azureTTS.getAudio();
        return audio;
    }

    private void clean() {
        audio = null;
        phonemes = new ArrayList<Phoneme>();
        speech = null;
        tmnumber = 0;
        
        this.azureTTS.clean();
        
    }

    private void setupCharacterLanguageVoiceParameters() {
        languageID = "To be implemented";
        voiceName = "To be implemented";
    }

    public void onCharacterChanged() {
        String newLanguage = getCharacterManager().getLanguage();
        String newVoiceName = "To be implemented";        
    }

    protected void finalize() throws Throwable {
        getCharacterManager().remove((CharacterDependent) this);
        super.finalize();
    }

    public CharacterManager getCharacterManager() {
        return this.azureTTS.getCharacterManager();
    }

    /**
     * @param characterManager the characterManager to set
     */
    public void setCharacterManager(CharacterManager characterManager) {
        this.azureTTS.setCharacterManager(characterManager);
    }

    public static CharacterManager getCharacterManagerStatic() {
        return CharacterManager.getStaticInstance();
    }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem1;
    // End of variables declaration//GEN-END:variables
}
