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
using System.Collections.Generic;
using Apache.NMS.ActiveMQ.Util;

namespace Apache.NMS.ActiveMQ.Transport.Discovery
{
    public class DiscoveryAgentFactory
    {
        private static readonly FactoryFinder<ActiveMQDiscoveryAgentFactoryAttribute, ITransportFactory> FACTORY_FINDER =
            new FactoryFinder<ActiveMQDiscoveryAgentFactoryAttribute, ITransportFactory>();
        
        private readonly static object AGENT_FACTORY_TYPES_LOCK = new object();
        private readonly static Dictionary<String, Type> AGENT_FACTORY_TYPES = new Dictionary<String, Type>();

        public void RegisterAgentFactory(string scheme, Type factoryType)
        {
            lock (AGENT_FACTORY_TYPES_LOCK)
            {
                AGENT_FACTORY_TYPES[scheme] = factoryType;
            }
        }

        public static IDiscoveryAgent CreateAgent(Uri location)
        {
            IDiscoveryAgentFactory tf = DiscoveryAgentFactory.CreateAgentFactory(location);
            return tf.CreateAgent(location);
        }

        /// <summary>
        /// Create a DiscoveryAgent Factory for the scheme.  If we do not support the agent protocol,
        /// an NMSConnectionException will be thrown.
        /// </summary>
        /// <param name="location"></param>
        /// <returns></returns>
        private static IDiscoveryAgentFactory CreateAgentFactory(Uri location)
        {
            string scheme = location.Scheme;

            if(string.IsNullOrEmpty(scheme))
            {
                throw new NMSConnectionException(String.Format("Discovery Agent scheme invalid: [{0}]", location.ToString()));
            }

            IDiscoveryAgentFactory factory = null;

            try
            {
                factory = NewInstance(scheme.ToLower());
            }
            catch(NMSConnectionException)
            {
                throw;
            }
            catch(Exception e)
            {
                throw new NMSConnectionException("Error creating discovery agent.", e);
            }

            if(null == factory)
            {
                throw new NMSConnectionException("Unable to create a discovery agent.");
            }

            return factory;
        }

        private static IDiscoveryAgentFactory NewInstance(string scheme)
        {
            try
            {
                Type factoryType = FindAgentFactory(scheme);

                if(factoryType == null)
                {
                    throw new Exception("NewInstance failed to find a match for id = " + scheme);
                }

                return (IDiscoveryAgentFactory) Activator.CreateInstance(factoryType);
            }
            catch(Exception ex)
            {
                Tracer.WarnFormat("NewInstance failed to create an IDiscoveryAgentFactory with error: {0}", ex.Message);
                throw;
            }
        }

        private static Type FindAgentFactory(string scheme)
        {
            lock (AGENT_FACTORY_TYPES_LOCK)
            {           
                if(AGENT_FACTORY_TYPES.ContainsKey(scheme))
                {
                    return AGENT_FACTORY_TYPES[scheme];
                }
            }
            
            try
            {
                Type factoryType = FACTORY_FINDER.FindFactoryType(scheme);
                
                lock (AGENT_FACTORY_TYPES_LOCK)
                {           
                    AGENT_FACTORY_TYPES[scheme] = factoryType;
                }
                return factoryType;
            }
            catch
            {
                throw new NMSConnectionException("Failed to find Factory for Discovery Agent type: " + scheme);
            }
        }
    }
}

