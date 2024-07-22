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
using System.Threading;

namespace Apache.NMS.ActiveMQ.Threads
{
	class PooledTaskRunner : TaskRunner
	{
		private readonly int maxIterationsPerRun;
		private readonly Task task;
		private readonly Object runable = new Object();
		private bool queued;
		private bool _shutdown;
		private bool iterating;
		private volatile System.Threading.Thread runningThread;

		public void Run(Object o)
		{
			PooledTaskRunner p = o as PooledTaskRunner;
			p.runningThread = System.Threading.Thread.CurrentThread;
			try
			{
				p.RunTask();
			}
			finally
			{
				p.runningThread = null;
			}
		}

		public PooledTaskRunner(Task task, int maxIterationsPerRun)
		{
			this.maxIterationsPerRun = maxIterationsPerRun;
			this.task = task;
			this._shutdown = false;
			this.iterating = false;
			this.queued = true;
			ThreadPool.QueueUserWorkItem(new WaitCallback(Run), this);
		}

		/// <summary>
		/// We Expect MANY wakeup calls on the same TaskRunner.
		/// </summary>
		public void Wakeup()
		{
			lock(runable)
			{
				// When we get in here, we make some assumptions of state:
				// queued=false, iterating=false: wakeup() has not be called and
				// therefore task is not executing.
				// queued=true, iterating=false: wakeup() was called but, task
				// execution has not started yet
				// queued=false, iterating=true : wakeup() was called, which caused
				// task execution to start.
				// queued=true, iterating=true : wakeup() called after task
				// execution was started.

				if(queued || _shutdown)
				{
					return;
				}

				queued = true;

				// The runTask() method will do this for me once we are done
				// iterating.
				if(!iterating)
				{
					ThreadPool.QueueUserWorkItem(new WaitCallback(Run), this);
				}
			}
		}

		/// <summary>
		/// shut down the task
		/// </summary>
		/// <param name="timeout"></param>
		public void Shutdown(TimeSpan timeout)
		{
			lock(runable)
			{
				_shutdown = true;
				// the check on the thread is done
				// because a call to iterate can result in
				// shutDown() being called, which would wait forever
				// waiting for iterating to finish
				if(runningThread != System.Threading.Thread.CurrentThread)
				{
					if(iterating)
					{
						System.Threading.Thread.Sleep(timeout);
					}
				}
			}
		}

        public void ShutdownWithAbort(TimeSpan timeout)
        {
            lock(runable)
            {
                _shutdown = true;

                if(runningThread != System.Threading.Thread.CurrentThread)
                {
                    if(iterating)
                    {
                        System.Threading.Thread.Sleep(timeout);
                    }

                    if(iterating)
                    {
                        runningThread.Abort();
                    }
                }
            }
        }

		public void Shutdown()
		{
			Shutdown(new TimeSpan(Timeout.Infinite));
		}

		internal void RunTask()
		{
			lock(runable)
			{
				queued = false;
				if(_shutdown)
				{
					iterating = false;
					return;
				}
				iterating = true;
			}

			// Don't synchronize while we are iterating so that
			// multiple wakeup() calls can be executed concurrently.
			bool done = false;
			try
			{
				for(int i = 0; i < maxIterationsPerRun; i++)
				{
					if(!task.Iterate())
					{
						done = true;
						break;
					}
				}
			}
			finally
			{
				lock(runable)
				{
					iterating = false;
					if(_shutdown)
					{
						queued = false;
					}
					else
					{
						// If we could not iterate all the items
						// then we need to re-queue.
						if(!done)
						{
							queued = true;
						}

						if(queued)
						{
							ThreadPool.QueueUserWorkItem(new WaitCallback(Run), this);
						}
					}
				}
			}
		}
	}
}
