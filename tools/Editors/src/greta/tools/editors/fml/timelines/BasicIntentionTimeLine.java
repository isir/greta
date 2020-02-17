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
package greta.tools.editors.fml.timelines;

import greta.core.intentions.BasicIntention;
import greta.core.util.time.TimeMarker;
import greta.tools.editors.TemporizableContainer;
import greta.tools.editors.TimeLine;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Frame;

/**
 *
 * @author Andre-Marie
 */
public class BasicIntentionTimeLine extends TimeLine<BasicIntention>{

    private String functionName;
    private String defaultType;
    public BasicIntentionTimeLine(String functionName, String defaultType){
        this.functionName = functionName;
        this.defaultType = defaultType;
    }

    @Override
    protected TemporizableContainer<BasicIntention> instanciateTemporizable(double startTime, double endTime) {
        return new TemporizableContainer<BasicIntention>(new BasicIntention(functionName, functionName+"_"+System.currentTimeMillis(), defaultType, new TimeMarker("start",startTime), new TimeMarker("end",endTime), 0.5), manager.getLabel());
    }

    @Override
    protected String getDescription(TemporizableContainer<BasicIntention> temporizableContainer, FontMetrics metrics, int limitSize) {
        return isGoodSize(temporizableContainer.getTemporizable().getType(), metrics, limitSize) ? temporizableContainer.getTemporizable().getType() : null;
    }

    @Override
    protected TemporizableContainer<BasicIntention> editTemporizable(TemporizableContainer<BasicIntention> temporizableContainer){
        Container parent = this.getParent();
        while(parent!=null && !(parent instanceof Frame)) {
            parent = parent.getParent();
        }
        if(parent!=null){
            BasicIntentionEditor dialog =
                    new BasicIntentionEditor(
                            (Frame)parent,
                            true,
                            temporizableContainer,
                            IntentionsAviable.getAviableFor(functionName)
                            );
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
            return dialog.edited;
        }

        return temporizableContainer;
    }
}
