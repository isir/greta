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
package greta.tools.animation.gestureeditor;

import greta.core.signals.gesture.TrajectoryDescription;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;

/**
 *
 * @author HUANG
 */
public class TrajectoryParametersDialog extends javax.swing.JDialog {

    boolean _apply;
    double[] amplitude = {0, 0, 0};
    double[] frequency = {1, 1, 1};
    double[] shift = {0, 0, 0};
    TrajectoryDescription.Variation[] spatialVariation = {TrajectoryDescription.Variation.NONE, TrajectoryDescription.Variation.NONE, TrajectoryDescription.Variation.NONE};
    TrajectoryDescription.Variation[] temporalVariation = {TrajectoryDescription.Variation.NONE, TrajectoryDescription.Variation.NONE, TrajectoryDescription.Variation.NONE};
    String typeName = "Linear";

    /**
     * Creates new form TrajectoryParametersDialog
     */
    public TrajectoryParametersDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setTitle("TrajectoryParametersDialog");
        _apply = false;
        type.removeAllItems();
        type.addItem("Linear");
        type.addItem("Circle");
    }

    void initInterface(TrajectoryDescription trajectory) {
        amplitude[0] = trajectory.getAmplitude()[0];
        amplitude[1] = trajectory.getAmplitude()[1];
        amplitude[2] = trajectory.getAmplitude()[2];
        xAmplitude.setValue((int) (amplitude[0] * 50));
        yAmplitude.setValue((int) (amplitude[1] * 50));
        zAmplitude.setValue((int) (amplitude[2] * 50));
        frequency[0] = trajectory.getFrequency()[0];
        frequency[1] = trajectory.getFrequency()[1];
        frequency[2] = trajectory.getFrequency()[2];
        xFrequency.setValue((int) (frequency[0] * 20));
        yFrequency.setValue((int) (frequency[1] * 20));
        zFrequency.setValue((int) (frequency[2] * 20));
        shift[0] = trajectory.getShift()[0];
        shift[1] = trajectory.getShift()[1];
        shift[2] = trajectory.getShift()[2];
        xShift.setValue((int) (shift[0] * 100));
        yShift.setValue((int) (shift[0] * 100));
        zShift.setValue((int) (shift[0] * 100));
        spatialVariation[0] = trajectory.getSpatialVariation()[0];
        spatialVariation[1] = trajectory.getSpatialVariation()[1];
        spatialVariation[2] = trajectory.getSpatialVariation()[2];
        if (spatialVariation[0] == TrajectoryDescription.Variation.NONE) {
            xEqualSpatial.setSelected(true);
        } else if (spatialVariation[0] == TrajectoryDescription.Variation.GREATER) {
            xPlusSpatial.setSelected(true);
        } else if (spatialVariation[0] == TrajectoryDescription.Variation.SMALLER) {
            xMinusSpatial.setSelected(true);
        }
        if (spatialVariation[1] == TrajectoryDescription.Variation.NONE) {
            yEqualSpatial.setSelected(true);
        } else if (spatialVariation[1] == TrajectoryDescription.Variation.GREATER) {
            xPlusSpatial.setSelected(true);
        } else if (spatialVariation[1] == TrajectoryDescription.Variation.SMALLER) {
            xMinusSpatial.setSelected(true);
        }
        if (spatialVariation[2] == TrajectoryDescription.Variation.NONE) {
            zEqualSpatial.setSelected(true);
        } else if (spatialVariation[2] == TrajectoryDescription.Variation.GREATER) {
            zPlusSpatial.setSelected(true);
        } else if (spatialVariation[2] == TrajectoryDescription.Variation.SMALLER) {
            zMinusSpatial.setSelected(true);
        }
        temporalVariation[0] = trajectory.getTemporalVariation()[0];
        temporalVariation[1] = trajectory.getTemporalVariation()[1];
        temporalVariation[2] = trajectory.getTemporalVariation()[2];
        if (temporalVariation[0] == TrajectoryDescription.Variation.NONE) {
            xEqualTemporal.setSelected(true);
        } else if (temporalVariation[0] == TrajectoryDescription.Variation.GREATER) {
            xPlusTemporal.setSelected(true);
        } else if (temporalVariation[0] == TrajectoryDescription.Variation.SMALLER) {
            xMinusTemporal.setSelected(true);
        }
        if (temporalVariation[1] == TrajectoryDescription.Variation.NONE) {
            yEqualTemporal.setSelected(true);
        } else if (temporalVariation[1] == TrajectoryDescription.Variation.GREATER) {
            xPlusTemporal.setSelected(true);
        } else if (temporalVariation[1] == TrajectoryDescription.Variation.SMALLER) {
            xMinusTemporal.setSelected(true);
        }
        if (temporalVariation[2] == TrajectoryDescription.Variation.NONE) {
            zEqualTemporal.setSelected(true);
        } else if (temporalVariation[2] == TrajectoryDescription.Variation.GREATER) {
            zPlusTemporal.setSelected(true);
        } else if (temporalVariation[2] == TrajectoryDescription.Variation.SMALLER) {
            zMinusTemporal.setSelected(true);
        }
        type.setSelectedItem(trajectory.getName());
        updateGraph();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        xSpatial = new javax.swing.ButtonGroup();
        xTemporal = new javax.swing.ButtonGroup();
        ySpatial = new javax.swing.ButtonGroup();
        yTemporal = new javax.swing.ButtonGroup();
        zSpatial = new javax.swing.ButtonGroup();
        zTemporal = new javax.swing.ButtonGroup();
        type = new javax.swing.JComboBox();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        x = new javax.swing.JPanel();
        xAmplitudeLabel = new javax.swing.JLabel();
        xFrequencyLabel = new javax.swing.JLabel();
        xShiftLabel = new javax.swing.JLabel();
        xAmplitude = new javax.swing.JSlider();
        xFrequency = new javax.swing.JSlider();
        xShift = new javax.swing.JSlider();
        xSpatialVariationLabel = new javax.swing.JLabel();
        xEqualSpatial = new javax.swing.JRadioButton();
        xPlusSpatial = new javax.swing.JRadioButton();
        xMinusSpatial = new javax.swing.JRadioButton();
        xTemporalVariationLabel = new javax.swing.JLabel();
        xEqualTemporal = new javax.swing.JRadioButton();
        xPlusTemporal = new javax.swing.JRadioButton();
        xMinusTemporal = new javax.swing.JRadioButton();
        y = new javax.swing.JPanel();
        yAmplitudeLabel = new javax.swing.JLabel();
        yFrequencyLabel = new javax.swing.JLabel();
        yShiftLabel = new javax.swing.JLabel();
        yAmplitude = new javax.swing.JSlider();
        yFrequency = new javax.swing.JSlider();
        yShift = new javax.swing.JSlider();
        ySpatialVariationLabel = new javax.swing.JLabel();
        yEqualSpatial = new javax.swing.JRadioButton();
        yPlusSpatial = new javax.swing.JRadioButton();
        yMinusSpatial = new javax.swing.JRadioButton();
        yTemporalVariationLabel = new javax.swing.JLabel();
        yEqualTemporal = new javax.swing.JRadioButton();
        yPlusTemporal = new javax.swing.JRadioButton();
        yMinusTemporal = new javax.swing.JRadioButton();
        z = new javax.swing.JPanel();
        zAmplitudeLabel = new javax.swing.JLabel();
        zFrequencyLabel = new javax.swing.JLabel();
        zShiftLabel = new javax.swing.JLabel();
        zAmplitude = new javax.swing.JSlider();
        zFrequency = new javax.swing.JSlider();
        zShift = new javax.swing.JSlider();
        zSpatialVariationLabel = new javax.swing.JLabel();
        zEqualSpatial = new javax.swing.JRadioButton();
        zPlusSpatial = new javax.swing.JRadioButton();
        zMinusSpatial = new javax.swing.JRadioButton();
        zTemporalVariationLabel = new javax.swing.JLabel();
        zEqualTemporal = new javax.swing.JRadioButton();
        zPlusTemporal = new javax.swing.JRadioButton();
        zMinusTemporal = new javax.swing.JRadioButton();
        trajectoryMPanelxy = new TrajectoryExamplePanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("TrajectoryParametersDialog");

        type.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        type.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                typeItemStateChanged(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        x.setBorder(javax.swing.BorderFactory.createTitledBorder("x"));

        xAmplitudeLabel.setText("Amplitude:");

        xFrequencyLabel.setText("Frequency:");

        xShiftLabel.setText("Shift:");

        xAmplitude.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                xAmplitudeMouseReleased(evt);
            }
        });

        xFrequency.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                xFrequencyMouseReleased(evt);
            }
        });

        xShift.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                xShiftMouseReleased(evt);
            }
        });

        xSpatialVariationLabel.setText("SpatialVariation");

        xSpatial.add(xEqualSpatial);
        xEqualSpatial.setSelected(true);
        xEqualSpatial.setText("=");
        xEqualSpatial.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                xEqualSpatialItemStateChanged(evt);
            }
        });

        xSpatial.add(xPlusSpatial);
        xPlusSpatial.setText("+");
        xPlusSpatial.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                xPlusSpatialItemStateChanged(evt);
            }
        });

        xSpatial.add(xMinusSpatial);
        xMinusSpatial.setText(" -");
        xMinusSpatial.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                xMinusSpatialItemStateChanged(evt);
            }
        });

        xTemporalVariationLabel.setText("TemporalVariation");

        xTemporal.add(xEqualTemporal);
        xEqualTemporal.setSelected(true);
        xEqualTemporal.setText("=");
        xEqualTemporal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                xEqualTemporalItemStateChanged(evt);
            }
        });

        xTemporal.add(xPlusTemporal);
        xPlusTemporal.setText("+");
        xPlusTemporal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                xPlusTemporalItemStateChanged(evt);
            }
        });

        xTemporal.add(xMinusTemporal);
        xMinusTemporal.setText(" -");
        xMinusTemporal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                xMinusTemporalItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout xLayout = new javax.swing.GroupLayout(x);
        x.setLayout(xLayout);
        xLayout.setHorizontalGroup(
            xLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(xLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(xLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(xAmplitudeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xFrequencyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xShiftLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(xLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(xFrequency, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                    .addComponent(xAmplitude, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(xShift, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(xLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(xSpatialVariationLabel)
                    .addComponent(xEqualSpatial)
                    .addComponent(xPlusSpatial)
                    .addComponent(xMinusSpatial))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(xLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(xTemporalVariationLabel)
                    .addComponent(xEqualTemporal)
                    .addComponent(xPlusTemporal)
                    .addComponent(xMinusTemporal))
                .addContainerGap())
        );
        xLayout.setVerticalGroup(
            xLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(xLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(xLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(xAmplitude, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xAmplitudeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(xLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(xFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xFrequencyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(xLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(xShift, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xShiftLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(xLayout.createSequentialGroup()
                .addGroup(xLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(xLayout.createSequentialGroup()
                        .addComponent(xSpatialVariationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xEqualSpatial)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xPlusSpatial)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xMinusSpatial))
                    .addGroup(xLayout.createSequentialGroup()
                        .addComponent(xTemporalVariationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xEqualTemporal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xPlusTemporal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xMinusTemporal)))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        y.setBorder(javax.swing.BorderFactory.createTitledBorder("y"));

        yAmplitudeLabel.setText("Amplitude:");

        yFrequencyLabel.setText("Frequency:");

        yShiftLabel.setText("Shift:");

        yAmplitude.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                yAmplitudeMouseReleased(evt);
            }
        });

        yFrequency.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                yFrequencyMouseReleased(evt);
            }
        });

        yShift.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                yShiftMouseReleased(evt);
            }
        });

        ySpatialVariationLabel.setText("SpatialVariation");

        ySpatial.add(yEqualSpatial);
        yEqualSpatial.setSelected(true);
        yEqualSpatial.setText("=");
        yEqualSpatial.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                yEqualSpatialItemStateChanged(evt);
            }
        });

        ySpatial.add(yPlusSpatial);
        yPlusSpatial.setText("+");
        yPlusSpatial.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                yPlusSpatialItemStateChanged(evt);
            }
        });

        ySpatial.add(yMinusSpatial);
        yMinusSpatial.setText(" -");
        yMinusSpatial.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                yMinusSpatialItemStateChanged(evt);
            }
        });

        yTemporalVariationLabel.setText("TemporalVariation");

        yTemporal.add(yEqualTemporal);
        yEqualTemporal.setSelected(true);
        yEqualTemporal.setText("=");
        yEqualTemporal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                yEqualTemporalItemStateChanged(evt);
            }
        });

        yTemporal.add(yPlusTemporal);
        yPlusTemporal.setText("+");
        yPlusTemporal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                yPlusTemporalItemStateChanged(evt);
            }
        });

        yTemporal.add(yMinusTemporal);
        yMinusTemporal.setText(" -");
        yMinusTemporal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                yMinusTemporalItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout yLayout = new javax.swing.GroupLayout(y);
        y.setLayout(yLayout);
        yLayout.setHorizontalGroup(
            yLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(yLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(yLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(yAmplitudeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(yFrequencyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(yShiftLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(yLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(yFrequency, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                    .addComponent(yAmplitude, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(yShift, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(yLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(ySpatialVariationLabel)
                    .addComponent(yEqualSpatial)
                    .addComponent(yPlusSpatial)
                    .addComponent(yMinusSpatial))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(yLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(yTemporalVariationLabel)
                    .addComponent(yEqualTemporal)
                    .addComponent(yPlusTemporal)
                    .addComponent(yMinusTemporal))
                .addContainerGap())
        );
        yLayout.setVerticalGroup(
            yLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(yLayout.createSequentialGroup()
                .addGroup(yLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(yLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(yLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(yAmplitude, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(yAmplitudeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(yLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(yFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yFrequencyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(yLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(yShift, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(yShiftLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(yLayout.createSequentialGroup()
                        .addComponent(ySpatialVariationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yEqualSpatial)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yPlusSpatial)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yMinusSpatial))
                    .addGroup(yLayout.createSequentialGroup()
                        .addComponent(yTemporalVariationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yEqualTemporal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yPlusTemporal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yMinusTemporal)))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        z.setBorder(javax.swing.BorderFactory.createTitledBorder("z"));

        zAmplitudeLabel.setText("Amplitude:");

        zFrequencyLabel.setText("Frequency:");

        zShiftLabel.setText("Shift:");

        zAmplitude.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                zAmplitudeMouseReleased(evt);
            }
        });

        zFrequency.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                zFrequencyMouseReleased(evt);
            }
        });

        zShift.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                zShiftMouseReleased(evt);
            }
        });

        zSpatialVariationLabel.setText("SpatialVariation");

        zSpatial.add(zEqualSpatial);
        zEqualSpatial.setSelected(true);
        zEqualSpatial.setText("=");
        zEqualSpatial.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                zEqualSpatialItemStateChanged(evt);
            }
        });

        zSpatial.add(zPlusSpatial);
        zPlusSpatial.setText("+");
        zPlusSpatial.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                zPlusSpatialItemStateChanged(evt);
            }
        });

        zSpatial.add(zMinusSpatial);
        zMinusSpatial.setText(" -");
        zMinusSpatial.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                zMinusSpatialItemStateChanged(evt);
            }
        });

        zTemporalVariationLabel.setText("TemporalVariation");

        zTemporal.add(zEqualTemporal);
        zEqualTemporal.setSelected(true);
        zEqualTemporal.setText("=");
        zEqualTemporal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                zEqualTemporalItemStateChanged(evt);
            }
        });

        zTemporal.add(zPlusTemporal);
        zPlusTemporal.setText("+");
        zPlusTemporal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                zPlusTemporalItemStateChanged(evt);
            }
        });

        zTemporal.add(zMinusTemporal);
        zMinusTemporal.setText(" -");
        zMinusTemporal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                zMinusTemporalItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout zLayout = new javax.swing.GroupLayout(z);
        z.setLayout(zLayout);
        zLayout.setHorizontalGroup(
            zLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(zLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(zLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(zAmplitudeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zFrequencyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zShiftLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(zLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(zFrequency, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                    .addComponent(zAmplitude, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(zShift, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(zLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(zSpatialVariationLabel)
                    .addComponent(zEqualSpatial)
                    .addComponent(zPlusSpatial)
                    .addComponent(zMinusSpatial))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(zLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(zTemporalVariationLabel)
                    .addComponent(zEqualTemporal)
                    .addComponent(zPlusTemporal)
                    .addComponent(zMinusTemporal))
                .addContainerGap())
        );
        zLayout.setVerticalGroup(
            zLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(zLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(zLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(zAmplitude, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zAmplitudeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(zLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(zFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zFrequencyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(zLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(zShift, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zShiftLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(zLayout.createSequentialGroup()
                .addGroup(zLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(zLayout.createSequentialGroup()
                        .addComponent(zSpatialVariationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zEqualSpatial)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zPlusSpatial)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zMinusSpatial))
                    .addGroup(zLayout.createSequentialGroup()
                        .addComponent(zTemporalVariationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zEqualTemporal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zPlusTemporal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zMinusTemporal)))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout trajectoryMPanelxyLayout = new javax.swing.GroupLayout(trajectoryMPanelxy);
        trajectoryMPanelxy.setLayout(trajectoryMPanelxyLayout);
        trajectoryMPanelxyLayout.setHorizontalGroup(
            trajectoryMPanelxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        trajectoryMPanelxyLayout.setVerticalGroup(
            trajectoryMPanelxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 152, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(trajectoryMPanelxy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(x, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(y, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(z, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton)
                    .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(x, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(trajectoryMPanelxy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        _apply = true;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        _apply = false;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void xAmplitudeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xAmplitudeMouseReleased
        amplitude[0] = xAmplitude.getValue() * 0.02;
        updateGraph();
    }//GEN-LAST:event_xAmplitudeMouseReleased

    private void xFrequencyMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xFrequencyMouseReleased
        frequency[0] = xFrequency.getValue() * 0.05;
        updateGraph();
    }//GEN-LAST:event_xFrequencyMouseReleased

    private void xShiftMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xShiftMouseReleased
        shift[0] = xShift.getValue() * 0.01;
        updateGraph();
    }//GEN-LAST:event_xShiftMouseReleased

    private void yAmplitudeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yAmplitudeMouseReleased
        amplitude[1] = yAmplitude.getValue() * 0.02;
        updateGraph();
    }//GEN-LAST:event_yAmplitudeMouseReleased

    private void yFrequencyMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yFrequencyMouseReleased
        frequency[1] = yFrequency.getValue() * 0.05;
        updateGraph();
    }//GEN-LAST:event_yFrequencyMouseReleased

    private void yShiftMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yShiftMouseReleased
        shift[1] = yShift.getValue() * 0.01;
        updateGraph();
    }//GEN-LAST:event_yShiftMouseReleased

    private void zAmplitudeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zAmplitudeMouseReleased
        amplitude[2] = zAmplitude.getValue() * 0.02;
        updateGraph();
    }//GEN-LAST:event_zAmplitudeMouseReleased

    private void zFrequencyMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zFrequencyMouseReleased
        frequency[2] = zFrequency.getValue() * 0.05;
        updateGraph();
    }//GEN-LAST:event_zFrequencyMouseReleased

    private void zShiftMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zShiftMouseReleased
        shift[2] = zShift.getValue() * 0.01;
        updateGraph();
    }//GEN-LAST:event_zShiftMouseReleased

    private void xEqualSpatialItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_xEqualSpatialItemStateChanged
        if (xEqualSpatial.isSelected()) {
            spatialVariation[0] = TrajectoryDescription.Variation.NONE;
            updateGraph();
        }
    }//GEN-LAST:event_xEqualSpatialItemStateChanged

    private void xPlusSpatialItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_xPlusSpatialItemStateChanged
        if (xPlusSpatial.isSelected()) {
            spatialVariation[0] = TrajectoryDescription.Variation.GREATER;
            updateGraph();
        }
    }//GEN-LAST:event_xPlusSpatialItemStateChanged

    private void xMinusSpatialItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_xMinusSpatialItemStateChanged
        if (xMinusSpatial.isSelected()) {
            spatialVariation[0] = TrajectoryDescription.Variation.SMALLER;
            updateGraph();
        }
    }//GEN-LAST:event_xMinusSpatialItemStateChanged

    private void xEqualTemporalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_xEqualTemporalItemStateChanged
        if (xEqualTemporal.isSelected()) {
            temporalVariation[0] = TrajectoryDescription.Variation.NONE;
            updateGraph();
        }
    }//GEN-LAST:event_xEqualTemporalItemStateChanged

    private void xPlusTemporalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_xPlusTemporalItemStateChanged
        if (xPlusTemporal.isSelected()) {
            temporalVariation[0] = TrajectoryDescription.Variation.GREATER;
            updateGraph();
        }
    }//GEN-LAST:event_xPlusTemporalItemStateChanged

    private void xMinusTemporalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_xMinusTemporalItemStateChanged
        if (xMinusTemporal.isSelected()) {
            temporalVariation[0] = TrajectoryDescription.Variation.SMALLER;
            updateGraph();
        }
    }//GEN-LAST:event_xMinusTemporalItemStateChanged

    private void yEqualSpatialItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_yEqualSpatialItemStateChanged
        if (yEqualSpatial.isSelected()) {
            spatialVariation[1] = TrajectoryDescription.Variation.NONE;
            updateGraph();
        }
    }//GEN-LAST:event_yEqualSpatialItemStateChanged

    private void yPlusSpatialItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_yPlusSpatialItemStateChanged
        if (yPlusSpatial.isSelected()) {
            spatialVariation[1] = TrajectoryDescription.Variation.GREATER;
            updateGraph();
        }
    }//GEN-LAST:event_yPlusSpatialItemStateChanged

    private void yMinusSpatialItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_yMinusSpatialItemStateChanged
        if (yMinusSpatial.isSelected()) {
            spatialVariation[1] = TrajectoryDescription.Variation.SMALLER;
            updateGraph();
        }
    }//GEN-LAST:event_yMinusSpatialItemStateChanged

    private void yEqualTemporalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_yEqualTemporalItemStateChanged
        if (yEqualTemporal.isSelected()) {
            temporalVariation[1] = TrajectoryDescription.Variation.NONE;
            updateGraph();
        }
    }//GEN-LAST:event_yEqualTemporalItemStateChanged

    private void yPlusTemporalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_yPlusTemporalItemStateChanged
        if (yPlusTemporal.isSelected()) {
            temporalVariation[1] = TrajectoryDescription.Variation.GREATER;
            updateGraph();
        }
    }//GEN-LAST:event_yPlusTemporalItemStateChanged

    private void yMinusTemporalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_yMinusTemporalItemStateChanged
        if (yMinusTemporal.isSelected()) {
            temporalVariation[1] = TrajectoryDescription.Variation.SMALLER;
            updateGraph();
        }
    }//GEN-LAST:event_yMinusTemporalItemStateChanged

    private void zEqualSpatialItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_zEqualSpatialItemStateChanged
        if (zEqualSpatial.isSelected()) {
            spatialVariation[2] = TrajectoryDescription.Variation.NONE;
            updateGraph();
        }
    }//GEN-LAST:event_zEqualSpatialItemStateChanged

    private void zPlusSpatialItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_zPlusSpatialItemStateChanged
        if (zPlusSpatial.isSelected()) {
            spatialVariation[2] = TrajectoryDescription.Variation.GREATER;
            updateGraph();
        }
    }//GEN-LAST:event_zPlusSpatialItemStateChanged

    private void zMinusSpatialItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_zMinusSpatialItemStateChanged
        if (zMinusSpatial.isSelected()) {
            spatialVariation[2] = TrajectoryDescription.Variation.SMALLER;
            updateGraph();
        }
    }//GEN-LAST:event_zMinusSpatialItemStateChanged

    private void zEqualTemporalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_zEqualTemporalItemStateChanged
        if (zEqualTemporal.isSelected()) {
            temporalVariation[2] = TrajectoryDescription.Variation.NONE;
            updateGraph();
        }
    }//GEN-LAST:event_zEqualTemporalItemStateChanged

    private void zPlusTemporalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_zPlusTemporalItemStateChanged
        if (zPlusTemporal.isSelected()) {
            temporalVariation[2] = TrajectoryDescription.Variation.GREATER;
            updateGraph();
        }
    }//GEN-LAST:event_zPlusTemporalItemStateChanged

    private void zMinusTemporalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_zMinusTemporalItemStateChanged
        if (zMinusTemporal.isSelected()) {
            temporalVariation[2] = TrajectoryDescription.Variation.SMALLER;
            updateGraph();
        }
    }//GEN-LAST:event_zMinusTemporalItemStateChanged

    private void typeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_typeItemStateChanged
        if (type.getItemCount() < 1) {
            return;
        }
        typeName = type.getSelectedItem().toString();
    }//GEN-LAST:event_typeItemStateChanged

    public void showModalDialog(TrajectoryDescription trajectory) {
        if (_apply) {
            trajectory.setName(new String(typeName));
            trajectory.setAmplitude(amplitude);
            trajectory.setFrequency(frequency);
            trajectory.setShift(shift);
            trajectory.setSpatialVariation(spatialVariation);
            trajectory.setTemporalVariation(temporalVariation);
            trajectory.setUsed(true);
        }
        _apply = false;
    }


    void updateGraph() {
        TrajectoryDescription trajectory = new TrajectoryDescription();
        trajectory.setName(new String(typeName));
        trajectory.setAmplitude(amplitude);
        trajectory.setFrequency(frequency);
        trajectory.setShift(shift);
        trajectory.setSpatialVariation(spatialVariation);
        trajectory.setTemporalVariation(temporalVariation);
        trajectory.setUsed(true);
        trajectory.setStartPosition(new Vec3d(0, 0, 0));
        trajectory.setEndPosition(new Vec3d(10, 0, 0));
        ArrayList<Vec3d> points = new ArrayList<Vec3d>();
        for (float i = 0; i < 1; i += 0.01f) {
            points.add(new Vec3d(trajectory.getForAxisX(i) * 10, trajectory.getForAxisY(i) * 10, trajectory.getForAxisZ(i) * 10));
        }
        ((TrajectoryExamplePanel) trajectoryMPanelxy).updatePointlist(points);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel trajectoryMPanelxy;
    private javax.swing.JComboBox type;
    private javax.swing.JPanel x;
    private javax.swing.JSlider xAmplitude;
    private javax.swing.JLabel xAmplitudeLabel;
    private javax.swing.JRadioButton xEqualSpatial;
    private javax.swing.JRadioButton xEqualTemporal;
    private javax.swing.JSlider xFrequency;
    private javax.swing.JLabel xFrequencyLabel;
    private javax.swing.JRadioButton xMinusSpatial;
    private javax.swing.JRadioButton xMinusTemporal;
    private javax.swing.JRadioButton xPlusSpatial;
    private javax.swing.JRadioButton xPlusTemporal;
    private javax.swing.JSlider xShift;
    private javax.swing.JLabel xShiftLabel;
    private javax.swing.ButtonGroup xSpatial;
    private javax.swing.JLabel xSpatialVariationLabel;
    private javax.swing.ButtonGroup xTemporal;
    private javax.swing.JLabel xTemporalVariationLabel;
    private javax.swing.JPanel y;
    private javax.swing.JSlider yAmplitude;
    private javax.swing.JLabel yAmplitudeLabel;
    private javax.swing.JRadioButton yEqualSpatial;
    private javax.swing.JRadioButton yEqualTemporal;
    private javax.swing.JSlider yFrequency;
    private javax.swing.JLabel yFrequencyLabel;
    private javax.swing.JRadioButton yMinusSpatial;
    private javax.swing.JRadioButton yMinusTemporal;
    private javax.swing.JRadioButton yPlusSpatial;
    private javax.swing.JRadioButton yPlusTemporal;
    private javax.swing.JSlider yShift;
    private javax.swing.JLabel yShiftLabel;
    private javax.swing.ButtonGroup ySpatial;
    private javax.swing.JLabel ySpatialVariationLabel;
    private javax.swing.ButtonGroup yTemporal;
    private javax.swing.JLabel yTemporalVariationLabel;
    private javax.swing.JPanel z;
    private javax.swing.JSlider zAmplitude;
    private javax.swing.JLabel zAmplitudeLabel;
    private javax.swing.JRadioButton zEqualSpatial;
    private javax.swing.JRadioButton zEqualTemporal;
    private javax.swing.JSlider zFrequency;
    private javax.swing.JLabel zFrequencyLabel;
    private javax.swing.JRadioButton zMinusSpatial;
    private javax.swing.JRadioButton zMinusTemporal;
    private javax.swing.JRadioButton zPlusSpatial;
    private javax.swing.JRadioButton zPlusTemporal;
    private javax.swing.JSlider zShift;
    private javax.swing.JLabel zShiftLabel;
    private javax.swing.ButtonGroup zSpatial;
    private javax.swing.JLabel zSpatialVariationLabel;
    private javax.swing.ButtonGroup zTemporal;
    private javax.swing.JLabel zTemporalVariationLabel;
    // End of variables declaration//GEN-END:variables
}
