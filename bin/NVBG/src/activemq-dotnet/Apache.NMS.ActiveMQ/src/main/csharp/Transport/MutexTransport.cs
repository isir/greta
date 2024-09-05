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

namespace Apache.NMS.ActiveMQ.Transport
{
	/// <summary>
	/// A Transport which guards access to the next transport using a mutex.
	/// </summary>
	public class MutexTransport : TransportFilter
	{
		private readonly object transmissionLock = new object();

		private void GetTransmissionLock(int timeout)
		{
			if(timeout > 0)
			{
				DateTime timeoutTime = DateTime.Now + TimeSpan.FromMilliseconds(timeout);
				int waitCount = 1;

				while(true)
				{
					if(Monitor.TryEnter(transmissionLock))
					{
						break;
					}

					if(DateTime.Now > timeoutTime)
					{
						throw new IOException(string.Format("Oneway timed out after {0} milliseconds.", timeout));
					}

					// Back off from being overly aggressive.  Having too many threads
					// aggressively trying to get the lock pegs the CPU.
					Thread.Sleep(3 * (waitCount++));
				}
			}
			else
			{
				Monitor.Enter(transmissionLock);
			}
		}

		public MutexTransport(ITransport next) : base(next)
		{
		}

		public override void Oneway(Command command)
		{
			GetTransmissionLock(this.next.Timeout);
			try
			{
				base.Oneway(command);
			}
			finally
			{
				Monitor.Exit(transmissionLock);
			}
		}

		public override FutureResponse AsyncRequest(Command command)
		{
			GetTransmissionLock(this.next.AsyncTimeout);
			try
			{
				return base.AsyncRequest(command);
			}
			finally
			{
				Monitor.Exit(transmissionLock);
			}
		}

		public override Response Request(Command command, TimeSpan timeout)
		{
			GetTransmissionLock((int) timeout.TotalMilliseconds);
			try
			{
				return base.Request(command, timeout);
			}
			finally
			{
				Monitor.Exit(transmissionLock);
			}
		}
	}
}
