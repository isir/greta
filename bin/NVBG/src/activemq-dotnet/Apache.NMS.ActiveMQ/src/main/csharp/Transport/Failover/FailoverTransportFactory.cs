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
using System.Collections.Specialized;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Transport.Failover
{
    [ActiveMQTransportFactory("failover")]
	public class FailoverTransportFactory : ITransportFactory
	{
		private ITransport doConnect(Uri location)
		{
			ITransport transport = CreateTransport(URISupport.ParseComposite(location));
			transport = new MutexTransport(transport);
			transport = new ResponseCorrelator(transport);
			return transport;
		}

		public ITransport CompositeConnect(Uri location)
		{
			return CreateTransport(URISupport.ParseComposite(location));
		}

		public ITransport CreateTransport(Uri location)
		{
			return doConnect(location);
		}

		/// <summary>
        /// Virtual transport create method which can be overriden by subclasses to provide
        /// an alternate FailoverTransport implementation.  All transport creation methods in
        /// this factory calls through this method to create the ITransport instance so this
        /// is the only method that needs to be overriden.  
		/// </summary>
		/// <param name="compositData"></param>
		/// <returns></returns>
		public virtual ITransport CreateTransport(URISupport.CompositeData compositData)
		{
			StringDictionary options = compositData.Parameters;
			FailoverTransport transport = CreateTransport(options);
			transport.Add(false, compositData.Components);
			return transport;
		}

		protected FailoverTransport CreateTransport(StringDictionary parameters)
		{
			FailoverTransport transport = new FailoverTransport();
			URISupport.SetProperties(transport, parameters, "transport.");
			return transport;
		}
	}
}
