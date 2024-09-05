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

namespace Apache.NMS.ActiveMQ.Threads
{
	/// <summary>
	/// A Composite task is one of N tasks that can be managed by a 
	/// CompositTaskRunner instance.  The CompositeTaskRunner checks each
	/// task when its wakeup method is called to determine if the Task has
	/// any work it needs to complete, if no tasks have any pending work 
	/// then the CompositeTaskRunner can return to its sleep state until 
	/// the next time its wakeup method is called or it is shut down.
	/// </summary>
	public interface CompositeTask : Task
	{
		/// <summary>
		/// Indicates if this Task has any pending work.
		/// </summary>
		bool IsPending{ get; }		
	}
}
