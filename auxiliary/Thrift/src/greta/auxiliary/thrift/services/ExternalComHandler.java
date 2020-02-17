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
