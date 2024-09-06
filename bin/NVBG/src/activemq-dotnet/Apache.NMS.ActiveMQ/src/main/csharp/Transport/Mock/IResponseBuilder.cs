/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System.Collections.Generic;
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.Transport.Mock
{    
    /// <summary>
    /// Defines an Interface for a Command Response Builder used by the MockTransport
    /// to answer Commands sent via the Request and AsnycRequest methods.
    /// </summary>
    public interface IResponseBuilder
    {
        /// <summary>
        /// Given a Command, check if it requires a response and return the
        /// appropriate Response that the Broker would send for this Command
        /// </summary>
        Response BuildResponse(Command command);

        /// <summary>
        /// When called the ResponseBuilder must construct all the Responses or 
        /// Asynchronous commands that would be sent to this client by the Broker 
        /// upon receipt of the passed command.
        /// </summary>
        List<Command> BuildIncomingCommands(Command command);
            
    }
}
