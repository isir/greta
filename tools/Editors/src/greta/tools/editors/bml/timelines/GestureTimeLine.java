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

import greta.core.signals.Signal;
import greta.core.signals.SignalProvider;
import greta.core.signals.gesture.GestureSignal;
import greta.core.util.time.Temporizable;
import greta.tools.editors.MultiTimeLineEditors;
import greta.tools.editors.TemporizableContainer;
import greta.tools.editors.TimeLine;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Frame;

/**
 *
 * @author Andre-Marie
 */
public class GestureTimeLine extends TimeLine<GestureSignal>{

    private static int gestureSignalCount = 0;

    public GestureTimeLine(MultiTimeLineEditors<? extends Temporizable> _bmlEditor) {
        super(_bmlEditor);
    }

    @Override
    protected TemporizableContainer<GestureSignal> instanciateTemporizable(double startTime, double endTime) {
        String name = Integer.toString(gestureSignalCount);
        while (name.length() < NUM_DISPLAYIED_DIGITS_ID) name = "0" + name;
        Signal gesture = SignalProvider.create("gesture", "gesture_" + name);
        gestureSignalCount ++;

        if(gesture instanceof GestureSignal){
            ((GestureSignal)gesture).setCategory("EMPTY");
            ((GestureSignal)gesture).setReference("EMPTY=empty");
            gesture.getStart().setValue(startTime);
            gesture.getEnd().setValue(endTime);
            return new TemporizableContainer<GestureSignal>((GestureSignal)gesture, manager.getLabel());
        }
        else
            return null;
    }

    //Changed return value with ID (IGR students patch)
    @Override
    protected String getDescription(TemporizableContainer<GestureSignal> temporizableContainer, FontMetrics metrics, int limitSize) {
        GestureLib gestLib = new GestureLib();
        String id = temporizableContainer.getId();
        try{
            id += ":" + gestLib.getInstanceNameOf(temporizableContainer.getTemporizable());
        }
        catch(Exception e){}

        return isGoodSize(id, metrics, limitSize) ? id : null;
    }

    @Override
    protected TemporizableContainer<GestureSignal> editTemporizable(TemporizableContainer<GestureSignal> temporizableContainer){
        Container parent = this.getParent();
        while(parent!=null && !(parent instanceof Frame)) parent = parent.getParent();
        if(parent!=null){
            ParametricSignalEditor<GestureSignal> dialog =
                    new ParametricSignalEditor<GestureSignal>(
                            (Frame)parent,
                            true,
                            temporizableContainer,
                            new GestureLib(),
                            true,
                            this.multiTimeLineEditor
                            );
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
            return dialog.edited;
        }

        return temporizableContainer;
    }
}
