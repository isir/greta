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

namespace Apache.NMS.ActiveMQ.Threads
{
	/// <summary>
	/// Allows you to request a thread execute the associated Task.
	/// </summary>
	public interface TaskRunner
	{
        /// <summary>
        /// Wakeup the TaskRunner and have it check for any pending work that
        /// needs to be completed.  If none is found it will go back to sleep
        /// until another Wakeup call is made.
        /// </summary>
		void Wakeup();

        /// <summary>
        /// Attempt to Shutdown the TaskRunner, this method will wait indefinitely
        /// for the TaskRunner to quite if the task runner is in a call to its Task's
        /// run method and that never returns.
        /// </summary>
		void Shutdown();

        /// <summary>
        /// Performs a timed wait for the TaskRunner to shutdown.  If the TaskRunner
        /// is in a call to its Task's run method and that does not return before the
        /// timeout expires this method returns and the TaskRunner may remain in the
        /// running state.
        /// </summary>
        /// <param name="timeout">
        /// A <see cref="TimeSpan"/>
        /// </param>
		void Shutdown(TimeSpan timeout);

        /// <summary>
        /// Performs a timed wait for the TaskRunner to shutdown.  If the TaskRunner
        /// is in a call to its Task's run method and that does not return before the
        /// timeout expires this method sends an Abort to the Task thread and return.
        /// </summary>
        /// <param name="timeout">
        /// A <see cref="TimeSpan"/>
        /// </param>
        void ShutdownWithAbort(TimeSpan timeout);

	}
}
