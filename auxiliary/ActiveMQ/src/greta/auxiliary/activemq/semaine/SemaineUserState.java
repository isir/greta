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
package greta.auxiliary.activemq.semaine;

import greta.auxiliary.activemq.TextReceiver;
import greta.auxiliary.activemq.WhiteBoard;
import greta.core.signals.FaceSignal;
import greta.core.signals.HeadSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.time.Timer;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class SemaineUserState extends TextReceiver implements SignalEmitter{
    private static String semaine_ns = "http://www.semaine-project.eu/semaineml";
    private static String bml_ns = "http://www.mindmakers.org/projects/BML";
    private static String emotionml_ns = "http://www.w3.org/2009/10/emotionml";

    private XMLParser stateParser;
    private List<SignalPerformer> signalPerformers;
    private SpeechSignal currentSpeech;
    private long currentSpeechReceivedTime = Timer.getTimeMillis();
    private CharacterManager cm;

    public SemaineUserState(CharacterManager cm){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "semaine.data.state.user.behaviour", cm);
    }

    public SemaineUserState(String host, String port, String topic,CharacterManager cm){
        super(host, port, topic);
        this.cm = cm;
        stateParser = XML.createParser();
        stateParser.setValidating(false);
        signalPerformers = new ArrayList<SignalPerformer>();
    }

    private synchronized SpeechSignal startSpeaking(){
        stopSpeaking(); // finish the last speak
        currentSpeech = new SpeechSignal(cm);
        currentSpeech.getStart().setValue(0);//now
        currentSpeech.getEnd().setValue(604800); // we don't know the end yet... we put it to one week (une semaine ;) )
        currentSpeechReceivedTime = Timer.getTimeMillis();
        return currentSpeech;
    }

    private synchronized void stopSpeaking(){
        if(currentSpeech!=null){
            //the current speech may be send. we say for who receives it that is finish
            currentSpeech.getEnd().setValue((Timer.getTimeMillis()-currentSpeechReceivedTime)/1000.0);
            //then we don't care of it
            currentSpeech = null;
        }
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {
        XMLTree userstate = stateParser.parseBuffer(content.toString());
        if(userstate!=null && userstate.isNamed("user-state") && userstate.isNameSpaced(semaine_ns)) {
            long currentTime = Timer.getTimeMillis();
            int itemCount = 0;
            ArrayList<Signal> userBehaviours = new ArrayList<Signal>();
            for(XMLTree child : userstate.getChildrenElement()){
                if(child.isNamed("speaking") && child.isNameSpaced(semaine_ns)){
                    if(child.hasAttribute("status")){
                        if(child.getAttribute("status").equalsIgnoreCase("true")){
                            userBehaviours.add(startSpeaking());
                        }
                        else{
                            stopSpeaking();
                        }
                    }
                }
                if(child.isNamed("bml") && child.isNameSpaced(bml_ns)){
                    for(XMLTree bmlchild : child.getChildrenElement()){
                        if(bmlchild.isNamed("head")){
                            HeadSignal head = new HeadSignal("User_"+currentTime+"_Head_"+(itemCount++));
                            head.setLexeme(bmlchild.getAttribute("type").replace("TILT-", "Aside_"));
                            userBehaviours.add(head);
                        }
                        if(bmlchild.isNamed("face")){
                            if(bmlchild.hasAttribute("au")){
                                String aus = bmlchild.getAttribute("au");
                                if(aus.length()>0 && !aus.equals("0")){
                                    String[] auss = aus.split(" ");
                                    for(String au : auss){
                                        FaceSignal face = new FaceSignal("User_"+currentTime+"_AU"+au+"_"+(itemCount++));
                                        face.setReference("faceexp=AU"+au);
                                        userBehaviours.add(face);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if( ! userBehaviours.isEmpty()){
                for(SignalPerformer sp : signalPerformers){
                    sp.performSignals(userBehaviours, IDProvider.createID("SemaineUserBehaviours"), new Mode(CompositionType.blend));
                }
            }
//            else{
//                System.out.println(userstate);
//            }
        }
        else{
            Logs.error("not found");
        }
    }

    @Override
    public void addSignalPerformer(SignalPerformer sp) {
        if(sp != null){
            signalPerformers.add(sp);
        }
    }

    @Override
    public void removeSignalPerformer(SignalPerformer sp) {
        signalPerformers.remove(sp);
    }
}
