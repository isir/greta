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
	/// The mode used to acknowledge messages after they are consumed
	/// </summary>
	public enum AcknowledgementMode
	{
		/// <summary>
		/// With this acknowledgment mode, the session will not
		/// acknowledge receipt of a message since the broker assumes
		/// successful receipt of a message after the onMessage handler
		/// has returned without error.
		/// </summary>
		AutoAcknowledge,

		/// <summary>
		/// With this acknowledgment mode, the session automatically
		/// acknowledges a client's receipt of a message either when
		/// the session has successfully returned from a call to receive
		/// or when the message listener the session has called to
		/// process the message successfully returns.  Acknowlegements
		/// may be delayed in this mode to increase performance at
		/// the cost of the message being redelivered this client fails.
		/// </summary>
		DupsOkAcknowledge,

		/// <summary>
		/// With this acknowledgment mode, the client acknowledges a
		/// consumed message by calling the message's acknowledge method.
		/// This acknowledgement acknowledges the given message and all
		/// unacknowedged messages that have preceeded it for the session
		/// in which the message was delivered.
		/// </summary>
		ClientAcknowledge,

		/// <summary>
		/// Messages will be consumed when the transaction commits.
		/// </summary>
		Transactional,

		/// <summary>
		/// With this acknowledgment mode, the client acknowledges a
		/// consumed message by calling the message's acknowledge method.
		/// This acknowledgement mode allows the client to acknowledge a
		/// single message.  This mode is not required to be supported by
		/// all NMS providers, however the provider should throw an appropriate
		/// exception to indicate that the mode is unsupported.
		/// </summary>
		IndividualAcknowledge
	}

	/// <summary>
	/// A delegate that can receive transport level exceptions.
	/// </summary>
	public delegate void ExceptionListener(Exception exception);

	/// <summary>
	/// A delegate that is used by Fault tolerant NMS Implementation to notify their
	/// clients that the Connection is not currently active to due some error.
	/// </summary>
	public delegate void ConnectionInterruptedListener();

	/// <summary>
	/// A delegate that is used by Fault tolerant NMS Implementation to notify their
	/// clients that the Connection that was interrupted has now been restored.
	/// </summary>
	public delegate void ConnectionResumedListener();

	/// <summary>
	/// Represents a connection with a message broker
	/// </summary>
	public interface IConnection : IDisposable, IStartable, IStoppable
	{
		/// <summary>
		/// Creates a new session to work on this connection
		/// </summary>
		ISession CreateSession();

		/// <summary>
		/// Creates a new session to work on this connection
		/// </summary>
		ISession CreateSession(AcknowledgementMode acknowledgementMode);

		/// <summary>
		/// Closes the connection.
		/// </summary>
		void Close();

		/// <summary>
		/// An asynchronous listener which can be notified if an error occurs
		/// </summary>
		event ExceptionListener ExceptionListener;

		/// <summary>
		/// An asynchronous listener that is notified when a Fault tolerant connection
		/// has been interrupted.
		/// </summary>
		event ConnectionInterruptedListener ConnectionInterruptedListener;

		/// <summary>
		/// An asynchronous listener that is notified when a Fault tolerant connection
		/// has been resumed.
		/// </summary>
		event ConnectionResumedListener ConnectionResumedListener;

		/// <summary>
		/// A Delegate that is called each time a Message is dispatched to allow the client to do
		/// any necessary transformations on the received message before it is delivered.  The
		/// Connection sets the provided delegate instance on each Session it creates which then
		/// passes that along to the Consumers it creates.
		/// </summary>
		ConsumerTransformerDelegate ConsumerTransformer { get; set; }

		/// <summary>
		/// A delegate that is called each time a Message is sent from this Producer which allows
		/// the application to perform any needed transformations on the Message before it is sent.
		/// The Connection sets the provided delegate instance on each Session it creates which then
		/// passes that along to the Producer it creates.
		/// </summary>
		ProducerTransformerDelegate ProducerTransformer { get; set; }

		#region Attributes

		/// <summary>
		/// The default timeout for network requests.
		/// </summary>
		TimeSpan RequestTimeout { get; set; }

		/// <summary>
		/// The default acknowledgement mode
		/// </summary>
		AcknowledgementMode AcknowledgementMode { get; set; }

		/// <summary>
		/// Sets the unique clienet ID for this connection before Start() or returns the
		/// unique client ID after the connection has started
		/// </summary>
		string ClientId { get; set; }

		/// <summary>
		/// Get/or set the redelivery policy for this connection.
		/// </summary>
		IRedeliveryPolicy RedeliveryPolicy { get; set; }

		/// <summary>
		/// Gets the Meta Data for the NMS Connection instance.
		/// </summary>
		IConnectionMetaData MetaData{ get; }

		#endregion

        #region Connection Management methods

        /// <summary>
        /// For a long running Connection that creates many temp destinations
        /// this method will close and destroy all previously created temp
        /// destinations to reduce resource consumption.  This can be useful
        /// when the Connection is pooled or otherwise used for long periods
        /// of time.  Only locally created temp destinations should be removed
        /// by this call.
        /// NOTE: This is an optional operation and for NMS providers that
        /// do not support this functionality the method should just return
        /// without throwing any exceptions.
        /// </summary>
        void PurgeTempDestinations();

        #endregion
	}
}
