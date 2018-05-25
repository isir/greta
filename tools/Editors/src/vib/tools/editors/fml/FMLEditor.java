/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.fml;

import java.awt.Color;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import vib.core.intentions.BasicIntention;
import vib.core.intentions.EmotionIntention;
import vib.core.intentions.FMLFileReader;
import vib.core.intentions.FMLTranslator;
import vib.core.intentions.Intention;
import vib.core.intentions.IntentionEmitter;
import vib.core.intentions.IntentionPerformer;
import vib.core.intentions.PseudoIntentionSpeech;
import vib.core.intentions.WorldIntention;
import vib.core.util.Mode;
import vib.core.util.enums.CompositionType;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.log.LogPrinter;
import vib.core.util.log.Logs;
import vib.core.util.speech.FakeTTS;
import vib.core.util.speech.Speech;
import vib.core.util.time.Temporizer;
import vib.tools.editors.MultiTimeLineEditors;
import vib.tools.editors.TimeLineManager;
import vib.tools.editors.fml.timelines.BasicIntentionTimeLine;
import vib.tools.editors.fml.timelines.EmotionTimeLine;
import vib.tools.editors.fml.timelines.IntentionsAviable;
import vib.tools.editors.fml.timelines.PseudoIntentionSpeechTimeLine;

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

    public FMLEditor() {

        performers = new ArrayList<IntentionPerformer>();
        //speech
        speech = new TimeLineManager<PseudoIntentionSpeech>(new PseudoIntentionSpeechTimeLine());
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

        fmlFileReader = new FMLFileReader();
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
            if (Speech.getTTS() == null) {
                Speech.setTTS(fakeTTS);
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
            if (Speech.getTTS() == fakeTTS) {
                Speech.setTTS(null);
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
        FMLEditor fe = new FMLEditor();
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
    protected void saveAs(String filename) {

        boolean doUniqueFilename = false;

        if (filename == null) {
            doUniqueFilename = true;
        }
        else {
            if (filename.trim().isEmpty() ) {
                doUniqueFilename = true;
            }
        }

        if (doUniqueFilename) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
            Date now = new Date();
            String strDate = sdfDate.format(now);
            filename = "FML-Editor-" + strDate + ".xml";
        }
        else {
            if (! ((filename.endsWith(".xml")) || (filename.endsWith(".fml"))) ) {
                filename = filename.concat(".xml");
            }
        }

        this.changeTitle("FML Editor - " + filename);
        FMLTranslator.IntentionsToFML(getAllTemporizable(), new Mode(CompositionType.blend)).save(filename);
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
