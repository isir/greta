/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

//import greta.core.intentions.Intention;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

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
                try {
                    process();
                } catch (TransformerException ex) {
                    Logger.getLogger(MeaningMinerFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(MeaningMinerFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(MeaningMinerFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(MeaningMinerFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
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
            e.printStackTrace();
        }
    }

    public void setModule(MeaningMinerModule mmm){
        this.module = mmm;
    }

    private void process() throws TransformerException, TransformerConfigurationException, ParserConfigurationException, SAXException, IOException{
        this.module.processText(area.getText());
        //List<Intention> intentions = this.module.processText_2(area.getText());
    }

}
