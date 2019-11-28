/*
 * This file is part of Greta.
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
package vib.tools.editors.bml.timelines;

import vib.tools.editors.TimeLine;
import vib.core.signals.GazeSignal;
import vib.core.signals.Signal;
import vib.core.signals.SignalProvider;
import java.awt.FontMetrics;
import vib.core.util.time.Temporizable;
import vib.tools.editors.MultiTimeLineEditors;
import vib.tools.editors.TemporizableContainer;

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
