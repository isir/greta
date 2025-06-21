/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.auxiliary.activemq.gui;

import greta.auxiliary.activemq.WhiteBoard;
import greta.auxiliary.activemq.semaine.SemaineComponent;

/**
 *
 * @author Andre-Marie Pez
 */
public class SemaineComponentFrame extends WhitboardFrame{

    public SemaineComponentFrame(){
        changeDestinationLabel("semaine.component.name");
        removeTopicAndQueueOption();
    }

    SemaineComponent semaineComponent;

    @Override
    protected void updateURL(String host, String port){
        if(semaineComponent!=null) {
            semaineComponent.setURL(host, port);
        }
    }

    @Override
    protected void updateDestination(String name, boolean b){
        if(semaineComponent!=null) {
            semaineComponent.setName(name);
        }
    }

    @Override
    public void setWhitboard(WhiteBoard wb){}//useless

    public void setSemaineComponent(SemaineComponent component){
        semaineComponent = component;
        setHostValue(semaineComponent.getMetaReceiver().getHost());
        setPortValue(semaineComponent.getMetaReceiver().getPort());
        setDestinationValue(semaineComponent.getName());
        setConnected(semaineComponent.getMetaReceiver().isConnected());
        semaineComponent.getMetaReceiver().addConnectionListener(this);
    }
}
