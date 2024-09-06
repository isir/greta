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
     *  Command code for OpenWire format for TransactionInfo
     *
     *  NOTE!: This file is auto generated - do not modify!
     *         if you need to make a change, please see the Java Classes
     *         in the nms-activemq-openwire-generator module
     *
     */
    public class TransactionInfo : BaseCommand
    {
        public const byte ID_TRANSACTIONINFO = 7;

        public const byte BEGIN = 0;
        public const byte PREPARE = 1;
        public const byte COMMIT_ONE_PHASE = 2;
        public const byte COMMIT_TWO_PHASE = 3;
        public const byte ROLLBACK = 4;
        public const byte RECOVER = 5;
        public const byte FORGET = 6;
        public const byte END = 7;

        ConnectionId connectionId;
        TransactionId transactionId;
        byte type;

        ///
        /// <summery>
        ///  Get the unique identifier that this object and its own
        ///  Marshaler share.
        /// </summery>
        ///
        public override byte GetDataStructureType()
        {
            return ID_TRANSACTIONINFO;
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
                "TransactionId = " + TransactionId + ", " + 
                "Type = " + Type + " ]";
        }

        public ConnectionId ConnectionId
        {
            get { return connectionId; }
            set { this.connectionId = value; }
        }

        public TransactionId TransactionId
        {
            get { return transactionId; }
            set { this.transactionId = value; }
        }

        public byte Type
        {
            get { return type; }
            set { this.type = value; }
        }

        ///
        /// <summery>
        ///  Return an answer of true to the isTransactionInfo() query.
        /// </summery>
        ///
        public override bool IsTransactionInfo
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
            switch(type)
            {
                case TransactionInfo.BEGIN:
                    return visitor.ProcessBeginTransaction(this);
                case TransactionInfo.END:
                    return visitor.ProcessEndTransaction(this);
                case TransactionInfo.PREPARE:
                    return visitor.ProcessPrepareTransaction(this);
                case TransactionInfo.COMMIT_ONE_PHASE:
                    return visitor.ProcessCommitTransactionOnePhase(this);
                case TransactionInfo.COMMIT_TWO_PHASE:
                    return visitor.ProcessCommitTransactionTwoPhase(this);
                case TransactionInfo.ROLLBACK:
                    return visitor.ProcessRollbackTransaction(this);
                case TransactionInfo.RECOVER:
                    return visitor.ProcessRecoverTransactions(this);
                case TransactionInfo.FORGET:
                    return visitor.ProcessForgetTransaction(this);
                default:
                    throw new IOException("Transaction info type unknown: " + type);
            }
        }

    };
}

