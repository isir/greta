/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.auxiliary.thrift;

import vib.auxiliary.thrift.gen_java.Message;
import vib.auxiliary.thrift.services.Receiver;
import vib.core.util.log.Logs;

/**
 *
 * @author Ken Prepin
 */
public class ThriftMessageToLogs extends Receiver {

    @Override
    public void perform(Message m) {
        String logsMessage = "Message received by server:";
        logsMessage += " ID-" + m.id;
        logsMessage += " Type-" + m.type;
        logsMessage += " Time-" + m.time;
        Logs.info(logsMessage);
        if (m.string_content != "") {
            Logs.info("String_content: \n"+ m.string_content);
        }
    }
}
