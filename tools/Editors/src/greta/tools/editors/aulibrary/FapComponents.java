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
package greta.tools.editors.aulibrary;

import greta.core.animation.mpeg4.fap.FAPType;
import greta.tools.editors.SliderAndText;
import java.awt.Dimension;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;

/**
 *
 * @author Andre-Marie Pez
 */
public class FapComponents extends FacePoint{

    String name;
    String pointName;
    Movement vertical;
    Movement horizontal;
    Movement depth;

    JPanel panel;
    FacePanel facePanel;
    public void buildPanel(){
        panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        JLabel pointNameLabel = new JLabel(name);
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addContainerGap();
        verticalGroup.addComponent(pointNameLabel);
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        GroupLayout.ParallelGroup vert1 = layout.createParallelGroup();
        vert1.addComponent(pointNameLabel);
        GroupLayout.ParallelGroup vert2 = layout.createParallelGroup();
        if(horizontal!=null){
            layoutMovement(horizontal, layout, vert1, vert2, verticalGroup);
        }
        if(vertical!=null){
            layoutMovement(vertical, layout, vert1, vert2, verticalGroup);
        }
        if(depth!=null){
            layoutMovement(depth, layout, vert1, vert2, verticalGroup);
        }
        verticalGroup.addContainerGap();

        GroupLayout.SequentialGroup horizontalGroup = layout.createSequentialGroup();
        horizontalGroup.addContainerGap();
        horizontalGroup.addGroup(vert1);
        horizontalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        horizontalGroup.addGroup(vert2);
        horizontalGroup.addContainerGap();

        layout.setHorizontalGroup(horizontalGroup);
        layout.setVerticalGroup(verticalGroup);
        panel.setPreferredSize(new Dimension(150, 100));
        panel.setLayout(layout);
    }

    private void layoutMovement(Movement mov, GroupLayout layout, GroupLayout.ParallelGroup vert1, GroupLayout.ParallelGroup vert2, GroupLayout.SequentialGroup verticalGroup){
        JLabel label = new JLabel("FAP " + mov.type.ordinal() + " (" + pointName + " " + mov.axis + ")");
        label.setToolTipText(mov.type.toString());
        mov.value.slider.setToolTipText(mov.type.toString());
        mov.value.field.setToolTipText(mov.type.toString());

        vert1.addComponent(label);
        vert1.addComponent(mov.value.slider);
        vert2.addComponent(mov.value.field,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE);

        GroupLayout.ParallelGroup horiz = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
        horiz.addComponent(mov.value.slider);
        horiz.addComponent(mov.value.field, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE);
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        verticalGroup.addComponent(label);
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        verticalGroup.addGroup(horiz);
    }

    public FapComponents(FacePanel facePanel, String pointName, String name, double positionX, double positionY){
        super(positionX, positionY);
        this.facePanel = facePanel;
        this.name = name;
        this.pointName = pointName;
        vertical = null;
        horizontal = null;
        depth = null;
    }

    public void setVerticalMovement(FAPType type, int min, int max, int axeSignum, double unit){
        vertical = new Movement(type, "y", min, max, axeSignum, unit) {

            @Override
            protected void moveTo(double position) {
                FapComponents.this.y = originalY+position*axeSignum;
            }
        };
    }
    public void setHorizontalMovement(FAPType type, int min, int max, int axeSignum, double unit){
        horizontal = new Movement(type, "x", min, max, axeSignum, unit) {

            @Override
            protected void moveTo(double position) {
                FapComponents.this.x = originalX+position*axeSignum;
            }
        };
    }
    public void setDepthMovement(FAPType type, int min, int max, int axeSignum, double unit){
        depth = new Movement(type, "z", min, max, axeSignum, unit) {

            @Override
            protected void moveTo(double position) {
                //not possible
            }
        };
    }


    public abstract class Movement{
        FAPType type;
        SliderAndText value;
        String axis;
        int axeSignum;
        double unit;
        public Movement(FAPType type, String axis, int min, int max, int axeSignum, double unit){
            this.type = type;
            this.axis = axis;
            this.axeSignum = axeSignum;
            this.unit = unit;
            value = new SliderAndText(min, max) {

                @Override
                protected void onModification() {
                    moveTo(this.getValue()*Movement.this.unit);
                    facePanel.updateFrame(Movement.this.type, value.getValue());
                }
            };
        }
        protected abstract void moveTo(double position);
    }
}
