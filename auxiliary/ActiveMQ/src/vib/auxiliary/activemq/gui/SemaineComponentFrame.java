/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.gui;

import vib.auxiliary.activemq.WhiteBoard;
import vib.auxiliary.activemq.semaine.SemaineComponent;

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
