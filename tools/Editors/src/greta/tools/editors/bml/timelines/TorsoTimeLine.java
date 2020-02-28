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
import greta.core.signals.TorsoSignal;
import greta.core.util.time.Temporizable;
import greta.tools.editors.MultiTimeLineEditors;
import greta.tools.editors.TemporizableContainer;
import greta.tools.editors.TimeLine;
import java.awt.FontMetrics;

/**
 *
 * @author Andre-Marie
 */
public class TorsoTimeLine extends TimeLine<TorsoSignal>{

    private static int torsoSignalCount = 0;

    public TorsoTimeLine(MultiTimeLineEditors<? extends Temporizable> _bmlEditor) {
        super(_bmlEditor);
    }

    @Override
    protected TemporizableContainer<TorsoSignal> instanciateTemporizable(double startTime, double endTime) {
        String name = Integer.toString(torsoSignalCount);
        while (name.length() < NUM_DISPLAYIED_DIGITS_ID) name = "0" + name;
        Signal torso = SignalProvider.create("torso", "torso_" + name);
        torsoSignalCount ++;

        if(torso instanceof TorsoSignal){
            torso.getStart().setValue(startTime);
            torso.getEnd().setValue(endTime);
            return new TemporizableContainer<TorsoSignal>((TorsoSignal)torso, manager.getLabel());
        }
        else
            return null;
    }

    //Changed return value with ID (IGR students patch)
    @Override
    protected String getDescription(TemporizableContainer<TorsoSignal> temporizableContainer, FontMetrics metrics, int limitSize) {
        String id = temporizableContainer.getId();
        /*TorsoLib torsoLib = new TorsoLib();
        try{
            id += ":" + torsoLib.getInstanceNameOf(temporizable);
        }
        catch(Exception e){}
        */
        return isGoodSize(id, metrics, limitSize) ? id : null;
    }

}
