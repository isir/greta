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
	/// A Task that is run in a Timer instance either once or repeatedly.
	/// </summary>
	public abstract class TimerTask
	{
		internal object syncRoot = new object();

		internal DateTime when = DateTime.MinValue;
		internal DateTime scheduledTime = DateTime.MinValue;
		internal TimeSpan period;
		internal bool cancelled;
		internal bool fixedRate;

		protected TimerTask()
		{
		}

		public bool Cancel()
		{
			lock(this.syncRoot)
			{
	            bool willRun = !cancelled && when != DateTime.MinValue;
	            cancelled = true;
	            return willRun;
	        }
		}

		public DateTime ScheduledExecutionTime
		{
			get
			{
				lock(this.syncRoot)
				{
					return this.scheduledTime;
				}
			}
		}

		public abstract void Run();

		#region Timer Methods

		internal DateTime When
		{
			get
			{
				lock(this.syncRoot)
				{
					return this.when;
				}
			}
		}

	    internal DateTime ScheduledTime
		{
			set 
			{
				lock(this.syncRoot)
				{
					this.scheduledTime = value;
		        }
			}
	    }

	    internal bool IsScheduled
		{
			get
			{
				lock(this.syncRoot)
				{
	            	return when != DateTime.MinValue || scheduledTime != DateTime.MinValue;
	        	}
			}
	    }

		#endregion
	}
}

