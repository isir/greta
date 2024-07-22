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
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.Transport
{
	/// <summary>
	/// Handles asynchronous responses
	/// </summary>
	public class FutureResponse
	{
		private TimeSpan maxWait = TimeSpan.FromMilliseconds(Timeout.Infinite);
		public TimeSpan ResponseTimeout
		{
			get { return maxWait; }
			set { maxWait = value; }
		}

		private readonly CountDownLatch latch = new CountDownLatch(1);
		private Response response;

		public Response Response
		{
			// Blocks the caller until a value has been set
			get
			{
				lock(latch)
				{
					if(null != response)
					{
						return response;
					}
				}

				try
				{
					if(!latch.await(maxWait) && response == null)
					{
						throw new RequestTimedOutException(maxWait);
					}
				}
				catch(RequestTimedOutException e)
				{
					Tracer.Error("Caught Timeout Exception while waiting on monitor: " + e);
					throw;
				}
				catch(Exception e)
				{
					Tracer.Error("Caught Exception while waiting on monitor: " + e);
				}
				
				if(response == null && maxWait.TotalMilliseconds > 0)
				{
				}

				lock(latch)
				{
					return response;
				}
			}

			set
			{
				lock(latch)
				{
					response = value;
				}

				latch.countDown();
			}
		}
	}
}

