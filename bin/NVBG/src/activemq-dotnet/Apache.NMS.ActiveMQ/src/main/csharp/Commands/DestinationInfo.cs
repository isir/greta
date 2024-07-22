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
     *  Command code for OpenWire format for DestinationInfo
     *
     *  NOTE!: This file is auto generated - do not modify!
     *         if you need to make a change, please see the Java Classes
     *         in the nms-activemq-openwire-generator module
     *
     */
    public class DestinationInfo : BaseCommand
    {
        public const byte ID_DESTINATIONINFO = 8;

        public const byte ADD_OPERATION_TYPE = 0;
        public const byte REMOVE_OPERATION_TYPE = 1;
        ConnectionId connectionId;
        ActiveMQDestination destination;
        byte operationType;
        long timeout;
        BrokerId[] brokerPath;

        ///
        /// <summery>
        ///  Get the unique identifier that this object and its own
        ///  Marshaler share.
        /// </summery>
        ///
        public override byte GetDataStructureType()
        {
            return ID_DESTINATIONINFO;
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
                "ConnectionId = " + ConnectionId + ", " + 
                "Destination = " + Destination + ", " + 
                "OperationType = " + OperationType + ", " + 
                "Timeout = " + Timeout + ", " + 
                "BrokerPath = " + BrokerPath + " ]";
        }

        public bool IsAddOperation
        {
            get
            {
                return OperationType == ADD_OPERATION_TYPE;
            }
        }

        public bool IsRemoveOperation
        {
            get
            {
                return OperationType == REMOVE_OPERATION_TYPE;
            }
        }
        public ConnectionId ConnectionId
        {
            get { return connectionId; }
            set { this.connectionId = value; }
        }

        public ActiveMQDestination Destination
        {
            get { return destination; }
            set { this.destination = value; }
        }

        public byte OperationType
        {
            get { return operationType; }
            set { this.operationType = value; }
        }

        public long Timeout
        {
            get { return timeout; }
            set { this.timeout = value; }
        }

        public BrokerId[] BrokerPath
        {
            get { return brokerPath; }
            set { this.brokerPath = value; }
        }

        ///
        /// <summery>
        ///  Return an answer of true to the isDestinationInfo() query.
        /// </summery>
        ///
        public override bool IsDestinationInfo
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
        if(IsAddOperation)
        {
            return visitor.ProcessAddDestination(this);
        }
        else if(IsRemoveOperation)
        {
            return visitor.ProcessRemoveDestination(this);
        }
        throw new IOException("Unknown operation type: " + OperationType);
        }

    };
}

