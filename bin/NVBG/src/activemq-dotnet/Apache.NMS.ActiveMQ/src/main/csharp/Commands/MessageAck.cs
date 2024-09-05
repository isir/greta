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
     *  Command code for OpenWire format for MessageAck
     *
     *  NOTE!: This file is auto generated - do not modify!
     *         if you need to make a change, please see the Java Classes
     *         in the nms-activemq-openwire-generator module
     *
     */
    public class MessageAck : BaseCommand
    {
        public const byte ID_MESSAGEACK = 22;

        ActiveMQDestination destination;
        TransactionId transactionId;
        ConsumerId consumerId;
        byte ackType;
        MessageId firstMessageId;
        MessageId lastMessageId;
        int messageCount;
        BrokerError poisonCause;

        public MessageAck() : base()
        {
        }

        public MessageAck(MessageDispatch dispatch, byte ackType, int messageCount) : base()
        {
            this.ackType = ackType;
            this.consumerId = dispatch.ConsumerId;
            this.destination = dispatch.Destination;
            this.lastMessageId = dispatch.Message.MessageId;
            this.messageCount = messageCount;
        }

        public MessageAck(Message message, byte ackType, int messageCount) : base()
        {
            this.ackType = ackType;
            this.destination = message.Destination;
            this.lastMessageId = message.MessageId;
            this.messageCount = messageCount;
        }

        ///
        /// <summery>
        ///  Get the unique identifier that this object and its own
        ///  Marshaler share.
        /// </summery>
        ///
        public override byte GetDataStructureType()
        {
            return ID_MESSAGEACK;
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
                "Destination = " + Destination + ", " + 
                "TransactionId = " + TransactionId + ", " + 
                "ConsumerId = " + ConsumerId + ", " + 
                "AckType = " + AckType + ", " + 
                "FirstMessageId = " + FirstMessageId + ", " + 
                "LastMessageId = " + LastMessageId + ", " + 
                "MessageCount = " + MessageCount + ", " + 
                "PoisonCause = " + PoisonCause + " ]";
        }

        public ActiveMQDestination Destination
        {
            get { return destination; }
            set { this.destination = value; }
        }

        public TransactionId TransactionId
        {
            get { return transactionId; }
            set { this.transactionId = value; }
        }

        public ConsumerId ConsumerId
        {
            get { return consumerId; }
            set { this.consumerId = value; }
        }

        public byte AckType
        {
            get { return ackType; }
            set { this.ackType = value; }
        }

        public MessageId FirstMessageId
        {
            get { return firstMessageId; }
            set { this.firstMessageId = value; }
        }

        public MessageId LastMessageId
        {
            get { return lastMessageId; }
            set { this.lastMessageId = value; }
        }

        public int MessageCount
        {
            get { return messageCount; }
            set { this.messageCount = value; }
        }

        public BrokerError PoisonCause
        {
            get { return poisonCause; }
            set { this.poisonCause = value; }
        }

        ///
        /// <summery>
        ///  Return an answer of true to the isMessageAck() query.
        /// </summery>
        ///
        public override bool IsMessageAck
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
            return visitor.ProcessMessageAck(this);
        }

    };
}

