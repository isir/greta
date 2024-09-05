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

namespace Apache.NMS
{
	/// <summary>
	/// Represents a message either to be sent to a message broker or received from a message broker.
	/// </summary>
	public interface IMessage
	{
		/// <summary>
		/// If using client acknowledgement mode on the session, then this method will acknowledge that the
		/// message has been processed correctly.
		/// </summary>
		void Acknowledge();

		/// <summary>
		/// Clears out the message body. Clearing a message's body does not clear its header
		/// values or property entries.
		///
		/// If this message body was read-only, calling this method leaves the message body in
		/// the same state as an empty body in a newly created message.
		/// </summary>
		void ClearBody();

		/// <summary>
		/// Clears a message's properties.
		///
		/// The message's header fields and body are not cleared.
		/// </summary>
		void ClearProperties();

		/// <summary>
		/// Provides access to the message properties (headers).
		/// </summary>
		IPrimitiveMap Properties { get; }

		/// <summary>
		/// The correlation ID used to correlate messages from conversations or long running business processes.
		/// </summary>
		string NMSCorrelationID { get; set; }

		/// <summary>
		/// The destination of the message.  This property is set by the IMessageProducer.
		/// </summary>
		IDestination NMSDestination { get; set; }

		/// <summary>
		/// The amount of time for which this message is valid.  Zero if this message does not expire.
		/// </summary>
		TimeSpan NMSTimeToLive { get; set; }

		/// <summary>
		/// The message ID which is set by the provider.
		/// </summary>
		string NMSMessageId { get; set; }

		/// <summary>
		/// Whether or not this message is persistent.
		/// </summary>
		MsgDeliveryMode NMSDeliveryMode { get; set; }

		/// <summary>
		/// The Priority of this message.
		/// </summary>
		MsgPriority NMSPriority { get; set; }

		/// <summary>
		/// Returns true if this message has been redelivered to this or another consumer before being acknowledged successfully.
		/// </summary>
		bool NMSRedelivered { get; set; }

		/// <summary>
		/// The destination that the consumer of this message should send replies to
		/// </summary>
		IDestination NMSReplyTo { get; set; }

		/// <summary>
		/// The timestamp of when the message was pubished in UTC time.  If the publisher disables setting
		/// the timestamp on the message, the time will be set to the start of the UNIX epoc (1970-01-01 00:00:00).
		/// </summary>
		DateTime NMSTimestamp { get; set; }

		/// <summary>
		/// The type name of this message.
		/// </summary>
		string NMSType { get; set; }
	}
}
