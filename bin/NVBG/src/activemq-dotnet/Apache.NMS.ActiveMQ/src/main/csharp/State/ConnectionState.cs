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
using System.Collections.Generic;
using Apache.NMS.ActiveMQ.Commands;
using Apache.NMS.Util;

namespace Apache.NMS.ActiveMQ.State
{
	public class ConnectionState
	{
		private ConnectionInfo info;
		private readonly AtomicDictionary<TransactionId, TransactionState> transactions = 
			new AtomicDictionary<TransactionId, TransactionState>();
		private readonly AtomicDictionary<SessionId, SessionState> sessions = 
			new AtomicDictionary<SessionId, SessionState>();
		private readonly AtomicCollection<DestinationInfo> tempDestinations = 
			new AtomicCollection<DestinationInfo>();
		private readonly Atomic<bool> isShutdown = new Atomic<bool>(false);
	    private bool connectionInterruptProcessingComplete = true;
		private readonly Dictionary<ConsumerId, ConsumerInfo> recoveringPullConsumers = 
			new Dictionary<ConsumerId, ConsumerInfo>();

		public ConnectionState(ConnectionInfo info)
		{
			this.info = info;
			// Add the default session id.
			AddSession(new SessionInfo(info, -1));
		}

		public override String ToString()
		{
			return info.ToString();
		}

		public void Reset(ConnectionInfo info)
		{
			this.info = info;
			transactions.Clear();
			sessions.Clear();
			tempDestinations.Clear();
			isShutdown.Value = false;
		}

		public void AddTempDestination(DestinationInfo info)
		{
			CheckShutdown();
			tempDestinations.Add(info);
		}

		public void RemoveTempDestination(IDestination destination)
		{
			for(int i = tempDestinations.Count - 1; i >= 0; i--)
			{
				DestinationInfo di = tempDestinations[i];
				if(di.Destination.Equals(destination))
				{
					tempDestinations.RemoveAt(i);
				}
			}
		}

		public void AddTransactionState(TransactionId id)
		{
			CheckShutdown();
			TransactionState transactionState = new TransactionState(id);

			if(transactions.ContainsKey(id))
			{
				transactions[id] = transactionState;
			}
			else
			{
				transactions.Add(id, transactionState);
			}
		}

		public TransactionState this[TransactionId id]
		{
			get
			{
				TransactionState state = null;

				transactions.TryGetValue(id, out state);
				return state;
			}
		}

		public AtomicCollection<TransactionState> TransactionStates
		{
			get { return transactions.Values; }
		}

		public SessionState this[SessionId id]
		{
			get
			{
				SessionState sessionState = null;

				sessions.TryGetValue(id, out sessionState);

#if DEBUG
				if(null == sessionState)
				{
					// Useful for dignosing missing session ids
					string sessionList = string.Empty;
					foreach(SessionId sessionId in sessions.Keys)
					{
						sessionList += sessionId.ToString() + "\n";
					}

					System.Diagnostics.Debug.Assert(false,
						string.Format("Session '{0}' did not exist in the sessions collection.\n\nSessions:-\n{1}", id, sessionList));
				}
#endif
				return sessionState;
			}
		}

		public TransactionState RemoveTransactionState(TransactionId id)
		{
			TransactionState ret = null;

			if(transactions.TryGetValue(id, out ret))
			{
				transactions.Remove(id);
			}

			return ret;
		}

		public void AddSession(SessionInfo info)
		{
			CheckShutdown();
			SessionState sessionState = new SessionState(info);

			if(sessions.ContainsKey(info.SessionId))
			{
				sessions[info.SessionId] = sessionState;
			}
			else
			{
				sessions.Add(info.SessionId, sessionState);
			}
		}

		public SessionState RemoveSession(SessionId id)
		{
			SessionState ret = null;

			if(sessions.TryGetValue(id, out ret))
			{
				sessions.Remove(id);
			}

			return ret;
		}

		public ConnectionInfo Info
		{
			get { return info; }
		}

		public AtomicCollection<SessionId> SessionIds
		{
			get { return sessions.Keys; }
		}

		public AtomicCollection<DestinationInfo> TempDestinations
		{
			get { return tempDestinations; }
		}

		public AtomicCollection<SessionState> SessionStates
		{
			get { return sessions.Values; }
		}

		private void CheckShutdown()
		{
			if(isShutdown.Value)
			{
				throw new ApplicationException("Disposed");
			}
		}

		public Dictionary<ConsumerId, ConsumerInfo> RecoveringPullConsumers
		{
			get { return this.recoveringPullConsumers; }
		}
		
		public bool ConnectionInterruptProcessingComplete
		{
			get { return this.connectionInterruptProcessingComplete; }
			set { this.connectionInterruptProcessingComplete = value; }
		}

		public void Shutdown()
		{
			if(isShutdown.CompareAndSet(false, true))
			{
				foreach(SessionState ss in sessions.Values)
				{
					ss.Shutdown();
				}
			}
		}
	}
}
