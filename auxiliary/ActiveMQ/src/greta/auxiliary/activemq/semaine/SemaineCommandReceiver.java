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
package greta.auxiliary.activemq.semaine;

import greta.auxiliary.activemq.TextReceiver;
import greta.auxiliary.activemq.WhiteBoard;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class SemaineCommandReceiver extends TextReceiver implements SemaineCommandEmitter{

    private ArrayList<SemaineCommandPerformer> performers;

    public SemaineCommandReceiver(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "semaine.data.synthesis.lowlevel.command");
    }

    public SemaineCommandReceiver(String host, String port, String topic){
        super(host, port, topic);
        performers = new ArrayList<SemaineCommandPerformer>();
    }

    @Override
    public void addSemaineCommandPerformer(SemaineCommandPerformer semaineCommandPerformer) {
        if(semaineCommandPerformer!=null){
            performers.add(semaineCommandPerformer);
        }
    }

    @Override
    public void removeSemaineCommandPerformer(SemaineCommandPerformer semaineCommandPerformer) {
        if(semaineCommandPerformer!=null && performers.contains(semaineCommandPerformer)){
            performers.remove(semaineCommandPerformer);
        }
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {

        if(content == null){
            return;
        }

        String[] commands = content.toString().split("\\n");
        String id = properties.get("content-id").toString();

        if("dataInfo".equals(properties.get("datatype"))){
            boolean hasAudio = false;
            boolean hasFAP = false;
            boolean hasBAP = false;
            for(String command : commands){
                if(command.startsWith("HASAUDIO") && command.endsWith(" 1")){
                    hasAudio = true;
                }
                if(command.startsWith("HASFAP") && command.endsWith(" 1")){
                    hasFAP = true;
                }
                if(command.startsWith("HASBAP") && command.endsWith(" 1")){
                    hasBAP = true;
                }
            }
            for(SemaineCommandPerformer performer : performers){
                performer.performDataInfo(hasAudio, hasFAP, hasBAP, id);
            }
        }

        if("playCommand".equals(properties.get("datatype"))){
            long startAt = Timer.getTimeMillis();
            long lifeTime = 0;
            double priority = 0;
            for(String command : commands){
                String[] commandPair = command.split(" ");
                if(commandPair.length==2){
                    if(commandPair[0].equals("STARTAT")){
                        startAt += Long.parseLong(commandPair[1]);
                    }
                    if(commandPair[0].equals("LIFETIME")){
                        lifeTime = Long.parseLong(commandPair[1]);
                        if(lifeTime<0){ //a negative life time has no sens
                            lifeTime = 604800000; //une semaine :)
                        }
                    }
                    if(commandPair[0].equals("PRIORITY")){
                        priority = Double.parseDouble(commandPair[1]);
                    }
                }
            }
            for(SemaineCommandPerformer performer : performers){
                performer.performPlayCommand(startAt, lifeTime, priority, id);
            }
        }
    }
}
