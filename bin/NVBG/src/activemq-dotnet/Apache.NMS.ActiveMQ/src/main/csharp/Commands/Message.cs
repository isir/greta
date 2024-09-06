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
using System;

using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Commands
{
    /*
     *
     *  Command code for OpenWire format for Message
     *
     *  NOTE!: This file is auto generated - do not modify!
     *         if you need to make a change, please see the Java Classes
     *         in the nms-activemq-openwire-generator module
     *
     */
    public class Message : BaseCommand, MessageReference, MarshallAware, ICloneable
    {
        public const byte ID_MESSAGE = 0;

        ProducerId producerId;
        ActiveMQDestination destination;
        TransactionId transactionId;
        ActiveMQDestination originalDestination;
        MessageId messageId;
        TransactionId originalTransactionId;
        string groupID;
        int groupSequence;
        string correlationId;
        bool persistent;
        long expiration;
        byte priority;
        ActiveMQDestination replyTo;
        long timestamp;
        string type;
        byte[] content;
        byte[] marshalledProperties;
        DataStructure dataStructure;
        ConsumerId targetConsumerId;
        bool compressed;
        int redeliveryCounter;
        BrokerId[] brokerPath;
        long arrival;
        string userID;
        bool recievedByDFBridge;
        bool droppable;
        BrokerId[] cluster;
        long brokerInTime;
        long brokerOutTime;
        bool jMSXGroupFirstForConsumer;

        private bool readOnlyMsgProperties;
        private bool readOnlyMsgBody;

        public const int DEFAULT_MINIMUM_MESSAGE_SIZE = 1024;

        ///
        /// <summery>
        ///  Get the unique identifier that this object and its own
        ///  Marshaler share.
        /// </summery>
        ///
        public override byte GetDataStructureType()
        {
            return ID_MESSAGE;
        }

        ///
        /// <summery>
        ///  Clone this object and return a new instance that the caller now owns.
        /// </summery>
        ///
        public override Object Clone()
        {
            // Since we are a derived class use the base's Clone()
            // to perform the shallow copy. Since it is shallow it
            // will include our derived class. Since we are derived,
            // this method is an override.
            Message o = (Message) base.Clone();

            if( this.messageId != null )
            {
                o.MessageId = (MessageId) this.messageId.Clone();
            }

            return o;
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
                "ProducerId = " + ProducerId + ", " + 
                "Destination = " + Destination + ", " + 
                "TransactionId = " + TransactionId + ", " + 
                "OriginalDestination = " + OriginalDestination + ", " + 
                "MessageId = " + MessageId + ", " + 
                "OriginalTransactionId = " + OriginalTransactionId + ", " + 
                "GroupID = " + GroupID + ", " + 
                "GroupSequence = " + GroupSequence + ", " + 
                "CorrelationId = " + CorrelationId + ", " + 
                "Persistent = " + Persistent + ", " + 
                "Expiration = " + Expiration + ", " + 
                "Priority = " + Priority + ", " + 
                "ReplyTo = " + ReplyTo + ", " + 
                "Timestamp = " + Timestamp + ", " + 
                "Type = " + Type + ", " + 
                "Content = " + Content + ", " + 
                "MarshalledProperties = " + MarshalledProperties + ", " + 
                "DataStructure = " + DataStructure + ", " + 
                "TargetConsumerId = " + TargetConsumerId + ", " + 
                "Compressed = " + Compressed + ", " + 
                "RedeliveryCounter = " + RedeliveryCounter + ", " + 
                "BrokerPath = " + BrokerPath + ", " + 
                "Arrival = " + Arrival + ", " + 
                "UserID = " + UserID + ", " + 
                "RecievedByDFBridge = " + RecievedByDFBridge + ", " + 
                "Droppable = " + Droppable + ", " + 
                "Cluster = " + Cluster + ", " + 
                "BrokerInTime = " + BrokerInTime + ", " + 
                "BrokerOutTime = " + BrokerOutTime + ", " + 
                "JMSXGroupFirstForConsumer = " + JMSXGroupFirstForConsumer + " ]";
        }

        public virtual int Size()
        {
            int size = DEFAULT_MINIMUM_MESSAGE_SIZE;

            if(marshalledProperties != null)
            {
                size += marshalledProperties.Length;
            }
            if(content != null)
            {
                size += content.Length;
            }

            return size;
        }

        public virtual void OnSend()
        {
            this.ReadOnlyProperties = true;
            this.ReadOnlyBody = true;
        }

        public virtual void OnMessageRollback()
        {
            this.redeliveryCounter++;
        }

        public bool IsExpired()
        {
            return this.expiration == 0 ? false : DateTime.UtcNow > DateUtils.ToDateTimeUtc(this.expiration);
        }

        public ProducerId ProducerId
        {
            get { return producerId; }
            set { this.producerId = value; }
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

        public ActiveMQDestination OriginalDestination
        {
            get { return originalDestination; }
            set { this.originalDestination = value; }
        }

        public MessageId MessageId
        {
            get { return messageId; }
            set { this.messageId = value; }
        }

        public TransactionId OriginalTransactionId
        {
            get { return originalTransactionId; }
            set { this.originalTransactionId = value; }
        }

        public string GroupID
        {
            get { return groupID; }
            set { this.groupID = value; }
        }

        public int GroupSequence
        {
            get { return groupSequence; }
            set { this.groupSequence = value; }
        }

        public string CorrelationId
        {
            get { return correlationId; }
            set { this.correlationId = value; }
        }

        public bool Persistent
        {
            get { return persistent; }
            set { this.persistent = value; }
        }

        public long Expiration
        {
            get { return expiration; }
            set { this.expiration = value; }
        }

        public byte Priority
        {
            get { return priority; }
            set { this.priority = value; }
        }

        public ActiveMQDestination ReplyTo
        {
            get { return replyTo; }
            set { this.replyTo = value; }
        }

        public long Timestamp
        {
            get { return timestamp; }
            set { this.timestamp = value; }
        }

        public string Type
        {
            get { return type; }
            set { this.type = value; }
        }

        public byte[] Content
        {
            get { return content; }
            set { this.content = value; }
        }

        public byte[] MarshalledProperties
        {
            get { return marshalledProperties; }
            set { this.marshalledProperties = value; }
        }

        public DataStructure DataStructure
        {
            get { return dataStructure; }
            set { this.dataStructure = value; }
        }

        public ConsumerId TargetConsumerId
        {
            get { return targetConsumerId; }
            set { this.targetConsumerId = value; }
        }

        public bool Compressed
        {
            get { return compressed; }
            set { this.compressed = value; }
        }

        public int RedeliveryCounter
        {
            get { return redeliveryCounter; }
            set { this.redeliveryCounter = value; }
        }

        public BrokerId[] BrokerPath
        {
            get { return brokerPath; }
            set { this.brokerPath = value; }
        }

        public long Arrival
        {
            get { return arrival; }
            set { this.arrival = value; }
        }

        public string UserID
        {
            get { return userID; }
            set { this.userID = value; }
        }

        public bool RecievedByDFBridge
        {
            get { return recievedByDFBridge; }
            set { this.recievedByDFBridge = value; }
        }

        public bool Droppable
        {
            get { return droppable; }
            set { this.droppable = value; }
        }

        public BrokerId[] Cluster
        {
            get { return cluster; }
            set { this.cluster = value; }
        }

        public long BrokerInTime
        {
            get { return brokerInTime; }
            set { this.brokerInTime = value; }
        }

        public long BrokerOutTime
        {
            get { return brokerOutTime; }
            set { this.brokerOutTime = value; }
        }

        public bool JMSXGroupFirstForConsumer
        {
            get { return jMSXGroupFirstForConsumer; }
            set { this.jMSXGroupFirstForConsumer = value; }
        }

        public virtual bool ReadOnlyProperties
        {
            get { return this.readOnlyMsgProperties; }
            set { this.readOnlyMsgProperties = value; }
        }

        public virtual bool ReadOnlyBody
        {
            get { return this.readOnlyMsgBody; }
            set { this.readOnlyMsgBody = value; }
        }

        ///
        /// <summery>
        ///  Return an answer of true to the isMessage() query.
        /// </summery>
        ///
        public override bool IsMessage
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
            return visitor.ProcessMessage(this);
        }

    };
}

