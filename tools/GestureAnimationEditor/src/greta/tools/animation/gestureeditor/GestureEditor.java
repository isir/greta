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
package greta.tools.animation.gestureeditor;

import greta.core.keyframes.GestureKeyframe;
import greta.core.keyframes.Keyframe;
import greta.core.keyframes.KeyframeEmitter;
import greta.core.keyframes.KeyframePerformer;
import greta.core.repositories.Gestuary;
import greta.core.repositories.SignalEntry;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.signals.gesture.GesturePose;
import greta.core.signals.gesture.GestureSignal;
import greta.core.signals.gesture.Hand;
import greta.core.signals.gesture.Position;
import greta.core.signals.gesture.SymbolicPosition;
import greta.core.signals.gesture.TouchPosition;
import greta.core.signals.gesture.UniformPosition;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.enums.Side;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.math.Quaternion;
import greta.core.utilx.gui.ToolBox;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Jing Huang
 */
public class GestureEditor extends javax.swing.JFrame implements SignalEmitter, KeyframeEmitter, CharacterDependent {

    private final List<KeyframePerformer> _keyframePerformers = new ArrayList<KeyframePerformer>();
    private final List<SignalPerformer> _signalPerformers = new ArrayList<SignalPerformer>();

    private String _reference = null;
    private NewGestureDialog _newGestureDialog = new NewGestureDialog(this, true);

    /**
     * Creates new form GestureEditor
     */
    private boolean _ready = false;
    private boolean canSendKeyFrames = false;
    private int newDefaultGestureAdded = -1;
    private CharacterManager cm;


    public GestureEditor(CharacterManager cm) {
        this.cm = cm;
        initComponents();
        initField(spatialityValue);
        initField(temporalityValue);
        initField(fluidityValue);
        initField(powerValue);
        initField(stiffnessValue);
        initField(tensionValue);

        cm.add(this);

        _ready = true;
        canSendKeyFrames = true;

        loadGesturesList();

        spatialityValue.setText(String.valueOf(spatialityParameter.getValue()));
        temporalityValue.setText(String.valueOf(temporalityParameter.getValue()));
        fluidityValue.setText(String.valueOf(fluidityParameter.getValue()));
        powerValue.setText(String.valueOf(powerParameter.getValue()));
        stiffnessValue.setText(String.valueOf(stiffnessParameter.getValue()));
        tensionValue.setText(String.valueOf(tensionParameter.getValue()));
    }

    /**
     * load gesturelist
     */
    private void loadGesturesList() {
        Gestuary.global_gestuary.refreshAll();
        existingGesturesMenu.removeAllItems();

        List<SignalEntry<GestureSignal>> signals = Gestuary.global_gestuary.getAll();
        Collections.sort(signals, new Comparator<SignalEntry<GestureSignal>>() {
            @Override
            public int compare(SignalEntry<GestureSignal> t, SignalEntry<GestureSignal> t1) {
                String ref = t.getParamName();
                String ref1 = t1.getParamName();
                return ref.compareToIgnoreCase(ref1);
            }
        });

        for (SignalEntry<GestureSignal> signal : signals) {
            existingGesturesMenu.addItem(signal.getParamName());
        }
        if (_reference != null) {
            existingGesturesMenu.setSelectedItem(_reference);
        } else {
            existingGesturesMenu.setSelectedIndex(0);
        }
    }

    public void sendCurrentKeyFrame() {
        if (!canSendKeyFrames) {
            return;
        }
        int idx = tabbedPane.getSelectedIndex();
        if (idx == -1) {
            return;// no selection
        }
        //System.out.println(idx);
        GestureSignal gest = Gestuary.global_gestuary.getSignal(_reference);
        if (gest == null) {
            System.err.println("GestureEditor: " + _reference + " does not exist");
            return;
        }
        GesturePose phase = gest.getPhases().get(idx);
        if (phase == null) {
            System.out.println("GestureEditor: phase" + idx + " does not exist");
            return;
        }
        double time = greta.core.util.time.Timer.getTime();
        List<Keyframe> keyframes = new ArrayList<Keyframe>();
        //left
        if (phase.getLeftHand() != null) {
            Hand leftHand = new Hand(phase.getLeftHand());
            if (leftHand.getWristOrientation() == null) {
                Quaternion orientation = new Quaternion();
                for (int iter = 1; iter <= idx; iter++) {
                    GesturePose prev = gest.getPhases().get(idx - iter);
                    if (prev.getLeftHand() != null && prev.getLeftHand().getWristOrientation() != null) {
                        orientation = new Quaternion(prev.getLeftHand().getWristOrientation());
                        break;
                    }
                }
                leftHand.setWristOrientation(orientation);
            }
            if (leftHand.getHandShape() == null) {
                String shape = "empty";
                for (int iter = 1; iter <= idx; iter++) {
                    GesturePose prev = gest.getPhases().get(idx - iter);
                    if (prev.getLeftHand() != null && prev.getLeftHand().getHandShape() != null) {
                        shape = prev.getLeftHand().getHandShape();
                        break;
                    }
                }
                leftHand.setHandShape(shape);
            }
            leftHand.getPosition().applySpacial(getSPC());

            GestureKeyframe keyframe1 = new GestureKeyframe(gest.getId(),
                    "",
                    leftHand.getTrajectory(),
                    time,
                    time,
                    leftHand, "", false);

            GestureKeyframe keyframe2 = new GestureKeyframe(gest.getId(),
                    "",
                    leftHand.getTrajectory(),
                    time + 1,
                    time + 1,
                    leftHand, "", false);

            keyframes.add(keyframe1);
            keyframes.add(keyframe2);
        }
        //right
        if (phase.getRightHand() != null) {
            Hand rightHand = new Hand(phase.getRightHand());
            if (rightHand.getWristOrientation() == null) {
                Quaternion orientation = new Quaternion();
                for (int iter = 1; iter <= idx; iter++) {
                    GesturePose prev = gest.getPhases().get(idx - iter);
                    if (prev.getRightHand() != null && prev.getRightHand().getWristOrientation() != null) {
                        orientation = new Quaternion(prev.getRightHand().getWristOrientation());
                        break;
                    }
                }
                rightHand.setWristOrientation(orientation);
            }
            if (rightHand.getHandShape() == null) {
                String shape = "empty";
                for (int iter = 1; iter <= idx; iter++) {
                    GesturePose prev = gest.getPhases().get(idx - iter);
                    if (prev.getRightHand() != null && prev.getRightHand().getHandShape() != null) {
                        shape = prev.getRightHand().getHandShape();
                        break;
                    }
                }
                rightHand.setHandShape(shape);
            }
            rightHand.getPosition().applySpacial(getSPC());

            GestureKeyframe keyframe1 = new GestureKeyframe(gest.getId(),
                    "",
                    rightHand.getTrajectory(),
                    time,
                    time,
                    rightHand, "", false);

            GestureKeyframe keyframe2 = new GestureKeyframe(gest.getId(),
                    "",
                    rightHand.getTrajectory(),
                    time + 1,
                    time + 1,
                    rightHand, "", false);

            keyframes.add(keyframe1);
            keyframes.add(keyframe2);
        }
        ID id = IDProvider.createID("GestureEditor");
        for (KeyframePerformer perf : _keyframePerformers) {
            // TODO : Mode management in progress
            perf.performKeyframes(keyframes, id);
        }
//        System.out.println("GestureEditor: phase" + idx + " key frame sent");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel1 = new javax.swing.JPanel();
        gestureLabel = new javax.swing.JLabel();
        gestureNameLabel = new javax.swing.JLabel();
        existingGesturesMenu = new javax.swing.JComboBox();
        sendKeyFrameButton = new javax.swing.JButton();
        sendGestureButton = new javax.swing.JButton();
        resetGestureButton = new javax.swing.JButton();
        reloadGestuaryButton = new javax.swing.JButton();
        splitPane = new javax.swing.JSplitPane();
        tabbedPane = new javax.swing.JTabbedPane();
        panel2 = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        panel3 = new javax.swing.JPanel();
        spatialityLabel = new javax.swing.JLabel();
        temporalityLabel = new javax.swing.JLabel();
        fluidityLabel = new javax.swing.JLabel();
        powerLabel = new javax.swing.JLabel();
        stiffnessLabel = new javax.swing.JLabel();
        tensionLabel = new javax.swing.JLabel();
        spatialityValue = new javax.swing.JFormattedTextField();
        temporalityValue = new javax.swing.JFormattedTextField();
        fluidityValue = new javax.swing.JFormattedTextField();
        powerValue = new javax.swing.JFormattedTextField();
        stiffnessValue = new javax.swing.JFormattedTextField();
        tensionValue = new javax.swing.JFormattedTextField();
        spatialityParameter = new javax.swing.JSlider();
        temporalityParameter = new javax.swing.JSlider();
        fluidityParameter = new javax.swing.JSlider();
        powerParameter = new javax.swing.JSlider();
        stiffnessParameter = new javax.swing.JSlider();
        tensionParameter = new javax.swing.JSlider();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newGestureMenuItem = new javax.swing.JMenuItem();
        saveGestureMenuItem = new javax.swing.JMenuItem();
        gestureMenu = new javax.swing.JMenu();
        addKeyframeMenuItem = new javax.swing.JMenuItem();
        deleteKeyframeMenuItem = new javax.swing.JMenuItem();
        separator = new javax.swing.JPopupMenu.Separator();
        generateMirrorGestureMenuItem = new javax.swing.JMenuItem();

        setTitle("GestureEditor");
        setMinimumSize(new java.awt.Dimension(600, 600));

        gestureLabel.setText("Current Gesture: ");

        existingGesturesMenu.setMaximumRowCount(20);
        existingGesturesMenu.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                existingGesturesMenuItemStateChanged(evt);
            }
        });

        sendKeyFrameButton.setText("Send Current Keyframe");
        sendKeyFrameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendKeyFrameButtonActionPerformed(evt);
            }
        });

        sendGestureButton.setText("Send Gesture");
        sendGestureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendGestureButtonActionPerformed(evt);
            }
        });

        resetGestureButton.setText("Reset Current Gesture");
        resetGestureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetGestureButtonActionPerformed(evt);
            }
        });

        reloadGestuaryButton.setText("Reload Gestuary");
        reloadGestuaryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadGestuaryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(sendKeyFrameButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sendGestureButton))
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(gestureLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(gestureNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(resetGestureButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reloadGestuaryButton))
                    .addComponent(existingGesturesMenu, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(existingGesturesMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(gestureLabel)
                        .addComponent(gestureNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(sendGestureButton)
                        .addComponent(sendKeyFrameButton))
                    .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(reloadGestuaryButton)
                        .addComponent(resetGestureButton)))
                .addContainerGap())
        );

        splitPane.setBorder(null);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(1.0);

        tabbedPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tabbedPaneMousePressed(evt);
            }
        });

        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 575, Short.MAX_VALUE)
        );
        panel2Layout.setVerticalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 254, Short.MAX_VALUE)
        );

        tabbedPane.addTab("tab1", panel2);

        splitPane.setTopComponent(tabbedPane);

        scrollPane.setBorder(null);
        scrollPane.setMinimumSize(new java.awt.Dimension(337, 213));

        panel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Expressivity Parameters"));
        panel3.setMinimumSize(new java.awt.Dimension(337, 213));

        spatialityLabel.setText("Spatiality:");

        temporalityLabel.setText("Temporality:");

        fluidityLabel.setText("Fluidity");

        powerLabel.setText("Power:");

        stiffnessLabel.setText("Stiffness:");

        tensionLabel.setText("Tension:");

        spatialityValue.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                valueFocusLost(evt);
            }
        });
        spatialityValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueActionPerformed(evt);
            }
        });
        spatialityValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                spatialityValueKeyTyped(evt);
            }
        });

        temporalityValue.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                valueFocusLost(evt);
            }
        });
        temporalityValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueActionPerformed(evt);
            }
        });
        temporalityValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                temporalityValueKeyTyped(evt);
            }
        });

        fluidityValue.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                valueFocusLost(evt);
            }
        });
        fluidityValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueActionPerformed(evt);
            }
        });
        fluidityValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fluidityValueKeyTyped(evt);
            }
        });

        powerValue.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                valueFocusLost(evt);
            }
        });
        powerValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueActionPerformed(evt);
            }
        });
        powerValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                powerValueKeyTyped(evt);
            }
        });

        stiffnessValue.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                valueFocusLost(evt);
            }
        });
        stiffnessValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueActionPerformed(evt);
            }
        });
        stiffnessValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                stiffnessValueKeyTyped(evt);
            }
        });

        tensionValue.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                valueFocusLost(evt);
            }
        });
        tensionValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueActionPerformed(evt);
            }
        });
        tensionValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tensionValueKeyTyped(evt);
            }
        });

        spatialityParameter.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                parameterStateChanged(evt);
            }
        });

        temporalityParameter.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                parameterStateChanged(evt);
            }
        });

        fluidityParameter.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                parameterStateChanged(evt);
            }
        });

        powerParameter.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                parameterStateChanged(evt);
            }
        });

        stiffnessParameter.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                parameterStateChanged(evt);
            }
        });

        tensionParameter.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                parameterStateChanged(evt);
            }
        });

        javax.swing.GroupLayout panel3Layout = new javax.swing.GroupLayout(panel3);
        panel3.setLayout(panel3Layout);
        panel3Layout.setHorizontalGroup(
            panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(temporalityLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spatialityLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fluidityLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(powerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(stiffnessLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tensionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spatialityValue, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(temporalityValue, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fluidityValue, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(powerValue, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stiffnessValue, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tensionValue, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tensionParameter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(stiffnessParameter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(powerParameter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fluidityParameter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(temporalityParameter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spatialityParameter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panel3Layout.setVerticalGroup(
            panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(spatialityLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(spatialityValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(spatialityParameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(temporalityLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(temporalityValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(temporalityParameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(fluidityLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(fluidityValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(fluidityParameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(powerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(powerValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(powerParameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(stiffnessLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(stiffnessValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(stiffnessParameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tensionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tensionValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(tensionParameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        scrollPane.setViewportView(panel3);

        splitPane.setBottomComponent(scrollPane);

        fileMenu.setText("File");

        newGestureMenuItem.setText("New Gesture");
        newGestureMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newGestureMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newGestureMenuItem);

        saveGestureMenuItem.setText("Save Current Gesture");
        saveGestureMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveGestureMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveGestureMenuItem);

        menuBar.add(fileMenu);

        gestureMenu.setText("Gesture");

        addKeyframeMenuItem.setText("Add Keyframe");
        addKeyframeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addKeyframeMenuItemActionPerformed(evt);
            }
        });
        gestureMenu.add(addKeyframeMenuItem);

        deleteKeyframeMenuItem.setText("Delete Current Keyframe");
        deleteKeyframeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteKeyframeMenuItemActionPerformed(evt);
            }
        });
        gestureMenu.add(deleteKeyframeMenuItem);
        gestureMenu.add(separator);

        generateMirrorGestureMenuItem.setText("Generate Mirror Gesture");
        generateMirrorGestureMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateMirrorGestureMenuItemActionPerformed(evt);
            }
        });
        gestureMenu.add(generateMirrorGestureMenuItem);

        menuBar.add(gestureMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(splitPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(splitPane))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void initField(javax.swing.JFormattedTextField field) {
        field.setFormatterFactory(
                new javax.swing.text.DefaultFormatterFactory(
                        new javax.swing.text.NumberFormatter(
                                new java.text.DecimalFormat("#0")
                        )
                )
        );
        field.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    }

    private void newGestureMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newGestureMenuItemActionPerformed
        _newGestureDialog.setVisible(true);
        if (_newGestureDialog.isCreated()) {
            String id = _newGestureDialog.getNameValue();
            String cat = _newGestureDialog.getCategoryValue();
            GestureSignal gestureSiginal = new GestureSignal(id);
            GesturePose gesturePhase = new GesturePose();
            gestureSiginal.addPhase(gesturePhase);
            gestureSiginal.setCategory(cat);
            _reference = gestureSiginal.getCategory() + "=" + gestureSiginal.getId();

//            if (_ges.isDefaultGestuary()) {
//                Gestuary.global_gestuary.getDefaultDefinition().addParameter(new SignalEntry<GestureSignal>(_reference, gestureSiginal));
//            } else {
            Gestuary.global_gestuary.getCurrentDefinition().addParameter(new SignalEntry<GestureSignal>(_reference, gestureSiginal));
            //}

//            if (!Gestuary.global_gestuary.getDefaultDefinition().contains(_reference)) {
//                Gestuary.global_gestuary.getDefaultDefinition().addParameter(new SignalEntry<GestureSignal>(_reference, gestureSiginal));
//                newDefaultGestureAdded = true;
//            }
            existingGesturesMenu.addItem(_reference);
            existingGesturesMenu.setSelectedItem(_reference);
        }
    }//GEN-LAST:event_newGestureMenuItemActionPerformed

    private void saveGestureMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveGestureMenuItemActionPerformed
//        String ques;
//        if (_ges.isDefaultGestuary()) {
//            ques = "Would you like save the gest in Default Gestuary?";
//        } else {
//            ques = "Would you like save the gest for this personal gestuary?";
//        }
//        int n = JOptionPane.showConfirmDialog(this, ques, "Save", JOptionPane.YES_NO_OPTION);
//
//        if (n == JOptionPane.YES_OPTION) {
//
//        } else if (n == JOptionPane.NO_OPTION) {
//            return;
//        }

        Object[] possibilities = {"Default", "Character Specific", "Both"};
        String s = (String) JOptionPane.showInputDialog(
                this,
                "In which gestuary would you like to save this gesture?",
                "Save Dialog",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                "Default");
        if (s != null && s.length() > 0) {
            if (s.equals("Default")) {
                newDefaultGestureAdded = 1;
            } else if (s.equals("Character Specific")) {
                newDefaultGestureAdded = 2;
            } else if (s.equals("Both")) {
                newDefaultGestureAdded = 3;
            }
        }

        SignalEntry<GestureSignal> gest = Gestuary.global_gestuary.getCurrentDefinition().getParameter(_reference);
        if (gest == null) {
            gest = Gestuary.global_gestuary.getDefaultDefinition().getParameter(_reference);
        }

        if (newDefaultGestureAdded == 1) {
            Gestuary.global_gestuary.getDefaultDefinition().addParameter(gest);
            Gestuary.global_gestuary.saveDefaultDefinition();
            System.out.println("save in Defaut Definition");
        } else if (newDefaultGestureAdded == 2) {
            Gestuary.global_gestuary.getCurrentDefinition().addParameter(gest);
            Gestuary.global_gestuary.saveCurrentDefinition();
            System.out.println("save in Definition of person");
        } else if (newDefaultGestureAdded == 3) {
            Gestuary.global_gestuary.getDefaultDefinition().addParameter(gest);
            Gestuary.global_gestuary.saveDefaultDefinition();
            Gestuary.global_gestuary.getCurrentDefinition().addParameter(gest);
            Gestuary.global_gestuary.saveCurrentDefinition();
            System.out.println("save in both Definition of person and defaut");
        }

//        if (newDefaultGestureAdded) {
//            Gestuary.global_gestuary.saveDefaultDefinition();
//            newDefaultGestureAdded = false;
//        }
        loadGesturesList();
    }//GEN-LAST:event_saveGestureMenuItemActionPerformed

    private void tabbedPaneMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabbedPaneMousePressed
        sendCurrentKeyFrame();
    }//GEN-LAST:event_tabbedPaneMousePressed

    private void addKeyframeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addKeyframeMenuItemActionPerformed
        final int index = tabbedPane.getSelectedIndex();
        if (index != -1) {
            insertPhase(index);
        }
    }//GEN-LAST:event_addKeyframeMenuItemActionPerformed

    private void deleteKeyframeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteKeyframeMenuItemActionPerformed
        final int index = tabbedPane.getSelectedIndex();
        if (index != -1) {
            deletePhase(index);
        }
    }//GEN-LAST:event_deleteKeyframeMenuItemActionPerformed

    private void generateMirrorGestureMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateMirrorGestureMenuItemActionPerformed
        Pattern p = Pattern.compile("^(.+)=(.+)_(L|R|L(_.+)|R(_.+))$");
        Matcher m = p.matcher(_reference);
        if (m.matches()) {

            GestureSignal originalGesture = Gestuary.global_gestuary.getSignal(_reference);

            String firstPartOfGestureId = m.group(2);
            String lastPartOfGestureId = (m.group(4) != null) ? m.group(4) : ((m.group(5) != null) ? m.group(5) : "");

            boolean originalGestureSideIsLeft = m.group(3).startsWith("L");

            String mirrorGestureCategory = m.group(1);
            String mirrorGestureId = firstPartOfGestureId + (originalGestureSideIsLeft ? "_R" : "_L") + lastPartOfGestureId;
            String mirrorGestureReference = mirrorGestureCategory + "=" + mirrorGestureId;

            boolean mirrorGestureAlreadyExists = (Gestuary.global_gestuary.getSignal(mirrorGestureReference) != null);
            if (mirrorGestureAlreadyExists) {
                int option = JOptionPane.showConfirmDialog(this, "The mirror of this gesture already exists and will be overwritten!\nAre you sure to continue?", null, JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            GestureSignal mirrorGesture = originalGesture.generateMirrorGesture(mirrorGestureId);
            mirrorGesture.setReference(mirrorGestureReference);

            _reference = mirrorGestureReference;
            Gestuary.global_gestuary.getCurrentDefinition().addParameter(new SignalEntry<GestureSignal>(_reference, mirrorGesture));
            if (!mirrorGestureAlreadyExists) {
                existingGesturesMenu.addItem(_reference);
            }
            existingGesturesMenu.setSelectedItem(_reference);
        }
    }//GEN-LAST:event_generateMirrorGestureMenuItemActionPerformed

    private void resetGestureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetGestureButtonActionPerformed

        String id = "empty";
        String cat = "empty";
        GestureSignal gestureSiginal = new GestureSignal(id);
        GesturePose gesturePhase = new GesturePose();
        gestureSiginal.addPhase(gesturePhase);
        gestureSiginal.setCategory(cat);
        _reference = gestureSiginal.getCategory() + "=" + gestureSiginal.getId();

        if (Gestuary.global_gestuary.getSignal(_reference) == null) {
            Gestuary.global_gestuary.getCurrentDefinition().addParameter(new SignalEntry<GestureSignal>(_reference, gestureSiginal));
            existingGesturesMenu.addItem(_reference);
            existingGesturesMenu.setSelectedItem(_reference);
        }

        GestureSignal gest = Gestuary.global_gestuary.getSignal(_reference);
        if (gest == null) {
            System.err.println("GestureEditor: " + _reference + " does not exist");
            return;
        }
        List<Signal> Gsignals = new ArrayList<Signal>();
        GestureSignal gesture = new GestureSignal("newElementInGestureEditor");
        gesture.setReference(gest.getCategory() + "=" + gest.getId());
        gesture.getStart().setValue(0);
        Gsignals.add(gesture);
        Gestuary g = Gestuary.global_gestuary;
        ID id2 = IDProvider.createID("GestureEditor");
        for (SignalPerformer perf : _signalPerformers) {
            perf.performSignals(Gsignals, id2, new Mode(CompositionType.replace));
        }
    }//GEN-LAST:event_resetGestureButtonActionPerformed

    private void reloadGestuaryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadGestuaryButtonActionPerformed
        loadGesturesList();
    }//GEN-LAST:event_reloadGestuaryButtonActionPerformed

    private void sendKeyFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendKeyFrameButtonActionPerformed
        sendCurrentKeyFrame();
    }//GEN-LAST:event_sendKeyFrameButtonActionPerformed

    private void sendGestureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendGestureButtonActionPerformed

        GestureSignal gest = Gestuary.global_gestuary.getSignal(_reference);
        if (gest == null) {
            System.err.println("GestureEditor: " + _reference + " does not exist");
            return;
        }

        List<Signal> Gsignals = new ArrayList<Signal>();
        GestureSignal gesture = new GestureSignal("newElementInGestureEditor");
        gesture.setReference(gest.getCategory() + "=" + gest.getId());
        gesture.getStart().setValue(0);
        /*
         * gesture.getTimeMarker("ready").setValue(0.2);
         * gesture.getTimeMarker("stroke-start").setValue(0.3);
         * gesture.getTimeMarker("stroke-end").setValue(0.8);
         * gesture.getTimeMarker("relax").setValue(1.3);
         * gesture.getEnd().setValue(1.5);
         */
        gesture.setFLD(fluidityParameter.getValue() / 100.0);
        gesture.setPWR(powerParameter.getValue() / 100.0);
        gesture.setTMP(temporalityParameter.getValue() / 100.0);
        gesture.setSPC(getSPC());
        gesture.setTension(tensionParameter.getValue() / 100.0);
        Gsignals.add(gesture);
        Gestuary g = Gestuary.global_gestuary;
        ID id = IDProvider.createID("GestureEditor");
        for (SignalPerformer perf : _signalPerformers) {
            perf.performSignals(Gsignals, id, new Mode(CompositionType.replace));
        }
    }//GEN-LAST:event_sendGestureButtonActionPerformed

    private void existingGesturesMenuItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_existingGesturesMenuItemStateChanged
        canSendKeyFrames = false;
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if (!_ready) {
                canSendKeyFrames = true;
                return;
            }
            if (existingGesturesMenu.getItemCount() < 1) {
                canSendKeyFrames = true;
                return;
            }
            _reference = existingGesturesMenu.getSelectedItem().toString();
            GestureSignal gest = Gestuary.global_gestuary.getSignal(_reference);
            if (gest == null) {
                System.err.println("GestureEditor: " + _reference + " does not exist");
                canSendKeyFrames = true;
                return;
            }
            tabbedPane.removeAll();
            gestureNameLabel.setText(_reference);

            for (GesturePose phase : gest.getPhases()) {
                tabbedPane.addTab("stroke", buildTabForPhase(phase));
            }
            updatePhaseNames();

            boolean generateMirrorGestureMenuItemEnabled = _reference.matches("^.+=(.+)_(L|R|L(_.+)|R(_.+))$");
            generateMirrorGestureMenuItem.setEnabled(generateMirrorGestureMenuItemEnabled);
        }
        canSendKeyFrames = true;
    }//GEN-LAST:event_existingGesturesMenuItemStateChanged

    private void spatialityValueKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_spatialityValueKeyTyped
        ToolBox.checkIntegerInRange(evt, spatialityValue, 0, 100);
    }//GEN-LAST:event_spatialityValueKeyTyped

    private void temporalityValueKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_temporalityValueKeyTyped
        ToolBox.checkIntegerInRange(evt, temporalityValue, 0, 100);
    }//GEN-LAST:event_temporalityValueKeyTyped

    private void fluidityValueKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fluidityValueKeyTyped
        ToolBox.checkIntegerInRange(evt, fluidityValue, 0, 100);
    }//GEN-LAST:event_fluidityValueKeyTyped

    private void powerValueKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_powerValueKeyTyped
        ToolBox.checkIntegerInRange(evt, powerValue, 0, 100);
    }//GEN-LAST:event_powerValueKeyTyped

    private void stiffnessValueKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_stiffnessValueKeyTyped
        ToolBox.checkIntegerInRange(evt, stiffnessValue, 0, 100);
    }//GEN-LAST:event_stiffnessValueKeyTyped

    private void tensionValueKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tensionValueKeyTyped
        ToolBox.checkIntegerInRange(evt, tensionValue, 0, 100);
    }//GEN-LAST:event_tensionValueKeyTyped

    private void valueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueActionPerformed
        spatialityParameter.setValue(Integer.parseInt(spatialityValue.getText()));
        temporalityParameter.setValue(Integer.parseInt(temporalityValue.getText()));
        fluidityParameter.setValue(Integer.parseInt(fluidityValue.getText()));
        powerParameter.setValue(Integer.parseInt(powerValue.getText()));
        stiffnessParameter.setValue(Integer.parseInt(stiffnessValue.getText()));
        tensionParameter.setValue(Integer.parseInt(tensionValue.getText()));
    }//GEN-LAST:event_valueActionPerformed

    private void valueFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_valueFocusLost
        valueActionPerformed(null);
    }//GEN-LAST:event_valueFocusLost

    private void parameterStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_parameterStateChanged
        spatialityValue.setText(String.valueOf(spatialityParameter.getValue()));
        temporalityValue.setText(String.valueOf(temporalityParameter.getValue()));
        fluidityValue.setText(String.valueOf(fluidityParameter.getValue()));
        powerValue.setText(String.valueOf(powerParameter.getValue()));
        stiffnessValue.setText(String.valueOf(stiffnessParameter.getValue()));
        tensionValue.setText(String.valueOf(tensionParameter.getValue()));
        sendCurrentKeyFrame();
    }//GEN-LAST:event_parameterStateChanged

    private void updatePhaseNames() {
        if (tabbedPane.getTabCount() == 0) {
            return;
        }
        if (tabbedPane.getTabCount() == 1) {
            tabbedPane.setTitleAt(0, "STROKE");
            return;
        }
        tabbedPane.setTitleAt(0, "STROKE START");
        for (int i = 1; i < tabbedPane.getTabCount() - 1; ++i) {
            tabbedPane.setTitleAt(i, "STROKE");
        }
        tabbedPane.setTitleAt(tabbedPane.getTabCount() - 1, "STROKE END");
    }

    private void insertPhase(int idx) {
        GestureSignal gest = Gestuary.global_gestuary.getSignal(_reference);
        if (gest == null) {
            System.err.println("GestureEditor: " + _reference + " does not exist");
            return;
        }
        GesturePose gesturePhase = new GesturePose(gest.getPhases().get(idx));
        gest.getPhases().add(idx + 1, gesturePhase);
        tabbedPane.insertTab("STROKE", null, buildTabForPhase(gesturePhase), "", idx + 1);
        updatePhaseNames();
        System.out.println("GestureEditor: " + _reference + " insert phase: " + idx + 1);
    }

    private void deletePhase(int idx) {
        GestureSignal gest = Gestuary.global_gestuary.getSignal(_reference);
        if (gest == null) {
            System.err.println("GestureEditor: " + _reference + " does not exist");
            return;
        }
        tabbedPane.removeTabAt(idx);
        gest.getPhases().remove(idx);
        updatePhaseNames();
        System.out.println("GestureEditor: " + _reference + " remove phase: " + idx);
    }

    JPanel buildTabForPhase(GesturePose phase) {
        if (phase == null) {
            return null;
        }
        PhasePanel panel = new PhasePanel(phase, this);
        int v = phase.hashCode();
        //panel.setBackground(new Color((int) (v * 255/100), (int) ((255)/ ), (int) (Math.random() * 255)));
        Hand leftHand = phase.getLeftHand();
        panel.addLeft(buildOneHandTab(leftHand));
        Hand rightHand = phase.getRightHand();
        panel.addRight(buildOneHandTab(rightHand));
        return panel;
    }

    JPanel buildOneHandTab(Hand hand) {
        if (hand == null) {
            return null;
        }
        Position p = hand.getPosition();
        if (p instanceof SymbolicPosition) {
            SymbolicOneHandPanel panel = new SymbolicOneHandPanel();
            panel.loadHand(hand, this);
            return panel;
        } else if (p instanceof TouchPosition) {
            TouchPointOneHandPanel panel = new TouchPointOneHandPanel();
            panel.loadHand(hand, this);
            return panel;
        } else if (p instanceof UniformPosition) {
            UniformOneHandPanel panel = new UniformOneHandPanel();
            panel.loadHand(hand, this);
            return panel;
        }
        return null;
    }

    public void askApplySettingToOtherPhases(String settingName, boolean setting, Side handSide) {

        int option = JOptionPane.showConfirmDialog(this, "Do you want to apply this setting to other existing strokes?", null, JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.NO_OPTION) {
            return;
        }

        GestureSignal currentGesture = Gestuary.global_gestuary.getSignal(_reference);

        for (GesturePose gesturePose : currentGesture.getPhases()) {

            Hand hand = null;
            if (handSide == Side.LEFT) {
                hand = gesturePose.getLeftHand();
            } else if (handSide == Side.RIGHT) {
                hand = gesturePose.getRightHand();
            }

            if (hand != null) {

                if (hand.getPosition() instanceof UniformPosition) {

                    UniformPosition handPosition = (UniformPosition) hand.getPosition();

                    if (settingName.equals("xFixed")) {
                        handPosition.setXFixed(setting);
                    } else if (settingName.equals("yFixed")) {
                        handPosition.setYFixed(setting);
                    } else if (settingName.equals("zFixed")) {
                        handPosition.setZFixed(setting);
                    } else if (settingName.equals("xInvariant")) {
                        handPosition.setXOverridable(!setting);
                    } else if (settingName.equals("yInvariant")) {
                        handPosition.setYOverridable(!setting);
                    } else if (settingName.equals("zInvariant")) {
                        handPosition.setZOverridable(!setting);
                    } else if (settingName.equals("orientationLocal")) {
                        hand.setWristOrientationGlobal(!setting);
                    } else if (settingName.equals("orientationGlobal")) {
                        hand.setWristOrientationGlobal(setting);
                    } else if (settingName.equals("orientationInvariant")) {
                        hand.setWristOrientationOverridable(!setting);
                    } else if (settingName.equals("handShapeInvariant")) {
                        hand.setHandShapeOverridable(!setting);
                    }
                }
            }
        }

        refreshCurrentGestureView();
    }

    public void refreshCurrentGestureView() {
        int currentGestureIndex = existingGesturesMenu.getSelectedIndex();
        int currentGesturePhaseIndex = tabbedPane.getSelectedIndex();
        existingGesturesMenu.setSelectedItem("empty=empty");
        existingGesturesMenu.setSelectedIndex(currentGestureIndex);
        tabbedPane.setSelectedIndex(currentGesturePhaseIndex);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addKeyframeMenuItem;
    private javax.swing.JMenuItem deleteKeyframeMenuItem;
    private javax.swing.JComboBox existingGesturesMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel fluidityLabel;
    private javax.swing.JSlider fluidityParameter;
    private javax.swing.JFormattedTextField fluidityValue;
    private javax.swing.JMenuItem generateMirrorGestureMenuItem;
    private javax.swing.JLabel gestureLabel;
    private javax.swing.JMenu gestureMenu;
    private javax.swing.JLabel gestureNameLabel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newGestureMenuItem;
    private javax.swing.JPanel panel1;
    private javax.swing.JPanel panel2;
    private javax.swing.JPanel panel3;
    private javax.swing.JLabel powerLabel;
    private javax.swing.JSlider powerParameter;
    private javax.swing.JFormattedTextField powerValue;
    private javax.swing.JButton reloadGestuaryButton;
    private javax.swing.JButton resetGestureButton;
    private javax.swing.JMenuItem saveGestureMenuItem;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton sendGestureButton;
    private javax.swing.JButton sendKeyFrameButton;
    private javax.swing.JPopupMenu.Separator separator;
    private javax.swing.JLabel spatialityLabel;
    private javax.swing.JSlider spatialityParameter;
    private javax.swing.JFormattedTextField spatialityValue;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JLabel stiffnessLabel;
    private javax.swing.JSlider stiffnessParameter;
    private javax.swing.JFormattedTextField stiffnessValue;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel temporalityLabel;
    private javax.swing.JSlider temporalityParameter;
    private javax.swing.JFormattedTextField temporalityValue;
    private javax.swing.JLabel tensionLabel;
    private javax.swing.JSlider tensionParameter;
    private javax.swing.JFormattedTextField tensionValue;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addSignalPerformer(SignalPerformer sp) {
        if (sp != null) {
            _signalPerformers.add(sp);
        }
    }

    @Override
    public void removeSignalPerformer(SignalPerformer performer) {
        if (performer != null) {
            _signalPerformers.add(performer);
        }
    }

    @Override
    public void addKeyframePerformer(KeyframePerformer kp) {
        if (kp != null) {
            _keyframePerformers.add(kp);
        }
    }

    @Override
    public void removeKeyframePerformer(KeyframePerformer kp) {
        _keyframePerformers.remove(kp);
    }

    private double getSPC() {
        return (spatialityParameter.getValue() / 100.0) * 2 - 1;
    }

    @Override
    public void onCharacterChanged() {
        loadGesturesList();
//        {
//        if (!_ready) {
//                canSendKeyFrames = true;
//                return;
//            }
//            if (existinggesture.getItemCount() < 1) {
//                canSendKeyFrames = true;
//                return;
//            }
//            _reference = existinggesture.getSelectedItem().toString();
//            GestureSignal gest = Gestuary.global_gestuary.getSignal(_reference);
//            if (gest == null) {
//                System.err.println("GestureEditor: " + _reference + " does not exist");
//                canSendKeyFrames = true;
//                return;
//            }
//            mainTabbedPanel.removeAll();
//            gestureid.setText(_reference);
//
//            for (GesturePose phase : gest.getPhases()) {
//                mainTabbedPanel.addTab("stroke", buildTabForPhase(phase));
//            }
//            updatePhaseNames();
//        }
    }

    @Override
    public CharacterManager getCharacterManager() {
       return cm;
    }

    @Override
    public void setCharacterManager(CharacterManager cm) {
        this.cm = cm;
    }

    @Override
    protected void finalize() throws Throwable {
        cm.remove(this);
        super.finalize();
    }
}
