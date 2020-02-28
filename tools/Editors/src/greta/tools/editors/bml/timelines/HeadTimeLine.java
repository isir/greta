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

import greta.core.signals.HeadSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalProvider;
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
public class HeadTimeLine extends TimeLine<HeadSignal>{

    private static int headSignalCount = 0;

    public HeadTimeLine(MultiTimeLineEditors<? extends Temporizable> _bmlEditor) {
        super(_bmlEditor);
    }

    @Override
    protected TemporizableContainer<HeadSignal> instanciateTemporizable(double startTime, double endTime) {
        String name = Integer.toString(headSignalCount);
        while (name.length() < NUM_DISPLAYIED_DIGITS_ID) name = "0" + name;
        Signal head = SignalProvider.create("head", "head_" + name);
        headSignalCount ++;

        if(head instanceof HeadSignal){
            head.getStart().setValue(startTime);
            head.getEnd().setValue(endTime);
            return new TemporizableContainer<HeadSignal>((HeadSignal)head, manager.getLabel());
        }
        else
            return null;
    }

    //Changed return value with ID (IGR students patch)
    @Override
    protected String getDescription(TemporizableContainer<HeadSignal> temporizableContainer, FontMetrics metrics, int limitSize) {
        String id = temporizableContainer.getId();
        HeadLib headLib = new HeadLib();
        try{
            id += ":" + headLib.getInstanceNameOf(temporizableContainer.getTemporizable());
        }
        catch(Exception e){}
        return isGoodSize(id, metrics, limitSize) ? id : null;
    }


    @Override
    protected TemporizableContainer<HeadSignal> editTemporizable(TemporizableContainer<HeadSignal> temporizableContainer){
        Container parent = this.getParent();
        while(parent!=null && !(parent instanceof Frame)) {
            parent = parent.getParent();
        }
        if(parent!=null){
//            List<SignalEntry<HeadSignal>> entries = HeadLibrary.getGlobalLibrary().getAll();
//            ArrayList<HeadSignal> heads = new ArrayList<HeadSignal>(entries.size());
//            for(SignalEntry<HeadSignal> entry : entries){
//                heads.add(entry.getSignal());
//            }
            ParametricSignalEditor<HeadSignal> dialog =
                    new ParametricSignalEditor<HeadSignal>(
                            (Frame)parent,
                            true,
                            temporizableContainer,
                            new HeadLib(),
                            false,
                            this.multiTimeLineEditor
                            );
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
            return dialog.edited;
        }

        return temporizableContainer;
    }

}
