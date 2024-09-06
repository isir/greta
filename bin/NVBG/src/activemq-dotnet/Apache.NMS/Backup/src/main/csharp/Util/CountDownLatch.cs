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

namespace Apache.NMS.Util
{
	public class CountDownLatch
	{
		private readonly ManualResetEvent mutex = new ManualResetEvent(false);
		private int remaining;

		public CountDownLatch(int i)
		{
			remaining = i;
		}

        /// <summary>
        /// Decrement the count, releasing any waiting Threads when the count reaches Zero.
        /// </summary>
		public void countDown()
		{
			lock(mutex)
			{
				if(remaining > 0)
				{
					remaining--;
					if(0 == remaining)
					{
						mutex.Set();
					}
				}
			}
		}

        /// <summary>
        /// Gets the current count for this Latch.
        /// </summary>
		public int Remaining
		{
			get
			{
				lock(mutex)
				{
					return remaining;
				}
			}
		}

        /// <summary>
        /// Causes the current Thread to wait for the count to reach zero, unless
        /// the Thread is interrupted.
        /// </summary>
        public void await()
        {
            this.await(TimeSpan.FromMilliseconds(Timeout.Infinite));
        }

        /// <summary>
        /// Causes the current thread to wait until the latch has counted down to zero, unless
        /// the thread is interrupted, or the specified waiting time elapses.
        /// </summary>
		public bool await(TimeSpan timeout)
		{
			return mutex.WaitOne((int) timeout.TotalMilliseconds, false);
		}

		public WaitHandle AsyncWaitHandle
		{
			get { return mutex; }
		}
	}
}
