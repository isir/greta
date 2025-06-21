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
package greta.auxiliary.thrift.services;

import greta.auxiliary.thrift.gen_java.ExternalCom;
import greta.auxiliary.thrift.gen_java.Message;

/**
 *
 * @author Ken Prepin
 */
public class ExternalComHandler implements ExternalCom.Iface{

    public static final String messageInitId = "initMessage";
    private ServerToExternal serverToExt;
    private Message message;


    public ExternalComHandler(ServerToExternal serverToExt) {
        this.message = new Message();
        this.serverToExt = serverToExt;
    }

    @Override
    public void send(Message m) {
        message=m;

        if(message.type!=null) {
            System.out.println("message received by ServerToExt:"+message.type);
        }

        //serverToExt.perform(message);
    }

    @Override
    public boolean isStarted() {
       return serverToExt.isConnected();
    }

    @Override
    public Message update(String oldMessageId) {
        message = serverToExt.getMessage(oldMessageId);
        return message;
    }

    @Override
    public boolean isNewMessage(String oldMessageId) {
        return serverToExt.isNewMessage(oldMessageId);
    }

}
