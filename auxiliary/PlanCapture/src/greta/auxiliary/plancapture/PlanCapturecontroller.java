/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.plancapture;

import greta.core.behaviorplanner.MultimodalSignalSelector;
import greta.core.behaviorplanner.SignalSelector;
import greta.core.behaviorplanner.lexicon.BehaviorSet;
import greta.core.behaviorplanner.lexicon.Lexicon;
import greta.core.behaviorplanner.lexicon.Shape;
import greta.core.behaviorplanner.lexicon.SignalItem;
import greta.core.feedbacks.Callback;
import greta.core.feedbacks.CallbackPerformer;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.intentions.PseudoIntentionSpeech;
import greta.core.signals.FaceSignal;
import greta.core.signals.HeadSignal;
import greta.core.signals.ParametricSignal;
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
import greta.core.util.speech.Speech;
import greta.core.util.time.Temporizer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Brian Ravenet
 */
public class PlanCapturecontroller extends greta.auxiliary.player.ogre.capture.Capturecontroller implements SignalEmitter, IntentionPerformer, CallbackPerformer {

    private SignalSelector signalSelector;
    private List<SignalPerformer> signalPerformers;
    private List<IntentionEmitter> intentionEmitter;
    private List<List<Signal>> combiList;
    private SpeechSignal speech = null;
    private int currentCombi = 0;

    private Lexicon lexicon;
    private volatile boolean iscapturing = false;
    private boolean mustcapture = false;
    private CharacterManager cm;

    private String constructVideoName(List<Signal> signals) {
        String name = "M";
        for (Signal s : signals) {
            if (s instanceof FaceSignal) {
                ParametricSignal ps = (ParametricSignal) s;
                if (ps.getReference().toLowerCase().contains("anger")) {
                    name += ";Disliking";
                } else if (ps.getReference().toLowerCase().contains("au59")) {
                    name += ";Liking";
                }
            }
        }
        for (Signal s : signals) {
            if (s instanceof GestureSignal) {
                ParametricSignal ps = (ParametricSignal) s;

                if (ps.getSPC() == 0.5) {
                    name += ";SPCNormal";
                }
                if (ps.getSPC() > 0.5) {
                    name += ";SPCWide";
                }
                if (ps.getSPC() < 0.5) {
                    name += ";SPCSmall";
                }
                if (ps.getPWR() == 0.5) {
                    name += ";PWRNormal";
                }
                if (ps.getPWR() > 0.5) {
                    name += ";PWRBig";
                }
                if (ps.getPWR() < 0.5) {
                    name += ";PWRSmall";
                }
            }
        }
        for (Signal s : signals) {
            if (s instanceof HeadSignal) {
                ParametricSignal ps = (ParametricSignal) s;
                if (ps.getReference().toLowerCase().contains("shake")) {
                    name += ";Shake";
                } else if (ps.getReference().toLowerCase().contains("nod")) {
                    name += ";Nod";
                }else if (ps.getReference().toLowerCase().contains("up")) {
                    name += ";Up";
                }else if (ps.getReference().toLowerCase().contains("down")) {
                    name += ";Down";
                }else if (ps.getReference().toLowerCase().contains("aside")) {
                    name += ";Aside";
                }


            }
        }
        for (Signal s : signals) {
            if (s instanceof FaceSignal) {
                ParametricSignal ps = (ParametricSignal) s;
                if (ps.getReference().toLowerCase().contains("look_up")) {
                    name += ";AvoidUp";
                }
                if (ps.getReference().toLowerCase().contains("look_down")) {
                    name += ";AvoidDown";
                }
                if (ps.getReference().toLowerCase().contains("look_right")) {
                    name += ";AvoidRight";
                }
            }
        }
        return name;
    }
    @Override
    public void screenShot() {
        mustcapture = true;
        while (currentCombi < combiList.size()) {


            //GENERATE NAME
            List<Signal> comb = combiList.get(currentCombi);
            String videoName = constructVideoName(comb);
            File f = new File(videoName + ".flv");
            if (!f.exists()) {
                //SPEECH
                if (speech != null) {
                    comb.add(speech);
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PlanCapturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                }
                setBaseFileName(videoName);
                startVideoCapture();
                iscapturing = true;
                ID id = IDProvider.createID("PlanCaptureController");
                for (SignalPerformer sp : signalPerformers) {
                    sp.performSignals(comb, id, new Mode(CompositionType.replace));
                }
                while (iscapturing) {


                }
            }


            currentCombi++;
        }
    }


    /**
     * Creates new form Capturecontroller
     */
    public PlanCapturecontroller() {
        super();
        signalPerformers = new ArrayList<SignalPerformer>();
        lexicon = new Lexicon(cm);
        this.cm.add(lexicon);

        signalSelector = new MultimodalSignalSelector();

    }


    @Override
    public void addSignalPerformer(SignalPerformer sp) {
        if (sp != null) {
            signalPerformers.add(sp);
        }
    }

    @Override
    public void removeSignalPerformer(SignalPerformer sp) {
        signalPerformers.remove(sp);
    }

    @Override
    public void performIntentions(List<Intention> list, ID requestId, Mode mode) {


        //temporize every intentions
        Temporizer temporizer = new Temporizer();
        temporizer.add(list);
        temporizer.temporize();


        combiList = new ArrayList<List<Signal>>();
        //for each intention :
        for (Intention intention : list) {

            //character playing
            if (intention.hasCharacter()) {

                lexicon.setDefinition(Lexicon.CHARACTER_PARAMETER_INTENTION_LEXICON);// setDefinition(CharacterManager.getValueString(Lexicon.CHARACTER_PARAMETER_INTENTION_LEXICON, intention.getCharacter()));

            }
            if (intention instanceof PseudoIntentionSpeech) {
                speech = new SpeechSignal(cm,(Speech) intention);
            }
            //choose a signal selector
            SignalSelector selector = signalSelector;


            BehaviorSet set = lexicon.fromIntentionToBehaviorSet(intention, selector.getType());
            List<? extends List<SignalItem>> combis = set.getCombinations();

            //FOR EACH (LIST OF SIGNAL)/(COMBINATIONS)
            //GENERATE ALL SHAPE/PARAMETRIC SIGNAL COMBINATIONS
            for (List<SignalItem> combi : combis) {
                combiList.addAll(recursiveCombinate(combi));
            }


            //reset character playing
            if (intention.hasCharacter()) {

                lexicon.setDefinition(this.cm.getValueString(Lexicon.CHARACTER_PARAMETER_INTENTION_LEXICON));
            }
        }

    }

    @Override
    public void performCallback(Callback clbck) {
        if (mustcapture) {
            if ((clbck.type().equalsIgnoreCase("dead") || clbck.type().equalsIgnoreCase("end"))) {
                stopVideoCapture();

                iscapturing = false;

            }
        }
    }

    private List<List<Signal>> recursiveCombinate(List<SignalItem> listSignal) {
        List<List<Signal>> returnList = new ArrayList<List<Signal>>();
        if (listSignal.isEmpty()) {
            return returnList;
        } else if (listSignal.size() == 1) {
            SignalItem main = listSignal.get(0);
            for (Signal s : signalItemToSignal(main)) {
                ArrayList<Signal> temp = new ArrayList<Signal>();
                temp.add(s);
                returnList.add(temp);
            }
            return returnList;
        } else {
            SignalItem main = listSignal.get(0);
            List<SignalItem> others = new ArrayList<SignalItem>(listSignal);
            others.remove(main);
            List<List<Signal>> recursiveList = recursiveCombinate(others);
            for (Signal s : signalItemToSignal(main)) {

                for (List<Signal> ls : recursiveList) {
                    List<Signal> signalList = new ArrayList<Signal>(ls);
                    signalList.add(s);
                    returnList.add(signalList);
                }
            }
            return returnList;
        }
    }

    private List<Signal> signalItemToSignal(SignalItem si) {
        ArrayList<Signal> al = new ArrayList<Signal>();
        ParametricSignal ps = null;

        if (si.getModality().equalsIgnoreCase("gesture")) {
            ps = new GestureSignal(si.getMainShape().getName());

            ParametricSignal smallsmallGS = new GestureSignal(si.getMainShape().getName());
            smallsmallGS.setReference(si.getMainShape().getName());
            smallsmallGS.getStart().setValue(0);
            smallsmallGS.setFLD(0.5);
            smallsmallGS.setOpenness(0);
            smallsmallGS.setPWR(0);
            smallsmallGS.setTMP(0);
            smallsmallGS.setSPC(0);
            smallsmallGS.setTension(0.5);
            al.add(smallsmallGS);
            ParametricSignal smallmediumGS = new GestureSignal(si.getMainShape().getName());
            smallmediumGS.setReference(si.getMainShape().getName());
            smallmediumGS.getStart().setValue(0);
            smallmediumGS.setFLD(0.5);
            smallmediumGS.setOpenness(0);
            smallmediumGS.setPWR(0.5);
            smallmediumGS.setTMP(0.5);
            smallmediumGS.setSPC(0);
            smallmediumGS.setTension(0.5);
            al.add(smallmediumGS);
            ParametricSignal smalllargeGS = new GestureSignal(si.getMainShape().getName());
            smalllargeGS.setReference(si.getMainShape().getName());
            smalllargeGS.getStart().setValue(0);
            smalllargeGS.setFLD(0.5);
            smalllargeGS.setOpenness(0);
            smalllargeGS.setPWR(1);
            smalllargeGS.setTMP(1);
            smalllargeGS.setSPC(0);
            smalllargeGS.setTension(0.5);
            al.add(smalllargeGS);

            ParametricSignal mediumsmallGS = new GestureSignal(si.getMainShape().getName());
            mediumsmallGS.setReference(si.getMainShape().getName());
            mediumsmallGS.getStart().setValue(0);
            mediumsmallGS.setFLD(0.5);
            mediumsmallGS.setOpenness(0.2);
            mediumsmallGS.setPWR(0);
            mediumsmallGS.setTMP(0);
            mediumsmallGS.setSPC(0.5);
            mediumsmallGS.setTension(0.5);
            al.add(mediumsmallGS);

            ParametricSignal mediumlargeGS = new GestureSignal(si.getMainShape().getName());
            mediumlargeGS.setReference(si.getMainShape().getName());
            mediumlargeGS.getStart().setValue(0);
            mediumlargeGS.setFLD(0.5);
            mediumlargeGS.setOpenness(0.2);
            mediumlargeGS.setPWR(1);
            mediumlargeGS.setTMP(1);
            mediumlargeGS.setSPC(0.5);
            mediumlargeGS.setTension(0.5);
            al.add(mediumlargeGS);

            ParametricSignal largesmallGS = new GestureSignal(si.getMainShape().getName());
            largesmallGS.setReference(si.getMainShape().getName());
            largesmallGS.getStart().setValue(0);
            largesmallGS.setFLD(0.5);
            largesmallGS.setOpenness(0.3);
            largesmallGS.setPWR(0);
            largesmallGS.setTMP(0);
            largesmallGS.setSPC(0.8);
            largesmallGS.setTension(0.5);
            al.add(largesmallGS);
            ParametricSignal largemediumGS = new GestureSignal(si.getMainShape().getName());
            largemediumGS.setReference(si.getMainShape().getName());
            largemediumGS.getStart().setValue(0);
            largemediumGS.setFLD(0.5);
            largemediumGS.setOpenness(0.3);
            largemediumGS.setPWR(0.5);
            largemediumGS.setTMP(0.5);
            largemediumGS.setSPC(0.8);
            largemediumGS.setTension(0.5);
            al.add(largemediumGS);
            ParametricSignal largelargeGS = new GestureSignal(si.getMainShape().getName());
            largelargeGS.setReference(si.getMainShape().getName());
            largelargeGS.getStart().setValue(0);
            largelargeGS.setFLD(0.5);
            largelargeGS.setOpenness(0.3);
            largelargeGS.setPWR(1);
            largelargeGS.setTMP(1);
            largelargeGS.setSPC(0.8);
            largelargeGS.setTension(0.5);
            al.add(largelargeGS);

        } else if (si.getModality().equalsIgnoreCase("head")) {
            ps = new HeadSignal(si.getMainShape().getName());
            ps.getEnd().setValue(3);
        } else if (si.getModality().equalsIgnoreCase("face") || si.getModality().equalsIgnoreCase("gaze")) {
            ps = new FaceSignal(si.getMainShape().getName());
            ps.getEnd().setValue(3);
        } else if (si.getModality().equalsIgnoreCase("torso")) {
            ps = new TorsoSignal(si.getMainShape().getName());
            ps.getEnd().setValue(3);
        }
        ps.setReference(si.getMainShape().getName());
        ps.getStart().setValue(0);

        ps.setFLD(0.5);
        ps.setOpenness(0.2);
        ps.setPWR(0.5);
        ps.setTMP(0.5);
        ps.setSPC(0.5);
        ps.setTension(0.5);
        al.add(ps);
        for (Shape shp : si.getAlternativeShapes()) {
            ParametricSignal aps = null;
            if (si.getModality().equalsIgnoreCase("gesture")) {
                aps = new GestureSignal(shp.getName());
                ParametricSignal smallsmallGS = new GestureSignal(shp.getName());
                smallsmallGS.setReference(shp.getName());
                smallsmallGS.getStart().setValue(0);
                smallsmallGS.setFLD(0.5);
                smallsmallGS.setOpenness(0);
                smallsmallGS.setPWR(0);
                smallsmallGS.setTMP(0);
                smallsmallGS.setSPC(0);
                smallsmallGS.setTension(0.5);
                al.add(smallsmallGS);
                ParametricSignal smallmediumGS = new GestureSignal(shp.getName());
                smallmediumGS.setReference(shp.getName());
                smallmediumGS.getStart().setValue(0);
                smallmediumGS.setFLD(0.5);
                smallmediumGS.setOpenness(0);
                smallmediumGS.setPWR(0.5);
                smallmediumGS.setTMP(0.5);
                smallmediumGS.setSPC(0);
                smallmediumGS.setTension(0.5);
                al.add(smallmediumGS);
                ParametricSignal smalllargeGS = new GestureSignal(shp.getName());
                smalllargeGS.setReference(shp.getName());
                smalllargeGS.getStart().setValue(0);
                smalllargeGS.setFLD(0.5);
                smalllargeGS.setOpenness(0);
                smalllargeGS.setPWR(1);
                smalllargeGS.setTMP(1);
                smalllargeGS.setSPC(0);
                smalllargeGS.setTension(0.5);
                al.add(smalllargeGS);

                ParametricSignal mediumsmallGS = new GestureSignal(shp.getName());
                mediumsmallGS.setReference(shp.getName());
                mediumsmallGS.getStart().setValue(0);
                mediumsmallGS.setFLD(0.5);
                mediumsmallGS.setOpenness(0.2);
                mediumsmallGS.setPWR(0);
                mediumsmallGS.setTMP(0);
                mediumsmallGS.setSPC(0.5);
                mediumsmallGS.setTension(0.5);
                al.add(mediumsmallGS);

                ParametricSignal mediumlargeGS = new GestureSignal(shp.getName());
                mediumlargeGS.setReference(shp.getName());
                mediumlargeGS.getStart().setValue(0);
                mediumlargeGS.setFLD(0.5);
                mediumlargeGS.setOpenness(0.2);
                mediumlargeGS.setPWR(1);
                mediumlargeGS.setTMP(1);
                mediumlargeGS.setSPC(0.5);
                mediumlargeGS.setTension(0.5);
                al.add(mediumlargeGS);

                ParametricSignal largesmallGS = new GestureSignal(shp.getName());
                largesmallGS.setReference(shp.getName());
                largesmallGS.getStart().setValue(0);
                largesmallGS.setFLD(0.5);
                largesmallGS.setOpenness(0.3);
                largesmallGS.setPWR(0);
                largesmallGS.setTMP(0);
                largesmallGS.setSPC(0.8);
                largesmallGS.setTension(0.5);
                al.add(largesmallGS);
                ParametricSignal largemediumGS = new GestureSignal(shp.getName());
                largemediumGS.setReference(shp.getName());
                largemediumGS.getStart().setValue(0);
                largemediumGS.setFLD(0.5);
                largemediumGS.setOpenness(0.3);
                largemediumGS.setPWR(0.5);
                largemediumGS.setTMP(0.5);
                largemediumGS.setSPC(0.8);
                largemediumGS.setTension(0.5);
                al.add(largemediumGS);
                ParametricSignal largelargeGS = new GestureSignal(shp.getName());
                largelargeGS.setReference(shp.getName());
                largelargeGS.getStart().setValue(0);
                largelargeGS.setFLD(0.5);
                largelargeGS.setOpenness(0.3);
                largelargeGS.setPWR(1);
                largelargeGS.setTMP(1);
                largelargeGS.setSPC(0.8);
                largelargeGS.setTension(0.5);
                al.add(largelargeGS);
            } else if (si.getModality().equalsIgnoreCase("head")) {
                aps = new HeadSignal(shp.getName());
                aps.getEnd().setValue(3);
            } else if (si.getModality().equalsIgnoreCase("face") || si.getModality().equalsIgnoreCase("gaze")) {
                aps = new FaceSignal(shp.getName());
                aps.getEnd().setValue(3);
            } else if (si.getModality().equalsIgnoreCase("torso")) {
                aps = new TorsoSignal(shp.getName());
                aps.getEnd().setValue(3);
            }

            aps.setReference(shp.getName());
            aps.getStart().setValue(0);
            aps.setFLD(0.5);
            aps.setOpenness(0.2);
            aps.setPWR(0.5);
            aps.setTMP(0.5);
            aps.setSPC(0.5);
            aps.setTension(0.5);

            al.add(aps);
        }
        return al;
    }
}
