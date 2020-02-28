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
package greta.application.modular;

import greta.application.modular.modules.Module;
import greta.application.modular.modules.ModuleFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.JPanel;

/**
 *
 * @author Andre-Marie Pez
 */
public class IniManagerConfiguration extends JPanel {

    private javax.swing.JLabel usedLabel;
    private javax.swing.JLabel parameterName;
    private Map<String, String> renameing;
    private String[] frameAttributes = {"w", "h", "x", "y"};

    public static String inputName(Module m, ModuleFactory.ParameterInfo info){
        return  m.getId() + "." + info.getName();
    }

    public static String inputNameForJFrame(Module m, String attribute){
        return  m.getId() + ".window." + attribute;
    }

    public Map<String, String> getResult(){
        return renameing;
    }

    public IniManagerConfiguration(List<Module> modules, Map<Module, FrameChooser.Result> framesStatus) {
        renameing = new HashMap<String, String>(modules.size());

        usedLabel = new javax.swing.JLabel("Used");
        parameterName = new javax.swing.JLabel("Parameter Name");


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);

        GroupLayout.SequentialGroup column = layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(parameterName)
                .addComponent(usedLabel));

        GroupLayout.ParallelGroup useds = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(usedLabel);

        GroupLayout.ParallelGroup names = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(parameterName);

        for (Module m : modules) {
            FrameChooser.Result frameStaus = framesStatus.get(m);

            for (ModuleFactory.ParameterInfo info : m.getInfo().parameterInfos) {
                if (info.getSetOn().equals("object") || frameStaus != FrameChooser.Result.FRAME_DELETED) {

                    javax.swing.JCheckBox used = new javax.swing.JCheckBox();
                    javax.swing.JTextField name = new javax.swing.JTextField();

                    ComponentGroup group = new ComponentGroup(inputName(m, info), used, name);
                    used.addActionListener(group);
                    name.addActionListener(group);
                    name.addFocusListener(group);

                    column = column.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(name)
                            .addComponent(used));
                    useds = useds.addComponent(used);
                    names = names.addComponent(name);
                }
            }
            if(m.getFrame() != null && frameStaus == FrameChooser.Result.FRAME_VISIBLE){
                for(String attribute : frameAttributes){
                    javax.swing.JCheckBox used = new javax.swing.JCheckBox();
                    javax.swing.JTextField name = new javax.swing.JTextField();

                    ComponentGroup group = new ComponentGroup( inputNameForJFrame(m, attribute), used, name);
                    used.addActionListener(group);
                    name.addActionListener(group);
                    name.addFocusListener(group);

                    column = column.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(name)
                            .addComponent(used));
                    useds = useds.addComponent(used);
                    names = names.addComponent(name);
                }
            }
        }

        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(column.addContainerGap()));

        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(useds)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(names)
                .addContainerGap()));
    }

    private class ComponentGroup extends FocusAdapter implements ActionListener {

        String name;
        javax.swing.JCheckBox used;
        javax.swing.JTextField nameField;

        ComponentGroup(String name, javax.swing.JCheckBox used, javax.swing.JTextField nameField) {
            this.name = name;
            this.nameField = nameField;
            this.used = used;
            nameField.setText(name);
            nameField.setEnabled(used.isSelected());
        }

        private void update() {
            nameField.setEnabled(used.isSelected());
            if (used.isSelected()) {
                renameing.put(name, nameField.getText());
            } else {
                renameing.put(name, null);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            update();
        }

        @Override
        public void focusLost(FocusEvent e) {
            update();
        }
    }
}
