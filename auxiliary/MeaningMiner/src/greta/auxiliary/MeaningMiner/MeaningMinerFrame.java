/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.MeaningMiner;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Brian
 */
public class MeaningMinerFrame extends JFrame{

    private MeaningMinerModule module;

    private final int vertical = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
    private final int horizontal = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;

    private String SEND_BUTTON;
    private String OPEN_BUTTON;
    private JPanel panel;
    private GroupLayout layout;
    private JTextArea area;
    private JScrollPane scPan1;

    private JFileChooser openFileChooser;

    private JButton sendButton;
    private JButton opButton;

    public MeaningMinerFrame(){
        SEND_BUTTON = "Send";
        OPEN_BUTTON = "Open";

        panel = new JPanel();
        layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        area = new JTextArea(15, 20);

        area.setEditable(true);
        area.setLineWrap(true);
        // Création d'un JScrollPane pour Area
        scPan1 = new JScrollPane(area, vertical, horizontal);


        openFileChooser = new JFileChooser();
        openFileChooser .setCurrentDirectory(new File("./"));

        sendButton = new JButton(SEND_BUTTON);
        opButton = new JButton(OPEN_BUTTON);

        opButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openText();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                process();
            }
        });


        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addComponent(scPan1)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(opButton)
                )
                .addComponent(sendButton)
        );

        layout.setVerticalGroup(
                layout.createParallelGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(scPan1)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(opButton)
                        )
                        .addComponent(sendButton))
        );

        add(panel);
        setText("");
        setMinimumSize(new Dimension(600, 400));
        pack();


    }

    public void read() throws FileNotFoundException, IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(new File(openFileChooser.getSelectedFile().toString())), "UTF8"));
        String s;
        while ((s = in.readLine()) != null) {
            sb.append(s);
        }
        in.close();
        setText(sb.toString());
    }

    public void setText(String text) {
        area.setText(text);
    }

    public void openText() {
        openFileChooser.showOpenDialog(null);
        try {
            read();
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
    }

    public void setModule(MeaningMinerModule mmm){
        this.module = mmm;
    }

    private void process(){
        this.module.processText(area.getText());
    }

}
