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
using Apache.NMS.ActiveMQ.Util;
using Apache.NMS.ActiveMQ.Transport;
using Apache.NMS.Util;
using Apache.NMS.Policies;

namespace Apache.NMS.ActiveMQ
{
	/// <summary>
	/// Represents a connection with a message broker
	/// </summary>
	public class ConnectionFactory : IConnectionFactory
	{
		public const string DEFAULT_BROKER_URL = "failover:tcp://localhost:61616";
		public const string ENV_BROKER_URL = "ACTIVEMQ_BROKER_URL";

		private static event ExceptionListener onException;
		private Uri brokerUri;
		private string connectionUserName;
		private string connectionPassword;
		private string clientId;
		private string clientIdPrefix;
		private IdGenerator clientIdGenerator;

		private bool useCompression;
		private bool copyMessageOnSend = true;
		private bool dispatchAsync = true;
		private bool asyncSend;
		private bool asyncClose;
		private bool alwaysSyncSend;
		private bool sendAcksAsync = true;
		private int producerWindowSize = 0;
		private AcknowledgementMode acknowledgementMode = AcknowledgementMode.AutoAcknowledge;
		private TimeSpan requestTimeout = NMSConstants.defaultRequestTimeout;
		private bool messagePrioritySupported = false;
        private bool watchTopicAdvisories = true;
    	private bool optimizeAcknowledge;
    	private long optimizeAcknowledgeTimeOut = 300;
    	private long optimizedAckScheduledAckInterval = 0;
	    private bool useRetroactiveConsumer;
	    private bool exclusiveConsumer;
	    private long consumerFailoverRedeliveryWaitPeriod = 0;
	    private bool checkForDuplicates = true;
	    private bool transactedIndividualAck = false;
		private bool nonBlockingRedelivery = false;
		private int auditDepth = ActiveMQMessageAudit.DEFAULT_WINDOW_SIZE;
    	private int auditMaximumProducerNumber = ActiveMQMessageAudit.MAXIMUM_PRODUCER_COUNT;

		private IRedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
		private PrefetchPolicy prefetchPolicy = new PrefetchPolicy();
		private ICompressionPolicy compressionPolicy = new CompressionPolicy();

		static ConnectionFactory()
		{
			TransportFactory.OnException += ConnectionFactory.ExceptionHandler;
		}

		public static string GetDefaultBrokerUrl()
		{
#if (PocketPC||NETCF||NETCF_2_0)
			return DEFAULT_BROKER_URL;
#else
			return Environment.GetEnvironmentVariable(ENV_BROKER_URL) ?? DEFAULT_BROKER_URL;
#endif
		}

		public ConnectionFactory() : this(GetDefaultBrokerUrl())
		{
		}

		public ConnectionFactory(string brokerUri) : this(brokerUri, null)
		{
		}

		public ConnectionFactory(string brokerUri, string clientID)
			: this(URISupport.CreateCompatibleUri(brokerUri), clientID)
		{
		}

		public ConnectionFactory(Uri brokerUri) : this(brokerUri, null)
		{
		}

		public ConnectionFactory(Uri brokerUri, string clientID)
		{
			this.BrokerUri = brokerUri;
			this.ClientId = clientID;
		}

		public IConnection CreateConnection()
		{
			return CreateActiveMQConnection();
		}

		public IConnection CreateConnection(string userName, string password)
		{
            return CreateActiveMQConnection(userName, password);
		}

        protected virtual Connection CreateActiveMQConnection()
        {
            return CreateActiveMQConnection(connectionUserName, connectionPassword);
        }

        protected virtual Connection CreateActiveMQConnection(string userName, string password)
        {
            Connection connection = null;

            try
            {
                Tracer.InfoFormat("Connecting to: {0}", brokerUri.ToString());

                ITransport transport = TransportFactory.CreateTransport(brokerUri);

                connection = CreateActiveMQConnection(transport);

                ConfigureConnection(connection);

                connection.UserName = userName;
                connection.Password = password;

                if(this.clientId != null)
                {
                    connection.DefaultClientId = this.clientId;
                }

                return connection;
            }
            catch(NMSException)
            {
                try
                {
                    connection.Close();
                }
                catch
                {
                }

                throw;
            }
            catch(Exception e)
            {
                try
                {
                    connection.Close();
                }
                catch
                {
                }

                throw NMSExceptionSupport.Create("Could not connect to broker URL: " + this.brokerUri + ". Reason: " + e.Message, e);
            }
        }

        protected virtual Connection CreateActiveMQConnection(ITransport transport)
        {
            return new Connection(this.brokerUri, transport, this.ClientIdGenerator);
        }

		#region ConnectionFactory Properties

		/// <summary>
		/// Get/or set the broker Uri.
		/// </summary>
		public Uri BrokerUri
		{
			get { return brokerUri; }
			set
			{
				Tracer.Info("BrokerUri set = " + value.OriginalString);

                if(value.OriginalString.StartsWith("activemqnettx:"))
                {
                    brokerUri = new Uri(URISupport.StripPrefix(value.OriginalString, "activemqnettx:"));
                }
                else
                {
				    brokerUri = new Uri(URISupport.StripPrefix(value.OriginalString, "activemq:"));
                }

				if(!String.IsNullOrEmpty(brokerUri.Query) && !brokerUri.OriginalString.EndsWith(")"))
				{
					// Since the Uri class will return the end of a Query string found in a Composite
					// URI we must ensure that we trim that off before we proceed.
					string query = brokerUri.Query.Substring(brokerUri.Query.LastIndexOf(")") + 1);						
					
					StringDictionary properties = URISupport.ParseQuery(query);

					StringDictionary connection = URISupport.ExtractProperties(properties, "connection.");
					StringDictionary nms = URISupport.ExtractProperties(properties, "nms.");

			        IntrospectionSupport.SetProperties(this, connection, "connection.");
                    IntrospectionSupport.SetProperties(this, nms, "nms.");

					brokerUri = URISupport.CreateRemainingUri(brokerUri, properties);
				}
			}
		}

		public string UserName
		{
			get { return connectionUserName; }
			set { connectionUserName = value; }
		}

		public string Password
		{
			get { return connectionPassword; }
			set { connectionPassword = value; }
		}

		public string ClientId
		{
			get { return clientId; }
			set { clientId = value; }
		}

		public string ClientIdPrefix
		{
			get { return clientIdPrefix; }
			set { clientIdPrefix = value; }
		}

		public bool UseCompression
		{
			get { return this.useCompression; }
			set { this.useCompression = value; }
		}

		public bool CopyMessageOnSend
		{
			get { return copyMessageOnSend; }
			set { copyMessageOnSend = value; }
		}

		public bool AlwaysSyncSend
		{
			get { return alwaysSyncSend; }
			set { alwaysSyncSend = value; }
		}

		public bool AsyncClose
		{
			get { return asyncClose; }
			set { asyncClose = value; }
		}

		public bool SendAcksAsync
		{
			get { return sendAcksAsync; }
			set { sendAcksAsync = value; }
		}

		public bool AsyncSend
		{
			get { return asyncSend; }
			set { asyncSend = value; }
		}

		public bool DispatchAsync
		{
			get { return this.dispatchAsync; }
			set { this.dispatchAsync = value; }
		}

        public bool WatchTopicAdvisories
        {
            get { return this.watchTopicAdvisories; }
            set { this.watchTopicAdvisories = value; }
        }

		public bool MessagePrioritySupported
		{
			get { return this.messagePrioritySupported; }
			set { this.messagePrioritySupported = value; }
		}

		public int RequestTimeout
		{
			get { return (int)this.requestTimeout.TotalMilliseconds; }
			set { this.requestTimeout = TimeSpan.FromMilliseconds(value); }
		}

		public string AckMode
		{
			set { this.acknowledgementMode = NMSConvert.ToAcknowledgementMode(value); }
		}

		public AcknowledgementMode AcknowledgementMode
		{
			get { return acknowledgementMode; }
			set { this.acknowledgementMode = value; }
		}

		public int ProducerWindowSize
		{
			get { return producerWindowSize; }
			set { producerWindowSize = value; }
		}

		public PrefetchPolicy PrefetchPolicy
		{
			get { return this.prefetchPolicy; }
			set { this.prefetchPolicy = value; }
		}

		public IRedeliveryPolicy RedeliveryPolicy
		{
			get { return this.redeliveryPolicy; }
			set
			{
				if(value != null)
				{
					this.redeliveryPolicy = value;
				}
			}
		}

		public ICompressionPolicy CompressionPolicy
		{
			get { return this.compressionPolicy; }
			set
			{
				if(value != null)
				{
					this.compressionPolicy = value;
				}
			}
		}

		public IdGenerator ClientIdGenerator
		{
			set { this.clientIdGenerator = value; }
			get
			{
				lock(this)
				{
					if(this.clientIdGenerator == null)
					{
						if(this.clientIdPrefix != null)
						{
							this.clientIdGenerator = new IdGenerator(this.clientIdPrefix);
						}
						else
						{
							this.clientIdGenerator = new IdGenerator();
						}
					}

					return this.clientIdGenerator;
				}
			}
		}

		public event ExceptionListener OnException
		{
			add { onException += value; }
			remove
			{
				if(onException != null)
				{
					onException -= value;
				}
			}
		}

		private ConsumerTransformerDelegate consumerTransformer;
		/// <summary>
		/// A Delegate that is called each time a Message is dispatched to allow the client to do
		/// any necessary transformations on the received message before it is delivered.  The
		/// ConnectionFactory sets the provided delegate instance on each Connection instance that
		/// is created from this factory, each connection in turn passes the delegate along to each
		/// Session it creates which then passes that along to the Consumers it creates.
		/// </summary>
		public ConsumerTransformerDelegate ConsumerTransformer
		{
			get { return this.consumerTransformer; }
			set { this.consumerTransformer = value; }
		}

		private ProducerTransformerDelegate producerTransformer;
		/// <summary>
		/// A delegate that is called each time a Message is sent from this Producer which allows
		/// the application to perform any needed transformations on the Message before it is sent.
		/// The ConnectionFactory sets the provided delegate instance on each Connection instance that
		/// is created from this factory, each connection in turn passes the delegate along to each
		/// Session it creates which then passes that along to the Producers it creates.
		/// </summary>
		public ProducerTransformerDelegate ProducerTransformer
		{
			get { return this.producerTransformer; }
			set { this.producerTransformer = value; }
		}

    	public bool OptimizeAcknowledge 
		{
			get { return this.optimizeAcknowledge; }
			set { this.optimizeAcknowledge = value; }
		}

    	public long OptimizeAcknowledgeTimeOut
		{
			get { return this.optimizeAcknowledgeTimeOut; }
			set { this.optimizeAcknowledgeTimeOut = value; }
		}

		public long OptimizedAckScheduledAckInterval
		{
			get { return this.optimizedAckScheduledAckInterval; }
			set { this.optimizedAckScheduledAckInterval = value; }
		}

		public bool UseRetroactiveConsumer
		{
			get { return this.useRetroactiveConsumer; }
			set { this.useRetroactiveConsumer = value; }
		}

		public bool ExclusiveConsumer
		{
			get { return this.exclusiveConsumer; }
			set { this.exclusiveConsumer = value; }
		}

		public long ConsumerFailoverRedeliveryWaitPeriod
		{
			get { return this.consumerFailoverRedeliveryWaitPeriod; }
			set { this.consumerFailoverRedeliveryWaitPeriod = value; }
		}

		public bool CheckForDuplicates
		{
			get { return this.checkForDuplicates; }
			set { this.checkForDuplicates = value; }
		}

		public bool TransactedIndividualAck
		{
			get { return this.transactedIndividualAck; }
			set { this.transactedIndividualAck = value; }
		}

		public bool NonBlockingRedelivery
		{
			get { return this.nonBlockingRedelivery; }
			set { this.nonBlockingRedelivery = value; }
		}

		public int AuditDepth
		{
			get { return this.auditDepth; }
			set { this.auditDepth = value; }
		}

		public int AuditMaximumProducerNumber
		{
			get { return this.auditMaximumProducerNumber; }
			set { this.auditMaximumProducerNumber = value; }
		}

		#endregion

		protected virtual void ConfigureConnection(Connection connection)
		{
			connection.AsyncClose = this.AsyncClose;
			connection.AsyncSend = this.AsyncSend;
			connection.CopyMessageOnSend = this.CopyMessageOnSend;
			connection.AlwaysSyncSend = this.AlwaysSyncSend;
			connection.DispatchAsync = this.DispatchAsync;
			connection.SendAcksAsync = this.SendAcksAsync;
			connection.AcknowledgementMode = this.acknowledgementMode;
			connection.UseCompression = this.useCompression;
			connection.RequestTimeout = this.requestTimeout;
			connection.ProducerWindowSize = this.producerWindowSize;
			connection.MessagePrioritySupported = this.messagePrioritySupported;
			connection.RedeliveryPolicy = this.redeliveryPolicy.Clone() as IRedeliveryPolicy;
			connection.PrefetchPolicy = this.prefetchPolicy.Clone() as PrefetchPolicy;
			connection.CompressionPolicy = this.compressionPolicy.Clone() as ICompressionPolicy;
			connection.ConsumerTransformer = this.consumerTransformer;
			connection.ProducerTransformer = this.producerTransformer;
            connection.WatchTopicAdvisories = this.watchTopicAdvisories;
			connection.OptimizeAcknowledge = this.optimizeAcknowledge;
			connection.OptimizeAcknowledgeTimeOut = this.optimizeAcknowledgeTimeOut;
			connection.OptimizedAckScheduledAckInterval = this.optimizedAckScheduledAckInterval;
			connection.UseRetroactiveConsumer = this.useRetroactiveConsumer;
			connection.ExclusiveConsumer = this.exclusiveConsumer;
			connection.ConsumerFailoverRedeliveryWaitPeriod = this.consumerFailoverRedeliveryWaitPeriod;
			connection.CheckForDuplicates = this.checkForDuplicates;
			connection.TransactedIndividualAck = this.transactedIndividualAck;
			connection.NonBlockingRedelivery = this.nonBlockingRedelivery;
			connection.AuditDepth = this.auditDepth;
			connection.AuditMaximumProducerNumber = this.auditMaximumProducerNumber;
		}

		protected static void ExceptionHandler(Exception ex)
		{
			if(ConnectionFactory.onException != null)
			{
				ConnectionFactory.onException(ex);
			}
		}
	}
}
