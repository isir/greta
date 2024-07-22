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
	/// A facility for applications to schedule tasks for future execution in a background 
	/// thread. Tasks may be scheduled for one-time execution, or for repeated execution at 
	/// regular intervals.  Unlike the normal .NET Timer this Timer allows for multiple tasks
	/// to be scheduled in a single Timer object.  
	/// 
	/// Corresponding to each Timer object is a single background thread that is used to execute
	/// all of the timer's tasks, sequentially. Timer tasks should complete quickly. If a timer 
	/// task takes excessive time to complete, it "hogs" the timer's task execution thread. This
	/// can, in turn, delay the execution of subsequent tasks, which may "bunch up" and execute 
	/// in rapid succession when (and if) the offending task finally completes.
	/// 
	/// After the last live reference to a Timer object goes away and all outstanding tasks have 
	/// completed execution, the timer's task execution thread terminates gracefully (and becomes
	/// subject to garbage collection). However, this can take arbitrarily long to occur. By default, 
	/// the task execution thread does not run as a Background thread, so it is capable of keeping an 
	/// application from terminating. If a caller wants to terminate a timer's task execution thread
	/// rapidly, the caller should invoke the timer's cancel method.
	/// 
	/// If the timer's task execution thread terminates unexpectedly, any further attempt to schedule
	/// a task on the timer will result in an IllegalStateException, as if the timer's cancel method
	/// had been invoked.
	/// 
	/// This class is thread-safe: multiple threads can share a single Timer object without the 
	/// need for external synchronization.
	/// 
	/// This class does not offer real-time guarantees: it schedules tasks using the 
	/// EventWaitHandle.WaitOne(TimeSpan) method.
	/// </summary>
	public class TimerEx
	{
		#region Static Id For Anonymous Timer Naming.

		private static long timerId;

		private static long NextId() 
		{
			return Interlocked.Increment(ref timerId);
		}

		#endregion

		private readonly TimerImpl impl;

	    // Used to finalize thread
	    private readonly DisposeHelper disposal;

	    public TimerEx(String name, bool isBackground)
		{
	    	if (name == null)
			{
	    		throw new NullReferenceException("name is null");
	    	}
	        this.impl = new TimerImpl(name, isBackground);
	        this.disposal = new DisposeHelper(impl);
		}

	    public TimerEx(String name) : this(name, false)
		{
	    }

	    public TimerEx(bool isBackground) : this("Timer-" + TimerEx.NextId().ToString(), isBackground)
		{
	    }

	    public TimerEx() : this(false)
		{
	    }

		/// <summary>
		/// Terminates this timer, discarding any currently scheduled tasks. Does not interfere
		/// with a currently executing task (if it exists). Once a timer has been terminated, 
		/// its execution thread terminates gracefully, and no more tasks may be scheduled on it.
		/// 
		/// Note that calling this method from within the run method of a timer task that was 
		/// invoked by this timer absolutely guarantees that the ongoing task execution is the 
		/// last task execution that will ever be performed by this timer.
		/// 
		/// This method may be called repeatedly; the second and subsequent calls have no effect. 
		/// </summary>
	    public void Cancel() 
		{
	        this.impl.Cancel();
	    }

		/// <summary>
		/// Removes all cancelled tasks from this timer's task queue. Calling this method has 
		/// no effect on the behavior of the timer, but eliminates the references to the cancelled
		/// tasks from the queue. If there are no external references to these tasks, they become 
		/// eligible for garbage collection.
		/// 
		/// Most programs will have no need to call this method. It is designed for use by the 
		/// rare application that cancels a large number of tasks. Calling this method trades 
		/// time for space: the runtime of the method may be proportional to n + c log n, where 
		/// n is the number of tasks in the queue and c is the number of cancelled tasks.
		/// 
		/// Note that it is permissible to call this method from within a a task scheduled 
		/// on this timer. 
		/// </summary>
	    public int Purge() 
		{
	        lock(this.impl.SyncRoot)
			{
	            return impl.Purge();
	        }
	    }

		public override string ToString()
		{
			return string.Format("[TimerEx{0}]", this.impl.Name);
		}

		#region WaitCallback Scheduling Methods

		/// <summary>
		/// Schedules the specified WaitCallback task for execution at the specified time. If the 
		/// time is in the past, the task is scheduled for immediate execution.  The method returns
		/// a TimerTask instance that can be used to later cancel the scheduled task.
		/// </summary>
	    public TimerTask Schedule(WaitCallback callback, object arg, DateTime when) 
		{
			InternalTimerTask task = new InternalTimerTask(callback, arg);
	        TimeSpan delay = when - DateTime.Now;
	        DoScheduleImpl(task, delay, TimeSpan.FromMilliseconds(-1), false);
			return task;
	    }

		/// <summary>
		/// Schedules the specified WaitCallback task for execution after the specified delay. 
		/// The method returns a TimerTask instance that can be used to later cancel the 
		/// scheduled task.
		/// </summary>
	    public TimerTask Schedule(WaitCallback callback, object arg, int delay)
		{
 			if(delay < 0)
			{
	            throw new ArgumentOutOfRangeException();
	        }

			InternalTimerTask task = new InternalTimerTask(callback, arg);
			DoScheduleImpl(task, TimeSpan.FromMilliseconds(delay), TimeSpan.FromMilliseconds(-1), false);
			return task;
	    }

		/// <summary>
		/// Schedules the specified WaitCallback task for execution after the specified delay. 
		/// The method returns a TimerTask instance that can be used to later cancel the 
		/// scheduled task.
		/// </summary>
	    public TimerTask Schedule(WaitCallback callback, object arg, TimeSpan delay)
		{
 			if(delay.CompareTo(TimeSpan.Zero) < 0)
			{
	            throw new ArgumentOutOfRangeException();
	        }

			InternalTimerTask task = new InternalTimerTask(callback, arg);
			DoScheduleImpl(task, delay, TimeSpan.FromMilliseconds(-1), false);
			return task;
	    }

		/// <summary>
		/// Schedules the specified WaitCallback task for repeated fixed-delay execution, 
		/// beginning after the specified delay. Subsequent executions take place at approximately 
		/// regular intervals separated by the specified period.
		/// 
		/// In fixed-delay execution, each execution is scheduled relative to the actual execution
		/// time of the previous execution. If an execution is delayed for any reason (such as 
		/// garbage collection or other background activity), subsequent executions will be delayed.
		/// 
		/// Fixed-delay execution is appropriate for recurring activities that require "smoothness." 
		/// In other words, it is appropriate for activities where it is more important to keep the
		/// frequency accurate in the short run than in the long run.
		/// 
		/// The method returns a TimerTask instance that can be used to later cancel the 
		/// scheduled task.
		/// </summary>
	    public TimerTask Schedule(WaitCallback callback, object arg, int delay, int period)
		{
 			if(delay < 0 || period <= 0)
			{
	            throw new ArgumentOutOfRangeException();
	        }

			InternalTimerTask task = new InternalTimerTask(callback, arg);
	        DoScheduleImpl(task, TimeSpan.FromMilliseconds(delay), TimeSpan.FromMilliseconds(period), false);
			return task;
	    }

		/// <summary>
		/// Schedules the specified WaitCallback task for repeated fixed-delay execution, 
		/// beginning after the specified delay. Subsequent executions take place at approximately 
		/// regular intervals separated by the specified period.
		/// 
		/// In fixed-delay execution, each execution is scheduled relative to the actual execution
		/// time of the previous execution. If an execution is delayed for any reason (such as 
		/// garbage collection or other background activity), subsequent executions will be delayed.
		/// 
		/// Fixed-delay execution is appropriate for recurring activities that require "smoothness." 
		/// In other words, it is appropriate for activities where it is more important to keep the
		/// frequency accurate in the short run than in the long run.
		/// 
		/// The method returns a TimerTask instance that can be used to later cancel the 
		/// scheduled task.
		/// </summary>
	    public TimerTask Schedule(WaitCallback callback, object arg, TimeSpan delay, TimeSpan period)
		{
 			if(delay.CompareTo(TimeSpan.Zero) < 0 || period.CompareTo(TimeSpan.Zero) <= 0)
			{
	            throw new ArgumentOutOfRangeException();
	        }

			InternalTimerTask task = new InternalTimerTask(callback, arg);
	        DoScheduleImpl(task, delay, period, false);
			return task;
	    }

		/// <summary>
		/// Schedules the specified WaitCallback task for repeated fixed-delay execution, 
		/// beginning at the specified start time. Subsequent executions take place at approximately 
		/// regular intervals separated by the specified period.
		/// 
		/// In fixed-delay execution, each execution is scheduled relative to the actual execution
		/// time of the previous execution. If an execution is delayed for any reason (such as 
		/// garbage collection or other background activity), subsequent executions will be delayed.
		/// 
		/// Fixed-delay execution is appropriate for recurring activities that require "smoothness." 
		/// In other words, it is appropriate for activities where it is more important to keep the
		/// frequency accurate in the short run than in the long run.
		/// 
		/// The method returns a TimerTask instance that can be used to later cancel the 
		/// scheduled task.
		/// </summary>
	    public TimerTask Schedule(WaitCallback callback, object arg, DateTime when, int period) 
		{
	        if (period <= 0) 
			{
	            throw new ArgumentOutOfRangeException();
	        }
	        
			InternalTimerTask task = new InternalTimerTask(callback, arg);
			TimeSpan delay = when - DateTime.Now;	        
			DoScheduleImpl(task, delay, TimeSpan.FromMilliseconds(period), false);
			return task;
	    }

		/// <summary>
		/// Schedules the specified WaitCallback task for repeated fixed-delay execution, 
		/// beginning at the specified start time. Subsequent executions take place at approximately 
		/// regular intervals separated by the specified period.
		/// 
		/// In fixed-delay execution, each execution is scheduled relative to the actual execution
		/// time of the previous execution. If an execution is delayed for any reason (such as 
		/// garbage collection or other background activity), subsequent executions will be delayed.
		/// 
		/// Fixed-delay execution is appropriate for recurring activities that require "smoothness." 
		/// In other words, it is appropriate for activities where it is more important to keep the
		/// frequency accurate in the short run than in the long run.
		/// 
		/// The method returns a TimerTask instance that can be used to later cancel the 
		/// scheduled task.
		/// </summary>
	    public TimerTask Schedule(WaitCallback callback, object arg, DateTime when, TimeSpan period) 
		{
	        if (period.CompareTo(TimeSpan.Zero) <= 0) 
			{
	            throw new ArgumentOutOfRangeException();
	        }
	        
			InternalTimerTask task = new InternalTimerTask(callback, arg);
			TimeSpan delay = when - DateTime.Now;	        
			DoScheduleImpl(task, delay, period, false);
			return task;
	    }

		/// <summary>
		/// Schedules the specified WaitCallback task for repeated fixed-rate execution, beginning 
		/// after the specified delay. Subsequent executions take place at approximately regular 
		/// intervals, separated by the specified period.
		/// 
		/// In fixed-rate execution, each execution is scheduled relative to the scheduled execution
		/// time of the initial execution. If an execution is delayed for any reason (such as garbage
		/// collection or other background activity), two or more executions will occur in rapid 
		/// succession to "catch up." 
		/// 
		/// Fixed-rate execution is appropriate for recurring activities that are sensitive to 
		/// absolute time, such as ringing a chime every hour on the hour, or running scheduled 
		/// maintenance every day at a particular time.
		/// 
		/// The method returns a TimerTask instance that can be used to later cancel the 
		/// scheduled task.
		/// </summary>
	    public TimerTask ScheduleAtFixedRate(WaitCallback callback, object arg, int delay, int period)
		{
 			if(delay < 0 || period <= 0)
			{
	            throw new ArgumentOutOfRangeException();
	        }

			InternalTimerTask task = new InternalTimerTask(callback, arg);
	        DoScheduleImpl(task, TimeSpan.FromMilliseconds(delay), TimeSpan.FromMilliseconds(period), true);
			return task;
	    }

		/// <summary>
		/// Schedules the specified WaitCallback task for repeated fixed-rate execution, beginning 
		/// after the specified delay. Subsequent executions take place at approximately regular 
		/// intervals, separated by the specified period.
		/// 
		/// In fixed-rate execution, each execution is scheduled relative to the scheduled execution
		/// time of the initial execution. If an execution is delayed for any reason (such as garbage
		/// collection or other background activity), two or more executions will occur in rapid 
		/// succession to "catch up." 
		/// 
		/// Fixed-rate execution is appropriate for recurring activities that are sensitive to 
		/// absolute time, such as ringing a chime every hour on the hour, or running scheduled 
		/// maintenance every day at a particular time.
		/// 
		/// The method returns a TimerTask instance that can be used to later cancel the 
		/// scheduled task.
		/// </summary>
	    public TimerTask ScheduleAtFixedRate(WaitCallback callback, object arg, TimeSpan delay, TimeSpan period)
		{
 			if(delay.CompareTo(TimeSpan.Zero) < 0 || period.CompareTo(TimeSpan.Zero) <= 0)
			{
	            throw new ArgumentOutOfRangeException();
	        }

			InternalTimerTask task = new InternalTimerTask(callback, arg);
	        DoScheduleImpl(task, delay, period, true);
			return task;
	    }

		/// <summary>
		/// Schedules the specified WaitCallback task for repeated fixed-rate execution, beginning 
		/// at the specified time. Subsequent executions take place at approximately regular 
		/// intervals, separated by the specified period.
		/// 
		/// In fixed-rate execution, each execution is scheduled relative to the scheduled execution
		/// time of the initial execution. If an execution is delayed for any reason (such as garbage
		/// collection or other background activity), two or more executions will occur in rapid 
		/// succession to "catch up." 
		/// 
		/// Fixed-rate execution is appropriate for recurring activities that are sensitive to 
		/// absolute time, such as ringing a chime every hour on the hour, or running scheduled 
		/// maintenance every day at a particular time.
		/// 
		/// The method returns a TimerTask instance that can be used to later cancel the 
		/// scheduled task.
		/// </summary>
	    public TimerTask ScheduleAtFixedRate(WaitCallback callback, object arg, DateTime when, int period)
		{
	        if (period <= 0) 
			{
	            throw new ArgumentOutOfRangeException();
	        }

			InternalTimerTask task = new InternalTimerTask(callback, arg);
	        TimeSpan delay = when - DateTime.Now;
			DoScheduleImpl(task, delay, TimeSpan.FromMilliseconds(period), true);
			return task;
	    }

		/// <summary>
		/// Schedules the specified WaitCallback task for repeated fixed-rate execution, beginning 
		/// at the specified time. Subsequent executions take place at approximately regular 
		/// intervals, separated by the specified period.
		/// 
		/// In fixed-rate execution, each execution is scheduled relative to the scheduled execution
		/// time of the initial execution. If an execution is delayed for any reason (such as garbage
		/// collection or other background activity), two or more executions will occur in rapid 
		/// succession to "catch up." 
		/// 
		/// Fixed-rate execution is appropriate for recurring activities that are sensitive to 
		/// absolute time, such as ringing a chime every hour on the hour, or running scheduled 
		/// maintenance every day at a particular time.
		/// 
		/// The method returns a TimerTask instance that can be used to later cancel the 
		/// scheduled task.
		/// </summary>
	    public TimerTask ScheduleAtFixedRate(WaitCallback callback, object arg, DateTime when, TimeSpan period)
		{
	        if (period.CompareTo(TimeSpan.Zero) <= 0) 
			{
	            throw new ArgumentOutOfRangeException();
	        }

			InternalTimerTask task = new InternalTimerTask(callback, arg);
	        TimeSpan delay = when - DateTime.Now;
			DoScheduleImpl(task, delay, period, true);
			return task;
	    }

		#endregion

		#region TimerTask Scheduling Methods

		/// <summary>
		/// Schedules the specified TimerTask for execution at the specified time. If the 
		/// time is in the past.
		/// </summary>
	    public void Schedule(TimerTask task, DateTime when) 
		{
	        TimeSpan delay = when - DateTime.Now;
	        DoScheduleImpl(task, delay, TimeSpan.FromMilliseconds(-1), false);
	    }

		/// <summary>
		/// Schedules the specified TimerTask for execution after the specified delay. 
		/// </summary>
	    public void Schedule(TimerTask task, int delay)
		{
 			if(delay < 0)
			{
	            throw new ArgumentOutOfRangeException();
	        }

			DoScheduleImpl(task, TimeSpan.FromMilliseconds(delay), TimeSpan.FromMilliseconds(-1), false);
	    }

		/// <summary>
		/// Schedules the specified TimerTask for execution after the specified delay. 
		/// </summary>
	    public void Schedule(TimerTask task, TimeSpan delay)
		{
 			if(delay.CompareTo(TimeSpan.Zero) < 0)
			{
	            throw new ArgumentOutOfRangeException();
	        }

			DoScheduleImpl(task, delay, TimeSpan.FromMilliseconds(-1), false);
	    }

		/// <summary>
		/// Schedules the specified TimerTask for repeated fixed-delay execution, beginning 
		/// after the specified delay. Subsequent executions take place at approximately 
		/// regular intervals separated by the specified period.
		/// 
		/// In fixed-delay execution, each execution is scheduled relative to the actual execution
		/// time of the previous execution. If an execution is delayed for any reason (such as 
		/// garbage collection or other background activity), subsequent executions will be delayed.
		/// 
		/// Fixed-delay execution is appropriate for recurring activities that require "smoothness." 
		/// In other words, it is appropriate for activities where it is more important to keep the
		/// frequency accurate in the short run than in the long run.
		/// </summary>
	    public void Schedule(TimerTask task, int delay, int period)
		{
 			if(delay < 0 || period <= 0)
			{
	            throw new ArgumentOutOfRangeException();
	        }

	        DoScheduleImpl(task, TimeSpan.FromMilliseconds(delay), TimeSpan.FromMilliseconds(period), false);
	    }

		/// <summary>
		/// Schedules the specified TimerTask for repeated fixed-delay execution, beginning 
		/// after the specified delay. Subsequent executions take place at approximately 
		/// regular intervals separated by the specified period.
		/// 
		/// In fixed-delay execution, each execution is scheduled relative to the actual execution
		/// time of the previous execution. If an execution is delayed for any reason (such as 
		/// garbage collection or other background activity), subsequent executions will be delayed.
		/// 
		/// Fixed-delay execution is appropriate for recurring activities that require "smoothness." 
		/// In other words, it is appropriate for activities where it is more important to keep the
		/// frequency accurate in the short run than in the long run.
		/// </summary>
	    public void Schedule(TimerTask task, TimeSpan delay, TimeSpan period)
		{
 			if(delay.CompareTo(TimeSpan.Zero) < 0 || period.CompareTo(TimeSpan.Zero) <= 0)
			{
	            throw new ArgumentOutOfRangeException();
	        }

	        DoScheduleImpl(task, delay, period, false);
	    }

		/// <summary>
		/// Schedules the specified TimerTask for repeated fixed-delay execution, beginning 
		/// at the specified time. Subsequent executions take place at approximately 
		/// regular intervals separated by the specified period.
		/// 
		/// In fixed-delay execution, each execution is scheduled relative to the actual execution
		/// time of the previous execution. If an execution is delayed for any reason (such as 
		/// garbage collection or other background activity), subsequent executions will be delayed.
		/// 
		/// Fixed-delay execution is appropriate for recurring activities that require "smoothness." 
		/// In other words, it is appropriate for activities where it is more important to keep the
		/// frequency accurate in the short run than in the long run.
		/// </summary>
	    public void Schedule(TimerTask task, DateTime when, int period) 
		{
	        if (period <= 0) 
			{
	            throw new ArgumentOutOfRangeException();
	        }
	        
			TimeSpan delay = when - DateTime.Now;	        
			DoScheduleImpl(task, delay, TimeSpan.FromMilliseconds(period), false);
	    }

		/// <summary>
		/// Schedules the specified TimerTask for repeated fixed-delay execution, beginning 
		/// at the specified time. Subsequent executions take place at approximately 
		/// regular intervals separated by the specified period.
		/// 
		/// In fixed-delay execution, each execution is scheduled relative to the actual execution
		/// time of the previous execution. If an execution is delayed for any reason (such as 
		/// garbage collection or other background activity), subsequent executions will be delayed.
		/// 
		/// Fixed-delay execution is appropriate for recurring activities that require "smoothness." 
		/// In other words, it is appropriate for activities where it is more important to keep the
		/// frequency accurate in the short run than in the long run.
		/// </summary>
	    public void Schedule(TimerTask task, DateTime when, TimeSpan period) 
		{
	        if (period.CompareTo(TimeSpan.Zero) <= 0) 
			{
	            throw new ArgumentOutOfRangeException();
	        }
	        
			TimeSpan delay = when - DateTime.Now;	        
			DoScheduleImpl(task, delay, period, false);
	    }

		/// <summary>
		/// Schedules the specified TimerTask for repeated fixed-rate execution, beginning 
		/// after the specified delay. Subsequent executions take place at approximately regular 
		/// intervals, separated by the specified period.
		/// 
		/// In fixed-rate execution, each execution is scheduled relative to the scheduled execution
		/// time of the initial execution. If an execution is delayed for any reason (such as garbage
		/// collection or other background activity), two or more executions will occur in rapid 
		/// succession to "catch up." 
		/// 
		/// Fixed-rate execution is appropriate for recurring activities that are sensitive to 
		/// absolute time, such as ringing a chime every hour on the hour, or running scheduled 
		/// maintenance every day at a particular time.
		/// </summary>
	    public void ScheduleAtFixedRate(TimerTask task, int delay, int period)
		{
 			if(delay < 0 || period <= 0)
			{
	            throw new ArgumentOutOfRangeException();
	        }

	        DoScheduleImpl(task, TimeSpan.FromMilliseconds(delay), TimeSpan.FromMilliseconds(period), true);
	    }

		/// <summary>
		/// Schedules the specified TimerTask for repeated fixed-rate execution, beginning 
		/// after the specified delay. Subsequent executions take place at approximately regular 
		/// intervals, separated by the specified period.
		/// 
		/// In fixed-rate execution, each execution is scheduled relative to the scheduled execution
		/// time of the initial execution. If an execution is delayed for any reason (such as garbage
		/// collection or other background activity), two or more executions will occur in rapid 
		/// succession to "catch up." 
		/// 
		/// Fixed-rate execution is appropriate for recurring activities that are sensitive to 
		/// absolute time, such as ringing a chime every hour on the hour, or running scheduled 
		/// maintenance every day at a particular time.
		/// </summary>
	    public void ScheduleAtFixedRate(TimerTask task, TimeSpan delay, TimeSpan period)
		{
 			if(delay.CompareTo(TimeSpan.Zero) < 0 || period.CompareTo(TimeSpan.Zero) <= 0)
			{
	            throw new ArgumentOutOfRangeException();
	        }

	        DoScheduleImpl(task, delay, period, true);
	    }

		/// <summary>
		/// Schedules the specified TimerTask for repeated fixed-rate execution, beginning 
		/// at the specified time. Subsequent executions take place at approximately regular 
		/// intervals, separated by the specified period.
		/// 
		/// In fixed-rate execution, each execution is scheduled relative to the scheduled execution
		/// time of the initial execution. If an execution is delayed for any reason (such as garbage
		/// collection or other background activity), two or more executions will occur in rapid 
		/// succession to "catch up." 
		/// 
		/// Fixed-rate execution is appropriate for recurring activities that are sensitive to 
		/// absolute time, such as ringing a chime every hour on the hour, or running scheduled 
		/// maintenance every day at a particular time.
		/// </summary>
	    public void ScheduleAtFixedRate(TimerTask task, DateTime when, int period)
		{
	        if (period <= 0) 
			{
	            throw new ArgumentOutOfRangeException();
	        }

	        TimeSpan delay = when - DateTime.Now;
			DoScheduleImpl(task, delay, TimeSpan.FromMilliseconds(period), true);
	    }

		/// <summary>
		/// Schedules the specified TimerTask for repeated fixed-rate execution, beginning 
		/// at the specified time. Subsequent executions take place at approximately regular 
		/// intervals, separated by the specified period.
		/// 
		/// In fixed-rate execution, each execution is scheduled relative to the scheduled execution
		/// time of the initial execution. If an execution is delayed for any reason (such as garbage
		/// collection or other background activity), two or more executions will occur in rapid 
		/// succession to "catch up." 
		/// 
		/// Fixed-rate execution is appropriate for recurring activities that are sensitive to 
		/// absolute time, such as ringing a chime every hour on the hour, or running scheduled 
		/// maintenance every day at a particular time.
		/// </summary>
	    public void ScheduleAtFixedRate(TimerTask task, DateTime when, TimeSpan period)
		{
	        if (period.CompareTo(TimeSpan.Zero) <= 0) 
			{
	            throw new ArgumentOutOfRangeException();
	        }

	        TimeSpan delay = when - DateTime.Now;
			DoScheduleImpl(task, delay, period, true);
	    }

		#endregion

		#region Implementation of Scheduling method.

	    private void DoScheduleImpl(TimerTask task, TimeSpan delay, TimeSpan period, bool fixedRate) 
		{
			if (task == null)
			{
				throw new ArgumentNullException("TimerTask cannot be null");
			}

			lock(this.impl.SyncRoot)
			{
				if (impl.Cancelled) 
				{
	                throw new InvalidOperationException();
	            }

	            DateTime when = DateTime.Now + delay;

	            lock(task.syncRoot)
				{
	                if (task.IsScheduled)
					{
	                    throw new InvalidOperationException();
	                }

	                if (task.cancelled)
					{
	                    throw new InvalidOperationException("Task is already cancelled");
	                }

	                task.when = when;
	                task.period = period;
	                task.fixedRate = fixedRate;
	            }

	            // insert the newTask into queue
	            impl.InsertTask(task);
	        }
	    }

		#endregion

		#region Interal TimerTask to invoking WaitCallback tasks

		private class InternalTimerTask : TimerTask
		{
			private WaitCallback task;
			private object taskArg;

			public InternalTimerTask(WaitCallback task, object taskArg)
			{
				if (task == null)
				{
					throw new ArgumentNullException("The WaitCallback task cannot be null");
				}

				this.task = task;
				this.taskArg = taskArg;
			}

			public override void Run()
			{
				this.task(taskArg);
			}
		}

		#endregion

		#region Timer Heap that sorts Tasks into timed order.

        private sealed class TimerHeap  
		{	        
			internal static readonly int DEFAULT_HEAP_SIZE = 256;
            
			internal TimerTask[] timers = new TimerTask[DEFAULT_HEAP_SIZE];
            internal int size = 0;
            internal int deletedCancelledNumber = 0;

            public TimerTask Minimum() 
			{
                return timers[0];
            }

            public bool IsEmpty() 
			{
                return size == 0;
            }

            public void Insert(TimerTask task) 
			{
                if (timers.Length == size) 
				{
                    TimerTask[] appendedTimers = new TimerTask[size * 2];
					timers.CopyTo(appendedTimers, 0);
                    timers = appendedTimers;
                }
                timers[size++] = task;
                UpHeap();
            }

            public void Delete(int pos) 
			{
                // posible to delete any position of the heap
                if (pos >= 0 && pos < size) 
				{
                    timers[pos] = timers[--size];
                    timers[size] = null;
                    DownHeap(pos);
                }
            }

            private void UpHeap() 
			{
                int current = size - 1;
                int parent = (current - 1) / 2;

                while (timers[current].when < timers[parent].when) 
				{
                    // swap the two
                    TimerTask tmp = timers[current];
                    timers[current] = timers[parent];
                    timers[parent] = tmp;

                    // update pos and current
                    current = parent;
                    parent = (current - 1) / 2;
                }
            }

            private void DownHeap(int pos) 
			{
                int current = pos;
                int child = 2 * current + 1;

                while (child < size && size > 0) 
				{
                    // compare the children if they exist
                    if (child + 1 < size && timers[child + 1].when < timers[child].when) 
					{
                        child++;
                    }

                    // compare selected child with parent
                    if (timers[current].when < timers[child].when) 
					{
                        break;
                    }

                    // swap the two
                    TimerTask tmp = timers[current];
                    timers[current] = timers[child];
                    timers[child] = tmp;

                    // update pos and current
                    current = child;
                    child = 2 * current + 1;
                }
            }

            public void Reset() 
			{
                timers = new TimerTask[DEFAULT_HEAP_SIZE];
                size = 0;
            }

            public void AdjustMinimum() 
			{
                DownHeap(0);
            }

            public void DeleteIfCancelled() 
			{
                for (int i = 0; i < size; i++) 
				{
                    if (timers[i].cancelled) 
					{
                        deletedCancelledNumber++;
                        Delete(i);
                        // re-try this point
                        i--;
                    }
                }
            }

            internal int GetTask(TimerTask task) 
			{
                for (int i = 0; i < timers.Length; i++) 
				{
                    if (timers[i] == task) 
					{
                        return i;
                    }
                }
                return -1;
            }
        }

		#endregion

		#region TimerEx Task Runner Implementation

		private sealed class TimerImpl
		{
	        private bool cancelled;
	        private bool finished;
			private String name;
	        private TimerHeap tasks = new TimerHeap();
			private System.Threading.Thread runner;
			private object syncRoot = new object();

	        public TimerImpl(String name, bool isBackground) 
			{
				this.name = name;
				this.runner = new Thread(new ThreadStart(this.Run));
				this.runner.Name = name;
				this.runner.IsBackground = isBackground;
	            this.runner.Start();
	        }

			public String Name
			{
				get { return this.name; }
			}

			public object SyncRoot
			{
				get { return this.syncRoot; }
			}

			public bool Cancelled
			{
				get { return this.cancelled; }
			}

			public bool Finished 
			{
				set { this.finished = value; }
			}

			/// <summary>
			/// Run this Timers event loop in its own Thread.
			/// </summary>
	        public void Run() 
			{
	            while (true) 
				{
	                TimerTask task;
	                lock (this.syncRoot) 
					{
	                    // need to check cancelled inside the synchronized block
	                    if (cancelled) 
						{
	                        return;
	                    }

	                    if (tasks.IsEmpty()) 
						{
	                        if (finished) 
							{
	                            return;
	                        }
	                        
							// no tasks scheduled -- sleep until any task appear
	                        try
							{
								Monitor.Wait(this.syncRoot);
	                        } 
							catch (ThreadInterruptedException) 
							{
	                        }
	                        continue;
	                    }

	                    DateTime currentTime = DateTime.Now;

	                    task = tasks.Minimum();
	                    TimeSpan timeToSleep;

	                    lock (task.syncRoot)
						{
	                        if (task.cancelled) 
							{
	                            tasks.Delete(0);
	                            continue;
	                        }

	                        // check the time to sleep for the first task scheduled
	                        timeToSleep = task.when - currentTime;
	                    }

	                    if (timeToSleep.CompareTo(TimeSpan.Zero) > 0) 
						{
	                        // sleep!
	                        try 
							{
								Monitor.Wait(this.syncRoot, timeToSleep);
	                        } 
							catch (ThreadInterruptedException) 
							{
	                        }
	                        continue;
	                    }

	                    // no sleep is necessary before launching the task
	                    lock (task.syncRoot) 
						{
	                        int pos = 0;
	                        if (tasks.Minimum().when != task.when) 
							{
	                            pos = tasks.GetTask(task);
	                        }
	                        if (task.cancelled)
							{
	                            tasks.Delete(tasks.GetTask(task));
	                            continue;
	                        }

	                        // set time to schedule
	                        task.ScheduledTime = task.when;

	                        // remove task from queue
	                        tasks.Delete(pos);

	                        // set when the next task should be launched
	                        if (task.period.CompareTo(TimeSpan.Zero) >= 0) 
							{
	                            // this is a repeating task,
	                            if (task.fixedRate) 
								{
	                                // task is scheduled at fixed rate
	                                task.when = task.when + task.period;
	                            } 
								else 
								{
	                                // task is scheduled at fixed delay
	                                task.when = DateTime.Now + task.period;
	                            }

	                            // insert this task into queue
	                            InsertTask(task);
	                        }
							else 
							{
	                            task.when = DateTime.MinValue;
	                        }
	                    }
	                }

	                bool taskCompletedNormally = false;
	                try 
					{
	                    task.Run();
	                    taskCompletedNormally = true;
	                }
					finally 
					{
	                    if (!taskCompletedNormally) 
						{
	                        lock (this) 
							{
	                            cancelled = true;
	                        }
	                    }
	                }
	            }
	        }

	        public void InsertTask(TimerTask newTask) 
			{
	            // callers are synchronized
	            tasks.Insert(newTask);
				Monitor.Pulse(this.syncRoot);
	        }

	        public void Cancel() 
			{
				lock(this.syncRoot)
				{
	            	cancelled = true;
	            	tasks.Reset();
					Monitor.Pulse(this.syncRoot);
				}
	        }

	        public int Purge() 
			{
	            if (tasks.IsEmpty()) 
				{
	                return 0;
	            }
	            
				// callers are synchronized
	            tasks.deletedCancelledNumber = 0;
	            tasks.DeleteIfCancelled();
	            return tasks.deletedCancelledNumber;
	        }
	    }

		#endregion

		#region Helper class to handle Timer shutdown when Disposed

		private sealed class DisposeHelper : IDisposable
		{
			private readonly TimerImpl impl;
			
			public DisposeHelper(TimerImpl impl) 
			{
				this.impl = impl;
			}
			
			public void Dispose() 
			{
				lock(impl.SyncRoot)
				{
					impl.Finished = true;
					Monitor.PulseAll(impl.SyncRoot);
				}
			}
		}

		#endregion
	}
}

