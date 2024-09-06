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
using System.Threading;
using Apache.NMS.Util;
using Apache.NMS.ActiveMQ.Commands;
using Apache.NMS.ActiveMQ.Util;

namespace Apache.NMS.ActiveMQ
{
	/// <summary>
	/// An object capable of sending messages to some destination
	/// </summary>
	public class MessageProducer : IMessageProducer
	{
		private readonly Session session;
		private readonly MemoryUsage usage = null;
		private readonly object closedLock = new object();
		private bool closed = false;
		private readonly ProducerInfo info;
		private int producerSequenceId = 0;

		private MsgDeliveryMode msgDeliveryMode = NMSConstants.defaultDeliveryMode;
		private TimeSpan requestTimeout;
		private TimeSpan msgTimeToLive = NMSConstants.defaultTimeToLive;
		private MsgPriority msgPriority = NMSConstants.defaultPriority - 1;
		private bool disableMessageID = false;
		private bool disableMessageTimestamp = false;
		protected bool disposed = false;

		private readonly MessageTransformation messageTransformation;

		public MessageProducer(Session session, ProducerId id, ActiveMQDestination destination, TimeSpan requestTimeout)
		{
			this.session = session;
			this.RequestTimeout = requestTimeout;

			this.info = new ProducerInfo();
			this.info.ProducerId = id;
			this.info.Destination = destination;
			this.info.WindowSize = session.Connection.ProducerWindowSize;

			this.messageTransformation = session.Connection.MessageTransformation;

			// If the destination contained a URI query, then use it to set public
			// properties on the ProducerInfo
			if (destination != null && destination.Options != null)
			{
				URISupport.SetProperties(this.info, destination.Options, "producer.");
			}

			// Version Three and higher will send us a ProducerAck, but only if we
			// have a set producer window size.
			if (session.Connection.ProtocolVersion >= 3 && this.info.WindowSize > 0)
			{
                if (Tracer.IsDebugEnabled)
                {
                    Tracer.Debug("MessageProducer created with a Window Size of: " + this.info.WindowSize);
                }
			    this.usage = new MemoryUsage(this.info.WindowSize);
			}
		}

		~MessageProducer()
		{
			Dispose(false);
		}

		public void Dispose()
		{
			Dispose(true);
			GC.SuppressFinalize(this);
		}

		protected void Dispose(bool disposing)
		{
			if(disposed)
			{
				return;
			}

			try
			{
				Close();
			}
			catch
			{
			}

			disposed = true;
		}

		public void Close()
		{
			lock(closedLock)
			{
				if(closed)
				{
					return;
				}

				Shutdown();
				RemoveInfo removeInfo = new RemoveInfo();
				removeInfo.ObjectId = this.info.ProducerId;
				this.session.Connection.Oneway(removeInfo);
				if(Tracer.IsDebugEnabled)
				{
                    Tracer.DebugFormat("Remove of Producer[{0}] for destination[{1}] sent.", 
                                       this.ProducerId, this.info.Destination);
				}
			}
		}

		/// <summary>
		/// Called from the Parent session to deactivate this Producer, when a parent
		/// is closed all children are automatically removed from the broker so this
		/// method circumvents the need to send a Remove command to the broker.
		/// </summary>
		internal void Shutdown()
		{
			lock(closedLock)
			{
				if(closed)
				{
					return;
				}

				try
				{
					session.RemoveProducer(info.ProducerId);
				}
				catch(Exception ex)
				{
					Tracer.ErrorFormat("Error during producer close: {0}", ex);
				}

				if(this.usage != null)
				{
					this.usage.Stop();
				}

				closed = true;
			}
		}

		public void Send(IMessage message)
		{
			Send(info.Destination, message, this.msgDeliveryMode, this.msgPriority, this.msgTimeToLive, false);
		}

		public void Send(IDestination destination, IMessage message)
		{
			Send(destination, message, this.msgDeliveryMode, this.msgPriority, this.msgTimeToLive, false);
		}

		public void Send(IMessage message, MsgDeliveryMode deliveryMode, MsgPriority priority, TimeSpan timeToLive)
		{
			Send(info.Destination, message, deliveryMode, priority, timeToLive, true);
		}

		public void Send(IDestination destination, IMessage message, MsgDeliveryMode deliveryMode, MsgPriority priority, TimeSpan timeToLive)
		{
			Send(destination, message, deliveryMode, priority, timeToLive, true);
		}

		protected void Send(IDestination destination, IMessage message, MsgDeliveryMode deliveryMode, MsgPriority priority, TimeSpan timeToLive, bool specifiedTimeToLive)
		{
			if(null == destination)
			{
				// See if this producer was created without a destination.
				if(null == info.Destination)
				{
					throw new NotSupportedException();
				}

				// The producer was created with a destination, but an invalid destination
				// was specified.
				throw new Apache.NMS.InvalidDestinationException();
			}

			ActiveMQDestination dest = null;

			if(destination == this.info.Destination)
			{
				dest = destination as ActiveMQDestination;
			}
			else if(info.Destination == null)
			{
				dest = ActiveMQDestination.Transform(destination);
			}
			else
			{
				throw new NotSupportedException("This producer can only send messages to: " + this.info.Destination.PhysicalName);
			}

			if(this.ProducerTransformer != null)
			{
				IMessage transformed = this.ProducerTransformer(this.session, this, message);
				if(transformed != null)
				{
					message = transformed;
				}
			}

			ActiveMQMessage activeMessage = this.messageTransformation.TransformMessage<ActiveMQMessage>(message);

			activeMessage.ProducerId = info.ProducerId;
			activeMessage.Destination = dest;
			activeMessage.NMSDeliveryMode = deliveryMode;
			activeMessage.NMSPriority = priority;

			// Always set the message Id regardless of the disable flag.
			MessageId id = new MessageId();
			id.ProducerId = info.ProducerId;
			id.ProducerSequenceId = Interlocked.Increment(ref this.producerSequenceId);
			activeMessage.MessageId = id;
			
			// Ensure that the source message contains the NMSMessageId of the transformed
			// message for correlation purposes.
			if (!ReferenceEquals(message, activeMessage))
			{
				message.NMSMessageId = activeMessage.NMSMessageId;				
			}

			if(!disableMessageTimestamp)
			{
				activeMessage.NMSTimestamp = DateTime.UtcNow;
			}

			if(specifiedTimeToLive)
			{
				activeMessage.NMSTimeToLive = timeToLive;
			}

			// Ensure there's room left to send this message
			if(this.usage != null)
			{
				usage.WaitForSpace();
			}

			lock(closedLock)
			{
				if(closed)
				{
					throw new ConnectionClosedException();
				}

				session.DoSend(dest, activeMessage, this, this.usage, this.RequestTimeout);
			}
		}

		public ProducerId ProducerId
		{
			get { return info.ProducerId; }
		}

		public ProducerInfo ProducerInfo
		{
			get { return info; }
		}

		public MsgDeliveryMode DeliveryMode
		{
			get { return msgDeliveryMode; }
			set { this.msgDeliveryMode = value; }
		}

		public TimeSpan TimeToLive
		{
			get { return msgTimeToLive; }
			set { this.msgTimeToLive = value; }
		}

		public TimeSpan RequestTimeout
		{
			get { return requestTimeout; }
			set { this.requestTimeout = value; }
		}

		public MsgPriority Priority
		{
			get { return msgPriority; }
			set { this.msgPriority = value; }
		}

		public bool DisableMessageID
		{
			get { return disableMessageID; }
			set { this.disableMessageID = value; }
		}

		public bool DisableMessageTimestamp
		{
			get { return disableMessageTimestamp; }
			set { this.disableMessageTimestamp = value; }
		}

		private ProducerTransformerDelegate producerTransformer;
		public ProducerTransformerDelegate ProducerTransformer
		{
			get { return this.producerTransformer; }
			set { this.producerTransformer = value; }
		}

		public IMessage CreateMessage()
		{
			return session.CreateMessage();
		}

		public ITextMessage CreateTextMessage()
		{
			return session.CreateTextMessage();
		}

		public ITextMessage CreateTextMessage(string text)
		{
			return session.CreateTextMessage(text);
		}

		public IMapMessage CreateMapMessage()
		{
			return session.CreateMapMessage();
		}

		public IObjectMessage CreateObjectMessage(object body)
		{
			return session.CreateObjectMessage(body);
		}

		public IBytesMessage CreateBytesMessage()
		{
			return session.CreateBytesMessage();
		}

		public IBytesMessage CreateBytesMessage(byte[] body)
		{
			return session.CreateBytesMessage(body);
		}

		public IStreamMessage CreateStreamMessage()
		{
			return session.CreateStreamMessage();
		}

		internal void OnProducerAck(ProducerAck ack)
		{
            if (Tracer.IsDebugEnabled)
            {
                Tracer.Debug("Received ProducerAck for Message of Size = {" + ack.Size + "}");
            }

		    if(this.usage != null)
			{
				this.usage.DecreaseUsage( ack.Size );
			}
		}
	}
}
