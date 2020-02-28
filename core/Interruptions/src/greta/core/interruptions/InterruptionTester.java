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
import greta.core.interruptions.reactions.BehaviorType;
import greta.core.interruptions.reactions.InterruptionReaction;
import greta.core.interruptions.reactions.InterruptionReactionEmitter;
import greta.core.interruptions.reactions.InterruptionReactionImpl;
import greta.core.interruptions.reactions.InterruptionReactionParameters;
import greta.core.interruptions.reactions.InterruptionReactionPerformer;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
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

/**
 *
 * @author Brice Donval
 * @author Angelo Cafaro
 */
public class InterruptionTester extends javax.swing.JFrame implements IntentionEmitter, InterruptionReactionEmitter, CharacterDependent {

    private ArrayList<IntentionPerformer> intentionPerformers = new ArrayList<>();
    private ArrayList<InterruptionReactionPerformer> interruptionReactionPerformers = new ArrayList<>();
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
     * Creates new form InterruptionTester
     */
    public InterruptionTester(CharacterManager cm) {
        setCharacterManager(cm);
        Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH);

        initComponents();
        initPanels(interruptionOnOffCheckBox.isSelected());

        setLoader(this);
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

    public void setReactionReplanFileName(String fileName) {
        reactionReplanFMLFileNameTextField.setText(fileName);
    }

    public String getReactionReplanFileName() {
        return reactionReplanFMLFileNameTextField.getText();
    }

    protected void send(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        if (loadMethod != null) {
            try {
                loadMethod.invoke(loader, fileName);
            } catch (InvocationTargetException ex) {
                ex.getCause().printStackTrace();
            } catch (Exception ex) {
                System.err.println("Can not invoke method load(String) on " + loader.getClass().getCanonicalName());
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
                    return InterruptionTester.this.loader.getClass().getSimpleName() + " Files";
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
    /*                      InterruptionReactionEmitter                       */
    /* ---------------------------------------------------------------------- */

    @Override
    public void addInterruptionReactionPerformer(InterruptionReactionPerformer performer) {
        interruptionReactionPerformers.add(performer);
    }

    @Override
    public void removeInterruptionReactionPerformer(InterruptionReactionPerformer performer) {
        interruptionReactionPerformers.remove(performer);
    }

    /* ---------------------------------------------------------------------- */
    /*                           CharacterDependent                           */
    /* ---------------------------------------------------------------------- */

    @Override
    public void onCharacterChanged() {
        initPanels(interruptionOnOffCheckBox.isSelected());
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
        mainSeparator1 = new javax.swing.JSeparator();
        interruptionPanel = new javax.swing.JPanel();
        interruptionLabel = new javax.swing.JLabel();
        interruptionOnOffCheckBox = new javax.swing.JCheckBox();
        interruptionTimeLabel = new javax.swing.JLabel();
        interruptionTimeSpinner = new javax.swing.JSpinner();
        _debugAudioOnlyModeCheckBox = new javax.swing.JCheckBox();
        mainSeparator2 = new javax.swing.JSeparator();
        reactionPanel = new javax.swing.JPanel();
        reactionLabel = new javax.swing.JLabel();
        reactionTypeLabel = new javax.swing.JLabel();
        reactionTypeComboBox = new javax.swing.JComboBox();
        reactionDurationLabel = new javax.swing.JLabel();
        reactionDurationComboBox = new javax.swing.JComboBox();
        reactionReplanPanel = new javax.swing.JPanel();
        reactionReplanFMLFileNameLabel = new javax.swing.JLabel();
        reactionReplanFMLFileNameTextField = new javax.swing.JTextField();
        reactionReplanFMLFileOpenButton = new javax.swing.JButton();
        mainSeparator3 = new javax.swing.JSeparator();
        reactionBehaviorPanel = new javax.swing.JPanel();
        reactionBehaviorLabel = new javax.swing.JLabel();
        reactionBehaviorModalityLabel = new javax.swing.JLabel();
        reactionBehaviorBehaviorLabel = new javax.swing.JLabel();
        reactionBehaviorAmplitudeLabel = new javax.swing.JLabel();
        reactionBehaviorDurationLabel = new javax.swing.JLabel();
        reactionBehaviorModalityArrowLabel = new javax.swing.JLabel();
        reactionBehaviorBehaviorArrowLabel = new javax.swing.JLabel();
        reactionBehaviorAmplitudeArrowLabel = new javax.swing.JLabel();
        reactionBehaviorDurationArrowLabel = new javax.swing.JLabel();
        reactionBehaviorHeadLabel = new javax.swing.JLabel();
        reactionBehaviorHeadTiltCheckBox = new javax.swing.JCheckBox();
        reactionBehaviorHeadTiltAmplitudeSpinner = new javax.swing.JSpinner();
        reactionBehaviorHeadTiltDurationSpinner = new javax.swing.JSpinner();
        reactionBehaviorHeadNodTossCheckBox = new javax.swing.JCheckBox();
        reactionBehaviorHeadNodTossAmplitudeSpinner = new javax.swing.JSpinner();
        reactionBehaviorHeadNodTossDurationSpinner = new javax.swing.JSpinner();
        reactionBehaviorSeparator1 = new javax.swing.JSeparator();
        reactionBehaviorEyesLabel = new javax.swing.JLabel();
        reactionBehaviorEyesLidsCloseCheckBox = new javax.swing.JCheckBox();
        reactionBehaviorEyesLidsCloseAmplitudeSpinner = new javax.swing.JSpinner();
        reactionBehaviorEyesLidsCloseDurationSpinner = new javax.swing.JSpinner();
        reactionBehaviorEyesBrowsCheckBox = new javax.swing.JCheckBox();
        reactionBehaviorEyesBrowsAmplitudeSpinner = new javax.swing.JSpinner();
        reactionBehaviorEyesBrowsDurationSpinner = new javax.swing.JSpinner();
        reactionBehaviorEyesSqueezeCheckBox = new javax.swing.JCheckBox();
        reactionBehaviorEyesSqueezeAmplitudeSpinner = new javax.swing.JSpinner();
        reactionBehaviorEyesSqueezeDurationSpinner = new javax.swing.JSpinner();
        reactionBehaviorSeparator2 = new javax.swing.JSeparator();
        reactionBehaviorSmileLabel = new javax.swing.JLabel();
        reactionBehaviorSmileSmileCheckBox = new javax.swing.JCheckBox();
        reactionBehaviorSmileSmileAmplitudeSpinner = new javax.swing.JSpinner();
        reactionBehaviorSmileSmileDurationSpinner = new javax.swing.JSpinner();
        reactionBehaviorSeparator3 = new javax.swing.JSeparator();
        reactionBehaviorGesturesLabel = new javax.swing.JLabel();
        reactionBehaviorGestureHoldCheckBox = new javax.swing.JCheckBox();
        reactionBehaviorGestureHoldAmplitudeSpinner = new javax.swing.JSpinner();
        reactionBehaviorGestureHoldDurationSpinner = new javax.swing.JSpinner();
        reactionBehaviorGestureRetractCheckBox = new javax.swing.JCheckBox();
        reactionBehaviorGestureRetractAmplitudeSpinner = new javax.swing.JSpinner();
        reactionBehaviorGestureRetractDurationSpinner = new javax.swing.JSpinner();
        reactionBehaviorSeparator4 = new javax.swing.JSeparator();
        reactionBehaviorShouldersLabel = new javax.swing.JLabel();
        reactionBehaviorShouldersUpForwardCheckBox = new javax.swing.JCheckBox();
        reactionBehaviorShouldersUpForwardAmplitudeSpinner = new javax.swing.JSpinner();
        reactionBehaviorShouldersUpForwardDurationSpinner = new javax.swing.JSpinner();
        mainSeparator4 = new javax.swing.JSeparator();
        testInterruptionButton = new javax.swing.JButton();

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
                .addComponent(fmlFileNameTextField)
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

        interruptionLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        interruptionLabel.setText("Interruption:");

        interruptionOnOffCheckBox.setText("ON / OFF");
        interruptionOnOffCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                interruptionOnOffCheckBoxItemStateChanged(evt);
            }
        });

        interruptionTimeLabel.setText("Time (sec):");

        interruptionTimeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.5d, 0.01d, 100.0d, 0.1d));
        interruptionTimeSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(interruptionTimeSpinner, "0.00"));

        _debugAudioOnlyModeCheckBox.setText("Audio Only (debug)");
        _debugAudioOnlyModeCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _debugAudioOnlyModeCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout interruptionPanelLayout = new javax.swing.GroupLayout(interruptionPanel);
        interruptionPanel.setLayout(interruptionPanelLayout);
        interruptionPanelLayout.setHorizontalGroup(
            interruptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(interruptionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(interruptionLabel)
                .addGap(18, 18, 18)
                .addComponent(interruptionOnOffCheckBox)
                .addGap(18, 18, 18)
                .addComponent(interruptionTimeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(interruptionTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(_debugAudioOnlyModeCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        interruptionPanelLayout.setVerticalGroup(
            interruptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(interruptionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(interruptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(interruptionLabel)
                    .addComponent(interruptionTimeLabel)
                    .addComponent(interruptionTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_debugAudioOnlyModeCheckBox)
                    .addComponent(interruptionOnOffCheckBox))
                .addGap(0, 0, 0))
        );

        reactionLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        reactionLabel.setText("Reaction:");

        reactionTypeLabel.setText("Type:");

        reactionTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "HALT", "OVERLAP", "REPLAN" }));
        reactionTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reactionTypeComboBoxActionPerformed(evt);
            }
        });

        reactionDurationLabel.setText("Duration:");

        reactionDurationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "EXTRA_SHORT", "SHORT", "MEDIUM", "LONG" }));

        reactionReplanFMLFileNameLabel.setText("With File:");

        reactionReplanFMLFileOpenButton.setText("Open");
        reactionReplanFMLFileOpenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reactionReplanFMLFileOpenButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout reactionReplanPanelLayout = new javax.swing.GroupLayout(reactionReplanPanel);
        reactionReplanPanel.setLayout(reactionReplanPanelLayout);
        reactionReplanPanelLayout.setHorizontalGroup(
            reactionReplanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reactionReplanPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(reactionReplanFMLFileNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reactionReplanFMLFileNameTextField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reactionReplanFMLFileOpenButton)
                .addGap(0, 0, 0))
        );
        reactionReplanPanelLayout.setVerticalGroup(
            reactionReplanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reactionReplanPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(reactionReplanPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reactionReplanFMLFileNameLabel)
                    .addComponent(reactionReplanFMLFileNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionReplanFMLFileOpenButton))
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout reactionPanelLayout = new javax.swing.GroupLayout(reactionPanel);
        reactionPanel.setLayout(reactionPanelLayout);
        reactionPanelLayout.setHorizontalGroup(
            reactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reactionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(reactionLabel)
                .addGap(18, 18, 18)
                .addGroup(reactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reactionDurationLabel)
                    .addComponent(reactionTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reactionDurationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(reactionReplanPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        reactionPanelLayout.setVerticalGroup(
            reactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reactionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(reactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(reactionPanelLayout.createSequentialGroup()
                        .addGroup(reactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(reactionLabel)
                            .addComponent(reactionTypeLabel)
                            .addComponent(reactionTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(reactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(reactionDurationLabel)
                            .addComponent(reactionDurationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(reactionReplanPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        reactionBehaviorLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        reactionBehaviorLabel.setText("Reaction Behavior:");

        reactionBehaviorModalityLabel.setText("Modality:");

        reactionBehaviorBehaviorLabel.setText("Behavior:");

        reactionBehaviorAmplitudeLabel.setText("Amplitude:");

        reactionBehaviorDurationLabel.setText("Duration (sec):");

        reactionBehaviorModalityArrowLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        reactionBehaviorModalityArrowLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        reactionBehaviorModalityArrowLabel.setText("<html>&#x21E3;</html>");

        reactionBehaviorBehaviorArrowLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        reactionBehaviorBehaviorArrowLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        reactionBehaviorBehaviorArrowLabel.setText("<html>&#x21E3;</html>");

        reactionBehaviorAmplitudeArrowLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        reactionBehaviorAmplitudeArrowLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        reactionBehaviorAmplitudeArrowLabel.setText("<html>&#x21E3;</html>");

        reactionBehaviorDurationArrowLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        reactionBehaviorDurationArrowLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        reactionBehaviorDurationArrowLabel.setText("<html>&#x21E3;</html>");

        reactionBehaviorHeadLabel.setText("Head:");

        reactionBehaviorHeadTiltCheckBox.setText("Tilt");
        reactionBehaviorHeadTiltCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                reactionBehaviorHeadTiltCheckBoxItemStateChanged(evt);
            }
        });

        reactionBehaviorHeadTiltAmplitudeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 1.0d, 0.1d));
        reactionBehaviorHeadTiltAmplitudeSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorHeadTiltAmplitudeSpinner, "0.00"));

        reactionBehaviorHeadTiltDurationSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 2.0d, 0.1d));
        reactionBehaviorHeadTiltDurationSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorHeadTiltDurationSpinner, "0.00"));

        reactionBehaviorHeadNodTossCheckBox.setText("Nod / Toss");
        reactionBehaviorHeadNodTossCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                reactionBehaviorHeadNodTossCheckBoxItemStateChanged(evt);
            }
        });

        reactionBehaviorHeadNodTossAmplitudeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, -1.0d, 1.0d, 0.1d));
        reactionBehaviorHeadNodTossAmplitudeSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorHeadNodTossAmplitudeSpinner, "0.00"));

        reactionBehaviorHeadNodTossDurationSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 2.0d, 0.1d));
        reactionBehaviorHeadNodTossDurationSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorHeadNodTossDurationSpinner, "0.00"));

        reactionBehaviorEyesLabel.setText("Eyes:");

        reactionBehaviorEyesLidsCloseCheckBox.setText("Lids close");
        reactionBehaviorEyesLidsCloseCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                reactionBehaviorEyesLidsCloseCheckBoxItemStateChanged(evt);
            }
        });

        reactionBehaviorEyesLidsCloseAmplitudeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 1.0d, 0.1d));
        reactionBehaviorEyesLidsCloseAmplitudeSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorEyesLidsCloseAmplitudeSpinner, "0.00"));

        reactionBehaviorEyesLidsCloseDurationSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 2.0d, 0.1d));
        reactionBehaviorEyesLidsCloseDurationSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorEyesLidsCloseDurationSpinner, "0.00"));

        reactionBehaviorEyesBrowsCheckBox.setText("Brows");
        reactionBehaviorEyesBrowsCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                reactionBehaviorEyesBrowsCheckBoxItemStateChanged(evt);
            }
        });

        reactionBehaviorEyesBrowsAmplitudeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, -1.0d, 1.0d, 0.1d));
        reactionBehaviorEyesBrowsAmplitudeSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorEyesBrowsAmplitudeSpinner, "0.00"));

        reactionBehaviorEyesBrowsDurationSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 2.0d, 0.1d));
        reactionBehaviorEyesBrowsDurationSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorEyesBrowsDurationSpinner, "0.00"));

        reactionBehaviorEyesSqueezeCheckBox.setText("Squeeze");
        reactionBehaviorEyesSqueezeCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                reactionBehaviorEyesSqueezeCheckBoxItemStateChanged(evt);
            }
        });

        reactionBehaviorEyesSqueezeAmplitudeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 1.0d, 0.1d));
        reactionBehaviorEyesSqueezeAmplitudeSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorEyesSqueezeAmplitudeSpinner, "0.00"));

        reactionBehaviorEyesSqueezeDurationSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 2.0d, 0.1d));
        reactionBehaviorEyesSqueezeDurationSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorEyesSqueezeDurationSpinner, "0.00"));

        reactionBehaviorSmileLabel.setText("Smile:");

        reactionBehaviorSmileSmileCheckBox.setText("Smile");
        reactionBehaviorSmileSmileCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                reactionBehaviorSmileSmileCheckBoxItemStateChanged(evt);
            }
        });

        reactionBehaviorSmileSmileAmplitudeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, -1.0d, 1.0d, 0.1d));
        reactionBehaviorSmileSmileAmplitudeSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorSmileSmileAmplitudeSpinner, "0.00"));

        reactionBehaviorSmileSmileDurationSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 2.0d, 0.1d));
        reactionBehaviorSmileSmileDurationSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorSmileSmileDurationSpinner, "0.00"));

        reactionBehaviorGesturesLabel.setText("Gestures:");

        reactionBehaviorGestureHoldCheckBox.setText("Hold");
        reactionBehaviorGestureHoldCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                reactionBehaviorGestureHoldCheckBoxItemStateChanged(evt);
            }
        });

        reactionBehaviorGestureHoldAmplitudeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 100.0d, 0.1d));
        reactionBehaviorGestureHoldAmplitudeSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorGestureHoldAmplitudeSpinner, "0.00"));

        reactionBehaviorGestureHoldDurationSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 2.0d, 0.1d));
        reactionBehaviorGestureHoldDurationSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorGestureHoldDurationSpinner, "0.00"));

        reactionBehaviorGestureRetractCheckBox.setText("Retract");
        reactionBehaviorGestureRetractCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                reactionBehaviorGestureRetractCheckBoxItemStateChanged(evt);
            }
        });

        reactionBehaviorGestureRetractAmplitudeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 100.0d, 0.1d));
        reactionBehaviorGestureRetractAmplitudeSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorGestureRetractAmplitudeSpinner, "0.00"));

        reactionBehaviorGestureRetractDurationSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 2.0d, 0.1d));
        reactionBehaviorGestureRetractDurationSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorGestureRetractDurationSpinner, "0.00"));

        reactionBehaviorShouldersLabel.setText("Shoulders:");

        reactionBehaviorShouldersUpForwardCheckBox.setText("Up / Forward");
        reactionBehaviorShouldersUpForwardCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                reactionBehaviorShouldersUpForwardCheckBoxItemStateChanged(evt);
            }
        });

        reactionBehaviorShouldersUpForwardAmplitudeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 1.0d, 0.1d));
        reactionBehaviorShouldersUpForwardAmplitudeSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorShouldersUpForwardAmplitudeSpinner, "0.00"));

        reactionBehaviorShouldersUpForwardDurationSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 1.0d, 0.1d));
        reactionBehaviorShouldersUpForwardDurationSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(reactionBehaviorShouldersUpForwardDurationSpinner, "0.00"));

        javax.swing.GroupLayout reactionBehaviorPanelLayout = new javax.swing.GroupLayout(reactionBehaviorPanel);
        reactionBehaviorPanel.setLayout(reactionBehaviorPanelLayout);
        reactionBehaviorPanelLayout.setHorizontalGroup(
            reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reactionBehaviorPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(reactionBehaviorLabel)
                .addGap(30, 30, 30)
                .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(reactionBehaviorSeparator3)
                    .addComponent(reactionBehaviorSeparator4)
                    .addComponent(reactionBehaviorSeparator2)
                    .addComponent(reactionBehaviorSeparator1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reactionBehaviorPanelLayout.createSequentialGroup()
                        .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(reactionBehaviorModalityLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(reactionBehaviorModalityArrowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(reactionBehaviorSmileLabel)
                                .addComponent(reactionBehaviorEyesLabel)
                                .addComponent(reactionBehaviorHeadLabel))
                            .addComponent(reactionBehaviorGesturesLabel)
                            .addComponent(reactionBehaviorShouldersLabel))
                        .addGap(50, 50, 50)
                        .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(reactionBehaviorHeadTiltCheckBox)
                            .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(reactionBehaviorBehaviorArrowLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(reactionBehaviorBehaviorLabel))
                            .addComponent(reactionBehaviorGestureHoldCheckBox)
                            .addComponent(reactionBehaviorSmileSmileCheckBox)
                            .addComponent(reactionBehaviorEyesLidsCloseCheckBox)
                            .addComponent(reactionBehaviorShouldersUpForwardCheckBox)
                            .addComponent(reactionBehaviorHeadNodTossCheckBox)
                            .addComponent(reactionBehaviorGestureRetractCheckBox)
                            .addComponent(reactionBehaviorEyesBrowsCheckBox)
                            .addComponent(reactionBehaviorEyesSqueezeCheckBox))
                        .addGap(20, 20, 20)
                        .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(reactionBehaviorAmplitudeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorHeadTiltAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorAmplitudeArrowLabel)
                            .addComponent(reactionBehaviorEyesLidsCloseAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorSmileSmileAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorShouldersUpForwardAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorGestureRetractAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorGestureHoldAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorEyesSqueezeAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorEyesBrowsAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorHeadNodTossAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(50, 50, 50)
                        .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(reactionBehaviorGestureRetractDurationSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorGestureHoldDurationSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorSmileSmileDurationSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorEyesBrowsDurationSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorEyesLidsCloseDurationSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorHeadNodTossDurationSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorHeadTiltDurationSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorDurationArrowLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(reactionBehaviorDurationLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorShouldersUpForwardDurationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(reactionBehaviorEyesSqueezeDurationSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 0, 0))
        );
        reactionBehaviorPanelLayout.setVerticalGroup(
            reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reactionBehaviorPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reactionBehaviorLabel)
                    .addComponent(reactionBehaviorModalityLabel)
                    .addComponent(reactionBehaviorBehaviorLabel)
                    .addComponent(reactionBehaviorAmplitudeLabel)
                    .addComponent(reactionBehaviorDurationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reactionBehaviorModalityArrowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionBehaviorBehaviorArrowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionBehaviorAmplitudeArrowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionBehaviorDurationArrowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reactionBehaviorHeadLabel)
                    .addComponent(reactionBehaviorHeadTiltCheckBox)
                    .addComponent(reactionBehaviorHeadTiltAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionBehaviorHeadTiltDurationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reactionBehaviorHeadNodTossCheckBox)
                    .addComponent(reactionBehaviorHeadNodTossAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionBehaviorHeadNodTossDurationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(reactionBehaviorSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reactionBehaviorEyesLabel)
                    .addComponent(reactionBehaviorEyesLidsCloseCheckBox)
                    .addComponent(reactionBehaviorEyesLidsCloseAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionBehaviorEyesLidsCloseDurationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reactionBehaviorEyesBrowsCheckBox)
                    .addComponent(reactionBehaviorEyesBrowsAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionBehaviorEyesBrowsDurationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reactionBehaviorEyesSqueezeDurationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionBehaviorEyesSqueezeAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionBehaviorEyesSqueezeCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(reactionBehaviorSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reactionBehaviorSmileLabel)
                    .addComponent(reactionBehaviorSmileSmileCheckBox)
                    .addComponent(reactionBehaviorSmileSmileAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionBehaviorSmileSmileDurationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(reactionBehaviorSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reactionBehaviorGesturesLabel)
                    .addComponent(reactionBehaviorGestureHoldCheckBox)
                    .addComponent(reactionBehaviorGestureHoldAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionBehaviorGestureHoldDurationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reactionBehaviorGestureRetractCheckBox)
                    .addComponent(reactionBehaviorGestureRetractAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionBehaviorGestureRetractDurationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(reactionBehaviorSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(reactionBehaviorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reactionBehaviorShouldersLabel)
                    .addComponent(reactionBehaviorShouldersUpForwardCheckBox)
                    .addComponent(reactionBehaviorShouldersUpForwardAmplitudeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reactionBehaviorShouldersUpForwardDurationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        testInterruptionButton.setText("Test");
        testInterruptionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testInterruptionButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reactionBehaviorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainSeparator1)
                    .addComponent(mainSeparator2)
                    .addComponent(mainSeparator3)
                    .addComponent(mainSeparator4)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(testInterruptionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(fmlFilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(reactionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(interruptionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fmlFilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mainSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(interruptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mainSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(reactionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mainSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(reactionBehaviorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(mainSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(testInterruptionButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void initPanels(boolean enabled) {
        initFMLFilePanel(enabled);
        initInterruptionPanel(enabled);
        initReactionPanel(enabled);
        initReactionBehaviorPanel(enabled);
    }

    private void setPanelComponentsEnabled(javax.swing.JPanel panel, boolean enabled) {
        for (java.awt.Component component : panel.getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void initFMLFilePanel(boolean enabled) {
        // Nothing to do.
    }

    private void initInterruptionPanel(boolean enabled) {
        interruptionTimeLabel.setEnabled(enabled);
        interruptionTimeSpinner.setEnabled(enabled);
        _debugAudioOnlyModeCheckBox.setEnabled(enabled);
    }

    private void initReactionPanel(boolean enabled) {
        setPanelComponentsEnabled(reactionPanel, enabled);
        initReactionReplanPanel(enabled);
    }

    private void initReactionReplanPanel(boolean enabled) {
        setPanelComponentsEnabled(reactionReplanPanel, enabled);
        reactionReplanPanel.setVisible(reactionTypeComboBox.getSelectedItem().equals("REPLAN"));
    }

    private void initReactionBehaviorPanel(boolean enabled) {

        setPanelComponentsEnabled(reactionBehaviorPanel, enabled);

        setReactionBehaviorHeadTiltAreaEnabled(enabled && reactionBehaviorHeadTiltCheckBox.isSelected());
        setReactionBehaviorHeadNodTossAreaEnabled(enabled && reactionBehaviorHeadNodTossCheckBox.isSelected());
        setReactionBehaviorEyesLidsCloseAreaEnabled(enabled && reactionBehaviorEyesLidsCloseCheckBox.isSelected());
        setReactionBehaviorEyesBrowsAreaEnabled(enabled && reactionBehaviorEyesBrowsCheckBox.isSelected());
        setReactionBehaviorEyesSqueezeAreaEnabled(enabled && reactionBehaviorEyesSqueezeCheckBox.isSelected());
        setReactionBehaviorSmileSmileAreaEnabled(enabled && reactionBehaviorSmileSmileCheckBox.isSelected());
        setReactionBehaviorGestureHoldAreaEnabled(enabled && reactionBehaviorGestureHoldCheckBox.isSelected());
        setReactionBehaviorGestureRetractAreaEnabled(enabled && reactionBehaviorGestureRetractCheckBox.isSelected());
        setReactionBehaviorShouldersUpForwardAreaEnabled(enabled && reactionBehaviorShouldersUpForwardCheckBox.isSelected());

        reactionBehaviorGestureHoldAmplitudeSpinner.setVisible(false);
        reactionBehaviorGestureRetractAmplitudeSpinner.setVisible(false);
    }

    private void setReactionBehaviorHeadTiltAreaEnabled(boolean enabled) {
        reactionBehaviorHeadTiltAmplitudeSpinner.setEnabled(enabled);
        reactionBehaviorHeadTiltDurationSpinner.setEnabled(enabled);
    }

    private void setReactionBehaviorHeadNodTossAreaEnabled(boolean enabled) {
        reactionBehaviorHeadNodTossAmplitudeSpinner.setEnabled(enabled);
        reactionBehaviorHeadNodTossDurationSpinner.setEnabled(enabled);
    }

    private void setReactionBehaviorEyesLidsCloseAreaEnabled(boolean enabled) {
        reactionBehaviorEyesLidsCloseAmplitudeSpinner.setEnabled(enabled);
        reactionBehaviorEyesLidsCloseDurationSpinner.setEnabled(enabled);
    }

    private void setReactionBehaviorEyesBrowsAreaEnabled(boolean enabled) {
        reactionBehaviorEyesBrowsAmplitudeSpinner.setEnabled(enabled);
        reactionBehaviorEyesBrowsDurationSpinner.setEnabled(enabled);
    }

    private void setReactionBehaviorEyesSqueezeAreaEnabled(boolean enabled) {
        reactionBehaviorEyesSqueezeAmplitudeSpinner.setEnabled(enabled);
        reactionBehaviorEyesSqueezeDurationSpinner.setEnabled(enabled);
    }

    private void setReactionBehaviorSmileSmileAreaEnabled(boolean enabled) {
        reactionBehaviorSmileSmileAmplitudeSpinner.setEnabled(enabled);
        reactionBehaviorSmileSmileDurationSpinner.setEnabled(enabled);
    }

    private void setReactionBehaviorGestureHoldAreaEnabled(boolean enabled) {
        reactionBehaviorGestureHoldAmplitudeSpinner.setEnabled(enabled);
        reactionBehaviorGestureHoldDurationSpinner.setEnabled(enabled);

        if (!getCharacterManager().getValueString("INTERRUPTION_GESTURE_HOLD_DUR").trim().isEmpty()) {
            reactionBehaviorGestureHoldDurationSpinner.setValue(getCharacterManager().getValueDouble("INTERRUPTION_GESTURE_HOLD_DUR"));
        } else {
            reactionBehaviorGestureHoldDurationSpinner.setValue(0);
        }
    }

    private void setReactionBehaviorGestureRetractAreaEnabled(boolean enabled) {
        reactionBehaviorGestureRetractAmplitudeSpinner.setEnabled(enabled);
        reactionBehaviorGestureRetractDurationSpinner.setEnabled(enabled);

        if (!getCharacterManager().getValueString("INTERRUPTION_GESTURE_RETRACT_DUR").trim().isEmpty()) {
            reactionBehaviorGestureRetractDurationSpinner.setValue(getCharacterManager().getValueDouble("INTERRUPTION_GESTURE_RETRACT_DUR"));
        } else {
            reactionBehaviorGestureRetractDurationSpinner.setValue(0);
        }
    }

    private void setReactionBehaviorShouldersUpForwardAreaEnabled(boolean enabled) {
        reactionBehaviorShouldersUpForwardAmplitudeSpinner.setEnabled(enabled);
        reactionBehaviorShouldersUpForwardDurationSpinner.setEnabled(enabled);
    }

    private void fmlFileOpenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fmlFileOpenButtonActionPerformed
        fileChooser.setLocale(Locale.getDefault());
        fileChooser.updateUI();
        if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            this.fmlFileNameTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_fmlFileOpenButtonActionPerformed

    private void interruptionOnOffCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_interruptionOnOffCheckBoxItemStateChanged
        initPanels(evt.getStateChange() == java.awt.event.ItemEvent.SELECTED);
    }//GEN-LAST:event_interruptionOnOffCheckBoxItemStateChanged

    private void _debugAudioOnlyModeCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event__debugAudioOnlyModeCheckBoxItemStateChanged
        InterruptionManager._debugAudioOnlyMode = (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED);
    }//GEN-LAST:event__debugAudioOnlyModeCheckBoxItemStateChanged

    private void reactionTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reactionTypeComboBoxActionPerformed
        reactionReplanPanel.setVisible(reactionTypeComboBox.getSelectedItem().equals("REPLAN"));
    }//GEN-LAST:event_reactionTypeComboBoxActionPerformed

    private void reactionReplanFMLFileOpenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reactionReplanFMLFileOpenButtonActionPerformed
        fileChooser.setLocale(Locale.getDefault());
        fileChooser.updateUI();
        if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            this.reactionReplanFMLFileNameTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_reactionReplanFMLFileOpenButtonActionPerformed

    private void reactionBehaviorHeadTiltCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_reactionBehaviorHeadTiltCheckBoxItemStateChanged
        setReactionBehaviorHeadTiltAreaEnabled(evt.getStateChange() == java.awt.event.ItemEvent.SELECTED);
    }//GEN-LAST:event_reactionBehaviorHeadTiltCheckBoxItemStateChanged

    private void reactionBehaviorHeadNodTossCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_reactionBehaviorHeadNodTossCheckBoxItemStateChanged
        setReactionBehaviorHeadNodTossAreaEnabled(evt.getStateChange() == java.awt.event.ItemEvent.SELECTED);
    }//GEN-LAST:event_reactionBehaviorHeadNodTossCheckBoxItemStateChanged

    private void reactionBehaviorEyesLidsCloseCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_reactionBehaviorEyesLidsCloseCheckBoxItemStateChanged
        setReactionBehaviorEyesLidsCloseAreaEnabled(evt.getStateChange() == java.awt.event.ItemEvent.SELECTED);
    }//GEN-LAST:event_reactionBehaviorEyesLidsCloseCheckBoxItemStateChanged

    private void reactionBehaviorEyesBrowsCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_reactionBehaviorEyesBrowsCheckBoxItemStateChanged
        setReactionBehaviorEyesBrowsAreaEnabled(evt.getStateChange() == java.awt.event.ItemEvent.SELECTED);
    }//GEN-LAST:event_reactionBehaviorEyesBrowsCheckBoxItemStateChanged

    private void reactionBehaviorEyesSqueezeCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_reactionBehaviorEyesSqueezeCheckBoxItemStateChanged
        setReactionBehaviorEyesSqueezeAreaEnabled(evt.getStateChange() == java.awt.event.ItemEvent.SELECTED);
    }//GEN-LAST:event_reactionBehaviorEyesSqueezeCheckBoxItemStateChanged

    private void reactionBehaviorSmileSmileCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_reactionBehaviorSmileSmileCheckBoxItemStateChanged
        setReactionBehaviorSmileSmileAreaEnabled(evt.getStateChange() == java.awt.event.ItemEvent.SELECTED);
    }//GEN-LAST:event_reactionBehaviorSmileSmileCheckBoxItemStateChanged

    private void reactionBehaviorGestureHoldCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_reactionBehaviorGestureHoldCheckBoxItemStateChanged
        setReactionBehaviorGestureHoldAreaEnabled(evt.getStateChange() == java.awt.event.ItemEvent.SELECTED);
    }//GEN-LAST:event_reactionBehaviorGestureHoldCheckBoxItemStateChanged

    private void reactionBehaviorGestureRetractCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_reactionBehaviorGestureRetractCheckBoxItemStateChanged
        setReactionBehaviorGestureRetractAreaEnabled(evt.getStateChange() == java.awt.event.ItemEvent.SELECTED);
    }//GEN-LAST:event_reactionBehaviorGestureRetractCheckBoxItemStateChanged

    private void reactionBehaviorShouldersUpForwardCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_reactionBehaviorShouldersUpForwardCheckBoxItemStateChanged
        setReactionBehaviorShouldersUpForwardAreaEnabled(evt.getStateChange() == java.awt.event.ItemEvent.SELECTED);
    }//GEN-LAST:event_reactionBehaviorShouldersUpForwardCheckBoxItemStateChanged

    private void testInterruptionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testInterruptionButtonActionPerformed

        if (interruptionOnOffCheckBox.isSelected()) {

            File interruptionFile = new File("InterruptionTester_tempfile.xml");

            String interruptionFileContent = "";
            interruptionFileContent += "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n";
            interruptionFileContent += "<fml-apml>\n";
            interruptionFileContent += "\t<bml>\n";
            interruptionFileContent += "\t\t<speech id=\"s1\" start=\"0.0\" language=\"english\" text=\"\" voice=\"cereproc\">\n";
            interruptionFileContent += "\n";
            interruptionFileContent += "\t\t</speech>\n";
            interruptionFileContent += "\t</bml>\n";
            interruptionFileContent += "\t<fml>\n";
            interruptionFileContent += "\t</fml>\n";
            interruptionFileContent += "</fml-apml>\n";

            if (reactionTypeComboBox.getSelectedItem().equals("HALT") || reactionTypeComboBox.getSelectedItem().equals("OVERLAP")) {
                FileWriter out;
                try {
                    out = new FileWriter(interruptionFile);
                    out.write(interruptionFileContent);
                    out.close();
                } catch (IOException ex) {
                }
            } else if (reactionTypeComboBox.getSelectedItem().equals("REPLAN")) {
                interruptionFile = new File(reactionReplanFMLFileNameTextField.getText());
            }

            //get the base file name to use it as requestId
            String base = interruptionFile.getName().replaceAll("\\.xml$", "");

            //get the intentions of the FML file
            fmlParser.setValidating(true);
            XMLTree fml = fmlParser.parseFile(interruptionFile.getPath());
            List<Intention> intentions = FMLTranslator.FMLToIntentions(fml, characterManager);

            Mode mode = FMLTranslator.getDefaultFMLMode();
            mode.setCompositionType("replace");
            mode.setReactionType(reactionTypeComboBox.getSelectedItem().toString());
            mode.setReactionDuration(reactionDurationComboBox.getSelectedItem().toString());
            mode.setSocialAttitude("neutral");

            ID id = IDProvider.createID(base);

            List<InterruptionReaction> interruptionReactions = new ArrayList<>();

            if (reactionBehaviorHeadTiltCheckBox.isSelected()) {
                float amplitudeValue = (float) (double) reactionBehaviorHeadTiltAmplitudeSpinner.getValue();
                float durationValue = (float) (double) reactionBehaviorHeadTiltDurationSpinner.getValue();
                InterruptionReactionParameters parameters = new InterruptionReactionParameters(amplitudeValue, durationValue);
                interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.HEAD_TILT, parameters));
            }
            if (reactionBehaviorHeadNodTossCheckBox.isSelected()) {
                float amplitudeValue = (float) (double) reactionBehaviorHeadNodTossAmplitudeSpinner.getValue();
                float durationValue = (float) (double) reactionBehaviorHeadNodTossDurationSpinner.getValue();
                InterruptionReactionParameters parameters = new InterruptionReactionParameters(amplitudeValue, durationValue);
                interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.HEAD_NOD_TOSS, parameters));
            }

            if (reactionBehaviorEyesLidsCloseCheckBox.isSelected()) {
                float amplitudeValue = (float) (double) reactionBehaviorEyesLidsCloseAmplitudeSpinner.getValue();
                float durationValue = (float) (double) reactionBehaviorEyesLidsCloseDurationSpinner.getValue();
                InterruptionReactionParameters parameters = new InterruptionReactionParameters(amplitudeValue, durationValue);
                interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.EYES_LIDS_CLOSE, parameters));
            }
            if (reactionBehaviorEyesBrowsCheckBox.isSelected()) {
                float amplitudeValue = (float) (double) reactionBehaviorEyesBrowsAmplitudeSpinner.getValue();
                float durationValue = (float) (double) reactionBehaviorEyesBrowsDurationSpinner.getValue();
                InterruptionReactionParameters parameters = new InterruptionReactionParameters(amplitudeValue, durationValue);
                interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.EYES_BROWS, parameters));
            }
            if (reactionBehaviorEyesSqueezeCheckBox.isSelected()) {
                float amplitudeValue = (float) (double) reactionBehaviorEyesSqueezeAmplitudeSpinner.getValue();
                float durationValue = (float) (double) reactionBehaviorEyesSqueezeDurationSpinner.getValue();
                InterruptionReactionParameters parameters = new InterruptionReactionParameters(amplitudeValue, durationValue);
                interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.EYES_SQUEEZE, parameters));
            }

            if (reactionBehaviorSmileSmileCheckBox.isSelected()) {
                float amplitudeValue = (float) (double) reactionBehaviorSmileSmileAmplitudeSpinner.getValue();
                float durationValue = (float) (double) reactionBehaviorSmileSmileDurationSpinner.getValue();
                InterruptionReactionParameters parameters = new InterruptionReactionParameters(amplitudeValue, durationValue);
                interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.SMILE, parameters));
            }

            if (reactionBehaviorGestureHoldCheckBox.isSelected()) {
                float amplitudeValue = (float) (double) reactionBehaviorGestureHoldAmplitudeSpinner.getValue();
                float durationValue = (float) (double) reactionBehaviorGestureHoldDurationSpinner.getValue();
                InterruptionReactionParameters parameters = new InterruptionReactionParameters(amplitudeValue, durationValue);
                interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.GESTURE_HOLD, parameters));
            }

            if (reactionBehaviorGestureRetractCheckBox.isSelected()) {
                float amplitudeValue = (float) (double) reactionBehaviorGestureRetractAmplitudeSpinner.getValue();
                float durationValue = (float) (double) reactionBehaviorGestureRetractDurationSpinner.getValue();
                InterruptionReactionParameters parameters = new InterruptionReactionParameters(amplitudeValue, durationValue);
                interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.GESTURE_RETRACT, parameters));
            }
            if (reactionBehaviorShouldersUpForwardCheckBox.isSelected()) {
                float amplitudeValue = (float) (double) reactionBehaviorShouldersUpForwardAmplitudeSpinner.getValue();
                float durationValue = (float) (double) reactionBehaviorShouldersUpForwardDurationSpinner.getValue();
                InterruptionReactionParameters parameters = new InterruptionReactionParameters(amplitudeValue, durationValue);
                interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.SHOULDERS_UP_FORWARD, parameters));
            }

            send(fmlFileNameTextField.getText());

            Double interruptionTimeSpinnerValue = (Double) interruptionTimeSpinner.getValue();
            int interruptionTime = (int) (interruptionTimeSpinnerValue * 1000);

            if (interruptionTime != 0) {

                greta.core.util.time.Timer.sleep(interruptionTime);

                //send to all InterruptionReactionPerformer added
                for (InterruptionReactionPerformer performer : interruptionReactionPerformers) {
                    performer.performInterruptionReactions(interruptionReactions, id);
                }

                //send to all IntentionPerformer added
                for (IntentionPerformer performer : intentionPerformers) {
                    performer.performIntentions(intentions, id, mode);
                }
            }

            new File("InterruptionTester_tempfile.xml").delete();

        } else {
            send(fmlFileNameTextField.getText());
        }
    }//GEN-LAST:event_testInterruptionButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox _debugAudioOnlyModeCheckBox;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JLabel fmlFileNameLabel;
    private javax.swing.JTextField fmlFileNameTextField;
    private javax.swing.JButton fmlFileOpenButton;
    private javax.swing.JPanel fmlFilePanel;
    private javax.swing.JLabel fmlLabel;
    private javax.swing.JLabel interruptionLabel;
    private javax.swing.JCheckBox interruptionOnOffCheckBox;
    private javax.swing.JPanel interruptionPanel;
    private javax.swing.JLabel interruptionTimeLabel;
    private javax.swing.JSpinner interruptionTimeSpinner;
    private javax.swing.JSeparator mainSeparator1;
    private javax.swing.JSeparator mainSeparator2;
    private javax.swing.JSeparator mainSeparator3;
    private javax.swing.JSeparator mainSeparator4;
    private javax.swing.JLabel reactionBehaviorAmplitudeArrowLabel;
    private javax.swing.JLabel reactionBehaviorAmplitudeLabel;
    private javax.swing.JLabel reactionBehaviorBehaviorArrowLabel;
    private javax.swing.JLabel reactionBehaviorBehaviorLabel;
    private javax.swing.JLabel reactionBehaviorDurationArrowLabel;
    private javax.swing.JLabel reactionBehaviorDurationLabel;
    private javax.swing.JSpinner reactionBehaviorEyesBrowsAmplitudeSpinner;
    private javax.swing.JCheckBox reactionBehaviorEyesBrowsCheckBox;
    private javax.swing.JSpinner reactionBehaviorEyesBrowsDurationSpinner;
    private javax.swing.JLabel reactionBehaviorEyesLabel;
    private javax.swing.JSpinner reactionBehaviorEyesLidsCloseAmplitudeSpinner;
    private javax.swing.JCheckBox reactionBehaviorEyesLidsCloseCheckBox;
    private javax.swing.JSpinner reactionBehaviorEyesLidsCloseDurationSpinner;
    private javax.swing.JSpinner reactionBehaviorEyesSqueezeAmplitudeSpinner;
    private javax.swing.JCheckBox reactionBehaviorEyesSqueezeCheckBox;
    private javax.swing.JSpinner reactionBehaviorEyesSqueezeDurationSpinner;
    private javax.swing.JSpinner reactionBehaviorGestureHoldAmplitudeSpinner;
    private javax.swing.JCheckBox reactionBehaviorGestureHoldCheckBox;
    private javax.swing.JSpinner reactionBehaviorGestureHoldDurationSpinner;
    private javax.swing.JSpinner reactionBehaviorGestureRetractAmplitudeSpinner;
    private javax.swing.JCheckBox reactionBehaviorGestureRetractCheckBox;
    private javax.swing.JSpinner reactionBehaviorGestureRetractDurationSpinner;
    private javax.swing.JLabel reactionBehaviorGesturesLabel;
    private javax.swing.JLabel reactionBehaviorHeadLabel;
    private javax.swing.JSpinner reactionBehaviorHeadNodTossAmplitudeSpinner;
    private javax.swing.JCheckBox reactionBehaviorHeadNodTossCheckBox;
    private javax.swing.JSpinner reactionBehaviorHeadNodTossDurationSpinner;
    private javax.swing.JSpinner reactionBehaviorHeadTiltAmplitudeSpinner;
    private javax.swing.JCheckBox reactionBehaviorHeadTiltCheckBox;
    private javax.swing.JSpinner reactionBehaviorHeadTiltDurationSpinner;
    private javax.swing.JLabel reactionBehaviorLabel;
    private javax.swing.JLabel reactionBehaviorModalityArrowLabel;
    private javax.swing.JLabel reactionBehaviorModalityLabel;
    private javax.swing.JPanel reactionBehaviorPanel;
    private javax.swing.JSeparator reactionBehaviorSeparator1;
    private javax.swing.JSeparator reactionBehaviorSeparator2;
    private javax.swing.JSeparator reactionBehaviorSeparator3;
    private javax.swing.JSeparator reactionBehaviorSeparator4;
    private javax.swing.JLabel reactionBehaviorShouldersLabel;
    private javax.swing.JSpinner reactionBehaviorShouldersUpForwardAmplitudeSpinner;
    private javax.swing.JCheckBox reactionBehaviorShouldersUpForwardCheckBox;
    private javax.swing.JSpinner reactionBehaviorShouldersUpForwardDurationSpinner;
    private javax.swing.JLabel reactionBehaviorSmileLabel;
    private javax.swing.JSpinner reactionBehaviorSmileSmileAmplitudeSpinner;
    private javax.swing.JCheckBox reactionBehaviorSmileSmileCheckBox;
    private javax.swing.JSpinner reactionBehaviorSmileSmileDurationSpinner;
    private javax.swing.JComboBox reactionDurationComboBox;
    private javax.swing.JLabel reactionDurationLabel;
    private javax.swing.JLabel reactionLabel;
    private javax.swing.JPanel reactionPanel;
    private javax.swing.JLabel reactionReplanFMLFileNameLabel;
    private javax.swing.JTextField reactionReplanFMLFileNameTextField;
    private javax.swing.JButton reactionReplanFMLFileOpenButton;
    private javax.swing.JPanel reactionReplanPanel;
    private javax.swing.JComboBox reactionTypeComboBox;
    private javax.swing.JLabel reactionTypeLabel;
    private javax.swing.JButton testInterruptionButton;
    // End of variables declaration//GEN-END:variables

    @Override
    protected void finalize() throws Throwable {
        getCharacterManager().remove(this);
        super.finalize();
    }
}
