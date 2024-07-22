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

using System;

namespace Apache.NMS.ActiveMQ.Commands
{
    /*
     *
     *  Command code for OpenWire format for XATransactionId
     *
     *  NOTE!: This file is auto generated - do not modify!
     *         if you need to make a change, please see the Java Classes
     *         in the nms-activemq-openwire-generator module
     *
     */
    public class XATransactionId : TransactionId, Xid
    {
        public const byte ID_XATRANSACTIONID = 112;

        int formatId;
        byte[] globalTransactionId;
        byte[] branchQualifier;

        ///
        /// <summery>
        ///  Get the unique identifier that this object and its own
        ///  Marshaler share.
        /// </summery>
        ///
        public override byte GetDataStructureType()
        {
            return ID_XATRANSACTIONID;
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
                "FormatId = " + FormatId + ", " +
                "GlobalTransactionId = " + BitConverter.ToString(GlobalTransactionId).Replace("-", string.Empty) + ", " +
                "BranchQualifier = " + System.Text.ASCIIEncoding.ASCII.GetString(BranchQualifier) + " ]";
        }

        public int FormatId
        {
            get { return formatId; }
            set { this.formatId = value; }
        }

        public byte[] GlobalTransactionId
        {
            get { return globalTransactionId; }
            set { this.globalTransactionId = value; }
        }

        public byte[] BranchQualifier
        {
            get { return branchQualifier; }
            set { this.branchQualifier = value; }
        }

        public override int GetHashCode()
        {
            int answer = 0;

            answer = (answer * 37) + HashCode(FormatId);
            answer = (answer * 37) + HashCode(GlobalTransactionId);
            answer = (answer * 37) + HashCode(BranchQualifier);

            return answer;
        }

        public override bool Equals(object that)
        {
            if(that is XATransactionId)
            {
                return Equals((XATransactionId) that);
            }

            return false;
        }

        public virtual bool Equals(XATransactionId that)
        {
            if(!Equals(this.FormatId, that.FormatId))
            {
                return false;
            }
            if(!ArrayEquals(this.GlobalTransactionId, that.GlobalTransactionId))
            {
                return false;
            }
            if(!ArrayEquals(this.BranchQualifier, that.BranchQualifier))
            {
                return false;
            }

            return true;
        }
    };
}

