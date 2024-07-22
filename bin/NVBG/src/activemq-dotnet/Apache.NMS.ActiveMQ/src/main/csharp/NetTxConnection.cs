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

using System;
using System.Text.RegularExpressions;
using System.Transactions;
using Apache.NMS.ActiveMQ.Transport;
using Apache.NMS.ActiveMQ.Util;

namespace Apache.NMS.ActiveMQ
{
    /// <summary>
    /// Extends the basic Connection class to provide a transacted Connection
    /// instance that operates within the bounds of a .NET Scoped Transaction.
    ///
    /// The default Session creation methods of Connection are overriden here
    /// to always return a TX capable session instance.
    /// </summary>
    public sealed class NetTxConnection : Connection, INetTxConnection
    {
        private NetTxRecoveryPolicy recoveryPolicy = new NetTxRecoveryPolicy();
        private Guid configuredResourceManagerId = Guid.Empty;

        public NetTxConnection(Uri connectionUri, ITransport transport, IdGenerator clientIdGenerator)
            : base(connectionUri, transport, clientIdGenerator)
        {
        }

        public INetTxSession CreateNetTxSession()
        {
            return (INetTxSession) CreateSession(AcknowledgementMode.Transactional);
        }

        public INetTxSession CreateNetTxSession(Transaction tx)
        {
            NetTxSession session = (NetTxSession)CreateSession(AcknowledgementMode.Transactional);
            session.Enlist(tx);
            return session;
        }

        public INetTxSession CreateNetTxSession(Transaction tx, bool enlistNativeMsDtcResource)
        {
            NetTxSession session = (NetTxSession)CreateSession(AcknowledgementMode.Transactional);
            session.Enlist(tx);
            session.EnlistsMsDtcNativeResource = enlistNativeMsDtcResource;
            return session;
        }

        public INetTxSession CreateNetTxSession(bool enlistNativeMsDtcResource)
        {
            NetTxSession session = (NetTxSession)CreateSession(AcknowledgementMode.Transactional);
            session.EnlistsMsDtcNativeResource = enlistNativeMsDtcResource;
            return session;
        }

        protected override Session CreateActiveMQSession(AcknowledgementMode ackMode)
        {
            CheckConnected();
            return new NetTxSession(this, NextSessionId);
        }

        public NetTxRecoveryPolicy RecoveryPolicy
        {
            get { return this.recoveryPolicy; }
            set { this.recoveryPolicy = value; }
        }

        public Guid ConfiguredResourceManagerId
        {
            get { return this.configuredResourceManagerId; }
            set { this.configuredResourceManagerId = value; }
        }

        internal Guid ResourceManagerGuid
        {
            get
            {
                return ConfiguredResourceManagerId != Guid.Empty ? 
                    ConfiguredResourceManagerId : GuidFromId(ResourceManagerId);
            }
        }

        private static Guid GuidFromId(string id)
        {
            MatchCollection matches = Regex.Matches(id, @"(\d+)-(\d+)-(\d+):(\d+)$");
            if(0 == matches.Count)
            {
                throw new FormatException(string.Format("Unable to extract a GUID from string '{0}'", id));
            }
 
            GroupCollection groups = matches[0].Groups;
 
            // We don't use the hostname here, just the remaining bits.
            int a = Int32.Parse(groups[1].Value);
            short b = Int16.Parse(groups[3].Value);
            short c = Int16.Parse(groups[4].Value);
            byte[] d = BitConverter.GetBytes(Int64.Parse(groups[2].Value));
 
            return new Guid(a, b, c, d);
        }
    }
}

