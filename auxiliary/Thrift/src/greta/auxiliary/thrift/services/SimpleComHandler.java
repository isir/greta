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

import greta.auxiliary.thrift.gen_java.Message;
import greta.auxiliary.thrift.gen_java.SimpleCom;
import org.apache.thrift.TException;

// Generated code

/**
 *
 * @author Ken Prepin
 */
public class SimpleComHandler implements SimpleCom.Iface{

    private Message message;
    private Receiver receiver;

    public SimpleComHandler(Receiver receiver) {
        message = new Message();
        this.receiver = receiver;
    }

    @Override
    public void send(Message m) throws TException {
        message=m;

        if(message.id!=null)
            System.out.println("message received by server:"+message.id+" "+message.type);

        receiver.perform(message);
    }

    @Override
    public boolean isStarted() {
       return receiver.isConnected();
    }

}
