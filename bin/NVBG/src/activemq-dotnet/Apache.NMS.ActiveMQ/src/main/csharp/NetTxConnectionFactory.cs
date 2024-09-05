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
using System.Collections.Specialized;
using Apache.NMS;
using Apache.NMS.Util;
using Apache.NMS.ActiveMQ.Transport;

namespace Apache.NMS.ActiveMQ
{
    public sealed class NetTxConnectionFactory : ConnectionFactory, INetTxConnectionFactory
    {
        private NetTxRecoveryPolicy recoveryPolicy = new NetTxRecoveryPolicy();
        private Guid configuredResourceManagerId = Guid.Empty;

        public NetTxConnectionFactory() : base(GetDefaultBrokerUrl())
        {
        }

        public NetTxConnectionFactory(string brokerUri) : base(brokerUri, null)
        {
        }

        public NetTxConnectionFactory(string brokerUri, string clientID)
            : base(brokerUri, clientID)
        {
        }

        public NetTxConnectionFactory(Uri brokerUri)
            : base(brokerUri, null)
        {
        }

        public NetTxConnectionFactory(Uri brokerUri, string clientID)
            : base(brokerUri, clientID)
        {
        }

        public String ConfiguredResourceManagerId
        {
            get { return this.configuredResourceManagerId.ToString(); }
            set { this.configuredResourceManagerId = new Guid(value); }
        }

        public INetTxConnection CreateNetTxConnection()
        {
            return (INetTxConnection) base.CreateActiveMQConnection();
        }

        public INetTxConnection CreateNetTxConnection(string userName, string password)
        {
            return (INetTxConnection) base.CreateActiveMQConnection(userName, password);
        }

        protected override Connection CreateActiveMQConnection(ITransport transport)
        {
            NetTxConnection connection = new NetTxConnection(this.BrokerUri, transport, this.ClientIdGenerator);

            connection.RecoveryPolicy = this.recoveryPolicy.Clone() as NetTxRecoveryPolicy;
            connection.ConfiguredResourceManagerId = this.configuredResourceManagerId;

            return connection;
        }

        public NetTxRecoveryPolicy RecoveryPolicy
        {
            get { return this.recoveryPolicy; }
            set { this.recoveryPolicy = value; }
        }
    }
}

