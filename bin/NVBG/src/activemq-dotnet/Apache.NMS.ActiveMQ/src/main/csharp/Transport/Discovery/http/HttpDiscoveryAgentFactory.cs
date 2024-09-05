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
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Transport.Discovery.Http
{
    [ActiveMQDiscoveryAgentFactory("http")]
    public class HttpDiscoveryAgentFactory : IDiscoveryAgentFactory
	{
        public IDiscoveryAgent CreateAgent(Uri uri)
        {
            Tracer.DebugFormat("Creating DiscoveryAgent:[{0}]", uri);

            try
            {
                HttpDiscoveryAgent agent = new HttpDiscoveryAgent();
                agent.DiscoveryURI = uri;

                // allow Agent's params to be set via query arguments  
                // (e.g., http://localhost:8080?group=default

                StringDictionary parameters = URISupport.ParseParameters(uri);
                URISupport.SetProperties(agent, parameters);

                return agent;
            }
            catch(Exception e)
            {
                throw new IOException("Could not create HTTP discovery agent", e);
            }
        }
    }
}

