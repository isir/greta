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
package greta.tools.editors.fml;

import greta.core.intentions.BasicIntention;
import greta.core.intentions.EmotionIntention;
import greta.core.intentions.FMLFileReader;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.intentions.PseudoIntentionSpeech;
import greta.core.intentions.WorldIntention;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.LogPrinter;
import greta.core.util.log.Logs;
import greta.core.util.speech.FakeTTS;
import greta.core.util.time.Temporizer;
import greta.tools.editors.MultiTimeLineEditors;
import greta.tools.editors.TimeLineManager;
import greta.tools.editors.fml.timelines.BasicIntentionTimeLine;
import greta.tools.editors.fml.timelines.EmotionTimeLine;
import greta.tools.editors.fml.timelines.IntentionsAviable;
import greta.tools.editors.fml.timelines.PseudoIntentionSpeechTimeLine;
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
public class FMLEditor extends MultiTimeLineEditors<Intention> implements IntentionPerformer, IntentionEmitter {

    private static final FakeTTS fakeTTS = new FakeTTS();
    private ArrayList<IntentionPerformer> performers;
    private TimeLineManager<PseudoIntentionSpeech> speech;
    private TimeLineManager<EmotionIntention> emotions;
    private TimeLineManager<BasicIntention> performatives;
    private TimeLineManager<BasicIntention> beliefRelations;
    private TimeLineManager<BasicIntention> backchannels;
    private TimeLineManager<BasicIntention> certainties;
    private FMLFileReader fmlFileReader;
    private CharacterManager cm;

    public FMLEditor(CharacterManager cm) {
        this.cm = cm;

        performers = new ArrayList<IntentionPerformer>();
        //speech
        speech = new TimeLineManager<PseudoIntentionSpeech>(new PseudoIntentionSpeechTimeLine(cm));
        speech.setLabel("\nSpeech");

        //emotions
        emotions = new TimeLineManager<EmotionIntention>(new EmotionTimeLine());
        emotions.setColor(new Color(50, 100, 200));
        emotions.setLabel("\nEmotion");

        //performatives
        performatives = new TimeLineManager<BasicIntention>(new BasicIntentionTimeLine("performative", IntentionsAviable.PERFORMATIVES.length==0?"":IntentionsAviable.PERFORMATIVES[0]));
        performatives.setColor(new Color(200, 50, 100));
        performatives.setLabel("\nPerformative");

        //belief-relations
        beliefRelations = new TimeLineManager<BasicIntention>(new BasicIntentionTimeLine("belief-relation",  IntentionsAviable.BELIEF_RELATIONS.length==0?"":IntentionsAviable.BELIEF_RELATIONS[0]));
        beliefRelations.setColor(new Color(220, 220, 10));
        beliefRelations.setLabel("\nBelief-relation");

        //backchannels
        backchannels = new TimeLineManager<BasicIntention>(new BasicIntentionTimeLine("backchannel",  IntentionsAviable.BACKCHANNELS.length==0?"":IntentionsAviable.BACKCHANNELS[0]));
        backchannels.setColor(new Color(255, 150, 150));
        backchannels.setLabel("\nBackchannel");

        //certainties
        certainties = new TimeLineManager<BasicIntention>(new BasicIntentionTimeLine("certainty",  IntentionsAviable.CERTAINTIES.length==0?"":IntentionsAviable.CERTAINTIES[0]));
        certainties.setColor(new Color(100, 255, 150));
        certainties.setLabel("\nCertainty");

        this.updateCheckBox.setEnabled(true);

        addTimeLine(speech);
        addTimeLine(emotions);
        addTimeLine(performatives);
        addTimeLine(beliefRelations);
        addTimeLine(certainties);
        addTimeLine(backchannels);

        fmlFileReader = new FMLFileReader(cm);
        fmlFileReader.addIntentionPerformer(new IntentionPerformer() {

            @Override
            public void performIntentions(List<Intention> list, ID requestId, Mode mode) {
                diplayIntentions(list);
            }
        });

        setFileFilter(new javax.swing.filechooser.FileFilter() {
            java.io.FileFilter filter = fmlFileReader.getFileFilter();
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || filter.accept(f);
            }

            @Override
            public String getDescription() {
                return "FML File";
            }
        });
    }

    @Override
    protected void loadFile(File f) {
        fmlFileReader.load(f.getAbsolutePath());
        this.changeTitle("FML Editor - " + f.getAbsolutePath());
    }

    @Override
    public void performIntentions(List<Intention> list, ID string, Mode mode) {
        if (this.isUpdatedFromEvent()) {
            diplayIntentions(list);
        }
    }

    private void diplayIntentions(List<Intention> intentions) {
        synchronized (fakeTTS) {
            if (cm.getTTS() == null) {
                cm.setTTS(fakeTTS);
            }
            clearTimeLines();
            Temporizer t = new Temporizer();
            t.add(intentions);
            t.temporize();
            double duration = 0;
            for (Intention intention : intentions) {
                duration = Math.max(duration, intention.getEnd().getValue());

                if (intention instanceof PseudoIntentionSpeech) {
                    speech.add((PseudoIntentionSpeech) intention);
                } else if (intention instanceof EmotionIntention) {
                    emotions.add((EmotionIntention) intention);
                } else {
                    if (intention instanceof WorldIntention) {
                    } else {
                        if (intention instanceof BasicIntention) {
                            TimeLineManager<BasicIntention> whereAdd = null;
                            if (intention.getName().equalsIgnoreCase("performative")) {
                                whereAdd = performatives;
                            } else if (intention.getName().equalsIgnoreCase("belief-relation")) {
                                whereAdd = beliefRelations;
                            } else if (intention.getName().equalsIgnoreCase("backchannel")) {
                                whereAdd = backchannels;
                            } else if (intention.getName().equalsIgnoreCase("certainty")) {
                                whereAdd = certainties;
                            }
                            if (whereAdd != null) {
                                whereAdd.add((BasicIntention) intention);
                            }
                        }
                    }
                }
            }
            setTime((int) (duration + 1));
            if (cm.getTTS() == fakeTTS) {
                cm.setTTS(null);
            }
        }
    }

    public static void main(String[] args) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows Classic".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                }
                System.out.println(info.getName());
            }
        } catch (Exception ex) {
        }
        //</editor-fold>
        FMLEditor fe = new FMLEditor(CharacterManager.getStaticInstance());
        fe.addIntentionPerformer(fe);
        fe.setVisible(true);
        Logs.add(new LogPrinter());
    }

    @Override
    protected void performTestButton() {
        List<Intention> intentions = getAllTemporizable();
        for (IntentionPerformer ip : performers) {
            ip.performIntentions(intentions, IDProvider.createID("FML_EDITOR"), new Mode(CompositionType.replace));
        }
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        if (ip != null && ip != this) {
            performers.add(ip);
        }
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer ip) {
        performers.remove(ip);
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
            fileName = "FML-Editor-" + strDate + ".xml";
        }
        else {
            if (! ((fileName.endsWith(".xml")) || (fileName.endsWith(".fml"))) ) {
                fileName = fileName.concat(".xml");
            }
        }

        this.changeTitle("FML Editor - " + fileName);
        FMLTranslator.IntentionsToFML(getAllTemporizable(), new Mode(CompositionType.blend)).save(fileName);
    }

        @Override
    public void setTitle(String title) {
          super.setTitle("FML Editor - Untitled.xml");
    }

    private void changeTitle(String title) {
        super.setTitle(title);
    }

    @Override
    protected void newFile() {
        this.changeTitle("FML Editor - Untitled.xml");
    }
}
