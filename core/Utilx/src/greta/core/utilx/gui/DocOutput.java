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
package greta.core.utilx.gui;

import greta.core.util.log.LogOutput;
import java.awt.Color;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

/**
 *
 * @author Andre-Marie Pez
 */
public class DocOutput extends DefaultStyledDocument implements LogOutput{

    private boolean showtag;
    private boolean blackBack;

    private Style error_style;
    private Style error_tag_style;

    private Style warn_style;
    private Style warn_tag_style;

    private Style debug_style;
    private Style debug_tag_style;

    private Style info_style;

    public DocOutput(){ this(true, true); }
    public DocOutput(boolean showTag, boolean isBlack){
        showtag = showTag;
        info_style = this.addStyle("info", null);
        StyleConstants.setBold(info_style, false);
        StyleConstants.setFontFamily(info_style, "arial");

        error_style = this.addStyle("error", info_style);

        error_tag_style = this.addStyle("error_tag", error_style);
        StyleConstants.setBold(error_tag_style, true);

        warn_style = this.addStyle("warning", info_style);

        warn_tag_style = this.addStyle("warning_tag", warn_style);
        StyleConstants.setBold(warn_tag_style, true);

        debug_style = this.addStyle("debug", info_style);

        debug_tag_style = this.addStyle("debug_tag", debug_style);
        StyleConstants.setBold(debug_tag_style, true);

        blackBack = !isBlack;
        setBlackBackground(isBlack);
    }
    public boolean hasBlackBackground(){
        return blackBack;
    }
    public final void setBlackBackground(boolean isBlack){
        if(blackBack==isBlack) return;

        blackBack = isBlack;

        StyleConstants.setForeground(error_style, Color.red);

        StyleConstants.setForeground(warn_style, isBlack ? Color.orange : new Color(210,153,0));

        StyleConstants.setForeground(debug_style, isBlack ? Color.gray : Color.gray);

        StyleConstants.setForeground(info_style, isBlack ? Color.white : Color.black);
        this.styleChanged(info_style);
    }

    @Override
    public synchronized void onDebug(String string) {
        if(showtag)writeLogMessage("DEBUG : ", debug_style, debug_tag_style);
        writeLogMessage(string+"\n",debug_style,null);
    }

    @Override
    public synchronized void onInfo(String string) {
        writeLogMessage(string+"\n",info_style,null);
    }

    @Override
    public synchronized void onWarning(String string) {
        if(showtag)writeLogMessage("WARNING : ",warn_style,warn_tag_style);
        writeLogMessage(string+"\n",warn_style,null);
    }

    @Override
    public synchronized void onError(String string) {
        if(showtag)writeLogMessage("ERROR : ",error_style,error_tag_style);
        writeLogMessage(string+"\n",error_style,null);
    }


    private void writeLogMessage(String message, Style stylemain, Style stylelocal){
        try {
            this.setLogicalStyle(this.getLength(), stylemain);
            this.insertString(this.getLength(), message, stylelocal);
        } catch (BadLocationException ex) {}
    }

}
