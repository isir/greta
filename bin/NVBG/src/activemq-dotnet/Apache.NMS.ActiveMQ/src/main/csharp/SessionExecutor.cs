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
using System.Collections;
using Apache.NMS.ActiveMQ.Commands;
using Apache.NMS.ActiveMQ.Util;
using Apache.NMS.ActiveMQ.Threads;

namespace Apache.NMS.ActiveMQ
{
    public class SessionExecutor : Threads.Task
    {
        private readonly MessageDispatchChannel messageQueue = null;
        private TaskRunner taskRunner = null;

        private readonly Session session = null;
        private readonly IDictionary consumers = null;

        public SessionExecutor(Session session, IDictionary consumers)
        {
            this.session = session;
            this.consumers = consumers;

            if(this.session.Connection != null && this.session.Connection.MessagePrioritySupported)
            {
               this.messageQueue = new SimplePriorityMessageDispatchChannel();
            }
            else
            {
                this.messageQueue = new FifoMessageDispatchChannel();
            }
        }

        ~SessionExecutor()
        {
            try
            {
                Stop();
                Close();
                Clear();
            }
            catch
            {
            }
        }

        public void Execute(MessageDispatch dispatch)
        {
            // Add the data to the queue.
            this.messageQueue.Enqueue(dispatch);
            this.Wakeup();
        }

        public void ExecuteFirst(MessageDispatch dispatch)
        {
            // Add the data to the queue.
            this.messageQueue.EnqueueFirst(dispatch);
            this.Wakeup();
        }

        public void Wakeup()
        {
            TaskRunner taskRunner = this.taskRunner;

            lock(messageQueue.SyncRoot)
            {
                if(this.taskRunner == null)
                {
                    this.taskRunner = DefaultThreadPools.DefaultTaskRunnerFactory.CreateTaskRunner(this);
                }

                taskRunner = this.taskRunner;
            }

            taskRunner.Wakeup();
        }

        public void Start()
        {
            if(!messageQueue.Running)
            {
                messageQueue.Start();

                if(HasUncomsumedMessages)
                {
                    this.Wakeup();
                }
            }
        }

        public void Stop()
        {
            if(messageQueue.Running)
            {
                messageQueue.Stop();
                TaskRunner taskRunner = this.taskRunner;

                if(taskRunner != null)
                {
                    this.taskRunner = null;
                    taskRunner.Shutdown();
                }
            }
        }

        public void Stop(TimeSpan timeout)
        {
            if(messageQueue.Running)
            {
                messageQueue.Stop();
                TaskRunner taskRunner = this.taskRunner;

                if(taskRunner != null)
                {
                    this.taskRunner = null;
                    taskRunner.ShutdownWithAbort(timeout);
                }
            }
        }

        public void Close()
        {
            this.messageQueue.Close();
        }

        public void Dispatch(MessageDispatch dispatch)
        {
            try
            {
                MessageConsumer consumer = null;

                lock(this.consumers.SyncRoot)
                {
                    if(this.consumers.Contains(dispatch.ConsumerId))
                    {
                        consumer = this.consumers[dispatch.ConsumerId] as MessageConsumer;
                    }
                }
				
                // If the consumer is not available, just ignore the message.
                // Otherwise, dispatch the message to the consumer.
                if(consumer != null)
                {
                    consumer.Dispatch(dispatch);
                }
            }
            catch(Exception ex)
            {
                Tracer.DebugFormat("Caught Exception While Dispatching: {0}", ex.Message );
            }
        }

        public bool Iterate()
        {
            try
            {
                lock(this.consumers.SyncRoot)
                {
                    // Deliver any messages queued on the consumer to their listeners.
                    foreach( MessageConsumer consumer in this.consumers.Values )
                    {
                        if(consumer.Iterate())
                        {
                            return true;
                        }
                    }
                }

                // No messages left queued on the listeners.. so now dispatch messages
                // queued on the session
                MessageDispatch message = messageQueue.DequeueNoWait();

                if(message != null)
                {
                    this.Dispatch(message);
                    return !messageQueue.Empty;
                }

                return false;
            }
            catch(Exception ex)
            {
                Tracer.DebugFormat("Caught Exception While Dispatching: {0}", ex.Message );
                this.session.Connection.OnSessionException(this.session, ex);
            }

            return true;
        }

        public void ClearMessagesInProgress()
        {
            this.messageQueue.Clear();
        }

        public void Clear()
        {
            this.messageQueue.Clear();
        }

        public MessageDispatch[] UnconsumedMessages
        {
            get{ return messageQueue.RemoveAll(); }
        }

        public bool HasUncomsumedMessages
        {
            get{ return !messageQueue.Closed && messageQueue.Running && !messageQueue.Empty; }
        }

        public bool Running
        {
            get{ return this.messageQueue.Running; }
        }

        public bool Empty
        {
            get{ return this.messageQueue.Empty; }
        }

    }
}
