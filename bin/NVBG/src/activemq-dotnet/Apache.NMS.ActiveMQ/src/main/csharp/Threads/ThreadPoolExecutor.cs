/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

namespace Apache.NMS.ActiveMQ.Threads
{
    /// <summary>
    /// This class provides a wrapper around the ThreadPool mechanism in .NET
    /// to allow for serial execution of jobs in the ThreadPool and provide
    /// a means of shutting down the execution of jobs in a deterministic
    /// way.
    /// </summary>
    public class ThreadPoolExecutor
    {
        private Queue<Future> workQueue = new Queue<Future>();
        private Mutex syncRoot = new Mutex();
        private bool running = false;
        private bool closing = false;
        private bool closed = false;
        private ManualResetEvent executionComplete = new ManualResetEvent(true);
        private Thread workThread = null;

        /// <summary>
        /// Represents an asynchronous task that is executed on the ThreadPool
        /// at some point in the future.
        /// </summary>
        internal class Future
        {
            private readonly WaitCallback callback;
            private readonly object callbackArg;

            public Future(WaitCallback callback, object arg)
            {
                this.callback = callback;
                this.callbackArg = arg;
            }

            public void Run()
            {
                if(this.callback == null)
                {
                    throw new Exception("Future executed with null WaitCallback");
                }

                try
                {
                    this.callback(callbackArg);
                }
                catch
                {
                }
            }
        }

        public void QueueUserWorkItem(WaitCallback worker)
        {
            this.QueueUserWorkItem(worker, null);
        }

        public void QueueUserWorkItem(WaitCallback worker, object arg)
        {
            if(worker == null)
            {
                throw new ArgumentNullException("Invalid WaitCallback passed");
            }

            if(!this.closed)
            {
                lock(syncRoot)
                {
                    if(!this.closed || !this.closing)
                    {
                        this.workQueue.Enqueue(new Future(worker, arg));

                        if(!this.running)
                        {
                            this.executionComplete.Reset();
                            this.running = true;
                            ThreadPool.QueueUserWorkItem(new WaitCallback(QueueProcessor), null);
                        }
                    }
                }
            }
        }

		/// <summary>
		/// Returns true if this ThreadPoolExecutor has been shut down but has not 
		/// finished running all the tasks that have been Queue.  When a ThreadPoolExecutor
		/// is shut down it will not accept any new tasks but it will complete all tasks
		/// that have been previously queued.
		/// </summary>
        public bool IsShutdown
        {
            get { return this.closing; }
        }

		/// <summary>
		/// Returns true if this ThreadPoolExecutor has been shut down and has also
		/// completed processing of all outstanding tasks in its task Queue.
		/// </summary>
        public bool IsTerminated
        {
            get { return this.closed; }
        }

        public void Shutdown()
        {
            if(!this.closed)
            {
				lock(this.syncRoot)
				{
	                if(!this.closed)
	                {
	                    this.closing = true;

						// Must be no tasks in Queue and none can be accepted
						// now that we've flipped the closing toggle so safe to
						// mark this ThreadPoolExecutor as closed.
						if (!this.running)
						{
							this.closed = true;
							this.executionComplete.Set();
						}
	                }
				}
            }
        }

		public bool AwaitTermination(TimeSpan timeout) 
		{
            if(!this.closed)
            {
                syncRoot.WaitOne();

                if(!this.closed)
                {
					// If called from the worker thread we can't check this as it 
					// will deadlock us, just return whatever the closed state is.
                    if(this.running && Thread.CurrentThread != this.workThread)
                    {
                        syncRoot.ReleaseMutex();
                        this.closed = this.executionComplete.WaitOne(timeout, false);
                        syncRoot.WaitOne();
                    }
                }

                syncRoot.ReleaseMutex();
            }

			return this.closed;
		}

        private void QueueProcessor(object unused)
        {
            Future theTask = null;

            lock(syncRoot)
            {
                this.workThread = Thread.CurrentThread;

                if(this.workQueue.Count == 0)
                {
                    this.running = false;
                    this.executionComplete.Set();
                    return;
                }

                theTask = this.workQueue.Dequeue();
            }

            try
            {
                theTask.Run();
            }
            finally
            {
                this.workThread = null;

                if(this.workQueue.Count == 0)
                {
            		lock(syncRoot)
					{
                    	this.running = false;
                    	this.executionComplete.Set();
					}
                }
                else
                {
                    ThreadPool.QueueUserWorkItem(new WaitCallback(QueueProcessor), null);
                }
            }
        }
    }
}

