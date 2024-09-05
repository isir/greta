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
using System.Text;
using System.Collections.Generic;
using System.Collections.Specialized;
using Apache.NMS.Util;
using Apache.NMS.ActiveMQ.Commands;
using Apache.NMS.ActiveMQ.Util;

namespace Apache.NMS.ActiveMQ.Transport.Discovery
{
	public class DiscoveryTransport : TransportFilter
	{
        public const string DISCOVERED_OPTION_PREFIX = "discovered.";

        private readonly new ICompositeTransport next;
        private readonly object syncRoot = new object();
        private readonly Dictionary<String, Uri> serviceURIs = new Dictionary<String, Uri>();

        private IDiscoveryAgent discoveryAgent;
        public IDiscoveryAgent DiscoveryAgent
        {
            get { return this.discoveryAgent; }
            set { this.discoveryAgent = value; }
        }

        private StringDictionary properties;
        public StringDictionary Properties
        {
            get { return this.properties; }
            set { this.properties = value; }
        }

		public DiscoveryTransport(ICompositeTransport next) : base(next)
		{
            this.next = next;

            // Intercept the interrupted and resumed events so we can disable our
            // agent if its supports suspend / resume semantics.
            this.next.Interrupted = TransportInterrupted;
            this.next.Resumed = TransportResumed;
		}

        public override void Start()
        {
            if (discoveryAgent == null) 
            {
                throw new InvalidOperationException("discoveryAgent not configured");
            }

            // lets pass into the agent the broker name and connection details
            discoveryAgent.ServiceAdd = OnServiceAdded;
            discoveryAgent.ServiceRemove = OnServiceRemoved;
            discoveryAgent.Start();
            this.next.Start();
        }

        public override void Stop()
        {
            ServiceStopper ss = new ServiceStopper();

            ss.Stop(discoveryAgent);
            ss.Stop(next);
            ss.ThrowFirstException();
        }

        private void OnServiceAdded(DiscoveryEvent addEvent)
        {
            String url = addEvent.ServiceName;
            if (url != null) 
            {
                try 
                {
                    Uri uri = new Uri(url);
                    Tracer.InfoFormat("Adding new broker connection URL: {0}", uri);
                    uri = ApplyParameters(uri, properties, DISCOVERED_OPTION_PREFIX);
                  
                    lock (syncRoot)
                    {
                        serviceURIs[addEvent.ServiceName] = uri;
                    }
                    next.Add(false, new Uri[] {uri});
                } 
                catch (Exception e) 
                {
                    Tracer.WarnFormat("Could not connect to remote URI: {0} due to bad URI syntax: {1}", url, e.Message);
                }
            }
        }

        private void OnServiceRemoved(DiscoveryEvent removeEvent)
        {
            Uri toRemove = null;
            lock (syncRoot)
            {
                serviceURIs.TryGetValue(removeEvent.ServiceName, out toRemove);
            }
            if (toRemove != null) 
            {
                next.Remove(false, new Uri[] {toRemove});
            }
        }

        private void TransportResumed(ITransport sender) 
        {
            ISuspendable service = this.discoveryAgent as ISuspendable;
            if (service != null) 
            {
                try 
                {
                    service.Suspend();
                }
                catch (Exception e) 
                {
                    Tracer.WarnFormat("Caught error while suspending service: {0} - {1}", service, e.Message);
                }
            }

            if (this.Resumed != null)
            {
                this.Resumed(sender);
            }
        }

        private void TransportInterrupted(ITransport sender)
        {
            ISuspendable service = this.discoveryAgent as ISuspendable;
            if (service != null) 
            {
                try 
                {
                    service.Resume();
                }
                catch (Exception e) 
                {
                    Tracer.WarnFormat("Caught error while resuming service: {0} - {1}", service, e.Message);
                }
            }

            if (this.Interrupted != null)
            {
                this.Interrupted(sender);
            }
        }

        /// <summary>
        /// Given a Key / Value mapping create and append a URI query value that represents the mapped 
        /// entries, return the newly updated URI that contains the value of the given URI and the 
        /// appended query value.  Each entry in the query string is prefixed by the supplied 
        /// optionPrefix string.
        /// </summary>
        private static Uri ApplyParameters(Uri uri, StringDictionary queryParameters, String optionPrefix)
        {
            if (queryParameters != null && queryParameters.Count != 0) 
            {
                StringBuilder newQuery = uri.Query != null ? new StringBuilder(uri.Query) : new StringBuilder();

                foreach(KeyValuePair<string, string> entry in queryParameters)
                {
                    if (entry.Key.StartsWith(optionPrefix)) 
                    {
                        if (newQuery.Length !=0) 
                        {
                            newQuery.Append('&');
                        }
                        string key = entry.Key.Substring(optionPrefix.Length);
                        newQuery.Append(key).Append('=').Append(entry.Value);
                    }
                }
                uri = URISupport.CreateUriWithQuery(uri, newQuery.ToString());
            }
            return uri;
        }
	}
}

