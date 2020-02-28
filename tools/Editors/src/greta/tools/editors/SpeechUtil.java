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
package greta.tools.editors;

import greta.core.util.CharacterManager;
import greta.core.util.speech.Speech;
import greta.core.util.time.TimeMarker;
import java.awt.FontMetrics;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Andre-Marie
 */
public class SpeechUtil{

    private static int speechSignalCount = 0;
    private static int displayIdDigits = 3;

    public static Speech instanciateTemporizable(CharacterManager cm,double startTime, double endTime) {
        //Create Speech element
        Speech s = new Speech(cm);
        s.getStart().setValue(startTime);
        s.getEnd().setValue(endTime);

        //Get text input and parse it into words and TimeMarkers in between
        String text = JOptionPane.showInputDialog("Insert the speech here");
        s = SpeechUtil.addSpeechElementsFromText(s, text);

        //Compute Speech
        s.schedule();

        //Set ID for Speech element with appropriate number of digits
        String name = Integer.toString(speechSignalCount);
        while (name.length() < displayIdDigits) {
            name = "0" + name;
        }
        s.setId("speech_" + name);
        speechSignalCount++;

        return s;
    }


    public static String getDescription(Speech temporizable, FontMetrics metrics, int limitSize) {
        String text = "";
        int i=0;
        List elements = temporizable.getSpeechElements();
        boolean ok = true;
        while(i<elements.size() && ok){
            Object o = elements.get(i);
            if(o instanceof String){
                if(metrics.stringWidth(text+o)<limitSize)
                    text += o + " ";
                else
                    ok = false;
            }
            ++i;
        }
        return text.isEmpty() ? null: text;
    }

    public static Speech addSpeechElementsFromText(Speech s, String text){
        String delims = " ";
        String[] tokens = text.split(delims);
        int i = 0;

        String timeMarkerName = "";
        for (String word : tokens) {
            s.addSpeechElement(word);
            timeMarkerName = "tm_" + Integer.toString(i);
            if (i < tokens.length - 1) {
                s.addSpeechElement(new TimeMarker(timeMarkerName));
            }
            i++;
        }
        return s;
    }
    public static Speech editTemporizable(Speech temporizable) {

        // Return changeable text of the current Speech in popup.
        String old_text = "";
        String old_id = temporizable.getId();
        List elements = temporizable.getSpeechElements();
        int i=0;
        while(i<elements.size()){
            Object o = elements.get(i);
            if(o instanceof String){
                old_text += o + " ";
            }
            ++i;
        }
        //Get Speech text input and parse it into words and TimeMarkers in between
        String text = JOptionPane.showInputDialog("Edit the speech here",old_text);

        Speech newspeech = new Speech(temporizable.getCharacterManager());
        //
        newspeech = SpeechUtil.addSpeechElementsFromText(newspeech, text);
        newspeech.getStart().setValue(temporizable.getStart().getValue());
        temporizable = new Speech(newspeech);
        temporizable.setId(old_id);
        temporizable.schedule();
        return temporizable;
    }
}
