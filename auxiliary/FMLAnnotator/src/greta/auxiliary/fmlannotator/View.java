/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.fmlannotator;

import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author David Panou
 */
public class View extends JFrame implements IntentionEmitter {

    private CharacterManager charactermanager;

    private Model real;

    private final int vertical = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
    private final int horizontal = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;

    private String SEND_BUTTON;
    private String ANNOTATE_BUTTON;
    private String OPEN_BUTTON;

    private JPanel panel;
    private GroupLayout layout;

    private JTextArea area1;
    private JTextArea area2;
    private JScrollPane scPan1;
    private JScrollPane scPan2;

    private JCheckBox showTMButton;
    private JCheckBox showPauseButton;
    private JCheckBox showBoundaries;
    private JCheckBox showDA;

    private JLabel TMLab;
    private JLabel PauseLab;
    private JLabel BoundLab;
    private JLabel DALab;
    private JLabel LanguageLab;

    private boolean SHOWPAUSE;
    private boolean SHOWTM;
    private boolean SHOWBOUND;
    private boolean SHOWDA;

    private JFileChooser openFileChooser;

    private JComboBox languageComboBox;

    private JButton sendButton;
    private JButton anButton;
    private JButton opButton;

    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();

    public View(CharacterManager cm) {
        initCompenents();
        this.charactermanager = cm;
    }

    private void initCompenents() {

        SEND_BUTTON = "Send";
        ANNOTATE_BUTTON = "Annotate";
        OPEN_BUTTON = "Open";

        panel = new JPanel();
        layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        area1 = new JTextArea(15, 20);
        area2 = new JTextArea(15, 20);

        area1.setEditable(true);
        area1.setLineWrap(true);
        area2.setLineWrap(true);
        // Création d'un JScrollPane pour Area1 et Area2
        scPan1 = new JScrollPane(area1, vertical, horizontal);
        scPan2 = new JScrollPane(area2, vertical, horizontal);
        area2.setEditable(false);

        showTMButton = new JCheckBox();
        showPauseButton = new JCheckBox();
        showBoundaries = new JCheckBox();
        showDA = new JCheckBox();

        languageComboBox = new JComboBox(Model.Language.values()) {
            /**
             * @inherited <p>
             */
            @Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }
        };
        languageComboBox.setSelectedItem(Model.Language.fr_FR);
        languageComboBox.setPreferredSize(new Dimension(15,10));

        TMLab = new JLabel("show TM");
        PauseLab = new JLabel("show pause");
        BoundLab = new JLabel("show boundaries");
        DALab = new JLabel("show DA");
        LanguageLab = new JLabel("Language");

        SHOWPAUSE = false;
        SHOWTM = false;
        SHOWBOUND = false;
        SHOWDA = false;

        openFileChooser = new JFileChooser();
        openFileChooser .setCurrentDirectory(new File("./"));

        sendButton = new JButton(SEND_BUTTON);
        anButton = new JButton(ANNOTATE_BUTTON);
        opButton = new JButton(OPEN_BUTTON);

        opButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openText();
            }
        });

        anButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotate();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });

        showTMButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SHOWTM = !SHOWTM;
            }
        });

        showPauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SHOWPAUSE = !SHOWPAUSE;
            }
        });

        showDA.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SHOWDA = !SHOWDA;
            }
        });

        showBoundaries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SHOWBOUND = !SHOWBOUND;
            }
        });

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addComponent(scPan1)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(opButton)
                        .addComponent(anButton)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(LanguageLab)
                                .addComponent(languageComboBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(TMLab)
                                .addComponent(showTMButton))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(PauseLab)
                                .addComponent(showPauseButton))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(BoundLab)
                                .addComponent(showBoundaries))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(DALab)
                                .addComponent(showDA))
                )
                .addComponent(scPan2)
                .addComponent(sendButton)
        );

        layout.setVerticalGroup(
                layout.createParallelGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(scPan1)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(opButton)
                                .addComponent(anButton)
                                .addGroup(layout.createParallelGroup()
                                        .addComponent(LanguageLab)
                                        .addComponent(languageComboBox))
                                .addGroup(layout.createParallelGroup()
                                        .addComponent(TMLab)
                                        .addComponent(showTMButton))
                                .addGroup(layout.createParallelGroup()
                                        .addComponent(PauseLab)
                                        .addComponent(showPauseButton))
                                .addGroup(layout.createParallelGroup()
                                        .addComponent(BoundLab)
                                        .addComponent(showBoundaries))
                                .addGroup(layout.createParallelGroup()
                                        .addComponent(DALab)
                                        .addComponent(showDA))
                        )
                        .addComponent(sendButton)
                        .addComponent(scPan2))
        );

        add(panel);
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
        set(area1, sb.toString());
    }

    public static void set(JTextArea area, String text) {
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

    public void annotate() {
        try {
            real = new Model((Model.Language)languageComboBox.getSelectedItem());
            set(area2, real.Treat(area1.getText()));
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
    }

    public void send() {
        XMLParser fmlparser = XML.createParser();
        XMLTree fml = fmlparser.parseBuffer(area2.getText());
        if (fml != null) {
            List<Intention> intentions = FMLTranslator.FMLToIntentions(fml, this.charactermanager);
            ID id = IDProvider.createID("FMLAnnotation");
            for (IntentionPerformer performer : performers) {
                performer.performIntentions(intentions, id, new Mode(CompositionType.blend));
            }
        }

    }

    public void setSHOWPAUSE(boolean sHOWPAUSE) {
        SHOWPAUSE = sHOWPAUSE;
    }

    public boolean isSHOWPAUSE() {
        return SHOWPAUSE;
    }

    public boolean isSHOWTM() {
        return SHOWTM;
    }

    public void setSHOWTM(boolean sHOWTM) {
        SHOWTM = sHOWTM;
    }

    public boolean isSHOWBOUND() {
        return SHOWBOUND;
    }

    public void setSHOWBOUND(boolean sHOWBOUND) {
        SHOWBOUND = sHOWBOUND;
    }

    public boolean isSHOWDA() {
        return SHOWDA;
    }

    public void setSHOWDA(boolean sHOWDA) {
        SHOWDA = sHOWDA;
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        performers.add(performer);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }

}
