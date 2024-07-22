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
namespace Apache.NMS
{
    /// <summary>
    /// A delegate that can receive messages async.
    /// </summary>
    public delegate void MessageListener(IMessage message);

    /// <summary>
    /// A delegate that a client can register that will be called each time a consumer dispatches a message
    /// to the client code to allow the client to Transform a received message from one type to another,
    /// StreamMessage to TextMessage, ObjectMessage to TextMessage containing XML, etc.  This allows a
    /// client to create a consumer that will automatically transform a message to a type that the client is
    /// capable of processing or adding additional information to a received message.  For messages that do
    /// not need to be processed the client should return null from this method, in this case the original
    /// message will be dispatched to the client.
    /// </summary>
    public delegate IMessage ConsumerTransformerDelegate(ISession session, IMessageConsumer consumer, IMessage message);

    /// <summary>
    /// A consumer of messages
    /// </summary>
    public interface IMessageConsumer : System.IDisposable
    {
        /// <summary>
        /// Waits until a message is available and returns it
        /// </summary>
        IMessage Receive();

        /// <summary>
        /// If a message is available within the timeout duration it is returned otherwise this method returns null
        /// </summary>
        IMessage Receive(System.TimeSpan timeout);

        /// <summary>
        /// Receives the next message if one is immediately available for delivery on the client side
        /// otherwise this method returns null. It is never an error for this method to return null, the
        /// time of Message availability varies so your client cannot rely on this method to receive a
        /// message immediately after one has been sent.
        /// </summary>
        IMessage ReceiveNoWait();

        /// <summary>
        /// An asynchronous listener which can be used to consume messages asynchronously
        /// </summary>
        event MessageListener Listener;

        /// <summary>
        /// Closes the message consumer.
        /// </summary>
        /// <remarks>
        /// Clients should close message consumers them when they are not needed.
        /// This call blocks until a receive or message listener in progress has completed.
        /// A blocked message consumer receive call returns null when this message consumer is closed.
        /// </remarks>
        void Close();

        /// <summary>
        /// A Delegate that is called each time a Message is dispatched to allow the client to do
        /// any necessary transformations on the received message before it is delivered.
        /// </summary>
        ConsumerTransformerDelegate ConsumerTransformer { get; set; }

    }
}


