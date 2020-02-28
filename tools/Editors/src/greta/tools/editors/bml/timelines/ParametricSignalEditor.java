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
package greta.tools.editors.bml.timelines;

import greta.core.signals.ParametricSignal;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import greta.tools.editors.MultiTimeLineEditors;
import greta.tools.editors.TemporizableContainer;
import greta.tools.editors.TimeLine;
import greta.tools.editors.TimeLineManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 *
 * @author Andre-Marie
 */
public class ParametricSignalEditor<P extends ParametricSignal> extends javax.swing.JDialog {

    TemporizableContainer<P> edited;
    Map<String, List<String>> library;
    boolean useModalityAsCategory, startAbsolute, endAuto, endAbsolute;
    SignalLib<P> signalLib;
    MultiTimeLineEditors<? extends Temporizable> bmlEditor;
    TemporizableContainer<? extends Temporizable> startRef, endRef;
    String startMarkerName, endMarkerName;
    TimeMarker startMarker, endMarker;
    double oldDuration;
    int doublePrecision = 3;
    String doublePrecisionReset = "0.000";


    protected ParametricSignalEditor(java.awt.Frame parent, boolean modal) {
        this(parent, modal, null, null, true,null);
    }
    /** Creates new form ParametricSignalEditor */
    public ParametricSignalEditor(java.awt.Frame parent, boolean modal, TemporizableContainer<P> signal, SignalLib<P> library, boolean useModalityAsCategory, MultiTimeLineEditors<? extends Temporizable> _bmlEditor) {
        super(parent, modal);
        this.useModalityAsCategory = useModalityAsCategory;
        this.edited = signal;

        this.startAbsolute = !this.edited.hasStartRef();
        this.startMarker = new TimeMarker("Start Ref Marker");
        this.startMarkerName = new String();

        this.endAuto = !this.edited.hasEndRef();
        this.endAbsolute = !this.edited.hasEndRef();
        this.endMarkerName = new String();
        this.endMarker = new TimeMarker("Reference end Marker");
        this.bmlEditor = _bmlEditor;
        this.setTitle("Edit \"" + signal.getId() + "\"");
        initComponents();

        for(String item : this.bmlEditor.getNonEmptyTimeLineLables()){
            this.startTypeCombo.addItem(item);
            this.endTypeCombo.addItem(item);
        }
        this.resetAllStartCombo();
        this.resetAllEndCombo();

        if(startAbsolute){
            this.startAbsRadio.doClick();
        }

        else{
            this.startRelRadio.doClick();
            this.startRef = this.edited.getStartRef();
            this.startMarker = this.edited.getStartMarker();
            this.startMarkerName = this.startMarker.getName();
            this.startTypeCombo.setSelectedItem(this.startRef.getTemporizableType());
            this.startIDCombo.setSelectedItem(this.startRef.getId().toString());
            this.startTMCombo.setSelectedItem(this.startMarkerName);
            this.startOffsetField.setText(String.format(Locale.ENGLISH,"%." + doublePrecision + "f", this.edited.getStartOffset()));
        }

        if(endAuto){
            this.endAutoRadio.setSelected(false);
            this.endAutoRadio.doClick();
        }
        else if(endAbsolute){
            this.endAbsRadio.setSelected(false);
            this.endAbsRadio.doClick();
        }
        else{
            this.endRelRadio.doClick();
            this.endRef = this.edited.getEndRef();
            this.endMarker = this.edited.getEndMarker();
            this.endMarkerName = this.endMarker.getName();
            this.endTypeCombo.setSelectedItem(this.endRef.getTemporizableType());
            this.endIDCombo.setSelectedItem(this.endRef.getId().toString());
            this.endTMCombo.setSelectedItem(this.endMarkerName);
            this.endOffsetField.setText(String.format("%." + doublePrecision + "f",this.edited.getEndOffset()));
        }


        Hashtable<Integer, JComponent> dic =  new Hashtable<Integer, JComponent>();
        dic.put(0, new javax.swing.JLabel("0"));
        dic.put(50, new javax.swing.JLabel("0.5"));
        dic.put(100, new javax.swing.JLabel("1"));
        sliderPWR.setLabelTable(dic);
        signalLib = library;
        this.library = new HashMap<String, List<String>>();
        if(signalLib != null){
            this.library = signalLib.buildLib();
//            for(P param :library){
//                String key = param.getCategory()==null? param.getModality().toUpperCase() : param.getCategory().toUpperCase();
//                List<String> instances = this.library.get(key);
//                if(instances==null){
//                    instances = new ArrayList<String>();
//                    this.library.put(key, instances);
//                }
//                instances.add(param.getId().toUpperCase());
//            }
        }

        javax.swing.DefaultComboBoxModel model = new javax.swing.DefaultComboBoxModel(this.library.keySet().toArray());
        classComboBox.setModel(model);
        classComboBox.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                    List<String> instances = ParametricSignalEditor.this.library.get(e.getItem().toString());
                    javax.swing.DefaultComboBoxModel model = new javax.swing.DefaultComboBoxModel(instances.toArray());
                    instanceComboBox.setModel(model);
                    instanceComboBox.setEnabled(! instances.isEmpty());
                }
            }
        });
        classComboBox.setEnabled(! this.library.keySet().isEmpty());

        List<String> instances = this.library.get(classComboBox.getSelectedItem());
        javax.swing.DefaultComboBoxModel model2 = new javax.swing.DefaultComboBoxModel(instances==null ? new String[0] : instances.toArray());
        instanceComboBox.setModel(model2);
        instanceComboBox.setEnabled( ! instances.isEmpty());

        if(edited!=null){
            idField.setText(edited.getId());


            sliderSPC.setValue((int)(edited.getTemporizable().getSPC()*100.0));
            sliderTMP.setValue((int)(edited.getTemporizable().getTMP()*100.0));
            sliderFLD.setValue((int)(edited.getTemporizable().getFLD()*100.0));
            sliderPWR.setValue((int)(edited.getTemporizable().getPWR()*100.0));

            startField.setText(""+String.format(Locale.ENGLISH,"%." + doublePrecision + "f",edited.getStart().getValue()));
            endField.setText(""+String.format(Locale.ENGLISH,"%." + doublePrecision + "f",edited.getEnd().getValue()));

            String className = signalLib.getClassNameOf(edited.getTemporizable());
            String instanceName = signalLib.getInstanceNameOf(edited.getTemporizable());

            classComboBox.setSelectedItem(className);
            instanceComboBox.setSelectedItem(instanceName);
        }
        // pack();
    }

    public void close(){
        this.dispose();
    }

    public boolean apply(){
        edited.setReferencesState(TemporizableContainer.ReferencesState.UNCHANGED);
        edited.getTemporizable().setSPC(sliderSPC.getValue()/100.0);
        edited.getTemporizable().setTMP(sliderTMP.getValue()/100.0);
        edited.getTemporizable().setFLD(sliderFLD.getValue()/100.0);
        edited.getTemporizable().setPWR(sliderPWR.getValue()/100.0);
        if(signalLib!=null){
            signalLib.applyOn(edited.getTemporizable(),
                    classComboBox.getSelectedItem()==null ? "" : classComboBox.getSelectedItem().toString(),
                    instanceComboBox.getSelectedItem()==null ? "" : instanceComboBox.getSelectedItem().toString());
        }
        TimeMarker start = edited.getStart();
        TimeMarker end = edited.getEnd();

        if(endAuto){
            oldDuration = end.getValue() - start.getValue();
        }

        if(edited.hasStartRef()){
            edited.getStartRef().deleteLinkedSignal(edited.getId().toString());
        }

        if(edited.hasEndRef()){
            edited.getEndRef().deleteLinkedSignal(edited.getId().toString());
        }

        //If start reference is absolute
        if(this.startAbsolute)
        {
            try{
                double startValue = Double.parseDouble(startField.getText()); //if it is not number, it throws an exception
                if(start.getValue() != startValue)
                {
                    for(String linkedID : edited.getLinkedSignal())
                    {
                        if(this.bmlEditor != null)
                        {
                            for(TemporizableContainer tmp : this.bmlEditor.getAllTemporizableContainers())
                            {
                                if(linkedID.equals(tmp.getId())){
                                    tmp.setReferencesState(TemporizableContainer.ReferencesState.CHANGED);
                                }
                            }
                        }
                    }
                }
                start.removeReferences();
                start.setValue(startValue);
            }
            catch(Exception e){
                JOptionPane.showMessageDialog(this, "Please check that start time is integer.\nChanges were not applied.\n", "Start time not integer", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        //If start reference is relative
        else
        {
            //Check that elements are properly chosen in combo boxes
            //Or else throw a popup
            if(this.startTMCombo.getSelectedItem().toString().equals("<Select TM>")){
                JOptionPane.showMessageDialog(this, "Please select Type, ID and TM for start reference.\nChanges were not applied.\n", "Start reference missing", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            edited.setStartRef(this.startRef);
            edited.getStartRef().setLinkedSignal(edited.getId().toString());
            for(TimeMarker tm : this.startRef.getTimeMarkers()){
                if (tm.getName() == startMarkerName){
                    startMarker = tm;
                    edited.setStartMarker(tm);
                    break;
                }
            }
            double startValue = this.startMarker.getValue();
            double startOffset = 0;
            try{
                startOffset = Double.parseDouble(startOffsetField.getText()); //if it is not number, it throws an exception
            }
            catch(Exception e){
                JOptionPane.showMessageDialog(this, "Please check that start offset is integer.\nChanges were not applied.\n", "Start offset not integer", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            if(startValue + startOffset != edited.getStart().getValue())
            {
                for(String linkedID : edited.getLinkedSignal())
                {
                    if(this.bmlEditor != null)
                    {
                        for(TemporizableContainer tmp : this.bmlEditor.getAllTemporizableContainers())
                        {
                            if(linkedID.equals(tmp.getId())){
                                tmp.setReferencesState(TemporizableContainer.ReferencesState.CHANGED);
                            }
                        }
                    }
                }
            }

            start.setValue(startValue + startOffset);
            start.removeReferences();

            String startID = this.edited.getStartRef().getId();
            String startMarker = this.edited.getStartMarker().getName();
            String ref = startID + ":" + startMarker ;

            start.addReference(this.startMarker,startOffset);
            start.getReferences().get(0).setTargetName(ref);
            edited.setStartOffset(startOffset);

        }

        //If end reference is to compute automatically with old duration
        if(this.endAuto){
            double endValue = start.getValue() + oldDuration;
            end.setValue(endValue);
        }
        //If end reference is absolute
        else if(this.endAbsolute){
            try{
                double endValue = Double.parseDouble(endField.getText()); //if it is not number, it throws an exception
                if(end.getValue() != endValue)
                {
                    for(String linkedID : edited.getLinkedSignal())
                    {
                        if(this.bmlEditor != null)
                        {
                            for(TemporizableContainer tmp : this.bmlEditor.getAllTemporizableContainers())
                            {
                                if(linkedID.equals(tmp.getId())){
                                    tmp.setReferencesState(TemporizableContainer.ReferencesState.CHANGED);
                                }
                            }
                        }
                    }
                }
                end.removeReferences();
                end.setValue(endValue);
            }
            catch(Exception e){
                JOptionPane.showMessageDialog(this, "Please check that end time is integer.\nChanges were not applied.\n", "End time not integer", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        else{
            //Check that elements are properly chosen in combo boxes
            //Or else throw a popup
            if(this.endTMCombo.getSelectedItem().toString().equals("<Select TM>")){
                JOptionPane.showMessageDialog(this, "Please select Type, ID and TM for end reference.\nChanges were not applied.\n", "End reference missing", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            edited.setEndRef(this.endRef);
            edited.getEndRef().setLinkedSignal(edited.getId().toString());
            for(TimeMarker tm : this.endRef.getTimeMarkers()){
                if (tm.getName() == endMarkerName){
                    endMarker = tm;
                    edited.setEndMarker(tm);
                    break;
                }
            }
            double endValue = this.endMarker.getValue();
            double endOffset = 0;
            try{
                endOffset = Double.parseDouble(endOffsetField.getText()); //if it is not number, it throws an exception
            }
            catch(Exception e){
                JOptionPane.showMessageDialog(this, "Please check that end offset is integer.\nChanges were not applied.\n", "End offset not integer", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            if(end.getValue() != endValue + endOffset)
            {
                for(String linkedID : edited.getLinkedSignal())
                {
                    if(this.bmlEditor != null)
                    {
                        for(TemporizableContainer tmp : this.bmlEditor.getAllTemporizableContainers())
                        {
                            if(linkedID.equals(tmp.getId())){
                                tmp.setReferencesState(TemporizableContainer.ReferencesState.CHANGED);
                            }
                        }
                    }
                }
            }

            end.setValue(endValue + endOffset);
            edited.setEndOffset(endOffset);
            end.removeReferences();

            String endID = this.edited.getEndRef().getId();
            String endMarker = this.edited.getEndMarker().getName();
            String ref = endID + ":" + endMarker;

            end.addReference(this.endMarker,endOffset);
            end.getReferences().get(0).setTargetName(ref);
            edited.setEndOffset(endOffset);
        }

        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox<String>();
        buttonGroup1 = new javax.swing.ButtonGroup();
        startAbsRelButtonGroup = new javax.swing.ButtonGroup();
        endAbsRelButtonGroup = new javax.swing.ButtonGroup();
        deleteButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        sliderSPC = new javax.swing.JSlider();
        sliderPWR = new javax.swing.JSlider();
        spcLabel = new javax.swing.JLabel();
        sliderTMP = new javax.swing.JSlider();
        sliderFLD = new javax.swing.JSlider();
        tmpLabel = new javax.swing.JLabel();
        fldLabel = new javax.swing.JLabel();
        pwrLabel = new javax.swing.JLabel();
        idField = new javax.swing.JTextField();
        classComboBox = new javax.swing.JComboBox();
        instanceComboBox = new javax.swing.JComboBox();
        startField = new javax.swing.JTextField();
        endField = new javax.swing.JTextField();
        classLabel = new javax.swing.JLabel();
        instanceLabel = new javax.swing.JLabel();
        idLabel = new javax.swing.JLabel();
        startLabel1 = new javax.swing.JLabel();
        endLabel1 = new javax.swing.JLabel();
        startTypeCombo = new javax.swing.JComboBox<String>();
        startTMCombo = new javax.swing.JComboBox<String>();
        startIDCombo = new javax.swing.JComboBox<String>();
        endTypeCombo = new javax.swing.JComboBox<String>();
        endIDCombo = new javax.swing.JComboBox<String>();
        endTMCombo = new javax.swing.JComboBox<String>();
        startAbsRadio = new javax.swing.JRadioButton();
        startRelRadio = new javax.swing.JRadioButton();
        endAbsRadio = new javax.swing.JRadioButton();
        endRelRadio = new javax.swing.JRadioButton();
        endAutoRadio = new javax.swing.JRadioButton();
        startOffsetField = new javax.swing.JTextField();
        endOffsetField = new javax.swing.JTextField();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(null);
        setPreferredSize(new java.awt.Dimension(800, 600));
        setResizable(false);

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        sliderPWR.setPaintLabels(true);

        spcLabel.setText("SPC");

        tmpLabel.setText("TMP");

        fldLabel.setText("FLD");

        pwrLabel.setText("PWR");

        idField.setEditable(false);

        classComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        instanceComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        startField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startFieldActionPerformed(evt);
            }
        });

        endField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endFieldActionPerformed(evt);
            }
        });

        classLabel.setText("Class");

        instanceLabel.setText("Instance");

        idLabel.setText("ID");

        startLabel1.setText("Start");

        endLabel1.setText("End");

        startTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Select Type>" }));
        startTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTypeComboActionPerformed(evt);
            }
        });

        startTMCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Select TM>" }));
        startTMCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTMComboActionPerformed(evt);
            }
        });

        startIDCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Select ID>" }));
        startIDCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startIDComboActionPerformed(evt);
            }
        });

        endTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Select Type>" }));
        endTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endTypeComboActionPerformed(evt);
            }
        });

        endIDCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Select ID>" }));
        endIDCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endIDComboActionPerformed(evt);
            }
        });

        endTMCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Select TM>" }));
        endTMCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endTMComboActionPerformed(evt);
            }
        });

        startAbsRelButtonGroup.add(startAbsRadio);
        startAbsRadio.setText("Absolute");
        startAbsRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startAbsRadioActionPerformed(evt);
            }
        });

        startAbsRelButtonGroup.add(startRelRadio);
        startRelRadio.setText("Relative");
        startRelRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startRelRadioActionPerformed(evt);
            }
        });

        endAbsRelButtonGroup.add(endAbsRadio);
        endAbsRadio.setText("Absolute");
        endAbsRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endAbsRadioActionPerformed(evt);
            }
        });

        endAbsRelButtonGroup.add(endRelRadio);
        endRelRadio.setText("Relative");
        endRelRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endRelRadioActionPerformed(evt);
            }
        });

        endAbsRelButtonGroup.add(endAutoRadio);
        endAutoRadio.setText("Auto");
        endAutoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endAutoRadioActionPerformed(evt);
            }
        });

        startOffsetField.setColumns(10);
        startOffsetField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startOffsetFieldActionPerformed(evt);
            }
        });

        endOffsetField.setColumns(10);
        endOffsetField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endOffsetFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(classLabel)
                    .addComponent(idLabel)
                    .addComponent(instanceLabel)
                    .addComponent(startLabel1)
                    .addComponent(endLabel1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(deleteButton)
                        .addGap(99, 99, 99))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(instanceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(classComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(215, 215, 215)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(spcLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(sliderSPC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(fldLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(sliderFLD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(pwrLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(sliderPWR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(tmpLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sliderTMP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(endAutoRadio)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(endAbsRadio)
                                    .addComponent(endRelRadio)
                                    .addComponent(startRelRadio)
                                    .addComponent(startAbsRadio))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(endTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(endField)
                                    .addComponent(startTypeCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(startField, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(startIDCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(startTMCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(endIDCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(endTMCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(startOffsetField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(endOffsetField, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, deleteButton, okButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(46, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(idLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(classComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(classLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(instanceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(instanceLabel)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sliderSPC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spcLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sliderTMP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tmpLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sliderFLD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fldLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sliderPWR, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pwrLabel))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startLabel1)
                        .addGap(59, 59, 59)
                        .addComponent(endLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startAbsRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startRelRadio)
                        .addGap(27, 27, 27)
                        .addComponent(endAutoRadio)
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(endAbsRadio)
                            .addComponent(endField, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(endRelRadio)
                            .addComponent(endTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(endIDCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(endTMCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(endOffsetField, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startField, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(startTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(startIDCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(startTMCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(startOffsetField, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {cancelButton, deleteButton, okButton});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        boolean ok = apply();
        if(ok)
            close();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        close();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        edited = null;
        close();
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void startTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startTypeComboActionPerformed
        String s = this.startTypeCombo.getSelectedItem().toString();
        List<String>IDArray = new ArrayList<String>();
        this.resetStartIDAndTMCombo();

        TimeLineManager<? extends Temporizable> tlm = bmlEditor.getTimeLineManager(s);
        if (tlm != null) {
            TimeLine<? extends Temporizable> tl =  tlm.getTimeLine();
            for(String id : tl.getItemsNamesList()){
                IDArray.add(id);
            }
        }

        // If elements are found, put them in startIDCombo and enable it
        if(!IDArray.isEmpty()){
            for(String item : IDArray) {
                if (!item.equalsIgnoreCase(this.edited.getId()))
                {
                    startIDCombo.addItem(item);
                }
            }
            startIDCombo.setEnabled(true);
        }
    }//GEN-LAST:event_startTypeComboActionPerformed

    private void startFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_startFieldActionPerformed

    private void endFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_endFieldActionPerformed

    private void startIDComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startIDComboActionPerformed
        // Avoid computing when combo is disabled
        if( !startIDCombo.isEnabled()){
            return;
        }

        List<TimeMarker> TMarkersArray = new ArrayList<TimeMarker>();
        List<String> TMArray = new ArrayList<String>();
        String s = this.startTypeCombo.getSelectedItem().toString();
        String id = this.startIDCombo.getSelectedItem().toString();
        //Reset and empty TMCombo
        this.resetStartTMCombo();

        // Avoid computing when default element selected.
        if(id.equals("<Select ID>"))
            return;

        // Find start temporizable by Type and ID
        TimeLineManager<? extends Temporizable> tlm = bmlEditor.getTimeLineManager(s);

        if (tlm != null) {
             TimeLine<? extends Temporizable> tl =  tlm.getTimeLine();
            for(TemporizableContainer<? extends Temporizable> tmpCont : tl.getItems()){
                if (tmpCont.getId() == id) {
                    startRef = tmpCont;
                    break;
                }
            }
        }

        // Whend found, get TimeMarkers from start temporizable
        TMarkersArray = startRef.getTimeMarkers();
        for(TimeMarker tm: TMarkersArray){
            TMArray.add(tm.getName());
        }
        // If TM are found, put them in startTMCombo and enable it
        if(!TMArray.isEmpty()){
            for(String item : TMArray)
                startTMCombo.addItem(item);
            startTMCombo.setEnabled(true);
        }
    }//GEN-LAST:event_startIDComboActionPerformed

    private void startTMComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startTMComboActionPerformed
        // Avoid computing when combo is disabled
        if( !startTMCombo.isEnabled()){
            return;
        }
        // Avoid computing when default element is selected
        if(startTMCombo.getSelectedItem().toString() != "<Select TM>"){
            this.startMarkerName = this.startTMCombo.getSelectedItem().toString();
            this.startOffsetField.setEnabled(true);
        }
    }//GEN-LAST:event_startTMComboActionPerformed

    private void endTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endTypeComboActionPerformed
        String s = this.endTypeCombo.getSelectedItem().toString();
        List<String>IDArray = new ArrayList<String>();
        this.resetEndIDAndTMCombo();

        // Display elements of one type
         TimeLineManager<? extends Temporizable> tlm = bmlEditor.getTimeLineManager(s);
        if (tlm != null) {
            TimeLine<? extends Temporizable> tl =  tlm.getTimeLine();
            for(String id : tl.getItemsNamesList()){
                IDArray.add(id);
            }
        }
        // If elements are found, put them in endIDCombo and enable it
        if(!IDArray.isEmpty()){
            for(String item : IDArray)
                if (!item.equalsIgnoreCase(this.edited.getId()))
                {
                    endIDCombo.addItem(item);
                }
            endIDCombo.setEnabled(true);
        }
    }//GEN-LAST:event_endTypeComboActionPerformed

    private void endIDComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endIDComboActionPerformed
        // Avoid computing when combo is disabled
        if( !endIDCombo.isEnabled()){
            return;
        }

        List<TimeMarker> TMarkersArray;
        List<String> TMArray = new ArrayList<String>();
        String s = this.endTypeCombo.getSelectedItem().toString();
        String id = this.endIDCombo.getSelectedItem().toString();
        //Reset and empty TMCombo
        this.resetEndTMCombo();

        // Avoid computing when default element selected.
        if(id.equals("<Select ID>"))
            return;

        // Find end temporizable by Type and ID
        TimeLineManager<? extends Temporizable> tlm = bmlEditor.getTimeLineManager(s);

        if (tlm != null) {
            TimeLine<? extends Temporizable> tl =  tlm.getTimeLine();
            for(TemporizableContainer<? extends Temporizable> tmpCont : tl.getItems()){
                if (tmpCont.getId() == id) {
                    endRef = tmpCont;
                    break;
                }
            }
        }

        // Whend found, get TimeMarkers from end temporizable
        TMarkersArray = endRef.getTimeMarkers();
        for(TimeMarker tm: TMarkersArray){
            TMArray.add(tm.getName());
        }
        // If TM are found, put them in endTMCombo and enable it
        if(!TMArray.isEmpty()){
            for(String item : TMArray)
                endTMCombo.addItem(item);
            endTMCombo.setEnabled(true);
        }
    }//GEN-LAST:event_endIDComboActionPerformed

    private void endTMComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endTMComboActionPerformed
        // Avoid computing when combo is disabled
        if( !endTMCombo.isEnabled()){
            return;
        }
        // Avoid computing when default element is selected
        if(endTMCombo.getSelectedItem().toString() != "<Select TM>"){
            this.endMarkerName = this.endTMCombo.getSelectedItem().toString();
            this.endOffsetField.setEnabled(true);
        }
    }//GEN-LAST:event_endTMComboActionPerformed

    private void startAbsRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startAbsRadioActionPerformed
        this.startAbsolute = true;
        this.startField.setEnabled(true);
        this.resetAllStartCombo();
    }//GEN-LAST:event_startAbsRadioActionPerformed

    private void startRelRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startRelRadioActionPerformed
        this.startAbsolute = false;
        this.startField.setEnabled(false);
        this.startTypeCombo.setEnabled(true);
    }//GEN-LAST:event_startRelRadioActionPerformed

    private void endAbsRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endAbsRadioActionPerformed
        this.endAuto = false;
        this.endAbsolute = true;
        this.endField.setEnabled(true);
        this.resetAllEndCombo();
    }//GEN-LAST:event_endAbsRadioActionPerformed

    private void endRelRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endRelRadioActionPerformed
        this.endAuto = false;
        this.endAbsolute = false;
        this.endField.setEnabled(false);
        this.endTypeCombo.setEnabled(true);
    }//GEN-LAST:event_endRelRadioActionPerformed

    private void endAutoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endAutoRadioActionPerformed
        this.endAuto = true;
        this.endField.setEnabled(false);
        this.resetAllEndCombo();
    }//GEN-LAST:event_endAutoRadioActionPerformed

    private void startOffsetFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startOffsetFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_startOffsetFieldActionPerformed

    private void endOffsetFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endOffsetFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_endOffsetFieldActionPerformed

    private void resetAllStartCombo(){
        this.startTypeCombo.setSelectedIndex(0);
        this.startTypeCombo.setEnabled(false);
        this.resetStartIDAndTMCombo();
    }

    private void resetAllEndCombo(){
        this.endTypeCombo.setSelectedIndex(0);
        this.endTypeCombo.setEnabled(false);
        this.resetEndIDAndTMCombo();
    }

    private void resetStartIDAndTMCombo(){
        this.startIDCombo.setEnabled(false);
        this.startIDCombo.removeAllItems();
        this.startIDCombo.addItem("<Select ID>");
        this.resetStartTMCombo();
    }

    private void resetEndIDAndTMCombo(){
        this.endIDCombo.setEnabled(false);
        this.endIDCombo.removeAllItems();
        this.endIDCombo.addItem("<Select ID>");
        this.resetEndTMCombo();
    }


    private void resetStartTMCombo(){
        this.startTMCombo.setEnabled(false);
        this.startTMCombo.removeAllItems();
        this.startTMCombo.addItem("<Select TM>");
        this.resetStartOffset();
    }

    private void resetEndTMCombo(){
        this.endTMCombo.setEnabled(false);
        this.endTMCombo.removeAllItems();
        this.endTMCombo.addItem("<Select TM>");
        this.resetEndOffset();
    }

    private void resetStartOffset(){
        this.startOffsetField.setEnabled(false);
        this.startOffsetField.setText(doublePrecisionReset);
    }

    private void resetEndOffset(){
        this.endOffsetField.setEnabled(false);
        this.endOffsetField.setText(doublePrecisionReset);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {}
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ParametricSignalEditor dialog = new ParametricSignalEditor(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox classComboBox;
    private javax.swing.JLabel classLabel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JRadioButton endAbsRadio;
    private javax.swing.ButtonGroup endAbsRelButtonGroup;
    private javax.swing.JRadioButton endAutoRadio;
    private javax.swing.JTextField endField;
    private javax.swing.JComboBox<String> endIDCombo;
    private javax.swing.JLabel endLabel1;
    private javax.swing.JTextField endOffsetField;
    private javax.swing.JRadioButton endRelRadio;
    private javax.swing.JComboBox<String> endTMCombo;
    private javax.swing.JComboBox<String> endTypeCombo;
    private javax.swing.JLabel fldLabel;
    private javax.swing.JTextField idField;
    private javax.swing.JLabel idLabel;
    private javax.swing.JComboBox instanceComboBox;
    private javax.swing.JLabel instanceLabel;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel pwrLabel;
    private javax.swing.JSlider sliderFLD;
    private javax.swing.JSlider sliderPWR;
    private javax.swing.JSlider sliderSPC;
    private javax.swing.JSlider sliderTMP;
    private javax.swing.JLabel spcLabel;
    private javax.swing.JRadioButton startAbsRadio;
    private javax.swing.ButtonGroup startAbsRelButtonGroup;
    private javax.swing.JTextField startField;
    private javax.swing.JComboBox<String> startIDCombo;
    private javax.swing.JLabel startLabel1;
    private javax.swing.JTextField startOffsetField;
    private javax.swing.JRadioButton startRelRadio;
    private javax.swing.JComboBox<String> startTMCombo;
    private javax.swing.JComboBox<String> startTypeCombo;
    private javax.swing.JLabel tmpLabel;
    // End of variables declaration//GEN-END:variables

}
