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
