/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextReceiver;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.intentions.FMLTranslator;
import vib.core.intentions.Intention;
import vib.core.intentions.IntentionEmitter;
import vib.core.intentions.IntentionPerformer;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import vib.auxiliary.emotionml.EmotionMLTranslator;
import vib.auxiliary.socialparameters.SocialParameterEmitter;
import vib.auxiliary.socialparameters.SocialParameterFrame;
import vib.auxiliary.socialparameters.SocialParameterPerformer;

/**
 *
 * @author Mathieu Chollet
 */
public class EmotionMLReceiver extends TextReceiver implements IntentionEmitter, SocialParameterEmitter {

    private ArrayList<IntentionPerformer> intentionPerformers;
    private ArrayList<SocialParameterPerformer> socialPerformers;
    private XMLParser emotionmlParser;

    public EmotionMLReceiver() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "EmotionML");
    }

    public EmotionMLReceiver(String host, String port, String topic) {
        super(host, port, topic);
        intentionPerformers = new ArrayList<IntentionPerformer>();
        socialPerformers = new ArrayList<SocialParameterPerformer>();
        emotionmlParser = XML.createParser();
        emotionmlParser.setValidating(false);
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {
        XMLTree emotionml = emotionmlParser.parseBuffer(content.toString());

        if (emotionml == null) {
            return;
        }

        List<Intention> intentions = EmotionMLTranslator.EmotionMLToIntentions(emotionml);

        List<SocialParameterFrame> socialFrames = EmotionMLTranslator.EmotionMLToSocialParameters(emotionml);

        String request = "TardisAffectiveCore";
        if (properties.get("content-id") != null) {
            request = properties.get("content-id").toString();
        }

        ID id = IDProvider.createID(request);
        for (IntentionPerformer performer : intentionPerformers) {
            performer.performIntentions(intentions, id, FMLTranslator.getDefaultFMLMode());
        }
        for (SocialParameterPerformer performer : socialPerformers) {
            performer.performSocialParameter(socialFrames, id);
        }
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        intentionPerformers.add(ip);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer ip) {
        intentionPerformers.remove(ip);
    }

    @Override
    public void addSocialParameterPerformer(SocialParameterPerformer spp) {
        socialPerformers.add(spp);
    }

    @Override
    public void removeSocialParameterPerformer(SocialParameterPerformer spp) {
        socialPerformers.remove(spp);
    }
}
