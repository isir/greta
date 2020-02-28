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
package greta.tools.editors.bml.timelines;

import greta.core.signals.SpeechSignal;
import greta.core.util.CharacterManager;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import greta.tools.editors.MultiTimeLineEditors;
import greta.tools.editors.SpeechUtil;
import greta.tools.editors.TemporizableContainer;
import greta.tools.editors.TimeLine;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 *
 * @author Andre-Marie
 */
public class SpeechSignalTimeLine extends TimeLine<SpeechSignal>{

    private static final int SPEECH_SIGNAL_ID_FONTSIZE = 8;
    private static final int SPEECH_SIGNAL_ID_X_POS_OFFSET = 4;
    private static final int SPEECH_SIGNAL_ID_Y_POS_OFFSET = 12;
    private static final int SPEECH_SIGNAL_WORD_Y_POS_OFFSET = 4;
    private CharacterManager cm;

    public SpeechSignalTimeLine(CharacterManager cm,MultiTimeLineEditors<? extends Temporizable> _bmlEditor) {
        super(_bmlEditor);
        this.cm = cm;
    }

    @Override
    protected  TemporizableContainer<SpeechSignal> instanciateTemporizable(double startTime, double endTime) {
        return new TemporizableContainer<SpeechSignal>(new SpeechSignal(cm,SpeechUtil.instanciateTemporizable(cm,startTime, endTime)), manager.getLabel());
    }

    @Override
    protected String getDescription(TemporizableContainer<SpeechSignal> temporizableContainer, FontMetrics metrics, int limitSize) {
        return SpeechUtil.getDescription(temporizableContainer.getTemporizable(), metrics, limitSize);
    }

    // Override method to display words according to TimeMarkers
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int h = getHeight()-1;
        int y = h/2;
        g.setColor(Color.black);
        g.drawLine(0, 0, 0, h);

        for(TemporizableContainer<SpeechSignal> t : this.getItems()){
            int x = getPosOf(t.getStart().getValue());
            int w = getPosOf(t.getEnd().getValue()) - x;
            String description = getDescription(t,g.getFontMetrics(), w-10);

            int currentFontSize = (int) g.getFont().getSize2D();
            g.setFont(new Font("default", Font.BOLD, SPEECH_SIGNAL_ID_FONTSIZE));
            String id = new String("id");
            id = t.getId();
            g.drawString(id, x + SPEECH_SIGNAL_ID_X_POS_OFFSET, SPEECH_SIGNAL_ID_Y_POS_OFFSET);
            g.setFont(new Font("default", Font.PLAIN, currentFontSize));
            if(description!=null){
                String delims = " ";
                String[] tokens = description.split(delims);
                int i = 0;
                g.setColor(this.getForeground());
                //Setting the font back to normal
                g.setFont(new Font("default", Font.PLAIN, currentFontSize));
                for(TimeMarker tm : t.getTimeMarkers()){
                    if(tm.isConcretized() && i < tokens.length){
                        int tmpos = getPosOf(tm.getValue());
                        g.drawString(tokens[i], tmpos+2, y + SPEECH_SIGNAL_WORD_Y_POS_OFFSET);
                        i++;
                    }
                }
            }
        }
    }

    // Added method to try to display a popup when willing to edit a speech.
    @Override
    protected TemporizableContainer<SpeechSignal> editTemporizable(TemporizableContainer<SpeechSignal> temporizableContainer){
        return new TemporizableContainer<SpeechSignal>(new SpeechSignal(cm,SpeechUtil.editTemporizable(temporizableContainer.getTemporizable())), manager.getLabel());
    }

}
