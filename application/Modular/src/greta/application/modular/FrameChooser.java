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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JPanel;

/**
 *
 * @author Andre-Marie Pez
 */
public class FrameChooser extends JPanel {

    public static enum Result{
        FRAME_DELETED, FRAME_INVISIBLE, FRAME_VISIBLE
    }

    private Map<Module, Result> results;
    private javax.swing.JLabel emptyLabel;
    private javax.swing.JLabel presentName;
    private javax.swing.JLabel visibleName;

    public Map<Module, Result> getResults(){
        return results;
    }
    /**
     * Creates new form FrameChooser
     */
    public FrameChooser(List<Module> modules) {
        super();
        results = new HashMap<Module, Result>(modules.size());

        emptyLabel = new javax.swing.JLabel();
        presentName = new javax.swing.JLabel("Present");
        visibleName = new javax.swing.JLabel("Visible");


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);

        SequentialGroup column = layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(presentName)
                    .addComponent(visibleName)
                    .addComponent(emptyLabel));

        ParallelGroup names = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(emptyLabel);

        ParallelGroup presents = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(presentName);

        ParallelGroup visibles = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(visibleName);


        for(final Module m : modules){
            if(m.getFrame()==null){
                results.put(m, Result.FRAME_DELETED);
            }else{
                boolean mustPresent = m.getObject()==m.getFrame();
                boolean isVisible = m.getFrame().isVisible();
                boolean mayBeDelated = !mustPresent && !isVisible;
                for(ModuleFactory.ParameterInfo param : m.getInfo().parameterInfos){
                    if(param.getSetOn().equalsIgnoreCase("frame")){
                        mayBeDelated = false;
                    }
                }

                results.put(m, isVisible ? Result.FRAME_VISIBLE : mayBeDelated ? Result.FRAME_DELETED : Result.FRAME_INVISIBLE);


                final javax.swing.JCheckBox present = new javax.swing.JCheckBox();
                final javax.swing.JCheckBox visible = new javax.swing.JCheckBox();
                javax.swing.JLabel name = new javax.swing.JLabel(m.getId());

                if(mustPresent){
                    present.setSelected(true);
                    present.setEnabled(false);
                }
                else{
                    present.setSelected( ! mayBeDelated);
                }
                visible.setSelected(isVisible);

                present.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(present.isSelected()){
                            results.put(m, visible.isSelected() ? Result.FRAME_VISIBLE : Result.FRAME_INVISIBLE);
                        }
                        else{
                            visible.setSelected(false);
                            results.put(m, Result.FRAME_DELETED);
                        }
                    }
                });
                visible.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(visible.isSelected()){
                            present.setSelected(true);
                            results.put(m, Result.FRAME_VISIBLE);
                        }
                        else{
                            results.put(m, present.isSelected() ? Result.FRAME_INVISIBLE : Result.FRAME_DELETED);
                        }
                    }
                });

                column = column.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(present)
                        .addComponent(visible)
                        .addComponent(name));
                names = names.addComponent(name);
                presents = presents.addComponent(present);
                visibles = visibles.addComponent(visible);
            }
        }

        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(column.addContainerGap()));

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(names)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(presents)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(visibles)
                .addContainerGap())
        );
    }

}
