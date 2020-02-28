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
package greta.tools.editors.facelibrary;

import greta.core.keyframes.face.AUEmitter;
import greta.core.keyframes.face.AUPerformer;
import greta.core.repositories.AUAPFrame;
import greta.core.repositories.AUExpression;
import greta.core.repositories.AUItem;
import greta.core.repositories.AULibrary;
import greta.core.repositories.FaceLibrary;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.Constants;
import greta.core.util.enums.Side;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.time.Timer;
import greta.core.utilx.gui.ToolBox.LocalizedJMenu;
import greta.core.utilx.gui.ToolBox.LocalizedJMenuItem;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 *
 * @author Andre-Marie Pez
 */
public class FaceLibraryEditor extends JFrame implements CharacterDependent, AUEmitter {

    private AUComponents[] aus = new AUComponents[AUAPFrame.NUM_OF_AUS];
    private AUAPFrame frame = new AUAPFrame();
    private ArrayList<AUPerformer> perfomers = new ArrayList<AUPerformer>();
    private JComboBox instancesComboBox;
    private CharacterManager cm;
    private AULibrary auLibrary;

    public FaceLibraryEditor(CharacterManager cm) {
        setCharacterManager(cm);

        auLibrary = new AULibrary(cm);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        for (int i = 0; i < AUAPFrame.NUM_OF_AUS; ++i) {
            aus[i] = new AUComponents(i+1, frame) {
                @Override
                protected void send() {
                    FaceLibraryEditor.this.send();
                }
            };
            aus[i].setEnabled(auLibrary.get("AU" + (i+1)) != null); //disable au when not implemented in AU lib
        }

        JButton resetButton = new JButton("reset");//TODO use localizedJButton
        JButton reset0Button = new JButton("reset to zero");//TODO use localizedJButton
        JLabel faceexpLabel = new JLabel("Facial expression");//TODO use localizedJLabel
        JTabbedPane tabbedPane = new JTabbedPane();
        instancesComboBox = new JComboBox();

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applySelectedExpression();
            }
        });

        reset0Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setToZero();
            }
        });

        tabbedPane.setBorder(javax.swing.BorderFactory.createTitledBorder("FACS"));
        addTab(tabbedPane, "UpperFace AUs", 1, 2, 4, 5, 6, 7, 43);
        addTab(tabbedPane, "LowerFace - Up/Down/Horizontal Acts", 10, 15, 16, 17, 20, 25, 26, 27);
        addTab(tabbedPane, "LowerFace - Oblique:Orbital Acts", 12, 18, 22, 23, 24);
        addTab(tabbedPane, "Misc", 29, 30, 34, 35, 38, 39);
        addTab(tabbedPane, "Others", 3, 8, 9, 11, 13, 14, 19, 21, 28, 31, 32, 33, 36, 37, 40, 41, 42, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64);

        instancesComboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    applySelectedExpression();
                }
            }
        });
        refreshCombobox();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(tabbedPane)
                .addGroup(layout.createSequentialGroup()
                .addComponent(faceexpLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(instancesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(resetButton)
                .addGap(18, 18, 18)
                .addComponent(reset0Button)
                .addGap(0, 195, Short.MAX_VALUE)))
                .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(faceexpLabel)
                .addComponent(instancesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(resetButton)
                .addComponent(reset0Button))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                .addContainerGap()));

        //menu :
        JMenuBar menuBar = new JMenuBar();
        LocalizedJMenu fileNemu = new LocalizedJMenu("GUI.file");
        LocalizedJMenuItem newMenuItem = new LocalizedJMenuItem("GUI.new");
        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newExpression();
            }
        });
        LocalizedJMenuItem saveMenuItem = new LocalizedJMenuItem("GUI.save");
        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyModifications();
                //TODO save
            }
        });

        fileNemu.add(newMenuItem);
        fileNemu.add(saveMenuItem);
        menuBar.add(fileNemu);
        this.setJMenuBar(menuBar);

        applySelectedExpression();
        this.pack();
    }

    private void addTab(JTabbedPane tabbedPane, String title, int... auIndex) {
        AUComponents[] ausToUse = new AUComponents[auIndex.length];
        for (int i = 0; i < auIndex.length; ++i) {
            ausToUse[i] = aus[auIndex[i]-1];
        }
        tabbedPane.addTab(title, createScrollPane(new AUsPanel(ausToUse)));
    }

    private JScrollPane createScrollPane(JComponent c) {
        Dimension preferred = new Dimension(c.getPreferredSize().width + 5, c.getPreferredSize().height + 5);
        c.setPreferredSize(c.getMinimumSize());
        JScrollPane scrollPane = new JScrollPane(c);
        scrollPane.setPreferredSize(preferred);
        return scrollPane;
    }

    private void newExpression() {
        String name = JOptionPane.showInputDialog(this, "Facial expression name:", "New Facial Expression", JOptionPane.PLAIN_MESSAGE);
        if (name != null) {
            if(getFaceexp(name)!=null){
                if(isNameForbidden(name)){
                    JOptionPane.showMessageDialog(this, "Action units or neutral face can not be edited.", "New Facial Expression", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                else{
                    JOptionPane.showMessageDialog(this, "This facial expression already exists.", "New Facial Expression", JOptionPane.PLAIN_MESSAGE);
                }
            }
            else{
                AUExpression newFaceExp = new AUExpression(name);
                FaceLibrary.global_facelibrary.getDefaultDefinition().addParameter(newFaceExp);
            }
            refreshCombobox(name.toLowerCase());
        }
    }

    private void applyModifications(){
        boolean isInLocalDefinition = true;
        AUExpression localExpression = FaceLibrary.global_facelibrary.getCurrentDefinition().getParameter("faceexp="+instancesComboBox.getSelectedItem().toString());
        if(localExpression == null){
            AUExpression globalExpression = getFaceexp(instancesComboBox.getSelectedItem().toString());
            if(globalExpression.getActionUnits().isEmpty()){
                //we created it in newExpression()
                //update global
                localExpression = globalExpression;
                isInLocalDefinition = false;
            }
            else{
                //create new local
                localExpression = new AUExpression(instancesComboBox.getSelectedItem().toString());
                FaceLibrary.global_facelibrary.getCurrentDefinition().addParameter(localExpression);
            }
        }
        //update localExpression
        List<AUItem> ausInExpression = localExpression.getActionUnits();
        ausInExpression.clear();
        for(AUComponents au : aus){
            if( ! au.assymetryCheckBox.isSelected()){
                double intensity = au.right.getNormlizedValue();
                if(intensity>0){
                    ausInExpression.add(new AUItem(au.which, intensity, Side.BOTH));
                }
            }
            else{
                double intensityRight = au.right.getNormlizedValue();
                double intensityLeft = au.left.getNormlizedValue();
                if(intensityLeft==intensityRight && intensityLeft>0){
                    ausInExpression.add(new AUItem(au.which, intensityLeft, Side.BOTH));
                } else {
                    if(intensityLeft>0){
                        ausInExpression.add(new AUItem(au.which, intensityLeft, Side.LEFT));
                    }
                    if(intensityRight>0){
                        ausInExpression.add(new AUItem(au.which, intensityRight, Side.RIGHT));
                    }
                }
            }
        }
        if(isInLocalDefinition){
            FaceLibrary.global_facelibrary.saveCurrentDefinition();
        } else {
            FaceLibrary.global_facelibrary.saveDefaultDefinition();
        }
    }

    private boolean isNameForbidden(String name){
        if(name.equalsIgnoreCase("neutral")){
            return true;
        }
        if(name.toLowerCase().startsWith("au")){
            try{
                Integer.parseInt(name.substring(2));
                return true;
            }
            catch(Throwable t){
                //not a num, it's ok
            }
        }
        return false;
    }

    private void refreshCombobox() {
        refreshCombobox(instancesComboBox.getSelectedItem());
    }

    private void refreshCombobox(Object selected) {
        LinkedList<String> faceexpNames = new LinkedList<String>();
        for (AUExpression faceexp : FaceLibrary.global_facelibrary.getAll()) {
            if (faceexp.getType().equalsIgnoreCase("faceexp")) {//only faceexps
                if (!isNameForbidden(faceexp.getInstanceName())) {//skip AUs and neutral
                    faceexpNames.add(faceexp.getInstanceName().toLowerCase());
                }
            }
        }
        Collections.sort(faceexpNames, String.CASE_INSENSITIVE_ORDER);

        instancesComboBox.removeAllItems();

        for (String faceexp : faceexpNames) {
            instancesComboBox.addItem(faceexp);
        }
        if (selected != null) {
            instancesComboBox.setSelectedItem(selected);
        }
    }

    private AUExpression getFaceexp(String name){
        return FaceLibrary.global_facelibrary.get("faceexp="+name);
    }


    private void applySelectedExpression() {
        setFaceExpression(getFaceexp(instancesComboBox.getSelectedItem().toString()));
    }

    private void setToZero() {
        for (AUComponents au : aus) {
            au.setAssymetric(false);
            au.right.setValue(0);
        }
    }

    private void setFaceExpression(AUExpression faceexp) {
        setToZero();
        if (faceexp != null) {
            for (AUItem au : faceexp.getActionUnits()) {
                AUComponents auc = aus[au.getAUnum()-1];
                auc.setAssymetric(au.getSide() != Side.BOTH);
                if (au.getSide() == Side.LEFT) {
                    auc.left.setNormalisedValue(au.getIntensity());
                } else {
                    auc.right.setNormalisedValue(au.getIntensity());
                }
            }
        }
    }

    private void send() {
        AUAPFrame frame = new AUAPFrame(this.frame);
        frame.setFrameNumber((int) (Timer.getTime() * Constants.FRAME_PER_SECOND) + 1);
        ID id = IDProvider.createID("From_Face_Library");
        for (AUPerformer performer : perfomers) {
            performer.performAUAPFrame(frame, id);
        }
    }

    @Override
    public void onCharacterChanged() {
        applySelectedExpression();
    }

    @Override
    public void addAUPerformer(AUPerformer aup) {
        if (aup != null) {
            perfomers.add(aup);
        }
    }

    @Override
    public void removeAUPerformer(AUPerformer aup) {
        if (aup != null) {
            perfomers.remove(aup);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        cm.remove(this);
        super.finalize();
    }

    @Override
    public CharacterManager getCharacterManager() {
        return cm;
    }

    @Override
    public void setCharacterManager(CharacterManager cm) {
        this.cm = cm;
        cm.add(this);
    }


}
