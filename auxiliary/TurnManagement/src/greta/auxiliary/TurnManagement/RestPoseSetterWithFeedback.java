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
package greta.auxiliary.TurnManagement;

import greta.core.repositories.Gestuary;
import greta.core.repositories.SignalEntry;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.signals.gesture.GestureSignal;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import greta.core.feedbacks.Callback;
import greta.core.feedbacks.FeedbackPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;

public class RestPoseSetterWithFeedback extends javax.swing.JFrame implements FeedbackPerformer {
    
    private final List<SignalPerformer> _signalPerformers = new ArrayList<SignalPerformer>();
    private String _reference ="";
    private Boolean waiting = Boolean.TRUE;
    
    /** Creates new form TTSController */
    public RestPoseSetterWithFeedback() {
        initComponents();
        create_list();
        jTextField1.setText("");
    }

    private void create_list(){ 
        //START 
        Gestuary.global_gestuary.refreshAll();

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
            if(signal.getParamName().contains("rest="))
            choice1.addItem(signal.getParamName());
        }
        if (_reference != null) {
            choice1.select(_reference);
        } else {
            choice1.select(0);
        }
    // END
            
    }
    
    private void setRestPose() {
        
        Thread th = new Thread() {
            @Override
            synchronized public void run() {

                _reference=choice1.getItem(choice1.getSelectedIndex());
                _reference=_reference.replace("restpose_","rest=");
                jTextField1.setText(_reference);
                GestureSignal gest = Gestuary.global_gestuary.getSignal(_reference);

                if (gest == null) {
                    System.err.println("GestureEditor: " + _reference + " does not exist");
                    return;
                }

                List<Signal> Gsignals = new ArrayList<Signal>();
                GestureSignal gesture = new GestureSignal("rest");
                gesture.setReference(gest.getCategory() + "=" + gest.getId());

                gesture.getStart().setValue(0);                
                gesture.getTimeMarker("ready").setValue(0.2);
                gesture.getTimeMarker("stroke-start").setValue(0.3);
                gesture.getTimeMarker("stroke-end").setValue(1.0);
                gesture.getTimeMarker("relax").setValue(1.3);
                gesture.getEnd().setValue(1.5);
                gesture.getEnd().setValue(2);
                 
                gesture.setFLD(50 / 100.0);
                gesture.setPWR(50 / 100.0);
                gesture.setTMP(50 / 100.0);
                gesture.setSPC(getSPC());
                gesture.setTension(50 / 100.0);
                Gsignals.add(gesture);
                Gestuary g = Gestuary.global_gestuary;
                ID id = IDProvider.createID("rest");

        //        try {
        //            Thread.sleep(1000);        
        //        }
        //        catch (Exception e) {
        //            
        //        }

                for (SignalPerformer perf : _signalPerformers) {
                    perf.performSignals(Gsignals, id, new Mode(CompositionType.replace));
                }
                
            }
        };
        th.start();


    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        choice1 = new java.awt.Choice();
        jButton1 = new javax.swing.JButton();

        jLabel1.setText("Actual Rest_Pose");

        jTextField1.setText("jTextField1");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jButton1.setText("Apply");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(choice1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 104, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(66, 66, 66)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(87, 87, 87))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(choice1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton1)))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        setRestPose();

             }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Choice choice1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

    private Method loadMethod;
    private Object loader;

    protected void send(String fileName) {
        if(fileName==null || fileName.isEmpty()) return ;
        if(loadMethod!=null){
            try {
                loadMethod.invoke(loader, fileName);
            }
            catch (InvocationTargetException ex) {
                ex.getCause().printStackTrace();
            }
            catch (Exception ex) {
                System.err.println("Can not invoke method load(String) on "+loader.getClass().getCanonicalName());
            }
        }
        else{
            System.out.println("load is null");
        }
    }

    public void setLoader(Object loader){
        System.out.println("greta.core.utilx.gui.OpenAndLoad.setLoader()");
        this.loader = loader;
        try {
            loadMethod = loader.getClass().getMethod("load", String.class);
        } catch (Exception ex) {
            System.err.println("Can not find method load(String) in "+loader.getClass().getCanonicalName());
        }
    }
    
    private Document parseXML(String filePath) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(filePath);
        doc.getDocumentElement().normalize();
        return doc;
    }


    public void addSignalPerformer(SignalPerformer sp) {
        if (sp != null) {
            _signalPerformers.add(sp);
        }
    }

    public void removeSignalPerformer(SignalPerformer performer) {
        if (performer != null) {
            _signalPerformers.add(performer);
        }
    }
    
    private double getSPC() {
        return (50 / 100.0) * 2 - 1;
    }
    
    ////////////////////////
    // Feedback treatment
    ////////////////////////
    


    public void performFeedback(String str) {
        System.out.println("greta.auxiliary.TurnManagement.RestPoseSetterWithFeedback.performFeedback(): str: " + str);
        if (str == "end") {
            
//            if (waiting) {
//                setRestPose();
//                waiting = Boolean.FALSE;
//            }
//            else {
//                waiting = Boolean.TRUE;
//            }
            
            setRestPose();

        }
    }    

    @Override
    public void performFeedback(ID AnimId, String type, SpeechSignal speechSignal, TimeMarker tm){
        performFeedback(type);
    };

    @Override
    public void performFeedback(ID AnimId, String type, List<Temporizable> listTmp){
        performFeedback(type);
    };

    @Override
    public void performFeedback(Callback callback){
        performFeedback(callback.type());
    };
    @Override
    public void setDetailsOption(boolean detailed){

    };

    @Override
    public boolean areDetailedFeedbacks(){
        return true  ;
    };

    @Override
    public void setDetailsOnFace(boolean detailsOnFace){

    };

    @Override
    public boolean areDetailsOnFace(){
       return false;  
    };

    @Override
    public void setDetailsOnGestures(boolean detailsOnGestures){

    };

    @Override
    public boolean areDetailsOnGestures(){
        return false;  
    };    
    
}
