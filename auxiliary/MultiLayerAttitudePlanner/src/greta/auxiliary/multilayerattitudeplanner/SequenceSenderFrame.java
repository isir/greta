/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.multilayerattitudeplanner;

import au.com.bytecode.opencsv.CSVReader;
import greta.auxiliary.multilayerattitudeplanner.structures.FrequentSequence;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import greta.auxiliary.multilayerattitudeplanner.structures.AttitudeCluster;
import greta.auxiliary.multilayerattitudeplanner.structures.NVBEventType;
import greta.core.util.log.Logs;

/**
 * Frame I used to display frequent sequences
 * It is not used any more and the send button won't work because data 
 * structures have changed (need to update the jButtonSendActionPerformed function 
 * to use NVBEventType lists and the SequenceSignalProvider)
 * @author Mathieu
 */
public class SequenceSenderFrame extends javax.swing.JFrame {

    private SequencePlanner seqPlanner;
    private SequenceTableModel stm;
    private List<FrequentSequence> sequences;
    
    
    /**
     * Creates new form SequenceSenderFrame
     */
    public SequenceSenderFrame() {
        initComponents();
        seqPlanner = null;
        stm = new SequenceTableModel();
        jTable1.setModel(stm);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                setSelectedSequenceID(jTable1.getSelectedRow());
            }
        });
        jTable1.setAutoCreateRowSorter(true);
        jFormattedTextFieldSeqID.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter()));
    }
    
    
    public void loadSequences(String filename) throws FileNotFoundException, IOException, Exception {
        //input
        CSVReader csvr = new CSVReader(new FileReader(filename), ',');
        
        //handle column definitions
        int nCol = csvr.readNext().length; //data definition (first line)not used, but could be
        String[] nextLine;

        int nLine = 0;
        //handle rest of file
        while ((nextLine = csvr.readNext()) != null) {
            nLine++;
            if (nextLine.length != nCol) {
                throw new Exception("Line " + nLine + " in " + filename + " has a wrong amount of columns");
            } else {
                sequences.add(new FrequentSequence(nextLine, nLine - 1));
            }
        }
    }

    /*public FrequentSequence findMatchingFrequentSequence(List<NVBEventType> signals, AttitudeCluster attitude) {
        for (FrequentSequence fs : sequences) {
            boolean found = true;
            if (fs.getNvbEvents().size() == signals.size() && attitude.getClusterID() == fs.getClusterID()) {
                for (int i = 0; i < signals.size(); i++) {
                    if (!signals.get(i).equals(fs.getNvbEvents().get(i))) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    return fs;
                }
            }
        }
        return null;
    }*/

    public int getSequencesCount() {
        return sequences.size();
    }

    public List<FrequentSequence> getSequences() {
        return sequences;
    }
    

    private void setSelectedSequenceID(int selectedRow) {
        jTable1.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
        jTable1.scrollRectToVisible(jTable1.getCellRect(selectedRow, 0, true));
        jFormattedTextFieldSeqID.setText(String.valueOf(selectedRow));
        jSliderSeqID.setValue(selectedRow);
    }
    
    class SequenceTableModel extends AbstractTableModel {
        private List<FrequentSequence> sequences;
        
        public SequenceTableModel()
        {
            this.sequences = new ArrayList<FrequentSequence>();
        }
        
        public void setSequences(List<FrequentSequence> seq)
        {
            this.sequences = seq;
            this.fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return sequences.size();
        }

        @Override
        public String getColumnName(int columnIndex){
            switch(columnIndex)
            {
                case 0: {return "ID";}
                case 1: {return "Sequence of NVBEvents";}
                case 2: {return "Dimension";} 
                case 3: {return "Turn";} 
                case 4: {return "Value";}
                case 5: {return "Support";} 
                case 6: {return "Confidence";} 
                case 7: {return "Lift";} 
                case 8: {return "Conviction";} 
            }
            return "";
        }
        
        @Override
        public int getColumnCount() {
            return 9;
            //id, sequence, dim, turn, value, support, confidence, lift, conviction
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            FrequentSequence seq = sequences.get(rowIndex);
            switch(columnIndex)
            {
                case 0: {return seq.getID();} //ID
                case 1: //Sequence of NVBEvents
                {
                    String returned_s = "";
                    for(String s:seq.getNvbEvents())
                        {returned_s+=s+" ";}
                    return returned_s;
                }
                case 2: {return seq.getAttitudeDimension().toString();} //Dimension
                case 3: {return seq.getTurn().toString();} //Turn
                case 4: {return seq.getValue();} //Value
                case 5: {return seq.getSupport();} //Support
                case 6: {return seq.getConfidence();} //Confidence
                case 7: {return seq.getLift();} //Lift
                case 8: {return seq.getConviction();} //Conviction
            }
            return null;
        }
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jButtonSend = new javax.swing.JButton();
        jButtonLoad = new javax.swing.JButton();
        jButtonBrowse = new javax.swing.JButton();
        jTextFieldSeqFilePath = new javax.swing.JTextField();
        jSliderSeqID = new javax.swing.JSlider();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jFormattedTextFieldSeqID = new javax.swing.JFormattedTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButtonSend.setText("Send");
        jButtonSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSendActionPerformed(evt);
            }
        });

        jButtonLoad.setText("Load");
        jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadActionPerformed(evt);
            }
        });

        jButtonBrowse.setText("Browse");
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
            }
        });

        jTextFieldSeqFilePath.setText("C:\\PROJECTS\\VIB\\bin\\Common\\Data\\MultiLayerAttitude\\Sequences.csv");

        jSliderSeqID.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderSeqIDStateChanged(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jFormattedTextFieldSeqID.setEditable(false);
        jFormattedTextFieldSeqID.setText("0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextFieldSeqFilePath, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonBrowse)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonLoad)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSend)
                        .addGap(9, 9, 9))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jFormattedTextFieldSeqID, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSliderSeqID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSliderSeqID, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFormattedTextFieldSeqID))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldSeqFilePath)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonSend)
                        .addComponent(jButtonLoad)
                        .addComponent(jButtonBrowse)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        // TODO add your handling code here:                                                                      
        jFileChooser1.setLocale(Locale.getDefault());
        jFileChooser1.updateUI();
        if(jFileChooser1.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION){
            File file = jFileChooser1.getSelectedFile();
            this.jTextFieldSeqFilePath.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonBrowseActionPerformed

    private void jButtonSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSendActionPerformed
        // TODO add your handling code here:
        //frequentSequenceToSignals(stm.sequences.get((Integer)jTable1.getValueAt(jTable1.getSelectedRow(), 0)));
    }//GEN-LAST:event_jButtonSendActionPerformed

    private void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadActionPerformed
        // TODO add your handling code here:
        try {
            loadSequences(jTextFieldSeqFilePath.getText());
            stm.setSequences(sequences);
            jSliderSeqID.setMaximum(sequences.size()-1);
        } catch (Exception ex) {
            Logs.error(ex.getMessage());
        }
    }//GEN-LAST:event_jButtonLoadActionPerformed

    private void jSliderSeqIDStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderSeqIDStateChanged
        // TODO add your handling code here:
        setSelectedSequenceID(jSliderSeqID.getValue());
    }//GEN-LAST:event_jSliderSeqIDStateChanged
    
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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SequenceSenderFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SequenceSenderFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SequenceSenderFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SequenceSenderFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SequenceSenderFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JButton jButtonSend;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JFormattedTextField jFormattedTextFieldSeqID;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSliderSeqID;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextFieldSeqFilePath;
    // End of variables declaration//GEN-END:variables
}
