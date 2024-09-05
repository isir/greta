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
using System.Collections;
using Apache.NMS.ActiveMQ.Commands;
using System.Threading;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ
{
	public class QueueBrowser : IQueueBrowser, IEnumerator
	{
		private readonly Session session;
		private readonly ActiveMQDestination destination;
		private readonly string selector;

		private MessageConsumer consumer;
		private bool disposed;
		private bool closed;
		private readonly ConsumerId consumerId;
		private readonly Atomic<bool> browseDone = new Atomic<bool>(false);
		private readonly bool dispatchAsync;
		private readonly object semaphore = new object();
		private readonly object myLock = new object();

		internal QueueBrowser(Session session, ConsumerId consumerId, ActiveMQDestination destination, string selector, bool dispatchAsync)
		{
			this.session = session;
			this.consumerId = consumerId;
			this.destination = destination;
			this.selector = selector;
			this.dispatchAsync = dispatchAsync;
			this.consumer = CreateConsumer();
		}

		~QueueBrowser()
		{
			Dispose(false);
		}

		public void Dispose()
		{
			Dispose(true);
			GC.SuppressFinalize(this);
		}

		protected void Dispose(bool disposing)
		{
			if(disposed)
			{
				return;
			}

			if(disposing)
			{
				// Dispose managed code here.
			}

			try
			{
				Close();
			}
			catch
			{
				// Ignore network errors.
			}

			disposed = true;
		}

		private MessageConsumer CreateConsumer()
		{
			this.browseDone.Value = false;
			BrowsingMessageConsumer consumer = null;

			if(this.session.Connection.PrefetchPolicy.QueueBrowserPrefetch == 0)
			{
				Tracer.Warn("Attempted to create a Queue Browser with Zero sized prefetch buffer.");
				throw new NMSException("Cannot create a Queue Browser with Zero sized prefetch buffer");
			}
			
			try
			{
				consumer = new BrowsingMessageConsumer(
					this, session, this.consumerId, this.destination, null, this.selector,
					this.session.Connection.PrefetchPolicy.QueueBrowserPrefetch,
					this.session.Connection.PrefetchPolicy.MaximumPendingMessageLimit,
					false, true, this.dispatchAsync);

				this.session.AddConsumer(consumer);
				this.session.Connection.SyncRequest(consumer.ConsumerInfo);

				if(this.session.Connection.IsStarted)
				{
					consumer.Start();
				}
			}
			catch(Exception)
			{
				if(consumer != null)
				{
					this.session.RemoveConsumer(consumer);
				}

				throw;
			}

			return consumer;
		}

		private void DestroyConsumer()
		{
			if(consumer == null)
			{
				return;
			}

			try
			{
            	if(session.IsTransacted && session.TransactionContext.InLocalTransaction)
				{
                	session.Commit();
            	}
				
				consumer.Close();
				consumer = null;
			}
			catch(NMSException e)
			{
				Tracer.Debug(e.StackTrace.ToString());
			}
		}

		public IEnumerator GetEnumerator()
		{
			CheckClosed();

			lock(myLock)
			{
				if(this.consumer == null)
				{
					this.consumer = CreateConsumer();
				}
			}

			return this;
		}


		private void CheckClosed()
		{
			if(this.closed)
			{
				throw new IllegalStateException("The Consumer is closed");
			}
		}

		public bool MoveNext()
		{
			while(true)
			{
				lock(myLock)
				{
					if(consumer == null)
					{
                        Tracer.Debug("QB-MoveNext: Consumer was null, returning false.");
						return false;
					}

					if(consumer.UnconsumedMessageCount > 0)
					{
                        Tracer.Debug("QB-MoveNext: Consumer has unconsumed Messages, returning true.");
						return true;
					}

					if(browseDone.Value || !session.Started)
					{
                        Tracer.Debug("QB-MoveNext: Browse done or session not started, return false.");
						DestroyConsumer();
						return false;
					}
				}

				WaitForMessage();
			}
		}

		public object Current
		{
			get
			{
				while(true)
				{
					lock(myLock)
					{
						if(consumer == null)
						{
							return null;
						}

						try
						{
							IMessage answer = consumer.ReceiveNoWait();

							if(answer != null)
							{
								return answer;
							}
						}
						catch(NMSException)
						{
							//TODO: Not implemented.
							//this.session.Connection.OnClientInternalException(e);
							return null;
						}

						if(browseDone.Value || !session.Started)
						{
							DestroyConsumer();
							return null;
						}
					}

					WaitForMessage();
				}
			}
		}

		public void Close()
		{
			lock(myLock)
			{
				if(this.closed)
				{
					return;
				}

				try
				{
					DestroyConsumer();
				}
				catch(Exception ex)
				{
					Tracer.ErrorFormat("Error during QueueBrowser close: {0}", ex);
				}
				finally
				{
					this.closed = true;
				}
			}
		}

		public IQueue Queue
		{
			get { return (IQueue)destination; }
		}

		public string MessageSelector
		{
			get { return selector; }
		}

		protected void WaitForMessage()
		{
			try
			{
				lock(semaphore)
				{
					Monitor.Wait(semaphore, 2000);
				}
			}
			catch(ThreadInterruptedException)
			{
				Thread.CurrentThread.Interrupt();
			}
		}

		protected void NotifyMessageAvailable()
		{
			lock(semaphore)
			{
				Monitor.PulseAll(semaphore);
			}
		}

		public override string ToString()
		{
			return "QueueBrowser { value=" + consumerId + " }";
		}

		public void Reset()
		{
			if(consumer != null)
			{
				DestroyConsumer();
			}

			consumer = CreateConsumer();
		}

		public class BrowsingMessageConsumer : MessageConsumer
		{
			private readonly QueueBrowser parent;

			public BrowsingMessageConsumer(QueueBrowser parent, Session session, ConsumerId id, ActiveMQDestination destination,
										   String name, String selector, int prefetch, int maxPendingMessageCount,
										   bool noLocal, bool browser, bool dispatchAsync)
				: base(session, id, destination, name, selector, prefetch, maxPendingMessageCount, noLocal, browser, dispatchAsync)
			{
				this.parent = parent;
			}

			public override void Dispatch(MessageDispatch md)
			{
				if(md.Message == null)
				{
                    Tracer.Debug("QueueBrowser recieved Null Message in Dispatch, Browse Done.");
					parent.browseDone.Value = true;
				}
				else
				{
                    Tracer.Debug("QueueBrowser dispatching next Message to Consumer.");
					base.Dispatch(md);
				}

				parent.NotifyMessageAvailable();
			}
		}
	}
}
