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
using System.Net;
using System.Threading;
using System.IO;
using System.Collections.Generic;
using Apache.NMS.Util;
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.Transport.Discovery.Http
{
	public class HttpDiscoveryAgent : AbstractDiscoveryAgent, ISuspendable
	{
        private enum UpdateState
        {
            SUSPENDED,
            RESUMING,
            RESUMED
        }

        public const string DEFAULT_DISCOVERY_URI_STRING = "http://localhost:8080/discovery-registry";
        public const string DEFAULT_GROUP = "default";

        private readonly object updateMutex = new object();
        private UpdateState state = UpdateState.RESUMED;
        private const int DEFAULT_UPDATE_INTERVAL = 10 * 1000;
        private long keepAliveInterval = DEFAULT_UPDATE_INTERVAL;

		public HttpDiscoveryAgent()
		{
            this.Group = DEFAULT_GROUP;
		}

        #region Property setters and getters

        public override long KeepAliveInterval
        {
            get { return this.keepAliveInterval; }
            set { this.keepAliveInterval = value; }
        }

        #endregion

        public override String ToString()
        {
            return "HttpDiscoveryAgent-" + (SelfService != null ? "advertise:" + SelfService : "");
        }

        protected override void DoStartAgent()
        {
            if (DiscoveryURI == null || DiscoveryURI.Host.Equals("default")) 
            {
                DiscoveryURI = new Uri(DEFAULT_DISCOVERY_URI_STRING + "/" + Group);
            }

            if (Tracer.IsDebugEnabled) 
            {
                Tracer.DebugFormat("http agent started with discoveryURI = {0}", DiscoveryURI);                              
            }
        }

        protected override void DoStopAgent()
        {
            // Ensure the worker thread exits its wait so it can detect the stopped event.
            Resume();
        }

        public void Suspend()
        {
            Monitor.Enter(updateMutex);
            try 
            {
                this.state = UpdateState.SUSPENDED;
            }
            finally
            {
                Monitor.Exit(updateMutex);
            }
        }

        public void Resume()
        {
            Monitor.Enter(updateMutex);
            try 
            {
                this.state = UpdateState.RESUMING;
                Monitor.Pulse(updateMutex);
            }
            finally
            {
                Monitor.Exit(updateMutex);
            }
        }

        protected override void DoDiscovery()
        {
            DoUpdate();
            Monitor.Enter(updateMutex);
            try
            {
                do 
                {
                    if (state == UpdateState.RESUMING) 
                    {
                        state = UpdateState.RESUMED;
                    }
                    else 
                    {
                        Monitor.Wait(updateMutex, TimeSpan.FromMilliseconds(KeepAliveInterval));
                    }
                }
                while (state == UpdateState.SUSPENDED && started.Value);
            }
            finally
            {
                Monitor.Exit(updateMutex);
            }
        }

        private void DoUpdate()
        {
            List<string> activeServices = DoLookup(KeepAliveInterval * 3);
            // If there is error talking the the central server, then activeServices == null
            if (activeServices != null) 
            {
                lock (discoveredServicesLock) 
                {
                    foreach(String service in activeServices)
                    {
                        Tracer.DebugFormat("Http discovery found live service: {0}", service);
                        ProcessLiveService("", service);
                    }
                }
            }
        }

        private List<string> DoLookup(long freshness) 
        {
            String url = DiscoveryURI + "?freshness=" + freshness;
            try 
            {
                WebClient client = new WebClient();
                string response = client.DownloadString(url);
                if (response != null)
                {
                    Tracer.DebugFormat("GET to {0} got a {1}", url, response);
                    List<string> rc = new List<string>();

                    StringReader reader = new StringReader(response);
                    while (true)
                    {
                        string line = reader.ReadLine();
                        if (line == null)
                        {
                            break;
                        }

                        line = line.Trim();
                        if (line.Length != 0 && !rc.Contains(line))
                        {
                            rc.Add(line);
                        }
                    }
                    return rc;
                }

                Tracer.DebugFormat("GET to {0} failed to retrieve any services.", url);
                return null;
            } 
            catch (Exception e)
            {
                Tracer.WarnFormat("GET to {0} failed with: {1}", url, e.Message);
                return null;
            }
        }

        protected override void DoAdvertizeSelf()
        {
            // TODO - Don't need this yet unless we want to do some testing.
        }
	}
}

