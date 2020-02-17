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
