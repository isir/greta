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
using System.Collections.Generic;

namespace Apache.NMS.ActiveMQ.Threads
{
	/// <summary>
	/// Scheduler Service useful for running various delayed units of work.
	/// </summary>
	public class Scheduler
	{
		private object syncRoot = new object();
    	private readonly String name;
    	private TimerEx timer;
    	private readonly Dictionary<Object, TimerTask> timerTasks = new Dictionary<object, TimerTask>();
		private bool started = false;

		public Scheduler(String name)
		{
			this.name = name;
		}

		/// <summary>
		/// Executes the given task periodically using a fixed-delay execution style 
		/// which prevents tasks from bunching should there be some delay such as
		/// garbage collection or machine sleep.  
		/// 
		/// This repeating unit of work can later be cancelled using the WaitCallback
		/// that was originally used to initiate the processing.
		/// </summary>
	    public void ExecutePeriodically(WaitCallback task, object arg, int period) 
		{
			lock (this.syncRoot)
			{
				CheckStarted();
	        	TimerTask timerTask = timer.Schedule(task, arg, period, period);
	        	timerTasks.Add(task, timerTask);
			}
	    }

		/// <summary>
		/// Executes the given task periodically using a fixed-delay execution style 
		/// which prevents tasks from bunching should there be some delay such as
		/// garbage collection or machine sleep.  
		/// 
		/// This repeating unit of work can later be cancelled using the WaitCallback
		/// that was originally used to initiate the processing.
		/// </summary>
	    public void ExecutePeriodically(WaitCallback task, object arg, TimeSpan period) 
		{
			lock (this.syncRoot)
			{
				CheckStarted();
		        TimerTask timerTask = timer.Schedule(task, arg, period, period);
		        timerTasks.Add(task, timerTask);
			}
	    }

		/// <summary>
		/// Executes the given task the after delay, no reference is kept for this
		/// task so it cannot be cancelled later.  
		/// </summary>
	    public void ExecuteAfterDelay(WaitCallback task, object arg, int delay) 
		{
			lock (this.syncRoot)
			{
				CheckStarted();
			}

			timer.Schedule(task, arg, delay);
	    }

		/// <summary>
		/// Executes the given task the after delay, no reference is kept for this
		/// task so it cannot be cancelled later.  
		/// </summary>
	    public void ExecuteAfterDelay(WaitCallback task, object arg, TimeSpan delay) 
		{
			lock (this.syncRoot)
			{
				CheckStarted();
			}

			timer.Schedule(task, arg, delay);
	    }

	    public void Cancel(object task) 
		{
			lock (this.syncRoot)
			{
				if (timerTasks.ContainsKey(task))
				{
	        		TimerTask ticket = timerTasks[task];
	        		if (ticket != null) 
					{
			            ticket.Cancel();
	            		timer.Purge(); // remove cancelled TimerTasks
	        		}

					timerTasks.Remove(task);
				}
			}
	    }

	    public void Start()
		{
			lock (this.syncRoot)
			{
	        	this.timer = new TimerEx(name, true);
				this.started = true;
			}
	    }

	    public void Stop() 
		{
			lock (this.syncRoot)
			{
				this.started = false;
	        	if (this.timer != null)
            	{
	            	this.timer.Cancel();
	        	}
			}
	    }

		public String Name
		{
			get { return this.name; }
		}

		public bool Started
		{
			get 
			{ 
				lock (this.syncRoot)
				{
					return this.started; 
				}
			}
		}

	    public override string ToString()
		{
			return string.Format("[Scheduler][{0}]", name);
		}

		private void CheckStarted()
		{
			if (!this.started)
			{
				throw new InvalidOperationException("The Schedular has not been started yet");
			}
		}
	}
}

