/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.multilayerattitudeplanner;

import java.util.ArrayList;
import java.util.List;
import greta.auxiliary.multilayerattitudeplanner.structures.NVBEventType;
import greta.core.repositories.AUItem;
import greta.core.repositories.SignalFiller;
import greta.core.signals.FaceSignal;
import greta.core.signals.GazeSignal;
import greta.core.signals.HeadSignal;
import greta.core.signals.Signal;
import greta.core.signals.TorsoSignal;
import greta.core.signals.gesture.GestureSignal;
import greta.core.util.enums.GazeDirection;
import greta.core.util.enums.Influence;
import greta.core.util.enums.Side;
import greta.core.util.id.IDProvider;
import greta.core.util.time.TimeMarker;

/**
 * Provides signals for Mathieu's Multi-layer attitude model, using the taxonomy of signals defined in NVBEventType
 *
 * @author Mathieu
 */
public class SequenceSignalProvider {


    /*
     *
     * Translates a list of signals into a list of NVBEventType
     */
    public static List<NVBEventType> listSignalAsNVBEvents(List<Signal> signalsReturned) {
        List<NVBEventType> nvbevents = new ArrayList<NVBEventType>();
        for(Signal s : signalsReturned)
        {
            NVBEventType nvbev = SequenceSignalProvider.signalAsNVBEvent(s);
            if(nvbev!=null)
                nvbevents.add(nvbev);
        }
        return nvbevents;
    }

    /*
     * Translates a signal into a NVBEventType
     */
    public static NVBEventType signalAsNVBEvent(Signal s)
    {
        if(s instanceof HeadSignal)
        {
            HeadSignal hs = (HeadSignal)s;
            if(hs.getLexeme().equalsIgnoreCase("NOD"))
            {
                return NVBEventType.HeadNod;
            }
            else if(hs.getLexeme().equalsIgnoreCase("SHAKE"))
            {
                return NVBEventType.HeadShake;
            }
            else if(hs.getLexeme().equalsIgnoreCase("Aside_Right")
                    || hs.getLexeme().equalsIgnoreCase("Aside_Left"))
            {
                return NVBEventType.HeadTilt;
            }
            else if(hs.getLexeme().equalsIgnoreCase("Down")
                    || hs.getLexeme().equalsIgnoreCase("Down_Aside_Left")
                    || hs.getLexeme().equalsIgnoreCase("Down_Aside_Right")
                    || hs.getLexeme().equalsIgnoreCase("Down_Left")
                    || hs.getLexeme().equalsIgnoreCase("Down_Right"))
            {
                return NVBEventType.HeadDown;
            }
            else if(hs.getLexeme().equalsIgnoreCase("Up")
                    || hs.getLexeme().equalsIgnoreCase("Up_Left")
                    || hs.getLexeme().equalsIgnoreCase("Up_Right"))
            {
                return NVBEventType.HeadUp;
            }
            else if(hs.getLexeme().equalsIgnoreCase("Left")
                    || hs.getLexeme().equalsIgnoreCase("Right"))
            {
                return NVBEventType.HeadSide;
            }
            else if(hs.getLexeme().equalsIgnoreCase("Neutral"))
            {
                return NVBEventType.HeadAt;
            }
        }
        else if (s instanceof GestureSignal)
        {
            GestureSignal gs = (GestureSignal) s;
            if(gs.getReference().split("=")[0].equalsIgnoreCase("adaptor"))
            {
                return NVBEventType.GestAdaptorArms;
            }
            else
            {
                return NVBEventType.GestComm;
            }
        }
        else if (s instanceof FaceSignal)
        {
            FaceSignal fs = (FaceSignal) s;
            if(fs.getReference().equalsIgnoreCase("faceexp=Eyebrows_Raise")
                    ||fs.getReference().equalsIgnoreCase("faceexp=raise_eyebrows")
                    || fs.getReference().equalsIgnoreCase("Hstar"))
            {
                return NVBEventType.EyebrowUp;
            }
            else if(fs.getReference().equalsIgnoreCase("faceexp=Eyebrows_Frown")
                    ||fs.getReference().equalsIgnoreCase("faceexp=frown"))
            {
                return NVBEventType.EyebrowDown;
            }
            else if(fs.getReference().equalsIgnoreCase("faceexp=Smile"))
            {
                return NVBEventType.Smile;
            }
            for(AUItem aui : fs.getActionUnits())
            {
                if(aui.getAUnum()==12 || aui.getAUnum()==6 )
                {
                    return NVBEventType.Smile;
                }
                else if(aui.getAUnum()==1 || aui.getAUnum()==2 )
                {
                    return NVBEventType.EyebrowUp;
                }
                else if(aui.getAUnum()==4 )
                {
                    return NVBEventType.EyebrowDown;
                }
            }
        }
        return null;
    }

    public static List<Signal> cleanSignals(List<Signal> selectedSignals) {
        List<Signal> cleanedSignals = new ArrayList<Signal>();

        for(Signal s : selectedSignals)
        {

            if(s instanceof HeadSignal)
            {
                HeadSignal hs = (HeadSignal)s;
                if(hs.getLexeme().equalsIgnoreCase("NOD"))
                {
                    cleanedSignals.add(s);
                }
                else if(hs.getLexeme().equalsIgnoreCase("SHAKE"))
                {
                    cleanedSignals.add(s);
                }
                else if(hs.getLexeme().equalsIgnoreCase("Aside_Right")
                        || hs.getLexeme().equalsIgnoreCase("Aside_Left"))
                {
                    cleanedSignals.add(s);
                }
                else if(hs.getLexeme().equalsIgnoreCase("Down")
                        || hs.getLexeme().equalsIgnoreCase("Down_Aside_Left")
                        || hs.getLexeme().equalsIgnoreCase("Down_Aside_Right")
                        || hs.getLexeme().equalsIgnoreCase("Down_Left")
                        || hs.getLexeme().equalsIgnoreCase("Down_Right"))
                {
                    cleanedSignals.add(s);
                }
                else if(hs.getLexeme().equalsIgnoreCase("Up")
                        || hs.getLexeme().equalsIgnoreCase("Up_Left")
                        || hs.getLexeme().equalsIgnoreCase("Up_Right"))
                {
                    cleanedSignals.add(s);
                }
                else if(hs.getLexeme().equalsIgnoreCase("Left")
                        || hs.getLexeme().equalsIgnoreCase("Right"))
                {
                    cleanedSignals.add(s);
                }
                else if(hs.getLexeme().equalsIgnoreCase("Neutral"))
                {
                    cleanedSignals.add(s);
                }
            }
            else if (s instanceof GestureSignal)
            {
                GestureSignal gs = (GestureSignal) s;
                if(gs.getReference().split("=")[0].equalsIgnoreCase("adaptor"))
                {
                    cleanedSignals.add(s);
                }
                else
                {
                    cleanedSignals.add(s);
                }
            }
            else if (s instanceof FaceSignal)
            {
                FaceSignal fs = (FaceSignal) s;
                for(AUItem aui : fs.getActionUnits())
                {
                    if(aui.getAUnum()==12 || aui.getAUnum()==6 )
                    {
                        cleanedSignals.add(s);
                    }
                    else if(aui.getAUnum()==1 || aui.getAUnum()==2 )
                    {
                        cleanedSignals.add(s);
                    }
                    else if(aui.getAUnum()==4 )
                    {
                        cleanedSignals.add(s);
                    }
                }
            }
        }
        return cleanedSignals;
    }

    public static List<Signal> createSignal(TimeMarker start, TimeMarker end, NVBEventType nvbevent) {

        if(nvbevent.equals(NVBEventType.HeadAt)
                 || nvbevent.equals(NVBEventType.HeadDown)
                 || nvbevent.equals(NVBEventType.HeadUp)
                 || nvbevent.equals(NVBEventType.HeadSide)
                 || nvbevent.equals(NVBEventType.HeadShake)
                 || nvbevent.equals(NVBEventType.HeadNod)
                 || nvbevent.equals(NVBEventType.HeadTilt))
        {
            return createHeadSignal(nvbevent.toString(), start.getValue(), end.getValue());
           /* HeadSignal hs = new HeadSignal(IDProvider.createID("MultiAtt").toString());
            hs.getTimeMarker("start").setValue(start.getValue());
            hs.getTimeMarker("end").setValue(end.getValue());
            hs.setLexeme("Down");
            return hs;*/
        }
        else if(nvbevent.equals(NVBEventType.Smile)
                 || nvbevent.equals(NVBEventType.EyebrowDown)
                 || nvbevent.equals(NVBEventType.EyebrowUp))
        {
            return createFaceSignal(nvbevent.toString(), start.getValue(), end.getValue());
        }
        else if(nvbevent.equals(NVBEventType.GestAdaptorArms)
                 || nvbevent.equals(NVBEventType.GestComm)
                 || nvbevent.equals(NVBEventType.GestObjectManipulation))
        {
            return createGestureSignal(nvbevent.toString(), start.getValue(), end.getValue());
        }
        else if(nvbevent.equals(NVBEventType.BodyLean)
                 || nvbevent.equals(NVBEventType.BodyRecline)
                 || nvbevent.equals(NVBEventType.BodyStraight))
        {
            return createPostureSignal(nvbevent.toString(), start.getValue(), end.getValue());
        }
        else if(nvbevent.equals(NVBEventType.RestArmsCrossed)
                 || nvbevent.equals(NVBEventType.RestHandsTogether)
                 || nvbevent.equals(NVBEventType.RestOver)
                 || nvbevent.equals(NVBEventType.RestUnder))
        {
            return createRestHandsSignal(nvbevent, start.getValue(), end.getValue());
        }

        return null;
    }


    private static List<Signal> createFaceSignal(String s, double start, double end) {
        FaceSignal fs = new FaceSignal(IDProvider.createID("MLAttSeqPlan").toString());
        fs.getTimeMarker("start").setValue(start);
        if (s.startsWith("Smile")) {
            fs.setReference("Smile_Small_Open");
            if (s.endsWith("L")) {
                fs.add(new AUItem(12, 0.5, Side.BOTH));
                fs.getTimeMarker("end").setValue(end);
            } else if (s.endsWith("H")) {
                fs.add(new AUItem(12, 1.0, Side.BOTH));
                fs.add(new AUItem(6, 1.0, Side.BOTH));
                fs.getTimeMarker("end").setValue(end);
            } else //N or other
            {
                fs.add(new AUItem(12, 0.75, Side.BOTH));
                {
                    fs.add(new AUItem(6, 0.3, Side.BOTH));
                    fs.getTimeMarker("end").setValue(end);
                }
            }

        } else if (s.startsWith("EyebrowUp")) {
            fs.setReference("Eyebrows_Raise");
            if (s.endsWith("L")) {
                fs.add(new AUItem(1, 0.3, Side.BOTH));
                fs.add(new AUItem(2, 0.3, Side.BOTH));
                fs.getTimeMarker("end").setValue(end);
            } else if (s.endsWith("H")) {
                fs.add(new AUItem(1, 1.0, Side.BOTH));
                fs.add(new AUItem(2, 1.0, Side.BOTH));
                fs.getTimeMarker("end").setValue(end);
            } else //N or other
            {
                fs.add(new AUItem(1, 0.65, Side.BOTH));
                fs.add(new AUItem(2, 0.65, Side.BOTH));
                fs.getTimeMarker("end").setValue(end);
            }
        } else if (s.startsWith("EyebrowDown")) {
            fs.setReference("Eyebrows_Frown");
            if (s.endsWith("L")) {
                fs.add(new AUItem(4, 0.3, Side.BOTH));
                fs.getTimeMarker("end").setValue(end);
            } else if (s.endsWith("H")) {
                fs.add(new AUItem(4, 1.0, Side.BOTH));
                fs.getTimeMarker("end").setValue(end);
            } else //N or other
            {
                fs.add(new AUItem(4, 0.65, Side.BOTH));
                fs.getTimeMarker("end").setValue(end);
            }
        } else {
            return null;
        }

        SignalFiller.fillSignal(fs);
        List<Signal> lst = new ArrayList<Signal>();
        lst.add(fs);
        return lst;
    }

    private static List<Signal> createHeadSignal(String s, double start, double end) {
        HeadSignal hs = new HeadSignal(IDProvider.createID("MLAttSeqPlan").toString());
        hs.setSPC(1.0);
        hs.getTimeMarker("start").setValue(start);
        hs.getTimeMarker("end").setValue(end);
        if (s.startsWith("HeadNod")) {
            if (s.endsWith("LN") || s.endsWith("LY")) {
                hs.setReference("Nod_Small");
            } else if (s.endsWith("HN") || s.endsWith("HY")) {
                hs.setReference("Nod_Big");
            } else //N or other
            {
                hs.setReference("Nod_Middle");
            }

        } else if (s.startsWith("HeadShake")) {
            if (s.endsWith("LN") || s.endsWith("LY")) {
                hs.setReference("Shake_Small");
            } else if (s.endsWith("HN") || s.endsWith("HY")) {
                hs.setReference("Shake_Big");
            } else //N or other
            {
                hs.setReference("Shake_Middle");
            }
        } else if (s.startsWith("HeadDown")) {
            hs.setLexeme("Down");
            hs.setReference("Down");
            if (s.endsWith("L")) {
                hs.setPWR(0.0);
                hs.setSPC(0.0);
            } else if (s.endsWith("H")) {
                hs.setPWR(1.0);
                hs.setSPC(1.0);
            } else //N or other
            {
                hs.setPWR(0.5);
                hs.setSPC(0.5);
            }
        } else if (s.startsWith("HeadUp")) {
            hs.setLexeme("Up");
            hs.setReference("Up");
            if (s.endsWith("L")) {
                hs.setPWR(0.0);
                hs.setSPC(0.0);
            } else if (s.endsWith("H")) {
                hs.setPWR(1.0);
                hs.setSPC(1.0);
            } else //N or other
            {
                hs.setPWR(0.5);
                hs.setSPC(0.5);
            }
        } else if (s.startsWith("HeadSide")) {
            if (Math.random() >= 0.5) {
                hs.setLexeme("Right");
                hs.setReference("Right");
            } else {
                hs.setLexeme("Left");
                hs.setReference("Left");
            }

            if (s.endsWith("L")) {
                hs.setPWR(0.0);
                hs.setSPC(0.0);
            } else if (s.endsWith("H")) {
                hs.setPWR(1.0);
                hs.setSPC(1.0);
            } else //N or other
            {
                hs.setPWR(0.5);
                hs.setSPC(0.5);
            }
        } else if (s.startsWith("HeadTilt")) {
            if (Math.random() >= 0.5) {
                hs.setLexeme("Aside_Right");
                hs.setReference("Aside_Right");
            } else {
                hs.setLexeme("Aside_Left");
                hs.setReference("Aside_Left");
            }

            if (s.endsWith("L")) {
                hs.setPWR(0.0);
                hs.setSPC(0.0);
            } else if (s.endsWith("H")) {
                hs.setPWR(1.0);
                hs.setSPC(1.0);
            } else //N or other
            {
                hs.setPWR(0.5);
                hs.setSPC(0.5);
            }
        } else {
            return null;
        }

        SignalFiller.fillSignal(hs);
        List<Signal> lst = new ArrayList<Signal>();
        lst.add(hs);
        return lst;
    }

    private static List<Signal> createGestureSignal(String event, double start, double end) {
        List<Signal> lst = new ArrayList<Signal>();
        GestureSignal gs = new GestureSignal(IDProvider.createID("MLAttSeqPlan").toString());
        gs.getTimeMarker("start").setValue(start);
        gs.getTimeMarker("end").setValue(end);
        if (event.startsWith("GestAda")) {
            /*double r = Math.random();
            if(r <=0.5)
            {
                //belly
                if(Math.random()>0.5)
                    {*/
                        return lst;
                        //gs.setReference("adaptor=belly_Ges_R");
                        //gs.getTimeMarker("ready").setValue(start+(end-start)/5);
                        //gs.getTimeMarker("stroke-start").setValue(start+2*(end-start)/5);
                        //gs.getTimeMarker("stroke-end").setValue(start+3*(end-start)/5);
                        //gs.getTimeMarker("relax").setValue(start+4*(end-start)/5);

                        /*}
                else
                    {gs.setReference("adaptor=belly_Ges_R");}
            } else if (r>=0.75)
            {
                if(Math.random()>0.5)
                    {gs.setReference("adaptor=chest_Pos_L");}
                else
                    {gs.setReference("adaptor=chest_Pos_R");}
            } else {
                if(Math.random()>0.5)
                    {gs.setReference("adaptor=head_Ges_L");}
                else
                    {gs.setReference("adaptor=head_Ges_R");}
            }*/
            //SignalProvider.fillSignal(gs);
        } else if (event.startsWith("GestObj") && end-start>2.5) {
            gs.setReference("adaptor=objectManip");
            gs.getTimeMarker("ready").setValue(start+(end-start)/5);
            gs.getTimeMarker("stroke-start").setValue(start+2*(end-start)/5);
            gs.getTimeMarker("stroke-end").setValue(start+3*(end-start)/5);
            gs.getTimeMarker("relax").setValue(start+4*(end-start)/5);
            SignalFiller.fillSignal(gs);

            GazeSignal gas = new GazeSignal("gazeobject");
            gas.getTimeMarker("ready").setValue(start+(end-start)/5);
            gas.getTimeMarker("stroke-start").setValue(start+2*(end-start)/5);
            gas.getTimeMarker("stroke-end").setValue(start+3*(end-start)/5);
            gas.getTimeMarker("relax").setValue(start+4*(end-start)/5);
            gas.setOffsetAngle(0.3);
            gas.setOffsetDirection(GazeDirection.DOWNRIGHT);
            gas.setInfluence(Influence.HEAD);

            lst.add(gas);
        } else {
                if(Math.random()>0.5)
                    {gs.setReference("beat=beat_Ges_R");}
                else
                    {gs.setReference("beat=beat_Ges_L");}
            SignalFiller.fillSignal(gs);
        }
        lst.add(gs);
        return lst;
    }

    private static List<Signal> createRestHandsSignal(NVBEventType event, double start, double end) {
        GestureSignal gs = new GestureSignal(IDProvider.createID("MLAttSeqPlan").toString());
        gs.getTimeMarker("start").setValue(start);
        gs.getTimeMarker("end").setValue(end);
        if(event.equals(NVBEventType.RestArmsCrossed))
        {
            gs.setReference("rest=ArmsCrossed");
        }
        else if(event.equals(NVBEventType.RestOver))
        {
            gs.setReference("rest=OverDesk");
        }
        else if(event.equals(NVBEventType.RestUnder))
        {
            gs.setReference("rest=UnderTable");
        }
        else if(event.equals(NVBEventType.RestHandsTogether))
        {
            gs.setReference("rest=HandsTogether");
        }
        else
        {
            gs.setReference("rest=HandsTogether");
        }
        List<Signal> lst = new ArrayList<Signal>();
        lst.add(gs);
        return lst;
    }

    private static List<Signal> createPostureSignal(String event, double start, double end) {
        TorsoSignal ts = new TorsoSignal(IDProvider.createID("MLAttSeqPlan").toString());
        GazeSignal gs = new GazeSignal(IDProvider.createID("MLAttSeqPlan").toString());
        ts.getTimeMarker("start").setValue(start);
        ts.getTimeMarker("end").setValue(end);
        ts.setSPC(-1.0);
        ts.setTMP(-1.0);
        ts.setFLD(1.0);
        ts.setPWR(1.0);

        gs.getTimeMarker("start").setValue(start);
        gs.getTimeMarker("ready").setValue(start+0.5);
        gs.getTimeMarker("relax").setValue(end-0.5);
        gs.getTimeMarker("end").setValue(end);
        gs.setInfluence(Influence.HEAD);
        gs.setOffsetAngle(0.2);

        if (event.startsWith("BodyLean")) {
            ts.setReference("BodyLean");
            gs.setOffsetDirection(GazeDirection.UP);
        } else if (event.startsWith("BodyRecline")) {
            ts.setReference("BodyRecline");
            gs.setOffsetDirection(GazeDirection.DOWN);
        } else {
            ts.setReference("NeutralPosition");
        }
        SignalFiller.fillSignal(ts);

        List<Signal> lst = new ArrayList<Signal>();
        //lst.add(ts);
        return lst;
        //return ts;
    }
}
