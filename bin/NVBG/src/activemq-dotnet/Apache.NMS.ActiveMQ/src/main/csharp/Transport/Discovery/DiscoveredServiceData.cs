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
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.Transport.Discovery
{
    /// <summary>
    /// Discovered service data event object.  Used to contain information on the
    /// services that an agent discovers and track heartbeat and other service
    /// events used to determine if a service has failed or timed out due to a
    /// lack of recent reporting.
    /// </summary>
    public class DiscoveredServiceData : DiscoveryEvent
    {
        private DateTime recoveryTime = DateTime.MinValue;
        private int failureCount;
        private bool failed;
        private DateTime lastHeartBeat;

        private readonly object syncRoot = new object();

        public DiscoveredServiceData(string brokerName, string serviceName) : base()
        {
            this.BrokerName = brokerName;
            this.ServiceName = serviceName;
            this.lastHeartBeat = DateTime.Now;
        }

        public DiscoveredServiceData(string serviceName) : base()
        {
            this.ServiceName = serviceName;
            this.lastHeartBeat = DateTime.Now;
        }

        internal object SyncRoot
        {
            get { return this.syncRoot; }
        }

        internal bool Failed
        {
            get { return this.failed; }
            set { this.failed = value; }
        }

        internal int FailureCount
        {
            get { return this.failureCount; }
            set { this.failureCount = value; }
        }

        internal DateTime LastHeartBeat
        {
            get { return this.lastHeartBeat; }
            set { this.lastHeartBeat = value; }
        }

        internal DateTime RecoveryTime
        {
            get { return this.recoveryTime; }
            set { this.recoveryTime = value; }
        }
    }
}

