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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

/**
 *
 * @author Andre-Marie Pez
 */
public class MultiLineToolTip {

    private static final int TAB_SIZE = 1;
    public static class JMultiLineToolTip extends JToolTip {

        String tipText;
        JComponent component;
        protected int columns = 0;
        protected int fixedwidth = 0;

        public JMultiLineToolTip() {
            updateUI();
        }

        @Override
        public void updateUI() {
            setUI(MultiLineToolTipUI.createUI(this));
        }

        public void setColumns(int columns) {
            this.columns = columns;
            this.fixedwidth = 0;
        }

        public int getColumns() {
            return columns;
        }

        public void setFixedWidth(int width) {
            this.fixedwidth = width;
            this.columns = 0;
        }

        public int getFixedWidth() {
            return fixedwidth;
        }
    }

    public static class MultiLineToolTipUI extends BasicToolTipUI {

        static MultiLineToolTipUI sharedInstance = new MultiLineToolTipUI();
        static JToolTip tip;
        Font smallFont;
        protected CellRendererPane rendererPane;
        private static JTextArea textArea;

        public static ComponentUI createUI(JComponent c) {
            return sharedInstance;
        }

        public MultiLineToolTipUI() {
            super();
            ToolTipManager.sharedInstance().setDismissDelay(60000);
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            tip = (JToolTip) c;
            rendererPane = new CellRendererPane();
            c.add(rendererPane);
        }

        @Override
        public void uninstallUI(JComponent c) {
            super.uninstallUI(c);

            c.remove(rendererPane);
            rendererPane = null;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Dimension size = c.getSize();
            textArea.setBackground(c.getBackground());
            rendererPane.paintComponent(g, textArea, c, 1, 1,
                    size.width - 1, size.height - 1, true);
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            String tipText = ((JToolTip) c).getTipText();
            if (tipText == null) {
                return new Dimension(0, 0);
            }
            textArea = new JTextArea(tipText);
            rendererPane.removeAll();
            rendererPane.add(textArea);
            textArea.setWrapStyleWord(true);
            textArea.setFont(((JToolTip) c).getFont());
            textArea.setTabSize(TAB_SIZE);
            int width = ((JMultiLineToolTip) c).getFixedWidth();
            int columns = ((JMultiLineToolTip) c).getColumns();

            if (columns > 0) {
                textArea.setColumns(columns);
                textArea.setSize(0, 0);
                textArea.setLineWrap(true);
                textArea.setSize(textArea.getPreferredSize());
            } else if (width > 0) {
                textArea.setLineWrap(true);
                Dimension d = textArea.getPreferredSize();
                d.width = computeWidth(tipText,textArea.getFontMetrics(textArea.getFont()),width,5);
                d.height++;
                textArea.setSize(d);
            } else {
                textArea.setLineWrap(false);
            }

            Dimension dim = textArea.getPreferredSize();

            dim.height += 1;
            dim.width += 1;

            return dim;
        }

        @Override
        public Dimension getMinimumSize(JComponent c) {
            return getPreferredSize(c);
        }

        @Override
        public Dimension getMaximumSize(JComponent c) {
            return getPreferredSize(c);
        }
    }

    private static int computeWidth(String s, FontMetrics metrics, int limit, int margin){
        int max = 0;
        int tab = TAB_SIZE*metrics.getMaxAdvance();
        String [] lines = s.split("\\n");
        for(String line : lines){
            int width=widthOf(line, metrics, tab);
            if(width>limit){
                width=0;
                String [] words = line.split(" ");
                int lengths = 0;
                int space = metrics.stringWidth(" ");
                for(String word : words){
                    if(lengths>limit){
                        width = lengths;
                        break;
                    }
                    int length = widthOf(word, metrics, tab);
                    if(lengths+space+length<=limit) {
                        lengths=lengths+space+length;
                    }
                    else{
                        width=Math.max(lengths, width);
                        lengths = length;
                    }
                }
            }
            max = Math.max(max, Math.min(limit, width+margin));
        }
        return max;
    }

    private static int widthOf(String line, FontMetrics metrics, int tab){
        int width=0;
        for(int i=0;i<line.length();++i){
            String charr = line.substring(i, i+1);
            if (charr.equalsIgnoreCase("\t")) {
                int addition = tab - (width % tab);
                if (addition == 0) {
                    addition = tab;
                }
                width += addition;
            }
            else {
                width += metrics.stringWidth(charr);
            }
        }
        return width;
    }
}
