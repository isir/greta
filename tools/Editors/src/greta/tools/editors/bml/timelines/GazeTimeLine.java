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
package greta.tools.editors.bml.timelines;

import greta.core.signals.GazeSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalProvider;
import greta.core.util.time.Temporizable;
import greta.tools.editors.MultiTimeLineEditors;
import greta.tools.editors.TemporizableContainer;
import greta.tools.editors.TimeLine;
import java.awt.FontMetrics;

/**
 *
 * @author Andre-Marie
 */
public class GazeTimeLine extends TimeLine<GazeSignal>{
    private static int gazeSignalCount = 0;

    public GazeTimeLine(MultiTimeLineEditors<? extends Temporizable> _bmlEditor) {
        super(_bmlEditor);
    }
    @Override
    protected TemporizableContainer<GazeSignal> instanciateTemporizable(double startTime, double endTime) {
        String name = Integer.toString(gazeSignalCount);
        while (name.length() < NUM_DISPLAYIED_DIGITS_ID) name = "0" + name;
        Signal gaze = SignalProvider.create("gaze", "gaze_" + name);
        gazeSignalCount ++;
        if(gaze instanceof GazeSignal){
            gaze.getStart().setValue(startTime);
            gaze.getEnd().setValue(endTime);
            return new TemporizableContainer<GazeSignal>((GazeSignal)gaze, manager.getLabel());
        }
            return null;
    }

        //Changed return value with ID (IGR students patch)
    @Override
    protected String getDescription(TemporizableContainer<GazeSignal> temporizableContainer, FontMetrics metrics, int limitSize) {
        String id = temporizableContainer.getId();
        /*GazeLib gazeLib = new GazeLib();
        try{
            id += ":" + gestLib.getInstanceNameOf(temporizable);
        }
        catch(Exception e){}
        */
        return isGoodSize(id, metrics, limitSize) ? id : null;
    }

}
