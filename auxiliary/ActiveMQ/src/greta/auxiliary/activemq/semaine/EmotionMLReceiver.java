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
import greta.auxiliary.emotionml.EmotionMLTranslator;
import greta.auxiliary.socialparameters.SocialParameterEmitter;
import greta.auxiliary.socialparameters.SocialParameterFrame;
import greta.auxiliary.socialparameters.SocialParameterPerformer;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
