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
	/// Represents a task that may take a few iterations to complete.
	/// </summary>
	public interface Task
	{
		/// <summary>
		/// Performs some portion of the work that this Task object is
		/// assigned to complete.  When the task is entirely finished this
		/// method should return false. 
		/// </summary>
		/// <returns>
		/// A <see cref="System.Boolean"/> this indicates if this Task should 
		/// be run again.
		/// </returns>
		bool Iterate();
	}
}
