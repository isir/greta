/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.deepasr;


import greta.auxiliary.llm.LLMFrame;
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
import greta.auxiliary.llm.MistralFrame;
import greta.core.feedbacks.Callback;
import java.io.InputStream;
import java.util.stream.Collectors;
import greta.core.feedbacks.FeedbackPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import java.text.Normalizer;

/**
 *
 * @author miche
 */
public class DeepGramFrame extends DeepASRFrame {

    /**
     * Creates new form DeepGramFrame
     */
    
    private String DeepASR_python_env_checker_path = "Common\\Data\\DeepASR\\DeepGram\\check_env.py";
    private String DeepASR_python_env_installer_path = "Common\\Data\\DeepASR\\DeepGram\\init_env.bat";
    private String python_asr_path = "\\Common\\Data\\DeepASR\\DeepGram\\DeepGram.py";
    private Process server_process;
    private Thread server_shutdownHook;
    
    private volatile boolean automaticListenBool = false;
    private volatile boolean agentSpeaking = false;
    
    /** Source prefixes that identify LLM-generated FML animations (as opposed to listener backchannels, mimicry, etc.). */
    private static final String[] LLM_SOURCE_PREFIXES = {
        "fml_mistral",       // intermediate streaming chunks (fml_mistral.xml / fml_mistral_renew.xml)
        "fml_endSentence"    // end-of-answer marker loaded by incremental LLM frames
    };
    
    private static final long DUPLICATE_WINDOW_MS = 3000;
    private volatile String lastSentTranscript = "";
    private volatile long lastSentTimeMs = 0;
    private volatile boolean sentThisTurn = false;
    
    private ArrayList<MistralFrame> mistrals = new ArrayList<MistralFrame>();
    private static String markup = "fml-apml";
    private Server server;

    private final Object lock = new Object();    
    
//    private volatile Boolean IsListenning = Boolean.FALSE;

    private void updateListeningUi(boolean listening) {
        synchronized (lock) {
            IsListenning = listening;
            listen.setText(listening ? "Stop" : "Listen");
            System.out.println("[DeepGram] UI updated: IsListenning=" + listening);
        }
    }

    private void startListeningNow() throws IOException {
        String language = cm.getLanguage();
        System.out.println("[DeepGram] startListeningNow() called | language=" + language + " | automaticListenBool=" + automaticListenBool + " | agentSpeaking=" + agentSpeaking);
        sentThisTurn = false;
        server.sendMessage(language);
        updateListeningUi(true);
    }

    private void stopListeningNow() throws IOException {
        System.out.println("[DeepGram] stopListeningNow() called | IsListenning=" + IsListenning);
        server.sendMessage("STOP");
        updateListeningUi(false);
    }

    private void startListeningAsync() {
        System.out.println("[DeepGram] startListeningAsync() requested | IsListenning=" + IsListenning);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    if (!IsListenning) {
                        startListeningNow();
                    } else {
                        System.out.println("[DeepGram] startListeningAsync() skipped: already listening");
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        t.start();
    }

    private void stopListeningAsync() {
        System.out.println("[DeepGram] stopListeningAsync() requested | IsListenning=" + IsListenning);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    if (IsListenning) {
                        stopListeningNow();
                    } else {
                        System.out.println("[DeepGram] stopListeningAsync() skipped: not listening");
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        t.start();
    }

    /**
     * Strip accents, punctuation, and lowercase so that e.g.
     * "D'accord." and "d'accord" compare as equal.
     */
    private static String normalizeFuzzy(String text) {
        if (text == null) return "";
        String s = text.trim().toLowerCase();
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        s = s.replaceAll("[^a-z0-9 ]", "");
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }

    private boolean shouldSendTranscript(String transcript) {
        String raw = transcript == null ? "" : transcript.trim();
        if (raw.isEmpty()) {
            System.out.println("[DeepGram] shouldSendTranscript() -> FALSE (empty transcript)");
            return false;
        }

        if (sentThisTurn) {
            System.out.println("[DeepGram] shouldSendTranscript() -> FALSE (already sent this turn): \"" + raw + "\"");
            return false;
        }

        String fuzzy = normalizeFuzzy(raw);
        String lastFuzzy = normalizeFuzzy(lastSentTranscript);
        long now = System.currentTimeMillis();
        long elapsed = now - lastSentTimeMs;

        if (fuzzy.equals(lastFuzzy) && elapsed < DUPLICATE_WINDOW_MS) {
            System.out.println("[DeepGram] shouldSendTranscript() -> FALSE (fuzzy duplicate within " + elapsed + "ms): \"" + raw + "\" normalized=\"" + fuzzy + "\"");
            return false;
        }

        System.out.println("[DeepGram] shouldSendTranscript() -> TRUE (new transcript, " + elapsed + "ms since last): \"" + raw + "\" normalized=\"" + fuzzy + "\"");
        lastSentTranscript = raw;
        lastSentTimeMs = now;
        sentThisTurn = true;
        return true;
    }

    public DeepGramFrame(CharacterManager cm)throws InterruptedException {
        super(cm);
        initComponents();
        server = new greta.auxiliary.deepasr.Server();
        this.cm=cm;
    }
    
    private boolean matchesLLMSource(String source) {
        if (source == null) return false;
        for (String prefix : LLM_SOURCE_PREFIXES) {
            if (source.startsWith(prefix)) return true;
        }
        return false;
    }

    private boolean isLLMAnimation(ID animId) {
        if (animId == null) {
            System.out.println("[DeepGram] isLLMAnimation() -> false (animId is null)");
            return false;
        }
        String directSource = animId.getSource();
        if (matchesLLMSource(directSource)) {
            System.out.println("[DeepGram] isLLMAnimation() -> TRUE (direct source matches: \"" + directSource + "\")");
            return true;
        }
        List<ID> allParents = animId.getAllParents();
        StringBuilder parentSources = new StringBuilder();
        for (ID parent : allParents) {
            String ps = parent.getSource();
            if (parentSources.length() > 0) parentSources.append(", ");
            parentSources.append(ps);
            if (matchesLLMSource(ps)) {
                System.out.println("[DeepGram] isLLMAnimation() -> TRUE (parent source matches: \"" + ps + "\") | direct=\"" + directSource + "\" | all parents=[" + parentSources + "]");
                return true;
            }
        }
        System.out.println("[DeepGram] isLLMAnimation() -> FALSE | direct=\"" + directSource + "\" | parents=[" + parentSources + "]");
        return false;
    }

    @Override
    public void performFeedback(String type){
        System.out.println("[DeepGram] performFeedback(String) called with type=\"" + type + "\" -- no-op (routed via Callback path)");
    }

    @Override
    public void performFeedback(Callback callback){
        String type = callback.type();
        ID animId = callback.animId();
        String directSource = animId != null ? animId.getSource() : "null";
        String idStr = animId != null ? animId.toString() : "null";
        System.out.println("[DeepGram] ============ performFeedback(Callback) ============");
        System.out.println("[DeepGram]   type=\"" + type + "\" | animId=" + idStr + " | directSource=\"" + directSource + "\"");
        System.out.println("[DeepGram]   state: IsListenning=" + IsListenning + " | automaticListenBool=" + automaticListenBool + " | agentSpeaking=" + agentSpeaking);

        boolean isLLM = isLLMAnimation(animId);

        if ("start".equals(type)){
            if (isLLM){
                agentSpeaking = true;
                System.out.println("[DeepGram]   -> LLM animation STARTED. agentSpeaking=true");
                if (IsListenning){
                    System.out.println("[DeepGram]   -> Stopping ASR (agent is speaking)");
                    stopListeningAsync();
                }
            } else {
                System.out.println("[DeepGram]   -> Non-LLM start (ignored for auto-listen): source=\"" + directSource + "\"");
            }
        } else if ("end".equals(type) || "stopped".equals(type)){
            if (isLLM){
                agentSpeaking = false;
                System.out.println("[DeepGram]   -> LLM animation ENDED. agentSpeaking=false");
                if (!IsListenning && automaticListenBool){
                    System.out.println("[DeepGram]   -> Auto-starting ASR (LLM speech finished, automatic mode ON)");
                    startListeningAsync();
                } else {
                    System.out.println("[DeepGram]   -> NOT auto-starting ASR: IsListenning=" + IsListenning + " automaticListenBool=" + automaticListenBool);
                }
            } else {
                System.out.println("[DeepGram]   -> Non-LLM end/stopped (ignored for auto-listen): source=\"" + directSource + "\"");
            }
        } else {
            System.out.println("[DeepGram]   -> Unhandled callback type: \"" + type + "\"");
        }
        System.out.println("[DeepGram] ================================================");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        port = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        addressLabel = new javax.swing.JLabel();
        address = new javax.swing.JTextField();
        answer = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        TranscriptText = new javax.swing.JTextArea();
        enable = new javax.swing.JCheckBox();
        automaticListen = new javax.swing.JCheckBox();
        listen = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(810, 300));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "DeepGram", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Socket Parametes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13))); // NOI18N

        port.setText("4040");
        port.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portActionPerformed(evt);
            }
        });

        portLabel.setText("Port");

        addressLabel.setText("Address");

        address.setText("localhost");

        answer.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        answer.setText("Transcript");

        TranscriptText.setColumns(20);
        TranscriptText.setRows(5);
        jScrollPane3.setViewportView(TranscriptText);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addressLabel)
                    .addComponent(portLabel))
                .addGap(32, 32, 32)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(44, 44, 44)
                .addComponent(answer)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(portLabel)
                            .addComponent(answer))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addressLabel)
                            .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        enable.setText("Enable");
        enable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableActionPerformed(evt);
            }
        });

        automaticListen.setText("Automatic Listenning");
        automaticListen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                automaticListenActionPerformed(evt);
            }
        });

        listen.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        listen.setText("Listen");
        listen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(enable)
                        .addGap(28, 28, 28)
                        .addComponent(automaticListen)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(listen))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enable)
                    .addComponent(automaticListen)
                    .addComponent(listen))
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void enableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableActionPerformed
            
        if(enable.isSelected()){
            
            System.out.println("DeepGram port:"+server.getPort());
            server.setAddress(address.getText());
            server.setPort(port.getText());
            boolean python=true;
            try{
                server_process = new ProcessBuilder("python", DeepASR_python_env_checker_path).redirectErrorStream(true).start();
                server_process.waitFor();
            } catch (Exception e){
                e.printStackTrace();
            }

            InputStream inputStream = server_process.getInputStream();
            String result = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            System.out.println(".init_DeepGram_server(): DeepASR, python env exist: " + result);

            if(result.equals("0")){
                System.out.println(".init_DeepGram_server(): DeepASR, installing python environment...");
                try{
                    server_process = new ProcessBuilder(DeepASR_python_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                    server_process.waitFor();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            
            if(python){
                try{ 
                    System.out.println("Opening python DeepGram script");
                    
                    server.startConnection();
                    Thread server_launcher_thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                System.out.println("Checking new connections");
                                server.accept_new_connection();
                            } catch (IOException ex) {
                                Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (Exception ex) {
                                Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                            }                            
                        }
                    };
                    server_launcher_thread.start();


                    Thread main_thread = new Thread() {
                        @Override
                        public void run() {
                            
                            try {
                                String[] cmd = {
                                        "cmd.exe","/C","conda","activate","greta_deepgram","&&","python","-u",
                                    System.getProperty("user.dir")+python_asr_path,server.getPort(),
                                };
                                Runtime rt = Runtime.getRuntime();
                                System.out.println("command:"+cmd[0]+" "+cmd[1]+" "+cmd[2]);

                                Process proc = rt.exec(cmd);

                                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream(), "ISO-8859-1"));
                                BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

                                // Read the output from the command
                                System.out.println("Here is the standard output of the command:\n");
                                String s = null;

                                while ((s = stdInput.readLine()) != null) {
                                    
                                    System.out.println("READ INPUT PYTHON :"+s);
                                    
                                    synchronized (lock) {

                                        if(s.length()>1){
                                            System.out.println("CLIENT:"+s);

                                            if (s.contains("Speech Started")){
                                                System.out.println("[DeepGram][ASR] Speech Started detected | automaticListenBool=" + automaticListenBool + " | IsListenning=" + IsListenning);
                                                if (automaticListenBool && !IsListenning) {
                                                    System.out.println("[DeepGram][ASR] Updating UI to reflect user speaking");
                                                    updateListeningUi(true);
                                                }

                                            } else if (s.contains("Speech Final:")){
                                                String transcript = s.replace("Speech Final:","").trim();
                                                System.out.println("[DeepGram][ASR] Speech Final received | transcript=\"" + transcript + "\" | IsListenning=" + IsListenning);
                                                final String finalTranscript1 = transcript;
                                                javax.swing.SwingUtilities.invokeLater(() -> TranscriptText.setText(finalTranscript1));
                                                if (IsListenning && shouldSendTranscript(transcript)){
                                                    System.out.println("[DeepGram][ASR] Sending transcript to " + llms.size() + " LLM(s)");
                                                    for (LLMFrame llm : llms){
                                                        llm.setRequestTextandSend(transcript);
                                                    }
                                                    stopListeningAsync();
                                                } else {
                                                    System.out.println("[DeepGram][ASR] Speech Final NOT sent to LLM: IsListenning=" + IsListenning + " (shouldSend checked above)");
                                                }

                                            } else if (s.contains("Utterance End:")){
                                                String transcript = s.replace("Utterance End:","").trim();
                                                System.out.println("[DeepGram][ASR] Utterance End (with text) | transcript=\"" + transcript + "\" | automaticListenBool=" + automaticListenBool + " | IsListenning=" + IsListenning);
                                                final String finalTranscript2 = transcript;
                                                javax.swing.SwingUtilities.invokeLater(() -> TranscriptText.setText(finalTranscript2));
                                                if (automaticListenBool && IsListenning){
                                                    System.out.println("[DeepGram][ASR] Stopping listening (Utterance End fallback, no LLM send)");
                                                    stopListeningAsync();
                                                }

                                            } else if (s.contains("Utterance End")){
                                                System.out.println("[DeepGram][ASR] Utterance End (bare, no text) | automaticListenBool=" + automaticListenBool + " | IsListenning=" + IsListenning);
                                                if (automaticListenBool && IsListenning){
                                                    System.out.println("[DeepGram][ASR] Stopping listening (bare Utterance End)");
                                                    stopListeningAsync();
                                                }

                                            } else if (s.contains("Is Final:")){
                                                String partial = s.replace("Is Final:","").trim();
                                                System.out.println("[DeepGram][ASR] Is Final (partial) | text=\"" + partial + "\"");
                                                final String finalPartial = partial;
                                                javax.swing.SwingUtilities.invokeLater(() -> TranscriptText.setText(finalPartial));
                                            }

                                        }
                                        
                                    }

                                }

                                // Read any errors from the attempted command
                                System.out.println("Here is the standard error of the command (if any):\n");
                                while ((s = stdError.readLine()) != null) {
                                    System.out.println(s);
                                }   
                            } catch (IOException ex) {
                                Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                        }
                    };
                    
                    main_thread.start();
                    
                    System.out.println("greta.auxiliary.deepasr.DeepGram: " + server.port + "   " + server.address);
                    
                }
                catch(Exception e)
                {
                    e.printStackTrace(); 
                }
            }
            else{
                System.out.println(ANSI_YELLOW+"[INFO]This warning appears because it seems that you enabled the DeepGram module which is optional. "
                        + "Python and/or openai seem to be not installed. You need to install them in order to use this module!"+ANSI_RESET);
  
                enable.setSelected(false);
                enable.setEnabled(false);
                
            }
        
        }else{
            try{
                if(IsListenning){
                    stopListeningNow();

                }
                server.stopConnection();
            }
            catch(Exception e)
            {
                e.printStackTrace(); 
            }
        }
    }//GEN-LAST:event_enableActionPerformed

    private void listenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listenActionPerformed

        
        String language= cm.getLanguage();


        if (IsListenning){
            try{
                stopListeningNow();
            }catch (IOException ex) {
                Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        }else{

            try {
                startListeningNow();
            } catch (IOException ex) {
                Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    
    }//GEN-LAST:event_listenActionPerformed

 
    private void portActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_portActionPerformed

    private void automaticListenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_automaticListenActionPerformed
        automaticListenBool = automaticListen.isSelected();
        System.out.println("[DeepGram] Automatic listening toggled: " + automaticListenBool + " | agentSpeaking=" + agentSpeaking + " | IsListenning=" + IsListenning + " | enabled=" + enable.isSelected());

        if (automaticListenBool && !agentSpeaking && !IsListenning && enable.isSelected()) {
            System.out.println("[DeepGram] Agent is silent and not listening -> auto-starting ASR now");
            startListeningAsync();
        }
    }//GEN-LAST:event_automaticListenActionPerformed

    /**
     * @param args the command line arguments
     */


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea TranscriptText;
    private javax.swing.JTextField address;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JLabel answer;
    private javax.swing.JCheckBox automaticListen;
    private javax.swing.JCheckBox enable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton listen;
    private javax.swing.JTextField port;
    private javax.swing.JLabel portLabel;
    // End of variables declaration//GEN-END:variables

   
}
