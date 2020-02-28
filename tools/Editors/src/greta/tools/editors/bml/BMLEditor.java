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
package greta.tools.editors.bml;

import greta.core.repositories.SignalFiller;
import greta.core.signals.BMLFileReader;
import greta.core.signals.BMLTranslator;
import greta.core.signals.FaceSignal;
import greta.core.signals.GazeSignal;
import greta.core.signals.HeadSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.signals.TorsoSignal;
import greta.core.signals.gesture.GestureSignal;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.speech.FakeTTS;
import greta.core.util.time.Temporizer;
import greta.tools.editors.MultiTimeLineEditors;
import greta.tools.editors.TimeLineManager;
import greta.tools.editors.bml.timelines.FaceTimeLine;
import greta.tools.editors.bml.timelines.GazeTimeLine;
import greta.tools.editors.bml.timelines.GestureTimeLine;
import greta.tools.editors.bml.timelines.HeadTimeLine;
import greta.tools.editors.bml.timelines.SpeechSignalTimeLine;
import greta.tools.editors.bml.timelines.TorsoTimeLine;
import java.awt.Color;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Andre-Marie
 */
public class BMLEditor extends MultiTimeLineEditors<Signal> implements SignalPerformer, SignalEmitter {

    private static final FakeTTS fakeTTS = new FakeTTS();
    private ArrayList<SignalPerformer> performers;
    private TimeLineManager<SpeechSignal> speech;
    private TimeLineManager<HeadSignal> heads;
    private TimeLineManager<FaceSignal> faces;
    private TimeLineManager<GazeSignal> gazes;
    private TimeLineManager<GestureSignal> gestures;
    private TimeLineManager<TorsoSignal> torsoes;
    private BMLFileReader bmlFileReader;
    private CharacterManager cm;

    public List<SpeechSignal> getSpeechList(){
        return this.speech.getTimeLine().getItems();
    }

    public List<HeadSignal> getHeadsList(){
        return this.heads.getTimeLine().getItems();
    }

    public List<FaceSignal> getFacesList(){
        return this.faces.getTimeLine().getItems();
    }

    public List<GazeSignal> getGazesList(){
        return this.gazes.getTimeLine().getItems();
    }

    public List<GestureSignal> getGesturesList(){
        return this.gestures.getTimeLine().getItems();
    }

    public List<TorsoSignal> getTorsoesList(){
        return this.torsoes.getTimeLine().getItems();
    }


    public ArrayList<String> getSpeechIDs(){
        ArrayList<String> output = new ArrayList<String>();
        List<SpeechSignal> liste = this.speech.getTimeLine().getItems();
        for(SpeechSignal tmp : liste){
            output.add(tmp.getId());
        }
        return output;
    }

    public ArrayList<String> getHeadIDs(){
        ArrayList<String> output = new ArrayList<String>();
        List<HeadSignal> liste = this.heads.getTimeLine().getItems();
        for(HeadSignal tmp : liste){
            output.add(tmp.getId());
        }
        return output;
    }

    public ArrayList<String> getGazeIDs(){
        ArrayList<String> output = new ArrayList<String>();
        List<GazeSignal> liste = this.gazes.getTimeLine().getItems();
        for(GazeSignal tmp : liste){
            output.add(tmp.getId());
        }
        return output;
    }

    public ArrayList<String> getFaceIDs(){
        ArrayList<String> output = new ArrayList<String>();
        List<FaceSignal> liste = this.faces.getTimeLine().getItems();
        for(FaceSignal tmp : liste){
            output.add(tmp.getId());
        }
        return output;
    }

    public ArrayList<String> getGestureIDs(){
        ArrayList<String> output = new ArrayList<String>();
        List<GestureSignal> liste = this.gestures.getTimeLine().getItems();
        for(GestureSignal tmp : liste){
            output.add(tmp.getId());
        }
        return output;
    }

    public ArrayList<String> getTorsoIDs(){
        ArrayList<String> output = new ArrayList<String>();
        List<TorsoSignal> liste = this.torsoes.getTimeLine().getItems();
        for(TorsoSignal tmp : liste){
            output.add(tmp.getId());
        }
        return output;
    }

    public BMLEditor(CharacterManager cm) {
        this.cm = cm;
        performers = new ArrayList<SignalPerformer>();
        //speech
        speech = new TimeLineManager<SpeechSignal>(new SpeechSignalTimeLine(cm,this));
        speech.setLabel("Speech");

        //gestures
        gestures = new TimeLineManager<GestureSignal>(new GestureTimeLine(this));
        gestures.setColor(new Color(120, 200, 5));
        gestures.setLabel("Gesture");


        //faces
        faces = new TimeLineManager<FaceSignal>(new FaceTimeLine(this));
        faces.setColor(new Color(210, 70, 70));
        faces.setLabel("Face");

        //gazes
        gazes = new TimeLineManager<GazeSignal>(new GazeTimeLine(this));
        gazes.setColor(new Color(80, 130, 255));
        gazes.setLabel("Gazes");

        //heads
        heads = new TimeLineManager<HeadSignal>(new HeadTimeLine(this));
        heads.setColor(new Color(180, 20, 240));
        heads.setLabel("Heads");

        //torsoes
        torsoes = new TimeLineManager<TorsoSignal>(new TorsoTimeLine(this));
        torsoes.setColor(new Color(0, 210, 210));
        torsoes.setLabel("Torso");

        this.updateCheckBox.setEnabled(true);

        addTimeLine(speech);
        addTimeLine(heads);
        addTimeLine(gazes);
        addTimeLine(faces);
        addTimeLine(gestures);
        addTimeLine(torsoes);

        bmlFileReader = new BMLFileReader(cm);
        bmlFileReader.addSignalPerformer(new SignalPerformer() {

            @Override
            public void performSignals(List<Signal> list, ID requestId, Mode mode) {
                displaySignals(list);
            }
        });

        setFileFilter(new javax.swing.filechooser.FileFilter() {
            java.io.FileFilter filter = bmlFileReader.getFileFilter();
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || filter.accept(f);
            }

            @Override
            public String getDescription() {
                return "BML File";
            }
        });
    }

    @Override
    public void setTitle(String title) {
          super.setTitle("BML Editor - Untitled.xml");
    }

    private void changeTitle(String title) {
        super.setTitle(title);
    }

    private void displaySignals(List<Signal> signals) {
        synchronized (fakeTTS) {
            if (cm.getTTS() == null) {
                cm.setTTS(fakeTTS);
            }

            clearTimeLines();
            for (Signal signal : signals) {
                try {
                    SignalFiller.fillSignal(signal);
                } catch (Exception e) {
                }
            }
            Temporizer t = new Temporizer();
            t.add(signals);
            t.temporize();

            double duration = 0;
            for (Signal signal : signals) {
                duration = Math.max(duration, signal.getEnd().getValue());

                if (signal instanceof SpeechSignal) {
                    speech.add((SpeechSignal) signal);
                } else if (signal instanceof FaceSignal) {
                    faces.add((FaceSignal) signal);
                } else if (signal instanceof GazeSignal) {
                    gazes.add((GazeSignal) signal);
                } else if (signal instanceof HeadSignal) {
                    heads.add((HeadSignal) signal);
                } else if (signal instanceof GestureSignal) {
                    gestures.add((GestureSignal) signal);
                } else if (signal instanceof TorsoSignal) {
                    torsoes.add((TorsoSignal) signal);
                }
            }

            setTime((int) (duration + 1));

            if (cm.getTTS() == fakeTTS) {
                cm.setTTS(null);
            }
        }
    }

    @Override
    protected void loadFile(File f) {
        bmlFileReader.load(f.getAbsolutePath());
        this.changeTitle("BML Editor - " + f.getAbsolutePath());
    }

    @Override
    protected void performTestButton() {
        List<Signal> signals = getAllTemporizable();
        ID id = IDProvider.createID("BML_EDITOR");
        for (SignalPerformer sp : performers) {
            sp.performSignals(signals, id, new Mode(CompositionType.replace));
        }
    }

    @Override
    public void performSignals(List<Signal> list, ID requestId, Mode mode) {
        if (this.isUpdatedFromEvent()) {
            displaySignals(list);
        }
    }

    @Override
    public void addSignalPerformer(SignalPerformer sp) {
        if (sp != null && sp != this) {
            performers.add(sp);
        }
    }

    @Override
    public void removeSignalPerformer(SignalPerformer sp) {
        performers.remove(sp);
    }

    @Override
    protected void saveAs(String fileName) {

        boolean doUniqueFilename = false;

        if (fileName == null) {
            doUniqueFilename = true;
        }
        else {
            if (fileName.trim().isEmpty() ) {
                doUniqueFilename = true;
            }
        }

        if (doUniqueFilename) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
            Date now = new Date();
            String strDate = sdfDate.format(now);
            fileName = "BML-Editor-" + strDate + ".xml";
        }
        else {
            if (! ((fileName.endsWith(".xml")) || (fileName.endsWith(".bml"))) ) {
                fileName = fileName.concat(".xml");
            }
        }

        this.changeTitle("BML Editor - " + fileName);

        BMLTranslator.SignalsToBML(getAllTemporizable(), new Mode(CompositionType.blend)).save(fileName);
    }

    @Override
    protected void newFile() {
        this.changeTitle("BML Editor - Untitled.xml");
    }
}
