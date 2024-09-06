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
using Apache.NMS.ActiveMQ.Util;

namespace Apache.NMS.ActiveMQ.Transport.Mock
{
	/// <summary>
	/// Factory class to create the MockTransport when given on a URI as mock://XXX
	/// </summary>
    [ActiveMQTransportFactory("mock")]
	public class MockTransportFactory : ITransportFactory
	{
		public MockTransportFactory()
		{
		}

		#region Properties

		private bool useLogging = false;
		public bool UseLogging
		{
			get { return useLogging; }
			set { useLogging = value; }
		}

		private bool failOnReceiveMessage = false;
		public bool FailOnReceiveMessage
		{
			get { return failOnReceiveMessage; }
			set { failOnReceiveMessage = value; }
		}

		private int numReceivedMessagesBeforeFail = 0;
		public int NumReceivedMessagesBeforeFail
		{
			get { return numReceivedMessagesBeforeFail; }
			set { numReceivedMessagesBeforeFail = value; }
		}

		private bool failOnSendMessage = false;
		public bool FailOnSendMessage
		{
			get { return failOnSendMessage; }
			set { this.failOnSendMessage = value; }
		}

		private int numSentMessagesBeforeFail = 0;
		public int NumSentMessagesBeforeFail
		{
			get { return numSentMessagesBeforeFail; }
			set { numSentMessagesBeforeFail = value; }
		}

		private bool failOnCreate = false;
		public bool FailOnCreate
		{
			get { return failOnCreate; }
			set { this.failOnCreate = value; }
		}

        private string name = null;
        public string Name
        {
            get { return this.name; }
            set { this.name = value; }
        }

        private int numMessagesToRespondTo = -1;
        public int NumMessagesToRespondTo
        {
            get { return numMessagesToRespondTo; }
            set { numMessagesToRespondTo = value; }
        }

        private bool respondToMessages = true;
        public bool RespondToMessages
        {
            get { return respondToMessages; }
            set { respondToMessages = value; }
        }

		#endregion

		public ITransport CreateTransport(Uri location)
		{
			ITransport transport = CompositeConnect(location);

			transport = new MutexTransport(transport);
			transport = new ResponseCorrelator(transport);

			return transport;
		}

		public ITransport CompositeConnect(Uri location)
		{
			Tracer.Debug("MockTransportFactory: Create new Transport with options: " + location.Query);

			// Extract query parameters from broker Uri
			StringDictionary map = URISupport.ParseQuery(location.Query);

			// Set transport. properties on this (the factory)
			URISupport.SetProperties(this, map, "transport.");

			if(this.FailOnCreate == true)
			{
				throw new IOException("Failed to Create new MockTransport.");
			}

			// Create the Mock Transport
			MockTransport transport = new MockTransport(location);

			transport.FailOnReceiveMessage = this.FailOnReceiveMessage;
			transport.NumReceivedMessagesBeforeFail = this.NumReceivedMessagesBeforeFail;
			transport.FailOnSendMessage = this.FailOnSendMessage;
			transport.NumSentMessagesBeforeFail = this.NumSentMessagesBeforeFail;
            transport.Name = this.Name;
            transport.RespondToMessages = this.respondToMessages;
            transport.NumMessagesToRespondTo = this.numMessagesToRespondTo;

			return transport;
		}
	}
}
