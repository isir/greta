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
	/// <summary>
	/// Manages the thread pool for long running tasks. Long running tasks are not
	/// always active but when they are active, they may need a few iterations of
	/// processing for them to become idle. The manager ensures that each task is
	/// processes but that no one task overtakes the system. This is kina like
	/// cooperative multitasking.
    ///
    /// If your OS/JVM combination has a good thread model, you may want to avoid
    /// using a thread pool to run tasks and use a DedicatedTaskRunner instead.
    /// </summary>
	public class TaskRunnerFactory
	{
		public string name = "ActiveMQ Task";
        public ThreadPriority priority = ThreadPriority.Normal;
        public int maxIterationsPerRun = 1000;
        public bool dedicatedTaskRunner = true;

		public TaskRunnerFactory()
		{
		}

        public TaskRunner CreateTaskRunner(Task task)
        {
            return CreateTaskRunner(task, this.name);
        }

        public TaskRunner CreateTaskRunner(Task task, string name)
        {
            return CreateTaskRunner(task, name, this.priority);
        }

		public TaskRunner CreateTaskRunner(Task task, string name, ThreadPriority taskPriority)
		{
            if(this.dedicatedTaskRunner)
            {
                return new DedicatedTaskRunner(task, name, taskPriority);
            }
            else
            {
                return new PooledTaskRunner(task, this.maxIterationsPerRun);
            }
		}
	}
}
