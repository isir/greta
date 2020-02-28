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
package greta.auxiliary.activemq.aria;

import greta.auxiliary.activemq.TextReceiver;
import greta.auxiliary.activemq.WhiteBoard;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Angelo Cafaro
 */
public class ARIAInformationStateReceiver extends TextReceiver implements ARIAInformationStateEmitter {

    private ArrayList<ARIAInformationStatePerformer> ariaInfoStatePerformers;

    public ARIAInformationStateReceiver(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "aria.informationstate.receiver");
    }

    public ARIAInformationStateReceiver(String host, String port, String topic){
        super(host, port, topic);
        ariaInfoStatePerformers = new ArrayList<ARIAInformationStatePerformer>();
    }

    @Override
    public void addARIAInformationStatePerformer(ARIAInformationStatePerformer ariaInformationStatePerformer) {
        if (ariaInformationStatePerformer != null)
        {
            ariaInfoStatePerformers.add(ariaInformationStatePerformer);
        }
    }

    @Override
    public void removeARIAInformationStatePerformer(ARIAInformationStatePerformer ariaInformationStatePerformer) {
        if (ariaInformationStatePerformer != null && ariaInfoStatePerformers.contains(ariaInformationStatePerformer))
        {
            ariaInfoStatePerformers.remove(ariaInformationStatePerformer);
        }
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {

        if(content == null){
            return;
        }

        Object interactionState = properties.get("interaction-state");
        Object language = properties.get("language");

        for(ARIAInformationStatePerformer performer : ariaInfoStatePerformers){

            if (interactionState != null) {
                performer.performStateChange(interactionState.toString().trim());
            }

            if (language != null) {
                performer.performLanguageChange(language.toString().trim());
            }
        }
    }
}
