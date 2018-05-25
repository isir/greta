/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.aulibrary;

import vib.core.animation.mpeg4.fap.FAPFrame;
import vib.core.animation.mpeg4.fap.FAPFrameEmitter;
import vib.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import vib.core.animation.mpeg4.fap.FAPFramePerformer;
import vib.core.animation.mpeg4.fap.FAPType;
import vib.core.repositories.AULibrary;
import vib.core.repositories.FLExpression;
import vib.core.repositories.FLExpression.FAPItem;
import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.Constants;
import vib.core.util.id.IDProvider;
import vib.core.util.time.Timer;
import vib.core.utilx.gui.ToolBox;
import vib.tools.editors.SliderAndText;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;

/**
 *
 * @author Andre-Marie Pez
 */
public class AULibraryEditor extends JFrame implements FAPFrameEmitter, CharacterDependent{

    private Comparator<String> auNameComparator = new Comparator<String>() {

        @Override
        public int compare(String o1, String o2) {
            try{
                int o1num = Integer.parseInt(o1.substring(2));
                int o2num = Integer.parseInt(o2.substring(2));
                return o1num - o2num;
            }
            catch(Throwable t){}
            return o1.compareToIgnoreCase(o2);
        }
    };
    private FacePanel facePanel;
    private JScrollPane scp;
    private FAPFrameEmitterImpl fapEmitter = new FAPFrameEmitterImpl();
    private FAPFrame fapFrame = new FAPFrame();
    private JComboBox auComboBox;



    public AULibraryEditor(){
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        facePanel = new FacePanel(this);
        facePanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        scp = new JScrollPane();
        JButton resetButton = new JButton("reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applySelectedAU();
            }
        });
        JButton resetToZeroButton = new JButton("reset to zero");
        resetToZeroButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setToZero();
            }
        });
        auComboBox = new JComboBox();
        auComboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    applySelectedAU();
                }
            }
        });
        refreshCombobox();
        GroupLayout layout = new GroupLayout(this.getContentPane());
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(auComboBox,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
                    .addComponent(resetButton)
                    .addComponent(resetToZeroButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(facePanel, 100, 400, Short.MAX_VALUE)
                    .addComponent(scp, 50, 200, Short.MAX_VALUE))
                .addContainerGap();
        GroupLayout.SequentialGroup horizontalGroup = layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(auComboBox,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(resetButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(resetToZeroButton))
                    .addComponent(facePanel, 100, 400, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scp, 50, 200, 200)
                .addContainerGap();
        layout.setHorizontalGroup(horizontalGroup);
        layout.setVerticalGroup(verticalGroup);
        getContentPane().setLayout(layout);

        //menu :
        JMenuBar menuBar = new JMenuBar();
        ToolBox.LocalizedJMenu fileNemu = new ToolBox.LocalizedJMenu("GUI.file");
        ToolBox.LocalizedJMenuItem newMenuItem = new ToolBox.LocalizedJMenuItem("GUI.new");
        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newAU();
            }
        });
        ToolBox.LocalizedJMenuItem saveMenuItem = new ToolBox.LocalizedJMenuItem("GUI.save");
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

        applySelectedAU();
        this.pack();
        CharacterManager.add(this);
    }

    public void setRightPanel(JPanel p){
        scp.setViewportView(p);
    }

    @Override
    public void addFAPFramePerformer(FAPFramePerformer fapfp) {
        fapEmitter.addFAPFramePerformer(fapfp);
    }

    @Override
    public void removeFAPFramePerformer(FAPFramePerformer fapfp) {
        fapEmitter.removeFAPFramePerformer(fapfp);
    }

    public void updateFrame(FAPType fapType, int value){
        fapFrame.applyValue(fapType, value);
        FAPFrame toSend = new FAPFrame(fapFrame);
        toSend.setFrameNumber((int)(Timer.getTime()*Constants.FRAME_PER_SECOND));
        fapEmitter.sendFAPFrame(IDProvider.createID("From_AU_Library_Editor"), toSend);
    }


    private void refreshCombobox() {
        refreshCombobox(auComboBox.getSelectedItem());
    }

    private void refreshCombobox(Object selected) {
        LinkedList<String> auNames = new LinkedList<String>();
        for (FLExpression faceexp : AULibrary.global_aulibrary.getAll()) {
            auNames.add(faceexp.getParamName().toUpperCase());
        }
        Collections.sort(auNames, auNameComparator);

        auComboBox.removeAllItems();

        for (String faceexp : auNames) {
            auComboBox.addItem(faceexp);
        }
        if (selected != null) {
            auComboBox.setSelectedItem(selected);
        }
    }

    private FLExpression getAU(String name){
        return AULibrary.global_aulibrary.get(name);
    }

    private void applySelectedAU() {
        setAU(getAU(auComboBox.getSelectedItem().toString()));
    }

    private void setToZero() {
        for (SliderAndText fap : facePanel.getSliders()) {
            if(fap!=null){
                fap.setValue(0);
            }
        }
    }

    private void setAU(FLExpression au) {
        setToZero();
        if (au != null) {
            for (FLExpression.FAPItem fap : au.getFAPs()) {
                if(facePanel.getSliders()[fap.type.ordinal()] != null){
                    facePanel.getSliders()[fap.type.ordinal()].setValue(fap.value);
                }
            }
        }
    }


    private void newAU() {
        String name = JOptionPane.showInputDialog(this, "Action Unit name:", "New Action Unit", JOptionPane.PLAIN_MESSAGE);
        if (name != null) {
            try{
                Integer.parseInt(name); //if it doesn't crash, it's a number
                name = "AU"+name; //if it's a number, we add "AU" before
            }
            catch(Throwable t){}
            if(getAU(name)!=null){
                JOptionPane.showMessageDialog(this, "This Action Unit already exists.", "New Action Unit", JOptionPane.PLAIN_MESSAGE);
            }
            else{
                FLExpression newFaceExp = new FLExpression(name);
                AULibrary.global_aulibrary.getDefaultDefinition().addParameter(newFaceExp);
            }
            refreshCombobox(name.toLowerCase());
        }
    }


    private void applyModifications(){
        boolean isInLocalDefinition = true;
        FLExpression localAU = AULibrary.global_aulibrary.getCurrentDefinition().getParameter(auComboBox.getSelectedItem().toString());
        if(localAU == null){
            FLExpression globalAU = getAU(auComboBox.getSelectedItem().toString());
            if(globalAU.getFAPs().isEmpty()){
                //we created it in newExpression()
                //update global
                localAU = globalAU;
                isInLocalDefinition = false;
            }
            else{
                //create new local
                localAU = new FLExpression(auComboBox.getSelectedItem().toString());
                AULibrary.global_aulibrary.getCurrentDefinition().addParameter(localAU);
            }
        }
        //update localExpression
        List<FAPItem> fapInAU = localAU.getFAPs();
        fapInAU.clear();
        for(FapComponents fap : facePanel.getFace().allCotrolPoints){
            if(fap.horizontal != null && fap.horizontal.value.getValue() != 0){
                localAU.add(fap.horizontal.type, fap.horizontal.value.getValue());
            }
            if(fap.vertical != null && fap.vertical.value.getValue() != 0){
                localAU.add(fap.vertical.type, fap.vertical.value.getValue());
            }
            if(fap.depth != null && fap.depth.value.getValue() != 0){
                localAU.add(fap.depth.type, fap.depth.value.getValue());
            }
        }
        if(isInLocalDefinition){
            AULibrary.global_aulibrary.saveCurrentDefinition();
        } else {
            AULibrary.global_aulibrary.saveDefaultDefinition();
        }
    }

    @Override
    public void onCharacterChanged() {
        refreshCombobox();
        applySelectedAU();
    }
    @Override
    protected void finalize() throws Throwable {
        CharacterManager.remove(this);
        super.finalize();
    }

}
