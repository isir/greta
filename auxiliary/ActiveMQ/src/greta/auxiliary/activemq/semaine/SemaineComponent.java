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

import greta.auxiliary.activemq.Receiver;
import greta.auxiliary.activemq.Sender;
import greta.auxiliary.activemq.TextReceiver;
import greta.auxiliary.activemq.TextSender;
import greta.auxiliary.activemq.WhiteBoard;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class simulates a fake SEMAINE Component
 * @author Andre-Marie Pez
 */
public class SemaineComponent {

    private Receiver fromSemaine;
    private Sender toSemaine;
    private Map<String,Object> map;

    public SemaineComponent(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,WhiteBoard.DEFAULT_ACTIVEMQ_PORT,"SemaineComponent");
    }

    public SemaineComponent(String host, String port, String name){
        map = new HashMap<String,Object>();
        map.put("ComponentName", name);
        map.put("ComponentState", "ready");
        map.put("TotalMessagesReceived", new Integer(0));
        map.put("IsInputComponent", Boolean.FALSE);
        map.put("IsOutputComponent", Boolean.FALSE);
        updateTopics();
        fromSemaine = new TextReceiver(host, port, "semaine.meta"){
            @Override
            protected void onMessage(String content, Map<String, Object> properties) {
                if("SystemManager".equals(properties.get("source"))){
                    if(properties.containsKey("Ping") || properties.containsKey("DoReportTopics")) {
                        helloSemaine();
                    }
                    if (properties.containsKey("SystemReadyTime")) {
                        setTime(new Long(properties.get("SystemReadyTime").toString()));
                    }
                }
            }
        };

        toSemaine = new TextSender(host, port, "semaine.meta"){
            @Override
            protected void onConnectionStarted() {
                super.onConnectionStarted();
                helloSemaine();
            }
        };
    }

    @Override
    public void finalize() throws Throwable{
        toSemaine.stopConnection();
        fromSemaine.stopConnection();
        super.finalize();
    }

    public String getName(){
        return map.get("ComponentName").toString();
    }
    public void setName(String name){
        //to dup semaine
        helloSemaine(Long.MAX_VALUE); //the old component will be immortal
        //semaine dupped
        map.put("ComponentName", name);
        helloSemaine();
    }

    private List<WhiteBoard> whiteBorads = new ArrayList<WhiteBoard>();
    public void connect(WhiteBoard wb){
        whiteBorads.add(wb);
        helloSemaine();
    }

    public void disconnect(WhiteBoard wb){
        whiteBorads.remove(wb);
        helloSemaine();
    }
    private void updateTopics(){
        String receiveTopics="";
        String sendTopics="";
        for(WhiteBoard wb : whiteBorads){
            if(wb instanceof Receiver){
                if(!receiveTopics.isEmpty()) {
                    receiveTopics += " ";
                }
                receiveTopics += wb.getTopic();
            }
            if(wb instanceof Sender){
                if(!sendTopics.isEmpty()) {
                    sendTopics += " ";
                }
                sendTopics += wb.getTopic();
            }
        }
        //to dup semaine
        if(receiveTopics.isEmpty()) {
            receiveTopics = "nothing";
        }
        //semaine dupped
        map.put("ReceiveTopics", receiveTopics);
        map.put("SendTopics", sendTopics);
    }

   // private long delta = -System.currentTimeMillis();

    private void setTime(long time) {
        Timer.setTimeMillis(time);
       // delta = System.currentTimeMillis() - time;
    }

    public long getTime() {
       // return System.currentTimeMillis() - delta;
        return Timer.getTimeMillis();
    }

    private void helloSemaine(){ helloSemaine(getTime()); }

    private void helloSemaine(long time){
        map.put("LastSeenAlive", new Long(time));
        updateTopics();
        toSemaine.send("Hello Semaine !", map);
    }

    public void setURL(String host, String port){
        toSemaine.setURL(host, port);
        fromSemaine.setURL(host, port);
        helloSemaine();
    }

    public WhiteBoard getMetaSender(){
        return toSemaine;
    }

    public WhiteBoard getMetaReceiver(){
        return fromSemaine;
    }

      public void setHost(String host) {
        if (!this.getHost().equals(host)) {
            setURL(host, getPort());
        }
    }

    public String getHost() {
        return fromSemaine.getHost();
    }

    public void setPort(String port) {
        if (!this.getPort().equals(port)) {
            setURL(getHost(), port);
        }
    }

    public String getPort() {
        return fromSemaine.getPort();
    }
}
