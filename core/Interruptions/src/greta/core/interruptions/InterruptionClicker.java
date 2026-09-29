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
package greta.core.interruptions;

import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.util.CharacterDependent;
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
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple Interruption Clicker
 * Loads FML and triggers HALT interruption on button click
 * 
 * @author Simple Interruptor
 */
public class InterruptionClicker extends javax.swing.JFrame implements IntentionEmitter, CharacterDependent {

    private ArrayList<IntentionPerformer> intentionPerformers = new ArrayList<>();
    private CharacterManager characterManager;

    /**
     * @return the characterManager
     */
    public CharacterManager getCharacterManager() {
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    public void setCharacterManager(CharacterManager characterManager) {
        if(this.characterManager!=null)
            this.characterManager.remove(this);
        this.characterManager = characterManager;
        characterManager.add(this);
    }
    /**
     * Creates new form InterruptionClicker
     */
    public InterruptionClicker(CharacterManager cm) {
        setCharacterManager(cm);
        Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH);

        initComponents();
        setLoader(this);
        
        Logs.info("InterruptionClicker initialized. Load FML and click Interrupt button to trigger HALT.");
    }

    /* ---------------------------------------------------------------------- */
    /*             Inspired by greta.core.intentions.FMLFileReader              */
    /* ---------------------------------------------------------------------- */

    private XMLParser fmlParser = XML.createParser();
    private static String markup = "fml-apml";

    /**
     * Loads an FML file.<br> The communicative intentions in the specified file
     * will be send to all {@code IntentionPerformer} added with the
     * {@link #addIntentionPerformer(greta.core.intentions.IntentionPerformer) add}
     * function.<br> The base file name of the FML file is used as
     * {@code requestId} parameter when calling the
     * {@link greta.core.intentions.IntentionPerformer#performIntentions(java.util.List, greta.core.util.id.ID, greta.core.util.Mode) performIntentions}
     * function.
     *
     * @param fmlFileName the name of the file to load
     * @return The ID of the generated event
     */
    public ID load(String fmlFileName) {

        //get the base file name to use it as requestId
        String base = (new File(fmlFileName)).getName().replaceAll("\\.xml$", "");

        //get the intentions of the FML file
        fmlParser.setValidating(true);
        XMLTree fml = fmlParser.parseFile(fmlFileName);
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml, characterManager);

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

        //send to all IntentionPerformer added
        for (IntentionPerformer performer : intentionPerformers) {
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
                        fmlParser.setValidating(false);
                        return fmlParser.parseFile(pathName.getAbsolutePath()).getName().equalsIgnoreCase(markup);
                    } catch (Exception e) {
                    }
                }
                return false;
            }
        };
    }

    /* ---------------------------------------------------------------------- */
    /*               Inspired by greta.core.utilx.gui.OpenAndLoad               */
    /* ---------------------------------------------------------------------- */

    private Method loadMethod;
    private Object loader;

    public void setMainFileName(String fileName) {
        fmlFileNameTextField.setText(fileName);
    }

    public String getMainFileName() {
        return fmlFileNameTextField.getText();
    }

    protected void send(String fileName) throws IllegalAccessException {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        if (loadMethod != null) {
            try {
                loadMethod.invoke(loader, fileName);
            } catch (InvocationTargetException ex) {
                ex.getCause().printStackTrace();
            }
        } else {
            System.out.println("load is null");
        }
    }

    public void setLoader(Object loader) {
        this.loader = loader;
        try {
            loadMethod = loader.getClass().getMethod("load", String.class);
        } catch (Exception ex) {
            System.err.println("Can not find method load(String) in " + loader.getClass().getCanonicalName());
        }
        try {
            Method getFileFilterMethod = loader.getClass().getMethod("getFileFilter");
            final java.io.FileFilter ff = (java.io.FileFilter) getFileFilterMethod.invoke(loader);
            fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || ff.accept(f);
                }

                @Override
                public String getDescription() {
                    return InterruptionClicker.this.loader.getClass().getSimpleName() + " Files";
                }
            });

        } catch (Exception ex) {
        }
    }

    /* ---------------------------------------------------------------------- */
    /*                            IntentionEmitter                            */
    /* ---------------------------------------------------------------------- */

    @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        intentionPerformers.add(performer);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        intentionPerformers.remove(performer);
    }

    /* ---------------------------------------------------------------------- */
    /*                           CharacterDependent                           */
    /* ---------------------------------------------------------------------- */
    
    @Override
    public void onCharacterChanged() {
        // Nothing to do
    }


    /* ---------------------------------------------------------------------- */
    /*                                  GUI                                   */
    /* ---------------------------------------------------------------------- */

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileChooser = new javax.swing.JFileChooser();
        fileChooser.setCurrentDirectory(new File("./"));
        fmlFilePanel = new javax.swing.JPanel();
        fmlLabel = new javax.swing.JLabel();
        fmlFileNameLabel = new javax.swing.JLabel();
        fmlFileNameTextField = new javax.swing.JTextField();
        fmlFileOpenButton = new javax.swing.JButton();
        interruptionPanel = new javax.swing.JPanel();
        testInterruptionButton = new javax.swing.JButton();
        mainSeparator2 = new javax.swing.JSeparator();
        mainSeparator3 = new javax.swing.JSeparator();
        mainSeparator4 = new javax.swing.JSeparator();

        setMinimumSize(new java.awt.Dimension(555, 640));

        fmlLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        fmlLabel.setText("FML:");

        fmlFileNameLabel.setText("File:");

        fmlFileOpenButton.setText("Open");
        fmlFileOpenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fmlFileOpenButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout fmlFilePanelLayout = new javax.swing.GroupLayout(fmlFilePanel);
        fmlFilePanel.setLayout(fmlFilePanelLayout);
        fmlFilePanelLayout.setHorizontalGroup(
            fmlFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fmlFilePanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(fmlLabel)
                .addGap(18, 18, 18)
                .addComponent(fmlFileNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fmlFileNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fmlFileOpenButton)
                .addGap(0, 0, 0))
        );
        fmlFilePanelLayout.setVerticalGroup(
            fmlFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fmlFilePanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(fmlFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fmlLabel)
                    .addComponent(fmlFileNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fmlFileOpenButton)
                    .addComponent(fmlFileNameLabel))
                .addGap(0, 0, 0))
        );

        testInterruptionButton.setText("Interrupt");
        testInterruptionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testInterruptionButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout interruptionPanelLayout = new javax.swing.GroupLayout(interruptionPanel);
        interruptionPanel.setLayout(interruptionPanelLayout);
        interruptionPanelLayout.setHorizontalGroup(
            interruptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(interruptionPanelLayout.createSequentialGroup()
                .addGap(119, 119, 119)
                .addComponent(testInterruptionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        interruptionPanelLayout.setVerticalGroup(
            interruptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, interruptionPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(testInterruptionButton))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainSeparator3)
                    .addComponent(mainSeparator4)
                    .addComponent(fmlFilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(82, 82, 82)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mainSeparator2)
                            .addComponent(interruptionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fmlFilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(interruptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mainSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(77, 77, 77)
                .addComponent(mainSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(mainSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Open button action - Browse and load FML file
     */
    private void fmlFileOpenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fmlFileOpenButtonActionPerformed
        fileChooser.setLocale(Locale.getDefault());
        fileChooser.updateUI();
        if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            this.fmlFileNameTextField.setText(file.getAbsolutePath());
            
            try {
                // Immediately load and play the FML
                send(file.getAbsolutePath());
            } catch (IllegalAccessException ex) {
                Logger.getLogger(InterruptionClicker.class.getName()).log(Level.SEVERE, null, ex);
            }
            Logs.info("FML loaded and playing: " + file.getName());
        }
    }//GEN-LAST:event_fmlFileOpenButtonActionPerformed

    /**
     * Interrupt button action - Trigger HALT immediately
     */
    private void testInterruptionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testInterruptionButtonActionPerformed
        
        // Check if speech is actually playing
        Speech currentPlayingSpeech = Speech.getCurrentPlayingScheduledSpeech(false);
        if (currentPlayingSpeech == null) {
            Logs.warning("No speech is currently playing. Load FML first.");
            return;
        }
        
        Logs.info("Interrupting speech NOW with HALT mode...");
        
        try {
            // Create an empty speech intention for HALT interruption
            String interruptionFML = 
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                "<fml-apml>\n" +
                "\t<bml>\n" +
                "\t\t<speech id=\"s1\" start=\"0.0\" language=\"english\" text=\"\" voice=\"cereproc\">\n" +
                "\n" +
                "\t\t</speech>\n" +
                "\t</bml>\n" +
                "\t<fml>\n" +
                "\t</fml>\n" +
                "</fml-apml>\n";
            
            // Write to temporary file
            File tempFile = new File("InterruptionClicker_tempfile.xml");
            FileWriter writer = new FileWriter(tempFile);
            writer.write(interruptionFML);
            writer.close();
            
            // Parse the interruption FML
            fmlParser.setValidating(true);
            XMLTree fml = fmlParser.parseFile(tempFile.getPath());
            List<Intention> intentions = FMLTranslator.FMLToIntentions(fml, characterManager);
            
            // Create mode with HALT interruption
            Mode mode = FMLTranslator.getDefaultFMLMode();
            mode.setCompositionType("replace");
            mode.setReactionType("HALT");
            mode.setReactionDuration("SHORT");
            mode.setSocialAttitude("neutral");
            
            ID id = IDProvider.createID("InterruptionClicker_HALT");
            
            // Send the interruption to all IntentionPerformers
            for (IntentionPerformer performer : intentionPerformers) {
                performer.performIntentions(intentions, id, mode);
            }
            
            // Clean up temp file
            tempFile.delete();
            
            Logs.info("Interruption triggered successfully with HALT.");
            
        } catch (Exception ex) {
            Logs.error("Error triggering interruption: " + ex.getMessage());
            ex.printStackTrace();
        }
    }//GEN-LAST:event_testInterruptionButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JLabel fmlFileNameLabel;
    private javax.swing.JTextField fmlFileNameTextField;
    private javax.swing.JButton fmlFileOpenButton;
    private javax.swing.JPanel fmlFilePanel;
    private javax.swing.JLabel fmlLabel;
    private javax.swing.JPanel interruptionPanel;
    private javax.swing.JSeparator mainSeparator2;
    private javax.swing.JSeparator mainSeparator3;
    private javax.swing.JSeparator mainSeparator4;
    private javax.swing.JButton testInterruptionButton;
    // End of variables declaration//GEN-END:variables

    @Override
    protected void finalize() throws Throwable {
        getCharacterManager().remove(this);
        super.finalize();
    }
}
