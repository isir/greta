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

namespace Apache.NMS.ActiveMQ.Threads
{
    /// <summary>
    /// A TaskRunner that dedicates a single thread to running a single Task.
    /// </summary>
    public class DedicatedTaskRunner : TaskRunner
    {
        private readonly Mutex mutex = new Mutex();
        private readonly Thread theThread = null;
        private readonly Task task = null;

        private bool terminated = false;
        private bool pending = false;
        private bool shutdown = false;

        public DedicatedTaskRunner(Task task)
            : this(task, "ActiveMQ Task", ThreadPriority.Normal)
        {
        }

        public DedicatedTaskRunner(Task task, string taskName, ThreadPriority taskPriority)
        {
            if(task == null)
            {
                throw new NullReferenceException("Task was null");
            }

            this.task = task;

            this.theThread = new Thread(Run);
            this.theThread.IsBackground = true;
            this.theThread.Priority = taskPriority;
            this.theThread.Name = taskName;
            this.theThread.Start();
        }

        public void Shutdown(TimeSpan timeout)
        {
            lock(mutex)
            {
                this.shutdown = true;
                this.pending = true;

                Monitor.PulseAll(this.mutex);

                // Wait till the thread stops ( no need to wait if shutdown
                // is called from thread that is shutting down)
                if(Thread.CurrentThread != this.theThread && !this.terminated)
                {
                    Monitor.Wait(this.mutex, timeout);
                }
            }
        }

        public void Shutdown()
        {
            this.Shutdown(TimeSpan.FromMilliseconds(-1));
        }

        public void ShutdownWithAbort(TimeSpan timeout)
        {
            lock(mutex)
            {
                this.shutdown = true;
                this.pending = true;

                Monitor.PulseAll(this.mutex);

                // Wait till the thread stops ( no need to wait if shutdown
                // is called from thread that is shutting down)
                if(Thread.CurrentThread != this.theThread && !this.terminated)
                {
                    Monitor.Wait(this.mutex, timeout);

                    if(!this.terminated)
                    {
                        theThread.Abort();
                    }
                }
            }
        }

        public void Wakeup()
        {
            lock(mutex)
            {
                if(this.shutdown)
                {
                    return;
                }

                this.pending = true;

                Monitor.PulseAll(this.mutex);
            }
        }

        internal void Run()
        {
            try
            {
                while(true)
                {
                    lock(this.mutex)
                    {
                        pending = false;
                        if(this.shutdown)
                        {
                            return;
                        }
                    }

                    if(!this.task.Iterate())
                    {
                        // wait to be notified.
                        lock(this.mutex)
                        {
                            if(this.shutdown)
                            {
                                return;
                            }

                            while(!this.pending)
                            {
                                Monitor.Wait(this.mutex);
                            }
                        }
                    }
                }
            }
            catch(ThreadAbortException)
            {
                // Prevent the ThreadAbortedException for propogating.
                Thread.ResetAbort();
            }
            catch
            {
            }
            finally
            {
                // Make sure we notify any waiting threads that thread
                // has terminated.
                lock(this.mutex)
                {
                    this.terminated = true;
                    Monitor.PulseAll(this.mutex);
                }
            }
        }
    }
}
