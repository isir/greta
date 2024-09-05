/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


using Apache.NMS.ActiveMQ.State;

namespace Apache.NMS.ActiveMQ.Commands
{
    /*
     *
     *  Command code for OpenWire format for MessagePull
     *
     *  NOTE!: This file is auto generated - do not modify!
     *         if you need to make a change, please see the Java Classes
     *         in the nms-activemq-openwire-generator module
     *
     */
    public class MessagePull : BaseCommand
    {
        public const byte ID_MESSAGEPULL = 20;

        ConsumerId consumerId;
        ActiveMQDestination destination;
        long timeout;
        string correlationId;
        MessageId messageId;

        ///
        /// <summery>
        ///  Get the unique identifier that this object and its own
        ///  Marshaler share.
        /// </summery>
        ///
        public override byte GetDataStructureType()
        {
            return ID_MESSAGEPULL;
        }

        ///
        /// <summery>
        ///  Returns a string containing the information for this DataStructure
        ///  such as its type and value of its elements.
        /// </summery>
        ///
        public override string ToString()
        {
            return GetType().Name + "[ " + 
                "commandId = " + this.CommandId + ", " + 
                "responseRequired = " + this.ResponseRequired + ", " + 
                "ConsumerId = " + ConsumerId + ", " + 
                "Destination = " + Destination + ", " + 
                "Timeout = " + Timeout + ", " + 
                "CorrelationId = " + CorrelationId + ", " + 
                "MessageId = " + MessageId + " ]";
        }

        public ConsumerId ConsumerId
        {
            get { return consumerId; }
            set { this.consumerId = value; }
        }

        public ActiveMQDestination Destination
        {
            get { return destination; }
            set { this.destination = value; }
        }

        public long Timeout
        {
            get { return timeout; }
            set { this.timeout = value; }
        }

        public string CorrelationId
        {
            get { return correlationId; }
            set { this.correlationId = value; }
        }

        public MessageId MessageId
        {
            get { return messageId; }
            set { this.messageId = value; }
        }

        ///
        /// <summery>
        ///  Return an answer of true to the isMessagePull() query.
        /// </summery>
        ///
        public override bool IsMessagePull
        {
            get { return true; }
        }

        ///
        /// <summery>
        ///  Allows a Visitor to visit this command and return a response to the
        ///  command based on the command type being visited.  The command will call
        ///  the proper processXXX method in the visitor.
        /// </summery>
        ///
        public override Response Visit(ICommandVisitor visitor)
        {
            return visitor.ProcessMessagePull(this);
        }

    };
}

