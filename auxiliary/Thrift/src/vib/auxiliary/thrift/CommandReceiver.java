/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import vib.auxiliary.thrift.gen_java.Message;
import vib.auxiliary.thrift.services.Receiver;
import vib.core.intentions.FMLTranslator;
import vib.core.intentions.Intention;
import vib.core.intentions.IntentionEmitter;
import vib.core.intentions.IntentionPerformer;
import vib.core.signals.BMLTranslator;
import vib.core.signals.Signal;
import vib.core.signals.SignalEmitter;
import vib.core.signals.SignalPerformer;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.log.Logs;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;
import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import vib.auxiliary.socialparameters.SocialDimension;
import vib.auxiliary.socialparameters.SocialParameterEmitter;
import vib.auxiliary.socialparameters.SocialParameterFrame;
import vib.auxiliary.socialparameters.SocialParameterPerformer;
import vib.core.util.time.TimeController;
import vib.core.util.time.Timer;

/**
 *
 * @author Ken Prepin
 */
public class CommandReceiver extends Receiver  implements IntentionEmitter, SignalEmitter, SocialParameterEmitter{

   private ArrayList<IntentionPerformer> intentionPerformers;
   private ArrayList<SocialParameterPerformer> socialParamPerformers = new ArrayList<SocialParameterPerformer>();

   private ArrayList<SignalPerformer> signalPerformers;
    private XMLParser parser;
    int cpt ;


    public CommandReceiver() {
        super();
        intentionPerformers = new ArrayList<IntentionPerformer>();
        signalPerformers = new ArrayList<SignalPerformer>();
        parser = XML.createParser();
        parser.setValidating(false);
        cpt=0;
    }

    public CommandReceiver(int port) {
        super(port);
        intentionPerformers = new ArrayList<IntentionPerformer>();
        signalPerformers = new ArrayList<SignalPerformer>();
        parser = XML.createParser();
        parser.setValidating(false);
        cpt=0;
    }

    @Override
    public void perform(Message m) {
       // if(messageID>cpt){
       //     cpt = messageID;
        if(m.getProperties() != null && !m.getProperties().isEmpty()){
            double dominance = Double.parseDouble(m.getProperties().get("Dominance"));
            double liking = Double.parseDouble(m.getProperties().get("Liking"));
            ArrayList<SocialParameterFrame> listSPF = new ArrayList<SocialParameterFrame>();
                SocialParameterFrame spf = new SocialParameterFrame(Timer.getCurrentFrameNumber());
                spf.setDoubleValue(SocialDimension.Dominance, dominance);
                spf.setDoubleValue(SocialDimension.Liking, liking);
                listSPF.add(spf);
            for(SocialParameterPerformer spp : socialParamPerformers)
            {
                spp.performSocialParameter(listSPF, IDProvider.createID("Thrift command receiver"));
            }
            Logs.debug("attitude sent : Dominance " +dominance+"; Liking "+liking);
        }
          Logs.debug("animation to play received: " + m.getString_content())  ;
        String filename = (new File(m.getString_content())).getName().replaceAll("\\.xml$", "");
        ID messageID = IDProvider.createID(m.getId());
            try {
                XMLTree xml = parser.parseFile(m.getString_content());
                if (xml.getName().equalsIgnoreCase("bml")) {

                    Mode mode = BMLTranslator.getDefaultBMLMode();
                    if (xml.hasAttribute("composition")) {
                        mode.setCompositionType(xml.getAttribute("composition"));
                    }
                    if (xml.hasAttribute("reaction_type")) {
                        mode.setReactionType(xml.getAttribute("reaction_type"));
                    }
                    if (xml.hasAttribute("reaction_duration")) {
                        mode.setReactionDuration(xml.getAttribute("reaction_duration"));
                    }
                    if (xml.hasAttribute("social_attitude")) {
                        mode.setSocialAttitude(xml.getAttribute("social_attitude"));
                    }

                    propagateSignals(BMLTranslator.BMLToSignals(xml), IDProvider.createID(filename, messageID), mode);
                }
                if (xml.getName().equalsIgnoreCase("fml-apml")) {

                    Mode mode = FMLTranslator.getDefaultFMLMode();
                    if (xml.hasAttribute("composition")) {
                        mode.setCompositionType(xml.getAttribute("composition"));
                    }
                    if (xml.hasAttribute("reaction_type")) {
                        mode.setReactionType(xml.getAttribute("reaction_type"));
                    }
                    if (xml.hasAttribute("reaction_duration")) {
                        mode.setReactionDuration(xml.getAttribute("reaction_duration"));
                    }
                    if (xml.hasAttribute("social_attitude")) {
                        mode.setSocialAttitude(xml.getAttribute("social_attitude"));
                    }

                    propagateIntentions(FMLTranslator.FMLToIntentions(xml), IDProvider.createID(filename, messageID), mode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

       // }
    }
    private void propagateSignals(List<Signal> signals, ID request, Mode mode) {
        for (SignalPerformer performer : signalPerformers) {
            performer.performSignals(signals, request, mode);
        }
    }

    private void propagateIntentions(List<Intention> intentions, ID requestId, Mode mode) {
        for (IntentionPerformer performer : intentionPerformers) {
            performer.performIntentions(intentions, requestId, mode);
        }
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        if (ip != null) {
            intentionPerformers.add(ip);
        }
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer ip) {
        intentionPerformers.remove(ip);
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
    public void addSocialParameterPerformer(SocialParameterPerformer performer) {
        if (performer != null) {
            socialParamPerformers.add(performer);
        }
    }

    @Override
    public void removeSocialParameterPerformer(SocialParameterPerformer performer) {
        if (performer != null) {
            socialParamPerformers.remove(performer);
        }
    }


}
