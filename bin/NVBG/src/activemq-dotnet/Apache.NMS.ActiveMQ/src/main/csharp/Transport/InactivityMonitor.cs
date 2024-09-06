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
using Apache.NMS.ActiveMQ.Commands;
using Apache.NMS.ActiveMQ.Threads;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Transport
{
	/// <summary>
	/// This class make sure that the connection is still alive,
	/// by monitoring the reception of commands from the peer of
	/// the transport.
	/// </summary>
	public class InactivityMonitor : TransportFilter
	{
		private readonly Atomic<bool> monitorStarted = new Atomic<bool>(false);

		private readonly Atomic<bool> commandSent = new Atomic<bool>(false);
		private readonly Atomic<bool> commandReceived = new Atomic<bool>(false);

		private readonly Atomic<bool> failed = new Atomic<bool>(false);
		private readonly Atomic<bool> inRead = new Atomic<bool>(false);
		private readonly Atomic<bool> inWrite = new Atomic<bool>(false);

		private CompositeTaskRunner asyncTasks;
		private AsyncSignalReadErrorkTask asyncErrorTask;
		private AsyncWriteTask asyncWriteTask;

		private readonly Mutex monitor = new Mutex();

		private Timer connectionCheckTimer;

		private DateTime lastReadCheckTime;

		private static int id = 0;
		private readonly int instanceId = 0;
		private bool disposing = false;

		private long readCheckTime;
		public long ReadCheckTime
		{
			get { return this.readCheckTime; }
			set { this.readCheckTime = value; }
		}

		private long writeCheckTime;
		public long WriteCheckTime
		{
			get { return this.writeCheckTime; }
			set { this.writeCheckTime = value; }
		}

		private long initialDelayTime;
		public long InitialDelayTime
		{
			get { return this.initialDelayTime; }
			set { this.initialDelayTime = value; }
		}

		private readonly Atomic<bool> keepAliveResponseRequired = new Atomic<bool>(false);
		public bool KeepAliveResponseRequired
		{
			get { return this.keepAliveResponseRequired.Value; }
			set { keepAliveResponseRequired.Value = value; }
		}

		// Local and remote Wire Format Information
		private WireFormatInfo localWireFormatInfo;
		private WireFormatInfo remoteWireFormatInfo;

		/// <summary>
		/// Constructor or the Inactivity Monitor
		/// </summary>
		/// <param name="next"></param>
		public InactivityMonitor(ITransport next)
			: base(next)
		{
			this.instanceId = ++id;
			Tracer.DebugFormat("Creating Inactivity Monitor: {0}", instanceId);
		}

		~InactivityMonitor()
		{
			Dispose(false);
		}

		protected override void Dispose(bool disposing)
		{
			if(disposing)
			{
				// get rid of unmanaged stuff
			}

			lock(monitor)
			{
				this.localWireFormatInfo = null;
				this.remoteWireFormatInfo = null;
				this.disposing = true;
				StopMonitorThreads();
			}

			base.Dispose(disposing);
		}

		public void CheckConnection(object state)
		{
			// First see if we have written or can write.
			WriteCheck();

			// Now check is we've read anything, if not then we send
			// a new KeepAlive with response required.
			ReadCheck();
		}

		#region WriteCheck Related
		/// <summary>
		/// Check the write to the broker
		/// </summary>
		public void WriteCheck()
		{
			if(this.inWrite.Value || this.failed.Value)
			{
				Tracer.DebugFormat("InactivityMonitor[{0}]: is in write or already failed.", instanceId);
				return;
			}

			CompositeTaskRunner taskRunner = this.asyncTasks;

			if(!commandSent.Value)
			{
				Tracer.DebugFormat("InactivityMonitor[{0}]: No Message sent since last write check. Sending a KeepAliveInfo.", instanceId);
				if(null != this.asyncWriteTask)
				{
					this.asyncWriteTask.IsPending = true;
				}

				if (this.monitorStarted.Value && taskRunner != null) 
				{
					taskRunner.Wakeup();
				}
			}
			else
			{
				Tracer.DebugFormat("InactivityMonitor[{0}]: Message sent since last write check. Resetting flag.", instanceId);
			}

			commandSent.Value = false;
		}
		#endregion

		#region ReadCheck Related
		public void ReadCheck()
		{
			DateTime now = DateTime.Now;
			TimeSpan elapsed = now - this.lastReadCheckTime;
			CompositeTaskRunner taskRunner = this.asyncTasks;

			if(!AllowReadCheck(elapsed))
			{
				Tracer.Debug("InactivityMonitor[" + instanceId + "]: A read check is not currently allowed.");
				return;
			}

			this.lastReadCheckTime = now;

			if(this.inRead.Value || this.failed.Value)
			{
				Tracer.DebugFormat("InactivityMonitor[{0}]: A receive is in progress or already failed.", instanceId);
				return;
			}

			if(!commandReceived.Value)
			{
				Tracer.DebugFormat("InactivityMonitor[{0}]: No message received since last read check! Sending an InactivityException!", instanceId);
				if(null != this.asyncErrorTask)
				{
					this.asyncErrorTask.IsPending = true;
				}

				if (this.monitorStarted.Value && taskRunner != null) 
				{
					taskRunner.Wakeup();
				}
			}
			else
			{
				commandReceived.Value = false;
			}
		}

		/// <summary>
		/// Checks if we should allow the read check(if less than 90% of the read
		/// check time elapsed then we dont do the readcheck
		/// </summary>
		/// <param name="elapsed"></param>
		/// <returns></returns>
		public bool AllowReadCheck(TimeSpan elapsed)
		{
			return (elapsed.TotalMilliseconds > (readCheckTime * 9 / 10));
		}
		#endregion

		public override void Stop()
		{
			StopMonitorThreads();
			next.Stop();
		}

		protected override void OnCommand(ITransport sender, Command command)
		{
			commandReceived.Value = true;
			inRead.Value = true;
			try
			{
				if(command.IsKeepAliveInfo)
				{
					KeepAliveInfo info = command as KeepAliveInfo;
					if(info.ResponseRequired)
					{
						try
						{
							info.ResponseRequired = false;
							Oneway(info);
						}
						catch(IOException ex)
						{
							OnException(this, ex);
						}
					}
				}
				else if(command.IsWireFormatInfo)
				{
					lock(monitor)
					{
						remoteWireFormatInfo = command as WireFormatInfo;
						try
						{
							StartMonitorThreads();
						}
						catch(IOException ex)
						{
							OnException(this, ex);
						}
					}
				}
				base.OnCommand(sender, command);
			}
			finally
			{
				inRead.Value = false;
			}
		}

		public override void Oneway(Command command)
		{
			// Disable inactivity monitoring while processing a command.
			// synchronize this method - its not synchronized
			// further down the transport stack and gets called by more
			// than one thread  by this class
			lock(inWrite)
			{
				inWrite.Value = true;
				try
				{
					if(failed.Value)
					{
						throw new IOException("Channel was inactive for too long: " + next.RemoteAddress.ToString());
					}
					if(command.IsWireFormatInfo)
					{
						lock(monitor)
						{
							localWireFormatInfo = command as WireFormatInfo;
							StartMonitorThreads();
						}
					}
					next.Oneway(command);
				}
				finally
				{
					commandSent.Value = true;
					inWrite.Value = false;
				}
			}
		}

		protected override void OnException(ITransport sender, Exception command)
		{
			if(failed.CompareAndSet(false, true) && !this.disposing)
			{
				Tracer.DebugFormat("Exception received in the Inactivity Monitor: {0}", command.Message);
				StopMonitorThreads();
				base.OnException(sender, command);
			}
		}

		private void StartMonitorThreads()
		{
			lock(monitor)
			{
				if(this.IsDisposed || this.disposing)
				{
					return;
				}

				if(monitorStarted.Value)
				{
					return;
				}

				if(localWireFormatInfo == null)
				{
					return;
				}

				if(remoteWireFormatInfo == null)
				{
					return;
				}

				readCheckTime =
					Math.Min(
						localWireFormatInfo.MaxInactivityDuration,
						remoteWireFormatInfo.MaxInactivityDuration);
				initialDelayTime = remoteWireFormatInfo.MaxInactivityDurationInitialDelay > 0 ?
					Math.Min(localWireFormatInfo.MaxInactivityDurationInitialDelay,
						     remoteWireFormatInfo.MaxInactivityDurationInitialDelay) :
                    localWireFormatInfo.MaxInactivityDurationInitialDelay;

				if(readCheckTime > 0)
				{
					Tracer.DebugFormat("InactivityMonitor[{0}]: Read Check time interval: {1}",
								   instanceId, readCheckTime);
					Tracer.DebugFormat("InactivityMonitor[{0}]: Initial Delay time interval: {1}",
									   instanceId, initialDelayTime);

					monitorStarted.Value = true;
					this.asyncTasks = new CompositeTaskRunner("InactivityMonitor[" + instanceId + "].Runner");

					this.asyncErrorTask = new AsyncSignalReadErrorkTask(this, next.RemoteAddress);
					this.asyncWriteTask = new AsyncWriteTask(this);

					this.asyncTasks.AddTask(this.asyncErrorTask);
					this.asyncTasks.AddTask(this.asyncWriteTask);

					writeCheckTime = readCheckTime > 3 ? readCheckTime / 3 : readCheckTime;

					Tracer.DebugFormat("InactivityMonitor[{0}]: Write Check time interval: {1}",
									   instanceId, writeCheckTime);

					this.connectionCheckTimer = new Timer(
						new TimerCallback(CheckConnection),
						null,
						initialDelayTime,
						writeCheckTime);
				}
			}
		}

		private void StopMonitorThreads()
		{
			lock(monitor)
			{
				if(monitorStarted.CompareAndSet(true, false))
				{
					AutoResetEvent shutdownEvent = new AutoResetEvent(false);

					if(null != connectionCheckTimer)
					{
						// Attempt to wait for the Timer to shutdown, but don't wait
						// forever, if they don't shutdown after a few seconds, just quit.
						this.connectionCheckTimer.Dispose(shutdownEvent);
						if(!shutdownEvent.WaitOne(TimeSpan.FromMilliseconds(5000), false))
						{
							Tracer.WarnFormat("InactivityMonitor[{0}]: Timer Task didn't shutdown properly.", instanceId);
						}

						this.connectionCheckTimer = null;
					}

					if(null != this.asyncTasks)
					{
						this.asyncTasks.RemoveTask(this.asyncWriteTask);
						this.asyncTasks.RemoveTask(this.asyncErrorTask);
						this.asyncTasks.Shutdown();
						this.asyncTasks = null;
					}

					this.asyncWriteTask = null;
					this.asyncErrorTask = null;
				}
			}

			Tracer.DebugFormat("InactivityMonitor[{0}]: Stopped Monitor Threads.", instanceId);
		}

		#region Async Tasks
		// Task that fires when the TaskRunner is signaled by the ReadCheck Timer Task.
		class AsyncSignalReadErrorkTask : CompositeTask
		{
			private readonly InactivityMonitor parent;
			private readonly Uri remote;
			private readonly Atomic<bool> pending = new Atomic<bool>(false);

			public AsyncSignalReadErrorkTask(InactivityMonitor parent, Uri remote)
			{
				this.parent = parent;
				this.remote = remote;
			}

			public bool IsPending
			{
				get { return this.pending.Value; }
				set { this.pending.Value = value; }
			}

			public bool Iterate()
			{
				if(this.pending.CompareAndSet(true, false) && this.parent.monitorStarted.Value)
				{
					IOException ex = new IOException("Channel was inactive for too long: " + remote);
					this.parent.OnException(parent, ex);
				}

				return this.pending.Value;
			}
		}

		// Task that fires when the TaskRunner is signaled by the WriteCheck Timer Task.
		class AsyncWriteTask : CompositeTask
		{
			private readonly InactivityMonitor parent;
			private readonly Atomic<bool> pending = new Atomic<bool>(false);

			public AsyncWriteTask(InactivityMonitor parent)
			{
				this.parent = parent;
			}

			public bool IsPending
			{
				get { return this.pending.Value; }
				set { this.pending.Value = value; }
			}

			public bool Iterate()
			{
				Tracer.DebugFormat("InactivityMonitor[{0}] preparing for another Write Check", parent.instanceId);
				if(this.pending.CompareAndSet(true, false) && this.parent.monitorStarted.Value)
				{
					try
					{
						Tracer.DebugFormat("InactivityMonitor[{0}] Write Check required sending KeepAlive.",
										   parent.instanceId);
						KeepAliveInfo info = new KeepAliveInfo();
						info.ResponseRequired = this.parent.keepAliveResponseRequired.Value;
						this.parent.Oneway(info);
					}
					catch(IOException e)
					{
						this.parent.OnException(parent, e);
					}
				}

				return this.pending.Value;
			}
		}
		#endregion
	}

}
