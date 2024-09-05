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
using System.Threading;
using Apache.NMS.ActiveMQ.Commands;

namespace Apache.NMS.ActiveMQ.Util
{
    /// <summary>
    /// A FIFO based MessageDispatchChannel.
    /// </summary>
    public class FifoMessageDispatchChannel : MessageDispatchChannel
    {
        private readonly Mutex mutex = new Mutex();
        private bool closed;
        private bool running;
        private readonly LinkedList<MessageDispatch> channel = new LinkedList<MessageDispatch>();

        #region Properties

        public object SyncRoot
        {
            get{ return this.mutex; }
        }

        public bool Closed
        {
            get
            {
                lock(this.mutex)
                {
                    return this.closed;
                }
            }

            set
            {
                lock(this.mutex)
                {
                    this.closed = value;
                }
            }
        }

        public bool Running
        {
            get
            {
                lock(this.mutex)
                {
                    return this.running;
                }
            }

            set
            {
                lock(this.mutex)
                {
                    this.running = value;
                }
            }
        }

        public bool Empty
        {
            get
            {
                lock(mutex)
                {
                    return channel.Count == 0;
                }
            }
        }

        public long Count
        {
            get
            {
                lock(mutex)
                {
                    return channel.Count;
                }
            }
        }

        #endregion

        public void Start()
        {
            lock(this.mutex)
            {
                if(!Closed)
                {
                    this.running = true;
                    Monitor.PulseAll(this.mutex);
                }
            }
        }

        public void Stop()
        {
            lock(mutex)
            {
                this.running = false;
                Monitor.PulseAll(this.mutex);
            }
        }

        public void Close()
        {
            lock(mutex)
            {
                if(!Closed)
                {
                    this.running = false;
                    this.closed = true;
                }

                Monitor.PulseAll(this.mutex);
            }
        }

        public void Enqueue(MessageDispatch dispatch)
        {
            lock(this.mutex)
            {
                this.channel.AddLast(dispatch);
                Monitor.Pulse(this.mutex);
            }
        }

        public void EnqueueFirst(MessageDispatch dispatch)
        {
            lock(this.mutex)
            {
                this.channel.AddFirst(dispatch);
                Monitor.Pulse(this.mutex);
            }
        }

        public MessageDispatch Dequeue(TimeSpan timeout)
        {
            lock(this.mutex)
            {
                // Wait until the channel is ready to deliver messages.
                if( timeout != TimeSpan.Zero && !Closed && ( Empty || !Running ) )
                {
                    Monitor.Wait(this.mutex, timeout);
                }

                if( Closed || !Running || Empty )
                {
                    return null;
                }

                return DequeueNoWait();
            }
        }

        public MessageDispatch DequeueNoWait()
        {
            MessageDispatch result = null;

            lock(this.mutex)
            {
                if( Closed || !Running || Empty )
                {
                    return null;
                }

                result = channel.First.Value;
                this.channel.RemoveFirst();
            }

            return result;
        }

        public MessageDispatch Peek()
        {
            lock(this.mutex)
            {
                if( Closed || !Running || Empty )
                {
                    return null;
                }

                return channel.First.Value;
            }
        }

        public void Clear()
        {
            lock(mutex)
            {
                this.channel.Clear();
            }
        }

        public MessageDispatch[] RemoveAll()
        {
            MessageDispatch[] result;

            lock(mutex)
            {
                result = new MessageDispatch[this.Count];
                channel.CopyTo(result, 0);
                channel.Clear();
            }

            return result;
        }

        public void Signal()
        {
            lock(mutex)
            {
                Monitor.PulseAll(this.mutex);
            }
        }
    }
}

