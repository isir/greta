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


namespace Apache.NMS.ActiveMQ.Commands
{
    /*
     *
     *  Command code for OpenWire format for JournalTransaction
     *
     *  NOTE!: This file is auto generated - do not modify!
     *         if you need to make a change, please see the Java Classes
     *         in the nms-activemq-openwire-generator module
     *
     */
    public class JournalTransaction : BaseDataStructure
    {
        public const byte ID_JOURNALTRANSACTION = 54;

        TransactionId transactionId;
        byte type;
        bool wasPrepared;

        ///
        /// <summery>
        ///  Get the unique identifier that this object and its own
        ///  Marshaler share.
        /// </summery>
        ///
        public override byte GetDataStructureType()
        {
            return ID_JOURNALTRANSACTION;
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
                "TransactionId = " + TransactionId + ", " + 
                "Type = " + Type + ", " + 
                "WasPrepared = " + WasPrepared + " ]";
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

        public bool WasPrepared
        {
            get { return wasPrepared; }
            set { this.wasPrepared = value; }
        }

    };
}

