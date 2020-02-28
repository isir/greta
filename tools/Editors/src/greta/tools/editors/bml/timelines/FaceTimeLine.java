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

import greta.core.repositories.AUExpression;
import greta.core.repositories.FaceLibrary;
import greta.core.signals.FaceSignal;
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
public class FaceTimeLine extends TimeLine<FaceSignal>{
    private static int faceSignalCount = 0;

    public FaceTimeLine(MultiTimeLineEditors<? extends Temporizable> _bmlEditor) {
        super(_bmlEditor);
    }

    @Override
    protected TemporizableContainer<FaceSignal> instanciateTemporizable(double startTime, double endTime) {
        String name = Integer.toString(faceSignalCount);
        while (name.length() < NUM_DISPLAYIED_DIGITS_ID) name = "0" + name;
        Signal face = SignalProvider.create("face", "face_" + name);
        faceSignalCount ++;

        if(face instanceof FaceSignal){
            AUExpression auExp = FaceLibrary.global_facelibrary.getAll().get(0);
            ((FaceSignal)face).setReference(auExp.getType()+"="+auExp.getInstanceName());
            face.getStart().setValue(startTime);
            face.getEnd().setValue(endTime);
            return new TemporizableContainer<FaceSignal>((FaceSignal)face, manager.getLabel());
        }
        else
            return null;
    }

    //Changed return value with ID (IGR students patch)
    @Override
    protected String getDescription(TemporizableContainer<FaceSignal> temporizableContainer, FontMetrics metrics, int limitSize) {
        FaceLib faceLib = new FaceLib();
        String id = temporizableContainer.getId();
        try{
            id += ":" + faceLib.getInstanceNameOf(temporizableContainer.getTemporizable());
        }
        catch(Exception e){}

        return isGoodSize(id, metrics, limitSize) ? id : null;
    }

    @Override
    protected TemporizableContainer<FaceSignal> editTemporizable(TemporizableContainer<FaceSignal> temporizableContainer){
        Container parent = this.getParent();
        while(parent!=null && !(parent instanceof Frame)) parent = parent.getParent();
        if(parent!=null){
            ParametricSignalEditor<FaceSignal> dialog =
                    new ParametricSignalEditor<>(
                            (Frame)parent,
                            true,
                            temporizableContainer,
                            new FaceLib(),
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
