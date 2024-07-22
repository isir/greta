/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System;
using System.Reflection;
using System.Collections.Generic;
using Apache.NMS.ActiveMQ.Util;

namespace Apache.NMS.ActiveMQ.Transport
{
    public class TransportFactory
    {
        public static event ExceptionListener OnException;

        private static readonly FactoryFinder<ActiveMQTransportFactoryAttribute, ITransportFactory> FACTORY_FINDER =
            new FactoryFinder<ActiveMQTransportFactoryAttribute, ITransportFactory>();

        private readonly static object TRANSPORT_FACTORY_TYPES_LOCK = new object();
        private readonly static IDictionary<String, Type> TRANSPORT_FACTORY_TYPES = new Dictionary<String, Type>();

        public static void HandleException(Exception ex)
        {
            if(TransportFactory.OnException != null)
            {
                TransportFactory.OnException(ex);
            }
        }

        public void RegisterTransportFactory(string scheme, Type factoryType)
        {
            lock (TRANSPORT_FACTORY_TYPES_LOCK)
            {
                TRANSPORT_FACTORY_TYPES[scheme] = factoryType;
            }
        }

        /// <summary>
        /// Creates a normal transport.
        /// </summary>
        /// <param name="location"></param>
        /// <returns>the transport</returns>
        public static ITransport CreateTransport(Uri location)
        {
            ITransportFactory tf = TransportFactory.CreateTransportFactory(location);
            return tf.CreateTransport(location);
        }

        public static ITransport CompositeConnect(Uri location)
        {
            ITransportFactory tf = TransportFactory.CreateTransportFactory(location);
            return tf.CompositeConnect(location);
        }

        /// <summary>
        /// Create a transport factory for the scheme.  If we do not support the transport protocol,
        /// an NMSConnectionException will be thrown.
        /// </summary>
        /// <param name="location"></param>
        /// <returns></returns>
        private static ITransportFactory CreateTransportFactory(Uri location)
        {
            string scheme = location.Scheme;

            if(string.IsNullOrEmpty(scheme))
            {
                throw new NMSConnectionException(String.Format("Transport scheme invalid: [{0}]", location.ToString()));
            }

            ITransportFactory factory = null;

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
                throw new NMSConnectionException("Error creating transport.", e);
            }

            if(null == factory)
            {
                throw new NMSConnectionException("Unable to create a transport.");
            }

            return factory;
        }

        private static ITransportFactory NewInstance(string scheme)
        {
            try
            {
                Type factoryType = FindTransportFactory(scheme);

                if(factoryType == null)
                {
                    throw new Exception("NewInstance failed to find a match for id = " + scheme);
                }

                return (ITransportFactory) Activator.CreateInstance(factoryType);
            }
            catch(Exception ex)
            {
                Tracer.WarnFormat("NewInstance failed to create an ITransportFactory with error: {0}", ex.Message);
                throw;
            }
        }

        private static Type FindTransportFactory(string scheme)
        {
            lock (TRANSPORT_FACTORY_TYPES_LOCK)
            {
                if(TRANSPORT_FACTORY_TYPES.ContainsKey(scheme))
                {
                    return TRANSPORT_FACTORY_TYPES[scheme];
                }
            }

            try
            {
                Type factoryType = FACTORY_FINDER.FindFactoryType(scheme);

                lock (TRANSPORT_FACTORY_TYPES_LOCK)
                {
                    TRANSPORT_FACTORY_TYPES[scheme] = factoryType;
                }
                return factoryType;
            }
            catch
            {
                throw new NMSConnectionException("Failed to find Factory for Transport type: " + scheme);
            }
        }
    }
}
