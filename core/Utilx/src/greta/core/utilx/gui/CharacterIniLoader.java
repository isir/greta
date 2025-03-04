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
package greta.core.utilx.gui;

import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.IniParameter;
import greta.core.util.log.Logs;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Andre-Marie Pez
 */
public class CharacterIniLoader extends IniLoader implements CharacterDependent{

    private CharacterManager characterManager;

    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        if(characterManager==null)
            characterManager = CharacterManager.getStaticInstance();
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        this.characterManager = characterManager;
    }

    /** Creates new form CharacterIniLoader */
    public CharacterIniLoader(CharacterManager cm) {
        setCharacterManager(cm);
        initComponents();

        ArrayList<String>names = new ArrayList<String>();
        for(IniParameter param :IniManager.getGlobals().getAll()){
            if((new File(param.getParamValue()).exists() && param.getParamValue().toLowerCase().endsWith(".ini"))){
                names.add(param.getParamName().toUpperCase());
            }
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        javax.swing.DefaultComboBoxModel model = new javax.swing.DefaultComboBoxModel(names.toArray());
        jComboBox1.setModel(model);
        jComboBox1.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                    String characterFileName = e.getItem().toString();
                    getCharacterManager().setCharacter(characterFileName);

                    //managerFrame.setDefinition(characterFileName);
                }
            }
        });
        jComboBox1.setSelectedItem(getCharacterManager().getCurrentCharacterName().toUpperCase());
        getCharacterManager().add(this);
        // Phil : To-do while constructing the tree
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "greta" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox1, 0, 56, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void onCharacterChanged() {
        String characterName = getCharacterManager().getCurrentCharacterName();
        jComboBox1.setSelectedItem(characterName.toUpperCase());
        Logs.info("Current character parameters changed.");
        IniManager.getGlobals().get("CURRENT_CHARACTER").setParamValue(characterName.toUpperCase());
        fire = false;
        managerFrame.updateIniParameters();
        fire = true;
    }

    private boolean fire = true;
    @Override
    public void fireIniChanged() {
        if(fire){
            getCharacterManager().notifyChanges();
        }
    }

    @Override
    public void fireIniDefinitionChanged(String name) {}

}
