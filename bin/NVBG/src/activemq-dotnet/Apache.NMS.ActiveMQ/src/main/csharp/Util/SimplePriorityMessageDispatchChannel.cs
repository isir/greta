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
    public class SimplePriorityMessageDispatchChannel : MessageDispatchChannel
    {
        public const int MAX_PRIORITY = 10;

        private readonly Mutex mutex = new Mutex();
        private bool closed;
        private bool running;
        private readonly LinkedList<MessageDispatch>[] channels = new LinkedList<MessageDispatch>[MAX_PRIORITY];
        private int size;

        public SimplePriorityMessageDispatchChannel()
        {
            for(int i = 0; i < MAX_PRIORITY; ++i)
            {
                channels[i] = new LinkedList<MessageDispatch>();
            }
        }

        #region Properties

        public object SyncRoot
        {
            get{ return this.mutex; }
        }

        public bool Closed
        {
            get
            {
                return this.closed;
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
                return this.running;
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
                return this.size == 0;
            }
        }

        public long Count
        {
            get
            {
                return this.size;
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
                GetList(dispatch).AddLast(dispatch);
                this.size++;
                Monitor.Pulse(this.mutex);
            }
        }

        public void EnqueueFirst(MessageDispatch dispatch)
        {
            lock(this.mutex)
            {
                GetList(dispatch).AddFirst(dispatch);
                this.size++;
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

                return RemoveFirst();
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

                result = RemoveFirst();
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

                return GetFirst();
            }
        }

        public void Clear()
        {
            lock(mutex)
            {
                foreach(LinkedList<MessageDispatch> list in channels)
                {
                    list.Clear();
                }
            }
        }

        public MessageDispatch[] RemoveAll()
        {
            MessageDispatch[] result;

            lock(mutex)
            {
                result = new MessageDispatch[this.size];
                int copyPos = 0;

                for(int i = MAX_PRIORITY - 1; i >= 0; i--)
                {
                    LinkedList<MessageDispatch> list = channels[i];
                    list.CopyTo(result, copyPos);
                    copyPos += list.Count;
                    size -= list.Count;
                    list.Clear();
                }
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

        protected int GetPriority(MessageDispatch message)
        {
            int priority = (int) MsgPriority.Lowest;

            if(message.Message != null)
            {
                priority = Math.Max((int) message.Message.Priority, 0);
                priority = Math.Min(priority, 9);
            }

            return priority;
        }

        protected LinkedList<MessageDispatch> GetList(MessageDispatch md)
        {
            return channels[GetPriority(md)];
        }

        private MessageDispatch RemoveFirst()
        {
            if(this.size > 0)
            {
                for(int i = MAX_PRIORITY - 1; i >= 0; i--)
                {
                    LinkedList<MessageDispatch> list = channels[i];
                    if(list.Count != 0)
                    {
                        this.size--;
                        MessageDispatch dispatch = list.First.Value;
                        list.RemoveFirst();
                        return dispatch;
                    }
                }
            }
            return null;
        }

        private MessageDispatch GetFirst()
        {
            if(this.size > 0)
            {
                for(int i = MAX_PRIORITY - 1; i >= 0; i--)
                {
                    LinkedList<MessageDispatch> list = channels[i];
                    if(list.Count != 0)
                    {
                        return list.First.Value;
                    }
                }
            }
            return null;
        }
    }
}

