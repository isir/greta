/*
 * This file is part of the auxiliaries of Greta.
 * 
 * Greta is free software: you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Greta.If not, see <http://www.gnu.org/licenses/>.
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
