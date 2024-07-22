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
using Apache.NMS.ActiveMQ.Transport.Failover;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Transport.Discovery
{
    [ActiveMQTransportFactory("discovery")]
	public class DiscoveryTransportFactory : FailoverTransportFactory
	{
        public override ITransport CreateTransport(URISupport.CompositeData compositData)
        {
            StringDictionary options = compositData.Parameters;
            FailoverTransport failoverTransport = CreateTransport(options);
            return CreateTransport(failoverTransport, compositData, options);
        }

        /// <summary>
        /// Factory method for creating a DiscoveryTransport.  The Discovery Transport wraps the
        /// given ICompositeTransport and will add and remove Transport URIs as they are discovered.
        /// </summary>
        public static DiscoveryTransport CreateTransport(ICompositeTransport compositeTransport, URISupport.CompositeData compositeData, StringDictionary options)
        {
            DiscoveryTransport transport = new DiscoveryTransport(compositeTransport);

            URISupport.SetProperties(transport, options, "transport.");
            transport.Properties = options;
            
            Uri discoveryAgentURI = compositeData.Components[0];
            IDiscoveryAgent discoveryAgent = DiscoveryAgentFactory.CreateAgent(discoveryAgentURI);
            transport.DiscoveryAgent = discoveryAgent;

            return transport;
        }
	}
}
