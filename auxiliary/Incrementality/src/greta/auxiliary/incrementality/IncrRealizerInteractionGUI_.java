/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.incrementality;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author miche
 */
public class IncrRealizerInteractionGUI_ extends javax.swing.JFrame {

    /**
     * Creates new form IncrRealizerInteractionGUI_
     */
    public IncrRealizerInteractionGUI_() {
        initComponents();
    }
    
    
    private Method pauseGestureLoadMethod;
    private Method resumeLoadMethod;
    private Method stopLoadMethod;
    private Method clearQueueLoadMethod;
    private Object loader;

    protected void sendPauseGesture() {
        if(pauseGestureLoadMethod!=null){
            try {
                pauseGestureLoadMethod.invoke(loader);
            }
            catch (InvocationTargetException ex) {
                ex.getCause().printStackTrace();
            }
            catch (Exception ex) {
                System.err.println(ex);
            }
        }
        else{
            System.out.println("load is null");
        }
    }
    
    protected void sendResume() {
        if(resumeLoadMethod!=null){
            try {
                resumeLoadMethod.invoke(loader);
            }
            catch (InvocationTargetException ex) {
                ex.getCause().printStackTrace();
            }
            catch (Exception ex) {
                System.err.println(ex);
            }
        }
        else{
            System.out.println("load is null");
        }
    }
    
        protected void sendStop() {
        if(stopLoadMethod!=null){
            try {
                stopLoadMethod.invoke(loader);
            }
            catch (InvocationTargetException ex) {
                ex.getCause().printStackTrace();
            }
            catch (Exception ex) {
                System.err.println(ex);
            }
        }
        else{
            System.out.println("load is null");
        }
    }
        
        protected void sendClearQueue() {
        if(clearQueueLoadMethod!=null){
            try {
                clearQueueLoadMethod.invoke(loader);
            }
            catch (InvocationTargetException ex) {
                ex.getCause().printStackTrace();
            }
            catch (Exception ex) {
                System.err.println(ex);
            }
        }
        else{
            System.out.println("load is null");
        }
    }

    public void setLoader(Object loader){
        this.loader = loader;
        try {
            pauseGestureLoadMethod = loader.getClass().getMethod("sendPauseGesture");
            resumeLoadMethod = loader.getClass().getMethod("sendResume");
            stopLoadMethod = loader.getClass().getMethod("sendStop");
            clearQueueLoadMethod = loader.getClass().getMethod("sendClearQueue");
        } catch (Exception ex) {
            System.err.println(ex);
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

        interrupt = new javax.swing.JButton();
        resume = new javax.swing.JButton();
        stop = new javax.swing.JButton();
        clear_queue = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        interrupt.setText("Interrupt");
        interrupt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interruptActionPerformed(evt);
            }
        });

        resume.setText("Resume");
        resume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resumeActionPerformed(evt);
            }
        });

        stop.setText("Stop");
        stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopActionPerformed(evt);
            }
        });

        clear_queue.setText("Clear Thread Queue");
        clear_queue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clear_queueActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(interrupt)
                .addGap(18, 18, 18)
                .addComponent(resume)
                .addGap(18, 18, 18)
                .addComponent(stop)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clear_queue)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stop)
                    .addComponent(resume)
                    .addComponent(interrupt)
                    .addComponent(clear_queue))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void resumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resumeActionPerformed
        sendResume();
    }//GEN-LAST:event_resumeActionPerformed

    private void stopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopActionPerformed
        sendStop();
    }//GEN-LAST:event_stopActionPerformed

    private void interruptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interruptActionPerformed
        sendPauseGesture();
    }//GEN-LAST:event_interruptActionPerformed

    private void clear_queueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clear_queueActionPerformed
        sendClearQueue();
    }//GEN-LAST:event_clear_queueActionPerformed

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
            java.util.logging.Logger.getLogger(IncrRealizerInteractionGUI_.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(IncrRealizerInteractionGUI_.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(IncrRealizerInteractionGUI_.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(IncrRealizerInteractionGUI_.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new IncrRealizerInteractionGUI_().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clear_queue;
    private javax.swing.JButton interrupt;
    private javax.swing.JButton resume;
    private javax.swing.JButton stop;
    // End of variables declaration//GEN-END:variables
}