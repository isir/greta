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
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.Util
{
    /// <summary>
    /// Defines an interface for a Message Channel used to dispatch incoming
    /// Messages to a Session or MessageConsumer.  The implementation controls
    /// how the messages are dequeued from the channel, one option is for a
    /// FIFO ordering while another might be to sort the Message's based on the
    /// set Message Priority.
    /// </summary>
    public interface MessageDispatchChannel
    {
        object SyncRoot
        {
            get;
        }

        bool Closed
        {
            get;
            set;
        }

        bool Running
        {
            get;
            set;
        }

        bool Empty
        {
            get;
        }

        long Count
        {
            get;
        }

        void Start();

        void Stop();

        void Close();

        void Enqueue(MessageDispatch dispatch);

        void EnqueueFirst(MessageDispatch dispatch);

        MessageDispatch Dequeue(TimeSpan timeout);

        MessageDispatch DequeueNoWait();

        MessageDispatch Peek();

        void Clear();

        MessageDispatch[] RemoveAll();

        void Signal();
    }
}
